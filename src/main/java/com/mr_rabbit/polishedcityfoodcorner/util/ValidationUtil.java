package com.mr_rabbit.polishedcityfoodcorner.util;

import java.util.regex.Pattern;

public class ValidationUtil {

    // First Name (Middle Name) Surname - every word must start with a capital letter.
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[A-Z][a-zA-Z]*(\\s[A-Z][a-zA-Z]*){1,2}$");

    // lowercase letters / digits / underscore only, must end with @gmail.com
    private static final Pattern GMAIL_PATTERN =
            Pattern.compile("^[a-z0-9_]+@gmail\\.com$");

    // must start with 017, 019, 016, 015 or 018 and be exactly 11 digits long
    private static final Pattern MOBILE_PATTERN =
            Pattern.compile("^(017|019|016|015|018)\\d{8}$");

    // at least 6 characters, containing at least one of @ # $ & ! ? % *
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[@#$&!?%*]).{6,}$");

    private ValidationUtil() {
    }

    public static boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name.trim()).matches();
    }

    public static boolean isValidGmail(String email) {
        return email != null && GMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidMobile(String mobile) {
        return mobile != null && MOBILE_PATTERN.matcher(mobile.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }
}
