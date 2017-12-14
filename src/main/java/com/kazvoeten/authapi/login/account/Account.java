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

import java.util.Date;

/**
 *
 * @author kaz_v
 */
public class Account {

    private final int id;
    private final String name, token, email, ip;
    private final byte state, admin, gender;
    private final Date created, loaded, history, birthday;

    public Account(int id, 
            String name, String token, String email, String ip, 
            byte state, byte admin, byte gender, 
            Date created, Date loaded, Date history, Date birthday) {
        
        this.id = id;
        this.name = name;
        this.token = token;
        this.email = email;
        this.ip = ip;
        this.state = state;
        this.admin = admin;
        this.gender = gender;
        this.created = created;
        this.loaded = loaded;
        this.history = history;
        this.birthday = birthday;
        
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getToken() {
        return token;
    }

    public String getEmail() {
        return email;
    }
    
    public String getIP() {
        return ip;
    }
    
    public byte getAdmin() {
        return admin;
    }

    public byte getGender() {
        return gender;
    }
    
    public byte getState() {
        return state;
    }

    public Date getCreated() {
        return created;
    }

    public Date getLoaded() {
        return loaded;
    }
    
    public Date getHistory() {
        return history;
    }
    
    public Date getBirthday() {
        return birthday;
    }
}
