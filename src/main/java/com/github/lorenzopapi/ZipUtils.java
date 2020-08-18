package com.github.lorenzopapi;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

	private static String SOURCE_FOLDER = null;
	private final List<String> flameClasses;
	byte[] BUFFER = new byte[4096];


	public ZipUtils(String source) {
		SOURCE_FOLDER = source;
		flameClasses = new ArrayList<>();
	}

	public void generateFileList(File node) {
		if (node.isFile()) {
			String entry = generateJarEntry(node.toString());
			flameClasses.add(entry);
		}
		if (node.isDirectory()) {
			String[] subNote = node.list();
			for (String filename : subNote) {
				generateFileList(new File(node, filename));
			}
		}
	}

	private String generateJarEntry(String file) {
		return file.substring(SOURCE_FOLDER.length() + 1);
	}

	public void unZip(String zipFilePath, File dest) throws IOException {
		ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath));
		ZipEntry zipEntry = zis.getNextEntry();
		while (zipEntry != null) {
			File newFile = newFile(dest, zipEntry);
			if (!newFile.exists()) {
				newFile.getParentFile().mkdirs();
				newFile.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(newFile);
			readAndCopy(zis, fos, true);
			fos.close();
			zipEntry = zis.getNextEntry();
		}
		zis.closeEntry();
		zis.close();
	}

	public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
		File destFile = new File(destinationDir, zipEntry.getName());
		String destDirPath = destinationDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();
		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
		}
		return destFile;
	}

	public void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
		if (fileToZip.isDirectory()) {
			if (fileName.endsWith("/")) {
				zipOut.putNextEntry(new ZipEntry(fileName));
			} else {
				zipOut.putNextEntry(new ZipEntry(fileName + "/"));
			}
			zipOut.closeEntry();
			File[] children = fileToZip.listFiles();
			for (File childFile : children) {
				zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
			}
			return;
		}
		FileInputStream fis = new FileInputStream(fileToZip);
		ZipEntry zipEntry = new ZipEntry(fileName);
		zipOut.putNextEntry(zipEntry);
		readAndCopy(fis, zipOut, true);
		fis.close();
	}

	public void extractFile(JarInputStream zipIn, File file) throws IOException {
		FileOutputStream bos = new FileOutputStream(file, true);
		readAndCopy(zipIn, bos, true);
		bos.close();
	}

	public void copy(File source, File dest) throws IOException {
		FileInputStream is = new FileInputStream(source);
		FileOutputStream os = new FileOutputStream(dest);
		readAndCopy(is, os, false);
	}

	public void readAndCopy(InputStream is, OutputStream os, boolean isJar) throws IOException {
		int length;
		while ((length = is.read(BUFFER)) > 0) {
			os.write(BUFFER, 0, length);
		}
		if (!isJar) {
			is.close();
			os.close();
		}
	}

	public void addEntry(File source) throws IOException {
		File tmpZip = File.createTempFile(source.getName(), null);
		tmpZip.delete();
		if(!source.renameTo(tmpZip)) {
			throw new RuntimeException("Could not make temp file (" + source.getName() + ")");
		}
		ZipInputStream zin = new ZipInputStream(new FileInputStream(tmpZip));
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(source));

		for (String file : flameClasses) {
			InputStream in = new FileInputStream(SOURCE_FOLDER + "\\" + file);
			out.putNextEntry(new ZipEntry(file));
			readAndCopy(in, out, true);
			out.closeEntry();
			in.close();
		}
		for(ZipEntry ze = zin.getNextEntry(); ze != null; ze = zin.getNextEntry()) {
			out.putNextEntry(ze);
			readAndCopy(zin, out, true);
			out.closeEntry();
		}
		out.close();
		tmpZip.delete();
	}

	public void zipDirectory() {

	}

	//https://stackoverflow.com/questions/5244963/delete-files-from-a-zip-archive-without-decompressing-in-java-or-maybe-python
	public void removeEntry(File zipFile, TextArea log) throws IOException {
		File tempFile = File.createTempFile(zipFile.getName(), null);
		tempFile.delete();
		tempFile.deleteOnExit();
		boolean renameOk = zipFile.renameTo(tempFile);
		if (!renameOk) {
			throw new RuntimeException("could not rename the file "+zipFile.getAbsolutePath()+" to "+tempFile.getAbsolutePath());
		}
		ZipInputStream zin = new ZipInputStream(new FileInputStream(tempFile));
		ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zipFile));

		ZipEntry entry = zin.getNextEntry();
		while (entry != null) {
			String name = entry.getName();
			boolean toBeDeleted = false;
			if (name.contains("com/m")) {
				toBeDeleted = true;
			}
			if (!toBeDeleted) {
				zout.putNextEntry(new ZipEntry(name));
				readAndCopy(zin, zout, true);
			}
			entry = zin.getNextEntry();
		}
		zin.close();
		zout.close();
		tempFile.delete();
	}
}
