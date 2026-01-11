package org.payment.processor.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.TxnResponse;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.op.Cmp;
import io.etcd.jetcd.op.CmpTarget;
import io.etcd.jetcd.op.Op;
import io.etcd.jetcd.options.PutOption;
import io.netty.util.internal.StringUtil;
import org.payment.processor.config.SpringConfiguration;
import org.payment.processor.domain.Msg;
import org.payment.processor.domain.MsgState;
import org.payment.processor.etcd.EtcdConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class ObserverService {

    private static final Logger log = LoggerFactory.getLogger(ObserverService.class);

    private final Client client;
    private final MessageService messageService;
    private final SpringConfiguration springConfiguration;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ObserverService(Client client,
                           MessageService messageService,
                           SpringConfiguration springConfiguration) {
        this.client = client;
        this.messageService = messageService;
        this.springConfiguration = springConfiguration;
    }

    public void observe(String lockKey) {
        final String[] eventKey = lockKey.split("/");
        final String  msgId = eventKey[2];

        //Setting claim on the released lock
        final ByteSequence claimKey = ByteSequence.from(EtcdConstants.getClaimKey(msgId).getBytes(StandardCharsets.UTF_8));
        final ByteSequence claimValue = ByteSequence.from(springConfiguration.getAppInstanceId().getBytes(StandardCharsets.UTF_8));
        final KV kvClient = client.getKVClient();

        if(isEventKeyPresentOnCluster(msgId) != null) {
            try {
                final Txn txn = kvClient.txn();

                final GetResponse claimKeyResponse = kvClient.get(claimKey).get();
                long claimKeyVersion = 0;
                if (!claimKeyResponse.getKvs().isEmpty()) {
                    claimKeyVersion = claimKeyResponse.getKvs().get(0).getVersion();
                }
                log.info("Claim version : {} for claim key : {}", claimKeyVersion, claimKey);

                //Create lease
                final Lease leaseClient = client.getLeaseClient();
                //response for lease id creation with etcd cluster ( should be handle with care in PROD code ), this is a blocking request
                final LeaseGrantResponse leaseResponse = leaseClient.grant(springConfiguration.getLockTTL(), 5, TimeUnit.SECONDS).get();
                //if grant response is success, then get the leaseId
                final long leaseId = leaseResponse.getID();
                log.info("Lease id : {} for claim key : {}", leaseId, claimKey);

                //Check if the claim key doesnot exist, i.e. if it is version 0, then only create the claim key
                //If the version is not 0, then some other instance has already created the claim key
                txn.If(new Cmp(claimKey, Cmp.Op.EQUAL, CmpTarget.version(0)))
                        .Then(Op.put(claimKey, claimValue, PutOption.builder().withLeaseId(leaseId).build()))
                        .Else(); //Do nothing if claim key already exists

                TxnResponse txnResponse = txn.commit().get();
                if (txnResponse.isSucceeded()) {
                    Msg msg = isEventKeyPresentOnCluster(msgId);
                    if (msg != null) {
                        messageService.processEvent(msg);
                    } else {
                        log.warn("Message Id : {} is completely processed", msgId);
                    }
                } else {
                    log.info("Claim key: {} already exists. Another instance is already processing msgId: {}", claimKey, msgId);
                }
            } catch (JsonProcessingException e) {
                log.error(e.getMessage(), e);
            } catch (Exception e) {
                log.error("Etcd observer encountered an error while processing msgId: {}", msgId, e);
            } finally {
                kvClient.delete(claimKey).join();
                log.info("Claim key: {} deleted from etcd cluster", claimKey);
            }
        }


    }

    private Msg isEventKeyPresentOnCluster(final String msgId) {
        final String eventKey = EtcdConstants.getEventKey(msgId);
        final ByteSequence bsEventKey = ByteSequence.from(eventKey.getBytes(StandardCharsets.UTF_8));
        try {
            CompletableFuture<GetResponse> futureResponse = client.getKVClient().get(bsEventKey);
            final GetResponse response = futureResponse.get();
            List<KeyValue> keyValueList = response.getKvs();
            if (keyValueList != null && !keyValueList.isEmpty() && StringUtil.isNullOrEmpty(keyValueList.get(0).getValue().toString())) {
                final String value = keyValueList.get(0).getValue().toString();
                Msg msg = objectMapper.readValue(value, Msg.class);
                if (msg.getState() != MsgState.RESPONSE_SENT || msg.getState() != MsgState.EXCEPTION || msg.getState() != MsgState.PROCESSING_FAILED) {
                    return msg;
                }
            }
        } catch (InterruptedException e) {
            log.error("Etcd observer interrupted while checking event key presence for msgId: {}", msgId, e);
        } catch (ExecutionException e) {
            log.error("Etcd observer encountered an error while checking event key presence for msgId: {}", msgId, e);
        } catch (Exception e) {
            log.error("Etcd observer encountered an error while deserializing message for msgId: {}", msgId, e);
        }
        return null;
    }
}
