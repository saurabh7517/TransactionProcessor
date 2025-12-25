package org.payment.processor.dto;

import org.payment.processor.domain.Status;

import java.util.List;

public class MsgDto {
    private String msgId;
    private List<TransactionDto> transactions;
    private Status state;

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public List<TransactionDto> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionDto> transactions) {
        this.transactions = transactions;
    }

    public Status getState() {
        return state;
    }

    public void setState(Status state) {
        this.state = state;
    }
}
