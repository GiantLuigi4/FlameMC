package com.github.lorenzopapi;

import com.tfc.flamemc.FlameLauncher;
import com.tfc.utils.flame.dependency_management.Manager;
import net.lingala.zip4j.ZipFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class FlameServer {

	public static void main(String[] args) throws IOException {

		new File("server\\tmp\\").mkdirs();
		ZipFile zipFile = new ZipFile("server\\server.jar");

		FlameLauncher.unzip("server\\tmp\\", "build\\libs\\FlameInstaller.jar", name -> (name.startsWith("com/tfc/") && name.endsWith(".class")) || name.equals("META-INF/MANIFEST.MF"));

		//FlameLauncher.downloadDepJustURL("https://repo1.maven.org/maven2/net/lingala/zip4j/zip4j/2.6.3/zip4j-2.6.3.jar");

		zipFile.addFolder(new File("server\\tmp\\com\\"));

		ProcessBuilder builder =
			new ProcessBuilder(
				"java", "-jar", "server\\server.jar",
				"--main_class", "net.minecraft.server.MinecraftServer",
				"--serverDev",
				"--universe", "server"
			);

		builder.redirectErrorStream(true);

		Process startServerProcess = builder.start();

		StringBuilder output = new StringBuilder();

		BufferedReader r = new BufferedReader(new InputStreamReader(startServerProcess.getInputStream()));
		String line;
		while ((line = r.readLine()) != null) {
			output.append(line).append("\n");
		}

		System.out.println(output);
	}
}
