 package fr.xemnias.cclauncherclient;
 import java.util.ArrayList;

public class CCLauncherClient
{
  @SuppressWarnings("unused")
private static final int MIN_HEAP = 511;
   @SuppressWarnings("unused")
private static final int RECOMMENDED_HEAP = 1024;
 
  @SuppressWarnings("unchecked")
public static void main(String[] args)
    throws Exception
  {
    float heapSizeMegs = (float)(Runtime.getRuntime().maxMemory() / 1024L / 1024L);

    if (heapSizeMegs > 511.0F)
       LauncherFrame.main(args);
    else
      try {
       String pathToJar = CCLauncherClient.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
 
       @SuppressWarnings("rawtypes")
ArrayList params = new ArrayList();

        params.add("javaw");
       params.add("-Xmx1024m");
      params.add("-Dsun.java2d.noddraw=true");
      params.add("-Dsun.java2d.d3d=false");
      params.add("-Dsun.java2d.pmoffscreen=false");

        params.add("-classpath");
         params.add(pathToJar);
        params.add("net.minecraft.LauncherFrame");
        ProcessBuilder pb = new ProcessBuilder(params);
        Process process = pb.start();
      if (process == null) throw new Exception("!");
         System.exit(0);
     } catch (Exception e) {
        e.printStackTrace();
         LauncherFrame.main(args);
      }
  }
 }

