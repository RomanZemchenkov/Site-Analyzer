package searchengine.services.parser.lemma;

import java.util.Map;

public interface TextToLemmaParser {

    Map<String,Integer> parse(String text);

}
