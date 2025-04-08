package br.com.marques.kontaktapi.infrastructure.service;

import br.com.marques.kontaktapi.domain.dto.user.RegisterRequest;
import br.com.marques.kontaktapi.domain.entity.User;
import br.com.marques.kontaktapi.infrastructure.config.security.IAuthenticationFacade;
import br.com.marques.kontaktapi.infrastructure.exception.AlreadyExistsException;
import br.com.marques.kontaktapi.infrastructure.exception.NotFoundException;
import br.com.marques.kontaktapi.infrastructure.mapper.UserMapper;
import br.com.marques.kontaktapi.infrastructure.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserCrudService Tests")
class UserCrudServiceTest {
    @Mock
    private IAuthenticationFacade authenticationFacade;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private UserCrudService userCrudService;

    private RegisterRequest registerRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("test@example.com", "password", "Test User", "Silva");
        user = new User(1L, "test@example.com", "password", "Test User", "Silva", null);
    }

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("shouldCreateUser_WhenEmailDoesNotExist")
        void shouldCreateUser_WhenEmailDoesNotExist() {
            when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
            when(userMapper.toEntity(registerRequest)).thenReturn(user);
            when(userRepository.save(user)).thenReturn(user);

            userCrudService.create(registerRequest);

            verify(userRepository, times(1)).existsByEmail(registerRequest.email());
            verify(userMapper, times(1)).toEntity(registerRequest);
            verify(userRepository, times(1)).save(user);
            assertNotNull(user.getRole());
        }

        @Test
        @DisplayName("shouldThrowAlreadyExistsException_WhenEmailAlreadyExists")
        void shouldThrowAlreadyExistsException_WhenEmailAlreadyExists() {
            when(userRepository.existsByEmail(registerRequest.email())).thenReturn(true);

            assertThrows(AlreadyExistsException.class, () -> userCrudService.create(registerRequest));

            verify(userRepository, times(1)).existsByEmail(registerRequest.email());
            verify(userMapper, never()).toEntity(any());
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("existsByEmail")
    class ExistsByEmailTests {

        @Test
        @DisplayName("shouldThrowAlreadyExistsException_WhenEmailExists")
        void shouldThrowAlreadyExistsException_WhenEmailExists() {
            when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
            assertThrows(AlreadyExistsException.class, () -> userCrudService.existsByEmail("test@example.com"));
        }

        @Test
        @DisplayName("shouldNotThrowException_WhenEmailDoesNotExist")
        void shouldNotThrowException_WhenEmailDoesNotExist() {
            when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
            assertDoesNotThrow(() -> userCrudService.existsByEmail("test@example.com"));
        }
    }

    @Nested
    @DisplayName("findByEmail")
    class FindByEmailTests {

        @Test
        @DisplayName("shouldReturnUser_WhenUserExists")
        void shouldReturnUser_WhenUserExists() {
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            User foundUser = userCrudService.findByEmail("test@example.com");
            assertEquals(user, foundUser);
        }

        @Test
        @DisplayName("shouldThrowNotFoundException_WhenUserDoesNotExist")
        void shouldThrowNotFoundException_WhenUserDoesNotExist() {
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> userCrudService.findByEmail("test@example.com"));
        }
    }

    @Nested
    @DisplayName("list")
    class ListTests {

        @Test
        @DisplayName("shouldReturnAllUsers_WhenCalled")
        void shouldReturnAllUsers_WhenCalled() {
            List<User> users = List.of(user);
            when(userRepository.findAll()).thenReturn(users);
            List<User> result = userCrudService.list();
            assertEquals(users, result);
        }
    }

    @Nested
    @DisplayName("findByIdOrElseThrow")
    class FindByIdOrElseThrowTests {

        @Test
        @DisplayName("shouldReturnUser_WhenUserExists")
        void shouldReturnUser_WhenUserExists() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            User foundUser = userCrudService.findByIdOrElseThrow(1L);
            assertEquals(user, foundUser);
        }

        @Test
        @DisplayName("shouldThrowNotFoundException_WhenUserDoesNotExist")
        void shouldThrowNotFoundException_WhenUserDoesNotExist() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> userCrudService.findByIdOrElseThrow(1L));
        }
    }

    @Nested
    @DisplayName("getLogged")
    class GetLoggedTests {

        @Test
        @DisplayName("shouldReturnLoggedUser_WhenUserExists")
        void shouldReturnLoggedUser_WhenUserExists() {
            Authentication authentication = mock(Authentication.class);
            when(authenticationFacade.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("1");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            User loggedUserResult = userCrudService.getLogged();
            assertEquals(user, loggedUserResult);
        }

        @Test
        @DisplayName("shouldThrowNotFoundException_WhenLoggedUserDoesNotExist")
        void shouldThrowNotFoundException_WhenLoggedUserDoesNotExist() {
            Authentication authentication = mock(Authentication.class);
            when(authenticationFacade.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("1");
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> userCrudService.getLogged());
        }
    }
}
