package org.ardaozcan.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    public static List<File> getFilesInDirectory(final File directoryPath) {
        List<File> files = new ArrayList<File>();
        for (final File fileEntry : directoryPath.listFiles()) {
            if (fileEntry.isDirectory()) {
                getFilesInDirectory(fileEntry);
            } else {
                files.add(fileEntry);
            }
        }

        return files;
    }
    
}
