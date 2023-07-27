package com.github.lorenzopapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Utils {
	public static String readUrl(String urlString) {
	    try {
	        BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(urlString).openStream()));
	        StringBuilder buffer = new StringBuilder();
	        int read;
	        char[] chars = new char[1024];
	        while ((read = reader.read(chars)) != -1)
	            buffer.append(chars, 0, read);
	        return buffer.toString();
	    } catch (Throwable err) {
	        err.printStackTrace();
	    }
	    throw new RuntimeException("bad url");
	}
	
	public static void unzip(String targetDir, String zipFilename, Function<String, Boolean> fileV) {
	    Path targetDirPath = Paths.get(targetDir);
	    try (ZipFile zipFile = new ZipFile(zipFilename)) {
	        zipFile.stream()
	                .parallel()
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
	
	public static void downloadFromUrl(String url, String downloadFile) throws IOException {
	    File f = new File(downloadFile);
	    if (!f.exists()) {
	        f.getParentFile().mkdirs();
	        f.createNewFile();
	    }
	    try (BufferedInputStream inputStream = new BufferedInputStream(new URL(url).openStream())) {
	        FileOutputStream fileOS = new FileOutputStream(downloadFile);
	        byte[] data = new byte[inputStream.available()];
	        int byteContent;
	        while ((byteContent = inputStream.read(data, 0, 1024)) != -1) {
	            fileOS.write(data, 0, byteContent);
	        }
	        fileOS.close();
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
	
	public static void deleteDirectory(File f) throws IOException {
	    if (f.exists())
	        Files.walk(f.toPath())
	            .sorted(Comparator.reverseOrder())
	            .map(Path::toFile)
	            .forEach(File::delete);
	}
	
	public static String findMCDir() {
		String home = System.getProperty("user.home", ".");
		String os = System.getProperty("os.name").toLowerCase();
		String mcDir;
		if (os.contains("win") && System.getenv("APPDATA") != null) {
			mcDir = System.getenv("APPDATA") + File.separator + ".minecraft";
		} else if (os.contains("mac")) {
			mcDir = home + File.separator + "Library" + File.separator + "Application Support" + File.separator + "minecraft";
		} else {
			mcDir = home + File.separator + ".minecraft";
		}
		return mcDir;
	}
	
	public static class MinecraftVersionMeta {
		public List<Version> versions;
		
		public static class Version {
	        public String id;
	        public String url;
	    }
	}
	
	public static class FlamedJson {
	    public String id;
	    public String inheritsFrom;
	    public String type = "release";
	    public String mainClass;
	    public Utils.Arguments arguments = new Utils.Arguments();
	    public Utils.Downloads downloads = new Utils.Downloads();
	    public List<Utils.Library> libraries = new ArrayList<>();
	
	    public FlamedJson(String id, String inheritsFrom, String mainClass) {
	        this.id = id;
	        this.inheritsFrom = inheritsFrom;
	        this.mainClass = mainClass;
	    }
	}
	
	public static class Library {
	    public String name;
	    public String url;
	
	    public Library(String name, String url) {
	        this.name = name;
	        this.url = url;
	    }
	}
	
	public static class Arguments {
	    public List<String> game = new ArrayList<>();
	}
	
	public static class Downloads {}
}
