package org.crumb.core;

import ch.qos.logback.classic.Level;

public class LoggerManager {

    static Level currentLevel = Level.INFO;

    public static void setLoggerLevel(String level) {
        switch (level) {
            case "INFO" -> currentLevel = Level.INFO;
            case "DEBUG" -> currentLevel = Level.DEBUG;
            case "WARN" -> currentLevel = Level.WARN;
            case "TRACE" -> currentLevel = Level.TRACE;
            case "ERROR" -> currentLevel = Level.ERROR;
            case "OFF" -> currentLevel = Level.OFF;
            case "ALL" -> currentLevel = Level.ALL;
        }
    }

    public static void setLoggerLevel(Level level) {
        currentLevel = level;
    }
}
