package com.github.lorenzopapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class FlameServer {

	public static void main(String[] args) throws IOException {
		ProcessBuilder builder = new ProcessBuilder(
				"java", "-jar", "server\\server.jar", "--main_class", "net.minecraft.server.MinecraftServer");
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
