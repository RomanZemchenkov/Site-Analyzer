package searchengine.services.parser;

import lombok.Getter;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;

public final class LuceneMorphologyGiver {

    @Getter
    private volatile static RussianLuceneMorphology russian;

    public static void init(){
        try {
            russian = new RussianLuceneMorphology();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
