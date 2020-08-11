package com.tfc.flamemc;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FlameLauncher {
	private static final String dir = System.getProperty("user.dir");
	
	public static ArrayList<Class<?>> lockedClasses = new ArrayList<>();
	
	public static final FlameLoader loader;
	
	protected static FlameLog field = new FlameLog();
	
	static {
		FlameLoader loader1 = null;
		try {
			loader1 = new FlameLoader();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			field.append("FATAL ERROR: MAIN  LOADER FAILED TO INITIALIZE.\n");
			System.exit(-1);
		}
		loader = loader1;
	}
	
	protected static boolean log_bytecode = false;
	
	public static void main(String[] args) {
		field.append("Startup Flame\n");
		
		File flame_config = new File(dir + "\\flame_config\\flamemc.txt");
		boolean log = false;
		boolean save_log = true;
		String main_class = "net.minecraft.client.main.Main";
		if (!flame_config.exists()) {
			field.append("Write Configs");
			try {
				flame_config.getParentFile().mkdirs();
				flame_config.createNewFile();
				FileWriter writer = new FileWriter(flame_config);
				writer.write("log_window:false\n");
				writer.write("save_log:true\n");
				writer.write("log_bytecode:false\n");
				writer.write("main_class:net.minecraft.client.main.Main");
				writer.close();
			} catch (Throwable err) {
				logError(err);
			}
		}
		
		field.append("Read Configs");
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
					log_bytecode = Boolean.parseBoolean(line.replace("log_bytecode:", ""));
				} else if (line.startsWith("main_class:")) {
					main_class = source_line.substring("main_class:".length());
//					Class<?> mainClass = loader.load(main_class,true);
				}
			}
			sc.close();
		} catch (Throwable err) {
			logError(err);
		}
		
//		if (!(FlameLauncher.class.getClassLoader() instanceof FlameLoader)) {
//			try {
//				loader.load(main_class,true);
//				loader.setParent(FlameLauncher.class.getClassLoader());
//			} catch (Throwable ignored) {}
//			Class<?> clazz = loader.loadClass(FlameLauncher.class.getName());
//			try {
//				try {
//					loader.setPath(dir + "\\versions\\1.15.2-flame\\1.15.2-flame.jar");
//					loader.setParent(FlameLauncher.class.getClassLoader());
//				} catch (Throwable ignored) {}
//				try { clazz.getField("field").set(null,field); } catch (Throwable ignored) {}
//				try {
//					Field classes = ClassLoader.class.getDeclaredField("classes");
//					classes.setAccessible(true);
//					Vector<Class<?>> vc1 = (Vector<Class<?>>)classes.get(FlameLauncher.class.getClassLoader());
//					vc1.clear();
//				} catch (Throwable err) {
//					logError(err);
//				}
//				loader.load("org.apache.logging.log4j.spi.AbstractLogger",true);
//				loader.load("org.apache.logging.log4j.status.StatusLogger",true);
//				loader.load("org.apache.logging.log4j.Logger",true);
//				loader.load("org.apache.logging.log4j.core.config.ConfigurationSource",true);
////				loader.append("h",Class.forName("h"));
////				loader.append("djy",Class.forName("djy"));
////				loader.append("djy$a",Class.forName("djy$a"));
////				loader.append("djy$b",Class.forName("djy$b"));
////				loader.append("djy$c",Class.forName("djy$c"));
////				loader.append("djy$d",Class.forName("djy$d"));
////				loader.append("com.mojang.authlib.properties.PropertyMap",Class.forName("com.mojang.authlib.properties.PropertyMap"));
////				loader.append("dbz",Class.forName("dbz"));
////				loader.append("cxh",Class.forName("cxh"));
////				loader.load(FlameLog.class.getName(),true);
////				loader.load(IFlameMod.class.getName(),true);
//				clazz.getMethod("main",String[].class).invoke(null,(Object)args);
//			} catch (Throwable err) {
//				logError(err);
//				if (err instanceof InvocationTargetException) logError(err.getCause());
//			}
//			exit(null,null,false,true,"Boot Loader");
//			System.exit(-1);
//		}
		
