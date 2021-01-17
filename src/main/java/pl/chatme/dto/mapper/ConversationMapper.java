package pl.chatme.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.chatme.domain.Conversation;
import pl.chatme.dto.ConversationDTO;

@Mapper(componentModel = "spring")
public interface ConversationMapper {

    @Mapping(target = "conversationWithId", expression = "java(convertConversationWithObjectToId(conversation.getConversationWith()))")
    ConversationDTO mapToConversationDTO(Conversation conversation);

    default Long convertConversationWithObjectToId(Conversation conversationWith) {
        return conversationWith.getId();
    }

}
