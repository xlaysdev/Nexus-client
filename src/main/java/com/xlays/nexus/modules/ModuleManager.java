package com.xlays.nexus.modules;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class ModuleManager {
    private final Map<String, Module> modules = new LinkedHashMap<>();

    public <T extends Module> T register(T module) {
        String key = module.name.toLowerCase(java.util.Locale.ROOT);
        if (modules.putIfAbsent(key, module) != null)
            throw new IllegalArgumentException("Duplicate module: " + module.name);
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
        for (Module module : modules.values()) if (module.enabled) module.onTick();
    }

    public void render() {
        for (Module module : modules.values()) if (module.enabled) module.onRender();
    }
}
