package application.util;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerUtil {
    private static final Logger LOGGER = Logger.getLogger("CCLogger");

    static {
        try {
            Handler fileHandler = new FileHandler("cc.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);
            LOGGER.setLevel(Level.ALL);
            LOGGER.setUseParentHandlers(false);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao configurar logger", e);
        }
    }
    
    public static Logger getLogger(){
        return LOGGER;
    }
}