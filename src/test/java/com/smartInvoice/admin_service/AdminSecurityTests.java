package com.smartInvoice.admin_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminSecurityTests {
	@Autowired
	private MockMvc mockMvc;

	@Test
	void adminApisRejectRequestsWithoutGatewayAdminIdentity() throws Exception {
		mockMvc.perform(get("/admin/activity-logs"))
				.andExpect(status().isForbidden());
	}

	@Test
	void adminApisAllowRequestsWithGatewayForwardedAdminIdentity() throws Exception {
		mockMvc.perform(get("/admin/activity-logs")
						.header("X-User-Id", "admin_1")
						.header("X-User-Role", "ADMIN")
						.header("X-User-Email", "admin@example.com"))
				.andExpect(status().isOk());
	}

	@Test
	void adminApisRejectGatewayForwardedUserIdentity() throws Exception {
		mockMvc.perform(get("/admin/activity-logs")
						.header("X-User-Id", "usr_1")
						.header("X-User-Role", "USER")
						.header("X-User-Email", "user@example.com"))
				.andExpect(status().isForbidden());
	}
}
