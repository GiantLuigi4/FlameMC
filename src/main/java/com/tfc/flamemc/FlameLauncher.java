package com.tfc.flamemc;

import com.tfc.flame.*;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;

public class FlameLauncher {
	private static String dir = System.getProperty("user.dir");
	private static boolean isDev =
			new File(dir + "\\src").exists() &&
					(new File(dir + "\\build").exists() ||
							new File(dir + "\\build.gradle").exists()
					);
	
	public static ArrayList<Class> lockedClasses = new ArrayList<>();
	
	//	public static final FlameLoader loader = new FlameLoader();
	private static FlameURLLoader loader;
	
	public static FlameURLLoader getLoader() {
		return loader;
	}
	
	public static String getDir() {
		return dir;
	}
	
	public static final FlameLog field = new FlameLog();
	
	protected static final ArrayList<String> additionalURLs = new ArrayList<>();
	
	public static void main(String[] args) {
		if (isDev) {
			dir = dir + "\\run";
		}
		field.append("Startup Flame\n");
		field.append(Arrays.toString(args));
		FlameConfig.field = field;
		JFrame frame = null;
		
		String version = "1.15.2-flame";
		String gameDir = dir;
		String main_class = null;
		boolean isVersion = false;
		boolean isDir = false;
		boolean isMain = false;
		for (String s : args) {
			if (s.equals("--version")) {
				isVersion = true;
			} else if (isVersion) {
				version = s;
				isVersion = false;
			} else if (s.equals("--gameDir")) {
				isDir = true;
			} else if (isDir) {
				gameDir = s;
				isDir = false;
			} else if (s.equals("--main_class")) {
				isMain = true;
			} else if (isMain) {
				main_class = s;
				isMain = false;
			}
		}
		
		File flame_config = new File(gameDir + "\\flame_config\\flamemc.txt");
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
		}
		
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
		
		if (main_class == null) {
			File version_config = new File(dir + "\\versions\\" + version + "\\options.txt");
			if (!version_config.exists()) {
				try {
					version_config.getParentFile().mkdirs();
					version_config.createNewFile();
					FileWriter writer = new FileWriter(version_config);
					writer.write("main_class:" + "net.minecraft.client.main.Main");
					writer.close();
				} catch (Throwable err) {
					FlameConfig.logError(err);
				}
			}
			
			try {
				Scanner sc = new Scanner(version_config);
				while (sc.hasNextLine()) {
					String source_line = sc.nextLine();
					String line = source_line.toLowerCase();
					if (line.startsWith("main_class:")) {
						main_class = source_line.substring("main_class:".length());
					}
				}
				sc.close();
			} catch (Throwable err) {
				FlameConfig.logError(err);
			}
		}

//		field.append("Set Version Loader Path\n");
//		try {
//			loader.setPath(dir + "\\versions\\1.15.2-flame\\1.15.2-flame.jar");
//			loader.setParent(FlameLauncher.class.getClassLoader());
//		} catch (Throwable err) {
//			FlameConfig.logError(err);
//		}
		
		if (log) {
			frame = new JFrame("Flame MC log: " + version);
			frame.add(field);
			String finalVersion = version;
			frame.addWindowListener(new WindowListener() {
				@Override public void windowOpened(WindowEvent e) {}
				
				@Override
				public void windowClosing(WindowEvent e) {
					exit(null,null,false,true,dir, finalVersion);
				}
				
				@Override
				public void windowClosed(WindowEvent e) {
					exit(null,null,false,true,dir, finalVersion);
				}
				
				@Override public void windowIconified(WindowEvent e) { }
				
				@Override public void windowDeiconified(WindowEvent e) { }
				
				@Override public void windowActivated(WindowEvent e) { }
				
				@Override
				public void windowDeactivated(WindowEvent e) {
					exit(null,null,false,true,dir, finalVersion);
				}
			});
			frame.setSize(1000, 1000);
			frame.setVisible(true);
		}
		
