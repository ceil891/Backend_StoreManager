package org.example.storemanager.modules.partnerarea.service.customer.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.storemanager.modules.partnerarea.dto.request.customerdto.CustomerAddressRequest;
import org.example.storemanager.modules.partnerarea.dto.response.customer.CustomerAddressResponse;
import org.example.storemanager.modules.partnerarea.entity.Customer;
import org.example.storemanager.modules.partnerarea.entity.CustomerAddress;
import org.example.storemanager.modules.partnerarea.repository.CustomerAddressRepository;
import org.example.storemanager.modules.partnerarea.repository.CustomerRepository;
import org.example.storemanager.modules.partnerarea.service.customer.CustomerAddressService;
import org.example.storemanager.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerAddressServiceImpl implements CustomerAddressService {

    private final CustomerAddressRepository addressRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public List<CustomerAddressResponse> getAddresses(Long customerId, String phone) {
        String cleanPhone = phone != null ? phone.replace(" ", "").trim() : null;
        if (cleanPhone != null && cleanPhone.isBlank()) {
            cleanPhone = null;
        }

        // Prefer the immutable customer id.  Falling back to a phone number is
        // only for legacy customers that do not yet have an id; never union both
        // identities because that can expose another customer's address book.
        List<CustomerAddress> list = customerId != null
                ? addressRepository.findByCustomerIdAndIsDeletedFalseOrderByIdDesc(customerId)
                : (cleanPhone != null ? addressRepository.findByCustomerPhoneAndIsDeletedFalseOrderByIdDesc(cleanPhone) : new ArrayList<>());

        if (list == null) {
            list = new ArrayList<>();
        }

        return list.stream().map(CustomerAddressResponse::fromEntity).collect(Collectors.toList());
    }

    @Override
    public CustomerAddressResponse getAddressById(Long id) {
        CustomerAddress address = addressRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerAddress", "id", id));
        return CustomerAddressResponse.fromEntity(address);
    }

    @Override
    @Transactional
    public CustomerAddressResponse createAddress(CustomerAddressRequest request) {
        String cleanPhone = request.getCustomerPhone() != null ? request.getCustomerPhone().replace(" ", "").trim() : null;
        if (cleanPhone == null && request.getPhoneNumber() != null) {
            cleanPhone = request.getPhoneNumber().replace(" ", "").trim();
        }

        // If this is set as default or first address, reset other addresses
        List<CustomerAddress> existing = getAddressEntities(request.getCustomerId(), cleanPhone);
        boolean shouldBeDefault = Boolean.TRUE.equals(request.getIsDefault()) || existing.isEmpty();

        if (shouldBeDefault) {
            resetDefaultFlag(request.getCustomerId(), cleanPhone);
        }

        CustomerAddress address = CustomerAddress.builder()
                .customerId(request.getCustomerId())
                .customerPhone(cleanPhone)
                .recipientName(request.getRecipientName() != null ? request.getRecipientName() : "Khách hàng Online")
                .phoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber() : cleanPhone)
                .province(request.getProvince())
                .district(request.getDistrict())
                .ward(request.getWard())
                .street(request.getStreet())
                .fullAddress(request.getFullAddress())
                .addressType(request.getAddressType() != null ? request.getAddressType() : "HOME")
                .isDefault(shouldBeDefault)
                .notes(request.getNotes())
                .build();
        address.setIsDeleted(false);

        CustomerAddress saved = addressRepository.save(address);
        return CustomerAddressResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public CustomerAddressResponse updateAddress(Long id, CustomerAddressRequest request) {
        CustomerAddress address = addressRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerAddress", "id", id));

        String cleanPhone = request.getCustomerPhone() != null ? request.getCustomerPhone().replace(" ", "").trim() : address.getCustomerPhone();
        Long custId = request.getCustomerId() != null ? request.getCustomerId() : address.getCustomerId();

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            resetDefaultFlag(custId, cleanPhone);
            address.setIsDefault(true);
        } else if (request.getIsDefault() != null) {
            address.setIsDefault(request.getIsDefault());
        }

        if (request.getRecipientName() != null) address.setRecipientName(request.getRecipientName());
        if (request.getPhoneNumber() != null) address.setPhoneNumber(request.getPhoneNumber());
        if (request.getProvince() != null) address.setProvince(request.getProvince());
        if (request.getDistrict() != null) address.setDistrict(request.getDistrict());
        if (request.getWard() != null) address.setWard(request.getWard());
        if (request.getStreet() != null) address.setStreet(request.getStreet());
        if (request.getFullAddress() != null) address.setFullAddress(request.getFullAddress());
        if (request.getAddressType() != null) address.setAddressType(request.getAddressType());
        if (request.getNotes() != null) address.setNotes(request.getNotes());

        CustomerAddress saved = addressRepository.save(address);
        return CustomerAddressResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public void deleteAddress(Long id) {
        CustomerAddress address = addressRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerAddress", "id", id));
        address.setIsDeleted(true);
        addressRepository.save(address);
    }

    @Override
    @Transactional
    public CustomerAddressResponse setDefaultAddress(Long id, Long customerId, String phone) {
        CustomerAddress address = addressRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerAddress", "id", id));

        String cleanPhone = phone != null ? phone.replace(" ", "").trim() : address.getCustomerPhone();
        Long targetCustId = customerId != null ? customerId : address.getCustomerId();

        resetDefaultFlag(targetCustId, cleanPhone);
        address.setIsDefault(true);
        CustomerAddress saved = addressRepository.save(address);
        return CustomerAddressResponse.fromEntity(saved);
    }

    private List<CustomerAddress> getAddressEntities(Long customerId, String phone) {
        if (customerId != null) return addressRepository.findByCustomerIdAndIsDeletedFalseOrderByIdDesc(customerId);
        return phone != null && !phone.isBlank()
                ? addressRepository.findByCustomerPhoneAndIsDeletedFalseOrderByIdDesc(phone)
                : new ArrayList<>();
    }

    private void resetDefaultFlag(Long customerId, String phone) {
        if (customerId != null) {
            addressRepository.resetDefaultFlagForCustomerId(customerId);
        } else if (phone != null && !phone.isBlank()) {
            addressRepository.resetDefaultFlagForCustomerPhone(phone);
        }
    }
}
