package com.tfc.flamemc;

import org.apache.bcel.util.ClassPath;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

//MMD Discord:https://discord.mcmoddev.com/
//Message:https://discordapp.com/channels/176780432371744769/421377435041267712/723593029583241336
//Most stuff I know about class loaders is from fabric.
public class FlameLoader extends ClassLoader {
	private Class<?> define(String name, byte[] bytes) {
		try {
			return this.defineClass(name, bytes,0,bytes.length);
		} catch (Throwable err) {
			return this.trimAndDefine(name, bytes);
		}
	}
	
	private HashMap<String, Class> classes = new HashMap<>();
	
	private static final String dir = System.getProperty("user.dir");
	
	private String path = dir + "\\flame_mods";
	
	//USE WITH CAUTION
	public void setPath(String path) {
		this.path = path;
		File file = new File(path);
		try {
			if (!file.exists()) file.createNewFile();
		} catch (Throwable err) {
			FlameLauncher.logError(err);
		}
	}
	
	private ArrayList<ClassLoader> loaders = new ArrayList<>();
	
	private FlameLoader owner;
	
	public void addLoader(FlameLoader loader) {
		loaders.add(loader.setOwner(this));
	}
	
	private FlameLoader setOwner(FlameLoader loader) {
		this.owner = loader;
		return this;
	}
	
	private Class<?> checkAdditionalLoaders(String name) {
//		FlameMain.field.append("Checking additional classloaders for:"+name+"\n");
		int i = 0;
		for (ClassLoader loader : loaders) {
			try {
				Class<?> c = loader.loadClass(name);
//				FlameMain.field.append("Checking additional classloaders for:"+name+"\n");
				i++;
//				FlameMain.field.append("Loader:"+i+"/"+loaders.size()+"\n");
				if (c != null) {
					return c;
				}
			} catch (Throwable err) {
				FlameLauncher.logError(err);
			}
		}
		return null;
	}
	
	public void append(String name, Class<?> clazz) {
		classes.put(name, clazz);
	}
	
	@Override
	protected Class<?> findClass(String moduleName, String name) {
		return this.findClass(moduleName + "." + name);
	}
	
	@Override
	public Class<?> loadClass(String name) {
		if (!name.contains(".")) {
			try {
				return super.loadClass(name);
			} catch (Throwable ignored) {}
		}
//		FlameMain.field.append("Finding and loading class:"+name+"\n");
		if (
				!name.startsWith("java")
						&& !name.startsWith("joptsimple")
						&& !name.startsWith("com.mojang")
						&& !name.startsWith("com.google")
						&& !name.startsWith("org.apache")
						&& !name.startsWith("io.netty")
						&& !name.startsWith("it.unimi")
		) {
			Class c = findClassShort(name);
			if (c == null) c = checkAdditionalLoaders(name);
			if (c == null) {
				FlameLauncher.field.append("Loading Class:" + name + "\n");
				try {
					InputStream stream1 = new ClassPath(path).getClassFile(name).getInputStream();
					try {
						if (stream1 != null) {
							byte[] bytes1 = new byte[stream1.available()];
							stream1.read(bytes1);
							StringBuilder bytecode = new StringBuilder();
							for (int i = 0; i < bytes1.length; i++) {
								bytecode.append(bytes1[i]).append(" ");
							}
							FlameLauncher.field.append("Bytecode:" + bytecode.toString() + "\n");
							stream1.close();
							c = this.define(name, bytes1);
							append(name, c);
						}
					} catch (Throwable err) {
						FlameLauncher.logError(err);
						FlameLauncher.field.append("Failed to load class: " + path + "\\" + name + "\n");
						c = this.findClass(name);
						try {
							stream1.close();
						} catch (Throwable err2) {
//						FlameLauncher.logError(err2);
						}
					}
				} catch (Throwable err) {
					c = this.findClass(name);
					FlameLauncher.logError(err);
					FlameLauncher.field.append("Failed to load class: " + path + "\\" + name + "\n");
				}
			}
			if (c == null) c = this.findClass(name);
			if (c != null) return c;
		}
		try {
			return this.findClass(name);
		} catch (Throwable err) {
			FlameLauncher.logError(err);
			return null;
		}
	}
	
	public Class<?> findClassShort(String name) {
		Class c = classes.getOrDefault(name, null);
//		if (this.owner!=null) {
//			c=this.owner.findClass(name);
//		}
		return c;
	}
	
	public Class trimAndDefine(String name, byte[] bytes) {
		boolean tooLong=true;
		int offset=0;
		while (tooLong) {
			try {
//				if (offset!=0)
//					bytes[bytes.length-offset]=new Byte("");
				offset++;
				if (offset==bytes.length) return null;
				return this.defineClass(name, bytes, 0, bytes.length-offset);
			} catch (Throwable ignored) {}
		}
		return null;
	}
	
	@Override
	protected Class<?> findClass(String name) {
		Class c = classes.getOrDefault(name, null);
		if (this.owner != null) {
			c = this.owner.findClass(name);
		}
		try {
			if (c == null) c = this.findSystemClass(name);
		} catch (Throwable ignored) {
		}
		try {
			if (c == null) c = super.findClass(name);
		} catch (Throwable ignored) {
		}
		try {
			if (c == null && getParent() != null) c = getParent().loadClass(name);
		} catch (Throwable ignored) {
		}
		try {
			if (c == null && getParent() != null) c = this.getClass().getClassLoader().loadClass(name);
		} catch (Throwable ignored) {
		}
//		if (c == null) {
//			c = checkAdditionalLoaders(name);
//		}
		if (c==null) {
			try {
				c = this.getClass().getClassLoader().loadClass(name);
				append(name, c);
			} catch (Throwable ignored) {
			}
		}
		return c;
	}
	
	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		if (!name.contains(".")) {
			try {
				try {
					return this.getParent().loadClass(name);
				} catch (Throwable err) {
					try {
						Class<?> c = Class.forName(name);
						if (c!=null) return c;
					} catch (Throwable err2) {
					}
				}
				return super.loadClass(name, resolve);
			} catch (Throwable ignored) {
			}
		}
		Class c = this.loadClass(name);
		if (resolve) this.resolveClass(c);
		return c;
	}
	
	public Class<?> load(String name, boolean resolve) throws ClassNotFoundException {
		return loadClass(name, resolve);
	}
}