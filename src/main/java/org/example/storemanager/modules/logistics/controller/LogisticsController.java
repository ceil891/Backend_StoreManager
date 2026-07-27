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

    // --- SHIPPERS ---
    @GetMapping("/shippers")
    public ResponseEntity<ApiResponse<List<Shipper>>> getAllShippers() {
        return ResponseEntity.ok(ApiResponse.ok(shipperRepository.findByIsDeletedFalse()));
    }

    @PostMapping("/shippers")
    public ResponseEntity<ApiResponse<Shipper>> createShipper(@RequestBody Shipper req) {
        req.setIsDeleted(false);
        return ResponseEntity.status(201).body(ApiResponse.created(shipperRepository.save(req)));
    }

    // --- DELIVERY TRIPS ---
    @GetMapping("/trips")
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
        return ResponseEntity.ok(ApiResponse.ok(shippingCarrierRepository.findByIsDeletedFalse()));
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

    // --- OTHERS DUMMY ---
    @GetMapping("/charges")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getShippingCharges() {
        return ResponseEntity.ok(ApiResponse.ok(new ArrayList<>()));
    }

    @GetMapping("/fee-rates")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getShippingFeeRates() {
        return ResponseEntity.ok(ApiResponse.ok(new ArrayList<>()));
    }

    @GetMapping("/fee-groups")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getShippingFeeGroups() {
        return ResponseEntity.ok(ApiResponse.ok(new ArrayList<>()));
    }

    @GetMapping("/shipments")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getShipments() {
        return ResponseEntity.ok(ApiResponse.ok(new ArrayList<>()));
    }

    @GetMapping("/notes")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getShippingNotes() {
        return ResponseEntity.ok(ApiResponse.ok(new ArrayList<>()));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getShippingOrders() {
        return ResponseEntity.ok(ApiResponse.ok(new ArrayList<>()));
    }

    @GetMapping("/locations")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getShippingLocations() {
        return ResponseEntity.ok(ApiResponse.ok(new ArrayList<>()));
    }

    @GetMapping("/contacts")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getShippingContacts() {
        return ResponseEntity.ok(ApiResponse.ok(new ArrayList<>()));
    }

    @GetMapping("/packing-lists")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPackingLists() {
        return ResponseEntity.ok(ApiResponse.ok(new ArrayList<>()));
    }

    @GetMapping("/delivery-notes")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDeliveryNotes() {
        return ResponseEntity.ok(ApiResponse.ok(new ArrayList<>()));
    }
}
