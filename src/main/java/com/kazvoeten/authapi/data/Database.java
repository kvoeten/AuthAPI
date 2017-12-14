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
package com.kazvoeten.authapi.data;

import com.kazvoeten.authapi.create.CreationResponseCode;
import com.kazvoeten.authapi.login.account.Account;
import com.kazvoeten.authapi.crypto.BCrypt;
import com.kazvoeten.authapi.crypto.TokenFactory;
import com.kazvoeten.authapi.login.LoginResponseCode;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 *
 * @author kaz_v
 */
public class Database {

    private static final HikariConfig config;
    private static final HikariDataSource ds;
    private static HashMap<String, Account> accounts = new HashMap<>();

    static {
        File properties = new File("database.properties");
        if (!properties.exists()) {
            try (FileOutputStream fout = new FileOutputStream(properties)) {
                PrintStream out = new PrintStream(fout);
                out.println("dataSourceClassName=org.mariadb.jdbc.MariaDbDataSource");
                out.println("dataSource.user=root");
                out.println("dataSource.password=");
                out.println("dataSource.databaseName=service");
                out.println("dataSource.portNumber=3306");
                out.println("dataSource.serverName=localhost");
                fout.flush();
                fout.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("No database.properties file found. A default one has been generated.");
        }
        config = new HikariConfig("database.properties");
        ds = new HikariDataSource(config);
    }

    public static LoginResponseCode processLogin(String name, String password) {
        try {
            Connection connection = ds.getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM accounts WHERE name = ? OR email = ?");
            ps.setString(1, name);
            ps.setString(2, name);
            ResultSet rs = ps.executeQuery();
            if (rs.first()) {
                int id = rs.getInt("id");
                String pwd = rs.getString("password");
                int banned = rs.getInt("banned");
                if (banned > 0) {
                    return LoginResponseCode.BANNED;
                }
                if (!BCrypt.checkpw(password, pwd)) {
                    return LoginResponseCode.WRONG_INFO;
                }
                Date genTime = new Date();
                String token = TokenFactory.genToken(id, name, genTime);
                accounts.put(token, new Account(id, rs.getString("name"), token, rs.getString("email"), rs.getString("ip"),
                        rs.getByte("state"), rs.getByte("admin"), rs.getByte("gender"),
                        rs.getDate("creation"), genTime, rs.getDate("history"), rs.getDate("birthday")));
                return LoginResponseCode.SUCCESS;
            } else {
                return LoginResponseCode.FAIL_UNKNOWN;
            }
        } catch (Exception ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            return LoginResponseCode.SERVICE_UNAVAILABLE;
        }
    }

    public static Account getAccountByName(String name) {
        for (Account acc : accounts.values()) {
            if (acc.getName().equals(name)) {
                return acc;
            }
        }
        return null;
    }

    public static Account getAccountByToken(String token) {
        return accounts.get(token);
    }

    public static boolean isDuplicateName(String name, String email) {
        try {
            Connection connection = ds.getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM accounts WHERE name = ? OR email = ?");
            ps.setString(1, name);
            ps.setString(2, email);
            ResultSet rs = ps.executeQuery();
            return rs.first();
        } catch (Exception ex) {
            return true;
        }
    }
    
    public static CreationResponseCode createAccount(String email, String name, String password, String birthday, String gender, String ip) {
        try {
            Connection connection = ds.getConnection();
            PreparedStatement ps = connection.prepareStatement(""
                    + "INSERT INTO accounts (name, email, password, birthday, creation, history, gender, ip) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, BCrypt.hashpw(password, BCrypt.gensalt()));
            ps.setDate(4, new java.sql.Date(
                new DateTime(
                    Integer.parseInt(birthday.substring(4, 8)), //y
                    Integer.parseInt(birthday.substring(2, 4)), //m
                    Integer.parseInt(birthday.substring(0, 2)), //d
                    0, 0, DateTimeZone.getDefault()
                ).getMillis())
            );
            ps.setDate(5, new java.sql.Date(System.currentTimeMillis()));
            ps.setDate(6, new java.sql.Date(System.currentTimeMillis()));
            ps.setByte(7, (byte) Integer.parseInt(gender));
            ps.setString(8, ip);
            ps.execute();
            return CreationResponseCode.SUCCES;
        } catch (Exception ex) {
            ex.printStackTrace();
            return CreationResponseCode.FAILED;
        }
    }
}
