package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    private Properties properties;
    public ConfigReader(String configFile){
        properties = new Properties();
        try {
            FileInputStream inputStream = new FileInputStream(configFile);
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getProperty(String property){
        return properties.getProperty(property);
    }

}
