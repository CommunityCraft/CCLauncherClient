/*     */ package fr.xemnias.cclauncherclient;
/*     */ 
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Color;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.Frame;
/*     */ import java.awt.event.WindowAdapter;
/*     */ import java.awt.event.WindowEvent;
/*     */ import java.io.IOException;
/*     */ import java.net.URLEncoder;
/*     */ import javax.imageio.ImageIO;
/*     */ 
/*     */ public class LauncherFrame extends Frame
/*     */ {
/*     */   public static final int VERSION = 12;
/*     */   private static final long serialVersionUID = 1L;
/*     */   private Launcher launcher;
/*     */   private LoginForm loginForm;
/*     */ 
/*     */   public LauncherFrame()
/*     */   {
/*  24 */     super("CommunityCraft");
/*     */ 
/*  26 */     setBackground(Color.BLACK);
/*  27 */     this.loginForm = new LoginForm(this);
/*  28 */     setLayout(new BorderLayout());
/*  29 */     add(this.loginForm, "Center");
/*     */ 
/*  31 */     this.loginForm.setPreferredSize(new Dimension(854, 480));
/*  32 */     pack();
/*  33 */     setLocationRelativeTo(null);
/*     */     try
/*     */     {
/*  36 */       setIconImage(ImageIO.read(LauncherFrame.class.getResource("favicon.png")));
/*     */     } catch (IOException e1) {
/*  38 */       e1.printStackTrace();
/*     */     }
/*     */ 
/*  41 */     addWindowListener(new WindowAdapter() {
/*     */       public void windowClosing(WindowEvent arg0) {
/*  43 */         new Thread() {
/*     */           public void run() {
/*     */             try {
/*  46 */               Thread.sleep(30000L);
/*     */             } catch (InterruptedException e) {
/*  48 */               e.printStackTrace();
/*     */             }
/*  50 */             System.out.println("FORCING EXIT!");
/*  51 */             System.exit(0);
/*     */           }
/*     */         }
/*  54 */         .start();
/*  55 */         if (LauncherFrame.this.launcher != null) {
/*  56 */           LauncherFrame.this.launcher.stop();
/*  57 */           LauncherFrame.this.launcher.destroy();
/*     */         }
/*  59 */         System.exit(0);
/*     */       } } );
/*     */   }
/*     */ 
/*     */   public void playCached(String userName) {
/*     */     try {
/*  65 */       if ((userName == null) || (userName.length() <= 0)) {
/*  66 */         userName = "Minefieldien";
/*     */       }
/*  68 */       this.launcher = new Launcher();
/*  69 */       this.launcher.customParameters.put("userName", userName);
/*  70 */       this.launcher.init();
/*  71 */       removeAll();
/*  72 */       add(this.launcher, "Center");
/*  73 */       validate();
/*  74 */       this.launcher.start();
/*  75 */       this.loginForm = null;
/*  76 */       setTitle("CommunityCraft");
/*     */     } catch (Exception e) {
/*  78 */       e.printStackTrace();
/*  79 */       showError(e.toString());
/*     */     }
/*     */   }
/*     */ 
/*     */   public void login(String userName, String password) {
/*     */     try {
/*  85 */       String parameters = "user=" + URLEncoder.encode(userName, "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8") + "&version=" + 12;
/*  86 */       String result = Util.excutePost("https://login.minecraft.net/", parameters);
/*     */ 
/*  88 */       if (result == null) {
/*  89 */         showError("Can't connect to minecraft.net");
/*  90 */         this.loginForm.setNoNetwork();
/*  91 */         return;
/*     */       }
/*  93 */       if (!result.contains(":")) {
/*  94 */         if (result.trim().equals("Bad login")) {
/*  95 */           showError("Login failed");
/*  96 */         } else if (result.trim().equals("Old version")) {
/*  97 */           this.loginForm.setOutdated();
/*  98 */           showError("Outdated launcher");
/*     */         } else {
/* 100 */           showError(result);
/*     */         }
/* 102 */         this.loginForm.setNoNetwork();
/* 103 */         return;
/*     */       }
/* 105 */       String[] values = result.split(":");
/*     */ 
/* 107 */       System.out.println("Username is '" + values[2] + "'");
/*     */ 
/* 109 */       this.launcher = new Launcher();
/* 110 */       this.launcher.customParameters.put("userName", values[2].trim());
/* 111 */       this.launcher.customParameters.put("latestVersion", values[0].trim());
/* 112 */       this.launcher.customParameters.put("downloadTicket", values[1].trim());
/* 113 */       this.launcher.customParameters.put("sessionId", values[3].trim());
/* 114 */       this.launcher.init();
/*     */ 
/* 116 */       removeAll();
/* 117 */       add(this.launcher, "Center");
/* 118 */       validate();
/* 119 */       this.launcher.start();
/* 120 */       this.loginForm.loginOk();
/* 121 */       this.loginForm = null;
/* 122 */       setTitle("CommunityCraft");
/*     */     } catch (Exception e) {
/* 124 */       e.printStackTrace();
/* 125 */       showError(e.toString());
/* 126 */       this.loginForm.setNoNetwork();
/*     */     }
/*     */   }


public void startgame(String userName) {
    String result = getFakeResult(userName);
    String[] values = result.split(":");
    this.launcher = new Launcher();

    this.launcher.customParameters.put("userName", values[2].trim());
    this.launcher.customParameters.put("sessionId", values[3].trim());
    this.launcher.init();
    removeAll();
    add(this.launcher, "Center");
    validate();
    this.launcher.start();
    this.loginForm.loginOk();
    this.loginForm = null;
    setTitle("CommunityCraft");
  }

public String getFakeResult(String userName) {
    String userpass = "12345";
    System.out.println("Set sessionID to Password ( Pass 12345 normally ) : " + userpass);
    return Util.getFakeLatestVersion() + ":35b9fd01865fda9d70b157e244cf801c:" + userName + ":" + userpass + ":";
  }


/*     */ 
/*     */   private void showError(String error) {
/* 131 */     removeAll();
/* 132 */     add(this.loginForm);
/* 133 */     this.loginForm.setError(error);
/* 134 */     validate();
/*     */   }
/*     */ 
/*     */   public boolean canPlayOffline(String userName) {
/* 138 */     Launcher launcher = new Launcher();
/* 139 */     launcher.init(userName, null, null, null);
/* 140 */     return launcher.canPlayOffline();
/*     */   }
/*     */ 
/*     */   public static void main(String[] args) {
/* 144 */     LauncherFrame launcherFrame = new LauncherFrame();
/* 145 */     launcherFrame.setVisible(true);
/*     */   }
/*     */ }

/* Location:           C:\Users\Xemnias\Downloads\Minefield.jar
 * Qualified Name:     net.minecraft.LauncherFrame
 * JD-Core Version:    0.6.0
 */