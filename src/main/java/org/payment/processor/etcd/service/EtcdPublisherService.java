package org.payment.processor.etcd.service;

import io.etcd.jetcd.Client;
import org.payment.processor.etcd.EtcdConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.etcd.jetcd.ByteSequence;
import org.springframework.stereotype.Service;

@Service
public class EtcdPublisherService {

    private static final Logger log = LoggerFactory.getLogger(EtcdPublisherService.class);

    private final Client client;

    public EtcdPublisherService(Client client) {
        this.client = client;
    }

    public void publishState(final String msgId, final String state) {
        final String eventKey = EtcdConstants.getEventKey(msgId);
        ByteSequence key = ByteSequence.from(eventKey.getBytes());
        ByteSequence value = ByteSequence.from(state.getBytes());

        client.getKVClient().put(key, value).join();
    }

    public void unPublishState(final String msgId) {
        final String eventKey = EtcdConstants.getEventKey(msgId);
        ByteSequence key = ByteSequence.from(eventKey.getBytes());

        client.getKVClient().delete(key).join();
    }
}
