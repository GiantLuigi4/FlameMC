import java.io.*;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class FlameInstaller {
	private static final String dir = System.getProperty("user.dir");
	
	public static void main(String[] args) {
		try {
			JarOutputStream stream = new JarOutputStream(new FileOutputStream(new File(dir+"\\working\\1.15.2-flame.jar")));
			JarFile file = new JarFile(new File(dir+"\\input_version\\fabric-loader-0.9.1+build.205-1.16.1.jar"));
			Enumeration<JarEntry> entries = file.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				stream.putNextEntry(new JarEntry(entry.getName()));
				InputStream instream = file.getInputStream(entry);
				byte[] bytes = new byte[(int)entry.getSize()];
				instream.read(bytes);
				stream.write(bytes);
				instream.close();
			}
			stream.putNextEntry(new JarEntry("com/tfc/flamemc/FlameLauncher.class"));
			FileInputStream inStream1 = new FileInputStream(dir+"\\contents\\FlameLauncher.class");
			byte[] bytes = new byte[inStream1.available()];
			inStream1.read(bytes);
			stream.write(bytes);
			inStream1.close();
			
			stream.putNextEntry(new JarEntry("com/tfc/flamemc/FlameLoader.class"));
			inStream1 = new FileInputStream(dir+"\\contents\\FlameLoader.class");
			bytes = new byte[inStream1.available()];
			stream.write(bytes);
			inStream1.close();
			stream.close();
			
			stream.putNextEntry(new JarEntry("org/apache/bcel/util/ClassPath.class"));
			inStream1 = new FileInputStream(dir+"\\contents\\ClassPath.class");
			bytes = new byte[inStream1.available()];
			stream.write(bytes);
			inStream1.close();
			stream.close();
		} catch (Throwable err) {
			err.printStackTrace();
		}
	}
}
