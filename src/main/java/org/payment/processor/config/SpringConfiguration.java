package org.payment.processor.config;


import io.etcd.jetcd.Client;
import org.payment.processor.domain.Msg;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class SpringConfiguration {

    private String etcdEndpoints;

    private String appInstanceId;

    private long lockTTL;

    private long cliamTTL;

    @Bean
    public Client etcdClient() {
        return Client.builder().endpoints(etcdEndpoints).build();
    }

    @Bean
    public String getAppInstanceId() {
        return String.valueOf(appInstanceId);
    }

    @Bean
    public long getLockTTL() {
        return lockTTL;
    }

    @Bean
    public long getClaimTTL() {
        return cliamTTL;
    }

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
        jedisConnectionFactory.setHostName("localhost");
        jedisConnectionFactory.setPort(6379);
        return jedisConnectionFactory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setValueSerializer(new JacksonJsonRedisSerializer<>(Msg.class));
        template.setKeySerializer(new StringRedisSerializer());
        return template;
    }
}
