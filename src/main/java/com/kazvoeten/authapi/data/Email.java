/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kazvoeten.authapi.data;

import com.kazvoeten.authapi.Application;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author kaz_v
 */
public class Email {

    private static final String authMessage
            = "Your %s authentication code is: %s.\r\n"
            + "This code will remain valid for 15 minutes.\r\n"
            + "\r\nIf you did not request this code you can ignore this message.";

    public static void sendAuthMail(String recipent, String code) throws AddressException, MessagingException {
        System.out.println(recipent);

        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", Application.SMTP_HOST);
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.port", 25);

        Session session = Session.getDefaultInstance(properties);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(Application.SERVICE_EMAIL, "Verification generator."));

            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipent));

            message.setSubject(String.format("Your %s verification code.", Application.SERVICE_NAME));
            message.setText(String.format(authMessage, Application.SERVICE_NAME, code));

            Transport transport = session.getTransport();
            transport.connect(Application.SMTP_HOST, "", "");
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
