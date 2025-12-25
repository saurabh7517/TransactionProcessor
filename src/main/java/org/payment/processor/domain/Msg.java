package org.payment.processor.domain;

import java.util.List;

public class Msg {
    private String msgId;
    private List<Transaction> transactions;
    private MsgState state;

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public MsgState getState() {
        return state;
    }

    public void setState(MsgState state) {
        this.state = state;
    }
}
