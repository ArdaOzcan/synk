package org.ardaozcan.synk.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.common.hash.Hashing;

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

	public static void initializeDirectory(Path dotSynkPath) throws FileNotFoundException, IOException {
		if (Files.exists(dotSynkPath)) {
			return;
		}

		String code = System.console().readLine("Directory pass: ");
		String serverName = System.console().readLine("Server name: ");
		serverName = serverName.toLowerCase().replace(' ', '-');
		String hashedCode = Hashing.sha256().hashString(code, StandardCharsets.UTF_8).toString();

		try {
			File file = new File(dotSynkPath.toString());
			file.createNewFile();
			FileWriter writer = new FileWriter(file);
			writer.write(String.format("code: %s\n", hashedCode));
			writer.write(String.format("name: %s\n", serverName));
			writer.close();
			Logger.logInfo(String.format("Directory initialized in '%s'.", dotSynkPath));

		} catch (IOException e) {
			Logger.logError("An error occurred.");
			e.printStackTrace();
		}

	}

}
