package com.tfc.flamemc;

import com.tfc.flame.FlameConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class FlameExternalLauncher {
	private static final String dir = System.getProperty("user.dir");
	
	public static void main(String[] args) {
		boolean alternator = false;
		HashMap<String,String> argsMap = new HashMap<>();
		String s1 = "";
		for (String s : args) {
			if (alternator) {
				argsMap.put(s1,s);
				alternator = false;
				FlameLauncher.field.append(s1+":"+s+"\n");
			} else {
				s1 = s;
				alternator = true;
			}
		}
		JFrame frame = new JFrame();
		frame.setSize(200,500);
		frame.setTitle("Version Picker");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		AtomicReference<File> version = new AtomicReference<>(null);
		int x = 0;
		int y = 0;
		JPanel panelHolder = new JPanel();
		for (File fi:Objects.requireNonNull(new File(dir + "\\versions").listFiles())) {
			JButton button = new JButton();
			button.setText(fi.getName());
			button.setActionCommand(fi.getPath());
			button.addActionListener(e -> version.set(new File(e.getActionCommand())));
			JPanel panel = new JPanel();
			panel.setSize(200,100);
			panel.setLocation(x,y);
			panel.add(button);
			panel.setVisible(true);
			y+=30;
			panelHolder.add(panel);
		}
		panelHolder.setVisible(true);
		panelHolder.add(FlameLauncher.field);
		scrollPane.createVerticalScrollBar();
		frame.add(panelHolder);
		frame.setVisible(true);
		while (version.get() == null);
		argsMap.replace("--version",version.get().getName().replace(".jar",""));
		String fi1 = version.get()+"\\"+version.get().getName()+".json";
		File file = new File(fi1);
		try {
			Scanner sc = new Scanner(file);
			StringBuilder main = new StringBuilder();
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				if (line.contains("mainClass")) {
					String line1 = line.substring(line.indexOf("\"mainClass\"")+"\"mainClass\"".length());
					FlameLauncher.field.append(line1);
					boolean inQuotes = false;
					for (int i=0; i < line1.length(); i++) {
						if (line1.charAt(i) == '"') {
							if (!inQuotes) inQuotes = true;
							else break;
						} else if (inQuotes) main.append(line1.charAt(i));
					}
				}
			}
			if (argsMap.containsKey("--main_class")) argsMap.replace("--main_class",main.toString());
			else argsMap.put("--main_class",main.toString());
			sc.close();
			FlameLauncher.field.append(fi1+"\n");
			FlameLauncher.field.append(main+"\n");
			
			String[] argsGive = new String[argsMap.size()*2];
			Iterator<String> keys = argsMap.keySet().iterator();
			Iterator<String> vals = argsMap.values().iterator();
			for (int i=0;i<argsMap.size();i++) {
				argsGive[i*2] = keys.next();
				argsGive[(i*2)+1] = vals.next();
			}
			
			FlameLauncher.additionalURLs.addAll(collectAllJars(new File(dir+"\\libraries")));
			FlameLauncher.main(argsGive);
		} catch (Throwable err) {
			FlameConfig.logError(err);
		}
		version.set(null);
		panelHolder.add(FlameLauncher.field);
	}
	
	private static ArrayList<String> collectAllJars(File path) {
		ArrayList<String> paths = new ArrayList<>();
		for (File f:path.listFiles()) {
			if (f.isDirectory()) {
				paths.addAll(collectAllJars(f));
			} else if (f.getName().endsWith(".jar")) {
				paths.add(f.getPath());
			}
		}
		return paths;
	}
	
	private static void logError(Throwable err) {
		StringBuilder s = new StringBuilder();
		FlameLauncher.field.append("\n\n");
		s.append("Flame encountered an error:\n");
		s.append(err.getClass().getName()).append(": ").append(err.getLocalizedMessage()).append("\n");
		for (StackTraceElement element : err.getStackTrace()) {
			s.append(element.toString()).append("\n");
		}
//		try{Thread.sleep(2000);}catch(Throwable err2){}
		FlameLauncher.field.append(s.toString());
		err.getStackTrace();
	}
}
