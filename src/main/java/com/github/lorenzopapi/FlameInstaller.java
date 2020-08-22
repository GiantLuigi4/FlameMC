package com.github.lorenzopapi;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import java.util.Map;
import java.util.Objects;
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
		mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		mainFrame.setLocationRelativeTo(null);
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
				AtomicBoolean downloadedFromUrl = new AtomicBoolean(false);
				log.setText("");
				log.append("\nStart Installation");
				long start = System.nanoTime();
				Gson gson = new Gson();
				String versionPath = setVersionPath.getText();
				String versionNumber = new File(versionPath).getName();
				File flameInstaller = new File(FlameLauncher.getDir() + File.separator + "FlameInstaller.jar");
				File inputMinecraftJar = new File(versionPath + File.separator + versionNumber + ".jar");
				String versions = InstallerUtils.readUrl("https://launchermeta.mojang.com/mc/game/version_manifest.json");
				File jsonIn = new File(versionPath + File.separator + versionNumber + ".json");
				File outputFlameDir = new File(versionPath + "-flame");
				File fullOutput = new File(outputFlameDir + File.separator + versionNumber + "-flame.jar");
				if (!fullOutput.exists()) {
					if (!jsonIn.exists()) {
						log.setForeground(Color.yellow);
						log.append("\nWARN:No " + versionNumber + " json file found! The installer will try to download it from web.\nBe sure to have internet connection.");
						MinecraftVersionMeta meta = gson.fromJson(versions, MinecraftVersionMeta.class);
						for (MinecraftVersionMeta.Version version : meta.versions) {
							if (version.id.equals(versionNumber)) {
								jsonIn.getParentFile().mkdirs();
								jsonIn.createNewFile();
								InstallerUtils.downloadFromUrl(version.url, jsonIn.getPath());
								downloadedFromUrl.set(true);
								log.append("\nJson downloaded!");
								break;
							}
						}
						if (!downloadedFromUrl.get()) {
							log.setForeground(Color.red);
							log.append("\nERROR:No " + versionNumber + " json found! VERSION NOT EXISTING!!!");
							throw new IOException("Version not existing.");
						}
					}
					if (!inputMinecraftJar.exists()) {
						log.setForeground(Color.yellow);
						if (!outputFlameDir.exists()) outputFlameDir.mkdirs();
						log.append("\nWARN:No " + versionNumber + " version file found, but Json exists! The installer will try to download the jar from web.\nBe sure to have internet connection.");
						JsonParser parser = new JsonParser();
						JsonElement tree = parser.parse(Files.newBufferedReader(jsonIn.toPath()));

						JsonObject downloads = InstallerUtils.readJsonObject(tree.getAsJsonObject(), s -> s.equals("downloads"));
						JsonObject client = InstallerUtils.readJsonObject(Objects.requireNonNull(downloads).getAsJsonObject(), s -> s.equals("client"));
						for (Map.Entry<String, JsonElement> clientEntry : Objects.requireNonNull(client).entrySet()) {
							if (clientEntry.getKey().equals("url")) {
								InstallerUtils.downloadFromUrl(clientEntry.getValue().getAsString(), fullOutput.getPath());
								break;
							}
						}
					}
				}
				File flameTmpDir = new File(outputFlameDir + File.separator + "tmp");
				File jsonOut = new File(outputFlameDir + File.separator + versionNumber + "-flame.json");
				if (!outputFlameDir.exists()) outputFlameDir.mkdirs();

				if ((!flameTmpDir.exists() || flameTmpDir.length() == 0)) {
					if (downloadedFromUrl.get() || fullOutput.length() == 0) {
						log.append("\nUnzipping flame...");
						InstallerUtils.unzip(flameTmpDir.getPath(), flameInstaller.getPath(), name -> (name.startsWith("com/tfc/") && name.endsWith(".class") && !name.contains("FlameLoader")));
						log.append("\nUnzipping finished");
					}
				}

				ZipFile zipFile = new ZipFile(fullOutput);

				if (!jsonOut.exists()) {
					if (!downloadedFromUrl.get()) {
						log.append("\nCopying Minecraft jar...");
						Files.copy(Files.newInputStream(inputMinecraftJar.toPath()), fullOutput.toPath(), StandardCopyOption.REPLACE_EXISTING);
						log.append("\nMinecraft jar copied");
					}
					log.append("\nZipping FlameMC");
					zipFile.addFolder(new File(flameTmpDir + File.separator + "com"));
					log.append("\nZipping finished");
					log.append("\nWriting Json");
					FlamedJson launchJson = new FlamedJson(versionNumber + "-flame", versionNumber, "com.tfc.flamemc.FlameLauncher");
					launchJson.arguments.game = new ArrayList<>();
					String mavenUrl = "https://repo1.maven.org/maven2/";
					String asmRepo = "org.ow2.asm:asm";
					String asmVer = ":8.0.1";
					launchJson.libraries.add(new FlamedJson.Library(asmRepo + asmVer, mavenUrl));
					launchJson.libraries.add(new FlamedJson.Library(asmRepo + "-commons" + asmVer, mavenUrl));
					launchJson.libraries.add(new FlamedJson.Library(asmRepo + "-tree" + asmVer, mavenUrl));
					launchJson.libraries.add(new FlamedJson.Library(asmRepo + "-util" + asmVer, mavenUrl));
					launchJson.libraries.add(new FlamedJson.Library("org.apache.bcel:bcel:6.0", mavenUrl));

					try (Writer writer = Files.newBufferedWriter(jsonOut.toPath())) {
						JsonElement tree = gson.toJsonTree(launchJson);
						JsonWriter jsonWriter = new JsonWriter(writer);
						gson.toJson(tree, jsonWriter);
					}
					log.append("\nJson written");
				} else {
					log.append("\nVersion already existing");
					log.append("\nJson already generated");
				}

				if (flameTmpDir.exists()) {
					log.append("\nDeleting FlameMC temps...");
					Files.walk(Paths.get(flameTmpDir.getPath()))
							.sorted(Comparator.reverseOrder())
							.map(Path::toFile)
							.forEach(File::delete);
					log.append("\nTemps deleted");
				}
				if (downloadedFromUrl.get() && jsonIn.getParentFile().exists()) {
					log.append("\nDeleting download temps...");
					Files.walk(Paths.get(jsonIn.getParentFile().getPath()))
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
				downloadedFromUrl.set(false);
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