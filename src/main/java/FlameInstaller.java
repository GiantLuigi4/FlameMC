import com.tfc.flamemc.FlameLauncher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class FlameInstaller {
	private static final String dir = System.getProperty("user.dir");
	
	public static void main(String[] args) {
		AtomicBoolean ready = new AtomicBoolean(false);
		
		JFrame frame = new JFrame("Flame Installer");
		JPanel panel = new JPanel();
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel1 = new JPanel();
		panel1.setSize(400,100);
		JTextField field = new JTextField();
		field.setSize(400,100);
		field.setText("C:\\Users\\Owner\\AppData\\Roaming\\.minecraft\\versions\\1.15.2");
		panel1.add(field);
		
		JPanel panel2 = new JPanel();
		JButton button = new JButton("Install");
		button.addActionListener(e -> ready.set(true));
		panel2.setLocation(50,0);
		panel2.add(button);
		
		JPanel panel3 = new JPanel();
		panel3.setSize(400,400);
		panel3.setMinimumSize(new Dimension(400,400));
		panel3.setMaximumSize(new Dimension(400,400));
		TextArea field2 = new TextArea();
		panel3.add(field2);
		
		panel.add(panel1);
		panel.add(panel2);
		panel.add(panel3);
		
		frame.add(panel);
		frame.pack();
		
		frame.setVisible(true);
		
		while (frame.isVisible()) {
			while (!ready.get());
			
			ready.set(false);
			
			try {
				field2.append("Start Installation");
				String text = field.getText();
				File file1 = new File(text);
				JarFile file = new JarFile(new File(text+"\\"+file1.getName()+".jar"));
				JarOutputStream stream = new JarOutputStream(new FileOutputStream(new File(text+"-flame\\"+file1.getName()+"-flame.jar")));
				Enumeration<JarEntry> entries = file.entries();
				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
//					field2.append("\nCopy File: "+entry.getName());
					stream.putNextEntry(new JarEntry(entry.getName()));
					InputStream instream = file.getInputStream(entry);
					byte[] bytes = new byte[(int) entry.getSize()];
					instream.read(bytes);
					stream.write(bytes);
					instream.close();
				}
				file.close();
				
				JarFile file3 = new JarFile(dir+"\\FlameInstaller.jar");
				
				String[] needed = new String[] {
						"com/tfc/flamemc/FlameLauncher.class",
						"com/tfc/flame/FlameConfig.class",
						"com/tfc/flame/FlameURLLoader.class",
						"com/tfc/flame/IFlameMod.class",
						"com/tfc/flame/FlameLog.class"
				};
				
				for (String s:needed) {
					stream.putNextEntry(new JarEntry(s));
					field2.append(field2.getText()+"\nCreate File: "+s);
					InputStream inStream1 = file3.getInputStream(file3.getEntry(s));
					byte[] bytes = new byte[inStream1.available()];
					inStream1.read(bytes);
					stream.write(bytes);
					inStream1.close();
				}
				
				file3.close();
				
//				field2.append("\n"+text+"\\"+file1.getName()+".json");
				
//				Scanner sc = new Scanner(new File(text+"\\"+file1.getName()+".json"));
//				String json = "";
//				while (sc.hasNextLine()) {
//					String s=sc.nextLine();
//					json+=s.replace("\n","");
//				}
//				sc.close();
//
//				String remove = "";
//				int curlies = 0;
//				String jsonDownloads = json.substring(json.indexOf("\"downloads\"")+"\"downloads\"".length());
//				for (int i=0; i < jsonDownloads.length(); i++) {
//					if (jsonDownloads.charAt(i) == '{') {
//						curlies++;
//					} else if (jsonDownloads.charAt(i) == '}') {
//						curlies--;
//						if (curlies==0) break;
//					}
//					remove+=jsonDownloads.charAt(i);
//				}
////				field2.append("\n\n"+remove);
////				field2.append("\n\n"+json.contains(remove));
////				field2.append("\n\n"+json.indexOf(remove));
//				json = json.replace(remove,": {");
////				field2.append("\n\n"+json);
//
//				String jsonLibs = json.substring(json.indexOf("\"libraries\"")+"\"libraries\"".length());
//				String libsReplace = "";
//				int brackets = 0;
//				remove = "";
//				for (int i=0; i < jsonLibs.length(); i++) {
//					if (jsonLibs.charAt(i) == '{') {
//						brackets++;
//					} else if (jsonLibs.charAt(i) == '}') {
//						brackets--;
//						if (brackets==0) break;
//					}
//					remove+=jsonDownloads.charAt(i);
//				}
//
//				String jsonWithNewLibs = jsonLibs.replace(libsReplace, libsReplace+libraries);
//				json = json.replace(jsonLibs,jsonWithNewLibs.replace("\n",""));
//
//				json = json.replace(file1.getName(),file1.getName()+"-flame");
//
//				StringBuilder main = new StringBuilder();
//				String line1 = json.substring(json.indexOf("\"mainClass\"")+"\"mainClass\"".length());
//				Boolean inQuotes = false;
//				for (int i=0; i < line1.length(); i++) {
//					if (line1.charAt(i) == '"') {
//						if (!inQuotes) inQuotes = true;
//						else break;
//					} else if (inQuotes) main.append(line1.charAt(i));
//				}
//
//				json = json.replace(main,"com.tfc.flamemc.FlameLauncher");
				
				File jsonOut = new File(text+"-flame\\"+file1.getName()+"-flame.json");
				if (!jsonOut.exists()) jsonOut.createNewFile();
				FileWriter writer = new FileWriter(jsonOut);
				writer.write(json);
				writer.close();
				
				stream.close();
				
				field2.append("\nDone!");
			} catch (Throwable err) {
				for (StackTraceElement element:err.getStackTrace()) {
					field2.append("\n"+element.toString());
				}
			}
		}
	}
	
	private static final String json = "{\n" +
			"  \"arguments\": {\n" +
			"    \"game\": [\n" +
			"      \"--username\",\n" +
			"      \"${auth_player_name}\",\n" +
			"      \"--version\",\n" +
			"      \"${version_name}\",\n" +
			"      \"--gameDir\",\n" +
			"      \"${game_directory}\",\n" +
			"      \"--assetsDir\",\n" +
			"      \"${assets_root}\",\n" +
			"      \"--assetIndex\",\n" +
			"      \"${assets_index_name}\",\n" +
			"      \"--uuid\",\n" +
			"      \"${auth_uuid}\",\n" +
			"      \"--accessToken\",\n" +
			"      \"${auth_access_token}\",\n" +
			"      \"--userType\",\n" +
			"      \"${user_type}\",\n" +
			"      \"--versionType\",\n" +
			"      \"${version_type}\",\n" +
			"      {\n" +
			"        \"rules\": [\n" +
			"          {\n" +
			"            \"action\": \"allow\",\n" +
			"            \"features\": {\n" +
			"              \"is_demo_user\": true\n" +
			"            }\n" +
			"          }\n" +
			"        ],\n" +
			"        \"value\": \"--demo\"\n" +
			"      },\n" +
			"      {\n" +
			"        \"rules\": [\n" +
			"          {\n" +
			"            \"action\": \"allow\",\n" +
			"            \"features\": {\n" +
			"              \"has_custom_resolution\": true\n" +
			"            }\n" +
			"          }\n" +
			"        ],\n" +
			"        \"value\": [\n" +
			"          \"--width\",\n" +
			"          \"${resolution_width}\",\n" +
			"          \"--height\",\n" +
			"          \"${resolution_height}\"\n" +
			"        ]\n" +
			"      }\n" +
			"    ],\n" +
			"    \"jvm\": [\n" +
			"      {\n" +
			"        \"rules\": [\n" +
			"          {\n" +
			"            \"action\": \"allow\",\n" +
			"            \"os\": {\n" +
			"              \"name\": \"osx\"\n" +
			"            }\n" +
			"          }\n" +
			"        ],\n" +
			"        \"value\": [\n" +
			"          \"-XstartOnFirstThread\"\n" +
			"        ]\n" +
			"      },\n" +
			"      {\n" +
			"        \"rules\": [\n" +
			"          {\n" +
			"            \"action\": \"allow\",\n" +
			"            \"os\": {\n" +
			"              \"name\": \"windows\"\n" +
			"            }\n" +
			"          }\n" +
			"        ],\n" +
			"        \"value\": \"-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump\"\n" +
			"      },\n" +
			"      {\n" +
			"        \"rules\": [\n" +
			"          {\n" +
			"            \"action\": \"allow\",\n" +
			"            \"os\": {\n" +
			"              \"name\": \"windows\",\n" +
			"              \"version\": \"^10\\\\.\"\n" +
			"            }\n" +
			"          }\n" +
			"        ],\n" +
			"        \"value\": [\n" +
			"          \"-Dos.name=Windows 10\",\n" +
			"          \"-Dos.version=10.0\"\n" +
			"        ]\n" +
			"      },\n" +
			"      {\n" +
			"        \"rules\": [\n" +
			"          {\n" +
			"            \"action\": \"allow\",\n" +
			"            \"os\": {\n" +
			"              \"arch\": \"x86\"\n" +
			"            }\n" +
			"          }\n" +
			"        ],\n" +
			"        \"value\": \"-Xss1M\"\n" +
			"      },\n" +
			"      \"-Djava.library.path=${natives_directory}\",\n" +
			"      \"-Dminecraft.launcher.brand=${launcher_name}\",\n" +
			"      \"-Dminecraft.launcher.version=${launcher_version}\",\n" +
			"      \"-cp\",\n" +
			"      \"${classpath}\"\n" +
			"    ]\n" +
			"  },\n" +
			"  \"assetIndex\": {\n" +
			"    \"id\": \"1.15\",\n" +
			"    \"sha1\": \"bfdce6db2ba511d7dba9ef81910943d6c3d2608d\",\n" +
			"    \"size\": 235094,\n" +
			"    \"totalSize\": 222915779,\n" +
			"    \"url\": \"https://launchermeta.mojang.com/v1/packages/bfdce6db2ba511d7dba9ef81910943d6c3d2608d/1.15.json\"\n" +
			"  },\n" +
			"  \"assets\": \"1.15\",\n" +
			"  \"downloads\": {\n" +
			"  },\n" +
			"  \"id\": \"%versionid%-flame\",\n" +
			"  \"libraries\": [\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"com/mojang/patchy/1.1/patchy-1.1.jar\",\n" +
			"          \"sha1\": \"aef610b34a1be37fa851825f12372b78424d8903\",\n" +
			"          \"size\": 15817,\n" +
			"          \"url\": \"https://libraries.minecraft.net/com/mojang/patchy/1.1/patchy-1.1.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"com.mojang:patchy:1.1\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org.apache.bcel.bcel-6.0.jar\",\n" +
			"          \"sha1\": \"7d08bcac4832f81467d3e2ca5bd58ad627288656\",\n" +
			"          \"size\": 670734,\n" +
			"          \"url\": \"https://repo1.maven.org/maven2/org/apache/bcel/bcel/6.0/bcel-6.0.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.apache:bcel:6.0\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"name\": \"net.fabricmc:sponge-mixin:0.7.11.36\",\n" +
			"      \"url\": \"https://maven.fabricmc.net/\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"name\": \"org.ow2.asm:asm:7.1\",\n" +
			"      \"url\": \"https://maven.fabricmc.net/\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"name\": \"org.ow2.asm:asm-analysis:7.1\",\n" +
			"      \"url\": \"https://maven.fabricmc.net/\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"name\": \"org.ow2.asm:asm-commons:7.1\",\n" +
			"      \"url\": \"https://maven.fabricmc.net/\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"name\": \"org.ow2.asm:asm-tree:7.1\",\n" +
			"      \"url\": \"https://maven.fabricmc.net/\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"name\": \"org.ow2.asm:asm-util:7.1\",\n" +
			"      \"url\": \"https://maven.fabricmc.net/\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"oshi-project/oshi-core/1.1/oshi-core-1.1.jar\",\n" +
			"          \"sha1\": \"9ddf7b048a8d701be231c0f4f95fd986198fd2d8\",\n" +
			"          \"size\": 30973,\n" +
			"          \"url\": \"https://libraries.minecraft.net/oshi-project/oshi-core/1.1/oshi-core-1.1.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"oshi-project:oshi-core:1.1\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"net/java/dev/jna/jna/4.4.0/jna-4.4.0.jar\",\n" +
			"          \"sha1\": \"cb208278274bf12ebdb56c61bd7407e6f774d65a\",\n" +
			"          \"size\": 1091208,\n" +
			"          \"url\": \"https://libraries.minecraft.net/net/java/dev/jna/jna/4.4.0/jna-4.4.0.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"net.java.dev.jna:jna:4.4.0\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"net/java/dev/jna/platform/3.4.0/platform-3.4.0.jar\",\n" +
			"          \"sha1\": \"e3f70017be8100d3d6923f50b3d2ee17714e9c13\",\n" +
			"          \"size\": 913436,\n" +
			"          \"url\": \"https://libraries.minecraft.net/net/java/dev/jna/platform/3.4.0/platform-3.4.0.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"net.java.dev.jna:platform:3.4.0\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"com/ibm/icu/icu4j-core-mojang/51.2/icu4j-core-mojang-51.2.jar\",\n" +
			"          \"sha1\": \"63d216a9311cca6be337c1e458e587f99d382b84\",\n" +
			"          \"size\": 1634692,\n" +
			"          \"url\": \"https://libraries.minecraft.net/com/ibm/icu/icu4j-core-mojang/51.2/icu4j-core-mojang-51.2.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"com.ibm.icu:icu4j-core-mojang:51.2\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"com/mojang/javabridge/1.0.22/javabridge-1.0.22.jar\",\n" +
			"          \"sha1\": \"6aa6453aa99a52a5cd91749da1af6ab70e082ab3\",\n" +
			"          \"size\": 5111,\n" +
			"          \"url\": \"https://libraries.minecraft.net/com/mojang/javabridge/1.0.22/javabridge-1.0.22.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"com.mojang:javabridge:1.0.22\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"net/sf/jopt-simple/jopt-simple/5.0.3/jopt-simple-5.0.3.jar\",\n" +
			"          \"sha1\": \"cdd846cfc4e0f7eefafc02c0f5dce32b9303aa2a\",\n" +
			"          \"size\": 78175,\n" +
			"          \"url\": \"https://libraries.minecraft.net/net/sf/jopt-simple/jopt-simple/5.0.3/jopt-simple-5.0.3.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"net.sf.jopt-simple:jopt-simple:5.0.3\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"io/netty/netty-all/4.1.25.Final/netty-all-4.1.25.Final.jar\",\n" +
			"          \"sha1\": \"d0626cd3108294d1d58c05859add27b4ef21f83b\",\n" +
			"          \"size\": 3823147,\n" +
			"          \"url\": \"https://libraries.minecraft.net/io/netty/netty-all/4.1.25.Final/netty-all-4.1.25.Final.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"io.netty:netty-all:4.1.25.Final\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"com/google/guava/guava/21.0/guava-21.0.jar\",\n" +
			"          \"sha1\": \"3a3d111be1be1b745edfa7d91678a12d7ed38709\",\n" +
			"          \"size\": 2521113,\n" +
			"          \"url\": \"https://libraries.minecraft.net/com/google/guava/guava/21.0/guava-21.0.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"com.google.guava:guava:21.0\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org/apache/commons/commons-lang3/3.5/commons-lang3-3.5.jar\",\n" +
			"          \"sha1\": \"6c6c702c89bfff3cd9e80b04d668c5e190d588c6\",\n" +
			"          \"size\": 479881,\n" +
			"          \"url\": \"https://libraries.minecraft.net/org/apache/commons/commons-lang3/3.5/commons-lang3-3.5.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.apache.commons:commons-lang3:3.5\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"commons-io/commons-io/2.5/commons-io-2.5.jar\",\n" +
			"          \"sha1\": \"2852e6e05fbb95076fc091f6d1780f1f8fe35e0f\",\n" +
			"          \"size\": 208700,\n" +
			"          \"url\": \"https://libraries.minecraft.net/commons-io/commons-io/2.5/commons-io-2.5.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"commons-io:commons-io:2.5\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"commons-codec/commons-codec/1.10/commons-codec-1.10.jar\",\n" +
			"          \"sha1\": \"4b95f4897fa13f2cd904aee711aeafc0c5295cd8\",\n" +
			"          \"size\": 284184,\n" +
			"          \"url\": \"https://libraries.minecraft.net/commons-codec/commons-codec/1.10/commons-codec-1.10.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"commons-codec:commons-codec:1.10\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"net/java/jinput/jinput/2.0.5/jinput-2.0.5.jar\",\n" +
			"          \"sha1\": \"39c7796b469a600f72380316f6b1f11db6c2c7c4\",\n" +
			"          \"size\": 208338,\n" +
			"          \"url\": \"https://libraries.minecraft.net/net/java/jinput/jinput/2.0.5/jinput-2.0.5.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"net.java.jinput:jinput:2.0.5\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"net/java/jutils/jutils/1.0.0/jutils-1.0.0.jar\",\n" +
			"          \"sha1\": \"e12fe1fda814bd348c1579329c86943d2cd3c6a6\",\n" +
			"          \"size\": 7508,\n" +
			"          \"url\": \"https://libraries.minecraft.net/net/java/jutils/jutils/1.0.0/jutils-1.0.0.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"net.java.jutils:jutils:1.0.0\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"com/mojang/brigadier/1.0.17/brigadier-1.0.17.jar\",\n" +
			"          \"sha1\": \"c6b7dc51dd44379cc751b7504816006e9be4b1e6\",\n" +
			"          \"size\": 77392,\n" +
			"          \"url\": \"https://libraries.minecraft.net/com/mojang/brigadier/1.0.17/brigadier-1.0.17.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"com.mojang:brigadier:1.0.17\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"com/mojang/datafixerupper/2.0.24/datafixerupper-2.0.24.jar\",\n" +
			"          \"sha1\": \"0944c24a8519981847ffb36c6dcd059d96fcb4b0\",\n" +
			"          \"size\": 436066,\n" +
			"          \"url\": \"https://libraries.minecraft.net/com/mojang/datafixerupper/2.0.24/datafixerupper-2.0.24.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"com.mojang:datafixerupper:2.0.24\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"com/google/code/gson/gson/2.8.0/gson-2.8.0.jar\",\n" +
			"          \"sha1\": \"c4ba5371a29ac9b2ad6129b1d39ea38750043eff\",\n" +
			"          \"size\": 231952,\n" +
			"          \"url\": \"https://libraries.minecraft.net/com/google/code/gson/gson/2.8.0/gson-2.8.0.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"com.google.code.gson:gson:2.8.0\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"com/mojang/authlib/1.5.25/authlib-1.5.25.jar\",\n" +
			"          \"sha1\": \"9834cdf236c22e84b946bba989e2f94ef5897c3c\",\n" +
			"          \"size\": 65621,\n" +
			"          \"url\": \"https://libraries.minecraft.net/com/mojang/authlib/1.5.25/authlib-1.5.25.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"com.mojang:authlib:1.5.25\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org/apache/commons/commons-compress/1.8.1/commons-compress-1.8.1.jar\",\n" +
			"          \"sha1\": \"a698750c16740fd5b3871425f4cb3bbaa87f529d\",\n" +
			"          \"size\": 365552,\n" +
			"          \"url\": \"https://libraries.minecraft.net/org/apache/commons/commons-compress/1.8.1/commons-compress-1.8.1.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.apache.commons:commons-compress:1.8.1\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org/apache/httpcomponents/httpclient/4.3.3/httpclient-4.3.3.jar\",\n" +
			"          \"sha1\": \"18f4247ff4572a074444572cee34647c43e7c9c7\",\n" +
			"          \"size\": 589512,\n" +
			"          \"url\": \"https://libraries.minecraft.net/org/apache/httpcomponents/httpclient/4.3.3/httpclient-4.3.3.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.apache.httpcomponents:httpclient:4.3.3\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"commons-logging/commons-logging/1.1.3/commons-logging-1.1.3.jar\",\n" +
			"          \"sha1\": \"f6f66e966c70a83ffbdb6f17a0919eaf7c8aca7f\",\n" +
			"          \"size\": 62050,\n" +
			"          \"url\": \"https://libraries.minecraft.net/commons-logging/commons-logging/1.1.3/commons-logging-1.1.3.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"commons-logging:commons-logging:1.1.3\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org/apache/httpcomponents/httpcore/4.3.2/httpcore-4.3.2.jar\",\n" +
			"          \"sha1\": \"31fbbff1ddbf98f3aa7377c94d33b0447c646b6e\",\n" +
			"          \"size\": 282269,\n" +
			"          \"url\": \"https://libraries.minecraft.net/org/apache/httpcomponents/httpcore/4.3.2/httpcore-4.3.2.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.apache.httpcomponents:httpcore:4.3.2\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"it/unimi/dsi/fastutil/8.2.1/fastutil-8.2.1.jar\",\n" +
			"          \"sha1\": \"5ad88f325e424f8dbc2be5459e21ea5cab3864e9\",\n" +
			"          \"size\": 18800417,\n" +
			"          \"url\": \"https://libraries.minecraft.net/it/unimi/dsi/fastutil/8.2.1/fastutil-8.2.1.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"it.unimi.dsi:fastutil:8.2.1\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org/apache/logging/log4j/log4j-api/2.8.1/log4j-api-2.8.1.jar\",\n" +
			"          \"sha1\": \"e801d13612e22cad62a3f4f3fe7fdbe6334a8e72\",\n" +
			"          \"size\": 228859,\n" +
			"          \"url\": \"https://libraries.minecraft.net/org/apache/logging/log4j/log4j-api/2.8.1/log4j-api-2.8.1.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.apache.logging.log4j:log4j-api:2.8.1\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org/apache/logging/log4j/log4j-core/2.8.1/log4j-core-2.8.1.jar\",\n" +
			"          \"sha1\": \"4ac28ff2f1ddf05dae3043a190451e8c46b73c31\",\n" +
			"          \"size\": 1402925,\n" +
			"          \"url\": \"https://libraries.minecraft.net/org/apache/logging/log4j/log4j-core/2.8.1/log4j-core-2.8.1.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.apache.logging.log4j:log4j-core:2.8.1\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org/lwjgl/lwjgl/3.2.1/lwjgl-3.2.1.jar\",\n" +
			"          \"sha1\": \"2bb514e444994c6fece99a21f76e0c90438e377f\",\n" +
			"          \"size\": 317748,\n" +
			"          \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl/3.2.1/lwjgl-3.2.1.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.lwjgl:lwjgl:3.2.1\",\n" +
			"      \"rules\": [\n" +
			"        {\n" +
			"          \"action\": \"allow\",\n" +
			"          \"os\": {\n" +
			"            \"name\": \"osx\"\n" +
			"          }\n" +
			"        }\n" +
			"      ]\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org/lwjgl/lwjgl/3.2.2/lwjgl-3.2.2.jar\",\n" +
			"          \"sha1\": \"8ad6294407e15780b43e84929c40e4c5e997972e\",\n" +
			"          \"size\": 321900,\n" +
			"          \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl/3.2.2/lwjgl-3.2.2.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.lwjgl:lwjgl:3.2.2\",\n" +
			"      \"rules\": [\n" +
			"        {\n" +
			"          \"action\": \"allow\"\n" +
			"        },\n" +
			"        {\n" +
			"          \"action\": \"disallow\",\n" +
			"          \"os\": {\n" +
			"            \"name\": \"osx\"\n" +
			"          }\n" +
			"        }\n" +
			"      ]\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org/lwjgl/lwjgl-jemalloc/3.2.1/lwjgl-jemalloc-3.2.1.jar\",\n" +
			"          \"sha1\": \"7a0c583fcbec32b15784f846df536e1837d83666\",\n" +
			"          \"size\": 38616,\n" +
			"          \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-jemalloc/3.2.1/lwjgl-jemalloc-3.2.1.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.lwjgl:lwjgl-jemalloc:3.2.1\",\n" +
			"      \"rules\": [\n" +
			"        {\n" +
			"          \"action\": \"allow\",\n" +
			"          \"os\": {\n" +
			"            \"name\": \"osx\"\n" +
			"          }\n" +
			"        }\n" +
			"      ]\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org/lwjgl/lwjgl-jemalloc/3.2.2/lwjgl-jemalloc-3.2.2.jar\",\n" +
			"          \"sha1\": \"ee8e57a79300f78294576d87c4a587f8c99402e2\",\n" +
			"          \"size\": 34848,\n" +
			"          \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-jemalloc/3.2.2/lwjgl-jemalloc-3.2.2.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.lwjgl:lwjgl-jemalloc:3.2.2\",\n" +
			"      \"rules\": [\n" +
			"        {\n" +
			"          \"action\": \"allow\"\n" +
			"        },\n" +
			"        {\n" +
			"          \"action\": \"disallow\",\n" +
			"          \"os\": {\n" +
			"            \"name\": \"osx\"\n" +
			"          }\n" +
			"        }\n" +
			"      ]\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org/lwjgl/lwjgl-openal/3.2.1/lwjgl-openal-3.2.1.jar\",\n" +
			"          \"sha1\": \"dc7ff2dabb40a141ee9bf2e326d9b1b19f3278fb\",\n" +
			"          \"size\": 80103,\n" +
			"          \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-openal/3.2.1/lwjgl-openal-3.2.1.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.lwjgl:lwjgl-openal:3.2.1\",\n" +
			"      \"rules\": [\n" +
			"        {\n" +
			"          \"action\": \"allow\",\n" +
			"          \"os\": {\n" +
			"            \"name\": \"osx\"\n" +
			"          }\n" +
			"        }\n" +
			"      ]\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org/lwjgl/lwjgl-openal/3.2.2/lwjgl-openal-3.2.2.jar\",\n" +
			"          \"sha1\": \"2b772a102b0a11ee5f2109a5b136f4dc7c630827\",\n" +
			"          \"size\": 80012,\n" +
			"          \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-openal/3.2.2/lwjgl-openal-3.2.2.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.lwjgl:lwjgl-openal:3.2.2\",\n" +
			"      \"rules\": [\n" +
			"        {\n" +
			"          \"action\": \"allow\"\n" +
			"        },\n" +
			"        {\n" +
			"          \"action\": \"disallow\",\n" +
			"          \"os\": {\n" +
			"            \"name\": \"osx\"\n" +
			"          }\n" +
			"        }\n" +
			"      ]\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org/lwjgl/lwjgl-opengl/3.2.1/lwjgl-opengl-3.2.1.jar\",\n" +
			"          \"sha1\": \"57008c2374c5bc434b18adfef3f3653ee450ee18\",\n" +
			"          \"size\": 931322,\n" +
			"          \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-opengl/3.2.1/lwjgl-opengl-3.2.1.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.lwjgl:lwjgl-opengl:3.2.1\",\n" +
			"      \"rules\": [\n" +
			"        {\n" +
			"          \"action\": \"allow\",\n" +
			"          \"os\": {\n" +
			"            \"name\": \"osx\"\n" +
			"          }\n" +
			"        }\n" +
			"      ]\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org/lwjgl/lwjgl-opengl/3.2.2/lwjgl-opengl-3.2.2.jar\",\n" +
			"          \"sha1\": \"6ac5bb88b44c43ea195a570aab059f63da004cd8\",\n" +
			"          \"size\": 929780,\n" +
			"          \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-opengl/3.2.2/lwjgl-opengl-3.2.2.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.lwjgl:lwjgl-opengl:3.2.2\",\n" +
			"      \"rules\": [\n" +
			"        {\n" +
			"          \"action\": \"allow\"\n" +
			"        },\n" +
			"        {\n" +
			"          \"action\": \"disallow\",\n" +
			"          \"os\": {\n" +
			"            \"name\": \"osx\"\n" +
			"          }\n" +
			"        }\n" +
			"      ]\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org/lwjgl/lwjgl-glfw/3.2.1/lwjgl-glfw-3.2.1.jar\",\n" +
			"          \"sha1\": \"027abb7f64894b61cad163791acd8113f0b21296\",\n" +
			"          \"size\": 116708,\n" +
			"          \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-glfw/3.2.1/lwjgl-glfw-3.2.1.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.lwjgl:lwjgl-glfw:3.2.1\",\n" +
			"      \"rules\": [\n" +
			"        {\n" +
			"          \"action\": \"allow\",\n" +
			"          \"os\": {\n" +
			"            \"name\": \"osx\"\n" +
			"          }\n" +
			"        }\n" +
			"      ]\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org/lwjgl/lwjgl-glfw/3.2.2/lwjgl-glfw-3.2.2.jar\",\n" +
			"          \"sha1\": \"d3ad4df38e400b8afba1de63f84338809399df5b\",\n" +
			"          \"size\": 108907,\n" +
			"          \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-glfw/3.2.2/lwjgl-glfw-3.2.2.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.lwjgl:lwjgl-glfw:3.2.2\",\n" +
			"      \"rules\": [\n" +
			"        {\n" +
			"          \"action\": \"allow\"\n" +
			"        },\n" +
			"        {\n" +
			"          \"action\": \"disallow\",\n" +
			"          \"os\": {\n" +
			"            \"name\": \"osx\"\n" +
			"          }\n" +
			"        }\n" +
			"      ]\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org/lwjgl/lwjgl-stb/3.2.1/lwjgl-stb-3.2.1.jar\",\n" +
			"          \"sha1\": \"31f5eb5fce3791d58ec898bc5c1867d76d781ba1\",\n" +
			"          \"size\": 105765,\n" +
			"          \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-stb/3.2.1/lwjgl-stb-3.2.1.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.lwjgl:lwjgl-stb:3.2.1\",\n" +
			"      \"rules\": [\n" +
			"        {\n" +
			"          \"action\": \"allow\",\n" +
			"          \"os\": {\n" +
			"            \"name\": \"osx\"\n" +
			"          }\n" +
			"        }\n" +
			"      ]\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org/lwjgl/lwjgl-stb/3.2.2/lwjgl-stb-3.2.2.jar\",\n" +
			"          \"sha1\": \"3b8e6ebc5851dd3d17e37e5cadce2eff2a429f0f\",\n" +
			"          \"size\": 104469,\n" +
			"          \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-stb/3.2.2/lwjgl-stb-3.2.2.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.lwjgl:lwjgl-stb:3.2.2\",\n" +
			"      \"rules\": [\n" +
			"        {\n" +
			"          \"action\": \"allow\"\n" +
			"        },\n" +
			"        {\n" +
			"          \"action\": \"disallow\",\n" +
			"          \"os\": {\n" +
			"            \"name\": \"osx\"\n" +
			"          }\n" +
			"        }\n" +
			"      ]\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org/lwjgl/lwjgl-tinyfd/3.2.1/lwjgl-tinyfd-3.2.1.jar\",\n" +
			"          \"sha1\": \"259f1dbddb921e27e01b32458d6f584eb8bba13a\",\n" +
			"          \"size\": 7088,\n" +
			"          \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-tinyfd/3.2.1/lwjgl-tinyfd-3.2.1.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.lwjgl:lwjgl-tinyfd:3.2.1\",\n" +
			"      \"rules\": [\n" +
			"        {\n" +
			"          \"action\": \"allow\",\n" +
			"          \"os\": {\n" +
			"            \"name\": \"osx\"\n" +
			"          }\n" +
			"        }\n" +
			"      ]\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org/lwjgl/lwjgl-tinyfd/3.2.2/lwjgl-tinyfd-3.2.2.jar\",\n" +
			"          \"sha1\": \"fcbe606c8f8da6f8f9a05e2c540eb1ee8632b0e9\",\n" +
			"          \"size\": 7092,\n" +
			"          \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-tinyfd/3.2.2/lwjgl-tinyfd-3.2.2.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.lwjgl:lwjgl-tinyfd:3.2.2\",\n" +
			"      \"rules\": [\n" +
			"        {\n" +
			"          \"action\": \"allow\"\n" +
			"        },\n" +
			"        {\n" +
			"          \"action\": \"disallow\",\n" +
			"          \"os\": {\n" +
			"            \"name\": \"osx\"\n" +
			"          }\n" +
			"        }\n" +
			"      ]\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org/lwjgl/lwjgl/3.2.1/lwjgl-3.2.1.jar\",\n" +
			"          \"sha1\": \"2bb514e444994c6fece99a21f76e0c90438e377f\",\n" +
			"          \"size\": 317748,\n" +
			"          \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl/3.2.1/lwjgl-3.2.1.jar\"\n" +
			"        },\n" +
			"        \"classifiers\": {\n" +
			"          \"javadoc\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl/3.2.1/lwjgl-3.2.1-javadoc.jar\",\n" +
			"            \"sha1\": \"1f6b7050737559b775d797c0ea56612b8e373fd6\",\n" +
			"            \"size\": 1287174,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl/3.2.1/lwjgl-3.2.1-javadoc.jar\"\n" +
			"          },\n" +
			"          \"natives-linux\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl/3.2.1/lwjgl-3.2.1-natives-linux.jar\",\n" +
			"            \"sha1\": \"9bdd47cd63ce102cec837a396c8ded597cb75a66\",\n" +
			"            \"size\": 87484,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl/3.2.1/lwjgl-3.2.1-natives-linux.jar\"\n" +
			"          },\n" +
			"          \"natives-macos\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl/3.2.1/lwjgl-3.2.1-natives-macos.jar\",\n" +
			"            \"sha1\": \"5a4c271d150906858d475603dcb9479453c60555\",\n" +
			"            \"size\": 39835,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl/3.2.1/lwjgl-3.2.1-natives-macos.jar\"\n" +
			"          },\n" +
			"          \"natives-windows\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl/3.2.1/lwjgl-3.2.1-natives-windows.jar\",\n" +
			"            \"sha1\": \"e799d06b8969db0610e68776e0eff4b6191098bd\",\n" +
			"            \"size\": 255871,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl/3.2.1/lwjgl-3.2.1-natives-windows.jar\"\n" +
			"          },\n" +
			"          \"sources\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl/3.2.1/lwjgl-3.2.1-sources.jar\",\n" +
			"            \"sha1\": \"106f90ac41449004a969309488aa6e3a2f7d6731\",\n" +
			"            \"size\": 255671,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl/3.2.1/lwjgl-3.2.1-sources.jar\"\n" +
			"          }\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.lwjgl:lwjgl:3.2.1\",\n" +
			"      \"natives\": {\n" +
			"        \"osx\": \"natives-macos\"\n" +
			"      },\n" +
			"      \"rules\": [\n" +
			"        {\n" +
			"          \"action\": \"allow\",\n" +
			"          \"os\": {\n" +
			"            \"name\": \"osx\"\n" +
			"          }\n" +
			"        }\n" +
			"      ]\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org/lwjgl/lwjgl/3.2.2/lwjgl-3.2.2.jar\",\n" +
			"          \"sha1\": \"8ad6294407e15780b43e84929c40e4c5e997972e\",\n" +
			"          \"size\": 321900,\n" +
			"          \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl/3.2.2/lwjgl-3.2.2.jar\"\n" +
			"        },\n" +
			"        \"classifiers\": {\n" +
			"          \"natives-linux\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl/3.2.2/lwjgl-3.2.2-natives-linux.jar\",\n" +
			"            \"sha1\": \"ae7976827ca2a3741f6b9a843a89bacd637af350\",\n" +
			"            \"size\": 124776,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl/3.2.2/lwjgl-3.2.2-natives-linux.jar\"\n" +
			"          },\n" +
			"          \"natives-macos\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl/3.2.2/lwjgl-3.2.2-natives-macos.jar\",\n" +
			"            \"sha1\": \"bbfb75693bdb714c0c69c2c9f9be73d259b43b62\",\n" +
			"            \"size\": 48462,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl/3.2.2/lwjgl-3.2.2-natives-macos.jar\"\n" +
			"          },\n" +
			"          \"natives-windows\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl/3.2.2/lwjgl-3.2.2-natives-windows.jar\",\n" +
			"            \"sha1\": \"05359f3aa50d36352815fc662ea73e1c00d22170\",\n" +
			"            \"size\": 279593,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl/3.2.2/lwjgl-3.2.2-natives-windows.jar\"\n" +
			"          }\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.lwjgl:lwjgl:3.2.2\",\n" +
			"      \"natives\": {\n" +
			"        \"linux\": \"natives-linux\",\n" +
			"        \"windows\": \"natives-windows\"\n" +
			"      },\n" +
			"      \"rules\": [\n" +
			"        {\n" +
			"          \"action\": \"allow\"\n" +
			"        },\n" +
			"        {\n" +
			"          \"action\": \"disallow\",\n" +
			"          \"os\": {\n" +
			"            \"name\": \"osx\"\n" +
			"          }\n" +
			"        }\n" +
			"      ]\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org/lwjgl/lwjgl-jemalloc/3.2.1/lwjgl-jemalloc-3.2.1.jar\",\n" +
			"          \"sha1\": \"7a0c583fcbec32b15784f846df536e1837d83666\",\n" +
			"          \"size\": 38616,\n" +
			"          \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-jemalloc/3.2.1/lwjgl-jemalloc-3.2.1.jar\"\n" +
			"        },\n" +
			"        \"classifiers\": {\n" +
			"          \"javadoc\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-jemalloc/3.2.1/lwjgl-jemalloc-3.2.1-javadoc.jar\",\n" +
			"            \"sha1\": \"04f6897be1e2d68bff5ec5e91a2b96e32f084c09\",\n" +
			"            \"size\": 461041,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-jemalloc/3.2.1/lwjgl-jemalloc-3.2.1-javadoc.jar\"\n" +
			"          },\n" +
			"          \"natives-linux\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-jemalloc/3.2.1/lwjgl-jemalloc-3.2.1-natives-linux.jar\",\n" +
			"            \"sha1\": \"5536616b558cea2fea6330ca682fd7c733db9c43\",\n" +
			"            \"size\": 156057,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-jemalloc/3.2.1/lwjgl-jemalloc-3.2.1-natives-linux.jar\"\n" +
			"          },\n" +
			"          \"natives-macos\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-jemalloc/3.2.1/lwjgl-jemalloc-3.2.1-natives-macos.jar\",\n" +
			"            \"sha1\": \"439ab9d0264167a949cc7bcce673704322baaf50\",\n" +
			"            \"size\": 117001,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-jemalloc/3.2.1/lwjgl-jemalloc-3.2.1-natives-macos.jar\"\n" +
			"          },\n" +
			"          \"natives-windows\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-jemalloc/3.2.1/lwjgl-jemalloc-3.2.1-natives-windows.jar\",\n" +
			"            \"sha1\": \"3c869b3d7638c800b7039cd859d064658643ad6e\",\n" +
			"            \"size\": 218136,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-jemalloc/3.2.1/lwjgl-jemalloc-3.2.1-natives-windows.jar\"\n" +
			"          },\n" +
			"          \"sources\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-jemalloc/3.2.1/lwjgl-jemalloc-3.2.1-sources.jar\",\n" +
			"            \"sha1\": \"4450dca46228c02c51bb9bbda70e7cfc3154296d\",\n" +
			"            \"size\": 31279,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-jemalloc/3.2.1/lwjgl-jemalloc-3.2.1-sources.jar\"\n" +
			"          }\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.lwjgl:lwjgl-jemalloc:3.2.1\",\n" +
			"      \"natives\": {\n" +
			"        \"osx\": \"natives-macos\"\n" +
			"      },\n" +
			"      \"rules\": [\n" +
			"        {\n" +
			"          \"action\": \"allow\",\n" +
			"          \"os\": {\n" +
			"            \"name\": \"osx\"\n" +
			"          }\n" +
			"        }\n" +
			"      ]\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org/lwjgl/lwjgl-jemalloc/3.2.2/lwjgl-jemalloc-3.2.2.jar\",\n" +
			"          \"sha1\": \"ee8e57a79300f78294576d87c4a587f8c99402e2\",\n" +
			"          \"size\": 34848,\n" +
			"          \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-jemalloc/3.2.2/lwjgl-jemalloc-3.2.2.jar\"\n" +
			"        },\n" +
			"        \"classifiers\": {\n" +
			"          \"natives-linux\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-jemalloc/3.2.2/lwjgl-jemalloc-3.2.2-natives-linux.jar\",\n" +
			"            \"sha1\": \"268c08a150347e04e44ba56e359d62c9b78669df\",\n" +
			"            \"size\": 156173,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-jemalloc/3.2.2/lwjgl-jemalloc-3.2.2-natives-linux.jar\"\n" +
			"          },\n" +
			"          \"natives-macos\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-jemalloc/3.2.2/lwjgl-jemalloc-3.2.2-natives-macos.jar\",\n" +
			"            \"sha1\": \"805f5a10465375ba034b27b72331912fd2846690\",\n" +
			"            \"size\": 117127,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-jemalloc/3.2.2/lwjgl-jemalloc-3.2.2-natives-macos.jar\"\n" +
			"          },\n" +
			"          \"natives-windows\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-jemalloc/3.2.2/lwjgl-jemalloc-3.2.2-natives-windows.jar\",\n" +
			"            \"sha1\": \"338b25b99da3ba5f441f6492f2ce2a9c608860ed\",\n" +
			"            \"size\": 220623,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-jemalloc/3.2.2/lwjgl-jemalloc-3.2.2-natives-windows.jar\"\n" +
			"          }\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.lwjgl:lwjgl-jemalloc:3.2.2\",\n" +
			"      \"natives\": {\n" +
			"        \"linux\": \"natives-linux\",\n" +
			"        \"windows\": \"natives-windows\"\n" +
			"      },\n" +
			"      \"rules\": [\n" +
			"        {\n" +
			"          \"action\": \"allow\"\n" +
			"        },\n" +
			"        {\n" +
			"          \"action\": \"disallow\",\n" +
			"          \"os\": {\n" +
			"            \"name\": \"osx\"\n" +
			"          }\n" +
			"        }\n" +
			"      ]\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org/lwjgl/lwjgl-openal/3.2.1/lwjgl-openal-3.2.1.jar\",\n" +
			"          \"sha1\": \"dc7ff2dabb40a141ee9bf2e326d9b1b19f3278fb\",\n" +
			"          \"size\": 80103,\n" +
			"          \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-openal/3.2.1/lwjgl-openal-3.2.1.jar\"\n" +
			"        },\n" +
			"        \"classifiers\": {\n" +
			"          \"javadoc\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-openal/3.2.1/lwjgl-openal-3.2.1-javadoc.jar\",\n" +
			"            \"sha1\": \"95752f443686da1b3443e397dc83e730e1907a1e\",\n" +
			"            \"size\": 617869,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-openal/3.2.1/lwjgl-openal-3.2.1-javadoc.jar\"\n" +
			"          },\n" +
			"          \"natives-linux\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-openal/3.2.1/lwjgl-openal-3.2.1-natives-linux.jar\",\n" +
			"            \"sha1\": \"bcd4be67863dd908f696f628c3ca9f6eb9ae5152\",\n" +
			"            \"size\": 590716,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-openal/3.2.1/lwjgl-openal-3.2.1-natives-linux.jar\"\n" +
			"          },\n" +
			"          \"natives-macos\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-openal/3.2.1/lwjgl-openal-3.2.1-natives-macos.jar\",\n" +
			"            \"sha1\": \"9357ebfc82a0d6f64e17093dd963219367cd6fa2\",\n" +
			"            \"size\": 528004,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-openal/3.2.1/lwjgl-openal-3.2.1-natives-macos.jar\"\n" +
			"          },\n" +
			"          \"natives-windows\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-openal/3.2.1/lwjgl-openal-3.2.1-natives-windows.jar\",\n" +
			"            \"sha1\": \"92fb931e65c637cea209ad5c3ffebd1b325ed41d\",\n" +
			"            \"size\": 1310083,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-openal/3.2.1/lwjgl-openal-3.2.1-natives-windows.jar\"\n" +
			"          },\n" +
			"          \"sources\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-openal/3.2.1/lwjgl-openal-3.2.1-sources.jar\",\n" +
			"            \"sha1\": \"8fe3d6e6353685164b1eb3a22980aaa1115d4a32\",\n" +
			"            \"size\": 78379,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-openal/3.2.1/lwjgl-openal-3.2.1-sources.jar\"\n" +
			"          }\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.lwjgl:lwjgl-openal:3.2.1\",\n" +
			"      \"natives\": {\n" +
			"        \"osx\": \"natives-macos\"\n" +
			"      },\n" +
			"      \"rules\": [\n" +
			"        {\n" +
			"          \"action\": \"allow\",\n" +
			"          \"os\": {\n" +
			"            \"name\": \"osx\"\n" +
			"          }\n" +
			"        }\n" +
			"      ]\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org/lwjgl/lwjgl-openal/3.2.2/lwjgl-openal-3.2.2.jar\",\n" +
			"          \"sha1\": \"2b772a102b0a11ee5f2109a5b136f4dc7c630827\",\n" +
			"          \"size\": 80012,\n" +
			"          \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-openal/3.2.2/lwjgl-openal-3.2.2.jar\"\n" +
			"        },\n" +
			"        \"classifiers\": {\n" +
			"          \"natives-linux\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-openal/3.2.2/lwjgl-openal-3.2.2-natives-linux.jar\",\n" +
			"            \"sha1\": \"0364f9f5c3947393083ab5f37a571f5603aadd0b\",\n" +
			"            \"size\": 590997,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-openal/3.2.2/lwjgl-openal-3.2.2-natives-linux.jar\"\n" +
			"          },\n" +
			"          \"natives-macos\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-openal/3.2.2/lwjgl-openal-3.2.2-natives-macos.jar\",\n" +
			"            \"sha1\": \"a97b6345d5a9ddf889e262bd7ad8eed43b1bb063\",\n" +
			"            \"size\": 528006,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-openal/3.2.2/lwjgl-openal-3.2.2-natives-macos.jar\"\n" +
			"          },\n" +
			"          \"natives-windows\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-openal/3.2.2/lwjgl-openal-3.2.2-natives-windows.jar\",\n" +
			"            \"sha1\": \"ec20a7d42a2438528fca87e60b1705f1e2339ddb\",\n" +
			"            \"size\": 1310102,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-openal/3.2.2/lwjgl-openal-3.2.2-natives-windows.jar\"\n" +
			"          }\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.lwjgl:lwjgl-openal:3.2.2\",\n" +
			"      \"natives\": {\n" +
			"        \"linux\": \"natives-linux\",\n" +
			"        \"windows\": \"natives-windows\"\n" +
			"      },\n" +
			"      \"rules\": [\n" +
			"        {\n" +
			"          \"action\": \"allow\"\n" +
			"        },\n" +
			"        {\n" +
			"          \"action\": \"disallow\",\n" +
			"          \"os\": {\n" +
			"            \"name\": \"osx\"\n" +
			"          }\n" +
			"        }\n" +
			"      ]\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org/lwjgl/lwjgl-opengl/3.2.1/lwjgl-opengl-3.2.1.jar\",\n" +
			"          \"sha1\": \"57008c2374c5bc434b18adfef3f3653ee450ee18\",\n" +
			"          \"size\": 931322,\n" +
			"          \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-opengl/3.2.1/lwjgl-opengl-3.2.1.jar\"\n" +
			"        },\n" +
			"        \"classifiers\": {\n" +
			"          \"javadoc\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-opengl/3.2.1/lwjgl-opengl-3.2.1-javadoc.jar\",\n" +
			"            \"sha1\": \"e25fc8cbcbee68182a6b7f13ad71b1f0961005ad\",\n" +
			"            \"size\": 4307561,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-opengl/3.2.1/lwjgl-opengl-3.2.1-javadoc.jar\"\n" +
			"          },\n" +
			"          \"natives-linux\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-opengl/3.2.1/lwjgl-opengl-3.2.1-natives-linux.jar\",\n" +
			"            \"sha1\": \"c43bb08ed7dcf1a6d344803e464148b3b14dd274\",\n" +
			"            \"size\": 77401,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-opengl/3.2.1/lwjgl-opengl-3.2.1-natives-linux.jar\"\n" +
			"          },\n" +
			"          \"natives-macos\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-opengl/3.2.1/lwjgl-opengl-3.2.1-natives-macos.jar\",\n" +
			"            \"sha1\": \"dca9ad9e59a87172144d531e08ef7f6988073db0\",\n" +
			"            \"size\": 38998,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-opengl/3.2.1/lwjgl-opengl-3.2.1-natives-macos.jar\"\n" +
			"          },\n" +
			"          \"natives-windows\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-opengl/3.2.1/lwjgl-opengl-3.2.1-natives-windows.jar\",\n" +
			"            \"sha1\": \"80954961b31084d7b4f2f041d6b5a799a774c880\",\n" +
			"            \"size\": 170804,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-opengl/3.2.1/lwjgl-opengl-3.2.1-natives-windows.jar\"\n" +
			"          },\n" +
			"          \"sources\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-opengl/3.2.1/lwjgl-opengl-3.2.1-sources.jar\",\n" +
			"            \"sha1\": \"47930ffbef53c0f45c7e35c01b1c6ad5b2205809\",\n" +
			"            \"size\": 1251582,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-opengl/3.2.1/lwjgl-opengl-3.2.1-sources.jar\"\n" +
			"          }\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.lwjgl:lwjgl-opengl:3.2.1\",\n" +
			"      \"natives\": {\n" +
			"        \"osx\": \"natives-macos\"\n" +
			"      },\n" +
			"      \"rules\": [\n" +
			"        {\n" +
			"          \"action\": \"allow\",\n" +
			"          \"os\": {\n" +
			"            \"name\": \"osx\"\n" +
			"          }\n" +
			"        }\n" +
			"      ]\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org/lwjgl/lwjgl-opengl/3.2.2/lwjgl-opengl-3.2.2.jar\",\n" +
			"          \"sha1\": \"6ac5bb88b44c43ea195a570aab059f63da004cd8\",\n" +
			"          \"size\": 929780,\n" +
			"          \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-opengl/3.2.2/lwjgl-opengl-3.2.2.jar\"\n" +
			"        },\n" +
			"        \"classifiers\": {\n" +
			"          \"natives-linux\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-opengl/3.2.2/lwjgl-opengl-3.2.2-natives-linux.jar\",\n" +
			"            \"sha1\": \"338d33387919cb3f4cdba143c2b738a71ccfda60\",\n" +
			"            \"size\": 77392,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-opengl/3.2.2/lwjgl-opengl-3.2.2-natives-linux.jar\"\n" +
			"          },\n" +
			"          \"natives-macos\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-opengl/3.2.2/lwjgl-opengl-3.2.2-natives-macos.jar\",\n" +
			"            \"sha1\": \"cf4f43e69ee70d8ebfbb6ba93dec9016339e4fdc\",\n" +
			"            \"size\": 38989,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-opengl/3.2.2/lwjgl-opengl-3.2.2-natives-macos.jar\"\n" +
			"          },\n" +
			"          \"natives-windows\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-opengl/3.2.2/lwjgl-opengl-3.2.2-natives-windows.jar\",\n" +
			"            \"sha1\": \"d8dcdc91066cae2d2d8279cb4a9f9f05d9525826\",\n" +
			"            \"size\": 170798,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-opengl/3.2.2/lwjgl-opengl-3.2.2-natives-windows.jar\"\n" +
			"          }\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.lwjgl:lwjgl-opengl:3.2.2\",\n" +
			"      \"natives\": {\n" +
			"        \"linux\": \"natives-linux\",\n" +
			"        \"windows\": \"natives-windows\"\n" +
			"      },\n" +
			"      \"rules\": [\n" +
			"        {\n" +
			"          \"action\": \"allow\"\n" +
			"        },\n" +
			"        {\n" +
			"          \"action\": \"disallow\",\n" +
			"          \"os\": {\n" +
			"            \"name\": \"osx\"\n" +
			"          }\n" +
			"        }\n" +
			"      ]\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org/lwjgl/lwjgl-glfw/3.2.1/lwjgl-glfw-3.2.1.jar\",\n" +
			"          \"sha1\": \"027abb7f64894b61cad163791acd8113f0b21296\",\n" +
			"          \"size\": 116708,\n" +
			"          \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-glfw/3.2.1/lwjgl-glfw-3.2.1.jar\"\n" +
			"        },\n" +
			"        \"classifiers\": {\n" +
			"          \"javadoc\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-glfw/3.2.1/lwjgl-glfw-3.2.1-javadoc.jar\",\n" +
			"            \"sha1\": \"81482a14b617e4fb0c7de69b3e06ef2e28ef894f\",\n" +
			"            \"size\": 690774,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-glfw/3.2.1/lwjgl-glfw-3.2.1-javadoc.jar\"\n" +
			"          },\n" +
			"          \"natives-linux\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-glfw/3.2.1/lwjgl-glfw-3.2.1-natives-linux.jar\",\n" +
			"            \"sha1\": \"5a2fb27f9e12a34ecabf6f6a7606c61849e347ee\",\n" +
			"            \"size\": 157431,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-glfw/3.2.1/lwjgl-glfw-3.2.1-natives-linux.jar\"\n" +
			"          },\n" +
			"          \"natives-macos\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-glfw/3.2.1/lwjgl-glfw-3.2.1-natives-macos.jar\",\n" +
			"            \"sha1\": \"72fe6dab6110a5a1cd4833f11840eef7b2eadce5\",\n" +
			"            \"size\": 64724,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-glfw/3.2.1/lwjgl-glfw-3.2.1-natives-macos.jar\"\n" +
			"          },\n" +
			"          \"natives-windows\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-glfw/3.2.1/lwjgl-glfw-3.2.1-natives-windows.jar\",\n" +
			"            \"sha1\": \"00def7c58ad2e1cb258d6d73be181ffab8ef8bd5\",\n" +
			"            \"size\": 265304,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-glfw/3.2.1/lwjgl-glfw-3.2.1-natives-windows.jar\"\n" +
			"          },\n" +
			"          \"sources\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-glfw/3.2.1/lwjgl-glfw-3.2.1-sources.jar\",\n" +
			"            \"sha1\": \"4c56ae817da75996b19601c87d7e759b846c3902\",\n" +
			"            \"size\": 101885,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-glfw/3.2.1/lwjgl-glfw-3.2.1-sources.jar\"\n" +
			"          }\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.lwjgl:lwjgl-glfw:3.2.1\",\n" +
			"      \"natives\": {\n" +
			"        \"osx\": \"natives-macos\"\n" +
			"      },\n" +
			"      \"rules\": [\n" +
			"        {\n" +
			"          \"action\": \"allow\",\n" +
			"          \"os\": {\n" +
			"            \"name\": \"osx\"\n" +
			"          }\n" +
			"        }\n" +
			"      ]\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org/lwjgl/lwjgl-glfw/3.2.2/lwjgl-glfw-3.2.2.jar\",\n" +
			"          \"sha1\": \"d3ad4df38e400b8afba1de63f84338809399df5b\",\n" +
			"          \"size\": 108907,\n" +
			"          \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-glfw/3.2.2/lwjgl-glfw-3.2.2.jar\"\n" +
			"        },\n" +
			"        \"classifiers\": {\n" +
			"          \"natives-linux\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-glfw/3.2.2/lwjgl-glfw-3.2.2-natives-linux.jar\",\n" +
			"            \"sha1\": \"0957733f26a6661d4883da0335f7ef46d3bbbd7d\",\n" +
			"            \"size\": 159198,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-glfw/3.2.2/lwjgl-glfw-3.2.2-natives-linux.jar\"\n" +
			"          },\n" +
			"          \"natives-macos\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-glfw/3.2.2/lwjgl-glfw-3.2.2-natives-macos.jar\",\n" +
			"            \"sha1\": \"98f745038d17ac3192fcd01dc44126b03ec1570d\",\n" +
			"            \"size\": 67311,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-glfw/3.2.2/lwjgl-glfw-3.2.2-natives-macos.jar\"\n" +
			"          },\n" +
			"          \"natives-windows\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-glfw/3.2.2/lwjgl-glfw-3.2.2-natives-windows.jar\",\n" +
			"            \"sha1\": \"dc6826d636bf796b33a49038c354210e661bfc17\",\n" +
			"            \"size\": 266648,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-glfw/3.2.2/lwjgl-glfw-3.2.2-natives-windows.jar\"\n" +
			"          }\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.lwjgl:lwjgl-glfw:3.2.2\",\n" +
			"      \"natives\": {\n" +
			"        \"linux\": \"natives-linux\",\n" +
			"        \"windows\": \"natives-windows\"\n" +
			"      },\n" +
			"      \"rules\": [\n" +
			"        {\n" +
			"          \"action\": \"allow\"\n" +
			"        },\n" +
			"        {\n" +
			"          \"action\": \"disallow\",\n" +
			"          \"os\": {\n" +
			"            \"name\": \"osx\"\n" +
			"          }\n" +
			"        }\n" +
			"      ]\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org/lwjgl/lwjgl-stb/3.2.1/lwjgl-stb-3.2.1.jar\",\n" +
			"          \"sha1\": \"31f5eb5fce3791d58ec898bc5c1867d76d781ba1\",\n" +
			"          \"size\": 105765,\n" +
			"          \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-stb/3.2.1/lwjgl-stb-3.2.1.jar\"\n" +
			"        },\n" +
			"        \"classifiers\": {\n" +
			"          \"javadoc\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-stb/3.2.1/lwjgl-stb-3.2.1-javadoc.jar\",\n" +
			"            \"sha1\": \"524d79537f840d6cfe50e030d24413933f0d464b\",\n" +
			"            \"size\": 684972,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-stb/3.2.1/lwjgl-stb-3.2.1-javadoc.jar\"\n" +
			"          },\n" +
			"          \"natives-linux\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-stb/3.2.1/lwjgl-stb-3.2.1-natives-linux.jar\",\n" +
			"            \"sha1\": \"66e01b8036258619332cb452b970ca0a52db1a87\",\n" +
			"            \"size\": 197208,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-stb/3.2.1/lwjgl-stb-3.2.1-natives-linux.jar\"\n" +
			"          },\n" +
			"          \"natives-macos\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-stb/3.2.1/lwjgl-stb-3.2.1-natives-macos.jar\",\n" +
			"            \"sha1\": \"1f5615c952451c30afafba4a6e3ba4e1cd9e7f5c\",\n" +
			"            \"size\": 192364,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-stb/3.2.1/lwjgl-stb-3.2.1-natives-macos.jar\"\n" +
			"          },\n" +
			"          \"natives-windows\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-stb/3.2.1/lwjgl-stb-3.2.1-natives-windows.jar\",\n" +
			"            \"sha1\": \"d100bfd2b0d03223a043cfcb64a2dfd2bb7f4c61\",\n" +
			"            \"size\": 454473,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-stb/3.2.1/lwjgl-stb-3.2.1-natives-windows.jar\"\n" +
			"          },\n" +
			"          \"sources\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-stb/3.2.1/lwjgl-stb-3.2.1-sources.jar\",\n" +
			"            \"sha1\": \"50ac43d4c6ea5846f354f9576134c0f9264345c2\",\n" +
			"            \"size\": 96479,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-stb/3.2.1/lwjgl-stb-3.2.1-sources.jar\"\n" +
			"          }\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.lwjgl:lwjgl-stb:3.2.1\",\n" +
			"      \"natives\": {\n" +
			"        \"osx\": \"natives-macos\"\n" +
			"      },\n" +
			"      \"rules\": [\n" +
			"        {\n" +
			"          \"action\": \"allow\",\n" +
			"          \"os\": {\n" +
			"            \"name\": \"osx\"\n" +
			"          }\n" +
			"        }\n" +
			"      ]\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org/lwjgl/lwjgl-tinyfd/3.2.2/lwjgl-tinyfd-3.2.2.jar\",\n" +
			"          \"sha1\": \"fcbe606c8f8da6f8f9a05e2c540eb1ee8632b0e9\",\n" +
			"          \"size\": 7092,\n" +
			"          \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-tinyfd/3.2.2/lwjgl-tinyfd-3.2.2.jar\"\n" +
			"        },\n" +
			"        \"classifiers\": {\n" +
			"          \"javadoc\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-tinyfd/3.2.2/lwjgl-tinyfd-3.2.2-javadoc.jar\",\n" +
			"            \"sha1\": \"ba657a222ee267b75fa81ae5ab29ae29b50f725f\",\n" +
			"            \"size\": 368913,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-tinyfd/3.2.2/lwjgl-tinyfd-3.2.2-javadoc.jar\"\n" +
			"          },\n" +
			"          \"natives-linux\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-tinyfd/3.2.2/lwjgl-tinyfd-3.2.2-natives-linux.jar\",\n" +
			"            \"sha1\": \"39e35b161c130635d9c8918ce04e887a30c5b687\",\n" +
			"            \"size\": 38804,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-tinyfd/3.2.2/lwjgl-tinyfd-3.2.2-natives-linux.jar\"\n" +
			"          },\n" +
			"          \"natives-macos\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-tinyfd/3.2.2/lwjgl-tinyfd-3.2.2-natives-macos.jar\",\n" +
			"            \"sha1\": \"46d0798228b8a28e857a2a0f02310fd6ba2a4eab\",\n" +
			"            \"size\": 42136,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-tinyfd/3.2.2/lwjgl-tinyfd-3.2.2-natives-macos.jar\"\n" +
			"          },\n" +
			"          \"natives-windows\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-tinyfd/3.2.2/lwjgl-tinyfd-3.2.2-natives-windows.jar\",\n" +
			"            \"sha1\": \"e9115958773644e863332a6a06488d26f9e1fc9f\",\n" +
			"            \"size\": 208314,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-tinyfd/3.2.2/lwjgl-tinyfd-3.2.2-natives-windows.jar\"\n" +
			"          },\n" +
			"          \"sources\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-tinyfd/3.2.2/lwjgl-tinyfd-3.2.2-sources.jar\",\n" +
			"            \"sha1\": \"2fe76dcf2ca02ae0e64ac7c69eb251c09df0e922\",\n" +
			"            \"size\": 5034,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-tinyfd/3.2.2/lwjgl-tinyfd-3.2.2-sources.jar\"\n" +
			"          }\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.lwjgl:lwjgl-tinyfd:3.2.2\",\n" +
			"      \"natives\": {\n" +
			"        \"linux\": \"natives-linux\",\n" +
			"        \"windows\": \"natives-windows\"\n" +
			"      },\n" +
			"      \"rules\": [\n" +
			"        {\n" +
			"          \"action\": \"allow\"\n" +
			"        },\n" +
			"        {\n" +
			"          \"action\": \"disallow\",\n" +
			"          \"os\": {\n" +
			"            \"name\": \"osx\"\n" +
			"          }\n" +
			"        }\n" +
			"      ]\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org/lwjgl/lwjgl-tinyfd/3.2.1/lwjgl-tinyfd-3.2.1.jar\",\n" +
			"          \"sha1\": \"259f1dbddb921e27e01b32458d6f584eb8bba13a\",\n" +
			"          \"size\": 7088,\n" +
			"          \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-tinyfd/3.2.1/lwjgl-tinyfd-3.2.1.jar\"\n" +
			"        },\n" +
			"        \"classifiers\": {\n" +
			"          \"javadoc\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-tinyfd/3.2.1/lwjgl-tinyfd-3.2.1-javadoc.jar\",\n" +
			"            \"sha1\": \"0a85d995178cdab6b94d9a172dd9e7d2a0d70cfb\",\n" +
			"            \"size\": 368913,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-tinyfd/3.2.1/lwjgl-tinyfd-3.2.1-javadoc.jar\"\n" +
			"          },\n" +
			"          \"natives-linux\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-tinyfd/3.2.1/lwjgl-tinyfd-3.2.1-natives-linux.jar\",\n" +
			"            \"sha1\": \"4ad49108397322596d7b85c2c687e5de6ee52157\",\n" +
			"            \"size\": 38192,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-tinyfd/3.2.1/lwjgl-tinyfd-3.2.1-natives-linux.jar\"\n" +
			"          },\n" +
			"          \"natives-macos\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-tinyfd/3.2.1/lwjgl-tinyfd-3.2.1-natives-macos.jar\",\n" +
			"            \"sha1\": \"759c2fd9cc5c6ce0b5b7af77ac8200483b7fb660\",\n" +
			"            \"size\": 41962,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-tinyfd/3.2.1/lwjgl-tinyfd-3.2.1-natives-macos.jar\"\n" +
			"          },\n" +
			"          \"natives-windows\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-tinyfd/3.2.1/lwjgl-tinyfd-3.2.1-natives-windows.jar\",\n" +
			"            \"sha1\": \"85750d2ca022852e15f58c0b94b3d1d4e7f0ba52\",\n" +
			"            \"size\": 207577,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-tinyfd/3.2.1/lwjgl-tinyfd-3.2.1-natives-windows.jar\"\n" +
			"          },\n" +
			"          \"sources\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-tinyfd/3.2.1/lwjgl-tinyfd-3.2.1-sources.jar\",\n" +
			"            \"sha1\": \"c375699fd794c4c87d935e0f9a84e7d80d0de77e\",\n" +
			"            \"size\": 5034,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-tinyfd/3.2.1/lwjgl-tinyfd-3.2.1-sources.jar\"\n" +
			"          }\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.lwjgl:lwjgl-tinyfd:3.2.1\",\n" +
			"      \"natives\": {\n" +
			"        \"osx\": \"natives-macos\"\n" +
			"      },\n" +
			"      \"rules\": [\n" +
			"        {\n" +
			"          \"action\": \"allow\",\n" +
			"          \"os\": {\n" +
			"            \"name\": \"osx\"\n" +
			"          }\n" +
			"        }\n" +
			"      ]\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"org/lwjgl/lwjgl-stb/3.2.2/lwjgl-stb-3.2.2.jar\",\n" +
			"          \"sha1\": \"3b8e6ebc5851dd3d17e37e5cadce2eff2a429f0f\",\n" +
			"          \"size\": 104469,\n" +
			"          \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-stb/3.2.2/lwjgl-stb-3.2.2.jar\"\n" +
			"        },\n" +
			"        \"classifiers\": {\n" +
			"          \"natives-linux\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-stb/3.2.2/lwjgl-stb-3.2.2-natives-linux.jar\",\n" +
			"            \"sha1\": \"172c52e586fecf43f759bc4f70a778c01f6fdcc1\",\n" +
			"            \"size\": 203476,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-stb/3.2.2/lwjgl-stb-3.2.2-natives-linux.jar\"\n" +
			"          },\n" +
			"          \"natives-macos\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-stb/3.2.2/lwjgl-stb-3.2.2-natives-macos.jar\",\n" +
			"            \"sha1\": \"ee059b129b09fdecbd8595273926ae930bf5a5d7\",\n" +
			"            \"size\": 196796,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-stb/3.2.2/lwjgl-stb-3.2.2-natives-macos.jar\"\n" +
			"          },\n" +
			"          \"natives-windows\": {\n" +
			"            \"path\": \"org/lwjgl/lwjgl-stb/3.2.2/lwjgl-stb-3.2.2-natives-windows.jar\",\n" +
			"            \"sha1\": \"811f705cbb29e8ae8d60bdf8fdd38c0c123ad3ef\",\n" +
			"            \"size\": 465810,\n" +
			"            \"url\": \"https://libraries.minecraft.net/org/lwjgl/lwjgl-stb/3.2.2/lwjgl-stb-3.2.2-natives-windows.jar\"\n" +
			"          }\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"org.lwjgl:lwjgl-stb:3.2.2\",\n" +
			"      \"natives\": {\n" +
			"        \"linux\": \"natives-linux\",\n" +
			"        \"windows\": \"natives-windows\"\n" +
			"      },\n" +
			"      \"rules\": [\n" +
			"        {\n" +
			"          \"action\": \"allow\"\n" +
			"        },\n" +
			"        {\n" +
			"          \"action\": \"disallow\",\n" +
			"          \"os\": {\n" +
			"            \"name\": \"osx\"\n" +
			"          }\n" +
			"        }\n" +
			"      ]\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"com/mojang/text2speech/1.11.3/text2speech-1.11.3.jar\",\n" +
			"          \"sha1\": \"f378f889797edd7df8d32272c06ca80a1b6b0f58\",\n" +
			"          \"size\": 13164,\n" +
			"          \"url\": \"https://libraries.minecraft.net/com/mojang/text2speech/1.11.3/text2speech-1.11.3.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"com.mojang:text2speech:1.11.3\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"com/mojang/text2speech/1.11.3/text2speech-1.11.3.jar\",\n" +
			"          \"sha1\": \"f378f889797edd7df8d32272c06ca80a1b6b0f58\",\n" +
			"          \"size\": 13164,\n" +
			"          \"url\": \"https://libraries.minecraft.net/com/mojang/text2speech/1.11.3/text2speech-1.11.3.jar\"\n" +
			"        },\n" +
			"        \"classifiers\": {\n" +
			"          \"natives-linux\": {\n" +
			"            \"path\": \"com/mojang/text2speech/1.11.3/text2speech-1.11.3-natives-linux.jar\",\n" +
			"            \"sha1\": \"ac641755a2a841d1fca9e660194f42523ee5cfe0\",\n" +
			"            \"size\": 7833,\n" +
			"            \"url\": \"https://libraries.minecraft.net/com/mojang/text2speech/1.11.3/text2speech-1.11.3-natives-linux.jar\"\n" +
			"          },\n" +
			"          \"natives-windows\": {\n" +
			"            \"path\": \"com/mojang/text2speech/1.11.3/text2speech-1.11.3-natives-windows.jar\",\n" +
			"            \"sha1\": \"c0b242c0091be5acbf303263c7eeeaedd70544c7\",\n" +
			"            \"size\": 81379,\n" +
			"            \"url\": \"https://libraries.minecraft.net/com/mojang/text2speech/1.11.3/text2speech-1.11.3-natives-windows.jar\"\n" +
			"          },\n" +
			"          \"sources\": {\n" +
			"            \"path\": \"com/mojang/text2speech/1.11.3/text2speech-1.11.3-sources.jar\",\n" +
			"            \"sha1\": \"772a37dd77417571e6f119a8d306f0c14c2ee410\",\n" +
			"            \"size\": 5332,\n" +
			"            \"url\": \"https://libraries.minecraft.net/com/mojang/text2speech/1.11.3/text2speech-1.11.3-sources.jar\"\n" +
			"          }\n" +
			"        }\n" +
			"      },\n" +
			"      \"extract\": {\n" +
			"        \"exclude\": [\n" +
			"          \"META-INF/\"\n" +
			"        ]\n" +
			"      },\n" +
			"      \"name\": \"com.mojang:text2speech:1.11.3\",\n" +
			"      \"natives\": {\n" +
			"        \"linux\": \"natives-linux\",\n" +
			"        \"windows\": \"natives-windows\"\n" +
			"      }\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"ca/weblite/java-objc-bridge/1.0.0/java-objc-bridge-1.0.0.jar\",\n" +
			"          \"sha1\": \"6ef160c3133a78de015830860197602ca1c855d3\",\n" +
			"          \"size\": 40502,\n" +
			"          \"url\": \"https://libraries.minecraft.net/ca/weblite/java-objc-bridge/1.0.0/java-objc-bridge-1.0.0.jar\"\n" +
			"        },\n" +
			"        \"classifiers\": {\n" +
			"          \"javadoc\": {\n" +
			"            \"path\": \"ca/weblite/java-objc-bridge/1.0.0/java-objc-bridge-1.0.0-javadoc.jar\",\n" +
			"            \"sha1\": \"fb0092f22cb4fe8e631452f577b7a238778abf2a\",\n" +
			"            \"size\": 174060,\n" +
			"            \"url\": \"https://libraries.minecraft.net/ca/weblite/java-objc-bridge/1.0.0/java-objc-bridge-1.0.0-javadoc.jar\"\n" +
			"          },\n" +
			"          \"natives-osx\": {\n" +
			"            \"path\": \"ca/weblite/java-objc-bridge/1.0.0/java-objc-bridge-1.0.0-natives-osx.jar\",\n" +
			"            \"sha1\": \"08befab4894d55875f33c3d300f4f71e6e828f64\",\n" +
			"            \"size\": 5629,\n" +
			"            \"url\": \"https://libraries.minecraft.net/ca/weblite/java-objc-bridge/1.0.0/java-objc-bridge-1.0.0-natives-osx.jar\"\n" +
			"          },\n" +
			"          \"sources\": {\n" +
			"            \"path\": \"ca/weblite/java-objc-bridge/1.0.0/java-objc-bridge-1.0.0-sources.jar\",\n" +
			"            \"sha1\": \"865837a198189aee737019561ece842827f24278\",\n" +
			"            \"size\": 43283,\n" +
			"            \"url\": \"https://libraries.minecraft.net/ca/weblite/java-objc-bridge/1.0.0/java-objc-bridge-1.0.0-sources.jar\"\n" +
			"          }\n" +
			"        }\n" +
			"      },\n" +
			"      \"extract\": {\n" +
			"        \"exclude\": [\n" +
			"          \"META-INF/\"\n" +
			"        ]\n" +
			"      },\n" +
			"      \"name\": \"ca.weblite:java-objc-bridge:1.0.0\",\n" +
			"      \"natives\": {\n" +
			"        \"osx\": \"natives-osx\"\n" +
			"      },\n" +
			"      \"rules\": [\n" +
			"        {\n" +
			"          \"action\": \"allow\",\n" +
			"          \"os\": {\n" +
			"            \"name\": \"osx\"\n" +
			"          }\n" +
			"        }\n" +
			"      ]\n" +
			"    },\n" +
			"    {\n" +
			"      \"downloads\": {\n" +
			"        \"artifact\": {\n" +
			"          \"path\": \"ca/weblite/java-objc-bridge/1.0.0/java-objc-bridge-1.0.0.jar\",\n" +
			"          \"sha1\": \"6ef160c3133a78de015830860197602ca1c855d3\",\n" +
			"          \"size\": 40502,\n" +
			"          \"url\": \"https://libraries.minecraft.net/ca/weblite/java-objc-bridge/1.0.0/java-objc-bridge-1.0.0.jar\"\n" +
			"        }\n" +
			"      },\n" +
			"      \"name\": \"ca.weblite:java-objc-bridge:1.0.0\",\n" +
			"      \"rules\": [\n" +
			"        {\n" +
			"          \"action\": \"allow\",\n" +
			"          \"os\": {\n" +
			"            \"name\": \"osx\"\n" +
			"          }\n" +
			"        }\n" +
			"      ]\n" +
			"    }\n" +
			"  ],\n" +
			"  \"logging\": {\n" +
			"    \"client\": {\n" +
			"      \"argument\": \"-Dlog4j.configurationFile=${path}\",\n" +
			"      \"file\": {\n" +
			"        \"id\": \"client-1.12.xml\",\n" +
			"        \"sha1\": \"ef4f57b922df243d0cef096efe808c72db042149\",\n" +
			"        \"size\": 877,\n" +
			"        \"url\": \"https://launcher.mojang.com/v1/objects/ef4f57b922df243d0cef096efe808c72db042149/client-1.12.xml\"\n" +
			"      },\n" +
			"      \"type\": \"log4j2-xml\"\n" +
			"    }\n" +
			"  },\n" +
			"  \"mainClass\": \"com.tfc.flamemc.FlameLauncher\",\n" +
			"  \"minimumLauncherVersion\": 21,\n" +
			"  \"releaseTime\": \"2020-01-17T10:03:52+00:00\",\n" +
			"  \"time\": \"2020-01-17T10:03:52+00:00\",\n" +
			"  \"type\": \"release\"\n" +
			"}";
}
