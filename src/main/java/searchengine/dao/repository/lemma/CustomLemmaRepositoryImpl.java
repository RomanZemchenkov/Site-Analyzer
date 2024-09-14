package searchengine.dao.repository.lemma;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import searchengine.dao.model.Lemma;

import java.sql.PreparedStatement;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CustomLemmaRepositoryImpl implements CustomLemmaRepository{

    private final JdbcTemplate template;
    private static final String LEMMA_SAVE_SQL = """
                                                 INSERT INTO lemma(site_id, lemma, frequency) 
                                                 VALUES (?,?,?);
                                                 """;

    @Override
    public List<Lemma> batchSave(List<Lemma> lemmaList) {
        return save(lemmaList);
    }

    public List<Lemma> save(List<Lemma> lemmaList){
        for(Lemma lemma : lemmaList){
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            template.update((con) -> {
                PreparedStatement ps = con.prepareStatement(LEMMA_SAVE_SQL, new String[]{"id"});
                ps.setInt(1,lemma.getSite().getId());
                ps.setString(2,lemma.getLemma());
                ps.setInt(3,lemma.getFrequency());
                return ps;
            }, keyHolder);
            Integer key = keyHolder.getKey().intValue();
            lemma.setId(key);
        }
        return lemmaList;
    }


}
