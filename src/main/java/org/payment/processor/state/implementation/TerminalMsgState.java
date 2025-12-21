package org.payment.processor.state.implementation;

import org.payment.processor.domain.Msg;
import org.payment.processor.service.StatePublisherService;
import org.payment.processor.state.TransactionStateMachine;
import org.payment.processor.state.behaviour.IMsgState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TerminalMsgState implements IMsgState {

    private static final Logger log = LoggerFactory.getLogger(TerminalMsgState.class);

    private final TransactionStateMachine transactionStateMachine;
    private final StatePublisherService statePublisherService;

    public TerminalMsgState(TransactionStateMachine transactionStateMachine, StatePublisherService statePublisherService) {
        this.transactionStateMachine = transactionStateMachine;
        this.statePublisherService = statePublisherService;
    }

    @Override
    public void receiveMsg(Msg msg) {

    }

    @Override
    public void validateMsg(Msg msg) {

    }

    @Override
    public void createRequestForTransaction(Msg msg) {

    }

    @Override
    public void processTransaction(Msg msg) {

    }

    @Override
    public void aggregateAllTransactions(Msg msg) {

    }

    @Override
    public void sendTransactionResponse(Msg msg) {

    }
}
