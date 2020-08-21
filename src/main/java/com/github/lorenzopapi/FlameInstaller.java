package com.github.lorenzopapi;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import com.tfc.flamemc.FlameLauncher;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;

public class FlameInstaller {

	private static final FlameInstaller INSTANCE = new FlameInstaller();

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

		INSTANCE.install(mainFrame, log, clicked, setVersionPath, installButton);
	}

	public void install(JFrame mainFrame, TextArea log, AtomicBoolean clicked, JTextField setVersionPath, JButton installButton) {
		while (mainFrame.isVisible()) {
			while (!clicked.get());

			clicked.set(false);

			try  {
				log.append("\nStart Installation");
				long start = System.nanoTime();
				ZipUtils zipper = new ZipUtils();
				String versionPath = setVersionPath.getText();
				String versionNumber = new File(versionPath).getName();
				File flameInstaller = new File(FlameLauncher.getDir() + "\\FlameInstaller.jar");
				File inputMinecraftJar = new File(versionPath + "\\" + versionNumber + ".jar");
				if (!inputMinecraftJar.exists()) throw new RuntimeException("No " + versionNumber + " file found! You must run the version alone before running FlameInstaller!");
				File outputFlameDir = new File(versionPath + "-flame");
				if (!outputFlameDir.exists()) outputFlameDir.mkdirs();
				File flameTmpDir = new File(outputFlameDir + "\\tmp");
				File fullOutput = new File(outputFlameDir + "\\" + versionNumber + "-flame.jar");
				if (!fullOutput.exists()) fullOutput.createNewFile();
				File jsonIn = new File(versionPath + "\\" + versionNumber + ".json");
				if (!jsonIn.exists()) throw new IOException("No " + versionNumber + " file found! You must run the version alone before running FlameInstaller!");
				File jsonOut = new File(outputFlameDir + "\\" + versionNumber + "-flame.json");
				if (!jsonOut.exists()) jsonOut.createNewFile();

				log.append("\nUnzipping Minecraft");
				zipper.unZip(inputMinecraftJar.getPath(), flameTmpDir);
				log.append("\nUnzipping Flame");
				zipper.unZip(flameInstaller.getPath(), flameTmpDir);
				
				log.append("\nZipping FlameMC");
				zipper.zip(flameTmpDir.listFiles(), fullOutput.getPath());

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
				launchJson.libraries.add(new MinecraftLaunchJson.Library("org.apache.bcel:bcel:6.0", mavenUrl));

				try (Writer writer = Files.newBufferedWriter(jsonOut.toPath())) {
					Gson gson = new Gson();
					JsonElement tree = gson.toJsonTree(launchJson);
					JsonWriter jsonWriter = new JsonWriter(writer);
					gson.toJson(tree, jsonWriter);
				}

				long stop = System.nanoTime();
				if (flameTmpDir.exists()) {
					Files.walk(Paths.get(flameTmpDir.getPath()))
						.sorted(Comparator.reverseOrder())
						.map(Path::toFile)
						.forEach(File::delete);
				}
				log.append("\nDone!\n");
				installButton.setEnabled(true);
				log.append("\nTime passed == " + (stop - start));
				//wait(10000);
				//System.exit(0);

			} catch (Throwable err) {
				for (StackTraceElement element : err.getStackTrace()) {
					log.append("\n"+element);
				}
				throw new RuntimeException(err);
			}
		}
	}
}

/*Thread unzipAssetsThread = new Thread(() -> {
					try {
						log.append("\nDecompressing assets");
						zipper.unZip(inputMinecraftJar.getPath(), flameTmpDir, s -> s.startsWith("assets"));
						log.append("\nFinished assets decompression");
					} catch (Throwable err) {
						throw new RuntimeException(err);
					}
				}, "Unzip Assets");

				Thread unzipDataThread = new Thread(() -> {
					try {
						log.append("\nDecompressing data (if Flame is for 1.13+, else doing nothing)");
						zipper.unZip(inputMinecraftJar.getPath(), flameTmpDir, s -> s.startsWith("data"));
						log.append("\nFinished data decompression");
					} catch (Throwable err) {
						throw new RuntimeException(err);
					}
				}, "Unzip Data");

				Thread unzipClassesThread = new Thread(() -> {
					try {
						log.append("\nDecompressing classes");
						zipper.unZip(inputMinecraftJar.getPath(), flameTmpDir, s -> s.endsWith(".class") || s.startsWith("META"));
						log.append("\nFinished classes decompression");
					} catch (Throwable err) {
						throw new RuntimeException(err);
					}
				}, "Unzip Classes");

				Thread unzipInstallerThread = new Thread(() -> {
					try {
						log.append("\nDecompressing Installer");
						zipper.unZip(flameInstaller.getPath(), flameTmpDir, name -> name.startsWith("com/tfc") && name.endsWith(".class") && !name.contains("FlameLoader"));
						log.append("\nFinished decompressing installer");
					} catch (Throwable err) {
						throw new RuntimeException(err);
					}
				}, "Unzip Installer");

				Thread generateJson = new Thread(() -> {
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
						gson.toJson(tree, jsonWriter);
					} catch (Throwable ex) {
						throw new RuntimeException(ex);
					}
				});

				Thread zipThread = new Thread(() -> {
					try {
						zipper.zip(flameTmpDir.listFiles(), fullOutput.getPath());
					} catch (Throwable e) {
						throw new RuntimeException(e);
					}
				}, "Zip Thread");

				Thread endThread = new Thread(() -> {
					long stop = System.nanoTime();
					/*if (flameTmpDir.exists()) {
						try {
							Files.walk(Paths.get(flameTmpDir.getPath()))
									.sorted(Comparator.reverseOrder())
									.map(Path::toFile)
									.forEach(File::delete);
						} catch (IOException ignored) {}
					}
					log.append("\nDone!\n");
					installButton.setEnabled(true);
					log.append("\nTime passed == " + (stop - start));
					//System.exit(0);
				});

				if (!flameTmpDir.exists() && fullOutput.length() == 0) {
					flameTmpDir.getParentFile().mkdirs();
					unzipAssetsThread.start();
					unzipDataThread.start();
					unzipClassesThread.start();
					unzipInstallerThread.start();
					unzipAssetsThread.join();
					unzipClassesThread.join();
					unzipDataThread.join();
					unzipInstallerThread.join();
					zipThread.start();
				}

				if (jsonOut.length() == 0)
					generateJson.start();

				generateJson.join();
				unzipAssetsThread.join();
				unzipClassesThread.join();
				unzipDataThread.join();
				unzipInstallerThread.join();
				zipThread.join();
				endThread.start();*/