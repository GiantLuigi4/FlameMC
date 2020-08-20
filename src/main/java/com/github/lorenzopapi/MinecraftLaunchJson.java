package com.github.lorenzopapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MinecraftLaunchJson {

	public String id;
	public String inheritsFrom;
	public String releaseTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date());
	public String time = releaseTime;
	public String type = "release";
	public String mainClass;
	public Arguments arguments = new Arguments();
	public Downloads downloads = new Downloads();
	public List<Library> libraries = new ArrayList<>();

	public MinecraftLaunchJson(String id, String inheritsFrom, String mainClass) {
		this.id = id;
		this.inheritsFrom = inheritsFrom;
		this.mainClass = mainClass;
	}

	public static class Library {
		public String name;
		public String url;

		public Library(String name, String url) {
			this.name = name;
			this.url = url;
		}
	}

	public static class Arguments {
		public List<String> game = new ArrayList<>();
	}

	public static class Downloads {}

}
