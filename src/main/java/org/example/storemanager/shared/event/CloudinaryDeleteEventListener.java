package org.example.storemanager.shared.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.storemanager.modules.common.service.CloudinaryService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class CloudinaryDeleteEventListener {

    private final CloudinaryService cloudinaryService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCloudinaryDeleteEvent(CloudinaryDeleteEvent event) {
        log.info("Received CloudinaryDeleteEvent after commit.");
        if (event.getUrl() != null) {
            cloudinaryService.deleteFileByUrlAsync(event.getUrl());
        }
        if (event.getUrls() != null && !event.getUrls().isEmpty()) {
            event.getUrls().forEach(cloudinaryService::deleteFileByUrlAsync);
        }
    }
}
