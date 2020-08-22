package com.github.lorenzopapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

	public static File findVersionsDir() {
		String home = System.getProperty("user.home", ".");
		String os = System.getProperty("os.name").toLowerCase();
		File dir;
		File homeDir = new File(home);

		if (os.contains("win") && System.getenv("APPDATA") != null) {
			dir = new File(System.getenv("APPDATA"), ".minecraft" + File.separator + "versions");
		} else if (os.contains("mac")) {
			dir = new File(homeDir, "Library" + File.separator + "Application Support" + File.separator + "minecraft" + File.separator + "versions");
		} else {
			dir = new File(homeDir, ".minecraft" + File.separator + "versions");
		}
		return dir;
	}

	public static void downloadFromUrl(String url, String downloadFile) throws IOException {
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

	public void readJson(Path jsonPath) throws IOException {
		JsonParser parser = new JsonParser();
		JsonElement tree = parser.parse(Files.newBufferedReader(jsonPath));
		JsonObject jsonObject = tree.getAsJsonObject();
		for (Map.Entry<String, JsonElement> jsonEntry : jsonObject.entrySet()) {
			JsonElement entryValue = jsonEntry.getValue();
			String entryKey = jsonEntry.getKey();
			/*if (entryValue.isJsonObject() && entryKey.equals("entryValue")) {
				for (Map.Entry<String, JsonElement> downloadEntry : jsonObject.entrySet()) {

				}
				System.out.println(entryValue.toString());
							for (Map.Entry<String, JsonElement> entryObject : entryValue.getAsJsonObject().entrySet()) {

							}
				//System.out.println(entryValue.toString());
			}*/
		}
	}
}

