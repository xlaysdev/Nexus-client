package com.xlays.nexus.modules;

import java.util.Locale;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

public abstract class Module {
    public enum Category { COMBAT, HUD, MOVEMENT, PLAYER, RENDER, MISC, SETTINGS, PROFILES }

    public final String name;
    public final Category category;
    public boolean enabled;
    private final List<Setting> settings = new ArrayList<>();

    public static final class Setting {
        public final String id, label;
        public final boolean booleanType;
        private final BooleanSupplier booleanGetter;
        private final Consumer<Boolean> booleanSetter;
        private final DoubleSupplier numberGetter;
        private final Consumer<Double> numberSetter;
        public final double min, max, step;
        private Setting(String id,String label,BooleanSupplier get,Consumer<Boolean> set){this.id=id;this.label=label;booleanType=true;booleanGetter=get;booleanSetter=set;numberGetter=null;numberSetter=null;min=0;max=1;step=1;}
        private Setting(String id,String label,DoubleSupplier get,Consumer<Double> set,double min,double max,double step){this.id=id;this.label=label;booleanType=false;booleanGetter=null;booleanSetter=null;numberGetter=get;numberSetter=set;this.min=min;this.max=max;this.step=step;}
        public boolean bool(){return booleanGetter.getAsBoolean();}
        public double number(){return numberGetter.getAsDouble();}
        public void toggle(){if(booleanType)booleanSetter.accept(!bool());}
        public void adjust(double direction){if(!booleanType)numberSetter.accept(Math.max(min,Math.min(max,number()+step*direction)));}
        public void load(String value){try{if(booleanType)booleanSetter.accept(Boolean.parseBoolean(value));else{double v=Double.parseDouble(value);if(Double.isFinite(v))numberSetter.accept(Math.max(min,Math.min(max,v)));}}catch(RuntimeException ignored){}}
        public String value(){return booleanType?Boolean.toString(bool()):Double.toString(number());}
    }

    protected Module(String name, Category category, boolean enabled) {
        this.name = name;
        this.category = category;
        this.enabled = enabled;
    }

    public final void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) onEnable(); else onDisable();
    }

    public final void toggle() { setEnabled(!enabled); }
    protected final void booleanSetting(String id,String label,BooleanSupplier get,Consumer<Boolean> set){settings.add(new Setting(id,label,get,set));}
    protected final void numberSetting(String id,String label,DoubleSupplier get,Consumer<Double> set,double min,double max,double step){settings.add(new Setting(id,label,get,set,min,max,step));}
    public final List<Setting> settings(){return Collections.unmodifiableList(settings);}
    protected void onEnable() {}
    protected void onDisable() {}
    public void onTick() {}
    public void onRender() {}

    public final String categoryName() {
        return category.name().toLowerCase(Locale.ROOT);
    }
}
