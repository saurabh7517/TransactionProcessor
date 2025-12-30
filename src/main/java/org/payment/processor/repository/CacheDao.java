package org.payment.processor.repository;

import java.util.List;

public interface CacheDao {
    void pushOnStack(String key, Object value);
    Object popFromStack(String key);
    List<Object> getAllEntriesFromStack(String key);
}
