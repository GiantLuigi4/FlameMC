package com.tfc.flame;

//import jdk.internal.perf.PerfCounter;
import org.apache.bcel.util.ClassPath;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;

public class FlameURLLoader extends URLClassLoader {
	public FlameURLLoader(URL[] urls) {
		super(urls);
	}
	
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return this.loadClass(name,true);
	}
	
	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		synchronized(this.getClassLoadingLock(name)) {
			if (FlameConfig.log_classnames) FlameConfig.field.append(name+"\n");
			Class<?> c = this.findLoadedClass(name);
			if (c == null) {
				long t0 = System.nanoTime();
				
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
					for (URL url : this.getURLs()) {
						try {
							byte[] bytes = new ClassPath(url.getPath()).getBytes("merges."+name);
							bytes1 = merge(bytes1,bytes);
						} catch (Throwable ignored) {
							try {
								InputStream stream = this.getResourceAsStream(name);
								assert stream!=null;
								byte[] bytes2 = new byte[stream.available()];
								stream.read(bytes2);
								stream.close();
							} catch (Throwable ignored1) {}
						}
					}
					if (FlameConfig.log_bytecode) {
						FlameConfig.field.append(Arrays.toString(bytes1)+"\n");
					}
					if (bytes1 != null) {
						c = this.defineClass(bytes1,0,bytes1.length);
					}
					if (c==null) {
						if (this.getParent() != null) {
							c = this.getParent().loadClass(name);
						}
					}
				} catch (ClassNotFoundException err) {
					FlameConfig.logError(err);
				}
				
				if (c == null) {
					long t1 = System.nanoTime();
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
		while (char_source <= source.length) {
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
