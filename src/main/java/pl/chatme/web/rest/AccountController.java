package pl.chatme.web.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import pl.chatme.dto.UserDTO;
import pl.chatme.dto.mapper.UserMapper;
import pl.chatme.service.SendMailService;
import pl.chatme.service.UserService;
import pl.chatme.web.rest.vm.ChangePasswordVM;
import pl.chatme.web.rest.vm.UserVM;

import javax.validation.Valid;
import java.security.Principal;

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

    @GetMapping
    public ResponseEntity<UserDTO> getCurrentLoggedUser(Principal principal) {
        return ResponseEntity.ok(userMapper.mapToUserDTO(userService.getUser(principal.getName())));
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody UserVM userVM, UriComponentsBuilder uriComponentsBuilder) {
        var user = userService.createUser(userVM, userVM.getPassword());
        var uri = uriComponentsBuilder.path("/users/{id}").buildAndExpand(user.getId());
        sendMailService.sendActivationEmail(user);
        return ResponseEntity.created(uri.toUri()).body(userMapper.mapToUserDTO(user));
    }


    /**
     * {@code  PATCH /activate} : activate the registered user.
     *
     * @param activationKey the activation key generated during registration.
     * @return When activation process is successful 204 else 404.
     */
    @PatchMapping("/activate")
    public ResponseEntity<Void> activateAccount(@RequestParam("data") String activationKey) {
        userService.activateUser(activationKey);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/renew-friend-code")
    public ResponseEntity<UserDTO> renewFriendRequestCode(Principal principal) {
        return ResponseEntity.ok(userMapper.mapToUserDTO(userService.renewFriendRequestCode(principal.getName())));
    }

    @PatchMapping("/change-password")
    public ResponseEntity<UserDTO> changeUserPassword(@Valid @RequestBody ChangePasswordVM changePasswordVM, Principal principal) {
        return ResponseEntity.ok(userMapper.mapToUserDTO(userService.changeUserPassword(principal.getName(),
                changePasswordVM.getCurrentPassword(),
                changePasswordVM.getNewPassword())));
    }

}
