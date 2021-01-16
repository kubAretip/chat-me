package pl.chatme.service.impl;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import pl.chatme.domain.ChatMessage;
import pl.chatme.domain.Conversation;
import pl.chatme.domain.MessageStatus;
import pl.chatme.repository.ChatMessageRepository;
import pl.chatme.repository.UserRepository;
import pl.chatme.service.ChatMessageService;
import pl.chatme.service.dto.ChatMessageDTO;
import pl.chatme.service.mapper.ChatMessageMapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ChatMessageMapper chatMessageMapper;

    public ChatMessageServiceImpl(ChatMessageRepository chatMessageRepository,
                                  UserRepository userRepository,
                                  ChatMessageMapper chatMessageMapper) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
        this.chatMessageMapper = chatMessageMapper;
    }

    @Override
    public ChatMessage saveChatMessage(Conversation conversation, String content, String time) {

        var newMessage = new ChatMessage();

        // TODO : catch some exception in parse error case
        try {
            newMessage.setTime(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
                    .parse(time)
                    .toInstant().
                            atOffset(ZoneOffset.systemDefault().getRules().getOffset(Instant.now()))
            );
        } catch (ParseException e) {
            e.printStackTrace();
        }
        newMessage.setContent(content);
        newMessage.setConversation(conversation);
        newMessage.setRecipient(conversation.getRecipient());
        newMessage.setSender(conversation.getSender());
        newMessage.setMessageStatus(MessageStatus.RECEIVED);
        return chatMessageRepository.save(newMessage);
    }


    @Override
    public List<ChatMessageDTO> getMessagesWithSizeAndBeforeTime(long firstUserId, long secondUserId, String beforeTime, int to) {

        var firstUserOptional = userRepository.findById(firstUserId);
        var secondUserOptional = userRepository.findById(secondUserId);

        if (firstUserOptional.isPresent() && secondUserOptional.isPresent()) {

            var chatMessageList = chatMessageRepository
                    .findBySenderAndRecipientOrRecipientAndSenderAndTimeBefore(firstUserId, secondUserId,
                            LocalDateTime.parse(beforeTime, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")),
                            PageRequest.of(0, to));
            chatMessageList.sort((o1, o2) -> o2.getTime().compareTo(o1.getTime()));
            return chatMessageList
                    .stream()
                    .map(chatMessageMapper::mapToChatMessageDTO)
                    .collect(Collectors.toList());
        }

        // TODO : throw exception
        return Collections.emptyList();
    }

    @Override
    public List<ChatMessageDTO> getMessagesWithSize(long firstUserId, long secondUserId, int size) {

        var firstUserOptional = userRepository.findById(firstUserId);
        var secondUserOptional = userRepository.findById(secondUserId);

        if (firstUserOptional.isPresent() && secondUserOptional.isPresent()) {

            var chatMessageList = chatMessageRepository
                    .findTopByOrderByTimeSenderAndRecipientOrRecipientAndSender(firstUserId, secondUserId, PageRequest.of(0, size));

            chatMessageList.sort((Comparator.comparing(ChatMessage::getTime)));
            return chatMessageList
                    .stream()
                    .map(chatMessageMapper::mapToChatMessageDTO)
                    .collect(Collectors.toList());
        }

        // TODO : throw exception
        return Collections.emptyList();
    }

}
