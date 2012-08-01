/*     */ package fr.xemnias.cclauncherclient;
/*     */ 
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Button;
/*     */ import java.awt.Checkbox;
/*     */ import java.awt.Color;
/*     */ import java.awt.Cursor;
/*     */ import java.awt.Desktop;
/*     */ import java.awt.Font;
/*     */ import java.awt.FontMetrics;
/*     */ import java.awt.Graphics;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.GridLayout;
/*     */ import java.awt.Image;
/*     */ import java.awt.Insets;
/*     */ import java.awt.Label;
/*     */ import java.awt.Panel;
/*     */ import java.awt.TextField;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.MouseAdapter;
/*     */ import java.awt.event.MouseEvent;
/*     */ import java.awt.image.VolatileImage;
/*     */ import java.io.DataInputStream;
/*     */ import java.io.DataOutputStream;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.net.URL;
/*     */ import java.util.Random;
/*     */ import javax.crypto.Cipher;
/*     */ import javax.crypto.CipherInputStream;
/*     */ import javax.crypto.CipherOutputStream;
/*     */ import javax.crypto.SecretKey;
/*     */ import javax.crypto.SecretKeyFactory;
/*     */ import javax.crypto.spec.PBEKeySpec;
/*     */ import javax.crypto.spec.PBEParameterSpec;
/*     */ import javax.imageio.ImageIO;
/*     */ 
/*     */ public class LoginForm extends Panel
/*     */ {
/*     */   private static final long serialVersionUID = 1L;
/*     */   private Image bgImage;
/*  48 */   private TextField userName = new TextField(20);
/*  49 */   private TextField password = new TextField(20);
/*  50 */   private Checkbox rememberBox = new Checkbox("Se souvenir de moi");
/*  51 */   private Button launchButton = new Button("Connexion");
/*  52 */   private Button retryButton = new Button("Réessayer");
/*  53 */   private Button offlineButton = new Button("Jouer hors ligne");
/*  54 */   private Label errorLabel = new Label("", 1);
/*     */   private final LauncherFrame launcherFrame;
/*  56 */   private boolean outdated = false;
/*     */   private VolatileImage img;
/*     */ 
/*     */   public LoginForm(final LauncherFrame launcherFrame)
/*     */   {
/*  61 */     this.launcherFrame = launcherFrame;
/*     */ 
/*  63 */     GridBagLayout gbl = new GridBagLayout();
/*  64 */     setLayout(gbl);
/*     */ 
/*  66 */     add(buildLoginPanel());
/*     */     try
/*     */     {
/*  69 */       this.bgImage = ImageIO.read(LoginForm.class.getResource("dirt.png"));
/*     */     } catch (IOException e) {
/*  71 */       e.printStackTrace();
/*     */     }
/*     */ 
/*  74 */     readUsername();
/*     */ 
/*  76 */     this.retryButton.addActionListener(new ActionListener() {
/*     */       public void actionPerformed(ActionEvent ae) {
/*  78 */         LoginForm.this.errorLabel.setText("");
/*  79 */         LoginForm.this.removeAll();
/*  80 */         LoginForm.this.add(LoginForm.this.buildLoginPanel());
/*  81 */         LoginForm.this.validate();
/*     */       }
/*     */     });
/*  84 */     this.offlineButton.addActionListener(new ActionListener() {
/*     */       public void actionPerformed(ActionEvent ae) {
/*  86 */         launcherFrame.playCached(LoginForm.this.userName.getText());
/*     */       }
/*     */     });
/*  89 */     launchButton.addActionListener(new ActionListener() {
/*     */       public void actionPerformed(ActionEvent ae) {
				if(!LoginForm.this.password.getText().isEmpty())
/*  91 */         launcherFrame.login(LoginForm.this.userName.getText(), LoginForm.this.password.getText());
				else
					launcherFrame.startgame(LoginForm.this.userName.getText());
/*     */       } } );
/*     */   }
/*     */ 
/*     */   private void readUsername() {
/*     */     try {
/*  97 */       File lastLogin = new File(Util.getWorkingDirectory(), "lastlogin");
/*     */ 
/*  99 */       Cipher cipher = getCipher(2, "passwordfile");
/*     */       DataInputStream dis;
/* 101 */       if (cipher != null)
/* 102 */         dis = new DataInputStream(new CipherInputStream(new FileInputStream(lastLogin), cipher));
/*     */       else {
/* 104 */         dis = new DataInputStream(new FileInputStream(lastLogin));
/*     */       }
/* 106 */       this.userName.setText(dis.readUTF());
/* 107 */       this.password.setText(dis.readUTF());
/* 108 */       this.rememberBox.setState(this.password.getText().length() > 0);
/* 109 */       dis.close();
/*     */     } catch (Exception e) {
/* 111 */       e.printStackTrace();
/*     */     }
/*     */   }
/*     */ 
/*     */   private void writeUsername() {
/*     */     try {
/* 117 */       File lastLogin = new File(Util.getWorkingDirectory(), "lastlogin");
/*     */ 
/* 119 */       Cipher cipher = getCipher(1, "passwordfile");
/*     */       DataOutputStream dos;
/* 121 */       if (cipher != null)
/* 122 */         dos = new DataOutputStream(new CipherOutputStream(new FileOutputStream(lastLogin), cipher));
/*     */       else {
/* 124 */         dos = new DataOutputStream(new FileOutputStream(lastLogin));
/*     */       }
/* 126 */       dos.writeUTF(this.userName.getText());
/* 127 */       dos.writeUTF(this.rememberBox.getState() ? this.password.getText() : "");
/* 128 */       dos.close();
/*     */     } catch (Exception e) {
/* 130 */       e.printStackTrace();
/*     */     }
/*     */   }
/*     */ 
/*     */   private Cipher getCipher(int mode, String password) throws Exception {
/* 135 */     Random random = new Random(43287234L);
/* 136 */     byte[] salt = new byte[8];
/* 137 */     random.nextBytes(salt);
/* 138 */     PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 5);
/*     */ 
/* 140 */     SecretKey pbeKey = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(new PBEKeySpec(password.toCharArray()));
/* 141 */     Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
/* 142 */     cipher.init(mode, pbeKey, pbeParamSpec);
/* 143 */     return cipher;
/*     */   }
/*     */ 
/*     */   public void update(Graphics g) {
/* 147 */     paint(g);
/*     */   }
/*     */ 
/*     */   public void paint(Graphics g2)
/*     */   {
/* 152 */     int w = getWidth() / 2;
/* 153 */     int h = getHeight() / 2;
/* 154 */     if ((this.img == null) || (this.img.getWidth() != w) || (this.img.getHeight() != h)) {
/* 155 */       this.img = createVolatileImage(w, h);
/*     */     }
/*     */ 
/* 158 */     Graphics g = this.img.getGraphics();
/* 159 */     for (int x = 0; x <= w / 16; x++) {
/* 160 */       for (int y = 0; y <= h / 16; y++)
/* 161 */         g.drawImage(this.bgImage, x * 16, y * 16, null);
/*     */     }
/* 163 */     g.setColor(Color.WHITE);
/*     */ 
/* 165 */     String msg = "Connexion";
/* 166 */     g.setFont(new Font("Arial", 1, 20));
/* 167 */     FontMetrics fm = g.getFontMetrics();
/* 168 */     g.drawString(msg, w / 2 - fm.stringWidth(msg) / 2, h / 2 - fm.getHeight() * 2);
/*     */ 
/* 170 */     g.dispose();
/* 171 */     g2.drawImage(this.img, 0, 0, w * 2, h * 2, null);
/*     */   }
/*     */ 
/*     */   private Panel buildLoginPanel() {
/* 175 */     Panel panel = new Panel()
/*     */     {
/*     */       private static final long serialVersionUID = 1L;
/* 178 */       private Insets insets = new Insets(12, 24, 16, 32);
/*     */ 
/*     */       public Insets getInsets() {
/* 181 */         return this.insets;
/*     */       }
/*     */ 
/*     */       public void update(Graphics g) {
/* 185 */         paint(g);
/*     */       }
/*     */ 
/*     */       public void paint(Graphics g) {
/* 189 */         super.paint(g);
/* 190 */         int hOffs = 0;
/*     */ 
/* 192 */         g.setColor(Color.BLACK);
/* 193 */         g.drawRect(0, 0 + hOffs, getWidth() - 1, getHeight() - 1 - hOffs);
/* 194 */         g.drawRect(1, 1 + hOffs, getWidth() - 3, getHeight() - 3 - hOffs);
/* 195 */         g.setColor(Color.WHITE);
/*     */ 
/* 197 */         g.drawRect(2, 2 + hOffs, getWidth() - 5, getHeight() - 5 - hOffs);
/*     */       }
/*     */     };
/* 200 */     panel.setBackground(Color.GRAY);
/* 201 */     BorderLayout layout = new BorderLayout();
/* 202 */     layout.setHgap(0);
/* 203 */     layout.setVgap(8);
/* 204 */     panel.setLayout(layout);
/*     */ 
/* 206 */     GridLayout gl1 = new GridLayout(0, 1);
/* 207 */     GridLayout gl2 = new GridLayout(0, 1);
/* 208 */     gl1.setVgap(2);
/* 209 */     gl2.setVgap(2);
/* 210 */     Panel titles = new Panel(gl1);
/* 211 */     Panel values = new Panel(gl2);
/*     */ 
/* 213 */     titles.add(new Label("Identifiant :", 2));
/* 214 */     titles.add(new Label("Mot de passe (option):", 2));
/* 215 */     titles.add(new Label("", 2));
/*     */ 
/* 217 */     this.password.setEchoChar('*');
/* 218 */     values.add(this.userName);
/* 219 */     values.add(this.password);
/* 220 */     values.add(this.rememberBox);
/*     */ 
/* 222 */     panel.add(titles, "West");
/* 223 */     panel.add(values, "Center");
/*     */ 
/* 225 */     Panel loginPanel = new Panel(new BorderLayout());
/*     */ 
/* 227 */     Panel registerPanel = new Panel(new BorderLayout());
/*     */     try {
/* 229 */       if (this.outdated) {
/* 230 */         Label accountLink = new Label("You need to update the launcher!") { private static final long serialVersionUID = 0L;
/*     */ 
/* 233 */           public void paint(Graphics g) { super.paint(g);
/*     */ 
/* 235 */             int x = 0;
/* 236 */             int y = 0;
/*     */ 
/* 238 */             FontMetrics fm = g.getFontMetrics();
/* 239 */             int width = fm.stringWidth(getText());
/* 240 */             int height = fm.getHeight();
/*     */ 
/* 242 */             if (getAlignment() == 0) x = 0;
/* 243 */             else if (getAlignment() == 1) x = getBounds().width / 2 - width / 2;
/* 244 */             else if (getAlignment() == 2) x = getBounds().width - width;
/* 245 */             y = getBounds().height / 2 + height / 2 - 1;
/*     */ 
/* 247 */             g.drawLine(x + 2, y, x + width - 2, y); }
/*     */ 
/*     */           public void update(Graphics g)
/*     */           {
/* 251 */             paint(g);
/*     */           }
/*     */         };
/* 254 */         accountLink.setCursor(Cursor.getPredefinedCursor(12));
/* 255 */         accountLink.addMouseListener(new MouseAdapter() {
/*     */           public void mousePressed(MouseEvent arg0) {
/*     */             try {
/* 258 */               Desktop.getDesktop().browse(new URL("http://www.minecraft.net/download.jsp").toURI());
/*     */             } catch (Exception e) {
/* 260 */               e.printStackTrace();
/*     */             }
/*     */           }
/*     */         });
/* 264 */         accountLink.setForeground(Color.BLACK);
/* 265 */         registerPanel.add(accountLink, "West");
/* 266 */         registerPanel.add(new Panel(), "Center");
/*     */       } else {
/* 268 */         Label accountLink = new Label("Besoin d'un compte ?") { private static final long serialVersionUID = 0L;
/*     */ 
/* 271 */           public void paint(Graphics g) { super.paint(g);
/*     */ 
/* 273 */             int x = 0;
/* 274 */             int y = 0;
/*     */ 
/* 276 */             FontMetrics fm = g.getFontMetrics();
/* 277 */             int width = fm.stringWidth(getText());
/* 278 */             int height = fm.getHeight();
/*     */ 
/* 280 */             if (getAlignment() == 0) x = 0;
/* 281 */             else if (getAlignment() == 1) x = getBounds().width / 2 - width / 2;
/* 282 */             else if (getAlignment() == 2) x = getBounds().width - width;
/* 283 */             y = getBounds().height / 2 + height / 2 - 1;
/*     */ 
/* 285 */             g.drawLine(x + 2, y, x + width - 2, y); }
/*     */ 
/*     */           public void update(Graphics g)
/*     */           {
/* 289 */             paint(g);
/*     */           }
/*     */         };
/* 292 */         accountLink.setCursor(Cursor.getPredefinedCursor(12));
/* 293 */         accountLink.addMouseListener(new MouseAdapter() {
/*     */           public void mousePressed(MouseEvent arg0) {
/*     */             try {
/* 296 */               Desktop.getDesktop().browse(new URL("http://www.minecraft.net/register.jsp").toURI());
/*     */             } catch (Exception e) {
/* 298 */               e.printStackTrace();
/*     */             }
/*     */           }
/*     */         });
/* 302 */         accountLink.setForeground(Color.BLACK);
/* 303 */         registerPanel.add(accountLink, "West");
/* 304 */         registerPanel.add(new Panel(), "Center");
/*     */       }
/*     */     }
/*     */     catch (Error localError) {
/*     */     }
/* 309 */     loginPanel.add(registerPanel, "Center");
/* 310 */     loginPanel.add(this.launchButton, "East");
/* 311 */     panel.add(loginPanel, "South");
/*     */ 
/* 313 */     this.errorLabel.setFont(new Font(null, 2, 16));
/* 314 */     this.errorLabel.setForeground(new Color(8388608));
/* 315 */     panel.add(this.errorLabel, "North");
/*     */ 
/* 317 */     return panel;
/*     */   }
/*     */ 
/*     */   private Panel buildOfflinePanel() {
/* 321 */     Panel panel = new Panel()
/*     */     {
/*     */       private static final long serialVersionUID = 1L;
/* 324 */       private Insets insets = new Insets(12, 24, 16, 32);
/*     */ 
/*     */       public Insets getInsets() {
/* 327 */         return this.insets;
/*     */       }
/*     */ 
/*     */       public void update(Graphics g) {
/* 331 */         paint(g);
/*     */       }
/*     */ 
/*     */       public void paint(Graphics g) {
/* 335 */         super.paint(g);
/* 336 */         int hOffs = 0;
/* 337 */         g.setColor(Color.BLACK);
/* 338 */         g.drawRect(0, 0 + hOffs, getWidth() - 1, getHeight() - 1 - hOffs);
/* 339 */         g.drawRect(1, 1 + hOffs, getWidth() - 3, getHeight() - 3 - hOffs);
/* 340 */         g.setColor(Color.WHITE);
/*     */ 
/* 342 */         g.drawRect(2, 2 + hOffs, getWidth() - 5, getHeight() - 5 - hOffs);
/*     */       }
/*     */     };
/* 345 */     panel.setBackground(Color.GRAY);
/* 346 */     BorderLayout layout = new BorderLayout();
/* 347 */     panel.setLayout(layout);
/*     */ 
/* 349 */     Panel loginPanel = new Panel(new BorderLayout());
/* 350 */     loginPanel.add(new Panel(), "Center");
/* 351 */     panel.add(new Panel(), "Center");
/* 352 */     loginPanel.add(this.retryButton, "East");
/* 353 */     loginPanel.add(this.offlineButton, "West");
/*     */ 
/* 355 */     boolean canPlayOffline = this.launcherFrame.canPlayOffline(this.userName.getText());
/* 356 */     this.offlineButton.setEnabled(canPlayOffline);
/* 357 */     if (!canPlayOffline) {
/* 358 */       panel.add(new Label("Play online once to enable offline"), "Center");
/*     */     }
/* 360 */     panel.add(loginPanel, "South");
/*     */ 
/* 362 */     this.errorLabel.setFont(new Font(null, 2, 16));
/* 363 */     this.errorLabel.setForeground(new Color(8388608));
/* 364 */     panel.add(this.errorLabel, "North");
/*     */ 
/* 366 */     return panel;
/*     */   }
/*     */ 
/*     */   public void setError(String errorMessage) {
/* 370 */     removeAll();
/* 371 */     add(buildLoginPanel());
/* 372 */     this.errorLabel.setText(errorMessage);
/* 373 */     validate();
/*     */   }
/*     */ 
/*     */   public void loginOk() {
/* 377 */     writeUsername();
/*     */   }
/*     */ 
/*     */   public void setNoNetwork() {
/* 381 */     removeAll();
/* 382 */     add(buildOfflinePanel());
/* 383 */     validate();
/*     */   }
/*     */ 
/*     */   public void checkAutologin() {
/* 387 */     if (this.password.getText().length() > 0)
/* 388 */       this.launcherFrame.login(this.userName.getText(), this.password.getText());
/*     */   }
/*     */ 
/*     */   public void setOutdated()
/*     */   {
/* 393 */     this.outdated = true;
/*     */   }
/*     */ }

/* Location:           C:\Users\Xemnias\Downloads\Minefield.jar
 * Qualified Name:     net.minecraft.LoginForm
 * JD-Core Version:    0.6.0
 */