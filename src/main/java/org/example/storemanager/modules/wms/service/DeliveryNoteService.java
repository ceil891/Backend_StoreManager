package org.example.storemanager.modules.wms.service;

import org.example.storemanager.modules.wms.dto.DeliveryNoteDTO;
import java.util.List;

public interface DeliveryNoteService {
    List<DeliveryNoteDTO> getAll();
    DeliveryNoteDTO getById(Long id);
    DeliveryNoteDTO create(DeliveryNoteDTO dto);
    DeliveryNoteDTO update(Long id, DeliveryNoteDTO dto);
    void delete(Long id);

    DeliveryNoteDTO assignCarrier(Long id, String carrierName, String trackingNumber);
    DeliveryNoteDTO dispatch(Long id);
    DeliveryNoteDTO inTransit(Long id);
    DeliveryNoteDTO deliver(Long id, String recipientName);
    DeliveryNoteDTO failed(Long id, String failureReason);
    DeliveryNoteDTO cancel(Long id, String cancelReason);
}
