package org.crumb.container;

import ch.qos.logback.classic.Level;

public class LoggerManager {

    static Level currentLevel = Level.OFF;

    public static void openLogger() {
        currentLevel = Level.DEBUG;
    }

    public static void closeLogger() {
        currentLevel = Level.OFF;
    }

}
