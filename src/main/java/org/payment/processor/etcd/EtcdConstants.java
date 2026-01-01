package org.payment.processor.etcd;

public class EtcdConstants {
    private static final String LOCK = "/lock/";
    private static final String EVENT = "/event/";
    private static final String CLAIM = "/claim/";

    public static String getLockKey(final String messageId) {
        return LOCK + messageId;
    }

    public static String getEventKey(final String messageId) {
        return EVENT + messageId;
    }

    public static String getClaimKey(final String messageId) {
        return CLAIM + messageId;
    }
}
