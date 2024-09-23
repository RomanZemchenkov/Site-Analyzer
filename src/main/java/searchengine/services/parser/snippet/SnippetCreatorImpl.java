package searchengine.services.parser.snippet;

import lombok.NoArgsConstructor;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import searchengine.services.parser.TextToLemmaParser;

import java.util.*;

@NoArgsConstructor
public class SnippetCreatorImpl implements SnippetCreator {

    private final Map<String, List<Integer>> lemmaAndPosition = new HashMap<>();
    private static final Set<String> RUSSIAN_PARTICLES_NAMES = Set.of("СОЮЗ", "ПРЕДЛ", "МЕЖД");
    private static final String PUNCTUATION_END = "([.!?])";
    private static final String PUNCTUATION_ALL = "([,:;])";
    private static final int MAX_SNIPPET_WORDS = 60;
    private List<String> searchedLemmas;
    private List<Integer> positionOfLemmasInSuitableRange;
    private RussianLuceneMorphology russianLuceneMorphology;

    public SnippetCreatorImpl(List<String> searchedLemmas, RussianLuceneMorphology russianLuceneMorphology) {
        this.searchedLemmas = searchedLemmas;
        this.russianLuceneMorphology = russianLuceneMorphology;
    }

    public String createSnippet(String context) {
        String textWithoutTeg = parseToClearText(context);
        List<String> wordsAndSymbols = createWordsAndSymbolsFromText(textWithoutTeg);

        SnippetBestPosition bestSnippetRange = findBestSnippetRange(wordsAndSymbols.size());
        String snippetWithTeg = createSnippetWithPTeg(bestSnippetRange, wordsAndSymbols);
        return createFinalSnippetView(snippetWithTeg);
    }

    private String parseToClearText(String context) {
        return Jsoup.parse(context).text();
    }

    private String createFinalSnippetView(String snippet) {
        String snippetStart = snippet.substring(0, snippet.length() / 2);
        String snippetEnd = snippet.substring(snippetStart.length());

        return isStartFormatSnippetPart(snippetStart) +
               isEndFormatSnippetPart(snippetEnd);
    }

    private String isStartFormatSnippetPart(String snippetPart) {
        String firstChar = Character.toString(snippetPart.charAt(0));
        if (firstChar.matches(PUNCTUATION_END)) {
            return snippetPart.substring(1).trim();
        } else if (firstChar.matches(PUNCTUATION_ALL)) {
            return new StringBuilder(snippetPart).insert(0, "...").toString();
        } else {
            return snippetPart;
        }
    }

    private String isEndFormatSnippetPart(String snippetPart) {
        String lastChar = Character.toString(snippetPart.charAt(snippetPart.length() - 1));
        if (lastChar.matches(PUNCTUATION_END)) {
            return snippetPart;
        } else {
            return snippetPart + "...";
        }
    }

    private String createSnippetWithPTeg(SnippetBestPosition bestPosition, List<String> textByList) {
        int rarestLemma = bestPosition.getBestPositionRarestLemma();
        SnippetBestRange availableSentenceRange = createAvailableSentence(rarestLemma, textByList);

        StringBuilder sb = new StringBuilder();
        int leftPosition = availableSentenceRange.getLeftPosition();
        int rightPosition = availableSentenceRange.getRightPosition();

        List<Integer> positionForTeg = checkPositionForTeg(leftPosition, rightPosition, rarestLemma);
        for (int left = leftPosition; left < rightPosition; left++) {
            String wordOrSomethingElse = textByList.get(left);
            if (positionForTeg.contains(left)) {
                sb.append("<p>");
                sb.append(wordOrSomethingElse);
                sb.append("</p>");
            } else {
                sb.append(wordOrSomethingElse);
            }
        }
        return sb.toString();
    }

    private List<Integer> checkPositionForTeg(int leftPosition, int rightPosition, int rarestLemma) {
        List<Integer> positionsForTeg = new ArrayList<>();
        for (int position : positionOfLemmasInSuitableRange) {
            if (position >= leftPosition && position <= rightPosition) {
                positionsForTeg.add(position);
            }
        }
        positionsForTeg.add(rarestLemma);
        return positionsForTeg;
    }

