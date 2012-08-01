/*     */ package fr.xemnias.cclauncherclient;
/*     */ 
/*     */ import java.applet.Applet;
/*     */ import java.applet.AppletStub;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Color;
/*     */ import java.awt.Font;
/*     */ import java.awt.FontMetrics;
/*     */ import java.awt.Graphics;
/*     */ import java.awt.Image;
/*     */ import java.awt.image.VolatileImage;
/*     */ import java.io.IOException;
/*     */ import java.net.MalformedURLException;
/*     */ import java.net.URL;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
import javax.imageio.ImageIO;
/*     */ 
/*     */ public class Launcher extends Applet
/*     */   implements Runnable, AppletStub
/*     */ {
/*     */   private static final long serialVersionUID = 1L;
/*  24 */   @SuppressWarnings({ "unchecked", "rawtypes" })
public Map<String, String> customParameters = new HashMap();
/*     */   private GameUpdater gameUpdater;
/*  26 */   private boolean gameUpdaterStarted = false;
/*     */   private Applet applet;
/*     */   private Image bgImage;
/*  29 */   private boolean active = false;
/*  30 */   private int context = 0;
/*     */   private VolatileImage img;
/*     */ 
/*     */   public boolean isActive()
/*     */   {
/*  35 */     if (this.context == 0) {
/*  36 */       this.context = -1;
/*     */       try {
/*  38 */         if (getAppletContext() != null) this.context = 1; 
/*     */       }
/*     */       catch (Exception localException)
/*     */       {
/*     */       }
/*     */     }
/*  43 */     if (this.context == -1) return this.active;
/*  44 */     return super.isActive();
/*     */   }
/*     */ 
/*     */   public void init(String userName, String latestVersion, String downloadTicket, String sessionId)
/*     */   {
/*     */     try {
/*  50 */       this.bgImage = ImageIO.read(LoginForm.class.getResource("dirt.png"));
/*     */     } catch (IOException e) {
/*  52 */       e.printStackTrace();
/*     */     }
/*     */ 
/*  55 */     this.customParameters.put("username", userName);
/*  56 */     this.customParameters.put("sessionid", sessionId);
/*     */ 
/*  58 */     this.gameUpdater = new GameUpdater(latestVersion, "minecraft.jar?user=" + userName + "&ticket=" + downloadTicket);
/*     */   }
/*     */ 
/*     */   public boolean canPlayOffline() {
/*  62 */     return this.gameUpdater.canPlayOffline();
/*     */   }
/*     */ 
/*     */   public void init() {
/*  66 */     if (this.applet != null) {
/*  67 */       this.applet.init();
/*  68 */       return;
/*     */     }
/*  70 */     init(getParameter("userName"), getParameter("latestVersion"), getParameter("downloadTicket"), getParameter("sessionId"));
/*     */   }
/*     */ 
/*     */   public void start() {
/*  74 */     if (this.applet != null) {
/*  75 */       this.applet.start();
/*  76 */       return;
/*     */     }
/*  78 */     if (this.gameUpdaterStarted) return;
/*     */ 
/*  80 */     Thread t = new Thread() {
/*     */       public void run() {
/*  82 */         Launcher.this.gameUpdater.run();
/*     */         try {
/*  84 */           if (!Launcher.this.gameUpdater.fatalError)
/*  85 */             Launcher.this.replace(Launcher.this.gameUpdater.createApplet());
/*     */         }
/*     */         catch (ClassNotFoundException e)
/*     */         {
/*  89 */           e.printStackTrace();
/*     */         } catch (InstantiationException e) {
/*  91 */           e.printStackTrace();
/*     */         } catch (IllegalAccessException e) {
/*  93 */           e.printStackTrace();
/*     */         }
/*     */       }
/*     */     };
/*  97 */     t.setDaemon(true);
/*  98 */     t.start();
/*     */ 
/* 100 */     t = new Thread() {
/*     */       public void run() {
/* 102 */         while (Launcher.this.applet == null) {
/* 103 */           Launcher.this.repaint();
/*     */           try {
/* 105 */             Thread.sleep(10L);
/*     */           } catch (InterruptedException e) {
/* 107 */             e.printStackTrace();
/*     */           }
/*     */         }
/*     */       }
/*     */     };
/* 112 */     t.setDaemon(true);
/* 113 */     t.start();
/*     */ 
/* 115 */     this.gameUpdaterStarted = true;
/*     */   }
/*     */ 
/*     */   public void stop() {
/* 119 */     if (this.applet != null) {
/* 120 */       this.active = false;
/* 121 */       this.applet.stop();
/* 122 */       return;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void destroy() {
/* 127 */     if (this.applet != null) {
/* 128 */       this.applet.destroy();
/* 129 */       return;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void replace(Applet applet) {
/* 134 */     this.applet = applet;
/* 135 */     applet.setStub(this);
/* 136 */     applet.setSize(getWidth(), getHeight());
/*     */ 
/* 138 */     setLayout(new BorderLayout());
/* 139 */     add(applet, "Center");
/*     */ 
/* 141 */     applet.init();
/* 142 */     this.active = true;
/* 143 */     applet.start();
/* 144 */     validate();
/*     */   }
/*     */ 
/*     */   public void update(Graphics g)
/*     */   {
/* 149 */     paint(g);
/*     */   }
/*     */ 
/*     */   public void paint(Graphics g2) {
/* 153 */     if (this.applet != null) return;
/*     */ 
/* 155 */     int w = getWidth() / 2;
/* 156 */     int h = getHeight() / 2;
/* 157 */     if ((this.img == null) || (this.img.getWidth() != w) || (this.img.getHeight() != h)) {
/* 158 */       this.img = createVolatileImage(w, h);
/*     */     }
/*     */ 
/* 161 */     Graphics g = this.img.getGraphics();
/* 162 */     for (int x = 0; x <= w / 16; x++) {
/* 163 */       for (int y = 0; y <= h / 16; y++)
/* 164 */         g.drawImage(this.bgImage, x * 16, y * 16, null);
/*     */     }
/* 166 */     g.setColor(Color.LIGHT_GRAY);
/*     */ 
/* 168 */     String msg = "Updating Minecraft";
/* 169 */     if (this.gameUpdater.fatalError) {
/* 170 */       msg = "Failed to launch";
/*     */     }
/*     */ 
/* 173 */     g.setFont(new Font(null, 1, 20));
/* 174 */     FontMetrics fm = g.getFontMetrics();
/* 175 */     g.drawString(msg, w / 2 - fm.stringWidth(msg) / 2, h / 2 - fm.getHeight() * 2);
/*     */ 
/* 177 */     g.setFont(new Font(null, 0, 12));
/* 178 */     fm = g.getFontMetrics();
/* 179 */     msg = this.gameUpdater.getDescriptionForState();
/* 180 */     if (this.gameUpdater.fatalError) {
/* 181 */       msg = this.gameUpdater.fatalErrorDescription;
/*     */     }
/*     */ 
/* 184 */     g.drawString(msg, w / 2 - fm.stringWidth(msg) / 2, h / 2 + fm.getHeight() * 1);
/* 185 */     msg = this.gameUpdater.subtaskMessage;
/* 186 */     g.drawString(msg, w / 2 - fm.stringWidth(msg) / 2, h / 2 + fm.getHeight() * 2);
/*     */ 
/* 188 */     if (!this.gameUpdater.fatalError) {
/* 189 */       g.setColor(Color.black);
/* 190 */       g.fillRect(64, h - 64, w - 128 + 1, 5);
/* 191 */       g.setColor(new Color(32768));
/* 192 */       g.fillRect(64, h - 64, this.gameUpdater.percentage * (w - 128) / 100, 4);
/* 193 */       g.setColor(new Color(2138144));
/* 194 */       g.fillRect(65, h - 64 + 1, this.gameUpdater.percentage * (w - 128) / 100 - 2, 1);
/*     */     }
/*     */ 
/* 197 */     g.dispose();
/*     */ 
/* 199 */     g2.drawImage(this.img, 0, 0, w * 2, h * 2, null);
/*     */   }
/*     */ 
/*     */   public void run() {
/*     */   }
/*     */ 
/*     */   public String getParameter(String name) {
/* 206 */     String custom = (String)this.customParameters.get(name);
/* 207 */     if (custom != null) return custom; try
/*     */     {
/* 209 */       return super.getParameter(name);
/*     */     } catch (Exception e) {
/* 211 */       this.customParameters.put(name, null);
/* 212 */     }return null;
/*     */   }
/*     */ 
/*     */   public void appletResize(int width, int height)
/*     */   {
/*     */   }
/*     */ 
/*     */   public URL getDocumentBase() {
/*     */     try {
/* 221 */       return new URL("http://www.minecraft.net/game/");
/*     */     } catch (MalformedURLException e) {
/* 223 */       e.printStackTrace();
/*     */     }
/* 225 */     return null;
/*     */   }
/*     */ }

/* Location:           C:\Users\Xemnias\Downloads\Minefield.jar
 * Qualified Name:     net.minecraft.Launcher
 * JD-Core Version:    0.6.0
 */