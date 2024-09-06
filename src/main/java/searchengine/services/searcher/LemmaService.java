package searchengine.services.searcher;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dao.repository.LemmaRepository;
import searchengine.services.parser.TextToLemmaParser;

@Service
@RequiredArgsConstructor
public class LemmaService {

    private final LemmaRepository lemmaRepository;
    private TextToLemmaParser parser;
}
