package com.crumb.util;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class MailUtil {

    public static InternetAddress makeInternetAddress(String address) {
        try {
            return new InternetAddress(address);
        } catch (AddressException e) {
            e.printStackTrace();
            return null;
        }
    }
}
