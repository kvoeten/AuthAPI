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

import java.util.HashMap;

/**
 *
 * @author kaz_v
 */
public class CreationThrottle {

    private static HashMap<String, Long> history = new HashMap<>(); //Map of recent account creations by IP.
    private static final int time = 1000 * 60 * 15; //15 Minutes between each creation.

    /**
     * Checks if the supplied IP has created an Account in the last @time minutes.
     * 
     * @param IP User's remote address.
     * @return true if user has not made an account recently.
     */
    public static boolean checkIP(String IP) {
        if (history.containsKey(IP)) {
            if (history.get(IP) <= System.currentTimeMillis()) {
                history.put(IP, System.currentTimeMillis() + time);
                return true;
            } else {
                return false;
            }
        } else {
            history.put(IP, System.currentTimeMillis() + time); //New account every 15 minutes max
            return true;
        }
    }
}
