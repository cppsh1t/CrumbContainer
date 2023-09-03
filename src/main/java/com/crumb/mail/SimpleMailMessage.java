package com.crumb.mail;

import com.crumb.util.MailUtil;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class SimpleMailMessage implements CrumbMailMessage{

    private final MailMessage message = new MailMessage();

    @Override
    public void setSession(Session session) {
        message.setSession(session);
    }

    public MailMessage getMailMessage() {
        return this.message;
    }

    public Address[] getFrom() {
        try {
            return message.getFrom();
        } catch (MessagingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setFrom(String from) {
        try {
            message.setFrom(from);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void setTo(String to) {
        try {
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void setTo(String... to) {
        var addresses = Arrays.stream(to).map(MailUtil::makeInternetAddress)
                .filter(Objects::nonNull).toArray(InternetAddress[]::new);
        try {
            message.setRecipients(Message.RecipientType.TO, addresses);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void addTo(String to) {
        try {
            message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void addTo(String... to) {
        var addresses = Arrays.stream(to).map(MailUtil::makeInternetAddress)
                .filter(Objects::nonNull).toArray(InternetAddress[]::new);

        try {
            message.addRecipients(Message.RecipientType.TO, addresses);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void setSubject(String subject) {
        try {
            message.setSubject(subject);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void setText(String text) {
        try {
            message.setText(text);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
