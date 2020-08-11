package com.tfc.flamemc;

import org.apache.bcel.util.ClassPath;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

//MMD Discord:https://discord.mcmoddev.com/
//Message:https://discordapp.com/channels/176780432371744769/421377435041267712/723593029583241336
//Most stuff I know about class loaders is from fabric.
public class FlameLoader extends URLClassLoader {
	public FlameLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
		try {
			Field loader = this.getClass().getClass().getDeclaredField("classLoader");
			loader.set(this.getClass(),this);
		} catch (Throwable err) {}
	}
	
	public FlameLoader() throws MalformedURLException {
		super(new URL[] {new File(dir).toURL()});
		try {
			Field loader = this.getClass().getClass().getDeclaredField("classLoader");
			loader.set(this.getClass(),this);
		} catch (Throwable err) {}
	}
	
	public FlameLoader(URL[] urls) {
		super(urls);
		try {
			Field loader = this.getClass().getClass().getDeclaredField("classLoader");
			loader.set(this.getClass(),this);
		} catch (Throwable err) {}
	}
	
	public FlameLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
		super(urls, parent, factory);
		try {
			Field loader = this.getClass().getClass().getDeclaredField("classLoader");
			loader.set(this.getClass(),this);
		} catch (Throwable err) {}
	}
	
	private Class<?> define(String name, byte[] bytes) {
		try {
			Class<?> c = this.defineClass(name, bytes, 0, bytes.length);
			try {
				Vector<Class<?>> vc = ((Vector<Class<?>>) (this.getClass().getClassLoader().getClass().getDeclaredField("classes").get(this.getClass().getClassLoader())));
				try {
					AtomicReference<Class<?>> toRemove = new AtomicReference<>(null);
					vc.forEach(c1 -> {
						if (c1.getName().equals(c.getName())) {
							toRemove.set(c1);
						}
					});
					if (toRemove.get() != null) vc.remove(toRemove.get());
				} catch (Throwable err) {
//					FlameLauncher.logError(err);
				}
//				vc.add(c);
				ArrayList<Class<?>> toRemove_list = new ArrayList<>();
				vc.forEach(c1->{
					if (!c1.getName().contains(".")) {
						toRemove_list.add(c1);
					} else if (c1.getName().startsWith("org.apache") && !c1.getName().contains("bcel")) {
						toRemove_list.add(c1);
					} else if (c1.getName().startsWith("com.google")) {
						toRemove_list.add(c1);
					}
				});
				toRemove_list.forEach(vc::remove);
//				vc.add(c);
			} catch (Throwable ignored) {
//				FlameLauncher.logError(ignored);
			}
			try {
//				AtomicReference<Class<?>> toRemove = new AtomicReference<>(null);
				Vector<Class<?>> vc = ((Vector<Class<?>>) (this.getClass().getField("classes").get(this)));
//				vc.forEach(c1 -> {
//					if (c1.getName().equals(c.getName())) {
//						toRemove.set(c1);
//					}
//				});
//				if (toRemove.get() != null) vc.remove(toRemove.get());
				vc.clear();
			} catch (Throwable ignored) {
			}
			try {
				((Vector<Class<?>>)this.getClass().getClassLoader().getClass().getField("classes").get(this.getClass().getClassLoader())).add(c);
			} catch (Throwable err) {
				try {
					this.getClass().getClassLoader().getClass().getMethod("defineClass", String.class, byte[].class, int.class, int.class).invoke(this.getClass().getClassLoader(),name, bytes, 0, bytes.length);
				} catch (Throwable ignored) {}
			}
			if (!this.classMap.containsKey(name)) append(name,c);
			return c;
		} catch (Throwable err) {
			return this.trimAndDefine(name, bytes);
		}
	}
	
	private final HashMap<String, Class<?>> classMap = new HashMap<>();
	
	public HashMap<String, Class<?>> getClassMap() {
		return (HashMap<String, Class<?>>) classMap.clone();
	}
	
	private static final String dir = System.getProperty("user.dir");
	
	private String path = dir + "\\flame_mods";
	
	//USE WITH CAUTION
	public void setPath(String path) {
		this.path = path;
		File file = new File(path);
		try {
			if (!file.exists()) file.mkdirs();
		} catch (Throwable err) {
			FlameLauncher.logError(err);
		}
		checkAllFiles = false;
		try {
			Field classPaths = URLClassLoader.class.getDeclaredField("ucp");
			Field urls1 = Class.forName("sun.misc.URLClassPath").getDeclaredField("urls");
			Field urls2 = Class.forName("sun.misc.URLClassPath").getDeclaredField("path");
			((Stack<URL>)urls1.get(classPaths.get(this))).clear();
			((ArrayList<URL>)urls2.get(classPaths.get(this))).clear();
		} catch (Throwable ignored) {}
		try {
			super.addURL(new File(path).toURL());
		} catch (Throwable ignored) {}
	}
	
	private boolean checkAllFiles = false;
	
	//USE WITH CAUTION
	public void setPath(String path, boolean checkAllFiles) {
		this.path = path;
		File file = new File(path);
		try {
			if (!file.exists()) file.mkdirs();
		} catch (Throwable err) {
			FlameLauncher.logError(err);
		}
		this.checkAllFiles = checkAllFiles;
		try {
			Field classPaths = URLClassLoader.class.getDeclaredField("ucp");
			Field urls1 = Class.forName("sun.misc.URLClassPath").getDeclaredField("urls");
			Field urls2 = Class.forName("sun.misc.URLClassPath").getDeclaredField("path");
			((Stack<URL>)urls1.get(classPaths.get(this))).clear();
			((ArrayList<URL>)urls2.get(classPaths.get(this))).clear();
		} catch (Throwable ignored) {}
		if (checkAllFiles) {
			for (File fi:new File(path).listFiles()) {
				try {
					super.addURL(fi.toURL());
				} catch (Throwable ignored) {}
			}
		}
	}
	
	private final ArrayList<FlameLoader> loaders = new ArrayList<>();
	
	private FlameLoader owner;
	
	public FlameLoader addLoader(FlameLoader loader) {
		loaders.add(loader.setOwner(this));
		return this;
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
					append(name,c);
					return c;
				}
			} catch (Throwable err) {
//				FlameLauncher.logError(err);
			}
		}
		return null;
	}
	
	public void append(String name, Class<?> clazz) {
		if (!classMap.containsKey(name)) classMap.put(name, clazz);
	}
	
	public void append(String name, Class<?> clazz,boolean honest) {
		if (!classMap.containsKey(name)) classMap.put(name, clazz);
	}
	
