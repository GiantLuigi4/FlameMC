package com.github.lorenzopapi;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import com.tfc.flamemc.FlameLauncher;
import net.lingala.zip4j.ZipFile;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;

public class FlameInstaller {

	private static final FlameInstaller INSTANCE = new FlameInstaller();

	public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		AtomicBoolean clicked = new AtomicBoolean(false);
		JFrame mainFrame = new JFrame("Flame Loader Installer");

		JPanel panel = new JPanel();
		JPanel textPanel = new JPanel();
		textPanel.setSize(640, 320);
		JTextField setVersionPath = new JTextField();
		setVersionPath.setSize(640, 320);
		setVersionPath.setText(InstallerUtils.findVersionsDir() + File.separator + "1.16.2");
		textPanel.add(setVersionPath);

		JPanel installPanel = new JPanel();
		JButton installButton = new JButton("Install for 1.16.2");
		installButton.addActionListener(e -> {
			setVersionPath.setEnabled(false);
			installButton.setEnabled(false);
			clicked.set(true);
		});
		installPanel.setLocation(0, -100);
		installPanel.add(installButton);

		JPanel logPanel = new JPanel();
		logPanel.setSize(200, 200);
		logPanel.setMinimumSize(logPanel.getSize());
		logPanel.setMaximumSize(logPanel.getSize());
		TextArea log = new TextArea();
		log.setBackground(new Color(12632256));
		log.setEditable(false);
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
			while (installButton.isEnabled()) {
				String versionPath = setVersionPath.getText();
				String versionNumber = new File(versionPath).getName();
				installButton.setText("Install for " + versionNumber);
			}
			while (!clicked.get());

			clicked.set(false);

			try  {
				log.setText("");
				log.append("\nStart Installation");
				long start = System.nanoTime();
				String versionPath = setVersionPath.getText();
				String versionNumber = new File(versionPath).getName();
				File flameInstaller = new File(FlameLauncher.getDir() + File.separator + "FlameInstaller.jar");
				File inputMinecraftJar = new File(versionPath + File.separator + versionNumber + ".jar");
				if (!inputMinecraftJar.exists()) {
					log.setForeground(Color.red);
					log.append("\nERROR:No " + versionNumber + " version file found! You must run the version alone before running FlameInstaller!");
					throw new IOException("Version not found.");
				}
				File outputFlameDir = new File(versionPath + "-flame");
				if (!outputFlameDir.exists()) outputFlameDir.mkdirs();
				File flameTmpDir = new File(outputFlameDir + File.separator + "tmp");
				File fullOutput = new File(outputFlameDir + File.separator + versionNumber + "-flame.jar");
				File jsonIn = new File(versionPath + File.separator + versionNumber + ".json");
				if (!jsonIn.exists()) {
					log.setForeground(Color.red);
					log.append("\nERROR:No " + versionNumber + " version file found! You must run the version alone before running FlameInstaller!");
					throw new IOException("Version not found.");
				}
				File jsonOut = new File(outputFlameDir + File.separator + versionNumber + "-flame.json");

				if ((!flameTmpDir.exists() || flameTmpDir.length() == 0) && !fullOutput.exists()) {
					log.append("\nUnzipping flame...");
					InstallerUtils.unzip(flameTmpDir.getPath(), flameInstaller.getPath(), name -> (name.startsWith("com/tfc/") && name.endsWith(".class") && !name.contains("FlameLoader")));
				}

				if (!fullOutput.exists()) {
					ZipFile zipFile = new ZipFile(fullOutput);
					log.append("\nZipping FlameMC...");
					Files.copy(Files.newInputStream(inputMinecraftJar.toPath()), fullOutput.toPath(), StandardCopyOption.REPLACE_EXISTING);
					zipFile.addFolder(new File(flameTmpDir + File.separator + "com"));
					log.append("\nZipping finished");
				} else {
					log.append("\nFlameMC version already created");
				}

				if (!jsonOut.exists()) {
					log.append("\nWriting Json");
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
					log.append("\nJson written");
				} else {
					log.append("\nJson already generated");
				}

				if (flameTmpDir.exists()) {
					log.append("\nDeleting temps...");
					Files.walk(Paths.get(flameTmpDir.getPath()))
							.sorted(Comparator.reverseOrder())
							.map(Path::toFile)
							.forEach(File::delete);
					log.append("\nTemps deleted");
				}
				long stop = System.nanoTime();
				log.append("\nDone!\n");
				installButton.setEnabled(true);
				setVersionPath.setEnabled(true);
				long timePassed = (stop - start) / 1000000;
				String timePass = Long.toString(timePassed);
				log.append("\nInstallation took " + timePass + " milliseconds.\n");
			} catch (Throwable err) {
				for (StackTraceElement element : err.getStackTrace()) {
					log.append("\n" + element);
				}
				log.append("\nRestart installer");
				throw new RuntimeException(err);
			}
		}
	}
}