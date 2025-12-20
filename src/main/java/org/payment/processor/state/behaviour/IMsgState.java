package org.payment.processor.state.behaviour;

import org.payment.processor.domain.Msg;

public interface IMsgState {
    void receiveMsg(Msg msg);
    void validateMsg(Msg msg);
    void createRequestForTransaction(Msg msg);
    void processTransaction(Msg msg);
    void aggregateAllTransactions(Msg msg);
    void sendTransactionResponse(Msg msg);
}
