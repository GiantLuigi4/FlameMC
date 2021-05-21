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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FlameInstaller {

    private static final FlameInstaller INSTANCE = new FlameInstaller();

    public String getInstallerJar() {
        URL urlJar = this.getClass().getClassLoader().getSystemResource(this.getClass().getName().replace(".", "/") + ".class");
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
        String versions = readUrl("https://launchermeta.mojang.com/mc/game/version_manifest.json");
        File flameInstaller = new File(getInstallerJar());
        File inputJar = new File(versionPath + File.separator + versionNumber + ".jar");
        File inputJson = new File(versionPath + File.separator + versionNumber + ".json");
        File outputFlameDir = new File(versionPath + "-flame");
        File outputJar = new File(outputFlameDir + File.separator + versionNumber + "-flame.jar");
        if (!inputJson.exists()) {
            log.setForeground(Color.yellow);
            log.append("\nWARN: No json found for version " + versionNumber + "! The installer will try to download it now.\nBe sure to have internet connection");
            MinecraftVersionMeta meta = gson.fromJson(versions, MinecraftVersionMeta.class);
            boolean found = false;
            for (MinecraftVersionMeta.Version version : meta.versions) {
                if (version.id.equals(versionNumber)) {
                    inputJson.getParentFile().mkdirs();
                    inputJson.createNewFile();
                    downloadFromUrl(version.url, inputJson.getPath());
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

            JsonObject downloads = readJsonObject(tree.getAsJsonObject(), s -> s.equals("downloads"));
            JsonObject client = readJsonObject(Objects.requireNonNull(downloads).getAsJsonObject(), s -> s.equals("client"));
            for (Map.Entry<String, JsonElement> clientEntry : Objects.requireNonNull(client).entrySet()) {
                if (clientEntry.getKey().equals("url")) {
                    downloadFromUrl(clientEntry.getValue().getAsString(), outputJar.getPath());
                    downloadedFromUrl.set(true);
                    log.setForeground(Color.black);
                    log.append("\nDownloaded jar.");
                    break;
                }
            }
        }

        File tmpDir = new File(outputFlameDir + File.separator + "tmp");
        File jsonOut = new File(outputFlameDir + File.separator + versionNumber + "-flame.json");

        deleteDirectory(tmpDir);
        tmpDir.mkdirs();

        if (!downloadedFromUrl.get() && outputJar.exists()) {
            outputJar.delete();
            outputJar.createNewFile();
        }

        if (downloadedFromUrl.get() || inputJar.length() == 0) {
            log.append("\nUnzipping flame...");
            unzip(tmpDir.getPath(), flameInstaller.getPath(), name -> (name.startsWith("tfc/") && name.endsWith(".class")));
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
            FlamedJson launchJson = new FlamedJson(versionNumber + "-flame", versionNumber, "tfc.flamemc.FlameLauncher");
            launchJson.arguments.game = new ArrayList<>();
            String mavenUrl = "https://repo1.maven.org/maven2/";
            String asmRepo = "org.ow2.asm:asm";
            String asmVer = ":8.0.1";
            launchJson.libraries.add(new Library(asmRepo + asmVer, mavenUrl));
            launchJson.libraries.add(new Library(asmRepo + "-commons" + asmVer, mavenUrl));
            launchJson.libraries.add(new Library(asmRepo + "-tree" + asmVer, mavenUrl));
            launchJson.libraries.add(new Library(asmRepo + "-util" + asmVer, mavenUrl));
            //launchJson.libraries.add(new Library("org.apache.bcel:bcel:6.0", mavenUrl));
            Writer writer = Files.newBufferedWriter(jsonOut.toPath());
            JsonElement tree = gson.toJsonTree(launchJson);
            JsonWriter jsonWriter = new JsonWriter(writer);
            gson.toJson(tree, jsonWriter);
            log.append("\nJson written");
        } else {
            log.append("\nJson already generated");
        }

        deleteDirectory(tmpDir);
        if (downloadedFromUrl.get())
            deleteDirectory(inputJson.getParentFile());
        long stop = System.nanoTime();
        log.append("\nDone!\n");
        long timePassed = (stop - start) / 1000000;
        log.append("\nInstallation took " + timePassed + " milliseconds.");
        log.append("\nYou can install another version if you want now.");
    }

    public static File findVersionsDir() {
        String home = System.getProperty("user.home", ".");
        String os = System.getProperty("os.name").toLowerCase();
        String mcDir;
        if (!isDev) {
            if (os.contains("win") && System.getenv("APPDATA") != null) {
                mcDir = System.getenv("APPDATA") + "\\.minecraft";
            } else if (os.contains("mac")) {
                mcDir = home + "/Library/Application Support/minecraft";
            } else {
                mcDir = home + "/.minecraft";
            }
        } else {
            mcDir = dir + File.separator + "run";
        }
        return new File(mcDir + File.separator + "versions");
    }

    public static String readUrl(String urlString) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(urlString).openStream()));
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);
            return buffer.toString();
        } catch (Throwable err) {
            err.printStackTrace();
        }
        throw new RuntimeException("bad url");
    }

    public static void unzip(String targetDir, String zipFilename, Function<String, Boolean> fileV) {
        Path targetDirPath = Paths.get(targetDir);
        try (ZipFile zipFile = new ZipFile(zipFilename)) {
            zipFile.stream()
                    .parallel()
                    .forEach(e -> unzipEntry(zipFile, e, targetDirPath, fileV));
        } catch (IOException e) {
            throw new RuntimeException("Error opening zip file '" + zipFilename + "': " + e, e);
        }
    }

    private static void unzipEntry(ZipFile zipFile, ZipEntry entry, Path targetDir, Function<String, Boolean> fileV) {
        try {
            Path targetPath = targetDir.resolve(Paths.get(entry.getName()));
            if (fileV.apply(entry.getName())) {
                if (Files.isDirectory(targetPath)) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.createDirectories(targetPath.getParent());
                    try (InputStream in = zipFile.getInputStream(entry)) {
                        Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing zip entry '" + entry.getName() + "': " + e, e);
        }
    }

    public static void downloadFromUrl(String url, String downloadFile) throws IOException {
        File f = new File(downloadFile);
        if (!f.exists()) {
            f.getParentFile().mkdirs();
            f.createNewFile();
        }
        try (BufferedInputStream inputStream = new BufferedInputStream(new URL(url).openStream())) {
            FileOutputStream fileOS = new FileOutputStream(downloadFile);
            byte[] data = new byte[1024];
            int byteContent;
            while ((byteContent = inputStream.read(data, 0, 1024)) != -1) {
                fileOS.write(data, 0, byteContent);
            }
        }
    }

    public static JsonObject readJsonObject(JsonObject object, Function<String, Boolean> validator) {
        for (Map.Entry<String, JsonElement> jsonEntry : object.entrySet()) {
            if (validator.apply(jsonEntry.getKey())) {
                return jsonEntry.getValue().getAsJsonObject();
            }
        }
        return null;
    }

    public static void deleteDirectory(File f) throws IOException {
        if (f.exists())
            Files.walk(f.toPath())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    public static class MinecraftVersionMeta {
        public List<Version> versions;

        public static class Version {
            public String id;
            public String url;
        }
    }

    public static class FlamedJson {
        public String id;
        public String inheritsFrom;
        public String type = "release";
        public String mainClass;
        public Arguments arguments = new Arguments();
        public Downloads downloads = new Downloads();
        public List<Library> libraries = new ArrayList<>();

        public FlamedJson(String id, String inheritsFrom, String mainClass) {
            this.id = id;
            this.inheritsFrom = inheritsFrom;
            this.mainClass = mainClass;
        }
    }

    public static class Library {
        public String name;
        public String url;

        public Library(String name, String url) {
            this.name = name;
            this.url = url;
        }
    }

    public static class Arguments {
        public List<String> game = new ArrayList<>();
    }

    public static class Downloads {}
}
