package org.example.storemanager.modules.wms.service;

import org.example.storemanager.modules.wms.dto.PackingListDTO;
import java.util.List;

public interface PackingListService {
    List<PackingListDTO> getAll();
    PackingListDTO getById(Long id);
    PackingListDTO create(PackingListDTO dto);
    PackingListDTO update(Long id, PackingListDTO dto);
    void delete(Long id);

    PackingListDTO.Item addItem(Long packingListId, PackingListDTO.Item itemDto);
    PackingListDTO.Item updateItem(Long id, PackingListDTO.Item itemDto);
    void deleteItem(Long id);

    PackingListDTO startPicking(Long id);
    PackingListDTO pick(Long id, List<PackingListDTO.Item> items);
    PackingListDTO startPacking(Long id);
    PackingListDTO completePacking(Long id);
    PackingListDTO cancelPacking(Long id);
}
