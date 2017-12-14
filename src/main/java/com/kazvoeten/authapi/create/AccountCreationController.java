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
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
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

        String ip = request.getRemoteAddr(); //Verify if IP has made an account recently.
        if (CreationThrottle.checkIP(ip)) {
            CreationResponseCode verified = Database.verifyAccount(name, email);
            switch (verified) {
                case FAILED:
                    return new AccountCreationResponse(verified.getValue(),
                            "The provided name or email is already in use.");
                case EXISTS_UNVERIFIED:
                    return new AccountCreationResponse(verified.getValue(),
                            "This account already exists but hasn't been verified yet. Please login to this account to initiate "
                            + "the verification process.");
                case SUCCESS:
                    CreationResponseCode returned = Database.createAccount(email, name, password, birthday, gender, ip);
                    String message = "Account created succesfully! Please use to code sent to your e-mail adress to verify the account.";
                    if (returned == CreationResponseCode.FAILED) {
                        message = "Failed";
                    } else {
                        try {
                            Email.sendAuthMail(ip, Database.getAuthCode(name));
                        } catch (MessagingException ex) {
                            Logger.getLogger(AccountCreationController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    return new AccountCreationResponse(returned.getValue(), message);
                default:
                    return new AccountCreationResponse(verified.getValue(),
                            "Err.");
            }
        } else {
            return new AccountCreationResponse(CreationResponseCode.RECENT_BLOCK.getValue(),
                    "15 minutes needs to have passed before you can create another account.");
        }
    }
}
