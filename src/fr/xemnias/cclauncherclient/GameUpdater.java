/*     */ package fr.xemnias.cclauncherclient;
/*     */ 
/*     */ import java.applet.Applet;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.BufferedWriter;
/*     */ import java.io.DataInputStream;
/*     */ import java.io.DataOutputStream;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.FilePermission;
/*     */ import java.io.FileWriter;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.InputStreamReader;
/*     */ import java.io.OutputStream;
/*     */ import java.io.PrintWriter;
/*     */ import java.io.StringWriter;
/*     */ import java.io.Writer;
/*     */ import java.lang.reflect.Constructor;
/*     */ import java.lang.reflect.Field;
/*     */ import java.lang.reflect.Method;
/*     */ import java.net.HttpURLConnection;
/*     */ import java.net.JarURLConnection;
/*     */ import java.net.SocketPermission;
/*     */ import java.net.URL;
/*     */ import java.net.URLClassLoader;
/*     */ import java.net.URLConnection;
/*     */ import java.security.AccessControlException;
/*     */ import java.security.AccessController;
/*     */ import java.security.CodeSource;
/*     */ import java.security.PermissionCollection;
/*     */ import java.security.PrivilegedExceptionAction;
/*     */ import java.security.SecureClassLoader;
/*     */ import java.security.cert.Certificate;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Scanner;
/*     */ import java.util.StringTokenizer;
/*     */ import java.util.Vector;
/*     */ import java.util.jar.JarEntry;
/*     */ import java.util.jar.JarFile;
/*     */ import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
/*     */ 
/*     */ public class GameUpdater
/*     */   implements Runnable
/*     */ {
	
	
	private String folderOfDownload = "http://s3.amazonaws.com/MinecraftDownload/";
	
	private String pathOfJarFile = "http://www.communitycraft-fr.com/java/other/";
	
	private String urlToVersionFile = "http://www.communitycraft-fr.com/java/other/version_cc.txt";
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
/*     */   public static final int STATE_INIT = 1;
/*     */   public static final int STATE_DETERMINING_PACKAGES = 2;
/*     */   public static final int STATE_CHECKING_CACHE = 3;
/*     */   public static final int STATE_DOWNLOADING = 4;
/*     */   public static final int STATE_EXTRACTING_PACKAGES = 5;
/*     */   public static final int STATE_UPDATING_CLASSPATH = 6;
/*     */   public static final int STATE_SWITCHING_APPLET = 7;
/*     */   public static final int STATE_INITIALIZE_REAL_APPLET = 8;
/*     */   public static final int STATE_START_REAL_APPLET = 9;
/*     */   public static final int STATE_DONE = 10;
/*     */   public int percentage;
/*     */   public int currentSizeDownload;
/*     */   public int totalSizeDownload;
/*     */   public int currentSizeExtract;
/*     */   public int totalSizeExtract;
/*     */   protected URL[] urlList;
/*     */   private static ClassLoader classLoader;
/*     */   protected Thread loaderThread;
/*     */   protected Thread animationThread;
/*     */   public boolean fatalError;
/*     */   public String fatalErrorDescription;
/*  78 */   protected String subtaskMessage = "";
/*  79 */   protected int state = 1;
/*     */ 
/*  81 */   protected boolean lzmaSupported = false;
/*  82 */   protected boolean pack200Supported = false;
/*     */ 
/*  84 */   protected String[] genericErrorMessage = { "An error occured while loading the applet.", "Please contact support to resolve this issue.", "<placeholder for error message>" };
/*     */   protected boolean certificateRefused;
/*  86 */   protected String[] certificateRefusedMessage = { "Permissions for Applet Refused.", "Please accept the permissions dialog to allow", "the applet to continue the loading process." };
/*     */ 
/*  88 */   protected static boolean natives_loaded = false;
/*     */   private String latestVersion;
/*     */   private String mainGameUrl;
/*     */ 
/*     */   public GameUpdater(String latestVersion, String mainGameUrl)
/*     */   {
/*  94 */     this.latestVersion = latestVersion;
/*  95 */     this.mainGameUrl = mainGameUrl;
/*     */   }
/*     */ 
/*     */   public void init() {
/*  99 */     this.state = 1;
/*     */     try
/*     */     {
/* 102 */       Class.forName("LZMA.LzmaInputStream");
/* 103 */       this.lzmaSupported = true;
/*     */     }
/*     */     catch (Throwable localThrowable) {
/*     */     }
/*     */     try {
/* 108 */       Pack200.class.getSimpleName();
/* 109 */       this.pack200Supported = true;
/*     */     } catch (Throwable localThrowable1) {
/*     */     }
/*     */   }
/*     */ 
/*     */   private String generateStacktrace(Exception exception) {
/* 115 */     Writer result = new StringWriter();
/* 116 */     PrintWriter printWriter = new PrintWriter(result);
/* 117 */     exception.printStackTrace(printWriter);
/* 118 */     return result.toString();
/*     */   }
/*     */ 
/*     */   protected String getDescriptionForState()
/*     */   {
/* 123 */     switch (this.state) {
/*     */     case 1:
/* 125 */       return "Instantiation des objets";
/*     */     case 2:
/* 127 */       return "Détermiation des fichier a chargés";
/*     */     case 3:
/* 129 */       return "Vérification du cache";
/*     */     case 4:
/* 131 */       return "Téléchargement des  packages";
/*     */     case 5:
/* 133 */       return "Décompréssion des packages";
/*     */     case 6:
/* 135 */       return "Mise à jour du ClassPath";
/*     */     case 7:
/* 137 */       return "Switch de l'applet";
/*     */     case 8:
/* 139 */       return "Initialisation de l'applet";
/*     */     case 9:
/* 141 */       return "Démarrage de l'applet";
/*     */     case 10:
/* 143 */       return "Chargement terminé";
/*     */     case 11:
/* 146 */       return "Mise Ã  jour de CommunityCraft";
/*     */     }
/*     */ 
/* 149 */     return "What the hell is this state ?!";
/*     */   }
/*     */ 
/*     */   protected String trimExtensionByCapabilities(String file)
/*     */   {
/* 154 */     if (!this.pack200Supported) {
/* 155 */       file = file.replaceAll(".pack", "");
/*     */     }
/*     */ 
/* 158 */     if (!this.lzmaSupported) {
/* 159 */       file = file.replaceAll(".lzma", "");
/*     */     }
/* 161 */     return file;
/*     */   }
/*     */ 
/*     */   protected void loadJarURLs() throws Exception {
/* 165 */     this.state = 2;
/* 166 */     String jarList = "lwjgl.jar, jinput.jar, lwjgl_util.jar, " + this.mainGameUrl;
/* 167 */     jarList = trimExtensionByCapabilities(jarList);
/*     */ 
/* 169 */     StringTokenizer jar = new StringTokenizer(jarList, ", ");
/* 170 */     int jarCount = jar.countTokens() + 1;
/*     */ 
/* 172 */     this.urlList = new URL[jarCount];
/*     */ 
/* 174 */     URL path = new URL(folderOfDownload);
/*     */ 
/* 176 */     for (int i = 0; i < jarCount - 1; i++) {
/* 177 */       String nextToken = jar.nextToken();
/* 178 */       URL oldPath = path;
/*     */ 
/* 181 */       if (nextToken.indexOf("craft.jar") >= 0) {
/* 182 */         path = new URL(pathOfJarFile);
System.out.println("ok");
/*     */       }
/*     */ 
/* 185 */       System.out.println(path + nextToken.replaceAll("minecraft.jar", "communitycraft.jar"));
/* 186 */       if (nextToken.indexOf("craft.jar") >= 0) {
	System.out.println("ok");
/* 187 */         this.urlList[i] = new URL(path, nextToken.replaceAll("minecraft.jar", "communitycraft.jar"));
/*     */       }
/*     */       else {
/* 190 */         this.urlList[i] = new URL(path, nextToken);
/*     */       }
/*     */ 
/* 194 */       if (nextToken.indexOf("craft.jar") >= 0) {
/* 195 */         path = oldPath;
/*     */       }
/*     */     }
/*     */ 
/* 199 */     String osName = System.getProperty("os.name");
/* 200 */     String nativeJar = null;
/*     */ 
/* 202 */     if (osName.startsWith("Win"))
/* 203 */       nativeJar = "windows_natives.jar.lzma";
/* 204 */     else if (osName.startsWith("Linux"))
/* 205 */       nativeJar = "linux_natives.jar.lzma";
/* 206 */     else if (osName.startsWith("Mac"))
/* 207 */       nativeJar = "macosx_natives.jar.lzma";
/* 208 */     else if ((osName.startsWith("Solaris")) || (osName.startsWith("SunOS")))
/* 209 */       nativeJar = "solaris_natives.jar.lzma";
/*     */     else {
/* 211 */       fatalErrorOccured("OS (" + osName + ") not supported", null);
/*     */     }
/*     */ 
/* 214 */     if (nativeJar == null) {
/* 215 */       fatalErrorOccured("no lwjgl natives files found", null);
/*     */     } else {
/* 217 */       nativeJar = trimExtensionByCapabilities(nativeJar);
/* 218 */       this.urlList[(jarCount - 1)] = new URL(path, nativeJar);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/* 224 */     init();
/* 225 */     this.state = 3;
/*     */ 
/* 227 */     this.percentage = 5;
/*     */     try
/*     */     {
/* 230 */       loadJarURLs();
/*     */ 
/* 232 */       @SuppressWarnings({ "unchecked", "rawtypes" })
String path = (String)AccessController.doPrivileged(new PrivilegedExceptionAction() {
/*     */         public Object run() throws Exception {
/* 234 */           return Util.getWorkingDirectory() + File.separator + "bin" + File.separator;
/*     */         }
/*     */       });
/* 237 */       File dir = new File(path);
/*     */ 
/* 239 */       if (!dir.exists()) {
/* 240 */         dir.mkdirs();
/*     */       }
/*     */ 
/* 243 */       if (this.latestVersion != null) {
/* 244 */         File versionFile = new File(dir, "version");
/*     */ 
/* 246 */         boolean cacheAvailable = false;
/* 247 */         if ((versionFile.exists()) && (
/* 248 */           (this.latestVersion.equals("-1")) || (this.latestVersion.equals(readVersionFile(versionFile))))) {
/* 249 */           cacheAvailable = true;
/* 250 */           this.percentage = 90;
/*     */         }
/*     */ 
/* 254 */         boolean updateMinefield = false;
/*     */         try {
/* 256 */           String version_minefield = "";
/* 257 */           URL url_version = new URL(urlToVersionFile);
/*     */           try {
/* 259 */             BufferedReader in = new BufferedReader(new InputStreamReader(url_version.openStream()));
/* 260 */             version_minefield = in.readLine();
/*     */           }
/*     */           catch (Exception e) {
/* 263 */             System.err.println(e);
/*     */           }
/* 265 */           File current_version_minefield = new File(dir, "version_cc.txt");
/*     */ 
/* 267 */           if (!current_version_minefield.exists()) {
/* 268 */             updateMinefield = true;
/*     */             try {
/* 270 */               BufferedWriter bw = new BufferedWriter(new FileWriter(current_version_minefield));
/* 271 */               bw.append(version_minefield);
/* 272 */               bw.close();
/*     */             } catch (IOException e) {
/* 274 */               System.out.println("Erreur");
/*     */             }
/*     */           }
/*     */           else
/*     */           {
/*     */             try {
/* 280 */               Scanner scanner = new Scanner(current_version_minefield);
/* 281 */               while (scanner.hasNextLine()) {
/* 282 */                 String line = scanner.nextLine().trim();
/* 283 */                 if (!version_minefield.equals(line)) {
/* 284 */                   updateMinefield = true;
/*     */                   try {
/* 286 */                     BufferedWriter bw = new BufferedWriter(new FileWriter(current_version_minefield));
/* 287 */                     bw.append(version_minefield);
/* 288 */                     bw.close();
/*     */                   } catch (IOException e) {
/* 290 */                     System.out.println("Erreur");
/*     */                   }
/*     */                 }
/*     */               }
/*     */ 
/* 295 */               scanner.close();
/*     */             } catch (IOException e) {
/* 297 */               System.out.println("Erreur" + e.getMessage());
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/*     */         }
/*     */         catch (Exception localException1)
/*     */         {
/*     */         }
/*     */ 
/* 308 */         if ((!cacheAvailable) || (updateMinefield)) {
/* 309 */           downloadJars(path);
/* 310 */           extractJars(path);
/* 311 */           extractNatives(path);
/*     */ 
/* 313 */           if (this.latestVersion != null) {
/* 314 */             this.percentage = 90;
/* 315 */             writeVersionFile(versionFile, this.latestVersion);
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 320 */       updateClassPath(dir);
/* 321 */       this.state = 10;
/*     */     } catch (AccessControlException ace) {
/* 323 */       fatalErrorOccured(ace.getMessage(), ace);
/* 324 */       this.certificateRefused = true;
/*     */     } catch (Exception e) {
/* 326 */       fatalErrorOccured(e.getMessage(), e);
/*     */     } finally {
/* 328 */       this.loaderThread = null;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected String readVersionFile(File file) throws Exception {
/* 333 */     DataInputStream dis = new DataInputStream(new FileInputStream(file));
/* 334 */     String version = dis.readUTF();
/* 335 */     dis.close();
/* 336 */     return version;
/*     */   }
/*     */ 
/*     */   protected void writeVersionFile(File file, String version) throws Exception {
/* 340 */     DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));
/* 341 */     dos.writeUTF(version);
/* 342 */     dos.close();
/*     */   }
/*     */ 
/*     */   protected void updateClassPath(File dir)
/*     */     throws Exception
/*     */   {
/* 348 */     this.state = 6;
/*     */ 
/* 350 */     this.percentage = 95;
/*     */ 
/* 352 */     URL[] urls = new URL[this.urlList.length];
/* 353 */     for (int i = 0; i < this.urlList.length; i++) {
/* 354 */       urls[i] = new File(dir, getJarName(this.urlList[i])).toURI().toURL();
/*     */     }
/*     */ 
/* 357 */     if (classLoader == null)
/* 358 */       classLoader = new URLClassLoader(urls) {
/*     */         protected PermissionCollection getPermissions(CodeSource codesource) {
/* 360 */           PermissionCollection perms = null;
/*     */           try
/*     */           {
/* 363 */             Method method = SecureClassLoader.class.getDeclaredMethod("getPermissions", new Class[] { CodeSource.class });
/* 364 */             method.setAccessible(true);
/* 365 */             perms = (PermissionCollection)method.invoke(getClass().getClassLoader(), new Object[] { codesource });
/*     */ 
/* 367 */             String host = "www.minecraft.net";
/*     */ 
/* 369 */             if ((host != null) && (host.length() > 0))
/*     */             {
/* 371 */               perms.add(new SocketPermission(host, "connect,accept"));
/*     */             } else codesource.getLocation().getProtocol().equals("file");
/*     */ 
/* 374 */             perms.add(new FilePermission("<<ALL FILES>>", "read"));
/*     */           }
/*     */           catch (Exception e) {
/* 377 */             e.printStackTrace();
/*     */           }
/*     */ 
/* 380 */           return perms;
/*     */         }
/*     */       };
/* 384 */     String path = dir.getAbsolutePath();
/* 385 */     if (!path.endsWith(File.separator)) path = path + File.separator;
/* 386 */     unloadNatives(path);
/*     */ 
/* 388 */     System.setProperty("org.lwjgl.librarypath", path + "natives");
/* 389 */     System.setProperty("net.java.games.input.librarypath", path + "natives");
/*     */ 
/* 391 */     natives_loaded = true;
/*     */   }
/*     */ 
/*     */   private void unloadNatives(String nativePath)
/*     */   {
/* 396 */     if (!natives_loaded) {
/* 397 */       return;
/*     */     }
/*     */     try
/*     */     {
/* 401 */       Field field = ClassLoader.class.getDeclaredField("loadedLibraryNames");
/* 402 */       field.setAccessible(true);
/* 403 */       @SuppressWarnings("rawtypes")
Vector libs = (Vector)field.get(getClass().getClassLoader());
/*     */ 
/* 405 */       String path = new File(nativePath).getCanonicalPath();
/*     */ 
/* 407 */       for (int i = 0; i < libs.size(); i++) {
/* 408 */         String s = (String)libs.get(i);
/*     */ 
/* 410 */         if (s.startsWith(path)) {
/* 411 */           libs.remove(i);
/* 412 */           i--;
/*     */         }
/*     */       }
/*     */     } catch (Exception e) {
/* 416 */       e.printStackTrace();
/*     */     }
/*     */   }
/*     */ 
/*     */   public Applet createApplet() throws ClassNotFoundException, InstantiationException, IllegalAccessException
/*     */   {
/* 422 */     @SuppressWarnings("rawtypes")
Class appletClass = classLoader.loadClass("net.minecraft.client.MinecraftApplet");
/* 423 */     return (Applet)appletClass.newInstance();
/*     */   }
/*     */ 
/*     */   protected void downloadJars(String path)
/*     */     throws Exception
/*     */   {
/* 429 */     this.state = 4;
/*     */ 
/* 431 */     int[] fileSizes = new int[this.urlList.length];
/*     */ 
/* 433 */     for (int i = 0; i < this.urlList.length; i++) {
/* 434 */       System.out.println(this.urlList[i]);
/* 435 */       URLConnection urlconnection = this.urlList[i].openConnection();
/* 436 */       urlconnection.setDefaultUseCaches(false);
/* 437 */       if ((urlconnection instanceof HttpURLConnection)) {
/* 438 */         ((HttpURLConnection)urlconnection).setRequestMethod("HEAD");
/*     */       }
/* 440 */       fileSizes[i] = urlconnection.getContentLength();
/* 441 */       this.totalSizeDownload += fileSizes[i];
/*     */     }
/*     */ 
/* 444 */     int initialPercentage = this.percentage = 10;
/*     */ 
/* 446 */     byte[] buffer = new byte[65536];
/* 447 */     for (int i = 0; i < this.urlList.length; i++)
/*     */     {
/* 449 */       int unsuccessfulAttempts = 0;
/* 450 */       int maxUnsuccessfulAttempts = 3;
/* 451 */       boolean downloadFile = true;
/*     */ 
/* 453 */       while (downloadFile) {
/* 454 */         downloadFile = false;
/*     */ 
/* 456 */         URLConnection urlconnection = this.urlList[i].openConnection();
/*     */ 
/* 458 */         if ((urlconnection instanceof HttpURLConnection)) {
/* 459 */           urlconnection.setRequestProperty("Cache-Control", "no-cache");
/* 460 */           urlconnection.connect();
/*     */         }
/*     */ 
/* 463 */         String currentFile = getFileName(this.urlList[i]);
/* 464 */         InputStream inputstream = getJarInputStream(currentFile, urlconnection);
/* 465 */         FileOutputStream fos = new FileOutputStream(path + currentFile);
/*     */ 
/* 467 */         long downloadStartTime = System.currentTimeMillis();
/* 468 */         int downloadedAmount = 0;
/* 469 */         int fileSize = 0;
/* 470 */         String downloadSpeedMessage = "";
/*     */         int bufferSize;
/* 472 */         while ((bufferSize = inputstream.read(buffer, 0, buffer.length)) != -1)
/*     */         {
/* 474 */           fos.write(buffer, 0, bufferSize);
/* 475 */           this.currentSizeDownload += bufferSize;
/* 476 */           fileSize += bufferSize;
/* 477 */           this.percentage = (initialPercentage + this.currentSizeDownload * 45 / this.totalSizeDownload);
/* 478 */           this.subtaskMessage = ("Retrieving: " + currentFile + " " + this.currentSizeDownload * 100 / this.totalSizeDownload + "%");
/*     */ 
/* 480 */           downloadedAmount += bufferSize;
/* 481 */           long timeLapse = System.currentTimeMillis() - downloadStartTime;
/*     */ 
/* 483 */           if (timeLapse >= 1000L)
/*     */           {
/* 485 */             float downloadSpeed = downloadedAmount / (float)timeLapse;
/*     */ 
/* 487 */             downloadSpeed = (int)(downloadSpeed * 100.0F) / 100.0F;
/*     */ 
/* 489 */             downloadSpeedMessage = " @ " + downloadSpeed + " KB/sec";
/*     */ 
/* 491 */             downloadedAmount = 0;
/*     */ 
/* 493 */             downloadStartTime += 1000L;
/*     */           }
/*     */ 
/* 496 */           this.subtaskMessage += downloadSpeedMessage;
/*     */         }
/*     */ 
/* 499 */         inputstream.close();
/* 500 */         fos.close();
/*     */ 
/* 502 */         if ((!(urlconnection instanceof HttpURLConnection)) || 
/* 503 */           (fileSize == fileSizes[i]))
/*     */           continue;
/* 505 */         if (fileSizes[i] <= 0)
/*     */         {
/*     */           continue;
/*     */         }
/* 509 */         unsuccessfulAttempts++;
/*     */ 
/* 511 */         if (unsuccessfulAttempts < maxUnsuccessfulAttempts) {
/* 512 */           downloadFile = true;
/* 513 */           this.currentSizeDownload -= fileSize;
/*     */         }
/*     */         else {
/* 516 */           throw new Exception("failed to download " + currentFile);
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 522 */     this.subtaskMessage = "";
/*     */   }
/*     */ 
/*     */   protected InputStream getJarInputStream(String currentFile, final URLConnection urlconnection)
/*     */     throws Exception
/*     */   {
/* 528 */     final InputStream[] is = new InputStream[1];
/*     */ 
/* 530 */     for (int j = 0; (j < 3) && (is[0] == null); j++) {
	
/* 531 */       Thread t = new Thread() {
/*     */         public void run() {
/*     */           try {
/* 534 */             is[0] = urlconnection.getInputStream();
/*     */           }
/*     */           catch (IOException localIOException)
/*     */           {
/*     */           }
/*     */         }
/*     */       };
/* 541 */       t.setName("JarInputStreamThread");
/* 542 */       t.start();
/*     */ 
/* 544 */       int iterationCount = 0;
/* 545 */       while ((is[0] == null) && (iterationCount++ < 5)) {
/*     */         try {
/* 547 */           t.join(1000L);
/*     */         }
/*     */         catch (InterruptedException localInterruptedException)
/*     */         {
/*     */         }
/*     */       }
/* 553 */       if (is[0] != null) continue;
/*     */       try {
/* 555 */         t.interrupt();
/* 556 */         t.join();
/*     */       }
/*     */       catch (InterruptedException localInterruptedException1)
/*     */       {
/*     */       }
/*     */     }
/*     */ 
/* 563 */     if (is[0] == null) {
/* 564 */       if (currentFile.equals("minecraft.jar")) {
/* 565 */         throw new Exception("Unable to download " + currentFile);
/*     */       }
/* 567 */       throw new Exception("Unable to download " + currentFile);
/*     */     }
/*     */ 
/* 570 */     return is[0];
/*     */   }
/*     */ 
/*     */   @SuppressWarnings("rawtypes")
protected void extractLZMA(String in, String out)
/*     */     throws Exception
/*     */   {
/* 576 */     File f = new File(in);
/* 577 */     FileInputStream fileInputHandle = new FileInputStream(f);
/*     */ 
/* 579 */     Class clazz = Class.forName("LZMA.LzmaInputStream");
/* 580 */     @SuppressWarnings("unchecked")
Constructor constructor = clazz.getDeclaredConstructor(new Class[] { InputStream.class });
/* 581 */     InputStream inputHandle = (InputStream)constructor.newInstance(new Object[] { fileInputHandle });
/*     */ 
/* 583 */     OutputStream outputHandle = new FileOutputStream(out);
/*     */ 
/* 585 */     byte[] buffer = new byte[16384];
/*     */ 
/* 587 */     int ret = inputHandle.read(buffer);
/* 588 */     while (ret >= 1) {
/* 589 */       outputHandle.write(buffer, 0, ret);
/* 590 */       ret = inputHandle.read(buffer);
/*     */     }
/*     */ 
/* 593 */     inputHandle.close();
/* 594 */     outputHandle.close();
/*     */ 
/* 596 */     outputHandle = null;
/* 597 */     inputHandle = null;
/*     */ 
/* 599 */     f.delete();
/*     */   }
/*     */ 
/*     */   protected void extractPack(String in, String out)
/*     */     throws Exception
/*     */   {
/* 605 */     File f = new File(in);
/* 606 */     FileOutputStream fostream = new FileOutputStream(out);
/* 607 */     JarOutputStream jostream = new JarOutputStream(fostream);
/*     */ 
/* 609 */     Pack200.Unpacker unpacker = Pack200.newUnpacker();
/* 610 */     unpacker.unpack(f, jostream);
/* 611 */     jostream.close();
/*     */ 
/* 613 */     f.delete();
/*     */   }
/*     */ 
/*     */   protected void extractJars(String path)
/*     */     throws Exception
/*     */   {
/* 619 */     this.state = 5;
/*     */ 
/* 621 */     float increment = 10.0F / this.urlList.length;
/*     */ 
/* 623 */     for (int i = 0; i < this.urlList.length; i++) {
/* 624 */       this.percentage = (55 + (int)(increment * (i + 1)));
/* 625 */       String filename = getFileName(this.urlList[i]);
/*     */ 
/* 627 */       if (filename.endsWith(".pack.lzma")) {
/* 628 */         this.subtaskMessage = ("Extracting: " + filename + " to " + filename.replaceAll(".lzma", ""));
/* 629 */         extractLZMA(path + filename, path + filename.replaceAll(".lzma", ""));
/*     */ 
/* 631 */         this.subtaskMessage = ("Extracting: " + filename.replaceAll(".lzma", "") + " to " + filename.replaceAll(".pack.lzma", ""));
/* 632 */         extractPack(path + filename.replaceAll(".lzma", ""), path + filename.replaceAll(".pack.lzma", ""));
/* 633 */       } else if (filename.endsWith(".pack")) {
/* 634 */         this.subtaskMessage = ("Extracting: " + filename + " to " + filename.replace(".pack", ""));
/* 635 */         extractPack(path + filename, path + filename.replace(".pack", ""));
/* 636 */       } else if (filename.endsWith(".lzma")) {
/* 637 */         this.subtaskMessage = ("Extracting: " + filename + " to " + filename.replace(".lzma", ""));
/* 638 */         extractLZMA(path + filename, path + filename.replace(".lzma", ""));
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   @SuppressWarnings("rawtypes")
protected void extractNatives(String path) throws Exception
/*     */   {
/* 645 */     this.state = 5;
/*     */ 
/* 647 */     int initialPercentage = this.percentage;
/*     */ 
/* 649 */     String nativeJar = getJarName(this.urlList[(this.urlList.length - 1)]);
/*     */ 
/* 651 */     Certificate[] certificate = Launcher.class.getProtectionDomain().getCodeSource().getCertificates();
/*     */ 
/* 653 */     if (certificate == null) {
/* 654 */       URL location = Launcher.class.getProtectionDomain().getCodeSource().getLocation();
/*     */ 
/* 656 */       JarURLConnection jurl = (JarURLConnection)new URL("jar:" + location.toString() + "!/net/minecraft/Launcher.class").openConnection();
/* 657 */       jurl.setDefaultUseCaches(true);
/*     */       try {
/* 659 */         certificate = jurl.getCertificates();
/*     */       }
/*     */       catch (Exception localException)
/*     */       {
/*     */       }
/*     */     }
/* 665 */     File nativeFolder = new File(path + "natives");
/* 666 */     if (!nativeFolder.exists()) {
/* 667 */       nativeFolder.mkdir();
/*     */     }
/*     */ 
/* 670 */     JarFile jarFile = new JarFile(path + nativeJar, true);
/* 671 */     Enumeration entities = jarFile.entries();
/*     */ 
/* 673 */     this.totalSizeExtract = 0;
/*     */ 
/* 675 */     while (entities.hasMoreElements()) {
/* 676 */       JarEntry entry = (JarEntry)entities.nextElement();
/*     */ 
/* 678 */       if ((entry.isDirectory()) || (entry.getName().indexOf('/') != -1)) {
/*     */         continue;
/*     */       }
/* 681 */       this.totalSizeExtract = (int)(this.totalSizeExtract + entry.getSize());
/*     */     }
/*     */ 
/* 684 */     this.currentSizeExtract = 0;
/*     */ 
/* 686 */     entities = jarFile.entries();
/*     */ 
/* 688 */     while (entities.hasMoreElements()) {
/* 689 */       JarEntry entry = (JarEntry)entities.nextElement();
/*     */ 
/* 691 */       if ((entry.isDirectory()) || (entry.getName().indexOf('/') != -1))
/*     */       {
/*     */         continue;
/*     */       }
/* 695 */       File f = new File(path + "natives" + File.separator + entry.getName());
/* 696 */       if ((f.exists()) && 
/* 697 */         (!f.delete()))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 702 */       InputStream in = jarFile.getInputStream(jarFile.getEntry(entry.getName()));
/* 703 */       OutputStream out = new FileOutputStream(path + "natives" + File.separator + entry.getName());
/*     */ 
/* 705 */       byte[] buffer = new byte[65536];
/*     */       int bufferSize;
/* 707 */       while ((bufferSize = in.read(buffer, 0, buffer.length)) != -1)
/*     */       {
/* 709 */         out.write(buffer, 0, bufferSize);
/* 710 */         this.currentSizeExtract += bufferSize;
/*     */ 
/* 712 */         this.percentage = (initialPercentage + this.currentSizeExtract * 20 / this.totalSizeExtract);
/* 713 */         this.subtaskMessage = ("Extracting: " + entry.getName() + " " + this.currentSizeExtract * 100 / this.totalSizeExtract + "%");
/*     */       }
/*     */ 
/* 716 */       validateCertificateChain(certificate, entry.getCertificates());
/*     */ 
/* 718 */       in.close();
/* 719 */       out.close();
/*     */     }
/* 721 */     this.subtaskMessage = "";
/*     */ 
/* 723 */     jarFile.close();
/*     */ 
/* 725 */     File f = new File(path + nativeJar);
/* 726 */     f.delete();
/*     */   }
/*     */ 
/*     */   protected static void validateCertificateChain(Certificate[] ownCerts, Certificate[] native_certs)
/*     */     throws Exception
/*     */   {
/* 732 */     if (ownCerts == null) return;
/* 733 */     if (native_certs == null) throw new Exception("Unable to validate certificate chain. Native entry did not have a certificate chain at all");
/*     */ 
/* 735 */     if (ownCerts.length != native_certs.length) throw new Exception("Unable to validate certificate chain. Chain differs in length [" + ownCerts.length + " vs " + native_certs.length + "]");
/*     */ 
/* 737 */     for (int i = 0; i < ownCerts.length; i++)
/* 738 */       if (!ownCerts[i].equals(native_certs[i]))
/* 739 */         throw new Exception("Certificate mismatch: " + ownCerts[i] + " != " + native_certs[i]);
/*     */   }
/*     */ 
/*     */   protected String getJarName(URL url)
/*     */   {
/* 744 */     String fileName = url.getFile();
/*     */ 
/* 746 */     if (fileName.contains("?")) {
/* 747 */       fileName = fileName.substring(0, fileName.indexOf("?"));
/*     */     }
/* 749 */     if (fileName.endsWith(".pack.lzma"))
/* 750 */       fileName = fileName.replaceAll(".pack.lzma", "");
/* 751 */     else if (fileName.endsWith(".pack"))
/* 752 */       fileName = fileName.replaceAll(".pack", "");
/* 753 */     else if (fileName.endsWith(".lzma")) {
/* 754 */       fileName = fileName.replaceAll(".lzma", "");
/*     */     }
/*     */ 
/* 757 */     return fileName.substring(fileName.lastIndexOf('/') + 1);
/*     */   }
/*     */ 
/*     */   protected String getFileName(URL url) {
/* 761 */     String fileName = url.getFile();
/* 762 */     if (fileName.contains("?")) {
/* 763 */       fileName = fileName.substring(0, fileName.indexOf("?"));
/*     */     }
/* 765 */     return fileName.substring(fileName.lastIndexOf('/') + 1);
/*     */   }
/*     */ 
/*     */   protected void fatalErrorOccured(String error, Exception e) {
/* 769 */     e.printStackTrace();
/* 770 */     this.fatalError = true;
/* 771 */     this.fatalErrorDescription = ("Fatal error occured (" + this.state + "): " + error);
/* 772 */     System.out.println(this.fatalErrorDescription);
/*     */ 
/* 774 */     System.out.println(generateStacktrace(e));
/*     */   }
/*     */ 
/*     */   public boolean canPlayOffline()
/*     */   {
/*     */     try
/*     */     {
/* 781 */       @SuppressWarnings({ "unchecked", "rawtypes" })
String path = (String)AccessController.doPrivileged(new PrivilegedExceptionAction() {
/*     */         public Object run() throws Exception {
/* 783 */           return Util.getWorkingDirectory() + File.separator + "bin" + File.separator;
/*     */         }
/*     */       });
/* 786 */       File dir = new File(path);
/* 787 */       if (!dir.exists()) return false;
/*     */ 
/* 789 */       dir = new File(dir, "version");
/* 790 */       if (!dir.exists()) return false;
/*     */ 
/* 792 */       if (dir.exists()) {
/* 793 */         String version = readVersionFile(dir);
/* 794 */         if ((version != null) && (version.length() > 0))
/* 795 */           return true;
/*     */       }
/*     */     }
/*     */     catch (Exception e) {
/* 799 */       e.printStackTrace();
/* 800 */       return false;
/*     */     }
/* 802 */     return false;
/*     */   }
/*     */ }

/* Location:           C:\Users\Xemnias\Downloads\Minefield.jar
 * Qualified Name:     net.minecraft.GameUpdater
 * JD-Core Version:    0.6.0
 */