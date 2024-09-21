package searchengine.dao.repository.lemma;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import searchengine.dao.model.Lemma;
import searchengine.dao.model.Site;

import java.sql.PreparedStatement;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class CustomLemmaRepositoryImpl implements CustomLemmaRepository {

    private final JdbcTemplate template;
    private final NamedParameterJdbcTemplate namedTemplate;
    private static final String LEMMA_SAVE_SQL = """
            INSERT INTO lemma(site_id, lemma, frequency) 
            VALUES (?,?,?);
            """;
    private static final String SELECT_LEMMA_BY_SITE_ID_AND_LEMMA_SQL =
            """
                    SELECT l.id,l.lemma,l.frequency
                    FROM lemma l
                    WHERE l.site_id = :id AND l.lemma IN(:lemmas)
                    """;

    @Override
    public List<Lemma> batchSave(List<Lemma> lemmaList) {
        return save(lemmaList);
    }

    @Override
    public List<Lemma> findAllBySiteIdAndLemmas(Site site, Set<String> lemmas) {
        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue("id", site.getId());
        source.addValue("lemmas", lemmas);

        List<Lemma> allLemmas = namedTemplate.query(SELECT_LEMMA_BY_SITE_ID_AND_LEMMA_SQL, source,
                (rs) -> {
                    List<Lemma> lemmaList = new ArrayList<>();
                    while (rs.next()) {
                        Lemma lemma = new Lemma();
                        int id = rs.getInt("id");
                        String lemmaByString = rs.getString("lemma");
                        int frequency = rs.getInt("frequency");

                        lemma.setId(id);
                        lemma.setLemma(lemmaByString);
                        lemma.setFrequency(frequency);
                        lemma.setSite(site);

                        lemmaList.add(lemma);
                    }
                    return lemmaList;
                });
        return allLemmas;
    }

    public List<Lemma> save(List<Lemma> lemmaList) {
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


}
