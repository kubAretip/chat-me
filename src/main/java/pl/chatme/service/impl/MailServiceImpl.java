package pl.chatme.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import pl.chatme.domain.User;
import pl.chatme.service.SendMailService;

import javax.mail.MessagingException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
class MailServiceImpl implements SendMailService {

    private final JavaMailSender sender;
    private final TemplateEngine templateEngine;

    public MailServiceImpl(JavaMailSender sender,
                           TemplateEngine templateEngine) {
        this.sender = sender;
        this.templateEngine = templateEngine;
    }


    public void sendEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        var mimeMessage = this.sender.createMimeMessage();

        try {
            var mimeHelper = new MimeMessageHelper(mimeMessage, isMultipart, StandardCharsets.UTF_8.name());
            mimeHelper.setTo(to);
            //mimeHelper.setFrom();
            mimeHelper.setSubject(subject);
            mimeHelper.setText(content, isHtml);

            sender.send(mimeMessage);
        } catch (MailException | MessagingException e) {
            // e.printStackTrace();
            log.warn("Emil wasn't sent to user {}", to, e);
        }
    }

    public void sendMailTemplate(User user, String template, String subjectKey) {

        var userEmail = user.getEmail();
        if (userEmail != null) {
            log.debug("Sending email template to {}", userEmail);

            var context = new Context();
            context.setVariable("user", user);
            String content = templateEngine.process(template, context);

            sendEmail(userEmail, subjectKey, content, false, true);
        }
    }

    @Override
    public void sendActivationEmail(User user) {
        log.debug("Sending activation email to user with id {}", user.getId());
        sendMailTemplate(user, "mail/activationEmail", "Aktywacja konta");
    }


}
