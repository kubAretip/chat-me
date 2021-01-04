package pl.chatme.web.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import pl.chatme.service.SendMailService;
import pl.chatme.service.UserService;
import pl.chatme.service.dto.UserDTO;
import pl.chatme.service.mapper.UserMapper;
import pl.chatme.web.rest.vm.UserVM;

import javax.validation.Valid;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final SendMailService sendMailService;

    public AccountController(UserService userService,
                             UserMapper userMapper,
                             SendMailService sendMailService) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.sendMailService = sendMailService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody UserVM userVM,
                                                UriComponentsBuilder uriComponentsBuilder) {

        var user = userService.createUser(userVM, userVM.getPassword());
        var uri = uriComponentsBuilder.path("/users/{id}").buildAndExpand(user.getId());
        sendMailService.sendActivationEmail(user);
        return ResponseEntity.created(uri.toUri()).body(userMapper.mapToUserDTO(user));

    }

    @PatchMapping("/activate")
    public ResponseEntity<Void> activateAccount(@RequestParam("data") String activationKey) {

        if (userService.activateUser(activationKey))
            return ResponseEntity.noContent().build();

        return ResponseEntity.notFound().build();
    }

}