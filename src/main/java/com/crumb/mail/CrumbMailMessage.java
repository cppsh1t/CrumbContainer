package com.crumb.mail;

import javax.mail.Address;

public interface CrumbMailMessage extends MailMessageSessionSetter{

    MailMessage getMailMessage();

    Address[] getFrom();

    void setFrom(String from);

    void setTo(String to);

    void setTo(String... to);

    void addTo(String to);

    void addTo(String... to);

    void setSubject(String subject);

    void setText(String text);
}
