package searchengine.dao.repository.statistic;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dao.model.Site;
import searchengine.dao.model.Statistic;
import searchengine.dao.model.Status;


import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;


@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomStatisticRepositoryImpl implements CustomStatisticRepository {

    private final EntityManager entityManager;

    @Transactional()
    public Statistic readStatisticBySiteId(Site site){
        return mergeOrPersistStatistic(site);
    }

    @Transactional
    public void writeStatistics(){
        List<Site> allSites = entityManager.createQuery("SELECT s FROM Site s", Site.class).getResultList();
        for(Site site : allSites){
            mergeOrPersistStatistic(site);
        }

    }

    public Tuple readDetailInformation(Site site) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);
        Root<Site> root = query.from(Site.class);

        Join<Site, Statistic> statistic = root.join("statistic", JoinType.LEFT);

        Path<String> url = root.get("url");
        Path<String> name = root.get("name");
        Path<Status> status = root.get("status");
        Path<String> lastError = root.get("lastError");
        Path<OffsetDateTime> statusTime = root.get("statusTime");
        Path<Long> pages = statistic.get("countOfPages");
        Path<Long> lemmas = statistic.get("countOfLemmas");

        query.multiselect(url,name,status,lastError,statusTime,pages,lemmas);
        query.where(cb.equal(root.get("id"),site.getId()));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public void deleteAllBySite(Site site) {
        Integer siteId = site.getId();
        entityManager.createQuery("DELETE FROM Statistic st WHERE st.site.id = :siteId")
                .setParameter("siteId", siteId)
                .executeUpdate();
        System.out.println("Статистика удалена");
    }

    private Statistic mergeOrPersistStatistic(Site site){
        Integer siteId = site.getId();
        Optional<Statistic> mayBeStatistic = Optional.ofNullable(entityManager.find(Statistic.class, siteId));

        Long countOfPages = entityManager.createQuery("SELECT COUNT(p) FROM Page p WHERE p.site.id = :id", Long.class)
                .setParameter("id", siteId)
                .getSingleResult();

        Long countOfLemmas = entityManager.createQuery("SELECT COUNT(l) FROM Lemma l WHERE l.site.id = :id", Long.class)
                .setParameter("id", siteId)
                .getSingleResult();

        Statistic statistic;
        if(mayBeStatistic.isPresent()){
            statistic = mayBeStatistic.get();
            statistic.setCountOfLemmas(countOfLemmas);
            statistic.setCountOfPages(countOfPages);
            entityManager.merge(statistic);
        } else {
            statistic = new Statistic(countOfPages,countOfLemmas,site);
            entityManager.persist(statistic);
        }
        return statistic;
    }

}
