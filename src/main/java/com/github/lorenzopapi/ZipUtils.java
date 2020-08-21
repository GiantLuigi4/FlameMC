package com.github.lorenzopapi;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipUtils {

	public void unzip(String targetDir, String zipFilename, Function<String, Boolean> fileV) {
		Path targetDirPath = Paths.get(targetDir);
		try (ZipFile zipFile = new ZipFile(zipFilename)) {
			zipFile.stream()
					.parallel() // enable multi-threading
					.forEach(e -> unzipEntry(zipFile, e, targetDirPath, fileV));
		} catch (IOException e) {
			throw new RuntimeException("Error opening zip file '" + zipFilename + "': " + e, e);
		}
	}

	private static void unzipEntry(ZipFile zipFile, ZipEntry entry, Path targetDir, Function<String, Boolean> fileV) {
		try {
			Path targetPath = targetDir.resolve(Paths.get(entry.getName()));
			if (fileV.apply(entry.getName())) {
				if (Files.isDirectory(targetPath)) {
					Files.createDirectories(targetPath);
				} else {
					Files.createDirectories(targetPath.getParent());
					try (InputStream in = zipFile.getInputStream(entry)) {
						Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Error processing zip entry '" + entry.getName() + "': " + e, e);
		}
	}
}

