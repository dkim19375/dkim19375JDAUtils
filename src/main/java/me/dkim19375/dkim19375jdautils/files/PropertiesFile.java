package me.dkim19375.dkim19375jdautils.files;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Properties;

public class PropertiesFile {
    @NotNull
    private final String fileName;
    private Properties properties;

    public PropertiesFile(@NotNull String fileName) {
        this.fileName = fileName;
    }

    public boolean createFile(boolean load) {
        try (InputStream input = PropertiesFile.class.getClassLoader().getResourceAsStream(fileName)) {
            properties = new Properties();
            if (input == null) {
                try (InputStream inputStream = new FileInputStream(fileName)) {
                    if (load) {
                        properties.load(inputStream);
                    }
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            if (load) {
                properties.load(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean createFile() {
        return createFile(false);
    }

    public boolean loadFile(@NotNull InputStream stream) {
        try {
            properties.load(stream);
        } catch (IOException exception) {
            exception.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean loadFile() {
        if (properties == null) {
            return false;
        }
        try (InputStream inputStream = new FileInputStream(fileName)) {
            properties.load(inputStream);
        } catch (FileNotFoundException e) {
            createFile(true);
            try {
                properties.load(new FileInputStream(fileName));
            } catch (IOException exception) {
                exception.printStackTrace();
                return false;
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @NotNull
    public Properties getProperties() {
        return properties;
    }

    public boolean saveFile() {
        try (OutputStream output = new FileOutputStream(fileName)) {
            properties.store(output, null);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean saveFile(boolean create) {
        try (OutputStream output = new FileOutputStream(fileName)) {
            properties.store(output, null);
        } catch (FileNotFoundException e) {
            if (createFile(true) && create) {
                try (OutputStream output = new FileOutputStream(fileName)) {
                    properties.store(output, null);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    return false;
                }
                return true;
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}