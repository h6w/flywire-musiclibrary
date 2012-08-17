package org.bentokit;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipFile;
import java.util.zip.ZipException;
import java.util.zip.ZipEntry;

import org.bentokit.FileDownloadListener;
import org.bentokit.ErrorHandler;

public class SoftwareUpdater
{
    public static boolean isUpdating = false;

    static String RELEASETYPE = "<!-- ReleaseType -->";
    static String PLATFORM;
    static String PLATFORMVERSION;
    static String ARCH;
    static String VERSION = "Version information unavailable!";
    static String BUILD = "Version information unavailable!";

    private static ArrayList<FileDownloadListener> listeners = new ArrayList<FileDownloadListener>();

    public static String getReleaseType() { getRuntimeDetails(); return(RELEASETYPE); }

    private static void getRuntimeDetails() {
        if (PLATFORM != null && PLATFORMVERSION != null && ARCH != null && RELEASETYPE != null && VERSION != null && BUILD != null) return;
        PLATFORM = System.getProperty("os.name");
        PLATFORMVERSION = System.getProperty("os.version"); 
        ARCH = System.getProperty("os.arch"); 
        ErrorHandler.info("Identified platform as "+PLATFORM+" "+PLATFORMVERSION+" on arch "+ARCH);

        try {
            BufferedReader reader = new BufferedReader(new FileReader("VERSION.txt"));
            
            String line = null;
            line = reader.readLine();
            String[] versioninfo = line.trim().split("-");
            if (versioninfo.length == 3) {
                RELEASETYPE = versioninfo[0];
                VERSION = versioninfo[1];
                BUILD = versioninfo[2];
                ErrorHandler.info("Identified version as "+RELEASETYPE+" "+VERSION+"-"+BUILD);
            } else
                ErrorHandler.error("Unknown version "+line+" from VERSION.txt file.");
        } catch (Exception e){
            File f = new File("VERSION.txt");
            ErrorHandler.error("Could not read version from "+f.getAbsolutePath());
        }

    }

    public static void addListener(FileDownloadListener listener) { listeners.add(listener); }

  	public static BufferedReader read(String url) throws Exception {
 		return new BufferedReader(
 			new InputStreamReader(
 				new URL(url).openStream()));
 	}
 
    public static String getCurrentVersion() {
        getRuntimeDetails();
        try {
            BufferedReader reader = read("http://www.athsvic.org.au/pub/software/OpenScore/currentversion.php"
                            +"?app=OpenScore&releasetype="+RELEASETYPE+"&platform="+PLATFORM+"&arch="+ARCH);
            ErrorHandler.userInfo("Website says current "+RELEASETYPE+" version is "+reader.readLine().trim());            
            return(reader.readLine().trim());
        } catch (Exception e) {
            ErrorHandler.userError("Could not access the website.  Please check your internet connection.");
            return("");
        }
    }
    
    
    public static String getThisVersion() {
        getRuntimeDetails();
        return(VERSION+"-"+BUILD);
    }
    
    public static boolean versionUpdateAvailable() {
        getRuntimeDetails();
        String currentVersion = getCurrentVersion();
        if (currentVersion.equals("")) return(false);
        if (currentVersion.equals("-")) return(false);
        if (!currentVersion.equals(getThisVersion())) return(true);
        else return(false);
    }
    
    public static String downloadNewVersion(String version,String path) {
        getRuntimeDetails();
        if (!download("http://www.athsvic.org.au/pub/software/OpenScore/downloadUpdate.php"
                        +"?app=OpenScore&releasetype="+RELEASETYPE+"&platform="+PLATFORM+"&arch="+ARCH+"&version="+version,
                        path+File.separator+"OpenScore-"+RELEASETYPE+"-"+PLATFORM+"-"+ARCH+"-"+(version.replace('.','_'))+".zip")) {
            ErrorHandler.userError("Could not access the website.  Please check your internet connection.");
        }
        return("OpenScore-"+RELEASETYPE+"-"+PLATFORM+"-"+ARCH+"-"+(version.replace('.','_'))+".zip");                        
    }


