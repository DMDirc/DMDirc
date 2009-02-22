/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.util;

import com.dmdirc.Main;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import net.miginfocom.Base64;

/**
 * Helper class to encrypt and decrypt strings, requests passwords if needed.
 */
public class CipherUtils {
    
    /** Singleton instance. */
    private static CipherUtils me;
    
    /** Encryption cipher. */
    private Cipher ecipher;
    
    /** Decryption cipher. */
    private Cipher dcipher;
    
    /** Salt. */
    private final byte[] SALT = {
        (byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32,
        (byte) 0x56, (byte) 0x35, (byte) 0xE3, (byte) 0x03,
    };
    
    /** Iteration count. */
    private static final int ITERATIONS = 19;
    
    /** Number of auth attemps before failing the attempt. */
    private static final int AUTH_TRIES = 4;
    
    /** User password. */
    private String password;
    
    /**
     * Prevents creation of a new instance of Encipher.
     */
    protected CipherUtils() {
        // Do nothing
    }
    
    /**
     * Retrieves a singleton instance of CipherUtils.
     * 
     * @return A singleton cipher utils instance.
     */
    public static CipherUtils getCipherUtils() {
        synchronized(CipherUtils.class) {
            if (me == null) {
                me = new CipherUtils();
            }
            
            return me;
        }
    }
    
    /**
     * Encrypts a string using the stored settings. Will return null if the
     * automatic user authentication fails - use checkauth and auth.
     * @param str String to encrypt
     * @return Encrypted string
     */
    public String encrypt(final String str) {
        if (!checkAuthed()) {
            if (auth()) {
                createCiphers();
            } else {
                return null;
            }
        }
        try {
            return Base64.encodeToString(ecipher.doFinal(str.getBytes("UTF8")), false);
        } catch (BadPaddingException e) {
            Logger.userError(ErrorLevel.LOW, "Unable to decrypt string: " + e.getMessage());
        } catch (IllegalBlockSizeException e) {
            Logger.userError(ErrorLevel.LOW, "Unable to decrypt string: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            Logger.userError(ErrorLevel.LOW, "Unable to decrypt string: " + e.getMessage());
        }

        return null;
    }
    
    /**
     * Encrypts a string using the stored settings. Will return null if the
     * automatic user authentication fails - use checkauth and auth.
     * @param str String to decrypt
     * @return Decrypted string
     */
    public String decrypt(final String str) {
        if (!checkAuthed()) {
            if (auth()) {
                createCiphers();
            } else {
                return null;
            }
        }
        try {
            return new String(dcipher.doFinal(Base64.decode(str)));
        } catch (BadPaddingException e) {
            Logger.userError(ErrorLevel.LOW, "Unable to decrypt string: " + e.getMessage());
        } catch (IllegalBlockSizeException e) {
            Logger.userError(ErrorLevel.LOW, "Unable to decrypt string: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Performs a SHA-512 hash.
     * @param data String to hashed
     * @return hashed string
     */
    public String hash(final String data) {
        try {
            return new String(MessageDigest.getInstance("SHA-512")
            .digest(data.getBytes("UTF8")), Charset.forName("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            Logger.userError(ErrorLevel.LOW, "Unable to hash string");
        } catch (IOException e) {
            Logger.userError(ErrorLevel.LOW, "Unable to hash string");
        }
        return null;
    }
    
    /**
     * Checks if a user is authed.
     *
     * @return true if authed, false otherwise
     */
    public boolean checkAuthed() {
        if (dcipher != null && ecipher != null) {
            return true;
        }
        return false;
    }
    
    /**
     * creates ciphers.
     */
    protected void createCiphers() {
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
        } catch (InvalidAlgorithmParameterException e) {
            Logger.userError(ErrorLevel.LOW, "Unable to create ciphers");
            ecipher = null;
            dcipher = null;
        } catch (InvalidKeySpecException e) {
            Logger.userError(ErrorLevel.LOW, "Unable to create ciphers");
            ecipher = null;
            dcipher = null;
        } catch (NoSuchPaddingException e) {
            Logger.userError(ErrorLevel.LOW, "Unable to create ciphers");
            ecipher = null;
            dcipher = null;
        } catch (NoSuchAlgorithmException e) {
            Logger.userError(ErrorLevel.LOW, "Unable to create ciphers");
            ecipher = null;
            dcipher = null;
        } catch (InvalidKeyException e) {
            Logger.userError(ErrorLevel.LOW, "Unable to create ciphers");
            ecipher = null;
            dcipher = null;
        }
    }
    
    /**
     * Auths a user and sets the password.
     *
     * @return true if auth was successful, false otherwise.
     */
    public boolean auth() {
        String passwordHash = null;
        String prompt = "Please enter your password";
        int tries = 1;
        if (IdentityManager.getGlobalConfig().hasOptionString("encryption", "password")) {
            password = IdentityManager.getGlobalConfig().getOption("encryption", "password");
        } else {
            if (IdentityManager.getGlobalConfig().hasOptionString("encryption",
                    "passwordHash")) {
                passwordHash = IdentityManager.getGlobalConfig().getOption("encryption",
                        "passwordHash");
            }

            while ((password == null || password.isEmpty()) && tries < AUTH_TRIES) {
                password = getPassword(prompt);
                if (passwordHash == null) {
                    passwordHash = hash(password);
                    IdentityManager.getConfigIdentity().setOption("encryption", 
                            "passwordHash", passwordHash);
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
    
    /**
     * Requests the encryption password from the user.
     * 
     * @param prompt The prompt to show
     * @return The user-specified password
     */
    protected String getPassword(final String prompt) {
        return Main.getUI().getUserInput(prompt);
    }
}
