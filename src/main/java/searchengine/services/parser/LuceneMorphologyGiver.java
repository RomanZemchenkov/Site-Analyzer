package searchengine.services.parser;

import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;

public final class LuceneMorphologyGiver {

    private static RussianLuceneMorphology russianLuceneMorphology;

    public static void initLucene() {
        try {
            russianLuceneMorphology = new RussianLuceneMorphology();
        } catch (IOException e) {
            System.err.println("Ошибка при создании RussianLuceneMorphology: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static RussianLuceneMorphology get(){
        if(russianLuceneMorphology == null){
            initLucene();
        }
        return russianLuceneMorphology;
    }

    public static void clearLucene(){
        if(russianLuceneMorphology != null){
            russianLuceneMorphology = null;
        }
    }

}
