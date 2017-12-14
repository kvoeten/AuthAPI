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
import java.util.concurrent.atomic.AtomicLong;
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

    @RequestMapping("/create")
    public AccountCreationResponse create(
            @RequestParam(value = "email", defaultValue = "") String email,
            @RequestParam(value = "name", defaultValue = "") String name,
            @RequestParam(value = "password", defaultValue = "") String password,
            @RequestParam(value = "birthday", defaultValue = "") String birthday,
            @RequestParam(value = "gender", defaultValue = "") String gender,
            HttpServletRequest request) {

        String ip = request.getRemoteAddr();
        if (CreationThrottle.checkIP(ip)) {
            if (Database.isDuplicateName(name, email)) {
                return new AccountCreationResponse(CreationResponseCode.FAILED.getValue(),
                        "The provided name or email is already in use.");
            } else {
                CreationResponseCode returned = Database.createAccount(email, name, password, birthday, gender, ip);
                String message = "Success.";
                if (returned == CreationResponseCode.FAILED) message = "Failed";
                return new AccountCreationResponse(returned.getValue(), message);
            }
        } else {
            return new AccountCreationResponse(CreationResponseCode.RECENT_BLOCK.getValue(),
                    "15 minutes needs to have passed before you can create another account.");
        }
    }
}
