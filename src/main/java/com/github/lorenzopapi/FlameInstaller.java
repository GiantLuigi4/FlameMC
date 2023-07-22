package com.github.lorenzopapi;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

//TODO: refactor when launcher is ready
public class FlameInstaller {

    private static final FlameInstaller INSTANCE = new FlameInstaller();

    public String getInstallerJar() {
        URL urlJar = ClassLoader.getSystemResource(this.getClass().getName().replace(".", "/") + ".class");
        String urlStr = urlJar.toString();
        int from = "jar:file:".length();
        int to = urlStr.indexOf("!/");
        return urlStr.substring(from, to);
    }

    public static String dir = new File(INSTANCE.getInstallerJar()).getParentFile().getAbsolutePath();
    public static boolean isDev = new File(dir + File.separator + "gradlew.bat").exists();

    public static void main(String[] args) throws UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(UIManager.getLookAndFeel());
        String lastVersion = "1.16";
        JFrame main = new JFrame("FlameMC Installer");
        JPanel panel = new JPanel();

        JPanel textPanel = new JPanel();
        JTextField versionPath = new JTextField();
        textPanel.setSize(640, 320);
        versionPath.setSize(640, 320);
        versionPath.setText(findVersionsDir() + File.separator + lastVersion);
        textPanel.add(versionPath);

        JPanel logPanel = new JPanel();
        TextArea log = new TextArea();
        logPanel.setSize(200, 200);
        logPanel.setMinimumSize(logPanel.getSize());
        logPanel.setMaximumSize(logPanel.getSize());
        log.setBackground(new Color(12632256));
        log.setEditable(false);
        logPanel.add(log);

        JPanel installPanel = new JPanel();
        JButton installButton = new JButton("Install for " + lastVersion);
        installButton.addActionListener(e -> {
            versionPath.setEnabled(false);
            installButton.setEnabled(false);
            try {
                INSTANCE.install(log, versionPath.getText());
                installButton.setEnabled(true);
                versionPath.setEnabled(true);
            } catch (Throwable err) {
                log.append("\n" + err.getMessage());
                for (StackTraceElement element : err.getStackTrace()) {
                    log.append("\n" + element);
                }
                log.setForeground(Color.red);
                log.append("\nRestart installer");
                throw new RuntimeException(err);
            }
        });
        installPanel.setLocation(0, -100);
        installPanel.add(installButton);

        panel.add(textPanel);
        panel.add(installPanel);
        panel.add(logPanel);

        main.add(panel);
        main.pack();
        main.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        main.setLocationRelativeTo(null);
        main.setVisible(true);

