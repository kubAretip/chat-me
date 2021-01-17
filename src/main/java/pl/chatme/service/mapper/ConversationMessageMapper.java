package pl.chatme.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.chatme.domain.ConversationMessage;
import pl.chatme.service.dto.ConversationMessageDTO;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ConversationMessageMapper {

    List<ConversationMessageDTO> mapToChatMessageListDTO(List<ConversationMessage> entityList);

    @Mapping(target = "time", expression = "java(convertTime(conversationMessage.getTime()))")
    ConversationMessageDTO mapToConversationMessageDTO(ConversationMessage conversationMessage);

    default String convertTime(OffsetDateTime time) {
        return time.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    }

}
