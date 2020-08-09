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
			return this.defineClass(name, bytes, 0, bytes.length);
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
	
	//USE WITH CAUTION
	public void setPath(String path, boolean checkAllFiles) {
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
	
	private ClassLoader parent = null;
	
	protected FlameLoader setParent(ClassLoader loader) {
		parent = loader;
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
			Class<?> c = findClassShort(name);
			if (c != null) return c;
			try {
				c = super.loadClass(name, true);
				append(name, c);
				return c;
			} catch (Throwable ignored) {
			}
		}
		FlameLauncher.field.append("Finding and loading class:" + name + "\n");
		if (
				!name.startsWith("java")
						&& !name.startsWith("joptsimple")
						&& !name.startsWith("com.mojang")
						&& !name.startsWith("com.google")
						&& !name.startsWith("org.apache")
						&& !name.startsWith("io.netty")
						&& !name.startsWith("it.unimi")
		) {
			Class<?> c = findClassShort(name);
			Throwable error = null;
			if (c == null) {
				try {
					ClassPath path1 = new ClassPath(path);
					InputStream stream1 = path1.getClassFile(name).getInputStream();
					FlameLauncher.field.append("Loading Class:" + name + "\n");
					try {
						c = defineFromStream(name, stream1);
						if (c == null)
							throw new Exception("Failed to load class: " + name);
					} catch (Throwable err) {
						if (parent != null && c == null) {
							try {
								stream1.close();
							} catch (Throwable err2) {
							}
							stream1 = this.parent.getResourceAsStream(name);
							c = defineFromStream(name, stream1);
							if (c == null) {
								c = checkAdditionalLoaders(name);
								if (c == null) c = findClassNoAdditional(name, false);
							}
							stream1.close();
						}
						error = err;
						if (c == null) c = this.checkAdditionalLoaders(name);
					}
				} catch (Throwable err) {
					if (c == null) c = this.checkAdditionalLoaders(name);
					error = err;
				}
			}
			if (c == null && error != null) {
				FlameLauncher.logError(error);
				FlameLauncher.field.append("Failed to load class: " + path + "\\" + name + "\n");
			}
			if (c != null) FlameLauncher.field.append("Loaded Class: " + name + "\n");
			else {
				FlameLauncher.field.append("Checking additional loaders and other known loaders for class: " + name + "\n");
				c = this.checkAdditionalLoaders(name);
				if (c == null) c = this.findClassNoAdditional(name, true);
				if (c != null) FlameLauncher.field.append("Other loaders loaded class: " + name + "\n");
			}
			if (c != null) {
				append(name, c);
				return c;
			}
		}
		try {
			Class<?> c = this.checkAdditionalLoaders(name);
			if (c == null) c = this.findClassNoAdditional(name, true);
			if (c != null) {
				append(name, c);
				return c;
			} else throw new Exception("Missing Class: " + name);
		} catch (Throwable err) {
			FlameLauncher.logError(err);
			return null;
		}
	}
	
	private Class<?> defineFromStream(String name, InputStream stream1) throws Throwable {
		if (stream1 != null) {
			byte[] bytes1 = new byte[stream1.available()];
			stream1.read(bytes1);
			StringBuilder bytecode = new StringBuilder();
			for (byte b : bytes1) bytecode.append(b).append(" ");
			FlameLauncher.field.append("Bytecode:" + bytecode.toString() + "\n");
			stream1.close();
			return this.define(name, bytes1);
		}
		return null;
	}
	
	public Class<?> findClassShort(String name) {
		Class c = classes.getOrDefault(name, null);
//		if (this.owner!=null) {
//			c=this.owner.findClass(name);
//		}
		return c;
	}
	
	public Class trimAndDefine(String name, byte[] bytes) {
//		int offset = 0;
//		while (true) {
//			try {
//				offset++;
//				if (offset == bytes.length) return null;
//				return this.defineClass(name, bytes, 0, bytes.length - offset);
//			} catch (Throwable ignored) {
//			}
//		}
		return null;
	}
	
	@Override
	protected Class<?> findClass(String name) {
		Class<?> c = findClassNoAdditional(name, false);
		if (this.owner == null && c == null) c = checkAdditionalLoaders(name);
		return c;
	}
	
	private Class<?> findClassNoAdditional(String name, boolean useSuper) {
		Class c = classes.getOrDefault(name, null);
		if (this.owner != null) {
			c = this.owner.findClassNoAdditional(name, useSuper);
		}
		if (useSuper && c == null) {
			try {
				return super.loadClass(name, false);
			} catch (Throwable ignored) {
			}
		}
		if (c == null && useSuper) {
			try {
				c = this.findSystemClass(name);
			} catch (Throwable ignored) {
			}
		}
		if (c == null && useSuper) {
			try {
				c = super.findClass(name);
			} catch (Throwable ignored) {
			}
		}
		if (getParent() != null && c == null) {
			try {
				c = getParent().loadClass(name);
			} catch (Throwable ignored) {
			}
		}
		if (parent != null && c == null) {
			try {
				c = parent.loadClass(name);
			} catch (Throwable ignored) {
			}
		}
		if (c == null) {
			try {
				c = this.getClass().getClassLoader().loadClass(name);
			} catch (Throwable ignored) {
			}
		}
		return c;
	}
	
	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
//		if (!name.contains(".")) {
//			try {
//				try {
//					return this.getParent().loadClass(name);
//				} catch (Throwable err) {
//					try {
//						Class<?> c = Class.forName(name);
//						if (c != null) return c;
//					} catch (Throwable err2) {
//					}
//				}
//				return super.loadClass(name, resolve);
//			} catch (Throwable ignored) {
//			}
//		}
		Class c = this.loadClass(name);
		if (resolve) this.resolveClass(c);
		return c;
	}
	
	public Class<?> load(String name, boolean resolve) throws ClassNotFoundException {
		return loadClass(name, resolve);
	}
}