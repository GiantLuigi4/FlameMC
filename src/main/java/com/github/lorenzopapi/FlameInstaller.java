package com.github.lorenzopapi;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import com.tfc.flamemc.FlameLauncher;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
				log.append("Start Installation");
				long start = System.nanoTime();
				ZipUtils zipper = new ZipUtils();
				String versionPath = setVersionPath.getText();
				String versionNumber = new File(versionPath).getName();
				File inputMinecraftJar = new File(versionPath + "\\" + versionNumber + ".jar");
				if (!inputMinecraftJar.exists()) throw new RuntimeException("No " + versionNumber + " file found! You must run the version alone before running FlameInstaller!");
				File flameInstaller = new File(FlameLauncher.getDir() + "\\FlameInstaller.jar");
				File outputFlameDir = new File(versionPath + "-flame");
				if (!outputFlameDir.exists()) outputFlameDir.mkdirs();
				File flameTmpDir = new File(outputFlameDir + "\\tmp");
				File fullOutput = new File(outputFlameDir + "\\" + versionNumber + "-flame.jar");
				File jsonIn = new File(versionPath + "\\" + versionNumber + ".json");
				if (!jsonIn.exists()) throw new IOException("No " + versionNumber + " file found! You must run the version alone before running FlameInstaller!");
				File jsonOut = new File(outputFlameDir + "\\" + versionNumber + "-flame.json");
				if (!jsonOut.exists()) jsonOut.createNewFile();

				Thread unzip1 = null;
				Thread unzip2 = null;
				Thread unzip3 = null;
				Thread unzipInstaller = null;
				Thread generateJson;
				Thread finishInstall;

				if (!flameTmpDir.exists()) {
					flameTmpDir.getParentFile().mkdirs();
					unzip1 = new Thread(() -> {
						try {
							log.append("\nDecompressing assets");
							zipper.unZip(inputMinecraftJar.getPath(), flameTmpDir, s -> s.startsWith("assets\\"));
						} catch (Throwable err) {
							throw new RuntimeException(err);
						}
					}, "Unzip1");
					unzip2 = new Thread(() -> {
						try {
							log.append("\nDecompressing data (if Flame is for 1.13+, else doing nothing)");
							zipper.unZip(inputMinecraftJar.getPath(), flameTmpDir, s -> s.startsWith("data\\"));
						} catch (Throwable err) {
							throw new RuntimeException(err);
						}
					}, "Unzip2");
					unzip3 = new Thread(() -> {
						try {
							log.append("\nDecompressing classes and Manifests");
							zipper.unZip(inputMinecraftJar.getPath(), flameTmpDir, s -> s.endsWith("class"));
						} catch (Throwable err) {
							throw new RuntimeException(err);
						}
					}, "Unzip3");
					unzipInstaller = new Thread(() -> {
						try {
							log.append("\nDecompressing Installer");
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
						} catch (Throwable err) {
							throw new RuntimeException(err);
						}
					}, "Unzip installer");
					unzip1.start();
					unzip2.start();
					unzip3.start();
					unzipInstaller.start();
				}

				if (jsonOut.length() == 0) {
					generateJson = new Thread(() -> {
						log.append("\nGenerating Json");
						MinecraftLaunchJson launchJson = new MinecraftLaunchJson(versionNumber + "-flame", versionNumber, "com.tfc.flamemc.FlameLauncher");

						launchJson.arguments.game = new ArrayList<>();
						String mavenUrl = "https://repo1.maven.org/maven2/";
						String asmRepo = "org.ow2.asm:asm";
						String asmVer = ":8.0.1";
						launchJson.libraries.add(new MinecraftLaunchJson.Library(asmRepo + asmVer, mavenUrl));
						launchJson.libraries.add(new MinecraftLaunchJson.Library(asmRepo + "-commons" + asmVer, mavenUrl));
						launchJson.libraries.add(new MinecraftLaunchJson.Library(asmRepo + "-tree" + asmVer, mavenUrl));
						launchJson.libraries.add(new MinecraftLaunchJson.Library(asmRepo + "-util" + asmVer, mavenUrl));

						try (Writer writer = Files.newBufferedWriter(jsonOut.toPath())) {
							Gson gson = new Gson();
							JsonElement tree = gson.toJsonTree(launchJson);
							JsonWriter jsonWriter= new JsonWriter(writer);
							jsonWriter.setIndent("  ");
							gson.toJson(tree, jsonWriter);
						} catch (Throwable ex) {
							throw new RuntimeException(ex);
						}
					});
					generateJson.start();
				}

				finishInstall = new Thread(() -> {
					try {
						if (flameTmpDir.exists())
							Files.walk(Paths.get(flameTmpDir.getPath()))
									.sorted(Comparator.reverseOrder())
									.map(Path::toFile)
									.forEach(File::delete);
						log.append("\nDone!\n");
						long stop = System.nanoTime();
						installButton.setEnabled(true);
						log.append("\nTime passed == " + (stop - start));
					} catch (Throwable err) {
						throw new RuntimeException(err);
					}
				});

				//finishInstall.start();
			} catch (Throwable err) {
				throw new RuntimeException(err);
			}
		}
	}
}

