package pl.chatme.web.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import pl.chatme.service.UserService;
import pl.chatme.service.dto.UserDTO;
import pl.chatme.service.mapper.UserMapper;
import pl.chatme.web.rest.vm.UserVM;

import javax.validation.Valid;

@RestController
public class RegisterController {

    private final UserService userService;
    private final UserMapper userMapper;

    public RegisterController(UserService userService,
                              UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody UserVM userVM,
                                                UriComponentsBuilder uriComponentsBuilder) {

        var user = userService.registerUser(userVM, userVM.getPassword());

        var uri = uriComponentsBuilder.path("/users/{id}").buildAndExpand(user.getId());

        return ResponseEntity.created(uri.toUri()).body(userMapper.mapToUserDTO(user));

    }

}
