package pl.chatme.service.impl;

import org.springframework.stereotype.Service;
import pl.chatme.domain.Conversation;
import pl.chatme.domain.ConversationMessage;
import pl.chatme.dto.ConversationDTO;
import pl.chatme.dto.UserDTO;
import pl.chatme.dto.mapper.ConversationMapper;
import pl.chatme.exception.AlreadyExistsException;
import pl.chatme.exception.InvalidDataException;
import pl.chatme.repository.ConversationMessageRepository;
import pl.chatme.repository.ConversationRepository;
import pl.chatme.repository.FriendRequestRepository;
import pl.chatme.repository.UserRepository;
import pl.chatme.service.ConversationService;
import pl.chatme.util.ExceptionUtils;
import pl.chatme.util.Translator;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationMessageRepository conversationMessageRepository;
    private final ConversationMapper conversationMapper;
    private final UserRepository userRepository;
    private final Translator translator;
    private final ExceptionUtils exceptionUtils;
    private final FriendRequestRepository friendRequestRepository;

    public ConversationServiceImpl(ConversationRepository conversationRepository,
                                   ConversationMessageRepository conversationMessageRepository,
                                   UserRepository userRepository,
                                   Translator translator,
                                   ExceptionUtils exceptionUtils,
                                   FriendRequestRepository friendRequestRepository,
                                   ConversationMapper conversationMapper) {
        this.conversationRepository = conversationRepository;
        this.conversationMessageRepository = conversationMessageRepository;
        this.userRepository = userRepository;
        this.translator = translator;
        this.exceptionUtils = exceptionUtils;
        this.friendRequestRepository = friendRequestRepository;
        this.conversationMapper = conversationMapper;
    }

    @Transactional
    @Override
    public ConversationDTO getConversation(String senderUsername, long recipientUserId) {

        var sender = userRepository.findOneByLoginIgnoreCase(senderUsername)
                .orElseThrow(() -> exceptionUtils.userNotFoundException(senderUsername));
        var recipient = userRepository.findById(recipientUserId)
                .orElseThrow(() -> exceptionUtils.userNotFoundException(recipientUserId));

        var conversation = conversationRepository.findBySenderAndRecipient(sender, recipient)
                .orElseThrow(exceptionUtils::conversationNotFoundException);
        return conversationMapper.mapToConversationDTO(conversation);

    }


    @Override
    public void createUsersConversation(UserDTO userDTO1, UserDTO userDTO2) {

        var user1 = userRepository.findById(userDTO1.getId())
                .orElseThrow(() -> exceptionUtils.userNotFoundException(userDTO1.getId()));
        var user2 = userRepository.findById(userDTO2.getId())
                .orElseThrow(() -> exceptionUtils.userNotFoundException(userDTO2.getId()));

        if (!conversationRepository.existsBySenderAndRecipient(user1, user2)
                && !conversationRepository.existsBySenderAndRecipient(user2, user1)) {
            var conversationForFirstUser = new Conversation();
            var conversationForSecondUser = new Conversation();

            conversationForFirstUser.setSender(user1);
            conversationForFirstUser.setRecipient(user2);

            conversationForSecondUser.setSender(user2);
            conversationForSecondUser.setRecipient(user1);

            conversationRepository.saveAndFlush(conversationForFirstUser);
            conversationRepository.saveAndFlush(conversationForSecondUser);

            conversationForFirstUser.setConversationWith(conversationForSecondUser);
            conversationForSecondUser.setConversationWith(conversationForFirstUser);

            conversationRepository.save(conversationForFirstUser);
            conversationRepository.save(conversationForSecondUser);

        } else {
            throw new AlreadyExistsException(translator.translate("exception.invalid.action"),
                    translator.translate("exception.conversation.already.exists"));
        }
    }

    @Override
    public List<ConversationDTO> getSenderConversation(String username) {
        return userRepository.findOneByLoginIgnoreCase(username)
                .map(conversationRepository::findBySender)
                .orElse(Collections.emptyList())
                .stream()
                .map(conversationMapper::mapToConversationDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void deleteConversation(String username, long conversationId) {

        var user = userRepository.findOneByLoginIgnoreCase(username)
                .orElseThrow(() -> exceptionUtils.userNotFoundException(username));

        conversationRepository.findById(conversationId)
                .ifPresent(conversation -> {
                    if (conversation.getSender().equals(user) || conversation.getRecipient().equals(user)) {
                        var conversationWith = conversation.getConversationWith();
                        var messagesIds = conversationMessageRepository
                                .findByConversationOrConversation(conversation, conversationWith)
                                .stream()
                                .map(ConversationMessage::getId)
                                .collect(Collectors.toList());

                        conversationMessageRepository.deleteByIdIn(messagesIds);

                        friendRequestRepository.deleteFriendRequestByUsers(conversation.getSender().getId(),
                                conversation.getRecipient().getId());

                        conversationRepository.delete(conversation);
                    } else {
                        throw new InvalidDataException(translator.translate("exception.not.owner.conversation.delete"),
                                translator.translate("exception.not.owner.conversation.delete.body"));
                    }
                });
    }

}
