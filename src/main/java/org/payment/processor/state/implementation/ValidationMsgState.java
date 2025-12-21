package org.payment.processor.state.implementation;

import org.payment.processor.domain.Msg;
import org.payment.processor.domain.MsgState;
import org.payment.processor.service.StatePublisherService;
import org.payment.processor.state.TransactionStateMachine;
import org.payment.processor.state.behaviour.IMsgState;
import org.payment.processor.utility.SleepingThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidationMsgState implements IMsgState {

    private static final Logger log = LoggerFactory.getLogger(ValidationMsgState.class);

    private final TransactionStateMachine transactionStateMachine;
    private final StatePublisherService statePublisherService;

    public ValidationMsgState(TransactionStateMachine transactionStateMachine, StatePublisherService statePublisherService) {
        this.transactionStateMachine = transactionStateMachine;
        this.statePublisherService = statePublisherService;
    }

    @Override
    public void receiveMsg(Msg msg) {
        log.info("Message already received with ID: {}", msg.getMsgId());
    }

    @Override
    public void validateMsg(Msg msg) {
        log.info("Validating message with ID: {}", msg.getMsgId());
        SleepingThread.sleepForMillis(2000); // Simulate validation delay
        boolean isValidated = true;
        if(isValidated) {
            log.info("Message validated successfully for ID: {}", msg.getMsgId());
            msg.setState(MsgState.VALIDATION_SUCCESS);
            // store in redis and etcd
            statePublisherService.publishState(msg);
            // set the next state
            transactionStateMachine.setState(transactionStateMachine.getRequestCreationMsgState());
            log.info("Validation Complete");
        } else {
            msg.setState(MsgState.VALIDATION_FAILED);
            // store in redis and etcd
            statePublisherService.publishState(msg);
            // set the next state
            transactionStateMachine.setState(transactionStateMachine.getTerminalMsgState());
            log.error("Message validation failed for ID: {}", msg.getMsgId());
        }

    }

    @Override
    public void createRequestForTransaction(Msg msg) {
        log.info("Cannot create request. Message with ID: {} is still in validation state.", msg.getMsgId());
    }

    @Override
    public void processTransaction(Msg msg) {
        log.info("Cannot process transaction. Message with ID: {} is still in validation state.", msg.getMsgId());
    }

    @Override
    public void aggregateAllTransactions(Msg msg) {
        log.info("Cannot aggregate transactions. Message with ID: {} is still in validation state.", msg.getMsgId());
    }

    @Override
    public void sendTransactionResponse(Msg msg) {
        log.info("Cannot send response. Message with ID: {} is still in validation state.", msg.getMsgId());
    }
}
