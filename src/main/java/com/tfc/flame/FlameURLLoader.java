package com.tfc.flame;

import org.apache.bcel.util.ClassPath;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class FlameURLLoader extends URLClassLoader {
	public FlameURLLoader(URL[] urls) {
		super(urls);
	}
	
	public Class<?> load(String name,boolean resolve) throws ClassNotFoundException {
		return loadClass(name,resolve);
	}
	
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return this.loadClass(name,true);
	}
	
	private final HashMap<String,byte[]> merges = new HashMap<>();
	private final HashMap<String,byte[]> replacements = new HashMap<>();
	
	public void findReplacement(String name) {
		String name1 = "";
		try {
			name1 = name.replace("merges.","").replace("replacements.","");
			name1 = name1.substring(name1.indexOf('.')+1);
		} catch (Throwable ignored) {}
		try {
			InputStream stream = this.getResourceAsStream(name);
			assert stream!=null;
			byte[] bytes1 = new byte[stream.available()];
			stream.read(bytes1);
			stream.close();
			if (name.startsWith("merges.")) {
				if (!merges.containsKey(name)) {
					merges.put(name1,bytes1);
					FlameConfig.field.append("Found merge for class: "+name1);
				} else {
					merges.replace(name1.replace("merges.",""),merge(bytes1,merges.get(name1)));
				}
			} else if (name.startsWith("replacements.")) {
				FlameConfig.field.append("Found replacement for class: "+name1+"\n");
				replacements.putIfAbsent(name1,bytes1);
			}
		} catch (Throwable ignored) {}
	}
	
	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		if (name.startsWith("com.tfc.flame")) {
			try {
				return this.getParent().loadClass(name);
			} catch (Throwable err) {
				throw new SecurityException("Tried to load class in invalid namespace: \"com.tfc.flame\"");
			}
		}
		synchronized(this.getClassLoadingLock(name)) {
			if (FlameConfig.log_classnames) FlameConfig.field.append(name+"\n");
			Class<?> c = this.findLoadedClass(name);
			if (c == null) {
//				long t0 = System.nanoTime();
				try {
					byte[] bytes1 = null;
					for (URL url : this.getURLs()) {
						if (bytes1==null) {
							try {
								bytes1 = new ClassPath(url.getPath()).getBytes(name);
							} catch (Throwable ignored) {
								try {
									InputStream stream = this.getResourceAsStream(name);
									assert stream!=null;
									bytes1 = new byte[stream.available()];
									stream.read(bytes1);
									stream.close();
								} catch (Throwable ignored1) {}
							}
						}
					}
//					for (URL url : this.getURLs()) {
//						String modid = new File(url.getFile()).getName().replace(".jar","").replace(".zip","");
//						try {
//							byte[] bytes = new ClassPath(url.getFile()).getBytes("merges."+modid+"."+name);
//							assert bytes1 != null;
//							bytes1 = merge(bytes1,bytes);
//							FlameConfig.field.append("Merging class: "+name+" with modded versions of said class.\n");
//							FlameConfig.field.append("Things might go wrong.\n");
//						} catch (Throwable ignored) {
//							try {
//							} catch (Throwable ignored1) {
////								FlameConfig.logError(ignored);
//							}
//						}
//					}
//					try {
//						InputStream stream = this.getResourceAsStream("merges."+name);
//						byte[] bytes2 = new byte[stream.available()];
//						stream.read(bytes2);
//						FlameConfig.field.append(Arrays.toString(bytes2)+"\n");
//						FlameConfig.field.append("Merging class: "+name+" with modded versions of said class.\n");
//						FlameConfig.field.append("Things might go wrong.\n");
//						bytes1 = merge(bytes1,bytes2);
//						stream.close();
//					} catch (Throwable err) {}
					if (replacements.containsKey(name)) {
						bytes1 = replacements.get(name);
					} else if (bytes1!=null&&merges.containsKey(name)) {
						FlameConfig.field.append("Merging class: "+name+" with modded versions of said class.\n");
						FlameConfig.field.append("Things might go wrong.\n");
						bytes1 = merge(bytes1,merges.get(name));
					}
					if (FlameConfig.log_bytecode) FlameConfig.field.append(Arrays.toString(bytes1)+"\n");
					//Define if possible
					if (bytes1 != null) c = this.defineClass(bytes1,0,bytes1.length);
					//Load from parent
					if (c==null&&this.getParent() != null) c = this.getParent().loadClass(name);
				} catch (ClassNotFoundException err) {
					FlameConfig.logError(err);
				}
				
				if (c == null) {
//					long t1 = System.nanoTime();
					c = this.findClass(name);
//					PerfCounter.getParentDelegationTime().addTime(t1 - t0);
//					PerfCounter.getFindClassTime().addElapsedTimeFrom(t1);
//					PerfCounter.getFindClasses().increment();
				}
			}
			
			if (resolve) {
				this.resolveClass(c);
			}
			
			return c;
		}
	}
	
	private Class<?> defineFromStream(String name, InputStream stream1) throws Throwable {
		if (stream1 != null) {
			byte[] bytes1 = new byte[stream1.available()];
			stream1.read(bytes1);
			if (FlameConfig.log_bytecode) {
				StringBuilder bytecode = new StringBuilder();
				for (byte b : bytes1) bytecode.append(b).append(" ");
				FlameConfig.field.append("Bytecode:" + bytecode.toString() + "\n");
			}
			stream1.close();
			return this.defineClass(bytes1,0,bytes1.length);
		}
		return null;
	}
	
	private byte[] merge(byte[] source, byte[] to_merge) {
		int char_source = 0;
		int char_merge = 0;
		ArrayList<Byte> newBytes = new ArrayList<>();
		while (char_source < source.length && char_merge < to_merge.length) {
			boolean added = false;
			newBytes.add(source[char_source]);
			if (source[char_source] != to_merge[char_merge]) {
				added = true;
				newBytes.add(to_merge[char_merge]);
			} else {
				newBytes.add(to_merge[char_source]);
			}
			char_merge++;
			if (!added) {
				char_source++;
			}
		}
		byte[] newBytesReturn = new byte[newBytes.size()];
		for (int i = 0; i < newBytes.size(); i++) {
			newBytesReturn[i] = newBytes.get(i);
		}
		return newBytesReturn;
	}
}
