package com.tfc.flame;

import org.apache.bcel.util.ClassPath;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

//MMD Discord:https://discord.mcmoddev.com/
//Message:https://discordapp.com/channels/176780432371744769/421377435041267712/723593029583241336
//Most stuff I know about class loaders is from fabric.

 /**
 * Deprecated until someone can figure out how to get this to work.
 * I might support it again eventually, but for now, no.
 * It's ugly, obnoxious, and hard to work on.
 */
@Deprecated
public class FlameLoader extends ClassLoader {
	 private Class<?> define(String name, byte[] bytes) {
		 try {
			 Class<?> c = this.defineClass(name, bytes, 0, bytes.length);
//			try {
//				this.getClass().getClassLoader().getClass().getMethod("defineClass", String.class, byte[].class, int.class, int.class).invoke(this.getClass().getClassLoader(),name, bytes, 0, bytes.length);
//			} catch (Throwable ignored) {}
			 try {
				 AtomicReference<Class<?>> toRemove = new AtomicReference<>(null);
				 Vector<Class<?>> vc = ((Vector<Class<?>>) (this.getClass().getClassLoader().getClass().getField("classes").get(this.getClass().getClassLoader())));
				 vc.forEach(c1 -> {
					 if (c1.getName().equals(c.getName())) {
						 toRemove.set(c1);
					 }
				 });
				 if (toRemove.get() != null) vc.remove(toRemove.get());
				 vc.add(c);
			 } catch (Throwable ignored) {
			 }
			 try {
				 AtomicReference<Class<?>> toRemove = new AtomicReference<>(null);
				 Vector<Class<?>> vc = ((Vector<Class<?>>) (this.getClass().getField("classes").get(this)));
				 vc.forEach(c1 -> {
					 if (c1.getName().equals(c.getName())) {
						 toRemove.set(c1);
					 }
				 });
				 if (toRemove.get() != null) vc.remove(toRemove.get());
				 vc.add(c);
			 } catch (Throwable ignored) {
			 }
			 return c;
		 } catch (Throwable err) {
			 return this.trimAndDefine(name, bytes);
		 }
	 }
	
	 private final HashMap<String, Class<?>> classes = new HashMap<>();
	
	 private static final String dir = System.getProperty("user.dir");
	
	 private String path = dir + "\\flame_mods";
	
	 //USE WITH CAUTION
	 public void setPath(String path) {
		 this.path = path;
		 File file = new File(path);
		 try {
			 if (!file.exists()) file.mkdirs();
		 } catch (Throwable err) {
			 FlameConfig.logError(err);
		 }
		 checkAllFiles = false;
	 }
	
	 private boolean checkAllFiles = false;
	
	 //USE WITH CAUTION
	 public void setPath(String path, boolean checkAllFiles) {
		 this.path = path;
		 File file = new File(path);
		 try {
			 if (!file.exists()) file.mkdirs();
		 } catch (Throwable err) {
			 FlameConfig.logError(err);
		 }
		 this.checkAllFiles = checkAllFiles;
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
	
	 public FlameLoader setParent(ClassLoader loader) {
		 parent = loader;
		 return this;
	 }
	
	 public String getPath() {
		 return path;
	 }
	
	 public boolean isCheckAllFiles() {
		 return checkAllFiles;
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
					 if (!classes.containsKey(name)) append(name, c);
					 return c;
				 }
			 } catch (Throwable err) {
				 FlameConfig.logError(err);
			 }
		 }
		 return null;
	 }
	
	 public void append(String name, Class<?> clazz) {
		 classes.put(name, clazz);
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
					 if (url == null) {
						 url = new URL("jar:file:/" + fi.getPath() + "!/" + name);
						 try {
							 FlameConfig.field.append(url.toString() + "\n");
							 InputStream stream = url.openStream();
							 FlameConfig.field.append("Valid url\n");
							 try {
								 stream.close();
							 } catch (Throwable ignored) {
							 }
						 } catch (Throwable err) {
							 url = null;
						 }
					 }
				 }
			 } else {
				 url = new URL("jar:file:/" + path + "!/" + name);
				 try {
					 FlameConfig.field.append(url.toString() + "\n");
					 InputStream stream = url.openStream();
					 FlameConfig.field.append("Valid url\n");
					 try {
						 stream.close();
					 } catch (Throwable ignored) {
					 }
				 } catch (Throwable err) {
					 url = null;
				 }
			 }
		 } catch (Throwable ignored) {
		 }
		 if (this.parent != null) {
			 url = this.parent.getResource(name);
		 }
		 if (url == null && this.getParent() != null) {
			 url = this.getParent().getResource(name);
		 }
		 if (url == null && this.parent != null) {
			 url = this.parent.getResource(name);
		 }
		 if (url == null && this.owner != null) {
			 url = this.owner.getResource(name);
		 }
