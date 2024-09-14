package searchengine.services.parser;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class TextToLemmaParser {

    private static final String RUSSIAN_LETTERS = "[^а-я\\s]";
    private static final String ENGLISH_LETTERS = "[^a-z\\s]";
    private static final Set<String> RUSSIAN_PARTICLES_NAMES = Set.of("СОЮЗ","ПРЕДЛ","МЕЖД");
    private static final Set<String> ENGLISH_PARTICLES_NAMES = Set.of("NOUN","PART","ADJECTIVE");
    private final Set<String> LANGUAGES = Set.of("Russian");

    private final HashMap<String,Integer> words = new HashMap<>();


    public HashMap<String, Integer> parse(String text) {
        String clearText = parseToTextWithoutTeg(text);
        for(String language : LANGUAGES){
            switch (language){
                case "Russian" -> russianLanguageAnalyzer(clearText);
                case "English" -> englishLanguageAnalyzer(clearText);
            }
        }
        return words;
    }

    private void russianLanguageAnalyzer(String text){
        RussianLuceneMorphology morphology;
        try {
            morphology = new RussianLuceneMorphology();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        parseToLemma(text,RUSSIAN_LETTERS,RUSSIAN_PARTICLES_NAMES,morphology);
    }

    private void englishLanguageAnalyzer(String text){
        EnglishLuceneMorphology morphology;
        try {
            morphology = new EnglishLuceneMorphology();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        parseToLemma(text,ENGLISH_LETTERS,ENGLISH_PARTICLES_NAMES,morphology);
    }

    private void parseToLemma(String text, String regex, Set<String> particles, LuceneMorphology morphology){
        String[] anythingLanguageWords = parseToOneLanguageWords(text, regex);
        for(String word : anythingLanguageWords){
            if(words.containsKey(word)){
                words.put(word,words.get(word) + 1);
            }
            if(word.isBlank()){
                continue;
            }

            String infoAboutWorld = morphology.getMorphInfo(word).get(0);
            String particle = parseToParticles(infoAboutWorld);
            if(particles.contains(particle)){
                continue;
            }

            List<String> normalForms = morphology.getNormalForms(word);
            if(normalForms.isEmpty()){
                continue;
            }
            String normalForm = normalForms.get(0);

            words.put(normalForm, words.getOrDefault(normalForm,0) + 1);
        }
    }

    private String parseToParticles(String fullFormAnswer){
        StringBuilder sb = new StringBuilder();
        char[] charArray = fullFormAnswer.toCharArray();
        int length = charArray.length;
        for(int i = length - 1; i > 0; i--){
            char oneChar = charArray[i];
            if(Character.isLetter(oneChar)){
                sb.insert(0,oneChar);
            } else {
                break;
            }
        }
        return sb.toString();
    }

    private String[] parseToOneLanguageWords(String allLanguagesText, String regex){
        return allLanguagesText
                .toLowerCase()
                .replaceAll(regex," ")
                .trim()
                .split("\\s+");
    }

    private String parseToTextWithoutTeg(String htmlText) {
        return Jsoup.parse(htmlText).text();
    }




}
