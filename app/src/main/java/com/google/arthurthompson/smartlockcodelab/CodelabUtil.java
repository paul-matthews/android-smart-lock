package com.google.arthurthompson.smartlockcodelab;

import com.google.android.gms.auth.api.credentials.Credential;

/**
 * Created by arthurthompson on 9/15/15.
 */
public class CodelabUtil {

    public static boolean isValidCredential(Credential credential) {
        String username = credential.getId();
        String password = credential.getPassword();
        return isValidCredential(username, password);
    }

    public static boolean isValidCredential(String username, String password) {
        if ((username.equals(UsernamesAndPasswords.username1) && password.equals(UsernamesAndPasswords.password1)) ||
                (username.equals(UsernamesAndPasswords.username2) && password.equals(UsernamesAndPasswords.password2)) ||
                (username.equals(UsernamesAndPasswords.username3) && password.equals(UsernamesAndPasswords.password3))) {
            return true;
        }
        return false;
    }
}
