package tfc.flamemc;

import tfc.flame.FlameConfig;
import tfc.flame.FlameLog;
import tfc.flame.FlameURLLoader;
import tfc.utils.flame.FlameLoader;
import tfc.utils.flame.dependency_management.Manager;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.CodeSource;
import java.util.*;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
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
	
//	public static boolean isServer = false;
	
	public static ArrayList<Class<?>> lockedClasses = new ArrayList<>();
	
	private static FlameURLLoader loader;
	public static FlameLoader loader1;
	public static Manager dependencyManager;
	
	public static final ArrayList<Object> mods_list = new ArrayList<>();
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
	
	public static FlameURLLoader getLoader() {
		return loader;
	}
	
	public static String getDir() {
		return dir;
	}
	
	public static final FlameLog field = new FlameLog();
	
	protected static final ArrayList<String> additionalURLs = new ArrayList<>();

	//TODO: I'll leave this here: after client is done, we need to reimplement server
	public static void main(String[] args) {
		FlameConfig.field = field;
		field.append("Starting up FlameMC\n");
		
		if (isDev) dir = dir + File.separator + "run";
		
		List<String> argsLists = Arrays.asList(args);
//		if (argsLists.contains("--serverDev")) {
//			dir = System.getProperty("user.dir") + File.separator + "server" + File.separator;
//			isServer = true;
//		}
		
		JFrame frame = null;
		
		String version = "1.16.5";
		String gameDir = dir;
		String main_class = null;

		String[] globalArgs = new String[]{
				"--gameDir", gameDir, "--username", "FlameDev", "--assetsDir", findMCDir() + File.separator + "assets" + File.separator, "--accessToken", "PLEASE FLAME WORK I BEG YOU", "--uuid", UUID.randomUUID().toString(), "--userType", "mojang", "--versionType", "release"
		};
		
		List<String> actualArgs = new ArrayList<>(Arrays.asList(globalArgs));

		if (args.length == 0) {
			System.out.println("WARN: No args found, defaulting to version " + version + ".");
			actualArgs.add("--version");
			actualArgs.add(version);
		} else {
			System.out.println("Found some args: " + Arrays.toString(args) + ".");
			
			if (argsLists.contains("--version")) version = argsLists.get(argsLists.indexOf("--version") + 1);
			if (argsLists.contains("--gameDir")) gameDir = argsLists.get(argsLists.indexOf("--gameDir") + 1);
			if (argsLists.contains("--main_class")) {
				main_class = argsLists.get(argsLists.indexOf("--main_class") + 1);
//				if (main_class.contains("MinecraftServer")) isServer = true;
			}
			
//			if (isServer) {
//				argsLists.remove("--serverDev");
//				argsLists.remove("--main_class");
//				argsLists.remove(argsLists.indexOf("--main_class") + 1);
//			}
			actualArgs.addAll(argsLists);
		}
		if (!actualArgs.contains("--assetIndex")) {
			actualArgs.add("--assetIndex");
			if (version.indexOf(".") != version.lastIndexOf(".")) {
				actualArgs.add(version.substring(0, version.lastIndexOf(".")));
			} else {
				actualArgs.add(version.substring(0, version.indexOf("-")));
			}
		}
		
		System.out.println("Args that will be used: " + Arrays.toString(actualArgs.toArray()));
		
		File flame_config = new File(gameDir + File.separator + "flame_config" + File.separator + "tfc.flamemc.txt");
		boolean log = false;
		boolean save_log = true;
		if (!flame_config.exists()) {
			try {
				flame_config.getParentFile().mkdirs();
				flame_config.createNewFile();
				FileWriter writer = new FileWriter(flame_config);
				writer.write("log_window:false\n");
				writer.write("save_log:true\n");
				writer.write("log_bytecode:false\n");
				writer.write("log_class_names:false\n");
				writer.close();
			} catch (Throwable err) {
				FlameConfig.logError(err);
			}
		} else {
			try {
				Scanner sc = new Scanner(flame_config);
				while (sc.hasNextLine()) {
					String source_line = sc.nextLine();
					String line = source_line.toLowerCase();
					if (line.startsWith("log_window:")) {
						log = Boolean.parseBoolean(line.replace("log_window:", ""));
					} else if (line.startsWith("save_log:")) {
						save_log = Boolean.parseBoolean(line.replace("save_log:", ""));
					} else if (line.startsWith("log_bytecode:")) {
						FlameConfig.log_bytecode = Boolean.parseBoolean(line.replace("log_bytecode:", ""));
					} else if (line.startsWith("log_class_names:")) {
						FlameConfig.log_classnames = Boolean.parseBoolean(line.replace("log_class_names:", ""));
					}
				}
				sc.close();
			} catch (Throwable err) {
				FlameConfig.logError(err);
			}
		}
		
		if (main_class == null) {
			File version_config = new File(dir + File.separator + "versions" + File.separator + version + File.separator + "options.txt");
			if (!version_config.exists()) {
				try {
					version_config.getParentFile().mkdirs();
					version_config.createNewFile();
					FileWriter writer = new FileWriter(version_config);
					writer.write("main_class:net.minecraft.client.main.Main");
					writer.close();
				} catch (Throwable err) {
					FlameConfig.logError(err);
				}
				main_class = "net.minecraft.client.main.Main";
			} else {
				try {
					Scanner sc = new Scanner(version_config);
					while (sc.hasNextLine()) {
						String source_line = sc.nextLine();
						String line = source_line.toLowerCase();
						if (line.startsWith("main_class:")) {
							main_class = source_line.replace("main_class:", "");
						}
					}
					sc.close();
				} catch (Throwable err) {
					FlameConfig.logError(err);
				}
			}
		}

		if (log) {
			frame = new JFrame("Flame MC log: " + version);
			frame.add(field);
			String finalVersion = version;
			boolean finalSave_log = save_log;
			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					exit(null, finalSave_log, dir, finalVersion);
				}
			});
			frame.setSize(1000, 1000);
			frame.setVisible(true);
		}
		
		try {
			lockedClasses.add(Class.forName("tfc.flamemc.FlameLauncher"));
			lockedClasses.add(Class.forName("tfc.flame.IFlameMod"));
			lockedClasses.add(Class.forName("tfc.flame.FlameConfig"));
			lockedClasses.add(Class.forName("tfc.flame.FlameURLLoader"));
			lockedClasses.add(Class.forName("tfc.flame.IFlameAPIMod"));

			ArrayList<String> mods = new ArrayList<>();
			File modsFolder = new File(gameDir + File.separator + "flame_mods");
			modsFolder.mkdirs();
			for (File modFile : Objects.requireNonNull(modsFolder.listFiles())) {
				mods.add(modFile.getPath());
				readZip(modFile);
			}


			URL[] urls = new URL[mods.size() + 1 + additionalURLs.size()];
			urls[0] = new File(dir + File.separator + "versions" + File.separator + version + File.separator + "" + version + ".jar").toURL();
			for (int i = 0; i < mods.size(); i++) {
				String s = mods.get(i);
				urls[i + 1] = new File(s).toURL();
			}
			for (int i = 0; i < additionalURLs.size(); i++) {
				String s = additionalURLs.get(i);
				urls[i + mods.size() + 1] = new File(s).toURL();
			}

			loader = new FlameURLLoader(urls, FlameLauncher.class.getClassLoader());
			loader.classDefiner = (name, bytes) -> {
				name = name.replace(".", "/") + ".class";
				File f = getJarForEntry(name);
				CodeSource source = null;
				Manifest mf = null;
				if (f == null) {
					try {
						// TODO: this
						URL url = loader.getParent().getResource(name);
						System.out.println(url.getFile());
					} catch (Throwable ignored) {
					}
				} else {
					try {
						JarFile file = new JarFile(f);
						JarEntry entry = file.getJarEntry(name);
						source = new CodeSource(f.toURL(), entry.getCodeSigners());
						mf = file.getManifest();
						file.close();
					} catch (Throwable ignored) {
					}
				}
				try {
					String pkgname = name.replace(".class", "").replace("/", ".");
					int i = pkgname.lastIndexOf('.');
					pkgname = i > 0 ? pkgname.substring(0, i) : "";
					if (mf != null) {
						if (loader.getPackage(pkgname) == null) {
							loader.definePackage(pkgname, mf, f.toURL());
						}
					}
				} catch (Throwable ignored) {
				}
				if (source != null) return loader.define(name.replace(".class", "").replace("/", "."), bytes, source);
				else return loader.define(name.replace(".class", "").replace("/", "."), bytes);
			};
			loader1 = new FlameLoader(loader);
			dependencyManager = new Manager(loader);

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
						unzip(System.getProperty("user.dir") + File.separator + "libraries" + File.separator, System.getProperty("user.dir") + File.separator + "libraries" + File.separator + fileName, (n) -> n.endsWith("." + end));
						Files.delete(Paths.get(System.getProperty("user.dir") + File.separator + "libraries" + File.separator + fileName));
					} catch (Throwable ignored) {
					}
				}
			});

			//}

			field.append("Locking FlameMC classes\n");
			lockedClasses.forEach(c -> {
				field.append(c.getName() + '\n');
				try {
					loader.load(c.getName(), false);
				} catch (ClassNotFoundException ignored) {
				}
			});
			field.append("Locked FlameMC classes\n");

			try {
				for (String s : mods) {
					File fi1 = new File(s);
					try {
						Object mod = loader.load("entries." + fi1.getName().split("-")[0].replace("-", "").replace(".zip", "").replace(".jar", "") + ".Main", false).newInstance();
						mods_list.add(mod);
					} catch (Throwable err) {
						FlameConfig.logError(err);
					}
				}
			} catch (Throwable err) {
				FlameConfig.logError(err);
			}
			gameArgs = new String[actualArgs.size()];
			actualArgs.toArray(gameArgs);
			Class<?> clazz = loader.load("tfc.flamemc.ModInitalizer", false);
			clazz.newInstance();
			if (version.contains("fabric")) {
				System.setProperty("fabric.gameJarPath", dir + File.separator + "versions" + File.separator + version + File.separator + version + ".jar");
			}
			loader
					.loadClass(main_class)
					.getMethod("main", String[].class)
					.invoke(null, (Object) gameArgs);
		} catch (Throwable err) {
			FlameConfig.logError(err);
			if (frame == null) {
				exit(err, save_log, gameDir, version);
			} else {
				frame.dispose();
			}
			throw new RuntimeException(err);
		}
		
		if (frame == null) {
			exit(null, save_log, gameDir, version);
		} else {
			frame.dispose();
		}
	}
	
	private static void exit(Throwable err, boolean save_log, String dir, String version) {
		if (save_log) {
			try {
				File fi = new File(dir + File.separator + "flame_logs" + File.separator + version);
				if (!fi.exists()) fi.mkdirs();
				fi = new File(dir + File.separator + "flame_logs" + File.separator + version + File.separator + "" + System.nanoTime() + ".txt");
				fi.createNewFile();
				FileWriter writer = new FileWriter(fi);
				writer.write(field.getText());
				writer.close();
			} catch (Throwable ignored) {
			}
		}
		if (err != null) throw new RuntimeException(err);
	}

	public static void addClassReplacement(String clazz) {
		loader.findReplacement(clazz);
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
		} catch (Throwable err) {
			err.printStackTrace();
		}
	}

	public static void downloadDepJustURL(String url) {
		String name = url.substring(url.lastIndexOf("/") + 1);
		try {
			dependencyManager.addFromURL("libraries" + File.separator + name + "," + url);
			File file = new File("libraries" + File.separator + name);
			readZip(file);
		} catch (Throwable err) {
			FlameLauncher.downloadDep(name, url);
		}
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

	public static void downloadDep(String name, String url) {
		dependencyManager.addFromURL("libraries" + File.separator + name + "," + url);
		File file = new File("libraries" + File.separator + name);
		readZip(file);
	}
}
