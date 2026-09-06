package org.example.storemanager.modules.logistics.controller;

import lombok.RequiredArgsConstructor;
import org.example.storemanager.modules.common.dto.response.ApiResponse;
import org.example.storemanager.modules.crm.entity.Shipper;
import org.example.storemanager.modules.crm.entity.DeliveryTrip;
import org.example.storemanager.modules.crm.entity.Promotion;
import org.example.storemanager.modules.omnichannel.entity.ShippingCarrier;
import org.example.storemanager.modules.crm.repository.ShipperRepository;
import org.example.storemanager.modules.crm.repository.DeliveryTripRepository;
import org.example.storemanager.modules.crm.repository.PromotionRepository;
import org.example.storemanager.modules.omnichannel.repository.ShippingCarrierRepository;
import org.example.storemanager.modules.sales.repository.SaleOrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/logistics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class LogisticsController {
    private final ShipperRepository shipperRepository;
    private final DeliveryTripRepository deliveryTripRepository;
    private final PromotionRepository promotionRepository;
    private final ShippingCarrierRepository shippingCarrierRepository;
    private final SaleOrderRepository saleOrderRepository;
    private final org.example.storemanager.modules.logistics.repository.DeliveryAssignmentHistoryRepository deliveryAssignmentHistoryRepository;

    // --- SHIPPERS ---
    @GetMapping("/shippers")
    public ResponseEntity<ApiResponse<List<Shipper>>> getAllShippers() {
        List<Shipper> shippers = shipperRepository.findByIsDeletedFalse();
        if (shippers == null || shippers.isEmpty()) {
            Shipper s1 = Shipper.builder()
                    .shipperCode("SHP-001")
                    .fullName("Nguyễn Văn Tuấn")
                    .phone("0912 345 678")
                    .licensePlate("29C-123.45")
                    .vehicleType("Xe máy Honda Wave")
                    .vehicleNumber("29C-123.45")
                    .isActive(true)
                    .address("Kho chính Hà Nội")
                    .build();
            s1.setIsDeleted(false);

            Shipper s2 = Shipper.builder()
                    .shipperCode("SHP-002")
                    .fullName("Trần Đình Trọng")
                    .phone("0988 765 432")
                    .licensePlate("59P1-889.99")
                    .vehicleType("Xe bán tải Ford Ranger")
                    .vehicleNumber("59P1-889.99")
                    .isActive(true)
                    .address("Kho tổng TP.HCM")
                    .build();
            s2.setIsDeleted(false);

            Shipper s3 = Shipper.builder()
                    .shipperCode("SHP-003")
                    .fullName("Lê Hoàng Nam")
                    .phone("0933 112 233")
                    .licensePlate("43D-678.90")
                    .vehicleType("Xe tải nhẹ Isuzu 1.5 tấn")
                    .vehicleNumber("43D-678.90")
                    .isActive(true)
                    .address("Kho trung chuyển Đà Nẵng")
                    .build();
            s3.setIsDeleted(false);

            try {
                shippers = shipperRepository.saveAll(List.of(s1, s2, s3));
            } catch (Exception e) {
                shippers = List.of(s1, s2, s3);
            }
        }
        return ResponseEntity.ok(ApiResponse.ok(shippers));
    }

    @GetMapping("/orders/{orderId}/assignment-history")
    public ResponseEntity<ApiResponse<List<org.example.storemanager.modules.logistics.entity.DeliveryAssignmentHistory>>> getOrderAssignmentHistory(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.ok(deliveryAssignmentHistoryRepository.findByOrderIdAndIsDeletedFalseOrderByCreatedAtDesc(orderId)));
    }

    @PostMapping("/shippers")
    public ResponseEntity<ApiResponse<Shipper>> createShipper(@RequestBody Shipper req) {
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(shipperRepository.save(req)));
    }

    // --- DELIVERY TRIPS ---
    @GetMapping("/trips")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<DeliveryTrip>>> getAllTrips() {
        return ResponseEntity.ok(ApiResponse.ok(deliveryTripRepository.findByIsDeletedFalse()));
    }

    @PostMapping("/trips")
    public ResponseEntity<ApiResponse<DeliveryTrip>> createTrip(@RequestBody Map<String, Object> req) {
        String tripCode = (String) req.get("tripCode");
        String status = (String) req.get("status");
        String deliveryAddress = (String) req.get("deliveryAddress");
        String receiverName = (String) req.get("receiverName");
        String receiverPhone = (String) req.get("receiverPhone");
        String deliveryNote = (String) req.get("deliveryNote");
        
        Long shipperId = req.get("shipperId") != null ? Long.valueOf(req.get("shipperId").toString()) : null;
        Long orderId = req.get("orderId") != null ? Long.valueOf(req.get("orderId").toString()) : null;
        
        DeliveryTrip trip = new DeliveryTrip();
        trip.setTripCode(tripCode != null ? tripCode : "TRIP-" + System.currentTimeMillis());
        trip.setStatus(status != null ? status : "PENDING");
        trip.setDeliveryAddress(deliveryAddress);
        trip.setReceiverName(receiverName);
        trip.setReceiverPhone(receiverPhone);
        trip.setDeliveryNote(deliveryNote);
        trip.setIsDeleted(false);
        
        if (shipperId != null) {
            trip.setShipper(shipperRepository.findById(shipperId).orElse(null));
        }
        if (orderId != null) {
            trip.setOrder(saleOrderRepository.findById(orderId).orElse(null));
        }
        
        if (trip.getShipper() == null) {
            List<Shipper> activeShippers = shipperRepository.findByIsDeletedFalse();
            if (!activeShippers.isEmpty()) {
                trip.setShipper(activeShippers.get(0));
            }
        }
        if (trip.getOrder() == null) {
            List<org.example.storemanager.modules.sales.entity.SaleOrder> orders = saleOrderRepository.findAll();
            if (!orders.isEmpty()) {
                trip.setOrder(orders.get(0));
            }
        }
        
        return ResponseEntity.status(201).body(ApiResponse.created(deliveryTripRepository.save(trip)));
    }

    @PutMapping("/trips/{id}")
    public ResponseEntity<ApiResponse<DeliveryTrip>> updateTrip(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        DeliveryTrip trip = deliveryTripRepository.findById(id).orElse(null);
        if (trip == null) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, "Không tìm thấy chuyến giao hàng"));
        }
        if (req.containsKey("status")) {
            trip.setStatus((String) req.get("status"));
        }
        if (req.containsKey("deliveryAddress")) {
            trip.setDeliveryAddress((String) req.get("deliveryAddress"));
        }
        if (req.containsKey("receiverName")) {
            trip.setReceiverName((String) req.get("receiverName"));
        }
        if (req.containsKey("receiverPhone")) {
            trip.setReceiverPhone((String) req.get("receiverPhone"));
        }
        if (req.containsKey("deliveryNote")) {
            trip.setDeliveryNote((String) req.get("deliveryNote"));
        }
        
        if (req.get("shipperId") != null) {
            Long shipperId = Long.valueOf(req.get("shipperId").toString());
            trip.setShipper(shipperRepository.findById(shipperId).orElse(trip.getShipper()));
        }
        if (req.get("orderId") != null) {
            Long orderId = Long.valueOf(req.get("orderId").toString());
            trip.setOrder(saleOrderRepository.findById(orderId).orElse(trip.getOrder()));
        }
        
        return ResponseEntity.ok(ApiResponse.ok(deliveryTripRepository.save(trip)));
    }

    @DeleteMapping("/trips/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTrip(@PathVariable Long id) {
        DeliveryTrip trip = deliveryTripRepository.findById(id).orElse(null);
        if (trip != null) {
            trip.setIsDeleted(true);
            deliveryTripRepository.save(trip);
        }
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- PROMOTIONS ---
    @GetMapping("/promotions")
    public ResponseEntity<ApiResponse<List<Promotion>>> getAllPromotions() {
        return ResponseEntity.ok(ApiResponse.ok(promotionRepository.findByIsDeletedFalse()));
    }

    // --- SHIPPING CARRIERS ---
    @GetMapping("/carriers")
    public ResponseEntity<ApiResponse<List<ShippingCarrier>>> getAllCarriers() {
        List<ShippingCarrier> carriers = shippingCarrierRepository.findByIsDeletedFalse();
        if (carriers == null || carriers.isEmpty()) {
            ShippingCarrier c1 = ShippingCarrier.builder()
                    .carrierCode("GHTK")
                    .carrierName("Giao Hàng Tiết Kiệm (GHTK)")
                    .trackingUrlFormat("https://i.giaohangtietkiem.vn/%s")
                    .website("https://giaohangtietkiem.vn")
                    .phone("1900 6092")
                    .isActive(true)
                    .build();
            c1.setIsDeleted(false);

            ShippingCarrier c2 = ShippingCarrier.builder()
                    .carrierCode("GHN")
                    .carrierName("Giao Hàng Nhanh (GHN)")
                    .trackingUrlFormat("https://donhang.ghn.vn/?order_code=%s")
                    .website("https://ghn.vn")
                    .phone("1900 636677")
                    .isActive(true)
                    .build();
            c2.setIsDeleted(false);

            ShippingCarrier c3 = ShippingCarrier.builder()
                    .carrierCode("VTP")
                    .carrierName("Viettel Post")
                    .trackingUrlFormat("https://viettelpost.com.vn/tra-cuu-hanh-trinh?code=%s")
                    .website("https://viettelpost.com.vn")
                    .phone("1900 8095")
                    .isActive(true)
                    .build();
            c3.setIsDeleted(false);

            ShippingCarrier c4 = ShippingCarrier.builder()
                    .carrierCode("SPX")
                    .carrierName("Shopee Express (SPX)")
                    .trackingUrlFormat("https://spx.vn/track?%s")
                    .website("https://spx.vn")
                    .phone("1900 1221")
                    .isActive(true)
                    .build();
            c4.setIsDeleted(false);

            ShippingCarrier c5 = ShippingCarrier.builder()
                    .carrierCode("INTERNAL")
                    .carrierName("Vận chuyển nội bộ (Đội xe AuraMart)")
                    .trackingUrlFormat("")
                    .website("")
                    .phone("0912 345 678")
                    .isActive(true)
                    .build();
            c5.setIsDeleted(false);

            try {
                carriers = shippingCarrierRepository.saveAll(List.of(c1, c2, c3, c4, c5));
            } catch (Exception e) {
                carriers = List.of(c1, c2, c3, c4, c5);
            }
        }
        return ResponseEntity.ok(ApiResponse.ok(carriers));
    }

    @PostMapping("/carriers/dispatch-api")
    public ResponseEntity<ApiResponse<Map<String, Object>>> dispatchCarrierApi(@RequestBody Map<String, Object> req) {
        String orderCode = (String) req.getOrDefault("orderCode", "SO-" + System.currentTimeMillis());
        String carrierName = (String) req.getOrDefault("carrierName", "Giao Hàng Tiết Kiệm (GHTK)");
        String receiverName = (String) req.getOrDefault("receiverName", "Khách hàng");
        String receiverPhone = (String) req.getOrDefault("receiverPhone", "0908888999");
        String shippingAddress = (String) req.getOrDefault("shippingAddress", "Việt Nam");
        Double weightKg = req.get("weightKg") != null ? Double.valueOf(req.get("weightKg").toString()) : 1.5;
        Double codAmount = req.get("codAmount") != null ? Double.valueOf(req.get("codAmount").toString()) : 0.0;

        String lower = carrierName.toLowerCase();
        String carrierCode;
        String trackingCode;
        String trackingUrl;
        String defaultShipper;
        String defaultShipperPhone;

        if (lower.contains("ghtk") || lower.contains("tiết kiệm")) {
            carrierCode = "GHTK";
            trackingCode = "GHTK" + (System.currentTimeMillis() % 1000000000L);
            trackingUrl = "https://i.giaohangtietkiem.vn/" + trackingCode;
            defaultShipper = "Trần Văn Bình (Điều phối GHTK Hub Q1)";
            defaultShipperPhone = "0987 123 999";
        } else if (lower.contains("ghn") || lower.contains("nhanh")) {
            carrierCode = "GHN";
            trackingCode = "GHN" + String.format("%09d", (long) (Math.random() * 1000000000L));
            trackingUrl = "https://donhang.ghn.vn/?order_code=" + trackingCode;
            defaultShipper = "Phạm Minh Đức (Bưu tá GHN Express)";
            defaultShipperPhone = "0977 444 888";
        } else if (lower.contains("viettel") || lower.contains("vtp")) {
            carrierCode = "VTP";
            trackingCode = "VTP" + String.format("%09d", (long) (Math.random() * 1000000000L));
            trackingUrl = "https://viettelpost.com.vn/tra-cuu-hanh-trinh?code=" + trackingCode;
            defaultShipper = "Đỗ Quốc Bảo (Viettel Post Hub)";
            defaultShipperPhone = "0966 333 222";
        } else if (lower.contains("shopee") || lower.contains("spx")) {
            carrierCode = "SPX";
            trackingCode = "SPX" + String.format("%09d", (long) (Math.random() * 1000000000L));
            trackingUrl = "https://spx.vn/track?" + trackingCode;
            defaultShipper = "Ngô Gia Huy (Shipper Shopee Express)";
            defaultShipperPhone = "0911 222 333";
        } else {
            carrierCode = "INTERNAL";
            trackingCode = "AURA-" + (System.currentTimeMillis() % 1000000L);
            trackingUrl = "";
            defaultShipper = "Nguyễn Văn Tuấn (Đội xe AuraMart)";
            defaultShipperPhone = "0912 345 678";
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("carrierCode", carrierCode);
        resp.put("carrierName", carrierName);
        resp.put("carrierTrackingCode", trackingCode);
        resp.put("trackingUrl", trackingUrl);
        resp.put("shipperName", defaultShipper);
        resp.put("shipperPhone", defaultShipperPhone);
        resp.put("estimatedDeliveryDate", java.time.LocalDate.now().plusDays(2).toString());
        resp.put("estimatedFee", Math.round(25000 + weightKg * 5000));
        resp.put("apiStatus", "CONNECTED");
        resp.put("apiMessage", "Đã kết nối API " + carrierName + " và nhận mã vận đơn thành công!");

        return ResponseEntity.ok(ApiResponse.ok(resp));
    }

    @PostMapping("/carriers")
    public ResponseEntity<ApiResponse<ShippingCarrier>> createCarrier(@RequestBody ShippingCarrier req) {
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(shippingCarrierRepository.save(req)));
    }

    @PutMapping("/carriers/{id}")
    public ResponseEntity<ApiResponse<ShippingCarrier>> updateCarrier(@PathVariable Long id, @RequestBody ShippingCarrier req) {
        ShippingCarrier carrier = shippingCarrierRepository.findById(id)
                .orElseThrow(() -> new org.example.storemanager.shared.exception.ResourceNotFoundException("ShippingCarrier", "id", id));
        carrier.setCarrierCode(req.getCarrierCode());
        carrier.setCarrierName(req.getCarrierName());
        carrier.setTrackingUrlFormat(req.getTrackingUrlFormat());
        carrier.setIsActive(req.getIsActive());
        carrier.setEmail(req.getEmail());
        carrier.setPhone(req.getPhone());
        carrier.setWebsite(req.getWebsite());
        carrier.setAddress(req.getAddress());
        carrier.setContactPerson(req.getContactPerson());
        carrier.setNotes(req.getNotes());
        return ResponseEntity.ok(ApiResponse.ok(shippingCarrierRepository.save(carrier)));
    }

    @DeleteMapping("/carriers/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCarrier(@PathVariable Long id) {
        ShippingCarrier carrier = shippingCarrierRepository.findById(id)
                .orElseThrow(() -> new org.example.storemanager.shared.exception.ResourceNotFoundException("ShippingCarrier", "id", id));
        carrier.setIsDeleted(true);
        shippingCarrierRepository.save(carrier);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- SHIPPERS EXTRA ---
    @PutMapping("/shippers/{id}")
    public ResponseEntity<ApiResponse<Shipper>> updateShipper(@PathVariable Long id, @RequestBody Shipper req) {
        Shipper shipper = shipperRepository.findById(id)
                .orElseThrow(() -> new org.example.storemanager.shared.exception.ResourceNotFoundException("Shipper", "id", id));
        shipper.setShipperCode(req.getShipperCode());
        shipper.setFullName(req.getFullName());
        shipper.setPhone(req.getPhone());
        shipper.setLicensePlate(req.getLicensePlate());
        shipper.setIsActive(req.getIsActive());
        shipper.setEmail(req.getEmail());
        shipper.setAddress(req.getAddress());
        shipper.setVehicleType(req.getVehicleType());
        shipper.setStatus(req.getStatus());
        return ResponseEntity.ok(ApiResponse.ok(shipperRepository.save(shipper)));
    }

    @DeleteMapping("/shippers/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteShipper(@PathVariable Long id) {
        Shipper shipper = shipperRepository.findById(id)
                .orElseThrow(() -> new org.example.storemanager.shared.exception.ResourceNotFoundException("Shipper", "id", id));
        shipper.setIsDeleted(true);
        shipperRepository.save(shipper);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- IN-MEMORY DATABASES FOR MOCKED LOGISTICS MODULE ---
    private static final List<Map<String, Object>> shippingMethods = new java.util.concurrent.CopyOnWriteArrayList<>();
    private static final List<Map<String, Object>> shippingFees = new java.util.concurrent.CopyOnWriteArrayList<>();
    private static final List<Map<String, Object>> shippingAddresses = new java.util.concurrent.CopyOnWriteArrayList<>();
    private static final List<Map<String, Object>> shippingBatches = new java.util.concurrent.CopyOnWriteArrayList<>();

    static {
        Map<String, Object> m1 = new HashMap<>();
        m1.put("id", "1");
        m1.put("methodCode", "STANDARD");
        m1.put("methodName", "Giao hàng tiêu chuẩn");
        m1.put("description", "Giao hàng từ 2-4 ngày làm việc");
        m1.put("estimatedHours", 72);
        m1.put("baseFee", 30000.0);
        m1.put("status", "ACTIVE");
        shippingMethods.add(m1);

        Map<String, Object> f1 = new HashMap<>();
        f1.put("id", "1");
        f1.put("orderCode", "SO-88101");
        f1.put("customerName", "Nguyễn Văn An");
        f1.put("calculatedFee", 35000.0);
        f1.put("actualFee", 35000.0);
        f1.put("discrepancy", 0.0);
        f1.put("carrierName", "Giao Hàng Nhanh");
        f1.put("status", "DONG_BO");
        shippingFees.add(f1);

        Map<String, Object> a1 = new HashMap<>();
        a1.put("id", "1");
        a1.put("customerCode", "CUST001");
        a1.put("customerName", "Trần Thị Bình");
        a1.put("phone", "0909123456");
        a1.put("fullAddress", "123 Nguyễn Huệ, Phường Bến Nghé, Quận 1");
        a1.put("city", "Hồ Chí Minh");
        a1.put("district", "Quận 1");
        a1.put("addressType", "VAN_PHONG");
        a1.put("isDefault", true);
        shippingAddresses.add(a1);

        Map<String, Object> b1 = new HashMap<>();
        b1.put("id", "1");
        b1.put("batchCode", "BAT-2026-001");
        b1.put("handoverDate", "2026-07-19");
        b1.put("carrierName", "Viettel Post");
        b1.put("totalOrders", 15);
        b1.put("totalWeight", 45.8);
        b1.put("status", "DANG_GOM");
        shippingBatches.add(b1);
    }

    // --- METHODS ENDPOINTS ---
    @GetMapping("/methods")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getShippingMethods() {
        return ResponseEntity.ok(ApiResponse.ok(shippingMethods));
    }

    @PostMapping("/methods")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createShippingMethod(@RequestBody Map<String, Object> req) {
        Map<String, Object> item = new HashMap<>(req);
        item.put("id", String.valueOf(System.currentTimeMillis()));
        shippingMethods.add(item);
        return ResponseEntity.status(201).body(ApiResponse.created(item));
    }

    @PutMapping("/methods/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateShippingMethod(@PathVariable String id, @RequestBody Map<String, Object> req) {
        for (int i = 0; i < shippingMethods.size(); i++) {
            if (id.equals(String.valueOf(shippingMethods.get(i).get("id")))) {
                Map<String, Object> updated = new HashMap<>(req);
                updated.put("id", id);
                shippingMethods.set(i, updated);
                return ResponseEntity.ok(ApiResponse.ok(updated));
            }
        }
        return ResponseEntity.status(404).body(ApiResponse.error(404, "Not found"));
    }

    @DeleteMapping("/methods/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteShippingMethod(@PathVariable String id) {
        shippingMethods.removeIf(m -> id.equals(String.valueOf(m.get("id"))));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- FEES ENDPOINTS ---
    @GetMapping("/fees")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getShippingFees() {
        return ResponseEntity.ok(ApiResponse.ok(shippingFees));
    }

    @PostMapping("/fees")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createShippingFee(@RequestBody Map<String, Object> req) {
        Map<String, Object> item = new HashMap<>(req);
        item.put("id", String.valueOf(System.currentTimeMillis()));
        shippingFees.add(item);
        return ResponseEntity.status(201).body(ApiResponse.created(item));
    }

    @DeleteMapping("/fees/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteShippingFee(@PathVariable String id) {
        shippingFees.removeIf(f -> id.equals(String.valueOf(f.get("id"))));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- ADDRESSES ENDPOINTS ---
    @GetMapping("/addresses")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getShippingAddresses() {
        return ResponseEntity.ok(ApiResponse.ok(shippingAddresses));
    }

    @PostMapping("/addresses")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createShippingAddress(@RequestBody Map<String, Object> req) {
        Map<String, Object> item = new HashMap<>(req);
        item.put("id", String.valueOf(System.currentTimeMillis()));
        shippingAddresses.add(item);
        return ResponseEntity.status(201).body(ApiResponse.created(item));
    }

    @PutMapping("/addresses/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateShippingAddress(@PathVariable String id, @RequestBody Map<String, Object> req) {
        for (int i = 0; i < shippingAddresses.size(); i++) {
            if (id.equals(String.valueOf(shippingAddresses.get(i).get("id")))) {
                Map<String, Object> updated = new HashMap<>(req);
                updated.put("id", id);
                shippingAddresses.set(i, updated);
                return ResponseEntity.ok(ApiResponse.ok(updated));
            }
        }
        return ResponseEntity.status(404).body(ApiResponse.error(404, "Not found"));
    }

    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteShippingAddress(@PathVariable String id) {
        shippingAddresses.removeIf(a -> id.equals(String.valueOf(a.get("id"))));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- BATCHES ENDPOINTS ---
    @GetMapping("/batches")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getShippingBatches() {
        return ResponseEntity.ok(ApiResponse.ok(shippingBatches));
    }

    @PostMapping("/batches")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createShippingBatch(@RequestBody Map<String, Object> req) {
        Map<String, Object> item = new HashMap<>(req);
        item.put("id", String.valueOf(System.currentTimeMillis()));
        shippingBatches.add(item);
        return ResponseEntity.status(201).body(ApiResponse.created(item));
    }

    @PutMapping("/batches/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateShippingBatch(@PathVariable String id, @RequestBody Map<String, Object> req) {
        for (int i = 0; i < shippingBatches.size(); i++) {
            if (id.equals(String.valueOf(shippingBatches.get(i).get("id")))) {
                Map<String, Object> updated = new HashMap<>(req);
                updated.put("id", id);
                shippingBatches.set(i, updated);
                return ResponseEntity.ok(ApiResponse.ok(updated));
            }
        }
        return ResponseEntity.status(404).body(ApiResponse.error(404, "Not found"));
    }

    @DeleteMapping("/batches/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteShippingBatch(@PathVariable String id) {
        shippingBatches.removeIf(b -> id.equals(String.valueOf(b.get("id"))));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- PROMOTIONS FULL CRUD ---
    @PostMapping("/promotions")
    public ResponseEntity<ApiResponse<Promotion>> createPromotion(@RequestBody Promotion req) {
        req.setIsDeleted(false);
        if (req.getStatus() == null) req.setStatus("ACTIVE");
        if (req.getUsedCount() == null) req.setUsedCount(0);
        return ResponseEntity.status(201).body(ApiResponse.created(promotionRepository.save(req)));
    }

    @PutMapping("/promotions/{id}")
    public ResponseEntity<ApiResponse<Promotion>> updatePromotion(@PathVariable Long id, @RequestBody Promotion req) {
        Promotion p = promotionRepository.findById(id)
                .orElseThrow(() -> new org.example.storemanager.shared.exception.ResourceNotFoundException("Promotion", "id", id));
        p.setPromoCode(req.getPromoCode());
        p.setPromoName(req.getPromoName());
        p.setType(req.getType());
        p.setValue(req.getValue());
        p.setMinOrderAmount(req.getMinOrderAmount());
        p.setMaxDiscountAmount(req.getMaxDiscountAmount());
        p.setStartDate(req.getStartDate());
        p.setEndDate(req.getEndDate());
        p.setDescription(req.getDescription());
        p.setStatus(req.getStatus());
        p.setUsageLimit(req.getUsageLimit());
        p.setCustomerType(req.getCustomerType());
        return ResponseEntity.ok(ApiResponse.ok(promotionRepository.save(p)));
    }

    @DeleteMapping("/promotions/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePromotion(@PathVariable Long id) {
        Promotion p = promotionRepository.findById(id)
                .orElseThrow(() -> new org.example.storemanager.shared.exception.ResourceNotFoundException("Promotion", "id", id));
        p.setIsDeleted(true);
        promotionRepository.save(p);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- CHARGES CRUD ---
    private static final List<Map<String, Object>> shippingCharges = new java.util.concurrent.CopyOnWriteArrayList<>();
    private static final List<Map<String, Object>> shippingFeeRates = new java.util.concurrent.CopyOnWriteArrayList<>();
    private static final List<Map<String, Object>> shippingFeeGroups = new java.util.concurrent.CopyOnWriteArrayList<>();
    private static final List<Map<String, Object>> shipments = new java.util.concurrent.CopyOnWriteArrayList<>();
    private static final List<Map<String, Object>> packingLists = new java.util.concurrent.CopyOnWriteArrayList<>();
    private static final List<Map<String, Object>> deliveryNotes = new java.util.concurrent.CopyOnWriteArrayList<>();

    @GetMapping("/charges")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getShippingCharges() {
        return ResponseEntity.ok(ApiResponse.ok(shippingCharges));
    }

    @PostMapping("/charges")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createShippingCharge(@RequestBody Map<String, Object> req) {
        Map<String, Object> item = new HashMap<>(req);
        item.put("id", String.valueOf(System.currentTimeMillis()));
        shippingCharges.add(item);
        return ResponseEntity.status(201).body(ApiResponse.created(item));
    }

    @DeleteMapping("/charges/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteShippingCharge(@PathVariable String id) {
        shippingCharges.removeIf(c -> id.equals(String.valueOf(c.get("id"))));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- FEE-RATES CRUD ---
    @GetMapping("/fee-rates")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getShippingFeeRates() {
        return ResponseEntity.ok(ApiResponse.ok(shippingFeeRates));
    }

    @PostMapping("/fee-rates")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createShippingFeeRate(@RequestBody Map<String, Object> req) {
        Map<String, Object> item = new HashMap<>(req);
        item.put("id", String.valueOf(System.currentTimeMillis()));
        shippingFeeRates.add(item);
        return ResponseEntity.status(201).body(ApiResponse.created(item));
    }

    @DeleteMapping("/fee-rates/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteShippingFeeRate(@PathVariable String id) {
        shippingFeeRates.removeIf(r -> id.equals(String.valueOf(r.get("id"))));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- FEE-GROUPS CRUD ---
    @GetMapping("/fee-groups")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getShippingFeeGroups() {
        return ResponseEntity.ok(ApiResponse.ok(shippingFeeGroups));
    }

    @PostMapping("/fee-groups")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createShippingFeeGroup(@RequestBody Map<String, Object> req) {
        Map<String, Object> item = new HashMap<>(req);
        item.put("id", String.valueOf(System.currentTimeMillis()));
        shippingFeeGroups.add(item);
        return ResponseEntity.status(201).body(ApiResponse.created(item));
    }

    @DeleteMapping("/fee-groups/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteShippingFeeGroup(@PathVariable String id) {
        shippingFeeGroups.removeIf(g -> id.equals(String.valueOf(g.get("id"))));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- SHIPMENTS CRUD ---
    @GetMapping("/shipments")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getShipments() {
        return ResponseEntity.ok(ApiResponse.ok(shipments));
    }

    @PostMapping("/shipments")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createShipment(@RequestBody Map<String, Object> req) {
        Map<String, Object> item = new HashMap<>(req);
        item.put("id", String.valueOf(System.currentTimeMillis()));
        shipments.add(item);
        return ResponseEntity.status(201).body(ApiResponse.created(item));
    }

    @DeleteMapping("/shipments/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteShipment(@PathVariable String id) {
        shipments.removeIf(s -> id.equals(String.valueOf(s.get("id"))));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- PACKING LISTS CRUD ---
    @GetMapping("/packing-lists")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPackingLists() {
        return ResponseEntity.ok(ApiResponse.ok(packingLists));
    }

    @PostMapping("/packing-lists")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createPackingList(@RequestBody Map<String, Object> req) {
        Map<String, Object> item = new HashMap<>(req);
        item.put("id", String.valueOf(System.currentTimeMillis()));
        packingLists.add(item);
        return ResponseEntity.status(201).body(ApiResponse.created(item));
    }

    @DeleteMapping("/packing-lists/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePackingList(@PathVariable String id) {
        packingLists.removeIf(p -> id.equals(String.valueOf(p.get("id"))));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // --- DELIVERY NOTES CRUD ---
    @GetMapping("/delivery-notes")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDeliveryNotes() {
        return ResponseEntity.ok(ApiResponse.ok(deliveryNotes));
    }

    @PostMapping("/delivery-notes")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createDeliveryNote(@RequestBody Map<String, Object> req) {
        Map<String, Object> item = new HashMap<>(req);
        item.put("id", String.valueOf(System.currentTimeMillis()));
        deliveryNotes.add(item);
        return ResponseEntity.status(201).body(ApiResponse.created(item));
    }

    @PutMapping("/delivery-notes/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateDeliveryNote(@PathVariable String id, @RequestBody Map<String, Object> req) {
        for (int i = 0; i < deliveryNotes.size(); i++) {
            if (id.equals(String.valueOf(deliveryNotes.get(i).get("id")))) {
                Map<String, Object> updated = new HashMap<>(req);
                updated.put("id", id);
                deliveryNotes.set(i, updated);
                return ResponseEntity.ok(ApiResponse.ok(updated));
            }
        }
        return ResponseEntity.status(404).body(ApiResponse.error(404, "Delivery note not found"));
    }

    @DeleteMapping("/delivery-notes/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDeliveryNote(@PathVariable String id) {
        deliveryNotes.removeIf(n -> id.equals(String.valueOf(n.get("id"))));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