    private SnippetBestPosition findBestSnippetRange(int countOfTextElements) {
        String firstAndRarestLemma = searchedLemmas.get(0);
        List<Integer> positionByFirstAndRarestLemma = findPositionBySearchedLemma(firstAndRarestLemma);

        List<Integer> positionForAnotherLemmas = new ArrayList<>();
        for (String anotherLemma : searchedLemmas) {
            if (!anotherLemma.equals(firstAndRarestLemma)) {
                positionForAnotherLemmas.addAll(findPositionBySearchedLemma(anotherLemma));
            }
        }

        Collections.sort(positionByFirstAndRarestLemma);
        Collections.sort(positionForAnotherLemmas);

        List<Integer> theBestPositionRange = new ArrayList<>();
        int bestRarestPosition = positionByFirstAndRarestLemma.get(0);


        int left = 0;
        for (int right = 0; right < positionByFirstAndRarestLemma.size(); right++) {
            int currentBestRarestPosition = positionByFirstAndRarestLemma.get(right);
            List<Integer> tempPositionRange = noNameMethod(positionByFirstAndRarestLemma, left, countOfTextElements, positionForAnotherLemmas, right);

            if (theBestPositionRange.size() < tempPositionRange.size()) {
                theBestPositionRange = new ArrayList<>(tempPositionRange);
                bestRarestPosition = currentBestRarestPosition;
            }
        }
        positionOfLemmasInSuitableRange = positionForAnotherLemmas;
        return new SnippetBestPosition(bestRarestPosition, theBestPositionRange);
    }

    private List<Integer> noNameMethod(List<Integer> positionByFirstAndRarestLemma, int left,
                                       int countOfTextElements,
                                       List<Integer> positionForAnotherLemmas, int right) {
        int rightCursor = positionByFirstAndRarestLemma.get(right);

        while (rightCursor - positionByFirstAndRarestLemma.get(left) > MAX_SNIPPET_WORDS) {
            left++;
        }

        List<Integer> tempPositionRange = new ArrayList<>();
        Integer leftCursor = positionByFirstAndRarestLemma.get(left);
        if (leftCursor == rightCursor) {
            leftCursor = Math.max(0, leftCursor - MAX_SNIPPET_WORDS / 2);
            rightCursor = Math.min(countOfTextElements, rightCursor + MAX_SNIPPET_WORDS / 2);
        }
        for (int position : positionForAnotherLemmas) {
            if (position >= leftCursor && position <= rightCursor) {
                tempPositionRange.add(position);
            } else if (position > rightCursor) {
                break;
            }
        }
        return tempPositionRange;
    }


    private SnippetBestRange createAvailableSentence(int rarestPosition, List<String> textByList) {
        int leftPosition = Math.max(rarestPosition - MAX_SNIPPET_WORDS, 0);
        int rightPosition = Math.min(rarestPosition + MAX_SNIPPET_WORDS, textByList.size() - 1);
        List<Integer> punctuationPosition = new ArrayList<>();
        for (int left = leftPosition; left <= rightPosition; left++) {
            String wordOrSomethingElse = textByList.get(left);
            if (wordOrSomethingElse.matches(PUNCTUATION_ALL) || wordOrSomethingElse.matches(PUNCTUATION_END)) {
                punctuationPosition.add(left);
            }
        }
        if (punctuationPosition.size() <= 1) {
            return createSnippetBestRangeByOneOrNullPunctuation(rarestPosition, punctuationPosition, leftPosition, rightPosition);
        }
        return createSnippetBestRange(rarestPosition, punctuationPosition);
    }

