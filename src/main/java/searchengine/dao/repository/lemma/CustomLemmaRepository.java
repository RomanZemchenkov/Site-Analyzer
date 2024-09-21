package searchengine.dao.repository.lemma;

import searchengine.dao.model.Lemma;
import searchengine.dao.model.Site;

import java.util.List;
import java.util.Set;

public interface CustomLemmaRepository {

    List<Lemma> batchSave(List<Lemma> lemmaList);

    List<Lemma> findAllBySiteIdAndLemmas(Site site, Set<String> lemmas);
}
