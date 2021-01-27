package pl.chatme.web.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import pl.chatme.dto.UserDTO;
import pl.chatme.service.SendMailService;
import pl.chatme.service.UserService;
import pl.chatme.web.rest.vm.ChangePasswordVM;
import pl.chatme.web.rest.vm.ModifyUserRequestVM;
import pl.chatme.web.rest.vm.UserVM;

import javax.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final UserService userService;
    private final SendMailService sendMailService;

    public AccountController(UserService userService,
                             SendMailService sendMailService) {
        this.userService = userService;
        this.sendMailService = sendMailService;
    }

    /**
     * {@code GET /accounts} : get information of logged user
     *
     * @param principal Authenticated user
     * @return DTO representation of User entity. If user not found return 404 else 200.
     */
    @GetMapping
    public ResponseEntity<UserDTO> getCurrentLoggedUser(Principal principal) {
        return ResponseEntity.ok(userService.getUser(principal.getName()));
    }

    /**
     * {@code POST /accounts/register} : register new user
     *
     * @param userVM               Class which is representation of required fields to provide successfully registration.
     * @param uriComponentsBuilder uri builder needed to build location header
     * @return 200 and DTO representation of User entity if success, 409 if login or email are already in use, 400 if some of required
     * fields not inserted.
     */
    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody UserVM userVM, UriComponentsBuilder uriComponentsBuilder) {
        var user = userService.createUser(userVM, userVM.getPassword());
        var uri = uriComponentsBuilder.path("/users/{id}").buildAndExpand(user.getId());
        sendMailService.sendActivationEmail(user);
        return ResponseEntity.created(uri.toUri()).body(user);
    }

    /**
     * {@code  PATCH /accounts/activate} : activate the registered user.
     *
     * @param activationKey the activation key generated during registration.
     * @return When activation process is successful 204 else 404.
     */
    @PatchMapping("/activate")
    public ResponseEntity<Void> activateAccount(@RequestParam("data") String activationKey) {
        userService.activateUser(activationKey);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@code PATCH /accounts/renew-friend-code} : generate new friends code.
     *
     * @param principal Authenticated user
     * @return 404 if user not exists else 200 and DTO representation of User entity.
     */
    @PatchMapping("/renew-friend-code")
    public ResponseEntity<UserDTO> renewFriendRequestCode(Principal principal) {
        return ResponseEntity.ok(userService.renewFriendRequestCode(principal.getName()));
    }

    /**
     * {@code PATCH /accounts/change-password} : change user password
     *
     * @param changePasswordVM Class which is representation of required fields to provide change password.
     * @param principal        Authenticated user
     * @return 400 if current password is incorrect else 200 and DTO representation of User entity.
     */
    @PatchMapping("/change-password")
    public ResponseEntity<UserDTO> changeUserPassword(@Valid @RequestBody ChangePasswordVM changePasswordVM, Principal principal) {
        return ResponseEntity.ok(userService.changeUserPassword(principal.getName(),
                changePasswordVM.getCurrentPassword(),
                changePasswordVM.getNewPassword()));
    }

    /**
     * {@code PATCH /accounts/{id}} : modify user information by id.
     *
     * @param userId              edited user's id
     * @param modifyUserRequestVM VM object with supported fields to modify
     * @param principal           Authenticated user
     * @return 200 is success and body with UserDTO, 400 if field validation gone wrong or not the owner try to modify account information
     * @see ModifyUserRequestVM you can see what fields you can change
     */
    @PatchMapping("/{id}")
    public ResponseEntity<UserDTO> changeUserInformation(@PathVariable("id") Long userId,
                                                         @Valid @RequestBody ModifyUserRequestVM modifyUserRequestVM,
                                                         Principal principal) {
        var userDTO = new UserDTO();
        userDTO.setFirstName(modifyUserRequestVM.getFirstName());
        userDTO.setLastName(modifyUserRequestVM.getLastName());
        return ResponseEntity.ok(userService.modifyUserInformation(userId, userDTO, principal.getName()));
    }

}
