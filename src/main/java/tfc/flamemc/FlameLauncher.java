package tfc.flamemc;

import com.github.lorenzopapi.Utils;
import com.google.gson.Gson;
import tfc.flame.loader.IFlameLoader;
import tfc.flame.loader.util.JDKLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FlameLauncher {
	private static String dir = System.getProperty("user.dir");
	public static boolean isDev =
			new File(dir + File.separator + "src").exists() &&
					(new File(dir + File.separator + "build").exists() ||
							new File(dir + File.separator + "build.gradle").exists()
					);
	
	public static ArrayList<Class<?>> lockedClasses = new ArrayList<>();
	private static IFlameLoader loader;
	public static final ArrayList<Object> modsList = new ArrayList<>();
	private static final HashMap<File, HashMap<String, byte[]>> classFiles = new HashMap<>();
	public static String[] gameArgs;
	
	public static byte[] getSourceFile(File file, String entryName) {
		return classFiles.get(file.getAbsoluteFile()).get(entryName);
	}
	
	public static File getJarForEntry(String name) {
		for (File file : classFiles.keySet())
			if (classFiles.get(file).containsKey(name))
				return file;
		return null;
	}
	
	public static final TextArea field = new TextArea();
	
	protected static final ArrayList<String> additionalURLs = new ArrayList<>();

	//TODO: I'll leave this here: after client is done, we need to reimplement server
	//TODO: I'm supposing that the client HAS access to the internet, will need to do some fallback in case this assumption is wrong.
	public static void main(String[] args) {
		FlameConfig.field = field;
		field.append("Starting up FlameMC\n");

		if (isDev) dir = dir + File.separator + "run";
		
		Gson gson = new Gson();
		Utils.MinecraftVersionMeta versionsJSON = gson.fromJson(Utils.readUrl("https://launchermeta.mojang.com/mc/game/version_manifest.json"), Utils.MinecraftVersionMeta.class);
		if (args.length == 0) System.out.println("WARN: No args found, defaulting to version " + versionsJSON.latest.release + ".");
		
		ArrayList<String> arguments = new ArrayList<>(Arrays.asList(args));
		String version = arguments.contains("--version") ? arguments.get(arguments.indexOf("--version") + 1) : versionsJSON.latest.release;
		Utils.MinecraftVersion versionJSON = null;
		for (Utils.MinecraftVersionMeta.Version v : versionsJSON.versions)
			if (Objects.equals(v.id, version))
				versionJSON = gson.fromJson(Utils.readUrl(v.url), Utils.MinecraftVersion.class);
		if (versionJSON == null) throw new RuntimeException("WHAT?HOW?WHY?");
		
		String gameDir = arguments.contains("--gameDir") ? arguments.get(arguments.indexOf("--gameDir") + 1) : dir;
		String main_class = arguments.contains("--main_class") ? arguments.get(arguments.indexOf("--main_class") + 1) : versionJSON.mainClass;
		arguments.clear();
		
		//TODO: instead of adding them all in such a messy way, we should try using the versionJSON.arguments field
		arguments.addAll(Arrays.asList("--assetIndex", versionJSON.assetIndex.id, "--main_class", main_class, "--gameDir", dir, "--version", version, "--username", "FlameDev", "--assetsDir", Utils.findMCDir() + File.separator + "assets" + File.separator, "--accessToken", "PLEASE FLAME WORK I BEG YOU", "--uuid", UUID.randomUUID().toString(), "--userType", "mojang", "--versionType", versionJSON.type));
		
		System.out.println("Args that will be used: " + arguments);
		
		File flame_config = new File(gameDir + File.separator + "flame_config" + File.separator + "tfc.flamemc.txt");
		boolean log = false;
		boolean save_log = true;
		try {
			if (!flame_config.exists()) {
				flame_config.getParentFile().mkdirs();
				flame_config.createNewFile();
				FileWriter writer = new FileWriter(flame_config);
				writer.write("log_window:false\nsave_log:true\n");
				writer.close();
			} else {
				Scanner sc = new Scanner(flame_config);
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
			frame.add(field);
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
			//TODO add the locked classes (I guess?)
			
			ArrayList<String> mods = new ArrayList<>();
			File modsFolder = new File(gameDir + File.separator + "flame_mods");
			modsFolder.mkdirs();
			for (File modFile : Objects.requireNonNull(modsFolder.listFiles())) {
				mods.add(modFile.getPath());
				readZip(modFile);
			}
			
			URL[] urls = new URL[mods.size() + 1 + additionalURLs.size()];
			urls[0] = new File(dir + File.separator + "versions" + File.separator + version + File.separator + version + ".jar").toURL();
			for (int i = 0; i < mods.size(); i++) {
				String s = mods.get(i);
				urls[i + 1] = new File(s).toURL();
			}
			for (int i = 0; i < additionalURLs.size(); i++) {
				String s = additionalURLs.get(i);
				urls[i + mods.size() + 1] = new File(s).toURL();
			}

			loader = JDKLoader.createLoader(urls);
			
//			if (isServer) {
//				loader.addURL(new URL("file:\\" + dir + File.separator + "server.jar"));
//			} else {
			HashMap<String, Boolean> depMap = new HashMap<>();
			String os = System.getProperty("os.name").toLowerCase();
			String download;
			String end;
			if (os.contains("win")) {
				download = "windows";
				end = "dll";
			} else if (os.contains("mac")) {
				download = "macos";
				end = "dynlib";
			} else {
				download = "linux";
				end = "so";
			}
			//TODO: again, instead of hardcoding these things we should retrieve them from the versionJSON
			depMap.put("https://libraries.minecraft.net/org/lwjgl/lwjgl/3.2.2/lwjgl-3.2.2-natives-" + download + ".jar", true);
			depMap.put("https://libraries.minecraft.net/org/lwjgl/lwjgl-glfw/3.2.2/lwjgl-glfw-3.2.2-natives-" + download + ".jar", true);
			depMap.put("https://libraries.minecraft.net/org/lwjgl/lwjgl-opengl/3.2.2/lwjgl-opengl-3.2.2-natives-" + download + ".jar", true);
			depMap.put("https://libraries.minecraft.net/org/lwjgl/lwjgl-stb/3.2.2/lwjgl-stb-3.2.2-natives-" + download + ".jar", true);
			depMap.put("https://libraries.minecraft.net/org/lwjgl/lwjgl-openal/3.2.2/lwjgl-openal-3.2.2-natives-" + download + ".jar", true);
			depMap.put("https://libraries.minecraft.net/com/mojang/brigadier/1.0.17/brigadier-1.0.17.jar", false);
			depMap.put("https://libraries.minecraft.net/com/mojang/datafixerupper/2.0.24/datafixerupper-2.0.24.jar", false);
			depMap.forEach((dep, unzip) -> {
				String fileName = dep.substring(dep.lastIndexOf("/") + 1);
				downloadDepJustURL(dep);
				if (unzip) {
					try {
						Utils.unzip(System.getProperty("user.dir") + File.separator + "libraries" + File.separator, System.getProperty("user.dir") + File.separator + "libraries" + File.separator + fileName, (n) -> n.endsWith("." + end));
						Files.delete(Paths.get(System.getProperty("user.dir") + File.separator + "libraries" + File.separator + fileName));
					} catch (Throwable ignored) {}
				}
			});

			//}

			field.append("Locking FlameMC classes\n");
			lockedClasses.forEach(c -> {
				field.append(c.getName() + '\n');
				try {
					loader.loadClass(c.getName(), false);
				} catch (ClassNotFoundException ignored) {
				}
			});
			field.append("Locked FlameMC classes\n");

			try {
				for (String s : mods) {
					File fi1 = new File(s);
					try {
						Object mod = loader.loadClass("entries." + fi1.getName().split("-")[0].replace("-", "").replace(".zip", "").replace(".jar", "") + ".Main", false).newInstance();
						modsList.add(mod);
					} catch (Throwable err) {
						FlameConfig.logError(err);
					}
				}
			} catch (Throwable err) {
				FlameConfig.logError(err);
			}
			gameArgs = new String[arguments.size()];
			arguments.toArray(gameArgs);
			Class<?> clazz = loader.loadClass("tfc.flamemc.ModInitalizer", false);
			clazz.newInstance();
			if (version.contains("fabric")) System.setProperty("fabric.gameJarPath", dir + File.separator + "versions" + File.separator + version + File.separator + version + ".jar");
			loader
					.loadClass(main_class, true)
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
		else {
			if (save_log) {
				try {
					File fi = new File(dir + File.separator + "flame_logs" + File.separator + version);
					if (!fi.exists()) fi.mkdirs();
					fi = new File(dir + File.separator + "flame_logs" + File.separator + version + File.separator + System.nanoTime() + ".txt");
					fi.createNewFile();
					FileWriter writer = new FileWriter(fi);
					writer.write(field.getText());
					writer.close();
				} catch (Throwable ignored) {
				}
			}
		}
		if (err != null) throw new RuntimeException(err);
	}

	public static void readZip(File f) {
		try {
			ZipFile fileZip = new ZipFile(f);
			Stream<ZipEntry> entryStream = (Stream<ZipEntry>) fileZip.stream();
			HashMap<String, byte[]> entryBytes = new HashMap<>();
			classFiles.put(f.getAbsoluteFile(), entryBytes);
			entryStream.forEach((entry) -> {
				try {
					if (entry.isDirectory()) return;
					InputStream stream = fileZip.getInputStream(entry);
					ByteArrayOutputStream outStream = new ByteArrayOutputStream();
					int b;
					while ((b = stream.read()) != -1) outStream.write(b);
					entryBytes.put(entry.toString(), outStream.toByteArray());
					stream.close();
					outStream.flush();
					outStream.close();
				} catch (Throwable err) {
					err.printStackTrace();
				}
			});
			fileZip.close();
		} catch (Throwable err) {
			err.printStackTrace();
		}
	}
	
	public static void downloadDepJustURL(String url) {
		downloadDep(url, url.substring(url.lastIndexOf("/") + 1));
	}
	
	public static void downloadDep(String url, String name) {
		//TODO: deps
		//dependencyManager.addFromURL("libraries" + File.separator + name + "," + url);
		File file = new File("libraries" + File.separator + name);
		readZip(file);
	}
}
