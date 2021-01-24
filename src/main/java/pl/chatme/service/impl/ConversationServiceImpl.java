package pl.chatme.service.impl;

import org.springframework.stereotype.Service;
import pl.chatme.domain.Conversation;
import pl.chatme.domain.ConversationMessage;
import pl.chatme.domain.User;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationMessageRepository conversationMessageRepository;
    private final UserRepository userRepository;
    private final Translator translator;
    private final ExceptionUtils exceptionUtils;
    private final FriendRequestRepository friendRequestRepository;

    public ConversationServiceImpl(ConversationRepository conversationRepository,
                                   ConversationMessageRepository conversationMessageRepository,
                                   UserRepository userRepository,
                                   Translator translator,
                                   ExceptionUtils exceptionUtils,
                                   FriendRequestRepository friendRequestRepository) {
        this.conversationRepository = conversationRepository;
        this.conversationMessageRepository = conversationMessageRepository;
        this.userRepository = userRepository;
        this.translator = translator;
        this.exceptionUtils = exceptionUtils;
        this.friendRequestRepository = friendRequestRepository;
    }

    @Transactional
    @Override
    public Optional<Conversation> getConversation(String senderUsername, long recipientUserId) {

        var senderUserOptional = userRepository.findOneByLoginIgnoreCase(senderUsername);
        var recipientUserOptional = userRepository.findById(recipientUserId);

        if (senderUserOptional.isPresent() && recipientUserOptional.isPresent()) {
            var sender = senderUserOptional.get();
            var recipient = recipientUserOptional.get();

            return conversationRepository.findBySenderAndRecipient(sender, recipient);
        } else {
            throw exceptionUtils.conversationNotFoundException();
        }
    }


    @Override
    public void createUsersConversation(User user1, User user2) {

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
    public List<Conversation> getSenderConversation(String username) {
        return userRepository.findOneByLoginIgnoreCase(username)
                .map(conversationRepository::findBySender)
                .orElseThrow(() -> exceptionUtils.userNotFoundException(username));
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
