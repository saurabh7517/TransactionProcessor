package org.payment.processor.etcd.service;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.Lock;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.lock.LockResponse;
import org.payment.processor.config.SpringConfiguration;
import org.payment.processor.etcd.EtcdConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class EtcdLockService implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(EtcdLockService.class);

    private final Client client;
    private long leaseId;
    private ByteSequence lockKey;
    private SpringConfiguration springConfiguration;

    public EtcdLockService(Client client, SpringConfiguration springConfiguration) {
        this.client = client;
        this.springConfiguration = springConfiguration;
    }

    public boolean tryLock(final String msgId) {
        try {
            //create the key
            final String etcdLockKey = EtcdConstants.getLockKey(msgId);
            final ByteSequence key = ByteSequence.from(etcdLockKey.getBytes(StandardCharsets.UTF_8));
            final ByteSequence value = ByteSequence.from(springConfiguration.getAppInstanceId().getBytes(StandardCharsets.UTF_8));
            this.lockKey = key;

            //create the lease
            final Lease leaseClient = client.getLeaseClient();
            //response for lease creation with etcd cluster ( should be handle with care in PROD code ), this is a blocking request
            final LeaseGrantResponse leaseResponse = leaseClient.grant(springConfiguration.getLockTTL(), 5, TimeUnit.SECONDS).get();
            //if grant response is success, then get the leaseId
            this.leaseId = leaseResponse.getID();

            //create lock with the lease
            final Lock lockClient = client.getLockClient();
            //response for lock creation with etcd cluster, blocking request
            final CompletableFuture<LockResponse> futureLockResponse = lockClient.lock(key, leaseId);
            //get the response from completable future
            futureLockResponse.get(2, TimeUnit.SECONDS);
            log.info("Etcd lock acquired for key: {}", etcdLockKey);
            return true;
        } catch (Exception e) {
            log.error("Etcd lock could not be acquired", e);
            return false;
        }
    }

    @Override
    public void close() throws Exception {
        if(lockKey != null && leaseId != -1) {
            client.getLockClient().unlock(lockKey).get();
            log.info("Lock finally released for key: {}", lockKey.toString(StandardCharsets.UTF_8));
            client.getLeaseClient().revoke(leaseId).get();
            log.info("Lease revoked for leaseId: {}", leaseId);
        } else {
            log.error("Etcd lock could not be released");
        }

    }
}
