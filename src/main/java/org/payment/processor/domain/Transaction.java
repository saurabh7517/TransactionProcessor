package org.payment.processor.domain;

public class Transaction {
    private String transactionId;
    private String senderAccount;
    private String receiverAccount;
    private double amount;
    private MsgState state;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getSenderAccount() {
        return senderAccount;
    }

    public void setSenderAccount(String senderAccount) {
        this.senderAccount = senderAccount;
    }

    public String getReceiverAccount() {
        return receiverAccount;
    }

    public void setReceiverAccount(String receiverAccount) {
        this.receiverAccount = receiverAccount;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public MsgState getState() {
        return state;
    }

    public void setState(MsgState state) {
        this.state = state;
    }
}
