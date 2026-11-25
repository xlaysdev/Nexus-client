package com.xlays.nexus;

import com.xlays.nexus.modules.Module;
import com.xlays.nexus.modules.movement.MovementSettings;
import com.xlays.nexus.modules.render.VisualSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** C00lgui-inspired responsive module browser for NEXUS. */
public final class ClickGuiScreen extends Screen {
    private static final int W=660,H=390,HEADER=38,FOOTER=22;
    private static final int BG=0xF00D1017,PANEL=0xFF141923,CARD=0xFF191F2B,HOVER=0xFF252E3D,LINE=0xFF2A3444;
    private static final int TEXT=0xFFF1F5FC,MUTED=0xFF8995A8,GREEN=0xFF55D6A0,ACCENT=0xFF9585FF,RED=0xFFFF7185;
    private static final Module.Category[] CATEGORIES={Module.Category.COMBAT,Module.Category.MOVEMENT,Module.Category.PLAYER,Module.Category.RENDER,Module.Category.HUD,Module.Category.MISC};
    private static final int[] CATEGORY_COLORS={0xFFFF7185,0xFF69A9FF,0xFFFFC86B,0xFFB58CFF,0xFF55D6A0,0xFF65C9D8};
    private float scale=1,ox,oy;
    private Module.Category category=Module.Category.COMBAT;
    private String selected=null,search="";
    private boolean searchFocused=false;
    private int scroll=0;

    public ClickGuiScreen(){super(Component.literal("NEXUS V1"));}
    public ClickGuiScreen(String ignored){this();}

    @Override protected void init(){
        super.init();
        scale=Math.max(.1F,Math.min(1F,Math.min((width-16F)/W,(height-16F)/H)));
        ox=(width-W*scale)/2; oy=(height-H*scale)/2;
    }

    private List<Module> visibleModules(){
        List<Module> out=new ArrayList<>();
        for(Module m:NexusClient.MODULES.inCategory(category))
            if(m.name.toLowerCase().contains(search.toLowerCase())) out.add(m);
        return out;
    }