    public static void update(String version) {
        getRuntimeDetails();
        try {
            URL mainJarFile = SoftwareUpdater.class.getProtectionDomain().getCodeSource().getLocation();
            URL mainJarDir = new URL(mainJarFile,"./");
            if (mainJarFile != null) {
                ErrorHandler.info("Main Jar Dir: "+mainJarDir.toString());
                String filename = downloadNewVersion(version,mainJarDir.getPath());
                if (unzip(new File(mainJarDir.getPath()+File.separator+filename))) {
                    isUpdating = true;    
                    /** TODO: Close subapplication here. **/
                } else 
                    ErrorHandler.userWarn("There was an error unzipping the upgraded software.  Please try again later.");
            } else
                ErrorHandler.error("Couldn't locate main jar file!");
        } catch (java.net.MalformedURLException e) {
            ErrorHandler.error("Malformed URL:"+e.toString());
        }
    }

    
    /** Download the file located at address and save it as localFileName */
  	public static boolean download(String address, String localFileName) {
		OutputStream out = null;
		URLConnection conn = null;
		InputStream  in = null;
		try {
			URL url = new URL(address);
			
			out = new BufferedOutputStream(
				new FileOutputStream(localFileName));
			conn = url.openConnection();
			
			// Check for content length.
            int contentLength = conn.getContentLength();
            
			//Tell the listeners we're about to start, and the size of the file we're going to download
			for (FileDownloadListener listener : listeners) {
			     listener.downloadStart(contentLength);
			}            
            
			in = conn.getInputStream();
			byte[] buffer = new byte[1024];
			int numRead;
			long numWritten = 0;
			while ((numRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, numRead);
				numWritten += numRead;

    			//Tell the listeners we got some data
    			for (FileDownloadListener listener : listeners) {
    			     listener.downloadUpdate(numWritten);
    			}
			}
			
			//Tell the listeners we've finished
			for (FileDownloadListener listener : listeners) {
			     listener.downloadFinish();
			}

			System.out.println(localFileName + "\t" + numWritten);
		} catch (UnknownHostException uhe) {
            return(false);
		} catch (Exception exception) {
			exception.printStackTrace();
            return(false);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (IOException ioe) {
                return(false);
			}
		}
        return(true);
	}
	
	
	public static final void copyInputStream(InputStream in, OutputStream out)
        throws IOException
    {
        byte[] buffer = new byte[1024];
        int len;

        while((len = in.read(buffer)) >= 0)
          out.write(buffer, 0, len);

        in.close();
        out.close();
    }

	public static boolean unzip(File file) {	    
        Enumeration<? extends ZipEntry> entries;
        ZipFile zipFile;
        String path = file.getParentFile().getPath();

        try {
          zipFile = new ZipFile(file);

          entries = zipFile.entries();

          while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();

            if(entry.isDirectory()) {
              // Assume directories are stored parents first then children.
              ErrorHandler.info("Extracting directory: " + path + File.separator + entry.getName());
              // This is not robust, just for demonstration purposes.
              (new File(path + File.separator + entry.getName())).mkdir();
              continue;
            }

            File targetfile = new File(path + File.separator + entry.getName());
            if (targetfile.exists()) {
                ErrorHandler.info("Deleting file: " + path + File.separator + entry.getName());
                targetfile.delete();
            }
            ErrorHandler.info("Extracting file: " + path + File.separator + entry.getName());
            copyInputStream(zipFile.getInputStream(entry),
               new BufferedOutputStream(new FileOutputStream(path + File.separator + entry.getName())));
          }

          zipFile.close();
        } catch (ZipException ioe) { //There was a problem with unzipping the file.
          return(false);
        } catch (IOException ioe) {
          ErrorHandler.error("Unhandled exception:");
          ioe.printStackTrace();
          return(false);
        }
        return(true);
    }

    /** Called by BaseFrame to indicate if it's restarting.*/ 
    public static boolean isUpdating() {
        return(isUpdating);
    }
}

