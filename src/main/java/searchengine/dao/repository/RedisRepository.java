package searchengine.dao.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RedisRepository {

    private final RedisTemplate<String, String> redis;

    public void saveUsePage(String siteName, String usePageUrl){
        redis.opsForList().leftPush(siteName,usePageUrl);
    }

    public List<String> getUserPages(String siteName){
        return redis.opsForList().range(siteName, 0, -1);
    }
}
