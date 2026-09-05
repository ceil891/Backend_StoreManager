package org.example.storemanager.modules.purchase.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.purchase.entity.SupplierRequest;
import org.example.storemanager.modules.purchase.entity.SupplierRequestDetail;
import org.example.storemanager.modules.purchase.repository.SupplierRequestDetailRepository;
import org.example.storemanager.modules.purchase.repository.SupplierRequestRepository;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping({"/api/v1/purchase/supplier-requests", "/api/v1/purchase/supplier-rfqs"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SupplierRequestController {

    private final SupplierRequestRepository supplierRequestRepository;
    private final SupplierRequestDetailRepository supplierRequestDetailRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        List<SupplierRequest> list = supplierRequestRepository.findAllWithDetails();

        if (status != null && !status.isBlank()) {
            list = list.stream().filter(r -> status.equalsIgnoreCase(r.getStatus())).toList();
        }
        if (search != null && !search.isBlank()) {
            final String q = search.toLowerCase().trim();
            list = list.stream().filter(r ->
                    (r.getRfqCode() != null && r.getRfqCode().toLowerCase().contains(q)) ||
                    (r.getSupplierName() != null && r.getSupplierName().toLowerCase().contains(q)) ||
                    (r.getDestinationBranch() != null && r.getDestinationBranch().toLowerCase().contains(q)) ||
                    (r.getHandler() != null && r.getHandler().toLowerCase().contains(q))
            ).toList();
        }

        List<Map<String, Object>> response = list.stream().map(this::mapToResponseMap).toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Map<String, Object>>> getById(@PathVariable Long id) {
        SupplierRequest rfq = supplierRequestRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("SupplierRequest", "id", id));
        return ResponseEntity.ok(ApiResponse.ok(mapToResponseMap(rfq)));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> req) {
        String rfqCode = (String) req.get("rfqCode");
        if (rfqCode == null || rfqCode.isBlank()) {
            rfqCode = "RFQ-2026-" + String.format("%04d", (int)(Math.random() * 9000 + 1000));
        }

        SupplierRequest rfq = SupplierRequest.builder()
                .rfqCode(rfqCode)
                .supplierName((String) req.get("supplierName"))
                .destinationBranch((String) req.get("destinationBranch"))
                .handler((String) req.get("handler"))
                .status((String) req.getOrDefault("status", "CHO_BAO_GIA"))
                .notes((String) req.get("notes"))
                .build();

        if (req.get("sentDate") != null) {
            try { rfq.setSentDate(LocalDate.parse(req.get("sentDate").toString().substring(0, 10))); } catch (Exception ignored) {}
        } else {
            rfq.setSentDate(LocalDate.now());
        }

        if (req.get("expiryDate") != null) {
            try { rfq.setExpiryDate(LocalDate.parse(req.get("expiryDate").toString().substring(0, 10))); } catch (Exception ignored) {}
        }

        if (req.get("selectedSuppliers") instanceof List<?> list) {
            rfq.setSelectedSuppliers(String.join(", ", list.stream().map(Object::toString).toList()));
        } else if (req.get("selectedSuppliers") != null) {
            rfq.setSelectedSuppliers(req.get("selectedSuppliers").toString());
        }

        rfq.setIsDeleted(false);
        SupplierRequest savedRfq = supplierRequestRepository.save(rfq);

        List<SupplierRequestDetail> details = new ArrayList<>();
        if (req.get("items") instanceof List<?> items) {
            for (Object obj : items) {
                if (obj instanceof Map<?, ?> item) {
                    SupplierRequestDetail d = SupplierRequestDetail.builder()
                            .supplierRequest(savedRfq)
                            .sku((String) item.get("sku"))
                            .productName(item.get("productName") != null ? item.get("productName").toString() : "Sản phẩm")
                            .quantity(item.get("quantity") != null ? new BigDecimal(item.get("quantity").toString()) : BigDecimal.ONE)
                            .unit(item.get("unit") != null ? item.get("unit").toString() : "Cái")
                            .specifications((String) item.get("specifications"))
                            .build();
                    d.setIsDeleted(false);
                    details.add(d);
                }
            }
        }
        if (!details.isEmpty()) {
            supplierRequestDetailRepository.saveAll(details);
            savedRfq.setDetails(details);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(mapToResponseMap(savedRfq)));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> update(
            @PathVariable Long id,
            @RequestBody Map<String, Object> req) {
        SupplierRequest existing = supplierRequestRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("SupplierRequest", "id", id));

        if (req.containsKey("supplierName")) existing.setSupplierName((String) req.get("supplierName"));
        if (req.containsKey("destinationBranch")) existing.setDestinationBranch((String) req.get("destinationBranch"));
        if (req.containsKey("handler")) existing.setHandler((String) req.get("handler"));
        if (req.containsKey("status")) existing.setStatus((String) req.get("status"));
        if (req.containsKey("notes")) existing.setNotes((String) req.get("notes"));

        if (req.get("expiryDate") != null) {
            try { existing.setExpiryDate(LocalDate.parse(req.get("expiryDate").toString().substring(0, 10))); } catch (Exception ignored) {}
        }
        if (req.get("selectedSuppliers") instanceof List<?> list) {
            existing.setSelectedSuppliers(String.join(", ", list.stream().map(Object::toString).toList()));
        }

        if (req.get("items") instanceof List<?> items) {
            // Xóa chi tiết cũ và thêm mới
            List<SupplierRequestDetail> oldDetails = supplierRequestDetailRepository.findBySupplierRequestIdAndIsDeletedFalse(id);
            for (SupplierRequestDetail od : oldDetails) {
                od.setIsDeleted(true);
            }
            supplierRequestDetailRepository.saveAll(oldDetails);

            List<SupplierRequestDetail> newDetails = new ArrayList<>();
            for (Object obj : items) {
                if (obj instanceof Map<?, ?> item) {
                    SupplierRequestDetail d = SupplierRequestDetail.builder()
                            .supplierRequest(existing)
                            .sku((String) item.get("sku"))
                            .productName(item.get("productName") != null ? item.get("productName").toString() : "Sản phẩm")
                            .quantity(item.get("quantity") != null ? new BigDecimal(item.get("quantity").toString()) : BigDecimal.ONE)
                            .unit(item.get("unit") != null ? item.get("unit").toString() : "Cái")
                            .specifications((String) item.get("specifications"))
                            .build();
                    d.setIsDeleted(false);
                    newDetails.add(d);
                }
            }
            supplierRequestDetailRepository.saveAll(newDetails);
            existing.setDetails(newDetails);
        }

        SupplierRequest saved = supplierRequestRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật yêu cầu báo giá thành công", mapToResponseMap(saved)));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        SupplierRequest existing = supplierRequestRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SupplierRequest", "id", id));
        existing.setIsDeleted(true);
        supplierRequestRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.ok("Xóa yêu cầu báo giá thành công", null));
    }

    private Map<String, Object> mapToResponseMap(SupplierRequest rfq) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", String.valueOf(rfq.getId()));
        map.put("rfqCode", rfq.getRfqCode());
        map.put("supplierName", rfq.getSupplierName() != null ? rfq.getSupplierName() : "");
        
        List<String> suppliersList = new ArrayList<>();
        if (rfq.getSelectedSuppliers() != null && !rfq.getSelectedSuppliers().isBlank()) {
            suppliersList = Arrays.stream(rfq.getSelectedSuppliers().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        } else if (rfq.getSupplierName() != null && !rfq.getSupplierName().isBlank()) {
            suppliersList.add(rfq.getSupplierName());
        }
        map.put("selectedSuppliers", suppliersList);
        map.put("supplierEmails", suppliersList.stream().map(s -> "contact@" + s.split(" ")[0].toLowerCase() + ".vn").toList());
        map.put("destinationBranch", rfq.getDestinationBranch() != null ? rfq.getDestinationBranch() : "Kho phân phối Trung tâm (Hà Nội)");
        map.put("sentDate", rfq.getSentDate() != null ? rfq.getSentDate().toString() : "");
        map.put("expiryDate", rfq.getExpiryDate() != null ? rfq.getExpiryDate().toString() : "");
        map.put("handler", rfq.getHandler() != null ? rfq.getHandler() : "Nhân viên thu mua");
        map.put("status", rfq.getStatus() != null ? rfq.getStatus() : "CHO_BAO_GIA");
        map.put("notes", rfq.getNotes() != null ? rfq.getNotes() : "");

        List<Map<String, Object>> items = new ArrayList<>();
        if (rfq.getDetails() != null) {
            for (SupplierRequestDetail d : rfq.getDetails()) {
                if (Boolean.TRUE.equals(d.getIsDeleted())) continue;
                Map<String, Object> itemMap = new LinkedHashMap<>();
                itemMap.put("id", String.valueOf(d.getId()));
                itemMap.put("sku", d.getSku() != null ? d.getSku() : "");
                itemMap.put("productName", d.getProductName());
                itemMap.put("quantity", d.getQuantity());
                itemMap.put("unit", d.getUnit() != null ? d.getUnit() : "Cái");
                itemMap.put("specifications", d.getSpecifications() != null ? d.getSpecifications() : "");
                items.add(itemMap);
            }
        }
        map.put("items", items);
        return map;
    }
}