    @Override public void render(GuiGraphics g,int mx,int my,float partialTick){
        int localX=(int)((mx-ox)/scale),localY=(int)((my-oy)/scale);
        g.fill(0,0,width,height,0xB9000000);
        g.pose().pushMatrix();g.pose().translate(ox,oy);g.pose().scale(scale,scale);
        g.fill(0,0,W,H,BG);border(g,0,0,W,H,LINE);
        g.fill(0,0,W,HEADER,0xFF171D29);g.fill(0,HEADER-2,W,HEADER,ACCENT);
        g.fill(14,10,32,28,ACCENT);g.drawString(font,"N",20,15,0xFFFFFFFF,false);
        g.drawString(font,"NEXUS",40,10,TEXT,false);g.drawString(font,"CLIENT  /  MODULES",104,12,MUTED,false);
        g.drawString(font,"NEOFORGE 1.21.11",W-128,13,MUTED,false);
        int close=W-28;g.fill(close,9,close+17,27,hit(localX,localY,close,9,17,18)?0xFF633743:0xFF332630);
        g.drawString(font,"X",close+6,14,RED,false);

        g.fill(0,HEADER,118,H-FOOTER,PANEL);g.drawString(font,"CATEGORIES",14,52,MUTED,false);
        for(int i=0;i<CATEGORIES.length;i++){
            Module.Category c=CATEGORIES[i];int y=66+i*37;boolean active=category==c;
            g.fill(9,y,109,y+30,active?0xFF252E40:hit(localX,localY,9,y,100,30)?HOVER:PANEL);
            if(active)g.fill(9,y,12,y+30,CATEGORY_COLORS[i]);
            String initial=c.name().substring(0,1);
            g.drawString(font,initial,19,y+9,CATEGORY_COLORS[i],false);
            String label=pretty(c.name());g.drawString(font,label,34,y+9,active?TEXT:MUTED,false);
            String count=""+NexusClient.MODULES.inCategory(c).size();
            g.drawString(font,count,99-font.width(count),y+9,MUTED,false);
        }
        g.drawString(font,"STATUS",14,H-67,MUTED,false);g.fill(10,H-54,108,H-27,0xFF1C2930);
        g.fill(17,H-44,22,H-39,GREEN);g.drawString(font,"READY",29,H-45,GREEN,false);

        int x0=130;g.drawString(font,pretty(category.name()).toUpperCase()+" MODULES",x0,51,TEXT,false);
        g.drawString(font,"Tap a card to toggle  ·  gear to inspect",x0,64,MUTED,false);
        g.fill(x0,82,x0+246,105,0xFF171E29);border(g,x0,82,246,23,LINE);
        g.drawString(font,search.isEmpty()?"Search modules...":search,x0+8,90,searchFocused?TEXT:MUTED,false);

        List<Module> modules=visibleModules();
        int rowHeight=44;
        int maxScroll=Math.max(0,modules.size()*rowHeight-214);scroll=Math.max(0,Math.min(scroll,maxScroll));
        g.enableScissor((int)(ox+x0*scale),(int)(oy+112*scale),(int)(ox+376*scale),(int)(oy+(H-FOOTER-5)*scale));
        for(int i=0;i<modules.size();i++){
            Module m=modules.get(i);int x=x0,y=116+i*rowHeight-scroll;
            if(y<-rowHeight||y>H-FOOTER-4)continue;
            boolean on=m.enabled;
            boolean hovered=hit(localX,localY,x,y,246,38);
            g.fill(x,y,x+246,y+38,on?0xFF1D302D:hovered?HOVER:CARD);
            border(g,x,y,246,38,on?0xFF376D5C:LINE);
            g.fill(x+8,y+7,x+10,y+31,on?GREEN:0xFF485366);
            g.drawString(font,m.name,x+17,y+7,on?TEXT:MUTED,false);
            g.drawString(font,on?"ENABLED":"DISABLED",x+17,y+21,on?GREEN:MUTED,false);
            g.fill(x+185,y+8,x+220,y+29,on?0xFF245541:0xFF303846);
            g.drawString(font,on?"ON":"OFF",x+194,y+14,on?GREEN:MUTED,false);
            g.fill(x+225,y+8,x+238,y+29,selected!=null&&selected.equals(m.name)?0xFF403C67:0xFF252D3B);
            g.drawString(font,"*",x+229,y+14,ACCENT,false);
        }
        g.disableScissor();
        if(modules.isEmpty())g.drawString(font,"No modules in this category.",x0+8,124,MUTED,false);

        int px=390,py=51,pw=256,ph=H-HEADER-FOOTER-13;
        g.fill(px,py,px+pw,py+ph,PANEL);border(g,px,py,pw,ph,LINE);
        g.fill(px,py,px+pw,py+31,0xFF1B2230);g.drawString(font,"MODULE SETTINGS",px+11,62,MUTED,false);
        Module chosen=selected==null?null:NexusClient.MODULES.get(selected).orElse(null);
        if(chosen==null||chosen.category!=category){
            g.drawString(font,"Choose a module",px+13,108,TEXT,false);
            g.drawString(font,"Tap a module's gear icon to",px+13,127,MUTED,false);
            g.drawString(font,"inspect its available controls.",px+13,140,MUTED,false);
        }else{
            boolean on=chosen.enabled;
            g.drawString(font,chosen.name,px+12,94,TEXT,false);
            g.fill(px+pw-62,py+37,px+pw-12,py+56,on?0xFF214B3D:0xFF303846);
            g.drawString(font,on?"ON":"OFF",px+pw-47,py+44,on?GREEN:MUTED,false);
            g.drawString(font,"CATEGORY",px+12,125,MUTED,false);
            g.drawString(font,pretty(chosen.category.name()),px+12,141,TEXT,false);
            g.drawString(font,"STATE",px+12,169,MUTED,false);
            g.drawString(font,on?"Enabled":"Disabled",px+12,185,on?GREEN:MUTED,false);
            g.fill(px+12,204,px+pw-12,205,LINE);
            if("Speed".equals(chosen.name)){
                g.drawString(font,"SPEED MULTIPLIER",px+12,218,TEXT,false);
                settingButtons(g,px+pw-72,212,String.format("%.2fx",MovementSettings.speedMultiplier));
                g.drawString(font,"MODE",px+12,246,TEXT,false);
                settingButtons(g,px+pw-72,240,MovementSettings.speedMode);
            }else if("Fly".equals(chosen.name)){
                g.drawString(font,"VERTICAL SPEED",px+12,218,TEXT,false);
                settingButtons(g,px+pw-72,212,String.format("%.2f",MovementSettings.verticalSpeed));
            }else if("Velocity".equals(chosen.name)){
                g.drawString(font,"KNOCKBACK SCALE",px+12,218,TEXT,false);
                settingButtons(g,px+pw-72,212,String.format("%.1fx",MovementSettings.velocityScale));
            }else if("ESP".equals(chosen.name)||"BlockESP".equals(chosen.name)){
                g.drawString(font,"BOX STYLE",px+12,218,TEXT,false);
                settingButtons(g,px+pw-72,212,VisualSettings.esp3d?"3D":"2D");
            }else{
                g.drawString(font,"No extra settings exposed",px+12,224,MUTED,false);
                g.drawString(font,"for this module yet.",px+12,240,MUTED,false);
            }
            g.drawString(font,"Changes apply immediately.",px+12,H-FOOTER-40,MUTED,false);
        }
        g.fill(118,H-FOOTER,W,H,0xFF171D29);
        g.drawString(font,"RIGHT SHIFT  /  ESC  ·  CLOSE",132,H-14,MUTED,false);
        g.drawString(font,NexusClient.MODULES.all().stream().filter(m->m.enabled).count()+" ACTIVE",W-86,H-14,GREEN,false);
        g.pose().popMatrix();super.render(g,mx,my,partialTick);
    }

