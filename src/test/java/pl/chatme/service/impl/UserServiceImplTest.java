package pl.chatme.service.impl;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.chatme.dto.UserDTO;
import pl.chatme.dto.mapper.UserMapper;
import pl.chatme.repository.AuthorityRepository;
import pl.chatme.repository.UserRepository;
import pl.chatme.service.UserService;
import pl.chatme.util.ExceptionUtils;
import pl.chatme.util.Translator;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

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
        given(passwordEncoder.encode(anyString())).willReturn("fake_encoded_password");
        when(userRepository.save(any())).then(returnsFirstArg());
    }





}
