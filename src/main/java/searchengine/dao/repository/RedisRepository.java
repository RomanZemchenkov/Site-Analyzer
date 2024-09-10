package searchengine.dao.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RedisRepository {

    private final RedisTemplate<String, Object> redis;
    @Value("${spring.data.redis.live-time}")
    private long redisLiveTime;

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


//    public synchronized void saveUseLemma(String siteName, Lemma lemma) {
//        String lemmaByString;
//        String mapName = "mapOf" + siteName;
//        try {
//            lemmaByString = mapper.writeValueAsString(lemma);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//        lemma.setFrequency(lemma.getFrequency() + INCREMENT);
//        try {
//            redis.opsForHash().put(mapName, lemma.getLemma(), lemmaByString);
//        } catch (Throwable throwable){
//
//        }
//        redis.expire(mapName, redisLiveTime, TimeUnit.SECONDS);
//    }
//
//    public Optional<Lemma> getLemma(String siteName, String lemma) {
//        String mapName = "mapOf" + siteName;
//        Optional<String> mayBeLemma = Optional.ofNullable((String) redis.opsForHash().get(mapName, lemma));
//        Optional<Lemma> lemmaByObject;
//        if (mayBeLemma.isEmpty()) {
//            lemmaByObject = Optional.empty();
//        } else {
//            try {
//                lemmaByObject = Optional.ofNullable(mapper.readValue(mayBeLemma.get(), Lemma.class));
//            } catch (JsonProcessingException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        return lemmaByObject;
//    }
//
//    public List<Lemma> getAllLemmasOnSite(String siteName){
//        String mapName = "mapOf" + siteName;
//        List<Object> lemmasByObject = redis.opsForHash().values(mapName);
//        List<Lemma> lemmaList = new ArrayList<>();
//        for(Object o : lemmasByObject){
//            try {
//                lemmaList.add(mapper.readValue(o.toString(),Lemma.class));
//            } catch (JsonProcessingException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        return lemmaList;
//    }

    public void clearListByUrl(String siteUrl) {
        Set<String> keys = redis.keys(siteUrl);
        for(String key : keys){
            redis.delete(key);
        }
    }
}
