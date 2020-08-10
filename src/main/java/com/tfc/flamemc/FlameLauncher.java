package com.tfc.flamemc;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

public class FlameLauncher {
	private static final String dir = System.getProperty("user.dir");
	
	public static ArrayList<Class> lockedClasses = new ArrayList<>();
	
	public static final FlameLoader loader = new FlameLoader();
	
	public static final FlameTextArea field = new FlameTextArea();
	
	protected static boolean log_bytecode = false;
	
	public static void main(String[] args) {
		JFrame frame = null;
		File flame_config = new File(dir + "\\flame_config\\flamemc.txt");
		boolean log = false;
		boolean save_log = true;
		String main_class = "net.minecraft.client.main.Main";
		if (!flame_config.exists()) {
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
				}
			}
			sc.close();
		} catch (Throwable err) {
			logError(err);
		}
		if (log) {
			frame = new JFrame("Flame MC log");
			frame.add(field);
			frame.setSize(1000, 1000);
			frame.setVisible(true);
		}
		try {
			lockedClasses.add(Class.forName("com.tfc.flamemc.FlameLauncher"));
			lockedClasses.add(Class.forName("com.tfc.flamemc.FlameLoader"));
			lockedClasses.add(Class.forName("com.tfc.flamemc.FlameTextArea"));
		} catch (Throwable ignored) {
		}
		loader.blacklistName("h");
		loader.blacklistName("djy");
		loader.blacklistName("djy$a");
		loader.blacklistName("djy$b");
		loader.blacklistName("djy$c");
		loader.blacklistName("djy$d");
		loader.blacklistName("com.mojang.authlib.properties.PropertyMap");
		loader.blacklistName("dbz");
		loader.blacklistName("cxh");
		try {
			field.append("Startup Flame\n");
			field.append("Set Version Loader Path\n");
			try {
				loader.setPath(dir + "\\versions\\1.15.2-flame\\1.15.2-flame.jar");
				loader.setParent(FlameLauncher.class.getClassLoader());
			} catch (Throwable err) {
				logError(err);
			}
			field.append("Set Mod Loader path\n");
			FlameLoader modLoader = new FlameLoader();
			field.append("Dir:" + dir + "\n");
			modLoader.setPath(dir + "\\flame_mods", true);
			field.append("Set mod loader path to: " + dir + "\\flame_mods\n");
			loader.addLoader(modLoader);
			field.append("Locking FlameMC classes\n");
			lockedClasses.forEach(c -> {
				field.append(c.getName() + '\n');
				loader.append(c.getName(), c);
			});
			field.append("Locked FlameMC classes\n");
			field.append("Game arguments: " + Arrays.toString(args) + "\n");
			Class<?> mainClass = Class.forName(main_class, false, loader);
			field.append("Got main class\n");
			Method main = mainClass.getMethod("main", String[].class);
			if (args != null) main.invoke(null, (Object) args);
			else throw new Exception("Game args are missing???");
		} catch (Throwable err) {
			logError(err);
			if (err instanceof InvocationTargetException) logError(err.getCause());
			exit(err, frame, log, save_log);
		}
		exit(null, frame, log, save_log);
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
	
	private static void exit(Throwable err, JFrame frame, boolean log, boolean save_log) {
		if (save_log) {
			try {
				File fi = new File(dir + "\\flame_logs");
				if (!fi.exists()) fi.mkdirs();
				fi = new File(dir + "\\flame_logs\\" + System.nanoTime() + ".txt");
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
}
