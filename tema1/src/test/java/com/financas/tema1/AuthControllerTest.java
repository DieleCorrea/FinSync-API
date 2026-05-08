package com.financas.tema1;

import com.financas.tema1.DTO.UserRegisterDTO;
import com.financas.tema1.DTO.UserResponseDTO;
import com.financas.tema1.controller.AuthController;
import com.financas.tema1.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    private UserRegisterDTO dto;
    private UserResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        dto = new UserRegisterDTO("user@email.com", "senha123");
        responseDTO = new UserResponseDTO(1L, "user@email.com");
    }

    @Test
    @DisplayName("Deve retornar status 200 ao registrar usuário")
    void shouldReturn200WhenRegisteringUser() {
        when(userService.registerUser(any(UserRegisterDTO.class))).thenReturn(responseDTO);

        ResponseEntity<UserResponseDTO> response = authController.register(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Deve retornar o body com os dados do usuário registrado")
    void shouldReturnUserDataInBody() {
        when(userService.registerUser(any(UserRegisterDTO.class))).thenReturn(responseDTO);

        ResponseEntity<UserResponseDTO> response = authController.register(dto);

        assertThat(response.getBody()).isNotNull();

        assertThat(response.getBody().email()).isEqualTo("user@email.com");
        assertThat(response.getBody().id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Deve delegar o registro para o UserService")
    void shouldDelegateToUserService() {
        when(userService.registerUser(any(UserRegisterDTO.class))).thenReturn(responseDTO);

        authController.register(dto);

        verify(userService, times(1)).registerUser(dto);
    }

    @Test
    @DisplayName("Deve chamar registerUser exatamente uma vez")
    void shouldCallRegisterUserOnlyOnce() {
        when(userService.registerUser(any())).thenReturn(responseDTO);

        authController.register(dto);

        verify(userService, times(1)).registerUser(any());
        verifyNoMoreInteractions(userService);
    }
}