    private void settingButtons(GuiGraphics g,int x,int y,String value){
        g.fill(x,y,x+18,y+19,0xFF303846);g.fill(x+54,y,x+72,y+19,0xFF303846);
        g.drawString(font,"-",x+7,y+6,TEXT,false);g.drawString(font,"+",x+61,y+6,TEXT,false);
        g.drawString(font,value,x+25-font.width(value)/2,y+6,ACCENT,false);
    }

    @Override public boolean mouseClicked(MouseButtonEvent e,boolean dbl){
        int x=(int)((e.x()-ox)/scale),y=(int)((e.y()-oy)/scale);
        if(e.button()!=0)return true;
        if(hit(x,y,W-28,9,17,18)){closeGui();return true;}
        for(int i=0;i<CATEGORIES.length;i++)if(hit(x,y,9,66+i*37,100,30)){
            category=CATEGORIES[i];selected=null;search="";searchFocused=false;scroll=0;return true;
        }
        if(hit(x,y,130,82,246,23)){searchFocused=true;return true;}
        List<Module> modules=visibleModules();
        for(int i=0;i<modules.size();i++){
            Module m=modules.get(i);int bx=130,by=116+i*44-scroll;
            if(hit(x,y,bx+225,by+8,13,21)){selected=m.name;return true;}
            if(hit(x,y,bx,by,246,38)){
                m.toggle();NexusClient.saveConfig();selected=m.name;
                return true;
            }
        }
        if(selected!=null){
            int px=390,yy=212,bx=px+256-72;
            if("Speed".equals(selected)){
                if(hit(x,y,bx,yy,18,19)){MovementSettings.speedMultiplier=Math.max(1F,MovementSettings.speedMultiplier-.1F);NexusClient.saveConfig();return true;}
                if(hit(x,y,bx+54,yy,18,19)){MovementSettings.speedMultiplier=Math.min(3F,MovementSettings.speedMultiplier+.1F);NexusClient.saveConfig();return true;}
                if(hit(x,y,px+12,240,232,19)){MovementSettings.speedMode=switch(MovementSettings.speedMode){case "Normal"->"Bhop";case "Bhop"->"Timer";default->"Normal";};NexusClient.saveConfig();return true;}
            }else if("Fly".equals(selected)){
                if(hit(x,y,bx,yy,18,19)){MovementSettings.verticalSpeed=Math.max(.1F,MovementSettings.verticalSpeed-.05F);NexusClient.saveConfig();return true;}
                if(hit(x,y,bx+54,yy,18,19)){MovementSettings.verticalSpeed=Math.min(1.5F,MovementSettings.verticalSpeed+.05F);NexusClient.saveConfig();return true;}
            }else if("Velocity".equals(selected)){
                if(hit(x,y,bx,yy,18,19)){MovementSettings.velocityScale=Math.max(0F,MovementSettings.velocityScale-.1F);NexusClient.saveConfig();return true;}
                if(hit(x,y,bx+54,yy,18,19)){MovementSettings.velocityScale=Math.min(1F,MovementSettings.velocityScale+.1F);NexusClient.saveConfig();return true;}
            }else if("ESP".equals(selected)||"BlockESP".equals(selected)){
                if(hit(x,y,bx,yy,72,19)){VisualSettings.esp3d=!VisualSettings.esp3d;NexusClient.saveConfig();return true;}
            }
        }
        return true;
    }

