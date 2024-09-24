package searchengine.dao.repository.site;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import searchengine.dao.model.Page;
import searchengine.dao.model.Site;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class CustomSiteRepositoryImpl implements CustomSiteRepository {

    private final EntityManager entityManager;


    @Override
    public List<Site> findAllByName(Set<String> names) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Site> query = cb.createQuery(Site.class);
        Root<Site> root = query.from(Site.class);
        query.where(createPredicate(cb, root, names));

        TypedQuery<Site> finalQuery = entityManager.createQuery(query);
//        EntityGraph<?> graph = entityManager.getEntityGraph("Site.withAllPages");

//        finalQuery.setHint("jakarta.persistence.loadgraph",graph);


        return finalQuery.getResultList();
    }

    @Override
    public void deleteAllInfoBySite(Site site) {
        Integer siteId = site.getId();
        List<Page> pagesBySite = entityManager.createQuery("SELECT p FROM Page p WHERE p.site.id = :siteId", Page.class)
                .setParameter("siteId", siteId).getResultList();

        timeForIndex(() -> {
                    for (Page page : pagesBySite) {
                        Integer id = page.getId();
                        int batchSize = 1000;
                        int batchCounter = 0;
                        int counter = 1;
                        while (counter != 0) {
                            int i = entityManager.createQuery("DELETE FROM Index i WHERE i.page.id = :pageId")
                                    .setParameter("pageId", id)
                                    .executeUpdate();
                            batchCounter++;
                            counter = i;
                            if (batchCounter == batchSize) {
                                entityManager.flush();
                                entityManager.clear();
                            }
                        }
                        entityManager.flush();
                        entityManager.clear();
                    }
                });


        timeForLemma(() -> {
            entityManager.createNativeQuery("ALTER TABLE lemma DISABLE TRIGGER ALL").executeUpdate();
            int batchSize = 1000;
            int expectedCount = 0;
            int result;
            do {
                result = entityManager.createNativeQuery("DELETE FROM lemma AS l WHERE l.site_id = :siteId")
                        .setParameter("siteId", siteId)
                        .setMaxResults(batchSize)
                        .executeUpdate();

                entityManager.flush();
                entityManager.clear();
            } while (result != expectedCount);
            entityManager.flush();
            entityManager.clear();
            entityManager.createNativeQuery("ALTER TABLE lemma ENABLE TRIGGER ALL").executeUpdate();
        });

        entityManager.createQuery("DELETE FROM Statistic st WHERE st.site.id = :siteId")
                .setParameter("siteId", siteId)
                .executeUpdate();
        System.out.println("Статистика удалена");

        entityManager.createQuery("DELETE FROM Page p WHERE p.site.id = :siteId")
                .setParameter("siteId", siteId)
                .executeUpdate();
        System.out.println("Страницы удалены");

        entityManager.createQuery("DELETE FROM Site s WHERE s.id = :siteId")
                .setParameter("siteId", siteId)
                .executeUpdate();
        System.out.println("Сайт удален");


    }

    static void timeForLemma(Runnable runnable){
        long start = System.currentTimeMillis();
        runnable.run();
        long finish = System.currentTimeMillis();
        System.out.println("Леммы удалены за: " + (finish - start));
    }

    static void timeForIndex(Runnable runnable){
        long start = System.currentTimeMillis();
        runnable.run();
        long finish = System.currentTimeMillis();
        System.out.println("Индексы удалены за: " + (finish - start));
    }


    private Predicate createPredicate(CriteriaBuilder cb, Root<Site> root, Set<String> names) {
        List<Predicate> predicates = new ArrayList<>();
        for (String name : names) {
            Predicate predicate = cb.equal(root.get("name"), name);
            predicates.add(predicate);
        }
        Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);
        return cb.or(predicatesArray);
    }
}
