package pl.chatme.web.rest.vm;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Size;

import static pl.chatme.web.rest.vm.UserVM.PASSWORD_MAX_LENGTH;
import static pl.chatme.web.rest.vm.UserVM.PASSWORD_MIN_LENGTH;


@Getter
@Setter
@NoArgsConstructor
public class ChangePasswordVM {

    private String currentPassword;

    @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH)
    private String newPassword;
}
