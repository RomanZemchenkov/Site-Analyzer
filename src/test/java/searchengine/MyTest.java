package searchengine;

import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import searchengine.services.parser.snippet.SnippetBestRange;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;


public class MyTest {
    private static final int MAX_SNIPPET_WORDS = 30;

    @Test
    void luceneTest() throws IOException {
        RussianLuceneMorphology morphology = new RussianLuceneMorphology();
        List<String> normalForms = morphology.getNormalForms("самолёт");
        System.out.println(normalForms);
    }

    @Test
    void test() {
        List<String> list1 = new ArrayList<>(List.of("sdf"));
        List<String> list2 = new ArrayList<>(List.of("sdf"));
        List<String> list3 = new ArrayList<>(List.of("dfsf"));
        List<String> list4 = new ArrayList<>(List.of("fdf"));

        List<List<String>> list = new ArrayList<>(List.of(list1, list2, list3, list4));
        List<String> newList = list.stream()
                .reduce(new ArrayList<>(), (allLemmas, currentList) -> {
                    allLemmas.addAll(currentList);
                    return allLemmas;
                });

        System.out.println(newList);
    }

    @Test
    void sortTest() {
        Map<Integer, Integer> notSortedMap = new HashMap<>();
        notSortedMap.put(111, 1);
        notSortedMap.put(24, 1);
        notSortedMap.put(6, 1);
        notSortedMap.put(11, 1);
        notSortedMap.put(235, 1);

        Map<Integer, Integer> sortedMap = new TreeMap<>();
        for (Map.Entry<Integer, Integer> entry : notSortedMap.entrySet()) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        System.out.println(sortedMap);
    }

    @Test
    void russianLemmaTest() throws IOException {
        RussianLuceneMorphology morphology = new RussianLuceneMorphology();

        List<String> normalForms = morphology.getNormalForms("старшеклассник");
        List<String> info = morphology.getMorphInfo("старшеклассников");
        System.out.println(info);
        System.out.println(normalForms);
    }

    @Test
    void testEdgeCaseRight() {
        // Given
        int bestPosition = 60;
        List<Integer> punctuationPosition = new ArrayList<>(List.of(78));

        // When
        Map<Integer, Integer> result = runAlgorithm(bestPosition, punctuationPosition);

        // Then
        Map<Integer, Integer> expected = new HashMap<>();
        expected.put(80, 93);
        Assertions.assertEquals(expected, result);
    }

    @ParameterizedTest
    @DisplayName("Testing the oneOrNullAlgorithm")
    @MethodSource("argumentsFOrOneOrNullTest")
    void oneOrNull(int bestPosition, List<Integer> punctuationPosition,
                   int availableLeftPosition, int availableRightPosition, SnippetBestRange expectedResult) {
        SnippetBestRange snippetBestRange = oneOrNullAlgorithm(bestPosition, punctuationPosition, availableLeftPosition, availableRightPosition);
        int leftPosition = expectedResult.getLeftPosition();
        int rightPosition = expectedResult.getRightPosition();
        Assertions.assertEquals(snippetBestRange.getLeftPosition(), leftPosition);
        Assertions.assertEquals(snippetBestRange.getRightPosition(), rightPosition);

    }

    static Stream<Arguments> argumentsFOrOneOrNullTest() {
        return Stream.of(
                Arguments.of(10, List.of(5), 0, 40, new SnippetBestRange(5, 35)),
                Arguments.of(10, List.of(15), 0, 40, new SnippetBestRange(0, 30)),
                Arguments.of(40, List.of(11), 10, 70, new SnippetBestRange(11, 41)),
                Arguments.of(44, List.of(66), 14, 74, new SnippetBestRange(36, 66)),
                Arguments.of(10, List.of(), 0, 40, new SnippetBestRange(0, 30)),
                Arguments.of(20, List.of(), 0, 40, new SnippetBestRange(5, 35))
        );
    }


    private SnippetBestRange oneOrNullAlgorithm(int bestPosition, List<Integer> punctuationPosition,
                                                int availableLeftPosition, int availableRightPosition) {
        int leftPosition = availableLeftPosition;
        int rightPosition = availableRightPosition;

        if (punctuationPosition.isEmpty()) {
            leftPosition = Math.max(bestPosition - 15, availableLeftPosition);
            if (leftPosition == 0) {
                rightPosition = Math.min(leftPosition + MAX_SNIPPET_WORDS, rightPosition);
            } else {
                rightPosition = Math.min(bestPosition + 15, availableRightPosition);
            }
            return new SnippetBestRange(leftPosition, rightPosition);
        }

        if (punctuationPosition.size() == 1) {
            Integer onlyPosition = punctuationPosition.get(0);
            int range = bestPosition - onlyPosition;

            if (range > 0) {
                rightPosition = Math.min(MAX_SNIPPET_WORDS + onlyPosition, availableRightPosition);
                System.out.println(rightPosition);
                leftPosition = Math.max(rightPosition - MAX_SNIPPET_WORDS, availableLeftPosition);
            } else {
                leftPosition = Math.max(onlyPosition - MAX_SNIPPET_WORDS, availableLeftPosition);
                rightPosition = Math.min(leftPosition + MAX_SNIPPET_WORDS, availableRightPosition);
            }
        }

        return new SnippetBestRange(leftPosition, rightPosition);
    }

    private Map<Integer, Integer> runAlgorithm(int bestPosition, List<Integer> punctuationPosition) {
        Map<Integer, Integer> bestSentences = new HashMap<>();
        int lastRightCursor = 0;
        for (int leftCursor = 0; leftCursor < punctuationPosition.size(); leftCursor++) {
            Integer currentLeftPosition = punctuationPosition.get(leftCursor);
            if (currentLeftPosition >= bestPosition) {
                break;
            }
            for (int rightCursor = lastRightCursor + 1; rightCursor < punctuationPosition.size(); rightCursor++) {
                Integer currentRightPosition = punctuationPosition.get(rightCursor);
                if (currentRightPosition - currentLeftPosition <= 30) {
                    lastRightCursor = rightCursor;
                } else {
                    break;
                }
            }
            Integer lastRightAvailablePosition = punctuationPosition.get(lastRightCursor);
            if (bestPosition <= lastRightAvailablePosition) {
                bestSentences.put(currentLeftPosition, lastRightAvailablePosition);
            }
        }
        System.out.println(bestSentences);
        return bestSentences;
    }

}
