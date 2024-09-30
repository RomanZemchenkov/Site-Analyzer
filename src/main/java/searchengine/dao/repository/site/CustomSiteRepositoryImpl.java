package searchengine.dao.repository.site;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
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

        return finalQuery.getResultList();
    }

    @Override
    public void deleteSite(Site site) {
        Integer siteId = site.getId();
        entityManager.createQuery("DELETE FROM Site s WHERE s.id = :siteId")
                .setParameter("siteId", siteId)
                .executeUpdate();
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
