package org.example.storemanager;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.example.storemanager.modules.omnichannel.entity.ShippingCarrier;
import org.example.storemanager.modules.partnerarea.dto.request.customerdto.CreateCustomerRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StoremanagerApplicationTests {

	private static Validator validator;

	@BeforeAll
	static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	@DisplayName("Test valid customer phone with 09 prefix (10 digits) and valid email")
	void testValidCustomerRequest() {
		CreateCustomerRequest req = CreateCustomerRequest.builder()
				.name("Nguyễn Văn A")
				.phone("0912345678")
				.email("admin@storemanager.com")
				.build();

		Set<ConstraintViolation<CreateCustomerRequest>> violations = validator.validate(req);
		assertTrue(violations.isEmpty(), "Valid customer should have no constraint violations");
	}

	@Test
	@DisplayName("Test valid customer phone with +849 prefix")
	void testValidInternationalCustomerPhone() {
		CreateCustomerRequest req = CreateCustomerRequest.builder()
				.name("Nguyễn Văn A")
				.phone("+84912345678")
				.email("customer@gmail.com")
				.build();

		Set<ConstraintViolation<CreateCustomerRequest>> violations = validator.validate(req);
		assertTrue(violations.isEmpty(), "Valid +849 customer phone should pass validation");
	}

	@Test
	@DisplayName("Test invalid customer phone numbers: 03x, 08x, 07x, too short, too long")
	void testInvalidCustomerPhones() {
		String[] invalidPhones = {
				"0312345678", // 03 prefix
				"0812345678", // 08 prefix
				"0712345678", // 07 prefix
				"0912345",    // too short (7 digits)
				"091234567899", // too long (12 digits)
				"09abc12345", // letters
				""            // blank
		};

		for (String phone : invalidPhones) {
			CreateCustomerRequest req = CreateCustomerRequest.builder()
					.name("Test User")
					.phone(phone)
					.email("test@gmail.com")
					.build();

			Set<ConstraintViolation<CreateCustomerRequest>> violations = validator.validate(req);
			assertFalse(violations.isEmpty(), "Phone '" + phone + "' should fail validation");
		}
	}

	@Test
	@DisplayName("Test invalid emails: missing @, invalid format")
	void testInvalidCustomerEmails() {
		String[] invalidEmails = {
				"adminstoremanager.com",
				"admin@",
				"@domain.com",
				"plainaddress"
		};

		for (String email : invalidEmails) {
			CreateCustomerRequest req = CreateCustomerRequest.builder()
					.name("Test User")
					.phone("0912345678")
					.email(email)
					.build();

			Set<ConstraintViolation<CreateCustomerRequest>> violations = validator.validate(req);
			assertFalse(violations.isEmpty(), "Email '" + email + "' should fail validation");
		}
	}

	@Test
	@DisplayName("Test ShippingCarrier entity new fields")
	void testShippingCarrierFields() {
		ShippingCarrier carrier = ShippingCarrier.builder()
				.carrierCode("SHIP000001")
				.carrierName("Giao Hàng Nhanh (GHN)")
				.email("admin@storemanager.com")
				.phone("0912345678")
				.website("https://ghn.vn")
				.address("Số 100 Phố Kim Mã, Ba Đình, Hà Nội")
				.contactPerson("Nguyễn Văn Quản Lý")
				.notes("Hợp đồng VIP 2026")
				.build();

		assertEquals("admin@storemanager.com", carrier.getEmail());
		assertEquals("0912345678", carrier.getPhone());
		assertEquals("https://ghn.vn", carrier.getWebsite());
		assertEquals("Số 100 Phố Kim Mã, Ba Đình, Hà Nội", carrier.getAddress());
		assertEquals("Nguyễn Văn Quản Lý", carrier.getContactPerson());
		assertEquals("Hợp đồng VIP 2026", carrier.getNotes());
	}
}
