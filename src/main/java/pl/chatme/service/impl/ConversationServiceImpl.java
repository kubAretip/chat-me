package pl.chatme.service.impl;

import org.springframework.stereotype.Service;
import pl.chatme.domain.Conversation;
import pl.chatme.repository.ConversationRepository;
import pl.chatme.repository.UserRepository;
import pl.chatme.service.ConversationService;

import javax.transaction.Transactional;
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
    public Optional<Conversation> getConversation(long senderUserId, long recipientUserId) {

        var senderUserOptional = userRepository.findById(senderUserId);
        var recipientUserOptional = userRepository.findById(recipientUserId);

        if (senderUserOptional.isPresent() && recipientUserOptional.isPresent()) {
            var sender = senderUserOptional.get();
            var recipient = recipientUserOptional.get();

            return conversationRepository.findBySenderAndRecipient(sender, recipient);
        }

        return Optional.empty();
    }


}
