package pl.chatme.web.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import pl.chatme.dto.UserDTO;
import pl.chatme.dto.mapper.UserMapper;
import pl.chatme.service.SendMailService;
import pl.chatme.service.UserService;
import pl.chatme.service.exception.AlreadyExistsException;
import pl.chatme.service.exception.InvalidDataException;
import pl.chatme.service.exception.NotFoundException;
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
        try {
            return ResponseEntity.ok(userMapper.mapToUserDTO(userService.getUser(principal.getName())));
        } catch (NotFoundException ex) {
            throw Problem.builder()
                    .withStatus(Status.NOT_FOUND)
                    .withTitle(ex.getTitle())
                    .withDetail(ex.getLocalizedMessage())
                    .build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody UserVM userVM,
                                                UriComponentsBuilder uriComponentsBuilder) {
        try {
            var user = userService.createUser(userVM, userVM.getPassword());
            var uri = uriComponentsBuilder.path("/users/{id}").buildAndExpand(user.getId());
            sendMailService.sendActivationEmail(user);
            return ResponseEntity.created(uri.toUri()).body(userMapper.mapToUserDTO(user));
        } catch (AlreadyExistsException ex) {
            throw Problem.builder()
                    .withStatus(Status.CONFLICT)
                    .withTitle(ex.getTitle())
                    .withDetail(ex.getLocalizedMessage())
                    .build();
        }
    }


    // TODO : change to patch
    /**
     * {@code temporary POST mapping /activate} : activate the registered user.
     *
     * @param activationKey the activation key generated during registration.
     * @return When activation process is successful 204 else 404.
     */
    @PostMapping("/activate")
    public ResponseEntity<Void> activateAccount(@RequestParam("data") String activationKey) {

        if (userService.activateUser(activationKey))
            return ResponseEntity.noContent().build();

        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/renew-friend-code")
    public ResponseEntity<UserDTO> renewFriendRequestCode(Principal principal) {
        try {
            return ResponseEntity.ok(userMapper.mapToUserDTO(userService.renewFriendRequestCode(principal.getName())));
        } catch (NotFoundException exception) {
            throw Problem.builder()
                    .withStatus(Status.NOT_FOUND)
                    .withTitle(exception.getTitle())
                    .withDetail(exception.getLocalizedMessage())
                    .build();
        }
    }

    @PatchMapping("/change-password")
    public ResponseEntity<UserDTO> changeUserPassword(@Valid @RequestBody ChangePasswordVM changePasswordVM, Principal principal) {
        try {
            return ResponseEntity.ok(userMapper.mapToUserDTO(userService.changeUserPassword(principal.getName(),
                    changePasswordVM.getCurrentPassword(),
                    changePasswordVM.getNewPassword())));
        } catch (InvalidDataException ex) {
            throw Problem.builder()
                    .withStatus(Status.BAD_REQUEST)
                    .withTitle(ex.getTitle())
                    .withDetail(ex.getLocalizedMessage())
                    .build();
        } catch (NotFoundException exception) {
            throw Problem.builder()
                    .withStatus(Status.NOT_FOUND)
                    .withTitle(exception.getTitle())
                    .withDetail(exception.getLocalizedMessage())
                    .build();
        }
    }

}
