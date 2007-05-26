/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dmdirc;

import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.swing.JOptionPane;

/**
 * Helper class to encrypt and decrypt strings, requests passwords if needed.
 */
public final class CipherUtils {
    /**
     * Encryption cipher.
     */
    private static Cipher ecipher;
    
    /**
     * Decryption cipher.
     */
    private static Cipher dcipher;
    
    /**
     * Salt.
     */
    private static final byte[] SALT = {
        (byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32,
        (byte) 0x56, (byte) 0x35, (byte) 0xE3, (byte) 0x03,
    };
    
    /**
     * Iteration count.
     */
    private static final int ITERATIONS = 19;
    
    /**
     * number of auth attemps before failing the attempt.
     */
    private static final int AUTH_TRIES = 4;
    
    /**
     * User password.
     */
    private static String password;
    
    /**
     * Prevents creation of a new instance of Encipher.
     */
    private CipherUtils() {
    }
    
    /**
     * Encrypts a string using the stored settings. Will return null if the
     * automatic user authentication fails - use checkauth and auth.
     * @param str String to encrypt
     * @return Encrypted string
     */
    public static String encrypt(final String str) {
        if (!checkAuthed()) {
            if (auth()) {
                createCiphers();
            } else {
                return null;
            }
        }
        try {
            return new String(ecipher.doFinal(str.getBytes("UTF8")));
        } catch (javax.crypto.BadPaddingException e) {
            Logger.error(ErrorLevel.WARNING, "Unable to decrypt string", e);
        } catch (IllegalBlockSizeException e) {
            Logger.error(ErrorLevel.WARNING, "Unable to decrypt string", e);
        } catch (UnsupportedEncodingException e) {
            Logger.error(ErrorLevel.WARNING, "Unable to decrypt string", e);
        } catch (java.io.IOException e) {
            Logger.error(ErrorLevel.WARNING, "Unable to decrypt string", e);
        }
        return null;
    }
    
    /**
     * Encrypts a string using the stored settings. Will return null if the
     * automatic user authentication fails - use checkauth and auth.
     * @param str String to decrypt
     * @return Decrypted string
     */
    public static String decrypt(final String str) {
        if (!checkAuthed()) {
            if (auth()) {
                createCiphers();
            } else {
                return null;
            }
        }
        try {
            return new String(dcipher.doFinal(str.getBytes()), "UTF8");
        } catch (javax.crypto.BadPaddingException e) {
            Logger.error(ErrorLevel.WARNING, "Unable to decrypt string", e);
        } catch (IllegalBlockSizeException e) {
            Logger.error(ErrorLevel.WARNING, "Unable to decrypt string", e);
        } catch (UnsupportedEncodingException e) {
            Logger.error(ErrorLevel.WARNING, "Unable to decrypt string", e);
        } catch (java.io.IOException e) {
            Logger.error(ErrorLevel.WARNING, "Unable to decrypt string", e);
        }
        return null;
    }
    
    /**
     * Performs a SHA-512 hash.
     * @param data String to hashed
     * @return hashed string
     */
    public static String hash(final String data) {
        try {
            return new String(MessageDigest.getInstance("SHA-512")
            .digest(data.getBytes("UTF8")));
        } catch (NoSuchAlgorithmException e) {
            Logger.error(ErrorLevel.WARNING, "Unable to hash string", e);
        } catch (IOException e) {
            Logger.error(ErrorLevel.WARNING, "Unable to hash string", e);
        }
        return null;
    }
    
    /**
     * Checks if a user is authed.
     *
     * @return true if authed, false otherwise
     */
    public static boolean checkAuthed() {
        if (dcipher != null && ecipher != null) {
            return true;
        }
        return false;
    }
    
    /**
     * creates ciphers.
     */
    private static void createCiphers() {
        try {
            final KeySpec keySpec = new PBEKeySpec(
                    password.toCharArray(), SALT, ITERATIONS);
            final SecretKey key = SecretKeyFactory.
                    getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
            ecipher = Cipher.getInstance(key.getAlgorithm());
            dcipher = Cipher.getInstance(key.getAlgorithm());
            final AlgorithmParameterSpec paramSpec =
                    new PBEParameterSpec(SALT, ITERATIONS);
            ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
            dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
        } catch (java.security.InvalidAlgorithmParameterException e) {
            Logger.error(ErrorLevel.WARNING, "Unable to create ciphers", e);
            ecipher = null;
            dcipher = null;
        } catch (java.security.spec.InvalidKeySpecException e) {
            Logger.error(ErrorLevel.WARNING, "Unable to create ciphers", e);
            ecipher = null;
            dcipher = null;
        } catch (javax.crypto.NoSuchPaddingException e) {
            Logger.error(ErrorLevel.WARNING, "Unable to create ciphers", e);
            ecipher = null;
            dcipher = null;
        } catch (java.security.NoSuchAlgorithmException e) {
            Logger.error(ErrorLevel.WARNING, "Unable to create ciphers", e);
            ecipher = null;
            dcipher = null;
        } catch (java.security.InvalidKeyException e) {
            Logger.error(ErrorLevel.WARNING, "Unable to create ciphers", e);
            ecipher = null;
            dcipher = null;
        }
    }
    
    /**
     * Auths a user and sets the password.
     *
     * @return true if auth was successful, false otherwise.
     */
    public static boolean auth() {
        String passwordHash = null;
        String prompt = "Please enter your password";
        int tries = 1;
        if (Config.hasOption("encryption", "password")) {
            password = Config.getOption("encryption", "password");
        } else {
            if (Config.hasOption("encryption", "passwordHash")) {
                passwordHash = Config.getOption("encryption", "passwordHash");
            }
            passwordHash = "moo";
            while ((password == null || password.length() == 0) && tries < AUTH_TRIES) {
                password =  JOptionPane.showInputDialog(prompt);
                if (passwordHash == null) {
                    passwordHash = hash(password);
                    Config.setOption("encryption", "passwordHash", passwordHash);
                    Config.save();
                }
                if (!hash(password).equals(passwordHash)) {
                    prompt = "<html>Password mis-match<br>Please re-enter "
                            + "your password</html>";
                    tries++;
                    password = null;
                }
            }
        }
        if (tries == AUTH_TRIES) {
            return false;
        }
        return true;
    }
}
