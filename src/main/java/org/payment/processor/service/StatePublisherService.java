package org.payment.processor.service;

import org.payment.processor.domain.Msg;
import org.payment.processor.etcd.service.EtcdPublisherService;
import org.payment.processor.repository.RedisCacheImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class StatePublisherService {

    private static final Logger log = LoggerFactory.getLogger(StatePublisherService.class);

    private final RedisCacheImpl redisCacheImpl;
    private final EtcdPublisherService etcdPublisherService;
    private final ObjectMapper objectMapper;

    public StatePublisherService(RedisCacheImpl redisCacheImpl,
                                 EtcdPublisherService etcdPublisherService,
                                 ObjectMapper objectMapper) {
        this.redisCacheImpl = redisCacheImpl;
        this.etcdPublisherService = etcdPublisherService;
        this.objectMapper = objectMapper;
    }

    public void publishState(final Msg message) {
        try {
            final String json = objectMapper.writeValueAsString(message);
            redisCacheImpl.pushOnStack(message.getMsgId(), message);
            etcdPublisherService.publishState(message.getMsgId(), json);
        } catch (JacksonException e) {
            log.error(e.getMessage(), e);
        }
    }

    public List<Msg> getAllTransactionFromStack(final String msgId) {
        List<Object> stackObjectList = redisCacheImpl.getAllEntriesFromStack(msgId);
        List<Msg> stackMsgList = new ArrayList<>(stackObjectList.size());
        if(!stackObjectList.isEmpty()) {
            for (Object stackObject : stackObjectList) {
                if (stackObject instanceof Msg) {
                    stackMsgList.add((Msg) stackObject);
                }
            }
        }
        return stackMsgList;
    }
}
