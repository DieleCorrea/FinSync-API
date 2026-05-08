package com.financas.tema1;

import com.financas.tema1.controller.IngestionController;
import com.financas.tema1.domain.Category;
import com.financas.tema1.domain.Transaction;

import com.financas.tema1.domain.User;
import com.financas.tema1.repository.UserRepository;
import com.financas.tema1.service.IngestionService;
import com.financas.tema1.transaction.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IngestionController.class)
@Import(com.financas.tema1.config.SecurityConfig.class)
class IngestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IngestionService ingestionService;

    @MockitoBean
    private UserRepository userRepository;

    private User mockUser;
    private Transaction mockTransaction;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("user@test.com");

        mockTransaction = new Transaction(
                "Importado",
                new BigDecimal("200.00"),
                Category.SALARY,
                LocalDate.now(),
                TransactionType.INCOME,
                mockUser
        );
        mockTransaction.setId(1L);
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void ingest_success() throws Exception {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(mockUser));
        when(ingestionService.ingestFromExternalSources(any(User.class)))
                .thenReturn(List.of(mockTransaction));

        mockMvc.perform(post("/api/ingest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Importado"));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void ingest_empty_returnsEmptyList() throws Exception {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(mockUser));
        when(ingestionService.ingestFromExternalSources(any(User.class))).thenReturn(List.of());

        mockMvc.perform(post("/api/ingest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void ingest_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/ingest"))
                .andExpect(status().isUnauthorized());
    }
}