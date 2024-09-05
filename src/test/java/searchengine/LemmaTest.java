package searchengine;

import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class LemmaTest {

    @Test
    void test() throws IOException {
        EnglishLuceneMorphology morphology = new EnglishLuceneMorphology();
        String info = morphology.getMorphInfo("no").get(0);

        System.out.println(info);
    }
}
