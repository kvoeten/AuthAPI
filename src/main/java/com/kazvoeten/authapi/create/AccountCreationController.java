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
package com.kazvoeten.authapi.create;

import com.kazvoeten.authapi.data.Database;
import com.kazvoeten.authapi.data.Email;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.CharEncoding;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author kaz_v
 */
@RestController
public class AccountCreationController {

    private final AtomicLong counter = new AtomicLong();

    /**
     * Controls Account creation API requests.
     *
     * @param email Supplied email parameter.
     * @param name Supplied name parameter.
     * @param password Supplied password parameter.
     * @param birthday Supplied birthday parameter.
     * @param gender Supplied gender parameter.
     * @param request Supplied request parameter.
     *
     * @return new Account with supplied parameters as configuration.
     */
    @RequestMapping("/create")
    public AccountCreationResponse create(
            @RequestParam(value = "email", defaultValue = "") String email,
            @RequestParam(value = "name", defaultValue = "") String name,
            @RequestParam(value = "password", defaultValue = "") String password,
            @RequestParam(value = "birthday", defaultValue = "") String birthday,
            @RequestParam(value = "gender", defaultValue = "") String gender,
            HttpServletRequest request) {

        try {
            (new InternetAddress(email)).validate();
        } catch (Exception ex) {
            return new AccountCreationResponse(CreationResponseCode.FAILED.getValue(),
                    "Invalid e-mail address.");
        }

        if (name.length() < 5 || email.length() > 13) {
            return new AccountCreationResponse(CreationResponseCode.FAILED.getValue(),
                    "Username has to be at least 5 and maximum 13 characters long.");
        }
        
        if (password.length() < 5 || password.length() > 13) {
            return new AccountCreationResponse(CreationResponseCode.FAILED.getValue(),
                    "Password has to be at least 5 and maximum 13 characters long.");
        }

        if (!(gender.equals("0") || gender.equals("1"))) {
            return new AccountCreationResponse(CreationResponseCode.FAILED.getValue(),
                    "Gender has to be either 0 (male) or 1 (female).");
        }

        if (!Charset.forName(CharEncoding.UTF_8).newEncoder().canEncode(name)) {
            return new AccountCreationResponse(CreationResponseCode.FAILED.getValue(),
                    "Username contains invalid characters. Only utf8 characters are supported.");
        }

        if (!Charset.forName(CharEncoding.UTF_8).newEncoder().canEncode(password)) {
            return new AccountCreationResponse(CreationResponseCode.FAILED.getValue(),
                    "Password contains invalid characters. Only utf8 characters are supported.");
        }

        try {
            int day = Integer.parseInt(birthday.substring(0, 2));
            int month = Integer.parseInt(birthday.substring(2, 4));
            int year = Integer.parseInt(birthday.substring(4, 8));
            if (day > 31 || day < 1
                    || month > 12 || month < 1
                    || year > 9999 || year < 1900) {
                return new AccountCreationResponse(CreationResponseCode.FAILED.getValue(),
                        "Invalid birthday. Please use format ddmmyyyy");
            }
        } catch (Exception ex) {
            return new AccountCreationResponse(CreationResponseCode.FAILED.getValue(),
                    "Invalid birthday. Please use format ddmmyyyy");
        }

        String ip = request.getRemoteAddr(); //Verify if IP has made an account recently.
        if (CreationThrottle.checkIP(ip)) {
            CreationResponseCode valid = Database.verifyAccountName(name, email);
            switch (valid) {
                case FAILED:
                    return new AccountCreationResponse(valid.getValue(),
                            "The provided name or email is already in use.");
                case EXISTS_UNVERIFIED:
                    return new AccountCreationResponse(valid.getValue(),
                            "This account already exists but hasn't been verified yet. Please login to this account to initiate "
                            + "the verification process.");
                case SUCCESS:
                    CreationResponseCode returned = Database.createAccount(email, name, password, birthday, gender, ip);
                    String message = "Account created succesfully! Please use to code sent to your e-mail adress to verify the account.";
                    if (returned == CreationResponseCode.FAILED) {
                        message = "Failed";
                    } else {
                        try {
                            Database.addAuthcode(email);
                            Email.sendAuthMail(ip, Database.getAuthCode(name));
                        } catch (MessagingException ex) {
                            Logger.getLogger(AccountCreationController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    CreationThrottle.addIP(ip);
                    return new AccountCreationResponse(returned.getValue(), message);
                default:
                    return new AccountCreationResponse(valid.getValue(),
                            "Err.");
            }
        } else {
            return new AccountCreationResponse(CreationResponseCode.RECENT_BLOCK.getValue(),
                    "15 minutes needs to have passed before you can create another account.");
        }
    }
}
