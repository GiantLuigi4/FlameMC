package tfc.flamemc;

import java.io.*;
import java.lang.reflect.Array;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FlameUtils {
	public static boolean isDev = new File("src").exists() && (new File("build").exists() || new File("build.gradle").exists());
	public static String readUrl(String urlString) {
	    try {
	        BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(urlString).openStream()));
	        StringBuilder buffer = new StringBuilder();
	        int read;
	        char[] chars = new char[1024];
	        while ((read = reader.read(chars)) != -1) buffer.append(chars, 0, read);
	        return buffer.toString();
	    } catch (Throwable err) {
	        err.printStackTrace();
	    }
	    throw new RuntimeException("Malformed URL " + urlString);
	}
	
	public static File findVersionsDir() {
		return new File((isDev ? System.getProperty("user.dir") + File.separator + "run" : FlameUtils.findMCDir()) + File.separator + "versions");
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
	        while ((byteContent = inputStream.read(data, 0, 1024)) != -1) fileOS.write(data, 0, byteContent);
	        fileOS.close();
	    }
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
		if (os.contains("win") && System.getenv("APPDATA") != null) mcDir = System.getenv("APPDATA") + File.separator + ".minecraft";
		else if (os.contains("mac")) mcDir = home + File.separator + "Library" + File.separator + "Application Support" + File.separator + "minecraft";
		else mcDir = home + File.separator + ".minecraft";
		
		return mcDir;
	}
	
	public static String findRunDir() {
		if (isDev) return new File(System.getProperty("user.dir"), "run").getAbsolutePath();
		else return findMCDir();
	}
	
	public static String keyOrDefault(List<String> list, String key, String def) {
		return list.contains(key) ? list.get(list.indexOf(key) + 1) : def;
	}
	
	public static String keyOrDefault(String[] arr, String key, String def) {
		return keyOrDefault(Arrays.asList(arr), key, def);
	}
	
	public static class FlamedJson {
	    public String id;
		public String time;
		public String releaseTime;
		public String type = "release";
		public String mainClass;
	    public String inheritsFrom;
	    public FlameUtils.Arguments arguments = new FlameUtils.Arguments();
	    public FlameUtils.Downloads downloads = new FlameUtils.Downloads();
	    public List<FlameUtils.Library> libraries = new ArrayList<>();
	
	    public FlamedJson(String id, String inheritsFrom, String mainClass) {
	        this.id = id;
	        this.inheritsFrom = inheritsFrom;
	        this.mainClass = mainClass;
			this.releaseTime = OffsetDateTime.now().toString();
			this.time = this.releaseTime;
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
		public List<String> jvm = new ArrayList<>();
	}
	
	public static class Downloads {}
}
