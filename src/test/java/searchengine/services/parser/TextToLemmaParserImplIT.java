package searchengine.services.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import searchengine.services.parser.lemma.TextToLemmaParserImpl;
import searchengine.services.searcher.analyzer.PageAnalyzerImpl;
import searchengine.services.searcher.entity.HttpResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class TextToLemmaParserImplIT {

    private static final String TEST_ENGLISH_CLEAR_TEXT = "Reading is an important part of my life. I love immersing myself into an interesting story for an hour or two. For me a book is not just a source of information, it is a world I can visit.\n" +
                                                  "\n" +
                                                  "I read books of different genres, but my favorite books are set in fictional worlds. It might be in the future, another universe or a fantasy world. My favorite book is “Six of Crows” by Leigh Bardugo. To be more precise, it is not one book, but a series of two novels – “Six of Crows” and “Crooked Kingdom”. Anyway, they tell one story split into two big parts, so most readers consider them one novel.\n" +
                                                  "\n" +
                                                  "“Six of Crows” is an unusual combination of a heist story and fantasy. The story is set in a fantasy world, it begins in Ketterdam – a fictional country inspired by the Netherlands. The main character is a young criminal called Kaz. He is the leader of a group of people with different skills and talents. Besides the leader, the group includes a highly skilled gunslinger, an agile and lightfooted spy, a sorceress, a warrior, and a boy from a rich family, talented in music and demolition. That group is referred to as “Six of Crows”. The main storyline is about the Crows trying to pull off an impossible heist. They need to get into the most guarded fortress in the world and steal something extremely valuable.\n" +
                                                  "\n" +
                                                  "The book is full of suspense and plot twists, but I would not say that “Six of Crows” is a plot-based story. In my opinion, it is character-based. The longer we follow the Crows’ adventures, the more we get to learn about them. All of them are interesting, well-written characters. Their backstories are revealed gradually, and we get to know why Kaz never touches people, how Jesper was taught to shoot, and so on. The storyline that I love the most was the one about Nina and Matthias, the sorceress and the warrior. At first, they are sworn enemies – “a witch and a witch hunter”, but then it changes. I think this is the most dramatic and thought-out part of the book.\n" +
                                                  "\n" +
                                                  "If you like immersing yourself into a fun story with great characters, I would like to recommend you “Six of Crows”. It is one of the best fantasy books I have ever read. I hope you will enjoy it too.";

    private static final String TEST_RUSSIAN_CLEAR_TEXT = "Повторное появление леопарда в Осетии позволяет предположить,\n" +
                                                          "что леопард постоянно обитает в некоторых районах Северного\n" +
                                                          "Кавказа.\n";

    @Test
    @DisplayName("Testing the parsing of the purified Russian text")
    void parseToLemmasTest(){
        TextToLemmaParserImpl parser = new TextToLemmaParserImpl();
        Map<String, Integer> parse = assertDoesNotThrow(() -> parser.parse(TEST_RUSSIAN_CLEAR_TEXT));
        assertThat(parse).hasSize(12);
    }

    @Test
    @DisplayName("Тестирование полного функционала")
    void fullParseTest(){
        time(() -> {
            PageAnalyzerImpl pageAnalyzerImpl = new PageAnalyzerImpl();
            HttpResponseEntity response = pageAnalyzerImpl.searchLink("https://ru.wikipedia.org/wiki/Хронология_событий_сентября_—_октября_1993_года_в_Москве","https://ru.wikipedia.org/wiki/Хронология_событий_сентября_—_октября_1993_года_в_Москве");
            String content = response.getContent();

            TextToLemmaParserImpl textToLemmaParserImpl = new TextToLemmaParserImpl();
            Map<String, Integer> result = assertDoesNotThrow(() -> textToLemmaParserImpl.parse(content));
            assertThat(result).hasSize(5715);
        });
    }

    static void time(Runnable runnable){
        long start = System.currentTimeMillis();
        runnable.run();
        long finish = System.currentTimeMillis();

        System.out.println("Метод отработал за:" + (finish - start));
    }
}
