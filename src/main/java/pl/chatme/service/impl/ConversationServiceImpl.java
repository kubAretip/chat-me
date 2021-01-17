package pl.chatme.service.impl;

import org.springframework.stereotype.Service;
import pl.chatme.domain.Conversation;
import pl.chatme.domain.User;
import pl.chatme.repository.ConversationRepository;
import pl.chatme.repository.UserRepository;
import pl.chatme.service.ConversationService;
import pl.chatme.service.exception.AlreadyExistsException;
import pl.chatme.service.exception.NotFoundException;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public ConversationServiceImpl(ConversationRepository conversationRepository,
                                   UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
    }

    //TODO add exceptions
    @Transactional
    @Override
    public Optional<Conversation> getConversation(String senderUsername, long recipientUserId) {

        var senderUserOptional = userRepository.findOneByLoginIgnoreCase(senderUsername);
        var recipientUserOptional = userRepository.findById(recipientUserId);

        if (senderUserOptional.isPresent() && recipientUserOptional.isPresent()) {
            var sender = senderUserOptional.get();
            var recipient = recipientUserOptional.get();

            return conversationRepository.findBySenderAndRecipient(sender, recipient);
        }

        return Optional.empty();
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
            throw new AlreadyExistsException("Something gone wrong.", "Conversation for users already exists.");
        }
    }

    @Override
    public List<Conversation> getSenderConversation(String username) {
        return userRepository.findOneByLoginIgnoreCase(username)
                .map(conversationRepository::findBySender)
                .orElseThrow(() -> new NotFoundException("User not found.", "User with login " + username + " not exists."));
    }


}