		try {
			lockedClasses.add(Class.forName("com.tfc.flamemc.FlameLauncher"));
			lockedClasses.add(Class.forName("com.tfc.flame.FlameLoader"));
			lockedClasses.add(Class.forName("com.tfc.flamemc.FlameTextArea"));
			lockedClasses.add(Class.forName("com.tfc.flame.IFlameMod"));
			lockedClasses.add(Class.forName("com.tfc.flame.FlameConfig"));
			lockedClasses.add(Class.forName("com.tfc.flame.FlameURLLoader"));
			lockedClasses.add(Class.forName("com.tfc.flame.IFlameAPIMod"));
		} catch (Throwable ignored) {
		}
//		field.append("Locking FlameMC classes\n");
//		lockedClasses.forEach(c -> {
//			field.append(c.getName() + '\n');
//			loader.append(c.getName(), c);
//		});
//		field.append("Locked FlameMC classes\n");
		
		try {
			ArrayList<String> mods = new ArrayList<>();
			File fi = new File(gameDir + "\\flame_mods");
			if (!fi.exists()) fi.mkdirs();
			
			for (File fi1 : Objects.requireNonNull(fi.listFiles())) mods.add(fi1.getPath());
			
			URL[] urls = new URL[mods.size() + 1 + additionalURLs.size()];
			urls[0] = new File(dir + "\\versions\\" + version + "\\" + version + ".jar").toURL();
			for (int i = 0; i < mods.size(); i++) {
				String s = mods.get(i);
				File fi1 = new File(s);
				urls[i + 1] = fi1.toURL();
			}
			
			for (int i = 0; i < additionalURLs.size(); i++) {
				String s = additionalURLs.get(i);
				File fi1 = new File(s);
				urls[i + mods.size() + 1] = fi1.toURL();
			}
			
			loader = new FlameURLLoader(urls);
			field.append("Locking FlameMC classes\n");
			lockedClasses.forEach(c -> {
				field.append(c.getName() + '\n');
				try {
					loader.load(c.getName(), false);
				} catch (ClassNotFoundException ignored) {
				}
			});
			field.append("Locked FlameMC classes\n");
			ArrayList<Object> mods_list = new ArrayList<>();
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
			mods_list.forEach(mod -> {
				try {
					if (loader.load("com.tfc.flame.IFlameAPIMod", false).isInstance(mod)) {
						mod.getClass().getMethod("setupAPI", String[].class).invoke(mod, (Object) args);
					}
				} catch (Throwable err) {
					FlameConfig.logError(err);
				}
			});
			mods_list.forEach(mod -> {
				try {
					mod.getClass().getMethod("preinit", String[].class).invoke(mod, (Object) args);
				} catch (Throwable err) {
					FlameConfig.logError(err);
				}
			});
			mods_list.forEach(mod -> {
				try {
					mod.getClass().getMethod("init", String[].class).invoke(mod, (Object) args);
				} catch (Throwable err) {
					FlameConfig.logError(err);
				}
			});
			mods_list.forEach(mod -> {
				try {
					mod.getClass().getMethod("postinit", String[].class).invoke(mod, (Object) args);
				} catch (Throwable err) {
					FlameConfig.logError(err);
				}
			});
			if (version.contains("fabric")) {
				System.setProperty("fabric.gameJarPath", dir + "\\versions\\" + version + "\\" + version + ".jar");
			}
			loader.loadClass(main_class).getMethod("main", String[].class).invoke(null, (Object) args);
		} catch (Throwable err) {
			FlameConfig.logError(err);
			exit(err, frame, log, save_log, gameDir, version);
		}

//		field.append("Create Mod Loader\n");
//		FlameLoader modLoader = new FlameLoader();
//		field.append("Dir:" + dir + "\n");
//		field.append("Set Mod Loader path\n");
//		modLoader.setPath(dir + "\\flame_mods", true);
//		field.append("Set mod loader path to: " + dir + "\\flame_mods\n");
//		loader.addLoader(modLoader);
//
//		field.append("Discovering Flame Mods\n");
//
////		for (File f:new File(modLoader.getPath()).listFiles()) {
////			FlameLoader loader1 = new FlameLoader();
////			loader1.setPath(f.getPath());
////			modLoader.addLoader(loader1);
////		}
//
//		ArrayList<IFlameMod> mods = new ArrayList<>();
//		try {
//			for (File fi:Objects.requireNonNull(new File(dir + "\\flame_mods").listFiles())) {
//				if (fi.exists() && (fi.getName().endsWith(".zip") || fi.getName().endsWith(".jar"))) {
//					try {
//						Class<?> c = null;
//						if (fi.getName().endsWith(".jar")) {
//							JarFile file = new JarFile(fi);
//							Enumeration<JarEntry> entries = file.entries();
//							while (entries.hasMoreElements()) {
//								JarEntry entry = entries.nextElement();
//								String name = entry.getName();
//								if (name.startsWith("entries/"+fi.getName().replace(".jar","")+"/") && name.endsWith(".class")) {
//									c = loader.load(entry.getName().replace(".class", "").replace("/","."),true);
//									((IFlameMod)c.newInstance()).init(args);
//								}
//							}
//						} else {
//							ZipFile file = new ZipFile(fi);
//							Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) file.entries();
//							while (entries.hasMoreElements()) {
//								ZipEntry entry = entries.nextElement();
//								String name = entry.getName();
//								if (name.startsWith("entries/"+fi.getName().replace(".zip","")+"/") && name.endsWith(".class")) {
//									c = loader.load(entry.getName().replace(".class", "").replace("/","."),true);
//									((IFlameMod)c.newInstance()).init(args);
//								}
//							}
//						}
//						if (c == null) {
//							field.append("Main class for mod:" + fi.getName()+" does not appear to exist.\n");
//						}
//						for (Method m:c.getMethods()) {
//							field.append(m.getName()+"\n");
//						}
//					} catch (Throwable err) {
//						field.append("Something went wrong while initializing mod: "+fi.getName()+"\n");
//						FlameConfig.logError(err);
//						FlameConfig.logError(err.getCause());
//					}
//				}
//			}
//		} catch (Throwable ignored) {}
//
//		loader.blacklistName("h");
//		loader.blacklistName("djy");
//		loader.blacklistName("djy$a");
//		loader.blacklistName("djy$b");
//		loader.blacklistName("djy$c");
//		loader.blacklistName("djy$d");
//		loader.blacklistName("com.mojang.authlib.properties.PropertyMap");
//		loader.blacklistName("dbz");
//		loader.blacklistName("cxh");
//		try {
//			field.append("Game arguments: " + Arrays.toString(args) + "\n");
//			Class<?> mainClass = Class.forName(main_class, false, loader);
//			field.append("Got main class\n");
//			Method main = mainClass.getMethod("main", String[].class);
//			if (args != null) main.invoke(null, (Object) args);
//			else throw new Exception("Game args are missing???");
//		} catch (Throwable err) {
//			FlameConfig.logError(err);
//			if (err instanceof InvocationTargetException) FlameConfig.logError(err.getCause());
//			exit(err, frame, log, save_log);
//		}
		exit(null, frame, log, save_log, gameDir, version);
	}
	
	private static void exit(Throwable err, JFrame frame, boolean log, boolean save_log, String dir, String version) {
		if (save_log) {
			try {
				File fi = new File(dir + "\\flame_logs\\" + version);
				if (!fi.exists()) fi.mkdirs();
				fi = new File(dir + "\\flame_logs\\" + version + "\\" + System.nanoTime() + ".txt");
				fi.createNewFile();
				FileWriter writer = new FileWriter(fi);
				writer.write(field.getText());
				writer.close();
			} catch (Throwable ignored) {
			}
		}
		if (log) frame.dispose();
		if (err != null) throw new RuntimeException(err);
	}
	
	public static void addClassReplacement(String clazz) {
		loader.findReplacement(clazz);
	}
}