//	@Override
//	protected Class<?> findClass(String moduleName, String name) {
//		return this.findClass(moduleName + "." + name);
//	}
	
	private final ArrayList<String> unsafeMerging = new ArrayList<>();
	
	public void registerUnsafeMerging(String file) {
		unsafeMerging.add(file);
	}
	
	@Override
	public URL getResource(String name) {
		Objects.requireNonNull(name);
		URL url = null;
		try {
			if (this.checkAllFiles) {
				for (File fi : Objects.requireNonNull(new File(this.path).listFiles())) {
					if (url==null) {
						url = new URL("jar:file:/" + fi.getPath() + "!/" + name);
						try {
							FlameLauncher.field.append(url.toString()+"\n");
							InputStream stream = url.openStream();
							FlameLauncher.field.append("Valid url\n");
							try{stream.close();}catch(Throwable ignored){}
						} catch (Throwable err) {
							url = null;
						}
					}
				}
			} else {
				url = new URL("jar:file:/" + path + "!/" + name);
				try {
					FlameLauncher.field.append(url.toString()+"\n");
					InputStream stream = url.openStream();
					FlameLauncher.field.append("Valid url\n");
					try{stream.close();}catch(Throwable ignored){}
				} catch (Throwable err) {
					url = null;
				}
			}
		} catch (Throwable ignored) {}
		if (url == null) {
//			if (this.parent != null) {
//				url = this.parent.getResource(name);
//			}
//			if (url == null && this.getParent() != null) {
//				url = this.getParent().getResource(name);
//			}
//			if (url == null && this.parent != null) {
//				url = this.parent.getResource(name);
//			}
			if (url == null && this.owner != null) {
				url = this.owner.getResource(name);
			}
//			if (url == null) {
//				url = this.findResource(name);
//				if (url == null) {
//					url = this.getClass().getClassLoader().getResource(name);
//				}
//			}
		}
//		if (url == null) {
//			url = BootLoader.findResource(name);
//		}
		
		return url;
	}
	
	private final ArrayList<String> blacklisted_names = new ArrayList<>();
	
	public void blacklistName(String name) {
		blacklisted_names.add(name);
	}
	
	public String getPath() {
		return path;
	}
	
	public boolean isCheckAllFiles() {
		return checkAllFiles;
	}
	
	private int superLoad = 0;
	
	@Override
	public Class<?> loadClass(String name) {
		try {
			if (true) owner.loadClass(name);
		} catch (Throwable ignored) {}
		try {
			if (true) parent.loadClass(name);
		} catch (Throwable ignored) {}
		Class<?> c = null;
		if (name.startsWith("org.apache")||name.startsWith("com.google")||name.startsWith("org.slf4j")) {
			try {
				return parent.loadClass(name);
			} catch (Throwable err) {
				try {
					return null;
				} catch (Throwable ignored) {}
			}
		}
		try {
			InputStream stream = this.getResourceAsStream(name);
			c = this.defineFromStream(name, stream);
			stream.close();
			if (c == null)  throw new Exception();
		} catch (Throwable err) {
			try {
				FlameLauncher.logError(err);
				InputStream stream = parent.getResourceAsStream(name);
				c = this.defineFromStream(name, stream);
				stream.close();
				if (c == null)  throw new Exception();
			} catch (Throwable err2) {
				try {
					FlameLauncher.logError(err2);
					InputStream stream = owner.getResourceAsStream(name);
					c = this.defineFromStream(name, stream);
					stream.close();
					if (c == null)  throw new Exception();
				} catch (Throwable err3) {
					try {
//						FlameLauncher.logError(err3);
						if (this.checkAllFiles) {
							for (File fi:new File(path).listFiles()) {
								InputStream stream = new ClassPath(fi.getName()).getInputStream(name);
								c = this.defineFromStream(name, stream);
								stream.close();
								if (c != null) return c;
							}
						} else {
							InputStream stream = new ClassPath(path).getInputStream(name);
							c = this.defineFromStream(name, stream);
							stream.close();
						}
						if (c == null)  throw new Exception();
					} catch (Throwable err4) {
						try {
//							FlameLauncher.logError(err4);
							c = parent.loadClass(name);
							if (c == null)  throw new Exception();
						} catch (Throwable err5) {
							try {
								c = super.loadClass(name);
								if (c == null)  throw new Exception();
							} catch (Throwable ignored) {
							}
						}
					}
				}
			}
		}
		return c;
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
	
	private Class<?> defineFromStream(String name, InputStream stream1) throws Throwable {
		if (stream1 != null) {
			byte[] bytes1 = new byte[stream1.available()];
			stream1.read(bytes1);
			for (String file : unsafeMerging) {
				for (FlameLoader loader : loaders) {
					try {
						String path1 = loader.path + "\\" + file;
						ClassPath path2 = new ClassPath(path1);
						InputStream stream2 = path2.getResourceAsStream(name);
						byte[] bytes2 = new byte[stream1.available()];
						stream2.read(bytes2);
						bytes1 = merge(bytes1, bytes2);
					} catch (Throwable ignored) {}
				}
				try {
					String path1 = path + "\\" + file;
					ClassPath path2 = new ClassPath(path1);
					InputStream stream2 = path2.getResourceAsStream(name);
					byte[] bytes2 = new byte[stream1.available()];
					stream2.read(bytes2);
					bytes1 = merge(bytes1, bytes2);
				} catch (Throwable ignored) {}
			}
			if (FlameLauncher.log_bytecode) {
				StringBuilder bytecode = new StringBuilder();
				for (byte b : bytes1) bytecode.append(b).append(" ");
				FlameLauncher.field.append("Bytecode:" + bytecode.toString() + "\n");
			}
			stream1.close();
			return this.define(name, bytes1);
		}
		return null;
	}
	
	public Class<?> findClassShort(String name) {
		Class c = classMap.getOrDefault(name, null);
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
		FlameLauncher.field.append("Finding class:" + name + "\n");
		Class<?> c = findClassNoAdditional(name, false);
		if (this.owner == null && c == null) c = checkAdditionalLoaders(name);
		append(name,c);
		return c;
	}
	
	private Class<?> findClassNoAdditional(String name, boolean useSuper) {
		Class c = classMap.getOrDefault(name, null);
		if (this.owner != null) {
			c = this.owner.findClassNoAdditional(name, useSuper);
		}
//		if (useSuper && c == null) {
//			try {
////				return super.loadClass(name, false);
//			} catch (Throwable ignored) {
//			}
//		}
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
//		if (getParent() != null && c == null) {
//			try {
//				c = getParent().loadClass(name);
//			} catch (Throwable ignored) {
//			}
//		}
//		if (parent != null && c == null) {
//			try {
//				c = parent.loadClass(name);
//			} catch (Throwable ignored) {
//			}
//		}
//		if (c == null) {
//			try {
//				c = this.getClass().getClassLoader().loadClass(name);
//			} catch (Throwable ignored) {
//			}
//		}
		append(name,c);
		return c;
	}
	
	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		FlameLauncher.field.append("Finding class:" + name + "\n");
		Class<?> c = this.loadClass(name);
		try { if (resolve) this.resolveClass(c); } catch (Throwable err) {FlameLauncher.logError(err);}
		append(name,c);
		return c;
	}
	
	public Class<?> load(String name, boolean resolve) throws ClassNotFoundException {
		return loadClass(name, resolve);
	}
}