    @Override public boolean mouseScrolled(double mx,double my,double sx,double sy){
        int x=(int)((mx-ox)/scale),y=(int)((my-oy)/scale);
        if(x>=130&&x<376&&y>=112&&y<H-FOOTER){int max=Math.max(0,visibleModules().size()*44-214);scroll=Math.max(0,Math.min(max,scroll-(int)(sy*44)));return true;}
        return super.mouseScrolled(mx,my,sx,sy);
    }

    @Override public boolean keyPressed(KeyEvent e){
        int k=e.key();
        if(k==GLFW.GLFW_KEY_ESCAPE||k==GLFW.GLFW_KEY_RIGHT_SHIFT){if(searchFocused){searchFocused=false;return true;}closeGui();return true;}
        if(searchFocused){
            if(k==GLFW.GLFW_KEY_BACKSPACE){if(!search.isEmpty())search=search.substring(0,search.length()-1);scroll=0;return true;}
            if(k>=GLFW.GLFW_KEY_A&&k<=GLFW.GLFW_KEY_Z){search+=(char)('a'+k-GLFW.GLFW_KEY_A);scroll=0;return true;}
            if(k>=GLFW.GLFW_KEY_0&&k<=GLFW.GLFW_KEY_9){search+=(char)('0'+k-GLFW.GLFW_KEY_0);scroll=0;return true;}
            if(k==GLFW.GLFW_KEY_SPACE){search+=" ";return true;}
            return true;
        }
        return super.keyPressed(e);
    }

    private void closeGui(){NexusClient.saveConfig();Minecraft.getInstance().setScreen(null);}
    @Override public boolean isPauseScreen(){return false;}
    private static String pretty(String s){String lower=s.toLowerCase();return Character.toUpperCase(lower.charAt(0))+lower.substring(1);}
    private static boolean hit(int a,int b,int x,int y,int w,int h){return a>=x&&a<x+w&&b>=y&&b<y+h;}
    private static void border(GuiGraphics g,int x,int y,int w,int h,int c){g.fill(x,y,x+w,y+1,c);g.fill(x,y+h-1,x+w,y+h,c);g.fill(x,y,x+1,y+h,c);g.fill(x+w-1,y,x+w,y+h,c);}
}