package org.payment.processor.state.implementation;

import org.payment.processor.domain.Msg;
import org.payment.processor.domain.MsgState;
import org.payment.processor.service.StatePublisherService;
import org.payment.processor.state.TransactionStateMachine;
import org.payment.processor.state.behaviour.IMsgState;
import org.payment.processor.utility.SleepingThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendResponseMsgState implements IMsgState {

    private static final Logger log = LoggerFactory.getLogger(SendResponseMsgState.class);

    private final TransactionStateMachine transactionStateMachine;
    private final StatePublisherService statePublisherService;

    public SendResponseMsgState(TransactionStateMachine transactionStateMachine, StatePublisherService statePublisherService) {
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
        log.info("Transactions already aggregated for message ID: {}", msg.getMsgId());
    }

    @Override
    public void sendTransactionResponse(Msg msg) {
        log.info("Sending response for message ID: {}", msg.getMsgId());
        // Simulate sending response
        SleepingThread.sleepForMillis(2000);
        msg.setState(MsgState.RESPONSE_SENT);
        // add to redis and etcd
        statePublisherService.publishState(msg);
        log.info("Response sent for message ID: {}", msg.getMsgId());
    }
}
