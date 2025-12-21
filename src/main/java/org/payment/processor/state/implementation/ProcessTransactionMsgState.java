package org.payment.processor.state.implementation;

import org.payment.processor.domain.Msg;
import org.payment.processor.domain.MsgState;
import org.payment.processor.domain.Transaction;
import org.payment.processor.service.StatePublisherService;
import org.payment.processor.state.TransactionStateMachine;
import org.payment.processor.state.behaviour.IMsgState;
import org.payment.processor.utility.SleepingThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ProcessTransactionMsgState implements IMsgState {

    private static final Logger log = LoggerFactory.getLogger(ProcessTransactionMsgState.class);

    private final TransactionStateMachine transactionStateMachine;
    private final StatePublisherService statePublisherService;

    public ProcessTransactionMsgState(TransactionStateMachine transactionStateMachine, StatePublisherService statePublisherService) {
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
        msg.setState(MsgState.PROCESSING_INITIATED);
        // add to redis and etcd
        statePublisherService.publishState(msg);
        // call API
        boolean isResponseReceived = true; // simulate API response
        final String msgId = msg.getMsgId();
        for (Transaction transaction : msg.getTransactions()) {
            if (isTransactionNotProcessed(transaction.getTransactionId(), msgId)) {
                log.info("Processing transaction ID: {} for message ID: {}", transaction.getTransactionId(), msgId);
                SleepingThread.sleepForMillis(1000);
                if(isResponseReceived) {
                    transaction.setState(MsgState.PROCESSING_SUCCESS);
                    log.info("Successful response for Transaction ID: {} for message ID: {}", transaction.getTransactionId(), msgId);
                    // add to redis and etcd
                    statePublisherService.publishState(msg);
                } else {
                    transaction.setState(MsgState.PROCESSING_FAILED);
                    log.info("Failed response for Transaction ID: {} for message ID: {}", transaction.getTransactionId(), msgId);
                    // add to redis and etcd
                    statePublisherService.publishState(msg);
                }
            }
        }
        msg.setState(MsgState.PROCESSING_COMPLETED);
        // add to redis and etcd
        statePublisherService.publishState(msg);
        // set next state
        transactionStateMachine.setState(transactionStateMachine.getAggregateTransactionMsgState());
    }

    @Override
    public void aggregateAllTransactions(Msg msg) {
        log.info("Cannot aggregate transactions. Message with ID: {} is still in {} state.", msg.getMsgId(), msg.getState());
    }

    @Override
    public void sendTransactionResponse(Msg msg) {
        log.info("Cannot send transaction response. Message with ID: {} is still in {} state.", msg.getMsgId(), msg.getState());
    }

    private boolean isTransactionNotProcessed(final String incomingTransactionId, final String msgId) {
        List<Msg> msgStatusList = statePublisherService.getAllTransactionFromStack(msgId);
        if (msgStatusList.isEmpty()) {
            return true;
        }
        for (Msg msg : msgStatusList) {
            List<Transaction> transactions = msg.getTransactions();
            for (Transaction transactionFromStack : transactions) {
                if(transactionFromStack.getTransactionId().equals(incomingTransactionId) && transactionFromStack.getState() == MsgState.PROCESSING_SUCCESS) {
                    return false;
                }
            }
        }
        return true;
    }

}