//		if (FlameLauncher.class.getClassLoader() instanceof FlameLoader) {
//			((FlameLoader) FlameLauncher.class.getClassLoader()).getClassMap().forEach((name,clazz)->loader.append(name,clazz,true));
//		}
		
		Frame frame = null;
		
		if (log) {
			try {
				frame = new Frame("Flame MC log");
				frame.add(field);
				frame.setSize(1000, 1000);
				frame.setVisible(true);
			} catch (Throwable err) {logError(err);}
		}
		
		field.append(FlameLauncher.class.getClassLoader().getClass().toString()+"\n");
		
		field.append("Set Version Loader Path\n");
		try {
			loader.setPath(dir + "\\versions\\1.15.2-flame\\1.15.2-flame.jar");
			loader.setParent(FlameLauncher.class.getClassLoader());
		} catch (Throwable err) {
			logError(err);
		}
		
		field.append("Create Mod Loader\n");
		FlameLoader modLoader = null;
		try {
			modLoader = new FlameLoader();
		} catch (Throwable err) {
			logError(err);
			field.append("FATAL ERROR: MODLOADER FAILED TO INITIALIZE.\n");
			System.exit(-1);
		}
		
		field.append("Dir:" + dir + "\n");
		field.append("Set Mod Loader path\n");
		modLoader.setPath(dir + "\\flame_mods", true);
		field.append("Set mod loader path to: " + dir + "\\flame_mods\n");
		loader.addLoader(modLoader);
		
		try {
			lockedClasses.add(Class.forName("com.tfc.flamemc.FlameLauncher"));
			lockedClasses.add(Class.forName("com.tfc.flamemc.FlameLoader"));
			lockedClasses.add(Class.forName("com.tfc.flamemc.FlameLog"));
			lockedClasses.add(Class.forName("com.tfc.flamemc.IFlameMod"));
		} catch (Throwable ignored) {
		}
		field.append("Locking FlameMC classes\n");
		lockedClasses.forEach(c -> {
			field.append(c.getName() + '\n');
			loader.append(c.getName(), c);
		});
		field.append("Locked FlameMC classes\n");
		
		field.append("Discovering Flame Mods\n");
		HashMap<String,IFlameMod> mods = new HashMap<>();
		try {
			for (File fi:Objects.requireNonNull(new File(dir + "\\flame_mods").listFiles())) {
				if (fi.exists() && (fi.getName().endsWith(".zip") || fi.getName().endsWith(".jar"))) {
					try {
						Class<?> c = null;
						if (fi.getName().endsWith(".jar")) {
							JarFile file = new JarFile(fi);
							Enumeration<JarEntry> entries = file.entries();
							while (entries.hasMoreElements()) {
								try {
									JarEntry entry = entries.nextElement();
									String name = entry.getName();
									if (name.contains("entries/"+fi.getName().replace(".jar","")+"/") && name.endsWith(".class")) {
										c = loader.load(entry.getName().replace(".class", "").replace("/","."),true);
										IFlameMod mod = ((IFlameMod)c.newInstance());
										mods.put(name,mod);
									}
								} catch (Throwable err) {
									field.append("Something went wrong while discovering mod: "+fi.getName()+"\n");
									logError(err);
								}
							}
						} else {
							ZipFile file = new ZipFile(fi);
							Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) file.entries();
							while (entries.hasMoreElements()) {
								try {
									ZipEntry entry = entries.nextElement();
									String name = entry.getName();
									if (name.contains("entries/"+fi.getName().replace(".zip","")+"/") && name.endsWith(".class")) {
										c = loader.load(entry.getName().replace(".class", "").replace("/","."),true);
										IFlameMod mod = ((IFlameMod)c.newInstance());
										mods.put(name,mod);
									}
								} catch (Throwable err) {
									field.append("Something went wrong while discovering mod: "+fi.getName()+"\n");
									logError(err);
								}
							}
						}
						if (c == null) {
							field.append("Main class for mod:" + fi.getName()+" does not appear to exist.\n");
						}
						for (Method m:c.getMethods()) {
							field.append(m.getName()+"\n");
						}
					} catch (Throwable err) {
						field.append("Something went wrong while discovering mod: "+fi.getName()+"\n");
						logError(err);
					}
				}
			}
		} catch (Throwable ignored) {}
		mods.forEach((name,iFlameMod) -> {
			field.append("PreInit:"+name+"\n");
			iFlameMod.preinit(args,field);
		});
		mods.forEach((name,iFlameMod) -> {
			field.append("Init:"+name+"\n");
			iFlameMod.init(args,field);
		});
		mods.forEach((name,iFlameMod) -> {
			field.append("PostInit:"+name+"\n");
			iFlameMod.postinit(args,field);
		});
		try {
			field.append("Game arguments: " + Arrays.toString(args) + "\n");
			Class<?> mainClass = loader.load(main_class,true);
			field.append("Got main class\n");
			if (mainClass!=null){
			Method main = mainClass.getMethod("main", String[].class);
			if (args != null) main.invoke(null, (Object) args);
			else throw new Exception("Game args are missing???");}
			else throw new Exception("Main class is missing???");
		} catch (Throwable err) {
			logError(err);
			if (err instanceof InvocationTargetException) logError(err.getCause());
			exit(err, frame, log, save_log,"Crash");
			System.exit(-1);
		}
		exit(null, frame, log, save_log,"Success");
		System.exit(0);
	}
	
	public static void logError(Throwable err) {
		StringBuilder s = new StringBuilder();
		field.append("\n\n");
		s.append("Flame encountered an error:\n");
		s.append(err.getClass().getName()).append(": ").append(err.getLocalizedMessage()).append("\n");
		for (StackTraceElement element : err.getStackTrace()) {
			s.append(element.toString()).append("\n");
		}
		field.append(s.toString());
		err.getStackTrace();
	}
	
	private static void exit(Throwable err, Frame frame, boolean log, boolean save_log,String suffix) {
		if (save_log) {
			try {
				File fi = new File(dir + "\\flame_logs");
				if (!fi.exists()) fi.mkdirs();
				fi = new File(dir + "\\flame_logs\\" + System.nanoTime() + "_" + suffix + ".txt");
				fi.createNewFile();
				FileWriter writer = new FileWriter(fi);
				writer.write(field.getText());
				writer.close();
			} catch (Throwable ignored) {
			}
		}
		if (log && frame!=null) frame.dispose();
		if (err != null) throw new RuntimeException(err);
	}
}
