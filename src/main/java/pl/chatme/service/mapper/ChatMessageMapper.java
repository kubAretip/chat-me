package pl.chatme.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.chatme.domain.ChatMessage;
import pl.chatme.service.dto.ChatMessageDTO;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {

    List<ChatMessageDTO> mapToChatMessageListDTO(List<ChatMessage> entityList);

    @Mapping(target = "time", expression = "java(convertTime(chatMessage.getTime()))")
    ChatMessageDTO mapToChatMessageDTO(ChatMessage chatMessage);

    default String convertTime(OffsetDateTime time) {
        return time.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    }

}
