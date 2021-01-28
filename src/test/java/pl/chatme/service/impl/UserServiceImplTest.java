package pl.chatme.service.impl;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.chatme.domain.User;
import pl.chatme.dto.UserDTO;
import pl.chatme.dto.mapper.UserMapper;
import pl.chatme.exception.AlreadyExistsException;
import pl.chatme.exception.InvalidDataException;
import pl.chatme.exception.NotFoundException;
import pl.chatme.repository.AuthorityRepository;
import pl.chatme.repository.UserRepository;
import pl.chatme.service.UserService;
import pl.chatme.util.ExceptionUtils;
import pl.chatme.util.Translator;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {UserServiceImpl.class})
public class UserServiceImplTest {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private AuthorityRepository authorityRepository;

    @MockBean
    private Translator translator;

    @MockBean
    private ExceptionUtils exceptionUtils;

    @Autowired
    public UserService userService;

    @BeforeAll
    public void setup() {
        given(translator.translate(anyString())).willReturn("fake_translated_message");
        given(translator.translate(anyString(), any())).willReturn("fake_translated_message");
    }


    @Test
    public void shouldCreateUser() {

        //given
        var mockUserDto = mock(UserDTO.class);
        var mockUser = mock(User.class);
        given(mockUser.getLogin()).willReturn("fake_login");
        given(userRepository.findOneByLoginIgnoreCase(anyString())).willReturn(Optional.empty());
        given(userRepository.findOneByEmailIgnoreCase(anyString())).willReturn(Optional.empty());
        given(userMapper.mapToUser(mockUserDto)).willReturn(mockUser);
        given(userMapper.mapToUserDTO(mockUser)).willReturn(mockUserDto);
        given(userRepository.save(any())).will(returnsFirstArg());

        //when
        var result = userService.createUser(mockUserDto, "fake_password");

        //then
        verify(userRepository, times(1)).save(mockUser);
        assertThat(result.getId(), is(notNullValue()));
    }

    @Test
    public void createUserShouldThrowExceptionWhenLoginIsInUseByActivatedUser() {

        //given
        var existingActivatedUserMock = mock(User.class);
        given(existingActivatedUserMock.getActivated()).willReturn(true);
        given(userRepository.findOneByLoginIgnoreCase(existingActivatedUserMock.getLogin())).willReturn(Optional.of(existingActivatedUserMock));

        //when + then
        verify(userRepository, times(0)).save(any());
        assertThrows(AlreadyExistsException.class, () -> userService.createUser(mock(UserDTO.class), "fake_password"));
    }

    @Test
    public void createUserShouldThrowExceptionWhenEmailIsInUseByActivatedUser() {

        //given
        var existingActivatedUserMock = mock(User.class);
        given(existingActivatedUserMock.getActivated()).willReturn(true);

        given(userRepository.findOneByLoginIgnoreCase(existingActivatedUserMock.getLogin())).willReturn(Optional.empty());
        given(userRepository.findOneByEmailIgnoreCase(existingActivatedUserMock.getLogin())).willReturn(Optional.of(existingActivatedUserMock));

        //when + then
        verify(userRepository, times(0)).save(any());
        assertThrows(AlreadyExistsException.class, () -> userService.createUser(mock(UserDTO.class), "fake_password"));

    }


    @Test
    public void shouldCreateUserWhenLoginIsInUseByNotActivatedUser() {

        //given
        var existingUnactivatedUserMock = mock(User.class);
        given(existingUnactivatedUserMock.getActivated()).willReturn(false);

        var newUserDTOMock = mock(UserDTO.class);
        var newUserMock = mock(User.class);
        given(newUserMock.getLogin()).willReturn("fake_login");
        given(userMapper.mapToUser(newUserDTOMock)).willReturn(newUserMock);
        given(userMapper.mapToUserDTO(newUserMock)).willReturn(newUserDTOMock);
        given(userRepository.save(any())).will(returnsFirstArg());
        given(userRepository.findOneByLoginIgnoreCase(newUserDTOMock.getLogin())).willReturn(Optional.of(existingUnactivatedUserMock));


        //when
        var result = userService.createUser(newUserDTOMock, "fake_password");

        //then
        verify(userRepository, times(1)).save(newUserMock);
        verify(userRepository, times(1)).delete(existingUnactivatedUserMock);
        assertThat(result.getId(), is(notNullValue()));
    }

    @Test
    public void shouldCreateUserWhenEmailIsInUseByNotActivatedUser() {

        //given
        var existingUnactivatedUserMock = mock(User.class);
        given(existingUnactivatedUserMock.getActivated()).willReturn(false);
        var newUserDTOMock = mock(UserDTO.class);
        var newUserMock = mock(User.class);
        given(newUserMock.getLogin()).willReturn("fake_login");
        given(userMapper.mapToUser(newUserDTOMock)).willReturn(newUserMock);
        given(userMapper.mapToUserDTO(newUserMock)).willReturn(newUserDTOMock);
        given(userRepository.save(any())).will(returnsFirstArg());
        given(userRepository.findOneByEmailIgnoreCase(newUserDTOMock.getLogin())).willReturn(Optional.of(existingUnactivatedUserMock));

        //when
        var result = userService.createUser(newUserDTOMock, "fake_password");

        //then
        verify(userRepository, times(1)).save(newUserMock);
        verify(userRepository, times(1)).delete(existingUnactivatedUserMock);
        assertThat(result.getId(), is(notNullValue()));

    }

