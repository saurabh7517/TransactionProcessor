package org.payment.processor.state;

import org.payment.processor.domain.Msg;
import org.payment.processor.service.StatePublisherService;
import org.payment.processor.state.behaviour.IMsgState;
import org.payment.processor.state.implementation.*;

public class TransactionStateMachine {
    private IMsgState state;
    private IMsgState validationMsgState;
    private IMsgState requestCreationMsgState;
    private IMsgState processTransactionMsgState;
    private IMsgState aggregateTransactionMsgState;
    private IMsgState sendResponseMsgState;
    private IMsgState terminalMsgState;

    private StatePublisherService statePublisherService;

    public TransactionStateMachine(Msg msg, StatePublisherService statePublisherService) throws Exception {
        this.validationMsgState = new ValidationMsgState(this, statePublisherService);
        this.requestCreationMsgState = new RequestCreationMsgState(this, statePublisherService);
        this.processTransactionMsgState = new ProcessTransactionMsgState(this, statePublisherService);
        this.aggregateTransactionMsgState = new AggregateTransactionMsgState(this, statePublisherService);
        this.sendResponseMsgState = new SendResponseMsgState(this, statePublisherService);
        this.terminalMsgState = new TerminalMsgState(this, statePublisherService);

        switch (msg.getState()) {
            case VALIDATION_SUCCESS, REQUEST_CREATION_INITIATED -> this.state = requestCreationMsgState;
            case REQUEST_CREATION_COMPLETED -> this.state = processTransactionMsgState;
            case PROCESSING_COMPLETED -> this.state = aggregateTransactionMsgState;
            case VALIDATION_FAILED, PROCESSING_FAILED, EXCEPTION -> this.state = terminalMsgState;
            case AGGREGATED, PROCESSING_PARTIALLY_FAILED, PROCESSING_SUCCESS -> this.state = sendResponseMsgState;
            case RECEIVED -> this.state = validationMsgState;
            default -> throw new Exception("Invalid Msg State : " + msg.getState());
        }
    }

    public void validateMsg(Msg msg) {
        state.validateMsg(msg);
    }

    public void createRequest(Msg msg) {
        state.createRequestForTransaction(msg);
    }

    public void processTransaction(Msg msg) {
        state.processTransaction(msg);
    }

    public void aggregateTransaction(Msg msg) {
        state.aggregateAllTransactions(msg);
    }

    public void sendResponse(Msg msg) {
        state.sendTransactionResponse(msg);
    }

    public void setState(IMsgState state) {
        this.state = state;
    }

    public IMsgState getValidationMsgState() {
        return validationMsgState;
    }

    public IMsgState getRequestCreationMsgState() {
        return requestCreationMsgState;
    }

    public IMsgState getProcessTransactionMsgState() {
        return processTransactionMsgState;
    }

    public IMsgState getAggregateTransactionMsgState() {
        return aggregateTransactionMsgState;
    }

    public IMsgState getSendResponseMsgState() {
        return sendResponseMsgState;
    }

    public IMsgState getTerminalMsgState() {
        return terminalMsgState;
    }
}