    private SnippetBestRange createSnippetBestRangeByOneOrNullPunctuation(int bestPosition, List<Integer> punctuationPosition,
                                                                          int availableLeftPosition, int availableRightPosition) {
        int leftPosition = availableLeftPosition;
        int rightPosition = availableRightPosition;

        if (punctuationPosition.isEmpty()) {
            leftPosition = Math.max(bestPosition - MAX_SNIPPET_WORDS / 2, availableLeftPosition);
            if (leftPosition == 0) {
                rightPosition = Math.min(leftPosition + MAX_SNIPPET_WORDS, rightPosition);
            } else {
                rightPosition = Math.min(bestPosition + MAX_SNIPPET_WORDS / 2, availableRightPosition);
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

    private SnippetBestRange createSnippetBestRange(int bestPosition, List<Integer> punctuationPosition) {
        int leftPosition = 0;
        int rightPosition = 0;
        int range = 0;
        int lastRightCursor = 0;
        for (int leftCursor = 0; leftCursor < punctuationPosition.size(); leftCursor++) {
            Integer currentLeftPosition = punctuationPosition.get(leftCursor);
            if (currentLeftPosition >= bestPosition) {
                break;
            }
            for (int rightCursor = lastRightCursor + 1; rightCursor < punctuationPosition.size(); rightCursor++) {
                Integer currentRightPosition = punctuationPosition.get(rightCursor);
                if (currentRightPosition - currentLeftPosition <= MAX_SNIPPET_WORDS) {
                    lastRightCursor = rightCursor;
                } else {
                    break;
                }
            }
            Integer lastRightAvailablePosition = punctuationPosition.get(lastRightCursor);
            if (bestPosition <= lastRightAvailablePosition) {
                int currentBestLeftPosition = currentLeftPosition;
                int currentBestRightPosition = lastRightAvailablePosition;
                int currentRange = currentBestRightPosition - currentBestLeftPosition;
                if (currentRange >= range) {
                    leftPosition = currentBestLeftPosition;
                    rightPosition = currentBestRightPosition;
                    range = currentRange;
                }
            }
        }
        return new SnippetBestRange(leftPosition, rightPosition);
    }


    private List<String> createWordsAndSymbolsFromText(String textWithoutTags) {
        List<String> wordsAndSymbols = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        int position = 0;
        boolean prevLetter = false;
        boolean prevDigit = false;
        for (int i = 0; i < textWithoutTags.length(); i++) {
            char currentSymbol = textWithoutTags.charAt(i);
            if (Character.isLetter(currentSymbol)) {
                sb.append(currentSymbol);
                prevLetter = true;
                prevDigit = false;
            } else if (Character.isDigit(currentSymbol)) {
                sb.append(currentSymbol);
                prevDigit = true;
                prevLetter = false;
            } else {
                if (!sb.isEmpty()) {
                    position = isWordOrDigit(sb, wordsAndSymbols, prevLetter, prevDigit, position);
                    sb.setLength(0);
                }
                wordsAndSymbols.add(String.valueOf(currentSymbol));
                prevLetter = false;
                prevDigit = false;
                position++;
            }
        }
        if (!sb.isEmpty()) {
            isWordOrDigit(sb, wordsAndSymbols, prevLetter, prevDigit, position);
        }
        return wordsAndSymbols;
    }


    private int isWordOrDigit(StringBuilder sb, List<String> wordsAndSymbols, boolean prevLetter, boolean prevDigit, int position) {
        if (prevLetter) {
            String word = sb.toString();
            word = word.replaceAll("[^а-яА-ЯёЁ]", "");
            if (!word.isBlank()) {
                wordsAndSymbols.add(addWord(word, position));
                position++;
            }
        } else if (prevDigit) {
            wordsAndSymbols.add(addDigit(sb));
            position++;
        }
        return position;
    }

    private String addWord(String word, int position) {
        try {
            String mayBeWord = new TextToLemmaParser().parseToLemma(word, RUSSIAN_PARTICLES_NAMES, russianLuceneMorphology);
            if (!mayBeWord.isBlank()) {
                List<Integer> listByWord = lemmaAndPosition.getOrDefault(mayBeWord, new ArrayList<>());
                listByWord.add(position);
                lemmaAndPosition.put(mayBeWord, listByWord);
            }
            return word;
        } catch (Throwable throwable) {
            System.out.println(word);
            throw new RuntimeException(throwable);
        }
    }

    private String addDigit(StringBuilder sb) {
        return sb.toString();
    }

    private List<Integer> findPositionBySearchedLemma(String searchedLemma) {
        List<Integer> lemmasPosition = new ArrayList<>();
        for (Map.Entry<String, List<Integer>> entry : lemmaAndPosition.entrySet()) {
            String lemmaWithPosition = entry.getKey();
            if (lemmaWithPosition.equals(searchedLemma)) {
                lemmasPosition = entry.getValue();
                break;
            }
        }
        return lemmasPosition;
    }
}
