package ru.sovaowltv.service.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.unclassified.LanguageUtil;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Slf4j
@PropertySource("classpath:api/email.yml")
public class EmailUtil {
    private final LanguageUtil languageUtil;
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String username;

    private final HashMap<String, Long> map = new HashMap<>();

    boolean checkSpamBeforeSend(String emailTo) {
        if (emailTo == null) return false;
        if (map.containsKey(emailTo)) {
            return System.currentTimeMillis() - map.get(emailTo) > 5_000;
        } else {
            map.put(emailTo, System.currentTimeMillis());
            return true;
        }
    }

    boolean generateAndSendEmail(String emailTo, String subject, User user, String template) throws MessagingException, UnsupportedEncodingException {
        Context ctx = new Context(LocaleContextHolder.getLocale());
        ctx.setVariable("user", user);
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        mimeMessage.setFrom(new InternetAddress(username, "Sova Owl TV"));
        mimeMessage.setSubject(subject, "UTF-8");
        mimeMessage.setRecipients(Message.RecipientType.TO, emailTo);
        String htmlContent = templateEngine.process(template, ctx);
        mimeMessage.setText(htmlContent, "UTF-8", "html");
        mimeMessage.setHeader("Content-Type", "text/html; charset=UTF-8");
        mimeMessage.saveChanges();
        javaMailSender.send(mimeMessage);
        return true;
    }


    public void validateEmailVerification(String code, User user) {
        if (user == null) {
            log.error("user == null");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't find user");
        }
        if (user.getEmailVerification() == null) {
            log.error("user email already verified");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You already verified your email");
        }
        if (!user.getEmailVerification().equalsIgnoreCase(code)) {
            log.error("user email verification code not the same {} {}", code, user.getEmailVerification());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code not equals");
        }
    }

    public boolean sendRegEmail(String emailTo, User user) {
        String subject = languageUtil.getStringFor("email.subject");
        if (!checkSpamBeforeSend(emailTo)) return false;
        try {
            return generateAndSendEmail(emailTo, subject, user, "email/emailRegEmail.html");
        } catch (MessagingException | UnsupportedEncodingException | MailException e) {
            log.error("can't sendRegEmail email to {} {}", emailTo, e);
            return false;
        }
    }

    public void sendPasswordRecovery(String emailTo, String subject, User user) {
        if (!checkSpamBeforeSend(emailTo)) return;
        try {
            generateAndSendEmail(emailTo, subject, user, "email/passwordRecoveryEmail.html");
        } catch (MessagingException | UnsupportedEncodingException | MailException e) {
            log.error("can't sendPasswordRecovery email to {} {}", emailTo, e);
        }
    }
}

