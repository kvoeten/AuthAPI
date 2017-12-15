/*
    This file is part of AuthAPI by Kaz Voeten.

    AuthAPI is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    AuthAPI is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with AuthAPI.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.kazvoeten.authapi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.security.Key;
import java.util.Properties;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *
 * @author kaz_v
 */
@SpringBootApplication
public class Application {

    public static final String SMTP_HOST, SERVICE_NAME, SERVICE_EMAIL;
    public static final Key TOKEN_ENCRYPTION_KEY;

    /**
     * Initializes the application.
     *
     * @param args Start line args.
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    static {
        File f = new File("config.ini");
        if (!f.exists()) {
            try (FileOutputStream fout = new FileOutputStream(f)) {
                PrintStream out = new PrintStream(fout);
                out.println("[Service Information]");
                out.println("SMTP_HOST=localhost");
                out.println("SERVICE_NAME=AuthAPI.com");
                out.println("SERVICE_EMAIL=service@authapi.com");
                out.println("TOKEN_ENCRYPTION_KEY=NovakMadeDisTing");
                fout.flush();
                fout.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Please configure 'config.ini' and relaunch the service.");
            System.exit(0);
        }
        Properties p = new Properties();
        String a = "", b = "", c = "", d = "";
        try {
            try (FileReader fr = new FileReader(f)) {
                p.load(fr);
                a = p.getProperty("SMTP_HOST");
                b = p.getProperty("SERVICE_NAME");
                c = p.getProperty("SERVICE_EMAIL");
                d = p.getProperty("TOKEN_ENCRYPTION_KEY");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        SMTP_HOST = a;
        SERVICE_NAME = b;
        SERVICE_EMAIL = c;
        TOKEN_ENCRYPTION_KEY = new SecretKeySpec(d.getBytes(), "AES");
        p.clear();
    }
}
