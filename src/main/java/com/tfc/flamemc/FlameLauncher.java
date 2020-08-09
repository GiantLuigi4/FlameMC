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

public class FlameLauncher {
	private static final String dir = System.getProperty("user.dir");
	
	public static ArrayList<Class> lockedClasses = new ArrayList<>();
	
	public static final FlameLoader loader = new FlameLoader();
	
	public static final TextArea field = new TextArea() {
		@Override
		public void append(String str) {
			String pattern = "hh:mm:ss";
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
			//https://www.edureka.co/blog/date-format-in-java/#:~:text=Creating%20A%20Simple%20Date%20Format,-A%20SimpleDateFormat%20is&text=String%20pattern%20%3D%20%22yyyy%2DMM,for%20formatting%20and%20parsing%20dates.
			super.append("["+simpleDateFormat.format(new Date())+"] "+str);
		}
	};
	public static void main(String[] args) {
		JFrame frame=null;
		if (args[args.length-1].equals("flame_log")||true) {
			frame=new JFrame("Flame MC log");
			frame.add(field);
			frame.setSize(1000, 1000);
			frame.setVisible(true);
		}
		try {
			lockedClasses.add(Class.forName("com.tfc.flamemc.FlameLauncher"));
			lockedClasses.add(Class.forName("com.tfc.flamemc.FlameLoader"));
			try {
				lockedClasses.add(Class.forName("net.fabricmc.loader.launch.knot.KnotClient"));
			} catch (Throwable ignored) {}
		} catch (Throwable ignored) {}
		try {
			field.setText("Startup Flame\n");
			FlameLoader versionLoader = new FlameLoader();
			field.append("Dir:" + dir + "\n");
			versionLoader.setPath(dir + "\\versions\\1.15.2-flame\\1.15.2-flame.jar");
			field.append("Set version loader path to: " + dir + "\\versions\\1.15.2-flame\n");
			field.append("File exists:" + new File(dir + "\\versions\\1.15.2-flame").exists() + "\n");
			loader.addLoader(versionLoader);
			field.append("Locking FlameMC classes\n");
			lockedClasses.forEach(c->{
				field.append(c.getName()+'\n');
				loader.append(c.getName(), c);
			});
			field.append("Locked FlameMC classes\n");
			field.append("Game arguments: "+Arrays.toString(args)+"\n");
			Class<?> mainClass = loader.load("net.fabricmc.loader.launch.knot.KnotClient", false);
			if (mainClass != null) {
				field.append("Got main class\n");
				Method main = mainClass.getMethod("main", String[].class);
				if (main != null)
					if (args != null) main.invoke(null, (Object) args);
					else throw new Exception("Game args are missing???");
				else throw new Exception("Main method is missing???");
			} else throw new Exception("Main class is missing???");
		} catch (Throwable err) {
			logError(err);
		}
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
		if (args[args.length-1].equals("flame_log")||true) {
			frame.dispose();
		}
	}
	
	public static void logError(Throwable err) {
		field.append("\nFlame encountered an error\n");
		field.append(err.getClass().getName() + ": " + err.getLocalizedMessage() + "\n");
		for (StackTraceElement element : err.getStackTrace()) {
			field.append(element.toString() + "\n");
		}
		err.getStackTrace();
	}
}
