package searchengine.services.event_listeners.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import searchengine.services.dto.site.UpdateSiteDto;

@Getter
public class CreatePageEvent extends ApplicationEvent {

    private final UpdateSiteDto dto;

    public CreatePageEvent(UpdateSiteDto dto) {
        super(dto);
        this.dto = dto;
    }
}