    @Test
    public void shouldCapitalizedFirstLetterOfNameTheNewUser() {

        //given
        var newUserDTO = new UserDTO();
        newUserDTO.setLogin("fake_login");
        newUserDTO.setFirstName("john");
        newUserDTO.setLastName("mclean");
        var newUser = new User();
        newUser.setLogin("fake_login");
        newUser.setFirstName("john");
        newUser.setLastName("mclean");
        given(userMapper.mapToUser(newUserDTO)).willReturn(newUser);
        given(userMapper.mapToUserDTO(newUser)).willReturn(newUserDTO);
        given(userRepository.save(any())).will(returnsFirstArg());

        //when
        var result = userService.createUser(newUserDTO, "fake_password");

        //then
        assertThat(result.getFirstName(), equalTo("John"));
        assertThat(result.getLastName(), equalTo("Mclean"));
        verify(userRepository, times(1)).save(newUser);
    }


    @Test
    public void exceptionShouldBeThrownWhenActivateUserWithNullOrEmptyKey() {

        //given + when + then
        assertThrows(InvalidDataException.class, () -> userService.activateUser(""));
        assertThrows(InvalidDataException.class, () -> userService.activateUser(null));
        verify(userRepository, times(0)).save(any());
    }

    @Test
    public void exceptionShouldBeThrownWhenActivationKeyNotFound() {

        //given
        var key = "fake_key";
        given(userRepository.findOneByActivationKey(key)).willReturn(Optional.empty());
        //when + then
        assertThrows(NotFoundException.class, () -> userService.activateUser(key));
        verify(userRepository, times(0)).save(any());
    }


    @Test
    public void userShouldBeActivated() {

        //given
        var key = "fake_key";
        var user = new User();
        user.setActivated(false);
        user.setActivationKey(key);
        given(userRepository.findOneByActivationKey(key)).willReturn(Optional.of(user));
        given(userRepository.save(user)).willReturn(user);
        given(userRepository.findById(any())).willReturn(Optional.of(user));

        //when
        userService.activateUser(key);

        //then
        verify(userRepository, times(1)).save(user);
        assertThat(userRepository.findById(any()).get().getActivated(), equalTo(true));
        assertThat(userRepository.findById(any()).get().getActivationKey(), nullValue());
    }

    @Test
    public void shouldGenerateNewFriendsCode() {

        //given
        var oldFriendsCode = "old_code";
        var login = "fake_login";

        var userDTO = new UserDTO();
        userDTO.setLogin(login);

        var user = new User();
        user.setLogin(login);
        user.setFriendRequestCode(oldFriendsCode);

        given(userRepository.findOneByLoginIgnoreCase(login)).willReturn(Optional.of(user));
        given(userMapper.mapToUserDTO(user)).willReturn(userDTO);
        given(userRepository.findById(any())).willReturn(Optional.of(user));

        //when
        var result = userService.renewFriendRequestCode(login);

        //then
        verify(userRepository, times(1)).save(user);
        assertThat(userRepository.findById(any()).get().getLogin(), equalTo(login));
        assertThat(userRepository.findById(any()).get().getFriendRequestCode(), not(equalTo(oldFriendsCode)));
        assertThat(userRepository.findById(any()).get().getFriendRequestCode(), Matchers.startsWith(login));
    }

    @Test
    public void exceptionShouldBeThrownWhenTryGenerateNewFriendsCodeForNotExistingUser() {

        //given
        given(exceptionUtils.userNotFoundException(any())).willReturn(new NotFoundException("", ""));
        given(userRepository.findOneByLoginIgnoreCase(any())).willReturn(Optional.empty());

        //when + then
        assertThrows(NotFoundException.class, () -> userService.renewFriendRequestCode(any()));
    }

    @Test
    public void exceptionShouldBeThrownWhenTryChangePasswordForNotExistingUser() {

        //given
        given(exceptionUtils.userNotFoundException(any())).willReturn(new NotFoundException("", ""));
        given(userRepository.findOneByLoginIgnoreCase(any())).willReturn(Optional.empty());

        //when + then
        assertThrows(NotFoundException.class, () -> userService.changeUserPassword(any(), null, null));
    }

    @Test
    public void exceptionShouldBeThrownWhenChangePasswordWithIncorrectCurrentPassword() {

        //given
        var user = new User();
        user.setPassword("$2y$10$SPjlZDoEBDawKu.95ch/QOANuWXaDcov3CtPqz8lTMbuKyWa/W4P."); // 123abc in bcrypt
        given(userRepository.findOneByLoginIgnoreCase(any())).willReturn(Optional.of(user));

        //when + then
        assertThrows(InvalidDataException.class, () -> userService.changeUserPassword(any(), "wrong_current_password", null));
    }


    @Test
    public void passwordShouldBeChangeWithCorrectCurrentPassword() {

        //given
        var user = new User();
        given(userRepository.findOneByLoginIgnoreCase(any())).willReturn(Optional.of(user));
        given(passwordEncoder.matches("123abc", user.getPassword())).willReturn(true);
        given(passwordEncoder.encode(anyString())).willReturn("fake_encoded_password");
        given(userRepository.save(user)).willReturn(user);
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(userMapper.mapToUserDTO(user)).willReturn(mock(UserDTO.class));

        //when
        userService.changeUserPassword(any(), "123abc", "new_pass");

        //then
        verify(userRepository, times(1)).save(user);
        assertThat(userRepository.findById(any()).get().getPassword(), equalTo("fake_encoded_password"));
    }


}
