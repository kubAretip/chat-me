package pl.chatme.service.impl;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pl.chatme.domain.ConversationMessage;
import pl.chatme.domain.Conversation;
import pl.chatme.domain.enumerated.MessageStatus;
import pl.chatme.repository.ConversationMessageRepository;
import pl.chatme.repository.ConversationRepository;
import pl.chatme.repository.UserRepository;
import pl.chatme.service.ConversationMessageService;
import pl.chatme.dto.ConversationMessageDTO;
import pl.chatme.dto.mapper.ConversationMessageMapper;

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
class ConversationMessageServiceImpl implements ConversationMessageService {

    private final ConversationMessageRepository conversationMessageRepository;
    private final UserRepository userRepository;
    private final ConversationMessageMapper conversationMessageMapper;
    private final ConversationRepository conversationRepository;

    public ConversationMessageServiceImpl(ConversationMessageRepository conversationMessageRepository,
                                          UserRepository userRepository,
                                          ConversationMessageMapper conversationMessageMapper,
                                          ConversationRepository conversationRepository) {
        this.conversationMessageRepository = conversationMessageRepository;
        this.userRepository = userRepository;
        this.conversationMessageMapper = conversationMessageMapper;
        this.conversationRepository = conversationRepository;
    }

    @Override
    public ConversationMessage saveConversationMessage(Conversation conversation, String content, String time) {

        var newMessage = new ConversationMessage();

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
        return conversationMessageRepository.save(newMessage);
    }


    @Override
    public List<ConversationMessageDTO> getMessagesWithSizeAndBeforeTime(String senderUsername, long recipientId, String beforeTime, int size) {

        var senderUserOptional = userRepository.findOneByLoginIgnoreCase(senderUsername);
        var recipientUserOptional = userRepository.findById(recipientId);

        if (senderUserOptional.isPresent() && recipientUserOptional.isPresent()) {

            var chatMessageList = conversationMessageRepository
                    .findUserConversationMessagesBeforeTime(senderUserOptional.get().getId(), recipientUserOptional.get().getId(),
                            LocalDateTime.parse(beforeTime, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")),
                            PageRequest.of(0, size));
            chatMessageList.sort((o1, o2) -> o2.getTime().compareTo(o1.getTime()));
            return chatMessageList
                    .stream()
                    .map(conversationMessageMapper::mapToConversationMessageDTO)
                    .collect(Collectors.toList());
        }

        // TODO : throw exception
        return Collections.emptyList();
    }

    @Override
    public List<ConversationMessageDTO> getMessagesWithSize(String senderUsername, long recipientId, int size) {

        var senderUserOptional = userRepository.findOneByLoginIgnoreCase(senderUsername);
        var recipientUserOptional = userRepository.findById(recipientId);

        if (senderUserOptional.isPresent() && recipientUserOptional.isPresent()) {

            var sender = senderUserOptional.get();
            var recipient = recipientUserOptional.get();
            var senderConversationOptional = conversationRepository.findBySenderAndRecipient(sender, recipient);
            var recipientConversationOptional = conversationRepository.findBySenderAndRecipient(recipient, sender);

            if (senderConversationOptional.isPresent() && recipientConversationOptional.isPresent()) {

                var sortedByTimeDescWithSize = PageRequest.of(0, size, Sort.by("time").descending());
                var conversationMessages = conversationMessageRepository.findByConversationOrConversation(senderConversationOptional.get(),
                        recipientConversationOptional.get(), sortedByTimeDescWithSize);

                conversationMessages.sort((Comparator.comparing(ConversationMessage::getTime)));

                return conversationMessages
                        .stream()
                        .map(conversationMessageMapper::mapToConversationMessageDTO)
                        .collect(Collectors.toList());
            }
        }

        // TODO : throw exception
        return Collections.emptyList();
    }

}
