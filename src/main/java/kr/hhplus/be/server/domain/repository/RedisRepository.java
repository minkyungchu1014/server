package kr.hhplus.be.server.domain.repository;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface RedisRepository {
    void addToQueue(String key, String value, double score);
    void removeFromQueue(String key, String value);
    Set<String> getQueue(String key);
    String getValue(String key);
    void setValue(String key, String value, long timeout, TimeUnit unit);
}
