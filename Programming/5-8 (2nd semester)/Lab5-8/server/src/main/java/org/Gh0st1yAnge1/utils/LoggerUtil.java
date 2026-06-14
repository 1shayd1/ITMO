package org.Gh0st1yAnge1.utils;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerUtil {
    private static final Logger logger = Logger.getLogger("ServerLogger");

    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.INFO);
        handler.setFormatter(new SimpleFormatter());

        logger.addHandler(handler);
        logger.setLevel(Level.INFO);
        logger.setUseParentHandlers(false);
    }

    public static Logger getLogger(){
        return logger;
    }

    public static void logInfo(String message){
        logger.info(message);
    }

    public static void logWarning(String message){
        logger.warning(message);
    }

    public static void logSevere(String message){
        logger.severe(message);
    }

    public static void logFine(String message){
        logger.fine(message);
    }
}
