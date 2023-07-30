package tfc.flamemc;

import org.json.JSONObject;
import tfc.flame.loader.IFlameLoader;
import tfc.flame.loader.util.JDKLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.*;

public class FlameLauncher {
	private static IFlameLoader loader;
	public static final ArrayList<Object> modsList = new ArrayList<>();
	public static String[] gameArgs;
	
	protected static final ArrayList<URL> additionalURLs = new ArrayList<>();

	//TODO: I'll leave this here: after client is done, we need to reimplement server
	public static void main(String[] args) throws IOException {
		FlameConfig.field = new TextArea();
		FlameConfig.println("Starting up FlameMC");
		boolean hasConnection = FlameUtils.hasInternetConnection();
		
		JSONObject versionsJSON = hasConnection ? new JSONObject(FlameUtils.readUrl("https://launchermeta.mojang.com/mc/game/version_manifest.json")) : null;
		
		String gameDir = FlameUtils.keyOrDefault(args, "--gameDir", FlameUtils.findRunDir());
		String version = FlameUtils.keyOrDefault(args, "--version", hasConnection ? versionsJSON.getJSONObject("latest").getString("release") : "1.20.1");
		if (args.length == 0) FlameConfig.println("WARN: No args found, defaulting to version " + version);
		
		File versionJSONFile = new File(FlameUtils.findVersionsDir(), version + File.separator + version + ".json");
		if (!FlameUtils.hasInternetConnection() && !versionJSONFile.exists()) throw new RuntimeException("JSON doesn't exist and we cannot download it. Game cannot start.\nPlease enable your internet connection or place a JSON file in the version directory.");
		
		JSONObject versionJSON = versionJSONFile.exists() ? new JSONObject(Files.readAllBytes(versionJSONFile.toPath())) : null;
		if (versionJSON == null && versionsJSON != null)
			for (Object v : versionsJSON.getJSONArray("versions"))
				if (((JSONObject) v).getString("id").equals(version.replace("-flame", "")))
					versionJSON = new JSONObject(FlameUtils.readUrl(((JSONObject) v).getString("url")));
		if (versionJSON == null) throw new RuntimeException("Version " + version + " not found. Please check if it exists.");
		
		String mainClass = FlameUtils.keyOrDefault(args, "--main_class", versionJSON.getString("mainClass"));
		
		//TODO: some conditional arguments aren't captured (I hate "rules"), but I wouldn't worry about it now
		List<String> versionArgs = versionJSON.has("minecraftArguments") ? new ArrayList<>(Arrays.asList(versionJSON.getString("minecraftArguments").split(" "))) : new ArrayList<>();
		if (versionArgs.isEmpty())
			for (Object o : versionJSON.getJSONObject("arguments").getJSONArray("game"))
				if (o instanceof String) versionArgs.add((String) o);
		
		String stringArgs = String.join(" ", versionArgs)
				                    .replace("$", "")
				                    .replace("{auth_player_name}", FlameUtils.keyOrDefault(args, "--username", "FlameDev"))
				                    .replace("{version_name}", version)
				                    .replace("{game_directory}", gameDir)
				                    .replaceAll("\\{assets_root}|\\{game_assets}", FlameUtils.keyOrDefault(args, "--assetsDir", FlameUtils.findMCDir() + File.separator + "assets"))
				                    .replace("{assets_index_name}", FlameUtils.keyOrDefault(args, "--assetIndex", versionJSON.getJSONObject("assetIndex").getString("id")))
				                    .replace("{auth_uuid}", FlameUtils.keyOrDefault(args, "--uuid", UUID.randomUUID().toString()))
				                    .replaceAll("\\{auth_access_token}|\\{auth_session}", FlameUtils.keyOrDefault(args, "--accessToken", "PLEASE-FLAME-WORK-I-BEG-YOU"))
									//TODO: keyOrDefault for these arguments too?
				                    .replace("{user_type}", "mojang")
				                    .replace("{version_type}", versionJSON.getString("type"));
		gameArgs = stringArgs.split(" ");
		
		File flameConfig = new File(gameDir + File.separator + "flame_config" + File.separator + "tfc.flamemc.txt");
		boolean log = false;
		boolean save_log = true;
		try {
			if (!flameConfig.exists()) {
				flameConfig.getParentFile().mkdirs();
				flameConfig.createNewFile();
				FileWriter writer = new FileWriter(flameConfig);
				writer.write("log_window:false\nsave_log:true\n");
				writer.close();
			} else {
				Scanner sc = new Scanner(flameConfig);
				while (sc.hasNextLine()) {
					String line = sc.nextLine().toLowerCase();
					if (line.startsWith("log_window:")) log = Boolean.parseBoolean(line.replace("log_window:", ""));
					else if (line.startsWith("save_log:")) save_log = Boolean.parseBoolean(line.replace("save_log:", ""));
				}
				sc.close();
			}
		} catch (Throwable err) {
			FlameConfig.logError(err);
		}
		
		JFrame frame = log ? new JFrame("Flame MC log: " + version) : null;
		if (log) {
			frame.add(FlameConfig.field);
			boolean finalSave_log = save_log;
			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					exit(frame, null, finalSave_log, gameDir, version);
				}
			});
			frame.setSize(1000, 1000);
			frame.setVisible(true);
		}
		
		try {
			String os = System.getProperty("os.name").toLowerCase();
			String download;
			String end;
			if (os.contains("win")) {
				download = "windows";
				end = "dll";
			} else if (os.contains("mac")) {
				download = "osx"; //TODO: this sometimes becomes macos (I hate everything)
				end = "dynlib";
			} else {
				download = "linux";
				end = "so";
			}
			
			File modsFolder = new File(gameDir + File.separator + "flame_mods");
			modsFolder.mkdirs();
			ArrayList<File> mods = new ArrayList<>(Arrays.asList(Objects.requireNonNull(modsFolder.listFiles())));
			
			//TODO: put libraries in their own folder? (Instead of putting them all in the one big libraries folder?)
			HashMap<String, Boolean> depMap = new HashMap<>();
			for (Object l : versionJSON.getJSONArray("libraries")) {
				if (l instanceof JSONObject) {
					JSONObject library = (JSONObject) l;
					boolean hasRules = library.has("rules");
					boolean forThisOS = true;
					if (hasRules) {
						for (Object r : library.getJSONArray("rules")) {
							JSONObject rule = (JSONObject) r;
							if (rule.has("action") && rule.has("os")) {
								boolean A = rule.getString("action").equals("allow");
								boolean B = !rule.getJSONObject("os").has("name") || rule.getJSONObject("os").getString("name").equals(download);
								boolean C = !rule.getJSONObject("os").has("version") || rule.getJSONObject("os").getString("name").equals(System.getProperty("os.version"));
								forThisOS = A == (B && C);
							}
						}
					}
					if (forThisOS) {
						FlameConfig.println("Found library " + library.getString("name"));
						JSONObject downloads = library.getJSONObject("downloads");
						if (library.has("natives")) depMap.put(downloads.getJSONObject("classifiers").getJSONObject(library.getJSONObject("natives").getString(download)).getString("url"), true);
						else if (downloads.has("artifact")) depMap.put(downloads.getJSONObject("artifact").getString("url"), false);
					}
				}
			}
			
			List<URL> urlsList = new ArrayList<>();
			urlsList.add(new URL("jar:file:" + new File(FlameUtils.findVersionsDir(), version + File.separator + version + ".jar").getPath() + "!/"));
			
			String librariesFolder = gameDir + File.separator + "libraries" + File.separator + version;
			depMap.forEach((dep, unzip) -> {
				try {
					downloadDependencyJustUrl(dep, librariesFolder);
					File lib = new File(librariesFolder, dep.substring(dep.lastIndexOf("/") + 1));
					if (lib.exists() && unzip) {
						FlameUtils.unzip(librariesFolder, lib.getAbsolutePath(), (n) -> n.endsWith("." + end));
						Files.delete(lib.toPath());
						additionalURLs.remove(new URL("jar:file:" + lib.getPath() + "!/"));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			
			for (File s : mods) urlsList.add(new URL("jar:file:" + s.getPath() + "!/"));
			urlsList.addAll(additionalURLs);
			
			// TODO: I love java hacks, but I think there is a better way to do this
			{
				System.setProperty("java.library.path", System.getProperty("java.library.path") + ":" + librariesFolder);
				Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
				fieldSysPath.setAccessible(true);
				fieldSysPath.set(null, null);
			}
			
			System.setProperty("org.lwjgl.librarypath", new File(librariesFolder).getAbsolutePath());
			URL[] urls = new URL[urlsList.size()];
			urlsList.toArray(urls);
			loader = JDKLoader.createLoader(urls, FlameLauncher.class.getClassLoader(), true);
			Thread.currentThread().setContextClassLoader((ClassLoader) loader);
	
			FlameConfig.println(String.format("Found %d mods", mods.size()));
			for (File s : mods) {
				String name = s.getName().split("-")[0].replace("-", "").replace(".zip", "").replace(".jar", "");
				modsList.add(loader.loadClass("entries." + name + ".Main", false).newInstance());
				FlameConfig.println("Loading " + name);
			}
			
			FlameConfig.println("Urls used:" + urlsList);
			FlameConfig.println("Args that will be used: " + stringArgs);
			FlameConfig.println("Initializing mods");
			Class<?> clazz = loader.loadClass("tfc.flamemc.ModInitializer", false);
			clazz.newInstance();
			if (version.contains("fabric")) System.setProperty("fabric.gameJarPath", FlameUtils.findVersionsDir() + File.separator + version.replace("-flame", "") + File.separator + version.replace("-flame", "") + ".jar");
			loader
					.loadClass(mainClass, true)
					.getMethod("main", String[].class)
					.invoke(null, (Object) gameArgs);
		} catch (Throwable err) {
			FlameConfig.logError(err);
			exit(frame, err, save_log, gameDir, version);
		}
		
		exit(frame, null, save_log, gameDir, version);
	}
	
	private static void exit(JFrame f, Throwable err, boolean save_log, String dir, String version) {
		if (f != null) f.dispose();
		if (save_log) {
			try {
				File log = new File(dir + File.separator + "flame_logs" + File.separator + version + File.separator + FlameConfig.nanoTime + ".txt");
				if (!log.exists()) {
					log.getParentFile().mkdirs();
					log.createNewFile();
					FileWriter writer = new FileWriter(log);
					writer.write(FlameConfig.field.getText());
					writer.close();
				}
			} catch (Throwable ignored) {
			}
		}
		
		if (err != null) throw new RuntimeException(err);
	}
	
	public static void downloadDependencyJustUrl(String url, String path) {
		downloadDependency(url, path, url.substring(url.lastIndexOf("/") + 1));
	}
	
	public static void downloadDependency(String url, String path, String fileName) {
		try {
			File lib = new File(path, fileName);
			if (!lib.exists()) FlameUtils.downloadFromUrl(url, lib.getAbsolutePath());
			additionalURLs.add(new URL("jar:file:" + lib.getPath() + "!/"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
