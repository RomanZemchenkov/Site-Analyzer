package searchengine.services.event_listeners.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import searchengine.services.dto.page.CreatePageDto;

@Getter
public class AnalyzedPageEvent extends ApplicationEvent {

    private final CreatePageDto dto;

    public AnalyzedPageEvent(CreatePageDto dto) {
        super(dto);
        this.dto = dto;
    }
}
