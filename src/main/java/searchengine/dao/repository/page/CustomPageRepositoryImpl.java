package searchengine.dao.repository.page;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import searchengine.dao.model.Site;

@RequiredArgsConstructor
@Repository
public class CustomPageRepositoryImpl implements CustomPageRepository{

    private final EntityManager entityManager;

    @Override
    public void deleteAllBySite(Site site) {
        Integer siteId = site.getId();
        entityManager.createQuery("DELETE FROM Page p WHERE p.site.id = :siteId")
                .setParameter("siteId", siteId)
                .executeUpdate();
    }
}
