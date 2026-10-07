package com.mr_rabbit.polishedcityfoodcorner.util;

public class AppConfig {

    private AppConfig() {
    }

    /**
     * When false (default): an employee can only check in ONCE per calendar day
     * - trying to sign in again the same day is rejected.
     * When true: an employee may sign in multiple times per day; every sign-in
     * creates a brand new shift record.
     * Flip this constant to switch the behaviour app-wide.
     */
    public static final boolean ALLOW_MULTIPLE_LOGIN_PER_DAY = false;
}
