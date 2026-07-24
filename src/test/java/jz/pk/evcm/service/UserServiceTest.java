package jz.pk.evcm.service;

import jz.pk.evcm.entity.User;
import jz.pk.evcm.repository.UserRepository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    public void UserService_getUserByEmail() {

        String searchedUserEmail = "john@snow.got";
        User mockUser = new User();
        mockUser.setEmail(searchedUserEmail);

        Mockito.when(userRepository.findByEmail(searchedUserEmail)).thenReturn(Optional.of(mockUser));

        User result = userService.getUserByEmail(searchedUserEmail);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isExactlyInstanceOf(User.class);
        Assertions.assertThat(result.getEmail()).isEqualTo(searchedUserEmail);
        Mockito.verify(userRepository, Mockito.times(1)).findByEmail(searchedUserEmail);
    }
}
