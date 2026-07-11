package org.example.storemanager.service.catalog;

import org.example.storemanager.dto.request.catalog.pricelist.CreatePriceListRequest;
import org.example.storemanager.dto.request.catalog.pricelist.UpdatePriceListRequest;
import org.example.storemanager.dto.response.catalog.pricelist.ActivePriceResponse;
import org.example.storemanager.dto.response.catalog.pricelist.PriceListResponse;

import java.util.List;

public interface PriceListService {

    List<PriceListResponse> getAll();

    PriceListResponse getById(Long id);

    PriceListResponse create(CreatePriceListRequest request);

    PriceListResponse update(Long id, UpdatePriceListRequest request);

    void delete(Long id);

    ActivePriceResponse resolveActivePrice(Long branchId, Long productId, Long productUnitId);
}
