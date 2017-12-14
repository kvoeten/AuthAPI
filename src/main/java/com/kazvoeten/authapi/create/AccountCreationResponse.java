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

/**
 *
 * @author kaz_v
 */
public class AccountCreationResponse {
    private final int value; //Response value
    private final String response; //Response message
    
    /**
     * API response constructor
     * 
     * @param value API response value.
     * @param response API response text.
     */
    public AccountCreationResponse(int value, String response) {
        this.value = value;
        this.response = response;
    }
    
    /**
     * @return  Object's API response code value.
     */
    public int getValue() {
        return value;
    }
    
    /**
     * @return Object's API response text.
     */
    public String getResponse() {
        return response;
    }
    
}
