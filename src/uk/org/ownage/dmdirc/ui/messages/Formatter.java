package uk.org.ownage.dmdirc.ui.messages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import uk.org.ownage.dmdirc.Config;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.logger.Logger;

public class Formatter {
    
    private static Properties properties;
    
    private Formatter() {
    }
    
    public static String formatMessage(String messageType, Object... arguments) {
        if (properties == null) initialise();
        return String.format(properties.getProperty(messageType), arguments);
    }
    
    private static Properties getDefaults() {
        Properties properties = new Properties();
        properties.setProperty("channelMessage", "<%s> %s");
        return properties;
    }
    
    private static void initialise() {
        if (properties == null) {
            File file;
            if (Config.hasOption("ui", "formatter")) {
                file = new File(Config.getOption("ui", "formatter"));
            } else {
                file = new File(Config.getConfigDir()+"format.properties");
            }
            properties = getDefaults();
            if (file.exists()) {
                try {
                    properties.load(new FileInputStream(file));
                } catch (FileNotFoundException ex) {
                    //Do nothing, defaults used
                } catch (InvalidPropertiesFormatException ex) {
                    Logger.error(ErrorLevel.INFO, ex);
                } catch (IOException ex) {
                    Logger.error(ErrorLevel.WARNING, ex);
                }
            } else {
                try {
                    file.createNewFile();
                } catch (IOException ex) {
                    Logger.error(ErrorLevel.WARNING, ex);
                }
            }
        }
    }
}
