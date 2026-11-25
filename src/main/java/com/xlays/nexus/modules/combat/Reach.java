package com.xlays.nexus.modules.combat;
import com.xlays.nexus.modules.Module;
public final class Reach extends Module {
 public static float distance=3.5F;
 public Reach(){super("Reach",Category.COMBAT,false);numberSetting("distance","Reach distance",()->distance,v->distance=v.floatValue(),3,6,.1);}
}