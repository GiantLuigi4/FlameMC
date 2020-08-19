package com.github.lorenzopapi;

import com.tfc.flamemc.FlameLauncher;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class FlameInstaller {

	public static void main(String[] args) {
		AtomicBoolean clicked = new AtomicBoolean(false);
		JFrame mainFrame = new JFrame("Flame Loader Installer");
		if (!(System.getProperty("os.name").contains("Windows"))) {
			mainFrame.setTitle("Incompatible!");
			JTextField error = new JTextField();
			error.setText("OS is not compatible (for now)");
			JButton exit = new JButton();
			exit.addActionListener(e -> System.exit(1));
		}
		JPanel panel = new JPanel();

		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel textPanel = new JPanel();
		textPanel.setSize(640, 320);
		JTextField setVersionPath = new JTextField();
		setVersionPath.setSize(640, 320);
		setVersionPath.setText(System.getenv("APPDATA") + "\\.minecraft\\versions\\1.16.2");
		panel.add(setVersionPath);

		JPanel installPanel = new JPanel();
		JButton installButton = new JButton("Install for 1.16.2");
		installButton.addActionListener(e -> {
			installButton.setEnabled(false);
			clicked.set(true);
		});
		installPanel.setLocation(50, 0);
		installPanel.add(installButton);

		JPanel logPanel = new JPanel();
		logPanel.setSize(640, 320);
		logPanel.setMinimumSize(logPanel.getSize());
		logPanel.setMaximumSize(logPanel.getSize());
		TextArea log = new TextArea();
		logPanel.add(log);

		panel.add(textPanel);
		panel.add(installPanel);
		panel.add(logPanel);

		mainFrame.add(panel);
		mainFrame.pack();
		mainFrame.setVisible(true);

		while (mainFrame.isVisible()) {
			while (!clicked.get());

			clicked.set(false);

			try  {
				long start = System.nanoTime();
				log.append("Start Installation");
				String versionPath = setVersionPath.getText();
				String versionNumber = new File(versionPath).getName();
				File inputMinecraftJar = new File(versionPath + "\\" + versionNumber + ".jar");
				File outputFlameDir = new File(versionPath + "-flame");
				File flameInstaller = new File(FlameLauncher.getDir() + "\\FlameInstaller.jar");
				File flameTmpDir = new File(outputFlameDir + "\\tmp");
				File fullOutput = new File(outputFlameDir + "\\" + versionNumber + "-flame.jar");
				if (!flameTmpDir.exists())
					flameTmpDir.getParentFile().mkdirs();
				ZipUtils zipper = new ZipUtils();
				zipper.unZip(inputMinecraftJar.getPath(), flameTmpDir);

				JarInputStream installerStream = new JarInputStream(new FileInputStream(flameInstaller));
				JarEntry flameEntry = installerStream.getNextJarEntry();
				while (flameEntry != null) {
					String name = flameEntry.getName();
					if (name.startsWith("com/tfc") && name.endsWith(".class") && !name.contains("FlameLoader")) {
						File destFile = new File(flameTmpDir, name);
						if (!destFile.exists()) {
							destFile.getParentFile().mkdirs();
							destFile.createNewFile();
							zipper.extractFile(installerStream, destFile);
						}
					}
					flameEntry = installerStream.getNextJarEntry();
				}
				installerStream.closeEntry();
				installerStream.close();

				if (!fullOutput.exists()) {
					fullOutput.createNewFile();
				}

				zipper.zip(flameTmpDir.listFiles(), fullOutput.getPath());
				/*File jsonIn = new File(text + "-\\" + path + ".json");
				if (!jsonIn.exists()) throw new FileNotFoundException("No 1.16.2 json found! You must run the version alone before running FlameInstaller");

				String parsed = JSONParser.quote(String.valueOf(Files.readAllLines(Paths.get(jsonIn.getPath()))));
				File jsonOut = new File(text + "-flame\\" + path + "-flame.json");
				if (!jsonOut.exists()) jsonOut.createNewFile();
				FileWriter writer = new FileWriter(jsonOut);
				writer.write(parsed);
				writer.close();*/

				Files.walk(Paths.get(flameTmpDir.getPath()))
						.sorted(Comparator.reverseOrder())
						.map(Path::toFile)
						.forEach(File::delete);
				log.append("\nDone!\n");
				long stop = System.nanoTime();
				log.append("\nTime passed == " + (stop - start));
			} catch (Throwable err) {
				throw new RuntimeException(err);
			}
		}
	}
}