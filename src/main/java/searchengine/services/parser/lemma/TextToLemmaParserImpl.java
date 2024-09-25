package searchengine.services.parser.lemma;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import searchengine.services.parser.LuceneMorphologyGiver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TextToLemmaParserImpl implements TextToLemmaParser{

    private static final String RUSSIAN_LETTERS = "[^а-яёЁ\\s]";
    private static final Set<String> RUSSIAN_PARTICLES_NAMES = Set.of("СОЮЗ", "ПРЕДЛ", "МЕЖД");
    private final Set<String> LANGUAGES = Set.of("Russian");

    public Map<String, Integer> parse(String text) {
        String clearText = parseToTextWithoutTeg(text);
        Map<String,Integer> wordsByLemmas = new HashMap<>();
        for (String language : LANGUAGES) {
            switch (language) {
                case "Russian" -> {
                    Map<String, Integer> russianWordsByLemmas = russianLanguageAnalyzer(clearText);
                    wordsByLemmas.putAll(russianWordsByLemmas);
                }
            }
        }
        return wordsByLemmas;
    }

    private Map<String,Integer> russianLanguageAnalyzer(String text) {
        RussianLuceneMorphology morphology = LuceneMorphologyGiver.get();
        String[] oneLanguageWords = parseToOneLanguageWords(text, RUSSIAN_LETTERS);
        Map<String, Integer> lemmasMap = createLemmasMap(oneLanguageWords, RUSSIAN_PARTICLES_NAMES, morphology);
        LuceneMorphologyGiver.returnLucene(morphology);
        return lemmasMap;
    }

    private Map<String,Integer> createLemmasMap(String[] oneLanguageWords, Set<String> particles, LuceneMorphology morphology){
        Map<String,Integer> wordsByLemmas = new HashMap<>();
        for (String word : oneLanguageWords) {
            if(!word.isBlank()){
                String wordByLemma = parseToLemma(word, particles, morphology);
                if(!wordByLemma.isBlank()){
                    wordsByLemmas.put(wordByLemma,wordsByLemmas.getOrDefault(wordByLemma, 0) + 1);
                }
            }
        }
        return wordsByLemmas;
    }

    public String parseToLemma(String word, Set<String> particles, LuceneMorphology morphology) {
        word = word.toLowerCase();
        String normalForm = "";
        String infoAboutWorld = morphology.getMorphInfo(word).get(0);
        String particle = parseToParticles(infoAboutWorld);
        if (!particles.contains(particle)) {
            List<String> normalForms = morphology.getNormalForms(word);
            if (!normalForms.isEmpty()) {
                normalForm = normalForms.get(0);
            }
        }
        return normalForm;
    }

    private String parseToParticles(String fullFormAnswer) {
        StringBuilder sb = new StringBuilder();
        char[] charArray = fullFormAnswer.toCharArray();
        int length = charArray.length;
        for (int i = length - 1; i > 0; i--) {
            char oneChar = charArray[i];
            if (Character.isLetter(oneChar)) {
                sb.insert(0, oneChar);
            } else {
                break;
            }
        }
        return sb.toString();
    }

    private String[] parseToOneLanguageWords(String allLanguagesText, String regex) {
        return allLanguagesText
                .toLowerCase()
                .replaceAll(regex, " ")
                .trim()
                .split("\\s+");
    }

    private String parseToTextWithoutTeg(String htmlText) {
        return Jsoup.parse(htmlText).text();
    }

}
