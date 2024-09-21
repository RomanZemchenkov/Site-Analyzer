package searchengine.services.parser.snippet;

import lombok.Getter;

@Getter
public class SnippetBestRange {

    private final int leftPosition;
    private final int rightPosition;

    public SnippetBestRange(int leftPosition, int rightPosition) {
        this.leftPosition = leftPosition;
        this.rightPosition = rightPosition;
    }
}
