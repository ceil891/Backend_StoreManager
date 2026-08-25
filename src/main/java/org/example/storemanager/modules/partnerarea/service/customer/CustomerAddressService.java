package org.example.storemanager.modules.partnerarea.service.customer;

import org.example.storemanager.modules.partnerarea.dto.request.customerdto.CustomerAddressRequest;
import org.example.storemanager.modules.partnerarea.dto.response.customer.CustomerAddressResponse;

import java.util.List;

public interface CustomerAddressService {

    List<CustomerAddressResponse> getAddresses(Long customerId, String phone);

    CustomerAddressResponse getAddressById(Long id);

    CustomerAddressResponse createAddress(CustomerAddressRequest request);

    CustomerAddressResponse updateAddress(Long id, CustomerAddressRequest request);

    void deleteAddress(Long id);

    CustomerAddressResponse setDefaultAddress(Long id, Long customerId, String phone);
}
