package tfc.flamemc;

import java.awt.*;

public class FlameConfig {
	public static TextArea field = null;
	public static long nanoTime = System.nanoTime();
	
	public static void logError(Throwable err) {
		StringBuilder s = new StringBuilder();
		FlameLauncher.field.append("\n\n");
		s.append("Flame encountered an error:\n");
		s.append(err.getClass().getName()).append(": ").append(err.getLocalizedMessage()).append("\n");
		for (StackTraceElement element : err.getStackTrace()) {
			s.append(element.toString()).append("\n");
		}
		FlameLauncher.field.append(s.toString());
		err.getStackTrace();
	}
}
