package com.crumb.mail;


import javax.mail.*;
import javax.mail.internet.MimeMessage;


public class MailMessage extends MimeMessage implements MailMessageSessionSetter {

    public MailMessage() {
        super((Session) null);
    }

    @Override
    public void setSession(Session session) {
        this.session = session;
    }

}
