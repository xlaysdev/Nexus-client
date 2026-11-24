package com.xlays.nexus;

import com.xlays.nexus.visual.VisualModules;

import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

public final class NexusConfig {
    private static final Path ROOT = FMLPaths.CONFIGDIR.get().resolve("nexus");
    private static final Path PROFILES = ROOT.resolve("profiles");
    private static final Pattern SAFE_NAME = Pattern.compile("[a-zA-Z0-9_-]{1,24}");

    private NexusConfig() {}

    public static void load() {
        read(ROOT.resolve("settings.properties"));
    }

    public static void save() {
        write(ROOT.resolve("settings.properties"));
    }

    public static List<String> profiles() {
        try {
            Files.createDirectories(PROFILES);
            try (var files = Files.list(PROFILES)) {
                return files.filter(p -> p.getFileName().toString().endsWith(".properties"))
                    .map(p -> p.getFileName().toString().replaceFirst("\\.properties$", ""))
                    .sorted().toList();
            }
        } catch (IOException e) {
            System.err.println("[NEXUS] Could not list profiles: " + e.getMessage());
            return List.of();
        }
    }

    public static boolean saveProfile(String name) {
        if (!SAFE_NAME.matcher(name).matches()) return false;
        return write(PROFILES.resolve(name + ".properties"));
    }

    public static boolean loadProfile(String name) {
        if (!SAFE_NAME.matcher(name).matches()) return false;
        Path path = PROFILES.resolve(name + ".properties");
        if (!Files.isRegularFile(path)) return false;
        read(path);
        save();
        return true;
    }

    public static boolean deleteProfile(String name) {
        if (!SAFE_NAME.matcher(name).matches()) return false;
        try { return Files.deleteIfExists(PROFILES.resolve(name + ".properties")); }
        catch (IOException e) {
            System.err.println("[NEXUS] Could not delete profile: " + e.getMessage());
            return false;
        }
    }

    private static Properties snapshot() {
        Properties p = new Properties();
        NexusClient.FEATURES.values().forEach(f -> p.setProperty("module." + f.name, Boolean.toString(f.enabled)));
        p.setProperty("hud.x", Integer.toString(NexusClient.hudX));
        p.setProperty("hud.y", Integer.toString(NexusClient.hudY));
        p.setProperty("hud.scale", Integer.toString(NexusClient.hudScale));
        p.setProperty("theme.accent", Integer.toString(NexusClient.accentIndex));
        p.setProperty("hud.showBackground", Boolean.toString(NexusClient.hudBackground));
        p.setProperty("visual.enemyColor", Integer.toString(VisualModules.enemyColor));
        p.setProperty("visual.friendColor", Integer.toString(VisualModules.friendColor));
        p.setProperty("visual.blockColor", Integer.toString(VisualModules.blockColor));
        p.setProperty("visual.crosshairColor", Integer.toString(VisualModules.crosshairColor));
        p.setProperty("visual.crosshairSize", Integer.toString(VisualModules.crosshairSize));
        p.setProperty("visual.esp3d", Boolean.toString(VisualModules.esp3d));
        p.setProperty("visual.nametags", Boolean.toString(VisualModules.showNametags));
        p.setProperty("visual.tracers", Boolean.toString(VisualModules.showTracers));
        p.setProperty("visual.scanDiamond", Boolean.toString(VisualModules.scanDiamond));
        p.setProperty("visual.scanDebris", Boolean.toString(VisualModules.scanDebris));
        p.setProperty("visual.scanChests", Boolean.toString(VisualModules.scanChests));
        return p;
    }

    private static boolean write(Path path) {
        try {
            Files.createDirectories(path.getParent());
            try (OutputStream out = Files.newOutputStream(path)) {
                snapshot().store(out, "NEXUS V1 configuration");
            }
            return true;
        } catch (IOException e) {
            System.err.println("[NEXUS] Could not save config: " + e.getMessage());
            return false;
        }
    }

    private static void read(Path path) {
        if (!Files.isRegularFile(path)) return;
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            p.load(in);
            NexusClient.FEATURES.values().forEach(f ->
                f.enabled = Boolean.parseBoolean(p.getProperty("module." + f.name, Boolean.toString(f.enabled))));
            NexusClient.hudX = clamp(parse(p, "hud.x", NexusClient.hudX), 0, 10000);
            NexusClient.hudY = clamp(parse(p, "hud.y", NexusClient.hudY), 0, 10000);
            NexusClient.hudScale = clamp(parse(p, "hud.scale", NexusClient.hudScale), 75, 150);
            NexusClient.accentIndex = clamp(parse(p, "theme.accent", NexusClient.accentIndex), 0, NexusClient.ACCENTS.length - 1);
            NexusClient.hudBackground = Boolean.parseBoolean(p.getProperty("hud.showBackground", Boolean.toString(NexusClient.hudBackground)));
            VisualModules.enemyColor = parse(p, "visual.enemyColor", VisualModules.enemyColor);
            VisualModules.friendColor = parse(p, "visual.friendColor", VisualModules.friendColor);
            VisualModules.blockColor = parse(p, "visual.blockColor", VisualModules.blockColor);
            VisualModules.crosshairColor = parse(p, "visual.crosshairColor", VisualModules.crosshairColor);
            VisualModules.crosshairSize = clamp(parse(p, "visual.crosshairSize", VisualModules.crosshairSize), 2, 14);
            VisualModules.esp3d = Boolean.parseBoolean(p.getProperty("visual.esp3d", Boolean.toString(VisualModules.esp3d)));
            VisualModules.showNametags = Boolean.parseBoolean(p.getProperty("visual.nametags", Boolean.toString(VisualModules.showNametags)));
            VisualModules.showTracers = Boolean.parseBoolean(p.getProperty("visual.tracers", Boolean.toString(VisualModules.showTracers)));
            VisualModules.scanDiamond = Boolean.parseBoolean(p.getProperty("visual.scanDiamond", Boolean.toString(VisualModules.scanDiamond)));
            VisualModules.scanDebris = Boolean.parseBoolean(p.getProperty("visual.scanDebris", Boolean.toString(VisualModules.scanDebris)));
            VisualModules.scanChests = Boolean.parseBoolean(p.getProperty("visual.scanChests", Boolean.toString(VisualModules.scanChests)));
        } catch (IOException e) {
            System.err.println("[NEXUS] Could not load config: " + e.getMessage());
        }
    }

    private static int parse(Properties p, String key, int fallback) {
        try { return Integer.parseInt(p.getProperty(key, Integer.toString(fallback))); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
