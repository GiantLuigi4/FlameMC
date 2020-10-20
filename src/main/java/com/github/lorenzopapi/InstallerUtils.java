package com.github.lorenzopapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tfc.flamemc.FlameLauncher;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class InstallerUtils {

	public static void unzip(String targetDir, String zipFilename, Function<String, Boolean> fileV) {
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

	public static String findMCDir(boolean isDev) {
		String home = System.getProperty("user.home", ".");
		String os = System.getProperty("os.name").toLowerCase();
		String dir;
		if (!isDev) {
			if (os.contains("win") && System.getenv("APPDATA") != null) {
				dir = System.getenv("APPDATA") + File.separator + ".minecraft";
			} else if (os.contains("mac")) {
				dir = home + "Library" + File.separator + "Application Support" + File.separator + "minecraft";
			} else {
				dir = home + ".minecraft";
			}
		} else {
			dir = FlameLauncher.getDir()+ "\\run";
		}
		return dir;
	}

	public static File findVersionsDir() {
		File dir;
		dir = new File(findMCDir(FlameLauncher.isDev) + File.separator + "versions");
		if (!dir.exists())
			dir.mkdirs();
		return dir;
	}

	public static void downloadFromUrl(String url, String downloadFile) throws IOException {
		File testFile = new File(downloadFile);
		if (!testFile.exists()) {
			testFile.getParentFile().mkdirs();
			testFile.createNewFile();
		}
		try (BufferedInputStream inputStream = new BufferedInputStream(new URL(url).openStream());
			FileOutputStream fileOS = new FileOutputStream(downloadFile)) {
			byte[] data = new byte[1024];
			int byteContent;
			while ((byteContent = inputStream.read(data, 0, 1024)) != -1) {
				fileOS.write(data, 0, byteContent);
			}
		}
	}

	public static String readUrl(String urlString) throws Exception {
		BufferedReader reader = null;
		try {
			URL url = new URL(urlString);
			reader = new BufferedReader(new InputStreamReader(url.openStream()));
			StringBuffer buffer = new StringBuffer();
			int read;
			char[] chars = new char[1024];
			while ((read = reader.read(chars)) != -1)
				buffer.append(chars, 0, read);

			return buffer.toString();
		} finally {
			if (reader != null)
				reader.close();
		}
	}

	public static JsonObject readJsonObject(JsonObject object, Function<String, Boolean> validator) {
		for (Map.Entry<String, JsonElement> jsonEntry : object.entrySet()) {
			if (validator.apply(jsonEntry.getKey())) {
				return jsonEntry.getValue().getAsJsonObject();
			}
		}
		return null;
	}
}

