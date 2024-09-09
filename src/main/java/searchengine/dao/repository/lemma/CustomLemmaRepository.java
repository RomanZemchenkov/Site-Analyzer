package searchengine.dao.repository.lemma;

import searchengine.dao.model.Lemma;

import java.util.List;

public interface CustomLemmaRepository {

    List<Lemma> batchSave(List<Lemma> lemmaList);
}
