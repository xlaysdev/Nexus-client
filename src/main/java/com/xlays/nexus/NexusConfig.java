package com.xlays.nexus;

import com.xlays.nexus.modules.render.VisualSettings;
import com.xlays.nexus.modules.render.OreESP;
import com.xlays.nexus.modules.render.ChestESP;

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
        NexusClient.MODULES.all().forEach(f -> p.setProperty("module." + f.name, Boolean.toString(f.enabled)));
        p.setProperty("hud.x", Integer.toString(NexusClient.hudX));
        p.setProperty("hud.y", Integer.toString(NexusClient.hudY));
        p.setProperty("hud.scale", Integer.toString(NexusClient.hudScale));
        p.setProperty("theme.accent", Integer.toString(NexusClient.accentIndex));
        p.setProperty("hud.showBackground", Boolean.toString(NexusClient.hudBackground));
        p.setProperty("visual.enemyColor", Integer.toString(VisualSettings.enemyColor));
        p.setProperty("visual.friendColor", Integer.toString(VisualSettings.friendColor));
        p.setProperty("visual.blockColor", Integer.toString(VisualSettings.blockColor));
        p.setProperty("visual.crosshairColor", Integer.toString(VisualSettings.crosshairColor));
        p.setProperty("visual.crosshairSize", Integer.toString(VisualSettings.crosshairSize));
        p.setProperty("visual.esp3d", Boolean.toString(VisualSettings.esp3d));
        p.setProperty("visual.nametags", Boolean.toString(VisualSettings.showNametags));
        p.setProperty("visual.tracers", Boolean.toString(VisualSettings.showTracers));
        p.setProperty("visual.scanDiamond", Boolean.toString(VisualSettings.scanDiamond));
        p.setProperty("visual.scanDebris", Boolean.toString(VisualSettings.scanDebris));
        p.setProperty("visual.scanChests", Boolean.toString(VisualSettings.scanChests));
        p.setProperty("ore.radius", Integer.toString(OreESP.getRadius()));
        NexusClient.MODULES.get("ChestESP").filter(ChestESP.class::isInstance).map(ChestESP.class::cast).ifPresent(m -> p.setProperty("chest.radius", Integer.toString(m.radius())));
        for (OreESP.OreGroup group : OreESP.OreGroup.values()) {
            p.setProperty("ore." + group.name(), Boolean.toString(OreESP.isEnabled(group)));
        }
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
            NexusClient.MODULES.all().forEach(f ->
                f.setEnabled(Boolean.parseBoolean(p.getProperty("module." + f.name, Boolean.toString(f.enabled)))));
            NexusClient.hudX = clamp(parse(p, "hud.x", NexusClient.hudX), 0, 10000);
            NexusClient.hudY = clamp(parse(p, "hud.y", NexusClient.hudY), 0, 10000);
            NexusClient.hudScale = clamp(parse(p, "hud.scale", NexusClient.hudScale), 75, 150);
            NexusClient.accentIndex = clamp(parse(p, "theme.accent", NexusClient.accentIndex), 0, NexusClient.ACCENTS.length - 1);
            NexusClient.hudBackground = Boolean.parseBoolean(p.getProperty("hud.showBackground", Boolean.toString(NexusClient.hudBackground)));
            VisualSettings.enemyColor = parse(p, "visual.enemyColor", VisualSettings.enemyColor);
            VisualSettings.friendColor = parse(p, "visual.friendColor", VisualSettings.friendColor);
            VisualSettings.blockColor = parse(p, "visual.blockColor", VisualSettings.blockColor);
            VisualSettings.crosshairColor = parse(p, "visual.crosshairColor", VisualSettings.crosshairColor);
            VisualSettings.crosshairSize = clamp(parse(p, "visual.crosshairSize", VisualSettings.crosshairSize), 2, 14);
            VisualSettings.esp3d = Boolean.parseBoolean(p.getProperty("visual.esp3d", Boolean.toString(VisualSettings.esp3d)));
            VisualSettings.showNametags = Boolean.parseBoolean(p.getProperty("visual.nametags", Boolean.toString(VisualSettings.showNametags)));
            VisualSettings.showTracers = Boolean.parseBoolean(p.getProperty("visual.tracers", Boolean.toString(VisualSettings.showTracers)));
            VisualSettings.scanDiamond = Boolean.parseBoolean(p.getProperty("visual.scanDiamond", Boolean.toString(VisualSettings.scanDiamond)));
            VisualSettings.scanDebris = Boolean.parseBoolean(p.getProperty("visual.scanDebris", Boolean.toString(VisualSettings.scanDebris)));
            VisualSettings.scanChests = Boolean.parseBoolean(p.getProperty("visual.scanChests", Boolean.toString(VisualSettings.scanChests)));
            OreESP.setRadius(clamp(parse(p, "ore.radius", OreESP.getRadius()), 4, 32));
            NexusClient.MODULES.get("ChestESP").filter(ChestESP.class::isInstance).map(ChestESP.class::cast).ifPresent(m -> m.radius(clamp(parse(p, "chest.radius", m.radius()), 4, 48)));
            for (OreESP.OreGroup group : OreESP.OreGroup.values()) {
                OreESP.setEnabled(group, Boolean.parseBoolean(p.getProperty("ore." + group.name(), Boolean.toString(OreESP.isEnabled(group)))));
            }
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
