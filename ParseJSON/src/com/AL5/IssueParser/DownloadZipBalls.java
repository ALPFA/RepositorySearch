package com.AL5.IssueParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class DownloadZipBalls {

	public static void main(String[] args) {
		Properties properties_dir = new Properties();
		Properties properties_auth = new Properties();
		InputStream dir_props = null;
		InputStream auth_props = null;
		try {
			dir_props = new FileInputStream("lib"+File.separator+"windows_directories.properties");
			auth_props = new FileInputStream("lib"+File.separator+"authentication.properties");
			properties_dir.load(dir_props);
			properties_auth.load(auth_props);
			//TODO: Change path to releases
			final File folder = new File(properties_dir.getProperty("PATH_FOR_RELEASES"));
			listFilesForFolder(folder,properties_auth,properties_dir);
			System.out.println("Finished");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	// Source: http://stackoverflow.com/a/1846349/2833048
		public static void listFilesForFolder(final File folder,Properties properties_auth,Properties properties_dir) {
		    for (final File fileEntry : folder.listFiles()) {
		        if (fileEntry.isDirectory()) {
		            listFilesForFolder(fileEntry,properties_auth,properties_dir);
		        } 
		        else {
	        		String path = fileEntry.getPath();
	        		String tarball_URL = getZipBallURL(path);
		            downloadSourceCode(tarball_URL,properties_auth,properties_dir);
		        }
		    }
		    System.out.println("-----------------End of project-------------");
		}
		/**
		 * Get the URL to download source code
		 * @param path
		 * @return
		 */
		private static String getZipBallURL(String path) {
			String tarBall_URL = null;
			try{
				JSONParser parser = new JSONParser();
		        Object obj = parser.parse(new FileReader(path));
		        JSONObject jsonObject = (JSONObject) obj;
		        //get tar ball URL from this object
		        tarBall_URL = jsonObject.get("zipball_url").toString();
			}
			catch(Exception e){
				System.out.println("Exception "+e);
				System.exit(1);
			}
			return tarBall_URL;
		}
		/**
		 * Download the actual source code to the desired directory
		 * @param tarball_URL
		 */
		private static void downloadSourceCode(String zipball_URL,Properties properties_auth,Properties properties_dir) {
			try{
				URL url = new URL(zipball_URL+"?client_id="+properties_auth.getProperty("CLIENT_ID")+"&client_secret="+properties_auth.getProperty("CLIENT_SECRET"));
				//TODO: Change to actual download content directory
				String path = properties_dir.getProperty("PATH_FOR_SUBJECTS_SOURCE_CODE");
				String[] tar_split = zipball_URL.split("/");
				String fileName = tar_split[tar_split.length-1]+".zip";
				String projectName = tar_split[tar_split.length-3];
				File file = new File(path+projectName+File.separator);
				if (!file.exists()) {
					file.mkdirs();
				}
				File tar_file = new File(file.getAbsoluteFile()+File.separator+fileName);
				if(!tar_file.exists()){
					URLConnection connection = url.openConnection();
					InputStream in = connection.getInputStream();
					FileOutputStream fos = new FileOutputStream(tar_file);
					byte[] buf = new byte[512];
					while (true) {
					    int len = in.read(buf);
					    if (len == -1) {
					        break;
					    }
					    fos.write(buf, 0, len);
					}
					in.close();
					fos.flush();
					fos.close();
					System.out.println("Source code downloaded for "+projectName+File.separator+fileName);
				}
				else{
					System.out.println("Source code already downloaded for "+projectName+File.separator+fileName);
				}
			}
			catch(Exception e){
				System.out.println("Exception in downloadSourceCode for "+zipball_URL+" "+ e);
			}
			
		}

}
 