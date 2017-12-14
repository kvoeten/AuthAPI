/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kazvoeten.authapi.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
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
    private static final String host, service, address;
    private static final String authMessage = 
            "Your %s authentication code is: %n.\r\n"
            + "This code will remain valid for 15 minutes.\r\n"
            + "\r\nIf you did not request this code you can ignore this message.";

    public static void sendAuthMail(String recipent, int code) throws AddressException, MessagingException {

        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", host);

        Session session = Session.getDefaultInstance(properties);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(address));

            message.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(host));

            message.setSubject("Verification code.");
            message.setText(String.format(authMessage, service, code));

            Transport.send(message);
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }

    }
    
    static {
        File f = new File("config.ini");
        if (!f.exists()) {
            try (FileOutputStream fout = new FileOutputStream(f)) {
                PrintStream out = new PrintStream(fout);
                out.println("[Service Information]");
                out.println("smtp_host = localhost");
                out.println("service_title = AithAPI");
                out.println("service_email = test@localhost");
                fout.flush();
                fout.close();
            } catch (Exception e) {
            }
            System.out.println("Please configure 'config.ini' and relaunch the service.");
            System.exit(0);
        }
        Properties p = new Properties();
        String a = "", b = "", c = ""; 
        try {
            FileReader fr = new FileReader(f);
            p.load(fr);
            a = p.getProperty("smtp_host");
            b = p.getProperty("service_title");
            c = p.getProperty("service_email");
            fr.close();
        } catch (Exception e) {
            System.exit(1);
            e.printStackTrace();
        }
        host = a;
        service = b;
        address = c;
        p.clear();
    }
}
