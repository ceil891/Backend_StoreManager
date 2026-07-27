package org.example.storemanager.modules.catalog.service;

import org.example.storemanager.modules.catalog.dto.request.pricelist.CreatePriceListRequest;
import org.example.storemanager.modules.catalog.dto.request.pricelist.UpdatePriceListRequest;
import org.example.storemanager.modules.catalog.dto.response.pricelist.ActivePriceResponse;
import org.example.storemanager.modules.catalog.dto.response.pricelist.PriceListResponse;

import java.util.List;

public interface PriceListService {

    List<PriceListResponse> getAll();

    PriceListResponse getById(Long id);

    PriceListResponse create(CreatePriceListRequest request);

    PriceListResponse update(Long id, UpdatePriceListRequest request);

    void delete(Long id);

    ActivePriceResponse resolveActivePrice(Long branchId, Long productId, Long productUnitId);

    List<org.example.storemanager.modules.catalog.dto.response.pricelist.PriceListDetailResponse> getItems(Long priceListId);

    org.example.storemanager.modules.catalog.dto.response.pricelist.PriceListDetailResponse addItem(Long priceListId, org.example.storemanager.modules.catalog.dto.request.pricelist.PriceListDetailRequest request);

    org.example.storemanager.modules.catalog.dto.response.pricelist.PriceListDetailResponse updateItem(Long id, java.math.BigDecimal price);

    void deleteItem(Long id);

    java.math.BigDecimal getVariantPrice(Long variantId, Long branchId);

    org.example.storemanager.modules.catalog.dto.response.pricelist.ActualPriceResponse resolveActualPrice(Long variantId, Long branchId);
}