        while (main.isVisible()) {
            while (installButton.isEnabled()) {
                String path = versionPath.getText();
                String versionNumber = new File(path).getName();
                installButton.setText("Install for " + versionNumber);
            }
        }
    }

    public void install(TextArea log, String versionPath) throws Exception {
        AtomicBoolean downloadedFromUrl = new AtomicBoolean(false);
        log.setText("");
        log.append("\nStart Installation");
        long start = System.nanoTime();
        Gson gson = new Gson();
        String versionNumber = new File(versionPath).getName();
        String versions = Utils.readUrl("https://launchermeta.mojang.com/mc/game/version_manifest.json");
        File flameInstaller = new File(getInstallerJar());
        File inputJar = new File(versionPath + File.separator + versionNumber + ".jar");
        File inputJson = new File(versionPath + File.separator + versionNumber + ".json");
        File outputFlameDir = new File(versionPath + "-flame");
        File outputJar = new File(outputFlameDir + File.separator + versionNumber + "-flame.jar");
        if (!inputJson.exists()) {
            log.setForeground(Color.yellow);
            log.append("\nWARN: No json found for version " + versionNumber + "! The installer will try to download it now.\nBe sure to have internet connection");
            Utils.MinecraftVersionMeta meta = gson.fromJson(versions, Utils.MinecraftVersionMeta.class);
            boolean found = false;
            for (Utils.MinecraftVersionMeta.Version version : meta.versions) {
                if (version.id.equals(versionNumber)) {
                    inputJson.getParentFile().mkdirs();
                    inputJson.createNewFile();
                    Utils.downloadFromUrl(version.url, inputJson.getPath());
                    found = true;
                    log.append("\nJson downloaded!");
                    log.setForeground(Color.black);
                    break;
                }
            }
            if (!found) {
                log.setForeground(Color.red);
                log.append("\nERROR:No " + versionNumber + " json found! VERSION NOT EXISTING!!!");
                throw new IOException("Version not existing.");
            }
        }
        if (!outputFlameDir.exists())
            outputFlameDir.mkdirs();
        if (!inputJar.exists()) {
            log.setForeground(Color.yellow);
            log.append("\nWARN:No " + versionNumber + " version jar found, but Json exists! The installer will try to download the jar from web.\nBe sure to have internet connection.");
            JsonParser parser = new JsonParser();
            JsonElement tree = parser.parse(Files.newBufferedReader(inputJson.toPath()));

            JsonObject downloads = Utils.readJsonObject(tree.getAsJsonObject(), s -> s.equals("downloads"));
            JsonObject client = Utils.readJsonObject(Objects.requireNonNull(downloads).getAsJsonObject(), s -> s.equals("client"));
            for (Map.Entry<String, JsonElement> clientEntry : Objects.requireNonNull(client).entrySet()) {
                if (clientEntry.getKey().equals("url")) {
                    Utils.downloadFromUrl(clientEntry.getValue().getAsString(), outputJar.getPath());
                    downloadedFromUrl.set(true);
                    log.setForeground(Color.black);
                    log.append("\nDownloaded jar.");
                    break;
                }
            }
        }

        File tmpDir = new File(outputFlameDir + File.separator + "tmp");
        File jsonOut = new File(outputFlameDir + File.separator + versionNumber + "-flame.json");

        Utils.deleteDirectory(tmpDir);
        tmpDir.mkdirs();

        if (!downloadedFromUrl.get() && outputJar.exists()) {
            outputJar.delete();
            outputJar.createNewFile();
        }

        if (downloadedFromUrl.get() || inputJar.length() == 0) {
            log.append("\nUnzipping flame...");
            Utils.unzip(tmpDir.getPath(), flameInstaller.getPath(), name -> (name.startsWith("tfc/") && name.endsWith(".class")));
            log.append("\nUnzipping finished");
        }

        if (!downloadedFromUrl.get()) {
            log.append("\nCopying Minecraft jar...");
            Files.copy(Files.newInputStream(inputJar.toPath()), outputJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
            log.append("\nMinecraft jar copied");
        }

        net.lingala.zip4j.ZipFile zipFile = new net.lingala.zip4j.ZipFile(outputJar);
        log.append("\nZipping FlameMC");
        File f = new File(tmpDir + File.separator + "tfc");
        f.mkdirs();
        zipFile.addFolder(f);
        log.append("\nZipping finished");

        if (!jsonOut.exists()) {
            log.append("\nWriting Json");
            Utils.FlamedJson launchJson = new Utils.FlamedJson(versionNumber + "-flame", versionNumber, "tfc.flamemc.FlameLauncher");
            launchJson.arguments.game = new ArrayList<>();
            String mavenUrl = "https://repo1.maven.org/maven2/";
            String asmRepo = "org.ow2.asm:asm";
            String asmVer = ":8.0.1";
            launchJson.libraries.add(new Utils.Library(asmRepo + asmVer, mavenUrl));
            launchJson.libraries.add(new Utils.Library(asmRepo + "-commons" + asmVer, mavenUrl));
            launchJson.libraries.add(new Utils.Library(asmRepo + "-tree" + asmVer, mavenUrl));
            launchJson.libraries.add(new Utils.Library(asmRepo + "-util" + asmVer, mavenUrl));
            //launchJson.libraries.add(new Library("org.apache.bcel:bcel:6.0", mavenUrl));
            Writer writer = Files.newBufferedWriter(jsonOut.toPath());
            JsonElement tree = gson.toJsonTree(launchJson);
            JsonWriter jsonWriter = new JsonWriter(writer);
            gson.toJson(tree, jsonWriter);
            log.append("\nJson written");
        } else {
            log.append("\nJson already generated");
        }

        Utils.deleteDirectory(tmpDir);
        if (downloadedFromUrl.get())
            Utils.deleteDirectory(inputJson.getParentFile());
        long stop = System.nanoTime();
        log.append("\nDone!\n");
        long timePassed = (stop - start) / 1000000;
        log.append("\nInstallation took " + timePassed + " milliseconds.");
        log.append("\nYou can install another version if you want now.");
    }
    
    public static File findVersionsDir() {
        return new File((FlameInstaller.isDev ? FlameInstaller.dir + File.separator + "run" : Utils.findMCDir()) + File.separator + "versions");
    }
}
