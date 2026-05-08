package com.financas.tema1;

import com.financas.tema1.DTO.UserRegisterDTO;
import com.financas.tema1.DTO.UserResponseDTO;
import com.financas.tema1.controller.AuthController;
import com.financas.tema1.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financas.tema1.domain.User;
import com.financas.tema1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(com.financas.tema1.config.SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private ObjectMapper objectMapper;
    private User mockUser;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("user@test.com");
        mockUser.setPassword("encoded_password");
    }


    @Test
    void register_success() throws Exception {
        UserRegisterDTO dto = new UserRegisterDTO("user@test.com", "senha123");
        UserResponseDTO response = new UserResponseDTO(1L, "user@test.com");

        when(userService.registerUser(any(UserRegisterDTO.class))).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("user@test.com"));
    }

    @Test
    void login_success() throws Exception {
        UserRegisterDTO dto = new UserRegisterDTO("user@test.com", "senha123");

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("senha123", "encoded_password")).thenReturn(true);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@test.com"));
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        UserRegisterDTO dto = new UserRegisterDTO("user@test.com", "senha_errada");

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("senha_errada", "encoded_password")).thenReturn(false);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_userNotFound_returns401() throws Exception {
        UserRegisterDTO dto = new UserRegisterDTO("naoexiste@test.com", "senha123");

        when(userRepository.findByEmail("naoexiste@test.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }
}