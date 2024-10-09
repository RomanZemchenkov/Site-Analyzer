package searchengine.dao.repository.lemma;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import searchengine.aop.annotation.CheckTimeWorking;
import searchengine.dao.model.Lemma;
import searchengine.dao.model.Site;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static searchengine.dao.repository.lemma.LemmaSql.*;

@Repository
@RequiredArgsConstructor
public class CustomLemmaRepositoryImpl implements CustomLemmaRepository {

    private final JdbcTemplate template;
    private final NamedParameterJdbcTemplate namedTemplate;
    private final EntityManager entityManager;

    @Override
    public List<Lemma> batchSave(List<Lemma> lemmaList) {
        return save(lemmaList);
    }

    @Override
    public void checkExistAndSaveOrUpdate(List<Lemma> lemmaList, Site site) {
        List<Lemma> newLemmas = new ArrayList<>();
        List<Lemma> lemmasForUpdate = new ArrayList<>();
        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue("id", site.getId());
        for (Lemma mayBeExistLemma : lemmaList) {
            source.addValue("lemma", mayBeExistLemma.getLemma());

            Optional<Lemma> mayBeLemma = Optional.ofNullable(namedTemplate.query(SELECT_LEMMA_BY_SITE_ID_AND_LEMMA, source, (rs) -> {
                Lemma lemma = null;
                while (rs.next()) {
                    lemma = lemmaBuilder(rs, site);
                }
                return lemma;
            }));

            mayBeLemma.ifPresentOrElse(
                    lemma -> {
                        mayBeExistLemma.setId(lemma.getId());
                        mayBeExistLemma.setFrequency(lemma.getFrequency() + 1);
                        lemmasForUpdate.add(mayBeExistLemma);
                    },
                    () -> newLemmas.add(mayBeExistLemma));
        }
        save(newLemmas);
        update(lemmasForUpdate);
    }

    @Override
    @CheckTimeWorking
    public void deleteAllBySite(Site site) {
        Integer siteId = site.getId();
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
    }

    @Override
    @CheckTimeWorking
    public List<Lemma> findAllBySiteIdAndLemmasByMaxFrequency(Site site, Set<String> lemmas,float maxFrequency) {
        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue("id", site.getId());
        source.addValue("lemmas", lemmas);
        source.addValue("maxFrequency",maxFrequency);
        return namedTemplate.query(SELECT_LEMMA_BY_SITE_ID_AND_LEMMA_SQL_AND_FREQUENCY, source,
                (rs) -> {
                    List<Lemma> lemmaList = new ArrayList<>();
                    while (rs.next()) {
                        Lemma lemma = lemmaBuilder(rs, site);
                        lemmaList.add(lemma);
                    }
                    return lemmaList;
                });
    }


    private void update(List<Lemma> lemmaList) {
        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(lemmaList.toArray());
        namedTemplate.batchUpdate(UPDATE_FREQUENCY_FOR_EXIST_LEMMAS_SQL, batch);
    }

    private List<Lemma> save(List<Lemma> lemmaList) {
        for (Lemma lemma : lemmaList) {
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            template.update((con) -> {
                PreparedStatement ps = con.prepareStatement(LEMMA_SAVE_SQL, new String[]{"id"});
                ps.setInt(1, lemma.getSite().getId());
                ps.setString(2, lemma.getLemma());
                ps.setInt(3, lemma.getFrequency());
                return ps;
            }, keyHolder);
            Integer key = keyHolder.getKey().intValue();
            lemma.setId(key);
        }
        return lemmaList;
    }

    private Lemma lemmaBuilder(ResultSet rs, Site site) throws SQLException {
        Lemma lemma = new Lemma();
        int id = rs.getInt("id");
        String lemmaByString = rs.getString("lemma");
        int frequency = rs.getInt("frequency");

        lemma.setId(id);
        lemma.setLemma(lemmaByString);
        lemma.setFrequency(frequency);
        lemma.setSite(site);
        return lemma;
    }


}
