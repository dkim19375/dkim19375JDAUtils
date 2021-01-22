package me.dkim19375.dkim19375jdautils.files;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

public class FileGetterUtils {
    public static String getPrefix(Properties properties) {
        properties.putIfAbsent("prefix", "?");
        try (OutputStream output = new FileOutputStream("options.properties")) {
            properties.store(output, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties.getProperty("prefix", "?");
    }

    public static String getPrefix(PropertiesFile properties) {
        properties.getProperties().putIfAbsent("prefix", "?");
        properties.saveFile();
        return properties.getProperties().getProperty("prefix", "?");
    }

    public static void setPrefix(PropertiesFile properties, String prefix) {
        properties.getProperties().put("prefix", prefix);
        properties.saveFile();
    }

    public static String getToken(PropertiesFile properties) {
        return properties.getProperties().getProperty("token", "TOKEN");
    }
}