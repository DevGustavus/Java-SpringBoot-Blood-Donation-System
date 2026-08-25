package com.gustavo.blood_donation_system;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class DonorFlowIntegrationTest {

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

    @Test
    void enableAvailabilityAndSearchExposesOnlyPublicData() throws Exception {
        String token = registerAndLogin("joana@example.com", "Joana Silva");
        completeProfile(token, "Joana Silva", "1990-04-10", "68.0", "O_NEGATIVE", "Uberaba", "MG");

        mockMvc.perform(patch("/api/v1/donors/me/availability")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"available": true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));

        mockMvc.perform(get("/api/v1/donors")
                        .param("city", "Uberaba")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].fullName").value("Joana Silva"))
                .andExpect(jsonPath("$.content[0].bloodType").value("O_NEGATIVE"))
                .andExpect(jsonPath("$.content[0].city").value("Uberaba"))
                .andExpect(jsonPath("$.content[0].email").doesNotExist())
                .andExpect(jsonPath("$.content[0].phone").doesNotExist())
                .andExpect(jsonPath("$.content[0].address").doesNotExist())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void searchFiltersByBloodTypeCityAndState() throws Exception {
        String tokenA = registerAndLogin("doador.a@example.com", "Doador A");
        completeProfile(tokenA, "Doador A", "1990-04-10", "68.0", "O_NEGATIVE", "Araxa", "MG");
        activateAvailability(tokenA);

        String tokenB = registerAndLogin("doador.b@example.com", "Doador B");
        completeProfile(tokenB, "Doador B", "1985-08-20", "80.0", "A_POSITIVE", "Belo Horizonte", "MG");
        activateAvailability(tokenB);

        mockMvc.perform(get("/api/v1/donors")
                        .param("city", "Araxa")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].fullName").value("Doador A"));

        mockMvc.perform(get("/api/v1/donors")
                        .param("bloodType", "O_NEGATIVE")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].fullName", hasItem("Doador A")));

        mockMvc.perform(get("/api/v1/donors")
                        .param("state", "MG")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].fullName", hasItem("Doador A")))
                .andExpect(jsonPath("$.content[*].fullName", hasItem("Doador B")));

        mockMvc.perform(get("/api/v1/donors")
                        .param("city", "Porto Alegre")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void searchIsPaginated() throws Exception {
        registerDonor("pagina.1@example.com", "Pagina Um", "A_POSITIVE", "Frutal");
        registerDonor("pagina.2@example.com", "Pagina Dois", "B_POSITIVE", "Frutal");
        String token = registerDonor("pagina.3@example.com", "Pagina Tres", "O_POSITIVE", "Frutal");

        mockMvc.perform(get("/api/v1/donors")
                        .param("city", "Frutal")
                        .param("page", "0")
                        .param("size", "2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.number").value(0));

        mockMvc.perform(get("/api/v1/donors")
                        .param("city", "Frutal")
                        .param("page", "1")
                        .param("size", "2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void searchByZipCodeIgnoresHyphen() throws Exception {
        String token = registerAndLogin("cep@example.com", "Doador CEP");
        mockMvc.perform(put("/api/v1/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Doador CEP",
                                  "phone": "(34) 90000-0000",
                                  "birthDate": "1990-01-01",
                                  "weightKg": 70,
                                  "heightCm": 170,
                                  "bloodType": "O_POSITIVE",
                                  "address": "Rua X, 1",
                                  "city": "Sacramento",
                                  "state": "MG",
                                  "zipCode": "39010-000"
                                }
                                """))
                .andExpect(status().isOk());
        activateAvailability(token);

        mockMvc.perform(get("/api/v1/donors")
                        .param("zipCode", "39010-000")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].fullName").value("Doador CEP"))
                .andExpect(jsonPath("$.content[0].zipCode").value("39010-000"));

        mockMvc.perform(get("/api/v1/donors")
                        .param("zipCode", "39010000")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].fullName").value("Doador CEP"));
    }

    @Test
    void getDonorByIdReturnsPublicProfileOr404() throws Exception {
        String token = registerAndLogin("carlos@example.com", "Carlos Pereira");
        completeProfile(token, "Carlos Pereira", "1992-07-01", "90.0", "B_POSITIVE", "Uberlandia", "MG");
        activateAvailability(token);

        String searchResponse = mockMvc.perform(get("/api/v1/donors")
                        .param("city", "Uberlandia")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Number donorId = JsonPath.read(searchResponse, "$.content[0].id");

        mockMvc.perform(get("/api/v1/donors/" + donorId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Carlos Pereira"))
                .andExpect(jsonPath("$.email").doesNotExist());

        mockMvc.perform(get("/api/v1/donors/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void unavailableDonorIsHiddenFromSearchAndDetails() throws Exception {
        String token = registerAndLogin("marcos@example.com", "Marcos Lima");
        completeProfile(token, "Marcos Lima", "1991-01-15", "75.0", "AB_POSITIVE", "Patos de Minas", "MG");
        activateAvailability(token);

        String searchResponse = mockMvc.perform(get("/api/v1/donors")
                        .param("city", "Patos de Minas")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Number donorId = JsonPath.read(searchResponse, "$.content[0].id");

        mockMvc.perform(patch("/api/v1/donors/me/availability")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"available": false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));

        mockMvc.perform(get("/api/v1/donors")
                        .param("city", "Patos de Minas")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());

        mockMvc.perform(get("/api/v1/donors/" + donorId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void enablingAvailabilityWithIncompleteProfileReturns422() throws Exception {
        String token = registerAndLogin("incompleto@example.com", "Perfil Incompleto");

        mockMvc.perform(patch("/api/v1/donors/me/availability")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"available": true}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"));
    }

    @Test
    void searchWithAvailableFalseReturnsEmpty() throws Exception {
        String token = registerAndLogin("oculto@example.com", "Doador Oculto");
        completeProfile(token, "Doador Oculto", "1993-02-02", "70.0", "A_NEGATIVE", "Ituiutaba", "MG");
        activateAvailability(token);

        mockMvc.perform(get("/api/v1/donors")
                        .param("available", "false")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void requestWithInvalidTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/donors")
                        .header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void corsAllowsConfiguredOrigin() throws Exception {
        mockMvc.perform(options("/api/v1/donors")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    private String registerDonor(String email, String name, String bloodType, String city) throws Exception {
        String token = registerAndLogin(email, name);
        completeProfile(token, name, "1994-06-10", "72.0", bloodType, city, "MG");
        activateAvailability(token);
        return token;
    }

    private void activateAvailability(String token) throws Exception {
        mockMvc.perform(patch("/api/v1/donors/me/availability")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"available": true}
                                """))
                .andExpect(status().isOk());
    }

    private void completeProfile(String token, String name, String birthDate, String weightKg, String bloodType,
                                 String city, String state) throws Exception {
        mockMvc.perform(put("/api/v1/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "%s",
                                  "phone": "(34) 90000-0000",
                                  "birthDate": "%s",
                                  "weightKg": %s,
                                  "heightCm": 172,
                                  "bloodType": "%s",
                                  "address": "Rua A, 10",
                                  "city": "%s",
                                  "state": "%s",
                                  "zipCode": "38010-000"
                                }
                                """.formatted(name, birthDate, weightKg, bloodType, city, state)))
                .andExpect(status().isOk());
    }

    private String registerAndLogin(String email, String name) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","email":"%s","password":"password123"}
                                """.formatted(name, email)))
                .andExpect(status().isCreated());

        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"password123"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(loginResponse, "$.accessToken");
    }
}
