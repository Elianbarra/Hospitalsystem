package com.hospital.msuser.service;

import com.hospital.msuser.client.auth.AuthRestClient;
import com.hospital.msuser.dto.request.CreateUserRequestDTO;
import com.hospital.msuser.dto.request.UpdateUserRequestDTO;
import com.hospital.msuser.dto.response.UserResponseDTO;
import com.hospital.msuser.entity.User;
import com.hospital.msuser.entity.enums.DocumentType;
import com.hospital.msuser.entity.enums.UserRole;
import com.hospital.msuser.exception.UserAlreadyExistsException;
import com.hospital.msuser.exception.UserNotFoundException;
import com.hospital.msuser.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthRestClient authRestClient;

    @InjectMocks
    private UserService userService;

    private User sampleUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .firstName("Juan")
                .lastName("Perez")
                .email("juan@hospital.com")
                .phone("999888777")
                .documentType(DocumentType.DNI)
                .documentNumber("12345678")
                .role(UserRole.PATIENT)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private CreateUserRequestDTO sampleCreateDTO() {
        return CreateUserRequestDTO.builder()
                .firstName("Juan")
                .lastName("Perez")
                .email("juan@hospital.com")
                .password("secreto123")
                .phone("999888777")
                .documentType(DocumentType.DNI)
                .documentNumber("12345678")
                .role(UserRole.PATIENT)
                .build();
    }

    @Test
    void registerUser_happyPath_returnsResponse() {
        User saved = sampleUser();
        when(userRepository.existsByEmail("juan@hospital.com")).thenReturn(false);
        when(userRepository.existsByDocumentNumber("12345678")).thenReturn(false);
        when(userRepository.save(any())).thenReturn(saved);
        doNothing().when(authRestClient).registerUserCredentials(any());

        UserResponseDTO result = userService.registerUser(sampleCreateDTO());

        assertThat(result.getEmail()).isEqualTo("juan@hospital.com");
        assertThat(result.getRole()).isEqualTo(UserRole.PATIENT);
        assertThat(result.getIsActive()).isTrue();
        verify(authRestClient).registerUserCredentials(any());
        verify(userRepository).save(any());
    }

    @Test
    void registerUser_duplicateEmail_throwsUserAlreadyExistsException() {
        when(userRepository.existsByEmail("juan@hospital.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(sampleCreateDTO()))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("juan@hospital.com");

        verify(userRepository, never()).save(any());
        verifyNoInteractions(authRestClient);
    }

    @Test
    void registerUser_duplicateDocument_throwsUserAlreadyExistsException() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByDocumentNumber("12345678")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(sampleCreateDTO()))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("12345678");

        verify(userRepository, never()).save(any());
        verifyNoInteractions(authRestClient);
    }

    @Test
    void getUserById_found_returnsMappedResponse() {
        User user = sampleUser();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UserResponseDTO result = userService.getUserById(user.getId());

        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getFirstName()).isEqualTo("Juan");
        assertThat(result.getLastName()).isEqualTo("Perez");
    }

    @Test
    void getUserById_notFound_throwsUserNotFoundException() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(id))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void getAllActiveUsers_returnsMappedList() {
        when(userRepository.findByIsActive(true)).thenReturn(List.of(sampleUser(), sampleUser()));

        List<UserResponseDTO> result = userService.getAllActiveUsers();

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(u -> Boolean.TRUE.equals(u.getIsActive()));
    }

    @Test
    void getAllActiveUsers_emptyRepo_returnsEmptyList() {
        when(userRepository.findByIsActive(true)).thenReturn(List.of());

        List<UserResponseDTO> result = userService.getAllActiveUsers();

        assertThat(result).isEmpty();
    }

    @Test
    void updateUser_updatesOnlyProvidedFields() {
        User user = sampleUser();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        UpdateUserRequestDTO dto = UpdateUserRequestDTO.builder()
                .firstName("Carlos")
                .build();

        userService.updateUser(user.getId(), dto);

        assertThat(user.getFirstName()).isEqualTo("Carlos");
        assertThat(user.getLastName()).isEqualTo("Perez");
        assertThat(user.getPhone()).isEqualTo("999888777");
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_notFound_throwsUserNotFoundException() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(id, UpdateUserRequestDTO.builder().firstName("X").build()))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void deactivateUser_setsIsActiveFalse() {
        User user = sampleUser();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        userService.deactivateUser(user.getId());

        assertThat(user.getIsActive()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    void deactivateUser_notFound_throwsUserNotFoundException() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deactivateUser(id))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(id.toString());
    }
}
