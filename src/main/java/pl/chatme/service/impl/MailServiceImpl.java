package pl.chatme.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import pl.chatme.domain.User;
import pl.chatme.service.SendMailService;
import pl.chatme.util.Translator;

import javax.mail.MessagingException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
class MailServiceImpl implements SendMailService {

    private final JavaMailSender sender;
    private final TemplateEngine templateEngine;
    private final Translator translator;
    private final Environment env;

    public MailServiceImpl(JavaMailSender sender,
                           TemplateEngine templateEngine,
                           Translator translator,
                           Environment env) {
        this.sender = sender;
        this.templateEngine = templateEngine;
        this.translator = translator;
        this.env = env;
    }

    @Async
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

    @Async
    public void sendMailTemplate(User user, String template, String subjectCode) {

        var userEmail = user.getEmail();
        if (userEmail != null) {
            log.debug("Sending email template lang = {} to {}", LocaleContextHolder.getLocale(), userEmail);

            var baseUrl = env.getProperty("mail.process-url");
            var subject = translator.translate(subjectCode);
            var context = new Context(LocaleContextHolder.getLocale());
            context.setVariable("user", user);
            context.setVariable("baseUrl", baseUrl);
            var content = templateEngine.process(template, context);
            sendEmail(userEmail, subject, content, false, true);
        }
    }

    @Async
    @Override
    public void sendActivationEmail(User user) {
        log.debug("Sending activation email to user with id {}", user.getId());
        sendMailTemplate(user, "mail/activationEmail", "email.activation.title");
    }


}
