package org.payment.processor.service;


import org.payment.processor.domain.Msg;
import org.payment.processor.domain.MsgState;
import org.payment.processor.domain.Transaction;
import org.payment.processor.dto.MsgDto;
import org.payment.processor.dto.TransactionDto;
import org.payment.processor.etcd.service.EtcdPublisherService;
import org.payment.processor.etcd.service.EtcdLockService;
import org.payment.processor.state.TransactionStateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    private final EtcdLockService etcdLockService;
    private final StatePublisherService statePublisherService;
    private final EtcdPublisherService etcdPublisherService;

    public MessageService(EtcdLockService etcdLockService,
                          StatePublisherService statePublisherService,
                          EtcdPublisherService etcdPublisherService) {
        this.etcdLockService = etcdLockService;
        this.statePublisherService = statePublisherService;
        this.etcdPublisherService = etcdPublisherService;
    }

    public void processMessage(MsgDto msgDto) throws Exception {
        log.info("Processing message with Id : {}", msgDto.getMsgId());
        Msg message = enrichData(msgDto);
        processEvent(message);
    }

    public void processEvent(Msg message) throws Exception {

        if(!etcdLockService.tryLock(message.getMsgId())) {
            log.warn("Lock already acquired for message Id : {}", message.getMsgId());
        } else {
            try {
                TransactionStateMachine transactionStateMachine = new TransactionStateMachine(message, statePublisherService);
                transactionStateMachine.validateMsg(message);
                transactionStateMachine.createRequest(message);
                transactionStateMachine.processTransaction(message);
                transactionStateMachine.aggregateTransaction(message);
                transactionStateMachine.sendResponse(message);
                etcdPublisherService.unPublishState(message.getMsgId());
            } catch (Exception e) {
                log.error("Error processing message", e);
            } finally {
                etcdLockService.close();
            }
        }
    }




    public Msg enrichData(MsgDto msgDto) {
        log.info("Enriching message : {}", msgDto.getMsgId());
        Msg msg = new Msg();
        msg.setMsgId(msgDto.getMsgId());
        List<Transaction> transactionList = new ArrayList<>();
        List<TransactionDto> transactionDtoList = msgDto.getTransactions();
        for (TransactionDto transactionDto : transactionDtoList) {
            Transaction transaction = new Transaction();
            transaction.setTransactionId(transactionDto.getTransactionId());
            transaction.setSenderAccount(transactionDto.getSenderAccount());
            transaction.setReceiverAccount(transactionDto.getReceiverAccount());
            transaction.setAmount(transactionDto.getAmount());
            transaction.setState(MsgState.RECEIVED);
            transactionList.add(transaction);
        }
        msg.setTransactions(transactionList);
        msg.setState(MsgState.RECEIVED);
        statePublisherService.publishState(msg);
        return msg;
    }
}
