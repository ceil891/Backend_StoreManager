package org.example.storemanager.shared.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class CloudinaryDeleteEvent extends ApplicationEvent {
    private final String url;
    private final List<String> urls;

    public CloudinaryDeleteEvent(Object source, String url) {
        super(source);
        this.url = url;
        this.urls = null;
    }

    public CloudinaryDeleteEvent(Object source, List<String> urls) {
        super(source);
        this.url = null;
        this.urls = urls;
    }
}
