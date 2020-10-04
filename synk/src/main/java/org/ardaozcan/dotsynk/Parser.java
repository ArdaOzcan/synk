package org.ardaozcan.dotsynk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Parser {
    BufferedReader reader;

    public Parser(String filePath) throws FileNotFoundException {
        reader = new BufferedReader(new FileReader(new File(filePath)));
    }

    public Config parse() throws IOException {
        Config conf = new Config();
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            String key = line.split(":")[0];
            String value = line.split(":")[1];
            switch (key) {
                case "code":
                    conf.code = value;
                    break;
            }
        }

        return conf;
    }
}
