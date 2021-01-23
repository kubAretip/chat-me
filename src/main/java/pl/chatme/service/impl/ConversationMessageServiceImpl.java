package pl.chatme.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pl.chatme.domain.Conversation;
import pl.chatme.domain.ConversationMessage;
import pl.chatme.domain.enumerated.MessageStatus;
import pl.chatme.dto.ConversationMessageDTO;
import pl.chatme.dto.mapper.ConversationMessageMapper;
import pl.chatme.repository.ConversationMessageRepository;
import pl.chatme.repository.ConversationRepository;
import pl.chatme.repository.UserRepository;
import pl.chatme.service.ConversationMessageService;
import pl.chatme.exception.NotFoundException;
import pl.chatme.util.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
    public List<ConversationMessageDTO> getMessagesWithSizeAndBeforeTime(String senderUsername, long conversationId, String beforeTime, int size) {

        var user = userRepository.findOneByLoginIgnoreCase(senderUsername)
                .orElseThrow(() -> new NotFoundException("User not found.", "User with login " + senderUsername + " not exists."));

        return conversationRepository.findById(conversationId)
                .filter(conversation -> conversation.getSender().equals(user))
                .map(senderConversation -> {
                    var chatMessageList = conversationMessageRepository
                            .findUserConversationMessagesBeforeTime(senderConversation.getId(),
                                    senderConversation.getConversationWith().getId(),
                                    DateUtils.convertStringDateToOffsetTime(beforeTime),
                                    PageRequest.of(0, size));
                    chatMessageList.sort((o1, o2) -> o2.getTime().compareTo(o1.getTime()));
                    return chatMessageList.stream()
                            .map(conversationMessageMapper::mapToConversationMessageDTO)
                            .collect(Collectors.toList());
                })
                .orElseThrow(() -> new NotFoundException("Conversation not found", "We can not find this conversation."));
    }

    @Override
    public List<ConversationMessageDTO> getMessagesWithSize(String senderUsername, long conversationId, int size) {

        var user = userRepository.findOneByLoginIgnoreCase(senderUsername)
                .orElseThrow(() -> new NotFoundException("User not found.", "User with login " + senderUsername + " not exists."));

        return conversationRepository.findById(conversationId)
                .filter(conversation -> conversation.getSender().equals(user))
                .map(senderConversation -> {
                    var sortedByTimeDescWithSize = PageRequest.of(0, size, Sort.by("time").descending());
                    var conversationMessages = conversationMessageRepository.findByConversationOrConversation(senderConversation,
                            senderConversation.getConversationWith(), sortedByTimeDescWithSize);
                    conversationMessages.sort((Comparator.comparing(ConversationMessage::getTime)));
                    return conversationMessages
                            .stream()
                            .map(conversationMessageMapper::mapToConversationMessageDTO)
                            .collect(Collectors.toList());
                })
                .orElseThrow(() -> new NotFoundException("Conversation not found", "We can not find this conversation."));
    }

    @Override
    public void setAllRecipientMessagesStatusAsDelivered(long conversationWithId, String recipientUsername) {

        userRepository.findOneByLoginIgnoreCase(recipientUsername)
                .ifPresentOrElse(recipient -> {
                    conversationRepository.findById(conversationWithId)
                            .ifPresentOrElse(conversationWith -> {
                                var markedMessages = conversationMessageRepository
                                        .findByRecipientAndMessageStatusAndConversation(recipient, MessageStatus.RECEIVED, conversationWith)
                                        .stream()
                                        .peek(message -> message.setMessageStatus(MessageStatus.DELIVERED))
                                        .collect(Collectors.toList());
                                conversationMessageRepository.saveAll(markedMessages);
                            }, () -> {
                                throw new NotFoundException("Conversation not found.", "Conversation no exists.");
                            });
                }, () -> {
                    throw new NotFoundException("User not found.", "User with login " + recipientUsername + " not exists.");
                });
    }

}
