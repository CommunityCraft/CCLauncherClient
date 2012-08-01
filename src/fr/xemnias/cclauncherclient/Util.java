/*    */ package fr.xemnias.cclauncherclient;
/*    */ 
/*    */ import java.io.BufferedReader;
import java.io.DataInputStream;
/*    */ import java.io.DataOutputStream;
/*    */ import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
/*    */ import java.io.InputStream;
/*    */ import java.io.InputStreamReader;
/*    */ import java.net.HttpURLConnection;
import java.net.URL;
/*    */ 
/*    */ public class Util
/*    */ {
/* 11 */   private static File workDir = null;
/*    */ 
/*    */   public static File getWorkingDirectory() {
/* 14 */     if (workDir == null) workDir = getWorkingDirectory("minecraft");
/* 15 */     return workDir;
/*    */   }
/*    */ 
public static File getWorkingDirectory(String applicationName)
/*     */   {
/*     */     File workingDirectory;
/*  44 */     String userHome = System.getProperty("user.home", ".");
/*     */ 
/*  46 */     switch (getPlatform().ordinal())
/*     */     {
/*     */     case 0:
/*     */     case 1:
/*  49 */       workingDirectory = new File(userHome, '.' + applicationName + '/');
/*  50 */       break;
/*     */     case 2:
/*  52 */       String applicationData = System.getenv("APPDATA");
/*  53 */       if (applicationData != null) workingDirectory = new File(applicationData, "." + applicationName + '/');
/*     */       else workingDirectory = new File(userHome, '.' + applicationName + '/');
/*  55 */       break;
/*     */     case 3:
/*  57 */       workingDirectory = new File(userHome, "Library/Application Support/" + applicationName);
/*  58 */       break;
/*     */     default:
/*  60 */       workingDirectory = new File(userHome, applicationName + '/');
/*     */     }
/*  62 */     if ((!(workingDirectory.exists())) && (!(workingDirectory.mkdirs()))) throw new RuntimeException("The working directory could not be created: " + workingDirectory);
/*  63 */     return workingDirectory;
/*     */   }
/*    */   private static OS getPlatform() {
/* 42 */     String osName = System.getProperty("os.name").toLowerCase();
/* 43 */     if (osName.contains("win")) return OS.windows;
/* 44 */     if (osName.contains("mac")) return OS.macos;
/* 45 */     if (osName.contains("solaris")) return OS.solaris;
/* 46 */     if (osName.contains("sunos")) return OS.solaris;
/* 47 */     if (osName.contains("linux")) return OS.linux;
/* 48 */     if (osName.contains("unix")) return OS.linux;
/* 49 */     return OS.unknown;
/*    */   }
/*    */ 
/*    */   public static String excutePost(String targetURL, String urlParameters)
/*    */   {
/* 54 */     HttpURLConnection connection = null;
/*    */     try
/*    */     {
/* 57 */       URL url = new URL(targetURL);
/* 58 */       connection = (HttpURLConnection)url.openConnection();
/* 59 */       connection.setRequestMethod("POST");
/* 60 */       connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
/*    */ 
/* 62 */       connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
/* 63 */       connection.setRequestProperty("Content-Language", "en-US");
/*    */ 
/* 65 */       connection.setUseCaches(false);
/* 66 */       connection.setDoInput(true);
/* 67 */       connection.setDoOutput(true);
/*    */ 
/* 70 */       DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
/* 71 */       wr.writeBytes(urlParameters);
/* 72 */       wr.flush();
/* 73 */       wr.close();
/*    */ 
/* 76 */       InputStream is = connection.getInputStream();
/* 77 */       BufferedReader rd = new BufferedReader(new InputStreamReader(is));
/*    */ 
/* 79 */       StringBuffer response = new StringBuffer();
/*    */       String line;
/* 80 */       while ((line = rd.readLine()) != null)
/*    */       {
/* 81 */         response.append(line);
/* 82 */         response.append('\r');
/*    */       }
/* 84 */       rd.close();
/* 85 */       String str1 = response.toString();
/*    */       return str1;
/*    */     }
/*    */     catch (Exception e)
/*    */     {
/* 89 */       e.printStackTrace();
/*    */       return null;
/*    */     }
/*    */     finally
/*    */     {
/* 94 */       if (connection != null)
/* 95 */         connection.disconnect();
/*    */     }
/* 97 */     //throw localObject;
/*    */   }
/*    */ 
/*    */   private static enum OS
/*    */   {
/*  8 */     linux, solaris, windows, macos, unknown;
/*    */   }
/*    */
/*     */   public static String getFakeLatestVersion()
/*     */   {
/*     */     try
/*     */     {
/* 192 */       File dir = new File(getWorkingDirectory() + File.separator + "bin" + File.separator);
/* 193 */       File file = new File(dir, "version");
/* 194 */       DataInputStream dis = new DataInputStream(new FileInputStream(file));
/* 195 */       String version = dis.readUTF();
/* 196 */       dis.close();
/* 197 */       if (version.equals("0")) {
/* 198 */         return "1285241960000";
/*     */       }
/* 200 */       return version; } catch (IOException localIOException) {
/*     */     }
/* 202 */     return "1285241960000";
/*     */   } }

/* Location:           C:\Users\Xemnias\Downloads\Minefield.jar
 * Qualified Name:     net.minecraft.Util
 * JD-Core Version:    0.6.0
 */