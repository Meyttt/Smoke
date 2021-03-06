package utils;

import java.io.*;
import java.util.Properties;

public class Config {
    public Properties prop;

    public Config(String filename) throws IOException {
        this.prop = initProperties(filename);
    }

    public String get(String key)  {
        String val = prop.getProperty(key);
        try {
            return new String(val.getBytes("ISO-8859-1"), "windows-1251");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return val;
    }

    private Properties initProperties(String filename) throws IOException {
        Properties p = new Properties();
        InputStream inputStream = new FileInputStream(filename);
        if (inputStream != null) {
            p.load(inputStream);
        } else {
            throw new FileNotFoundException("utils.Config file not found: " + filename);
        }
        return p;
    }
}