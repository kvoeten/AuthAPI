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
package com.kazvoeten.authapi.login;

import com.kazvoeten.authapi.login.account.Account;
import com.kazvoeten.authapi.data.Database;
import com.kazvoeten.authapi.data.Email;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author kaz_v
 */
@RestController
public class LoginController {

    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/login")
    public LoginResponse login(
            @RequestParam(value = "name", defaultValue = "null") String name,
            @RequestParam(value = "password", defaultValue = "password") String password) {

        LoginResponseCode code = Database.processLogin(name, password);
        if (code == LoginResponseCode.SUCCESS) {
            Account acc = Database.getAccountByName(name);
            return new LoginResponse(acc.getName(), acc.getToken(), code);
        }

        if (code == LoginResponseCode.UNVERIFIED) {
            Account acc = Database.getAccountByName(name);
            if (!Database.getAuthCode(acc.getName()).equals("")) {
                return new LoginResponse("Please use the verification code sent to the account's "
                        + "e-mail address to verify the account.", "-1", code);
            } else {
                Database.addAuthcode(acc.getEmail());
                try {
                    Email.sendAuthMail(acc.getEmail(), Database.getAuthCode(acc.getEmail()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return new LoginResponse("New authentication code sent to account email.", "-1", code);
            }
        }

        return new LoginResponse("Login failed.", "null", code);
    }
}
