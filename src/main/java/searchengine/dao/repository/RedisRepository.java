package searchengine.dao.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import searchengine.dao.model.Lemma;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RedisRepository {

    private final RedisTemplate<String, Object> redis;
    @Value("${spring.data.redis.live-time}")
    private long redisLiveTime;
    private final ObjectMapper mapper;

    public void saveUsePage(String siteName, String usePageUrl) {
        redis.opsForList().leftPush(siteName, usePageUrl);
        redis.expire(siteName, redisLiveTime, TimeUnit.SECONDS);
    }

    public List<String> getUsePages(String siteName) {
        return redis.opsForList().range(siteName, 0, -1)
                .stream()
                .map(url -> (String) url)
                .toList();
    }

    public void saveUseLemma(String siteName, Lemma lemma) {
        String lemmaByString;
        try {
            lemmaByString = mapper.writeValueAsString(lemma);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        redis.opsForHash().put(siteName, lemma.getLemma(), lemmaByString);
        redis.expire(siteName, redisLiveTime, TimeUnit.SECONDS);
    }

    public Optional<Lemma> getLemma(String siteName, String lemma) {
        Optional<String> mayBeLemma = Optional.ofNullable((String) redis.opsForHash().get(siteName, lemma));
        Optional<Lemma> lemmaByObject;
        if (mayBeLemma.isEmpty()) {
            lemmaByObject = Optional.empty();
        } else {
            try {
                lemmaByObject = Optional.ofNullable(mapper.readValue(mayBeLemma.get(), Lemma.class));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return lemmaByObject;
    }

    public void clearListByUrl(String siteUrl) {
        redis.delete(siteUrl);
    }
}
