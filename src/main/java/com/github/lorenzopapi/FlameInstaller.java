package com.github.lorenzopapi;

import org.json.JSONArray;
import org.json.JSONObject;
import tfc.flamemc.FlameUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

public class FlameInstaller {
    private static final FlameInstaller INSTANCE = new FlameInstaller();

    public String getInstallerJar() {
        URL urlJar = ClassLoader.getSystemResource(this.getClass().getName().replace(".", "/") + ".class");
        String urlStr = urlJar.toString();
        int from = "jar:file:".length();
        int to = urlStr.indexOf("!/");
        return urlStr.substring(from, to);
    }
    
    public static void main(String[] args) throws UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(UIManager.getLookAndFeel());
        String lastVersion = "1.20.1";
        JFrame main = new JFrame("FlameMC Installer");
        JPanel panel = new JPanel();

        JPanel textPanel = new JPanel();
        JTextField versionPath = new JTextField();
        textPanel.setSize(640, 320);
        versionPath.setSize(640, 320);
        versionPath.setText(FlameUtils.findVersionsDir() + File.separator + lastVersion);
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
        
        String versionNumber = new File(versionPath).getName();
        File inputJar = new File(versionPath + File.separator + versionNumber + ".jar");
        File inputJson = new File(versionPath + File.separator + versionNumber + ".json");
        File outputFlameDir = new File(versionPath + "-flame");
        File outputJar = new File(outputFlameDir + File.separator + versionNumber + "-flame.jar");
        if (!inputJson.exists()) {
            log.setForeground(Color.yellow);
            log.append("\nWARN: No json found for version " + versionNumber + "! The installer will try to download it now.\nBe sure to have internet connection");
            JSONObject versionsJSON = new JSONObject(FlameUtils.readUrl("https://launchermeta.mojang.com/mc/game/version_manifest.json"));
            boolean found = false;
            for (Object v : versionsJSON.getJSONArray("versions")) {
                if (((JSONObject) v).getString("id").equals(versionNumber)) {
                    inputJson.getParentFile().mkdirs();
                    inputJson.createNewFile();
                    FlameUtils.downloadFromUrl(((JSONObject) v).getString("url"), inputJson.getPath());
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
        if (!outputFlameDir.exists()) outputFlameDir.mkdirs();
        if (!inputJar.exists()) {
            log.setForeground(Color.yellow);
            log.append("\nWARN:No " + versionNumber + " version jar found, but Json exists! The installer will try to download the jar from web.\nBe sure to have internet connection.");
            JSONObject versionJSON = new JSONObject(FlameUtils.readUrl(inputJson.toURL().toString()));
            if (versionJSON.has("downloads") && versionJSON.getJSONObject("downloads").has("client")) {
                FlameUtils.downloadFromUrl(versionJSON.getJSONObject("downloads").getJSONObject("client").getString("url"), outputJar.getPath());
                downloadedFromUrl.set(true);
                log.setForeground(Color.black);
                log.append("\nDownloaded jar.");
            }
        }

        File tmpDir = new File(outputFlameDir + File.separator + "tmp");
        File jsonOut = new File(outputFlameDir + File.separator + versionNumber + "-flame.json");

        FlameUtils.deleteDirectory(tmpDir);
        tmpDir.mkdirs();

        if (!downloadedFromUrl.get()) {
            if (outputJar.exists()) {
                outputJar.delete();
                outputJar.createNewFile();
            }
            log.append("\nCopying Minecraft jar...");
            Files.copy(Files.newInputStream(inputJar.toPath()), outputJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
            log.append("\nMinecraft jar copied");
        }

        if (downloadedFromUrl.get() || inputJar.length() == 0) {
            log.append("\nUnzipping flame...");
            FlameUtils.unzip(tmpDir.getPath(), new File(getInstallerJar()).getPath(), name -> (name.startsWith("tfc/") && name.endsWith(".class")));
            log.append("\nUnzipping finished");
        }
        
        {
            net.lingala.zip4j.ZipFile zipFile = new net.lingala.zip4j.ZipFile(outputJar);
            log.append("\nZipping FlameMC");
            File f = new File(tmpDir + File.separator + "tfc");
            f.mkdirs();
            zipFile.addFolder(f);
            log.append("\nZipping finished");
        }
        
        if (!jsonOut.exists()) {
            log.append("\nWriting Json");
            JSONObject launchJSON = new JSONObject();
            String currentTime = OffsetDateTime.now().toString();
            String asmRepo = "org.ow2.asm:asm";
            String asmVer = ":9.5";
            String mavenUrl = "https://repo1.maven.org/maven2/" + asmRepo.replaceAll("[.:]", "/") + "%s" + asmVer.replace(":", "/") + "/" + asmRepo.split(":")[1] + "%s" + asmVer.replaceAll(":", "-") + ".jar";
            JSONArray libs = new JSONArray();
            addLibrary(libs, asmRepo + asmVer, String.format(mavenUrl, "", ""));
            addLibrary(libs, asmRepo + "-commons" + asmVer, String.format(mavenUrl, "-commons", "-commons"));
            addLibrary(libs, asmRepo + "-tree" + asmVer, String.format(mavenUrl, "-tree", "-tree"));
            addLibrary(libs, asmRepo + "-util" + asmVer, String.format(mavenUrl, "-util", "-util"));
            launchJSON
                    .put("id", versionNumber + "-flame")
                    .put("inheritsFrom", versionNumber)
                    .put("mainClass", "tfc.flamemc.FlameLauncher")
                    .put("time", currentTime)
                    .put("releaseTime", currentTime)
                    .put("type", "release")
                    .put("libraries", libs);
            Writer writer = Files.newBufferedWriter(jsonOut.toPath());
            writer.write(launchJSON.toString());
            writer.close();
            log.append("\nJson written");
        } else log.append("\nJson already generated");

        FlameUtils.deleteDirectory(tmpDir);
        if (downloadedFromUrl.get()) FlameUtils.deleteDirectory(inputJson.getParentFile());
        long stop = System.nanoTime();
        log.append("\nDone!\n");
        long timePassed = (stop - start) / 1000000;
        log.append("\nInstallation took " + timePassed + " milliseconds.");
        log.append("\nYou can install another version if you want now.");
    }
    
    public static void addLibrary(JSONArray libs, String name, String url) {
        libs.put(new JSONObject().put("name", name).put("downloads", new JSONObject().put("artifact", new JSONObject().put("url", url))));
    }
}
