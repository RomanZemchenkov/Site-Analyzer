package searchengine.dao.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfiguration {


    @Bean(name = "redisConnectionFactory")
    public RedisConnectionFactory redisConnectionFactory(){
        return new LettuceConnectionFactory();
    }

    @Bean(name = "redisTemplate")
    public RedisTemplate<String,Object> redisTemplate(){
        RedisTemplate<String, Object> template = new RedisTemplate<>();

        template.setConnectionFactory(redisConnectionFactory());

        template.setValueSerializer(new StringRedisSerializer());
        template.setKeySerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }
}
