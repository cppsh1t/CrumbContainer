package com.crumb.mail;

import javax.mail.*;
import java.util.Properties;

public class MailSender {

    private final String host;
    private final String username;
    private final String password;
    private String port;
    private Session session;

    public MailSender(String host, String username, String password, String port) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.port = port;
        init();
    }

    public MailSender(String host, String username, String password) {
        this.host = host;
        this.username = username;
        this.password = password;
        init();
    }

    private void init() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        if (port != null) {
            props.put("mail.smtp.port", port);
        }

        session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    public void send(Message message) {
        if (message instanceof MailMessageSessionSetter mailMessage) {
            mailMessage.setSession(session);
        }

        try {
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void send(CrumbMailMessage message) {
        message.setSession(session);
        var from = message.getFrom();
        if (from == null || from.length == 0) {
            message.setFrom(username);
        }

        try {
            Transport.send(message.getMailMessage());
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
