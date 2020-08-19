package com.github.lorenzopapi;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

	byte[] BUFFER = new byte[4096];

	public ZipUtils() {}

	public void unZip(String zipFilePath, File dest) throws IOException {
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFilePath)));
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

	public void extractFile(JarInputStream zipIn, File file) throws IOException {
		FileOutputStream bos = new FileOutputStream(file, true);
		readAndCopy(zipIn, bos, true);
		bos.close();
	}

	public void readAndCopy(InputStream is, OutputStream os, boolean isJar) throws IOException {
		int length;
		while ((length = is.read(BUFFER)) != -1) {
			os.write(BUFFER, 0, length);
		}
		if (!isJar) {
			is.close();
			os.close();
		}
	}

	public void zip(List<File> listFiles, String destZipFile) throws IOException {
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destZipFile));
		for (File file : listFiles) {
			if (file.isDirectory()) {
				zipDirectory(file, file.getName(), zos);
			} else {
				zipFile(file, zos);
			}
		}
		zos.flush();
		zos.close();
	}

	private void zipDirectory(File folder, String parentFolder, ZipOutputStream zos) throws IOException {
		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				zipDirectory(file, parentFolder + "/" + file.getName(), zos);
				continue;
			}
			zos.putNextEntry(new ZipEntry(parentFolder + "/" + file.getName()));
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			readAndCopy(bis, zos, true);
			zos.closeEntry();
		}
	}

	public void zip(File[] files, String destZipFile) throws IOException {
		List<File> listFiles = new ArrayList<>(Arrays.asList(files));
		zip(listFiles, destZipFile);
	}

	private void zipFile(File file, ZipOutputStream zos) throws IOException {
		zos.putNextEntry(new ZipEntry(file.getName()));
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		readAndCopy(bis, zos, true);
		zos.closeEntry();
	}
}
