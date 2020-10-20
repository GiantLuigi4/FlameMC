package com.github.lorenzopapi;

import com.tfc.flamemc.FlameLauncher;
import net.lingala.zip4j.ZipFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public class FlameServer {

	public static void main(String[] args) throws IOException {
		mergeFlameWithMCServer();

		ProcessBuilder builder =
			new ProcessBuilder(
				"java", "-jar", "server\\server.jar",
				"--universe", "server",
				"--main_class", "net.minecraft.server.MinecraftServer",
				"--serverDev"
			);

		builder.redirectErrorStream(true);

		Process startServerProcess = builder.start();

		BufferedReader r = new BufferedReader(new InputStreamReader(startServerProcess.getInputStream()));
		String line;
		while ((line = r.readLine()) != null) {
			System.out.println(line);
		}
	}

	private static void mergeFlameWithMCServer() throws IOException {
		File jar = new File("server\\server.jar");
		File tmp = new File("server\\tmp");
		tmp.mkdirs();

		ZipFile zipFile = new ZipFile(jar);

		//FlameLauncher.downloadDepJustURL("https://repo1.maven.org/maven2/net/lingala/zip4j/zip4j/2.6.3/zip4j-2.6.3.jar");

		FlameLauncher.unzip(tmp.getPath() + "\\", "build\\libs\\FlameInstaller.jar", name -> (name.startsWith("com/tfc/") && name.endsWith(".class")));
		FlameLauncher.unzip(tmp.getPath() + "\\", jar.getPath(), name -> (name.startsWith("com") && !name.endsWith("/") && !name.endsWith(".class")));

		writeManifest(jar, tmp, zipFile);

		File com = new File(tmp + File.separator + "com");
		zipFile.addFolder(com);
		Files.walk(Paths.get(tmp.getPath()))
				.sorted(Comparator.reverseOrder())
				.map(Path::toFile)
				.forEach(File::delete);
	}

	private static void writeManifest(File jar, File tmp, ZipFile zipFile) throws IOException {
		FlameLauncher.unzip(tmp.getPath() + "\\", jar.getPath(), name -> (name.startsWith("META-INF/") && !name.endsWith("/")));
		File manifest = new File("server\\tmp\\META-INF\\MANIFEST.MF");
		manifest.delete();
		manifest.createNewFile();
		FileOutputStream writer = new FileOutputStream(manifest);
		writer.write(("Manifest-Version: 1.0\n" +
				              "Main-Class: com.tfc.flamemc.FlameLauncher\n").getBytes());
		writer.close();
		File meta = new File(tmp + "\\META-INF");
		zipFile.addFolder(meta);
	}
}
