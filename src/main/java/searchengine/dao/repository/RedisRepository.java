package searchengine.dao.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RedisRepository {

    private final RedisTemplate<String, Object> redis;
    @Value("${spring.data.redis.live-time}")
    private long redisLiveTime;

    public void saveUsePage(String siteName, String usePageUrl){
        redis.opsForList().leftPush(siteName,usePageUrl);
        redis.expire(siteName,redisLiveTime, TimeUnit.SECONDS);
    }

    public List<String> getUsePages(String siteName){
        return redis.opsForList().range(siteName, 0, -1)
                .stream()
                .map(url -> (String) url)
                .toList();
    }

    public void clearListByUrl(String siteUrl){
        redis.delete(siteUrl);
    }
}
