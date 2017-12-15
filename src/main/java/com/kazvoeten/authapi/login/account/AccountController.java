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
package com.kazvoeten.authapi.login.account;

import com.kazvoeten.authapi.data.Database;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author kaz_v
 */
@RestController
public class AccountController {
    private final AtomicLong counter = new AtomicLong();
    private static final Account basic = new Account(-1, false, "", "", "", "", (byte) 0, (byte) 0, (byte) 0, new Date(), new Date(), new Date(), new Date());
    
    @RequestMapping("/account")
    public Account account(@RequestParam(value="token", defaultValue="null") String token) {
        Account account = Database.getAccountByToken(token);
        if (account != null && account.getVerified()) {
            return account;
        } else {
            return basic;
        }
    }
}
