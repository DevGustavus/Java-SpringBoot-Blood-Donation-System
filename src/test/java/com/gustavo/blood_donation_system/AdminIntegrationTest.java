package com.gustavo.blood_donation_system;

import com.jayway.jsonpath.JsonPath;
import com.gustavo.blood_donation_system.user.domain.User;
import com.gustavo.blood_donation_system.user.domain.UserRole;
import com.gustavo.blood_donation_system.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class AdminIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void createAdmin() {
        if (!userRepository.existsByEmail("admin@example.com")) {
            userRepository.save(User.create("admin@example.com",
                    passwordEncoder.encode("admin123"), UserRole.ADMIN));
        }
    }

    @Test
    void regularUserCannotAccessAdminEndpoints() throws Exception {
        String userToken = registerAndLogin("comum@example.com", "Usuario Comum");

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/v1/admin/users/1")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanListUsersPaginated() throws Exception {
        String adminToken = login("admin@example.com", "admin123");

        mockMvc.perform(get("/api/v1/admin/users")
                        .param("page", "0")
                        .param("size", "5")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].email").isArray())
                .andExpect(jsonPath("$.content[*].email")
                        .value(org.hamcrest.Matchers.hasItem("admin@example.com")))
                .andExpect(jsonPath("$.content[*].passwordHash").doesNotExist())
                .andExpect(jsonPath("$.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    void adminCanDeleteUserAndDeletedUserProfileBecomesUnavailable() throws Exception {
        registerAndLogin("vitima@example.com", "Vitima");
        String victimToken = login("vitima@example.com", "password123");
        String adminToken = login("admin@example.com", "admin123");

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + victimToken))
                .andExpect(status().isOk());

        String adminList = mockMvc.perform(get("/api/v1/admin/users")
                        .param("size", "100")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Number victimId = ((java.util.List<Number>) JsonPath.read(adminList,
                "$.content[?(@.email == 'vitima@example.com')].id")).get(0);

        mockMvc.perform(delete("/api/v1/admin/users/" + victimId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + victimToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminDeletingOwnAccountReturns409() throws Exception {
        String adminToken = login("admin@example.com", "admin123");

        String adminList = mockMvc.perform(get("/api/v1/admin/users")
                        .param("size", "100")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Number adminId = ((java.util.List<Number>) JsonPath.read(adminList,
                "$.content[?(@.email == 'admin@example.com')].id")).get(0);

        mockMvc.perform(delete("/api/v1/admin/users/" + adminId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void adminDeletingNonexistentUserReturns404() throws Exception {
        String adminToken = login("admin@example.com", "admin123");

        mockMvc.perform(delete("/api/v1/admin/users/999999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    private String registerAndLogin(String email, String name) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","email":"%s","password":"password123"}
                                """.formatted(name, email)))
                .andExpect(status().isCreated());
        return login(email, "password123");
    }

    private String login(String email, String password) throws Exception {
        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(loginResponse, "$.accessToken");
    }
}
