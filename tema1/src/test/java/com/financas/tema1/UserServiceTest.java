package com.financas.tema1;

import com.financas.tema1.DTO.UserRegisterDTO;
import com.financas.tema1.DTO.UserResponseDTO;
import com.financas.tema1.domain.User;
import com.financas.tema1.repository.UserRepository;
import com.financas.tema1.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Deve registrar usuário com senha criptografada")
    void shouldRegisterUserWithEncodedPassword() {
        UserRegisterDTO dto = new UserRegisterDTO("user@email.com", "senha123");

        when(passwordEncoder.encode("senha123")).thenReturn("$2a$encoded");

        User savedUser = new User();
        savedUser.setEmail("user@email.com");
        savedUser.setPassword("$2a$encoded");
        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(savedUser, 1L);
        } catch (Exception ignored) {}

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponseDTO response = userService.registerUser(dto);

        assertThat(response.email()).isEqualTo("user@email.com");
        verify(passwordEncoder).encode("senha123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Deve salvar usuário com a senha encriptada (não em texto puro)")
    void shouldNotSaveRawPassword() {
        UserRegisterDTO dto = new UserRegisterDTO("user@email.com", "senha123");
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$hashed");

        User saved = new User();
        saved.setEmail("user@email.com");
        saved.setPassword("$2a$hashed");
        when(userRepository.save(any())).thenReturn(saved);

        userService.registerUser(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isNotEqualTo("senha123");
        assertThat(captor.getValue().getPassword()).isEqualTo("$2a$hashed");
    }


    @Test
    @DisplayName("Deve carregar UserDetails pelo email com sucesso")
    void shouldLoadUserByUsernameSuccessfully() {
        User user = new User();
        user.setEmail("user@email.com");
        user.setPassword("$2a$encoded");

        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(user));

        UserDetails details = userService.loadUserByUsername("user@email.com");

        assertThat(details.getUsername()).isEqualTo("user@email.com");
        assertThat(details.getPassword()).isEqualTo("$2a$encoded");
        assertThat(details.getAuthorities()).isNotEmpty();
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException quando usuário não existir")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findByEmail("naoexiste@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loadUserByUsername("naoexiste@email.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("não encontrado");
    }

    @Test
    @DisplayName("Deve retornar authority 'USER' para o usuário carregado")
    void shouldReturnUserAuthority() {
        User user = new User();
        user.setEmail("user@email.com");
        user.setPassword("pass");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        UserDetails details = userService.loadUserByUsername("user@email.com");

        assertThat(details.getAuthorities())
                .anyMatch(a -> a.getAuthority().equals("USER"));
    }
}
