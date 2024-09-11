package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dao.model.Index;
import searchengine.dao.model.Lemma;
import searchengine.dao.model.Page;
import searchengine.dao.repository.index.IndexRepository;
import searchengine.services.searcher.GlobalVariables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class IndexService {

    private final IndexRepository indexRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createIndex(){
        ConcurrentHashMap<Page, HashMap<Lemma, Integer>> pageAndLemmasWithCount = GlobalVariables.pageAndLemmasWithCount;
        List<Index> indexList = new ArrayList<>();
        System.out.println("Начало создания индексов");
        pageAndLemmasWithCount.forEach(
                (Page p, HashMap<Lemma, Integer> map) ->
                {
                    List<Index> indexList1 = parseToIndex(p, map);
                    indexList.addAll(indexList1);
                }
        );

        System.out.println("Начала сохранения батчем");
        indexRepository.createBatch(indexList);
    }

    private List<Index> parseToIndex(Page page, HashMap<Lemma, Integer> countOfLemmas){
        List<Index> indexList = new ArrayList<>();
        for(Map.Entry<Lemma,Integer> entry : countOfLemmas.entrySet()){
            Lemma lemma = entry.getKey();
            float count = (float) entry.getValue();
            Index index = new Index(page, lemma, count);
            indexList.add(index);
        }
        return indexList;
    }
}
