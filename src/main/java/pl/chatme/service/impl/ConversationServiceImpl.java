package pl.chatme.service.impl;

import org.springframework.stereotype.Service;
import pl.chatme.domain.Conversation;
import pl.chatme.domain.User;
import pl.chatme.exception.AlreadyExistsException;
import pl.chatme.repository.ConversationRepository;
import pl.chatme.repository.UserRepository;
import pl.chatme.service.ConversationService;
import pl.chatme.util.ExceptionUtils;
import pl.chatme.util.Translator;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final Translator translator;
    private final ExceptionUtils exceptionUtils;

    public ConversationServiceImpl(ConversationRepository conversationRepository,
                                   UserRepository userRepository,
                                   Translator translator,
                                   ExceptionUtils exceptionUtils) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.translator = translator;
        this.exceptionUtils = exceptionUtils;
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

}
