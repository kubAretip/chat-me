package pl.chatme.service.mapper;

import org.mapstruct.Mapper;
import pl.chatme.domain.User;
import pl.chatme.service.dto.UserDTO;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User mapToUser(UserDTO dto);

    UserDTO mapToUserDTO(User entity);
}
