package com.tfc.flame;

import java.lang.reflect.InvocationTargetException;

public class FlameConfig {
	public static boolean log_bytecode = false;
	public static boolean log_classnames = false;
	
	public static FlameLog field = null;
	
	public static void logError(Throwable err) {
		StringBuilder s = new StringBuilder();
		field.append("\n\n");
		s.append("Flame encountered an error:\n");
		s.append(err.getClass().getName()).append(": ").append(err.getLocalizedMessage()).append("\n");
		for (StackTraceElement element : err.getStackTrace()) {
			s.append(element.toString()).append("\n");
		}
		System.out.println(s.toString());
		if (err instanceof InvocationTargetException) FlameConfig.logError(err.getCause());
//		try{Thread.sleep(2000);}catch(Throwable err2){}
		field.append(s.toString());
		err.getStackTrace();
	}
}
