package searchengine.services.event_listeners.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import searchengine.services.dto.page.CreatePageDto;

@Getter
public class CreatePageEvent extends ApplicationEvent {

    private final CreatePageDto dto;

    public CreatePageEvent(CreatePageDto dto) {
        super(dto);
        this.dto = dto;
    }
}
