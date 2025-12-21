package org.payment.processor.state.implementation;

import org.payment.processor.domain.Msg;
import org.payment.processor.domain.MsgState;
import org.payment.processor.service.StatePublisherService;
import org.payment.processor.state.TransactionStateMachine;
import org.payment.processor.state.behaviour.IMsgState;
import org.payment.processor.utility.SleepingThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestCreationMsgState implements IMsgState {

    private static final Logger log = LoggerFactory.getLogger(RequestCreationMsgState.class);

    private final TransactionStateMachine transactionStateMachine;
    private final StatePublisherService statePublisherService;

    public RequestCreationMsgState(TransactionStateMachine transactionStateMachine, StatePublisherService statePublisherService) {
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
        msg.setState(MsgState.REQUEST_CREATION_INITIATED);
        // add to redis and etcd
        statePublisherService.publishState(msg);
        log.info("Request creation initiated for message ID: {}", msg.getMsgId());
        SleepingThread.sleepForMillis(2000);
        log.info("Request created successfully for message ID: {}", msg.getMsgId());
        //add to redis and etcd
        msg.setState(MsgState.REQUEST_CREATION_COMPLETED);
        statePublisherService.publishState(msg);
        // set next state
        transactionStateMachine.setState(transactionStateMachine.getProcessTransactionMsgState());

    }

    @Override
    public void processTransaction(Msg msg) {
        log.info("Cannot process transaction. Message with ID: {} is still in {} state.", msg.getMsgId(), msg.getState());
    }

    @Override
    public void aggregateAllTransactions(Msg msg) {
        log.info("Cannot aggregate transactions. Message with ID: {} is still in {} state.", msg.getMsgId(), msg.getState());
    }

    @Override
    public void sendTransactionResponse(Msg msg) {
        log.info("Cannot send transaction response. Message with ID: {} is still in {} state.", msg.getMsgId(), msg.getState());

    }
}
