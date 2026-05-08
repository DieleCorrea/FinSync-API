package com.financas.tema1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.financas.tema1.DTO.TransactionCreateDTO;
import com.financas.tema1.controller.TransactionController;
import com.financas.tema1.domain.Transaction;
import com.financas.tema1.domain.Category;
import com.financas.tema1.domain.User;
import com.financas.tema1.repository.TransactionRepository;
import com.financas.tema1.repository.UserRepository;
import com.financas.tema1.transaction.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@Import(com.financas.tema1.config.SecurityConfig.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionRepository transactionRepository;

    @MockitoBean
    private UserRepository userRepository;

    private ObjectMapper objectMapper;
    private User mockUser;
    private Transaction mockTransaction;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("user@test.com");
        mockUser.setPassword("encoded_password");

        mockTransaction = new Transaction(
                "Salário",
                new BigDecimal("5000.00"),
                Category.SALARY,
                LocalDate.now(),
                TransactionType.INCOME,
                mockUser
        );
        mockTransaction.setId(1L);
    }


    @Test
    @WithMockUser(username = "user@test.com")
    void createTransaction_success() throws Exception {
        TransactionCreateDTO dto = new TransactionCreateDTO(
                "Salário",
                new BigDecimal("5000.00"),
                Category.SALARY,
                LocalDate.now(),
                TransactionType.INCOME
        );

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(mockUser));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Salário"));
    }

    @Test
    void createTransaction_unauthenticated_returns401() throws Exception {
        TransactionCreateDTO dto = new TransactionCreateDTO(
                "Salário",
                new BigDecimal("5000.00"),
                Category.SALARY,
                LocalDate.now(),
                TransactionType.INCOME
        );

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser(username = "user@test.com")
    void getAll_success() throws Exception {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(mockUser));
        when(transactionRepository.findByUserIdOrderByDateDesc(1L))
                .thenReturn(List.of(mockTransaction));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Salário"));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void getAll_empty_returnsEmptyList() throws Exception {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(mockUser));
        when(transactionRepository.findByUserIdOrderByDateDesc(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }


    @Test
    @WithMockUser(username = "user@test.com")
    void getLast30Days_returnsOnlyRecentTransactions() throws Exception {
        Transaction old = new Transaction(
                "Antigo",
                new BigDecimal("100.00"),
                Category.SALARY,
                LocalDate.now().minusDays(60),
                TransactionType.INCOME,
                mockUser
        );
        old.setId(2L);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(mockUser));
        when(transactionRepository.findByUserIdOrderByDateDesc(1L))
                .thenReturn(List.of(mockTransaction, old));

        mockMvc.perform(get("/api/transactions/last30days"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Salário"));
    }


    @Test
    @WithMockUser(username = "user@test.com")
    void getSummary_groupsByCategory() throws Exception {
        Transaction t2 = new Transaction(
                "Freela",
                new BigDecimal("1000.00"),
                Category.SALARY,
                LocalDate.now(),
                TransactionType.INCOME,
                mockUser
        );
        t2.setId(2L);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(mockUser));
        when(transactionRepository.findByUserId(1L))
                .thenReturn(List.of(mockTransaction, t2));

        mockMvc.perform(get("/api/transactions/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.SALARY").value(6000.00)); // 5000 + 1000
    }
}