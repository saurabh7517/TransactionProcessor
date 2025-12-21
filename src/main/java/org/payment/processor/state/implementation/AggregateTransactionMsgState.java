package org.payment.processor.state.implementation;

import org.payment.processor.domain.Msg;
import org.payment.processor.domain.MsgState;
import org.payment.processor.domain.Transaction;
import org.payment.processor.service.StatePublisherService;
import org.payment.processor.state.TransactionStateMachine;
import org.payment.processor.state.behaviour.IMsgState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AggregateTransactionMsgState implements IMsgState {

    private static final Logger log = LoggerFactory.getLogger(AggregateTransactionMsgState.class);

    private final TransactionStateMachine transactionStateMachine;
    private final StatePublisherService statePublisherService;

    public AggregateTransactionMsgState(TransactionStateMachine transactionStateMachine, StatePublisherService statePublisherService) {
        this.transactionStateMachine = transactionStateMachine;
        this.statePublisherService = statePublisherService;
    }

    @Override
    public void receiveMsg(Msg msg) {
        log.info("Message already received with ID: {}", msg.getMsgId());
    }

    @Override
    public void validateMsg(Msg msg) {
        log.info("Message with ID: {} has already been validated.", msg.getMsgId());
    }

    @Override
    public void createRequestForTransaction(Msg msg) {
        log.info("Request for transaction already created for message ID: {}", msg.getMsgId());
    }

    @Override
    public void processTransaction(Msg msg) {
        log.info("Transaction already processed for message ID: {}", msg.getMsgId());
    }

    @Override
    public void aggregateAllTransactions(Msg msg) {
        int countSuccess = 0;
        int countFailed = 0;
        int totalTransactions = msg.getTransactions().size();

        for (Transaction transaction : msg.getTransactions()) {
            if (transaction.getState() == MsgState.PROCESSING_SUCCESS) {
                countSuccess++;
            } else if (transaction.getState() == MsgState.PROCESSING_FAILED) {
                countFailed++;
            }
        }

        if(countSuccess == totalTransactions) {
            msg.setState(MsgState.PROCESSING_SUCCESS);
            log.info("All transactions processed successfully for message ID: {}", msg.getMsgId());
            // add to redis and etcd
            statePublisherService.publishState(msg);
            // set next state
            transactionStateMachine.setState(transactionStateMachine.getSendResponseMsgState());
        } else if (countFailed == totalTransactions) {
            msg.setState(MsgState.PROCESSING_FAILED);
            log.info("All transactions failed for message ID: {}", msg.getMsgId());
            // add to redis and etcd
            statePublisherService.publishState(msg);
            // set next state
            transactionStateMachine.setState(transactionStateMachine.getTerminalMsgState());
        } else {
            msg.setState(MsgState.PROCESSING_PARTIALLY_FAILED);
            log.info("Partial success in processing transactions for msg Id : {}, Success : {}, Failed : {}", msg.getMsgId(), countSuccess, countFailed);
            // add to redis and etcd
            statePublisherService.publishState(msg);
            // set next state
            transactionStateMachine.setState(transactionStateMachine.getSendResponseMsgState());
        }
    }

    @Override
    public void sendTransactionResponse(Msg msg) {
        log.info("Cannot send transaction response. Message with ID: {} is still in {} state.", msg.getMsgId(), msg.getState());
    }
}
