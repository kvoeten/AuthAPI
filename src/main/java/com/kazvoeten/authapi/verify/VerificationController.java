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
package com.kazvoeten.authapi.verify;

import com.kazvoeten.authapi.data.Database;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author kaz_v
 */
@RestController
public class VerificationController {

    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/verify")
    public VerificationResult verify(@RequestParam(value = "email", defaultValue = "null") String email,
            @RequestParam(value = "code", defaultValue = "null") String code) {

        String right = Database.getAuthCode(email);
        if (right.equals("")) {
            return new VerificationResult("No verification process was found for the supplied email.");
        }
        if (code.equals(right)) {
            if (Database.verifyAccount(email)) {
                return new VerificationResult("The verification was successful!");
            }
            return new VerificationResult("The verification failed due to an unknown reason.");
        } else {
            return new VerificationResult("The supplied code was incorrect.");
        }
    }

}
