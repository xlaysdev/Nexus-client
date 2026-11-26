package com.xlays.nexus.modules;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class ModuleManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<String, Module> modules = new LinkedHashMap<>();

    public <T extends Module> T register(T module) {
        module.setEnabled(false);
        String key = module.name.toLowerCase(java.util.Locale.ROOT);
        if (modules.putIfAbsent(key, module) != null) {
            throw new IllegalArgumentException("Duplicate module: " + module.name);
        }
        return module;
    }

    public Optional<Module> get(String name) {
        return Optional.ofNullable(modules.get(name.toLowerCase(java.util.Locale.ROOT)));
    }

    public Collection<Module> all() {
        return Collections.unmodifiableCollection(modules.values());
    }

    public Collection<Module> inCategory(Module.Category category) {
        return modules.values().stream().filter(m -> m.category == category).toList();
    }

    public boolean isEnabled(String name) {
        return get(name).map(m -> m.enabled).orElse(false);
    }

    public void setEnabled(String name, boolean enabled) {
        get(name).ifPresent(m -> m.setEnabled(enabled));
    }

    public void toggle(String name) {
        get(name).ifPresent(Module::toggle);
    }

    public void tick() {
        for (Module module : modules.values()) {
            if (!module.enabled) continue;
            try {
                module.onTick();
            } catch (RuntimeException exception) {
                quarantine(module, "tick", exception);
            }
        }
    }

    public void render() {
        for (Module module : modules.values()) {
            if (!module.enabled) continue;
            try {
                module.onRender();
            } catch (RuntimeException exception) {
                quarantine(module, "render", exception);
            }
        }
    }

    private void quarantine(Module module, String phase, RuntimeException cause) {
        LOGGER.error("NEXUS disabled module '{}' after an exception during {}", module.name, phase, cause);
        try {
            module.setEnabled(false);
        } catch (RuntimeException disableFailure) {
            LOGGER.error("NEXUS failed to cleanly disable module '{}'", module.name, disableFailure);
            module.enabled = false;
        }
    }
}
