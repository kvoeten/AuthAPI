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
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 *
 * @author kaz_v
 */
public class Database {

    private static final HikariConfig config; //Hikari database config.
    private static final HikariDataSource ds; //Hikari datasource based on config.
    private static HashMap<String, Account> accounts = new HashMap<>(); //Map of loaded accounts by token.
    private static HashMap<String, Pair<String, Date>> authCodes = new HashMap<>(); //Map of account verification codes sorted by email.
    private static Random rand = new Random();

    static {
        //Check if file exists, if not: create and use default file.
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

    /**
     * Loads an account from the database by given name/e-mail. If the password matches the on in the database a token is generated and the
     * account is loaded. Incorrect account info or database errors are caught and result in different return codes.
     *
     * @param name Account username or email
     * @param password Account password (plaintext)
     * @return LoginResponseCode operation result.
     */
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
                String email = rs.getString("email");
                boolean verified = rs.getBoolean("verified");
                Date genTime = new Date();
                String token = TokenFactory.genToken(id, name, genTime);
                accounts.put(token, new Account(id, verified, rs.getString("name"), token, email, rs.getString("ip"),
                        rs.getByte("state"), rs.getByte("admin"), rs.getByte("gender"),
                        rs.getDate("creation"), genTime, rs.getDate("history"), rs.getDate("birthday")));
                return verified ? LoginResponseCode.SUCCESS : LoginResponseCode.UNVERIFIED;
            } else {
                return LoginResponseCode.FAIL_UNKNOWN;
            }
        } catch (Exception ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            return LoginResponseCode.SERVICE_UNAVAILABLE;
        }
    }

    /**
     * Gets Account from loaded accounts by name
     *
     * @param name Name of the Account.
     * @return Account
     */
    public static Account getAccountByName(String name) {
        for (Account acc : accounts.values()) {
            if (acc.getName().equals(name)
                    || acc.getEmail().equals(name)) {
                return acc;
            }
        }
        return null;
    }

    /**
     * Gets Account from loaded accounts by token
     *
     * @param token Token assigned to the Account.
     * @return Account
     */
    public static Account getAccountByToken(String token) {
        return accounts.get(token);
    }

    public static CreationResponseCode verifyAccountName(String name, String email) {
        try {
            Connection connection = ds.getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM accounts WHERE name = ? OR email = ?");
            ps.setString(1, name);
            ps.setString(2, email);
            ResultSet rs = ps.executeQuery();
            if (rs.first()) {
                if (rs.getBoolean("verified")) {
                    return CreationResponseCode.FAILED;
                }
                return CreationResponseCode.EXISTS_UNVERIFIED;
            }
            return CreationResponseCode.SUCCESS;
        } catch (Exception ex) {
            return CreationResponseCode.FAILED;
        }
    }

    /**
     *
     *
     * @param email New Account's email
     * @param name New Account's name
     * @param password New Account's password
     * @param birthday New Account's birthday
     * @param gender New Account's gender
     * @param ip New Account's ip
     * @return
     */
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
            return CreationResponseCode.SUCCESS;
        } catch (Exception ex) {
            ex.printStackTrace();
            return CreationResponseCode.FAILED;
        }
    }

    public static void checkTokenExpiration() {
        long expiration = (1000 * 60 * 15); //Tokens expire after 15 mins.
        long time = (new Date()).getTime(); //Current time.
        accounts.forEach((token, account) -> {
            if (account.getLoaded().getTime() + expiration <= time) {
                accounts.remove(token);
            }
        });
    }

    public static void checkAuthCodesExpirated() {
        long expiration = (1000 * 60 * 15); //Auth codes expire after 15 mins.
        long time = (new Date()).getTime(); //Current time.
        authCodes.forEach((name, pair) -> {
            if (pair.getValue().getTime() + expiration <= time) {
                authCodes.remove(name);
            }
        });
    }

    public static String getAuthCode(String email) {
        if (authCodes.containsKey(email)) {
            return authCodes.get(email).getKey();
        }
        return "";
    }

    public static void addAuthcode(String email) {
        authCodes.put(email, new Pair<>(TokenFactory.genAuthenCode(), new Date()));
    }

    public static boolean verifyAccount(String email) {
        try {
            Connection connection = ds.getConnection();
            PreparedStatement ps = connection.prepareStatement("UPDATE accounts SET verified=true WHERE email=?");
            ps.setString(1, email);
            ps.execute();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
