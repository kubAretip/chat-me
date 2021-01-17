package pl.chatme.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.chatme.domain.FriendRequest;
import pl.chatme.dto.FriendRequestDTO;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface FriendRequestMapper {

    @Mapping(target = "sentTime", expression = "java(convertTime(entity.getSentTime()))")
    FriendRequestDTO mapToFriendRequestDTO(FriendRequest entity);

    default String convertTime(OffsetDateTime time) {
        return time.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    }

}
