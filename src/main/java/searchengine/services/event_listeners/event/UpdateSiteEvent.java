package searchengine.services.event_listeners.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import searchengine.services.dto.site.UpdateSiteDto;

@Getter
public class UpdateSiteEvent extends ApplicationEvent {

    private final UpdateSiteDto dto;

    public UpdateSiteEvent(UpdateSiteDto dto) {
        super(dto);
        this.dto = dto;
    }
}