//		if (url == null) {
//			url = BootLoader.findResource(name);
//		}
		 if (url == null) {
			 url = this.findResource(name);
		 }
		 if (url == null) {
			 url = this.getClass().getClassLoader().getResource(name);
		 }
		
		 return url;
	 }
	
	 private final ArrayList<String> blacklisted_names = new ArrayList<>();
	
	 public void blacklistName(String name) {
		 blacklisted_names.add(name);
	 }
	
	 @Override
	 public Class<?> loadClass(String name) {
		 FlameConfig.field.append("Finding and loading class:" + name + "\n");
		 Class<?> c1 = findClassShort(name);
		 if (c1 != null) return c1;
		 boolean overriden = false;
		 for (String file : unsafeMerging) {
			 for (FlameLoader loader : loaders) {
				 try {
					 String path1 = loader.path + "\\" + file;
					 ClassPath path2 = new ClassPath(path1);
					 InputStream stream = path2.getInputStream(name);
					 stream.close();
					 overriden = true;
				 } catch (Throwable ignored) {
				 }
			 }
		 }
		 for (String name_test : blacklisted_names) {
			 try {
				 if (name.equals(name_test)) {
					 Class<?> c = this.getClass().getClassLoader().loadClass(name);
					 append(name, c);
					 return c;
				 }
			 } catch (Throwable err) {
				 return null;
			 }
		 }
		 if (!overriden) {
			 if (!checkAllFiles) {
				 try {
					 InputStream stream1 = owner.getResourceAsStream(name.replace(".", "/") + ".class");
					 Class<?> c = defineFromStream(name, stream1);
					 if (c != null) {
						 append(name, c);
						 return c;
					 }
					 throw new Exception();
				 } catch (Throwable ignored) {
				 }
			 }
		 }
		 if (!name.startsWith("org.apache") && !name.startsWith("com.google")) {
			 Class<?> c = null;
			 try {
				 InputStream stream1 = this.parent.getResourceAsStream(name);
				 c = defineFromStream(name, stream1);
				 if (c != null) {
					 append(name, c);
					 return c;
				 }
				 throw new Exception();
			 } catch (Throwable err) {
				 try {
					 InputStream stream1 = this.getResourceAsStream(name.replace(".", "/") + ".class");
					 c = defineFromStream(name, stream1);
					 if (c != null) {
						 append(name, c);
						 return c;
					 }
					 throw new Exception();
				 } catch (Throwable err2) {
					 try {
						 InputStream stream1 = owner.getResourceAsStream(name.replace(".", "/") + ".class");
						 c = defineFromStream(name, stream1);
						 if (c != null) {
							 append(name, c);
							 return c;
						 }
						 throw new Exception();
					 } catch (Throwable ignored) {
					 }
				 }
			 }
		 } else {
			 if (name.startsWith("org.apache"))
				 FlameConfig.field.append("Ugh... apache... why don't you let me load you with FlameLoader?");
			 if (name.startsWith("com.google"))
				 FlameConfig.field.append("Ugh... google... why don't you let me load you with FlameLoader?");
		 }
		 try {
			 Class<?> c = super.loadClass(name, true);
			 if (c != null) {
				 append(name, c);
				 return c;
			 }
			 throw new Exception();
		 } catch (Throwable err3) {
			 FlameConfig.logError(err3);
		 }
		 if (
				 !name.startsWith("java")
//						&& !name.startsWith("joptsimple")
//						&& !name.startsWith("com.mojang")
//						&& !name.startsWith("com.google")
//						&& !name.startsWith("org.apache")
//						&& !name.startsWith("io.netty")
//						&& !name.startsWith("it.unimi")
//						&& !name.startsWith("org.apache")
		 ) {
			 Class<?> c = null;
			 Throwable error = null;
			 try {
				 if (checkAllFiles) {
					 boolean foundClass = false;
					 for (File fi : Objects.requireNonNull(new File(path).listFiles())) {
						 ClassPath path1 = new ClassPath(fi.getPath());
						 InputStream stream1 = path1.getClassFile(name).getInputStream();
						 FlameConfig.field.append("Loading Class:" + name + "\n");
						 try {
							 c = defineFromStream(name, stream1);
							 if (c == null)
								 throw new Exception("Failed to load class: " + name);
							 else foundClass = true;
						 } catch (Throwable err) {
							 try {
								 stream1.close();
							 } catch (Throwable err2) {
							 }
							 error = err;
						 }
					 }
					 try {
						 if (!foundClass) {
//								if (parent != null && c == null) {
//									InputStream stream1 = this.parent.getResourceAsStream(name);
//									c = defineFromStream(name, stream1);
//									if (c == null) {
//										c = checkAdditionalLoaders(name);
//										if (c == null) c = findClassNoAdditional(name, false);
//									}
//								}
						 }
					 } catch (Throwable err) {
						 error = err;
					 }
					 if (c == null) c = this.checkAdditionalLoaders(name);
				 } else {
					 ClassPath path1 = new ClassPath(path);
					 InputStream stream1 = path1.getClassFile(name).getInputStream();
					 FlameConfig.field.append("Loading Class:" + name + "\n");
					 try {
						 c = defineFromStream(name, stream1);
						 if (c == null)
							 throw new Exception("Failed to load class: " + name);
					 } catch (Throwable err) {
						 if (parent != null && c == null) {
							 try {
								 stream1.close();
							 } catch (Throwable ignored) {
							 }
//								stream1 = this.parent.getResourceAsStream(name);
//								c = defineFromStream(name, stream1);
//								if (c == null) {
//									c = checkAdditionalLoaders(name);
//									if (c == null) c = findClassNoAdditional(name, false);
//								}
//								assert stream1 != null;
//								stream1.close();
						 }
						 error = err;
						 if (c == null) c = this.checkAdditionalLoaders(name);
					 }
				 }
			 } catch (Throwable err) {
				 if (c == null) c = this.checkAdditionalLoaders(name);
				 error = err;
			 }
			 if (c == null && error != null) {
				 FlameConfig.logError(error);
				 FlameConfig.field.append("Failed to load class: " + path + "\\" + name + "\n");
			 }
			 if (c != null) FlameConfig.field.append("Loaded Class: " + name + "\n");
			 else {
				 FlameConfig.field.append("Checking additional loaders and other known loaders for class: " + name + "\n");
				 c = this.checkAdditionalLoaders(name);
				 if (c == null) c = this.findClassNoAdditional(name, true);
				 if (c != null) FlameConfig.field.append("Other loaders loaded class: " + name + "\n");
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
			 } else {
				 try {
					 if (parent != null) c = parent.loadClass(name);
				 } catch (Throwable ignored) {
				 }
				 if (c == null && this.getClass().getClassLoader() != null) {
					 try {
						 c = this.getClass().getClassLoader().loadClass(name);
					 } catch (Throwable ignored) {
					 }
					 if (c == null && this.getClass().getClassLoader().getClass().getClassLoader() != null)
						 c = this.getClass().getClassLoader().getClass().getClassLoader().loadClass(name);
				 }
				 if (owner != null && c == null) c = owner.findClassShort(name);
				 if (c == null) throw new Exception("Missing Class: " + name);
				 else {
					 append(name, c);
					 return c;
				 }
			 }
		 } catch (Throwable err) {
			 FlameConfig.logError(err);
			 return null;
		 }
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
					 String path1 = loader.path + "\\" + file;
					 ClassPath path2 = new ClassPath(path1);
					 InputStream stream2 = path2.getResourceAsStream(name);
					 byte[] bytes2 = new byte[stream1.available()];
					 stream2.read(bytes2);
					 bytes1 = merge(bytes1, bytes2);
				 }
				 String path1 = path + "\\" + file;
				 ClassPath path2 = new ClassPath(path1);
				 InputStream stream2 = path2.getResourceAsStream(name);
				 byte[] bytes2 = new byte[stream1.available()];
				 stream2.read(bytes2);
				 bytes1 = merge(bytes1, bytes2);
			 }
			 if (FlameConfig.log_bytecode) {
				 StringBuilder bytecode = new StringBuilder();
				 for (byte b : bytes1) bytecode.append(b).append(" ");
				 FlameConfig.field.append("Bytecode:" + bytecode.toString() + "\n");
			 }
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
		 if (!classes.containsKey(name)) append(name, c);
		 return c;
	 }
	
	 private Class<?> findClassNoAdditional(String name, boolean useSuper) {
		 Class c = classes.getOrDefault(name, null);
		 if (this.owner != null) {
			 c = this.owner.findClassNoAdditional(name, useSuper);
		 }
		 if (useSuper && c == null) {
			 try {
//				return super.loadClass(name, false);
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
		 if (!classes.containsKey(name)) append(name, c);
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
		 if (!classes.containsKey(name)) append(name, c);
		 return c;
	 }
	
	 public Class<?> load(String name, boolean resolve) throws ClassNotFoundException {
		 return loadClass(name, resolve);
	 }
 }