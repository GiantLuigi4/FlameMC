package com.tfc.flamemc;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
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

	public static void main(String[] args) {
		JFrame frame=null;
		File flame_config = new File(dir+"\\flame_config\\flamemc.txt");
		boolean log = false;
		String main_class = "net.minecraft.client.main.Main";
		if (!flame_config.exists()) {
			try {
				flame_config.getParentFile().mkdirs();
				flame_config.createNewFile();
				FileWriter writer = new FileWriter(flame_config);
				writer.write("log:false\n");
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
				if (line.startsWith("log:")) {
					log = Boolean.parseBoolean(line.replace("log:", ""));
				} else if (line.startsWith("main_class:")) {
					main_class = source_line.substring("main_class:".length());
				}
			}
			sc.close();
		} catch (Throwable err) {
			logError(err);
		}
		if (log) {
			frame=new JFrame("Flame MC log");
			frame.add(field);
			frame.setSize(1000, 1000);
			frame.setVisible(true);
		}
		try {
			lockedClasses.add(Class.forName("com.tfc.flamemc.FlameLauncher"));
			lockedClasses.add(Class.forName("com.tfc.flamemc.FlameLauncher"));
			lockedClasses.add(Class.forName("dbn"));
		} catch (Throwable ignored) {}
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
			lockedClasses.forEach(c->{
				field.append(c.getName()+'\n');
				loader.append(c.getName(), c);
			});
			field.append("Locked FlameMC classes\n");
			field.append("Game arguments: "+Arrays.toString(args)+"\n");
			Class<?> mainClass = loader.load(main_class, false);
			if (mainClass != null) {
				field.append("Got main class\n");
				Method main = mainClass.getMethod("main", String[].class);
				if (args != null) main.invoke(null, (Object) args);
				else throw new Exception("Game args are missing???");
			} else throw new Exception("Main class is missing???");
		} catch (Throwable err) {
			logError(err);
			exit(err,frame,log);
		}
		exit(null,frame,log);
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
	
	private static void exit(Throwable err, JFrame frame, boolean log) {
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
		if (log) frame.dispose();
		if (err != null) throw new RuntimeException(err);
	}
}
