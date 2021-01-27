package pl.chatme.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pl.chatme.domain.ConversationMessage;
import pl.chatme.domain.enumerated.MessageStatus;
import pl.chatme.dto.ConversationMessageDTO;
import pl.chatme.dto.mapper.ConversationMessageMapper;
import pl.chatme.repository.ConversationMessageRepository;
import pl.chatme.repository.ConversationRepository;
import pl.chatme.repository.UserRepository;
import pl.chatme.service.ConversationMessageService;
import pl.chatme.util.DateUtils;
import pl.chatme.util.ExceptionUtils;

import javax.transaction.Transactional;
import java.time.format.DateTimeParseException;
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
    private final ExceptionUtils exceptionUtils;

    public ConversationMessageServiceImpl(ConversationMessageRepository conversationMessageRepository,
                                          UserRepository userRepository,
                                          ConversationMessageMapper conversationMessageMapper,
                                          ConversationRepository conversationRepository,
                                          ExceptionUtils exceptionUtils) {
        this.conversationMessageRepository = conversationMessageRepository;
        this.userRepository = userRepository;
        this.conversationMessageMapper = conversationMessageMapper;
        this.conversationRepository = conversationRepository;
        this.exceptionUtils = exceptionUtils;
    }

    @Transactional
    @Override
    public ConversationMessageDTO saveConversationMessage(long conversationId, String content, String time) {

        return conversationRepository.findById(conversationId)
                .map(conversation -> {
                    var newMessage = new ConversationMessage();
                    try {
                        newMessage.setTime(DateUtils.convertStringDateToOffsetTime(time));
                    } catch (DateTimeParseException ex) {
                        throw exceptionUtils.unsupportedDateFormatException();
                    }
                    newMessage.setContent(content);
                    newMessage.setConversation(conversation);
                    newMessage.setRecipient(conversation.getRecipient());
                    newMessage.setSender(conversation.getSender());
                    newMessage.setMessageStatus(MessageStatus.RECEIVED);
                    conversationMessageRepository.save(newMessage);
                    return conversationMessageMapper.mapToConversationMessageDTO(newMessage);
                })
                .orElseThrow(exceptionUtils::conversationNotFoundException);
    }

    @Override
    public List<ConversationMessageDTO> getMessagesWithSizeAndBeforeTime(String senderUsername, long conversationId, String beforeTime, int size) {

        var user = userRepository.findOneByLoginIgnoreCase(senderUsername)
                .orElseThrow(() -> exceptionUtils.userNotFoundException(senderUsername));

        return conversationRepository.findById(conversationId)
                .filter(conversation -> conversation.getSender().equals(user))
                .map(senderConversation -> {
                    try {
                        var chatMessageList = conversationMessageRepository
                                .findUserConversationMessagesBeforeTime(senderConversation.getId(),
                                        senderConversation.getConversationWith().getId(),
                                        DateUtils.convertStringDateToOffsetTime(beforeTime),
                                        PageRequest.of(0, size));
                        chatMessageList.sort((o1, o2) -> o2.getTime().compareTo(o1.getTime()));
                        return chatMessageList.stream()
                                .map(conversationMessageMapper::mapToConversationMessageDTO)
                                .collect(Collectors.toList());
                    } catch (DateTimeParseException ex) {
                        throw exceptionUtils.unsupportedDateFormatException();
                    }
                })
                .orElseThrow(exceptionUtils::conversationNotFoundException);
    }

    @Override
    public List<ConversationMessageDTO> getMessagesWithSize(String senderUsername, long conversationId, int size) {

        var user = userRepository.findOneByLoginIgnoreCase(senderUsername)
                .orElseThrow(() -> exceptionUtils.userNotFoundException(senderUsername));

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
                .orElseThrow(exceptionUtils::conversationNotFoundException);
    }

    @Override
    public void setAllRecipientMessagesStatusAsDelivered(long conversationWithId, String recipientUsername) {

        var recipient = userRepository.findOneByLoginIgnoreCase(recipientUsername)
                .orElseThrow(() -> exceptionUtils.userNotFoundException(recipientUsername));

        var conversationWith = conversationRepository.findById(conversationWithId)
                .orElseThrow(exceptionUtils::conversationNotFoundException);

        var markedMessages = conversationMessageRepository
                .findByRecipientAndMessageStatusAndConversation(recipient, MessageStatus.RECEIVED, conversationWith)
                .stream()
                .peek(message -> message.setMessageStatus(MessageStatus.DELIVERED))
                .collect(Collectors.toList());
        conversationMessageRepository.saveAll(markedMessages);
    }
}
