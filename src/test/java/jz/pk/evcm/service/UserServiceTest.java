package jz.pk.evcm.service;

import jakarta.persistence.EntityNotFoundException;
import jz.pk.evcm.entity.User;
import jz.pk.evcm.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getUserByEmail_whenUserExists_returnsUser() {
        // arrange
        String email = "john@snow.got";
        User user = new User();
        user.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // act
        User result = userService.getUserByEmail(email);

        // assert
        assertThat(result).isSameAs(user);
    }

    @Test
    void getUserByEmail_whenUserMissing_throwsException() {
        when(userRepository.findByEmail("x@y.z")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByEmail("x@y.z"))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
