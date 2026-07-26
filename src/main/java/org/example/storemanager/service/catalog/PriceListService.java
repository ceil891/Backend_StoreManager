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

    List<org.example.storemanager.dto.response.catalog.pricelist.PriceListDetailResponse> getItems(Long priceListId);

    org.example.storemanager.dto.response.catalog.pricelist.PriceListDetailResponse addItem(Long priceListId, org.example.storemanager.dto.request.catalog.pricelist.PriceListDetailRequest request);

    org.example.storemanager.dto.response.catalog.pricelist.PriceListDetailResponse updateItem(Long id, java.math.BigDecimal price);

    void deleteItem(Long id);

    java.math.BigDecimal getVariantPrice(Long variantId, Long branchId);

    org.example.storemanager.dto.response.catalog.pricelist.ActualPriceResponse resolveActualPrice(Long variantId, Long branchId);
}
