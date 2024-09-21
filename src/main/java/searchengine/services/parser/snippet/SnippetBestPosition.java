package searchengine.services.parser.snippet;

import lombok.Getter;

import java.util.List;

@Getter
public class SnippetBestPosition {

    private final int bestPositionRarestLemma;
    private final List<Integer> bestPositionRange;


    public SnippetBestPosition(int bestPositionRarestLemma, List<Integer> bestPositionRange) {
        this.bestPositionRarestLemma = bestPositionRarestLemma;
        this.bestPositionRange = bestPositionRange;
    }
}
