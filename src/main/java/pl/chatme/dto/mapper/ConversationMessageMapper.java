package pl.chatme.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.chatme.domain.Conversation;
import pl.chatme.domain.ConversationMessage;
import pl.chatme.dto.ConversationMessageDTO;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ConversationMessageMapper {

    List<ConversationMessageDTO> mapToChatMessageListDTO(List<ConversationMessage> entityList);

    @Mapping(target = "time", expression = "java(convertTime(conversationMessage.getTime()))")
    @Mapping(target = "conversationId", expression = "java(getConversationId(conversationMessage.getConversation()))")
    ConversationMessageDTO mapToConversationMessageDTO(ConversationMessage conversationMessage);

    default String convertTime(OffsetDateTime time) {
        return time.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    }

    default Long getConversationId(Conversation conversation) {
        return conversation.getId();
    }
}
