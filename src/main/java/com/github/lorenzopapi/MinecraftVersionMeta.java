package com.github.lorenzopapi;

import java.util.List;

public class MinecraftVersionMeta {

    public LatestVersion latest;
    public List<Version> versions;

    public static class LatestVersion {
        public String release;
        public String snapshot;
    }

    public static class Version {
        public String id;
        public String type;
        public String url;
        public String time;
        public String releaseTime;
    }
}
