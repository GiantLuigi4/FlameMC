package com.github.lorenzopapi;

import com.tfc.flame.FlameConfig;
import com.tfc.flame.FlameLog;
import com.tfc.flamemc.FlameLauncher;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FlameInstaller {

	static FlameLog field = new FlameLog();

	public static void main(String[] args) {
		AtomicBoolean ready = new AtomicBoolean(false);

		JFrame frame = new JFrame("Flame Loader Installer");
		FlameConfig.field = field;

		if (!(System.getProperty("os.name").contains("Windows"))) {
			frame.setTitle("Incompatible!");
			JTextField error = new JTextField();
			error.setText("OS is not compatible (for now)");
			JButton exit = new JButton();
			exit.addActionListener(e -> System.exit(1));
		}
		JPanel panel = new JPanel();

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel1 = new JPanel();
		panel1.setSize(640, 320);
		JTextField field = new JTextField();
		field.setSize(640, 320);
		field.setText(System.getenv("APPDATA") + "\\.minecraft\\versions\\1.16.2");
		panel.add(field);

		JPanel panel2 = new JPanel();
		JButton button = new JButton("Install for 1.16.2");
		button.addActionListener(e -> ready.set(true));
		panel2.setLocation(50, 0);
		panel2.add(button);

		JPanel panel3 = new JPanel();
		panel3.setSize(640, 320);
		panel3.setMinimumSize(panel3.getSize());
		panel3.setMaximumSize(panel3.getSize());
		TextArea field2 = new TextArea();
		panel3.add(field2);

		panel.add(panel1);
		panel.add(panel2);
		panel.add(panel3);

		frame.add(panel);
		frame.pack();

		frame.setVisible(true);
		while (frame.isVisible()) {
			while (!ready.get());

			ready.set(false);

			try  {
				field2.append("Start Installation");
				String text = field.getText();
				File path = new File(text);
				File inputFile = new File(text + "\\" + path.getName() + ".jar");
				File outputDir = new File(text + "-flame");
				File outputFile = new File(outputDir + "\\" + path.getName() + "-flame.jar");
				if (!outputDir.exists()) {
					outputFile.getParentFile().mkdirs();
					outputFile.createNewFile();
					copy(inputFile, outputFile);

				}
				//copy(flameFile, outputFile);
//				JarOutputStream outputStream = new JarOutputStream(new FileOutputStream(outputFile));
//				Enumeration<JarEntry> entries = inputJarFile.entries();
//				while (entries.hasMoreElements()) {
//					JarEntry entry = entries.nextElement();
//					//if (entry.getName().endsWith(".class") || entry.getName().startsWith("data") || entry.getName().startsWith("assets")) {
//						field2.append(entry.getName() + "\n");
//						outputStream.putNextEntry(new JarEntry(entry.getName()));
//						InputStream iStream = inputJarFile.getInputStream(entry);
//						byte[] bytes = new byte[(int) entry.getSize()];
//						iStream.read(bytes);
//						outputStream.write(bytes);
//						iStream.close();
//					//}
//				}
				File flameFile = new File(FlameLauncher.getDir() + "\\build\\libs\\FlameInstaller.jar");
				File tmpDir = new File(outputDir + "\\tmp");
				if (!tmpDir.exists())
					tmpDir.getParentFile().mkdirs();
				JarInputStream jarIn = new JarInputStream(new FileInputStream(flameFile));
				JarEntry entry = jarIn.getNextJarEntry();
				while (entry != null) {
					String name = entry.getName();
					if (name.startsWith("com/tfc") && name.endsWith(".class") && !name.contains("FlameLoader")) {
						File destFile = new File(tmpDir, name);
						if (!destFile.exists()) {
							destFile.getParentFile().mkdirs();
							destFile.createNewFile();
						}
						extractFile(jarIn, destFile);
						field2.append("\nAdding File: " + name);
					}
					entry = jarIn.getNextJarEntry();
				}
				jarIn.closeEntry();

				//still to understand how to copy flame classes
				//File flameTmpFolder = new File(tmpDir + "\\com");
				//JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(flameTmpFolder + ".jar"));
				//zipFile(flameTmpFolder, flameTmpFolder.getName(), jarOut);
				/*

				while (flame.entries().hasMoreElements()) {
					JarEntry entry = inputJarFile.entries().nextElement();
					String name = entry.getName();
					if (name.startsWith("com/tfc") && name.endsWith(".class") && !name.contains("FlameLoader")) {
						extractFile(name);
					}

				}
				 */

				/*for (String s : needed) {
					outputStream.putNextEntry(new JarEntry(s));
					field2.append(field2.getText() + "\nCreate File: " + s);
					InputStream inputStream = flame.getInputStream(flame.getEntry(s));
					byte[] bytes = new byte[inputStream.available()];
					inputStream.read(bytes);
					outputStream.write(bytes);
					inputStream.close();
				}

				flame.close();

				/*File jsonIn = new File(text + "-\\" + path.getName() + ".json");
				if (!jsonIn.exists()) throw new FileNotFoundException("No 1.16.2 json found! You must run the version alone before running FlameInstaller");

				String parsed = JSONParser.quote(String.valueOf(Files.readAllLines(Paths.get(jsonIn.getPath()))));
				File jsonOut = new File(text + "-flame\\" + path.getName() + "-flame.json");
				if (!jsonOut.exists()) jsonOut.createNewFile();
				FileWriter writer = new FileWriter(jsonOut);
				writer.write(parsed);
				writer.close();

				outputStream.close();*/
				field2.append("\nDone!");
			} catch (Throwable err) {
				FlameConfig.logError(err);
				throw new RuntimeException(err);
			}
		}
	}

	private static void zipFile(File fileToZip, String fileName, JarOutputStream jarOut) throws IOException {
		if (fileToZip.isDirectory()) {
			if (fileName.endsWith("\\")) {
				jarOut.putNextEntry(new JarEntry(fileName));
			} else {
				jarOut.putNextEntry(new JarEntry(fileName + "\\"));
			}
			jarOut.closeEntry();
			File[] children = fileToZip.listFiles();
			for (File child : children) {
				zipFile(child, fileName + "\\" + child.getName(), jarOut);
			}
			return;
		}
		FileInputStream fis = new FileInputStream(fileToZip);
		JarEntry jarEntry = new JarEntry(fileName);
		jarOut.putNextEntry(jarEntry);
		byte[] bytesIn = new byte[1024];
		int read;
		while ((read = fis.read(bytesIn)) >= 0) {
			jarOut.write(bytesIn, 0, read);
		}
		fis.close();
	}

	private static void extractFile(JarInputStream zipIn, File file) throws IOException {
		OutputStream bos = new FileOutputStream(file, true);
		byte[] bytesIn = new byte[1024];
		int read;
		while ((read = zipIn.read(bytesIn)) != -1) {
			bos.write(bytesIn, 0, read);
		}
		bos.close();
	}

	private static void copy(File source, File dest) throws IOException {
		InputStream is = new FileInputStream(source);
		OutputStream os = new FileOutputStream(dest);
		byte[] buffer = new byte[1024];
		int length;
		while ((length = is.read(buffer)) > 0) {
			os.write(buffer, 0, length);
		}
		is.close();
		os.close();
	}
}
