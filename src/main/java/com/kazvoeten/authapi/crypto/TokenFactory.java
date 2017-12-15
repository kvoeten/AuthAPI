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
package com.kazvoeten.authapi.crypto;

import java.security.Key;
import java.util.Date;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author kaz_v
 */
public class TokenFactory {

    private static final Key KEY = new SecretKeySpec("NovakMadeDisTing".getBytes(), "AES");
    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static final Random rand = new Random();

    /**
     * Creates a token based on ID, name and time of token generation. Tokens are encrypted with AES using 'KEY' as AESKey. Before
     * encryption all bytes of the Token's text are modified with a random integer. The random integer is appended before the actual data
     * for decryption later.
     *
     * @param id User's ID.
     * @param name User's name.
     * @param genTime Time the token was generated at.
     * @return token as Hexadecimal String.
     */
    public static String genToken(int id, String name, Date genTime) {
        String tokenText = id + ":" + name + ":" + genTime.getTime();
        int IV = rand.nextInt(Integer.MAX_VALUE);

        byte[] aTokenText = tokenText.getBytes();
        byte[] aIV = new byte[]{(byte) (IV & 0xFF), (byte) ((IV >> 8) & 0xFF), (byte) ((IV >> 16) & 0xFF), (byte) ((IV >> 24) & 0xFF)};

        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, KEY);
            byte[] encrypted = cipher.doFinal(encrypt(aTokenText, IV));

            byte[] token = new byte[encrypted.length + 4];
            System.arraycopy(aIV, 0, token, 0, 4);
            System.arraycopy(encrypted, 0, token, 4, encrypted.length);

            return bytesToHex(encrypted);
        } catch (Exception ex) {
            return "TOKEN_BROKEN";
        }
    }

    /**
     * Extracts the original token data from an encrypted token string.
     * 
     * @param token Encrypted token
     * @return String[] with token's original data (ID, name, creationTime)
     */
    public static String[] decryptToken(String token) {
        byte[] aTokenText = hexToBytes(token);
        byte[] aIV = new byte[4];
        byte[] aToken = new byte[aTokenText.length];

        System.arraycopy(aTokenText, 0, aIV, 0, 4);
        System.arraycopy(aTokenText, 4, aToken, 0, aTokenText.length - 4);

        int IV = ((int) aIV[0]) & 0xFF | (aIV[1] << 8) & 0xFF00 | (aIV[2] << 16) & 0xFF0000 | (aIV[3] << 24) & 0xFF000000;
        
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, KEY);
            
            byte[] decrypted = cipher.doFinal(decrypt(aToken, IV));
            String realToken = new String(decrypted);
            
            return realToken.split(":");
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Returns a hexadecimal string representation of any gives byte array.
     *
     * @param bytes Byte array to convert
     * @return Hexadecimal string representation of 'bytes'.
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Returns a byte array based on a given Hexadecimal string.
     *
     * @param hex Hexadecimal string
     * @return byte[] of given Hexadecimal string.
     */
    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Adds a given integer to each byte in a byte array to very simply encrypt it.
     *
     * @param pSrc Byte array to edit
     * @param dwKey Integer to add to each byte
     * @return Modified byte array.
     */
    public static byte[] encrypt(byte[] pSrc, int dwKey) {
        byte[] pDest = new byte[pSrc.length];

        for (int i = 0; i < pSrc.length; ++i) {
            pDest[i] = (byte) (dwKey + pSrc[i]);
        }

        return pDest;
    }

    /**
     * Subtracts a given integer to each byte in a byte array to very simply decrypt it.
     *
     * @param pSrc Byte array to edit
     * @param dwKey Integer to subtract from each byte
     * @return Modified byte array.
     */
    public static byte[] decrypt(byte[] pSrc, int dwKey) {
        byte[] pDest = new byte[pSrc.length];

        for (int i = 0; i < pSrc.length; ++i) {
            pDest[i] = (byte) (pSrc[i] - dwKey);
        }

        return pDest;
    }
    
    /**
     * Returns a random 4 digit string code.
     * 
     * @return random 4 digit code.
     */
    public static String genAuthenCode() {
        return String.valueOf(rand.nextInt(9))
                + String.valueOf(rand.nextInt(9))
                + String.valueOf(rand.nextInt(9))
                + String.valueOf(rand.nextInt(9));
    }
}