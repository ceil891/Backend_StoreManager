package org.example.storemanager.service.wms;

import org.example.storemanager.dto.wms.LocationTransferDTO;
import java.util.List;

public interface LocationTransferService {
    List<LocationTransferDTO.Response> getAllTransfers();
    List<LocationTransferDTO.Response> getTransfersByBranchId(Long branchId);
    LocationTransferDTO.Response getTransferById(Long id);
    LocationTransferDTO.Response createTransfer(LocationTransferDTO.Request request);
    LocationTransferDTO.Response updateTransfer(Long id, LocationTransferDTO.Request request);
    void deleteTransfer(Long id);

    LocationTransferDTO.Response setItem(Long id, LocationTransferDTO.Request request);
    LocationTransferDTO.Response clearItem(Long id);

    LocationTransferDTO.Response submitTransfer(Long id);
    LocationTransferDTO.Response approveTransfer(Long id);
    LocationTransferDTO.Response executeTransfer(Long id);
    LocationTransferDTO.Response completeTransfer(Long id);
    LocationTransferDTO.Response cancelTransfer(Long id);
}
