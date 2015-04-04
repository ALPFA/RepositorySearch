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
		Properties properties = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream("lib//ResourceHelper.properties");
			properties.load(input);
			//TODO: Change path to releases
			final File folder = new File(properties.getProperty("PATH_FOR_TEST"));
			listFilesForFolder(folder,properties);
			System.out.println("Finished");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	// Source: http://stackoverflow.com/a/1846349/2833048
	public static void listFilesForFolder(final File folder,Properties properties) {
	    for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            listFilesForFolder(fileEntry,properties);
	        } 
	        else {
        		String path = fileEntry.getPath();
        		String zipball_URL = getZipBallURL(path);
	            downloadSourceCode(zipball_URL,properties);
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
		String zipBall_URL = null;
		try{
			JSONParser parser = new JSONParser();
	        Object obj = parser.parse(new FileReader(path));
	        JSONObject jsonObject = (JSONObject) obj;
	        //get zip ball URL from this object
	        zipBall_URL = jsonObject.get("zipball_url").toString();
		}
		catch(Exception e){
			System.out.println("Exception "+e);
			System.exit(1);
		}
		return zipBall_URL;
	}
	/**
	 * Download the actual source code to the desired directory
	 * @param zipball_URL
	 */
	private static void downloadSourceCode(String zipball_URL,Properties properties) {
		try{
			URL url = new URL(zipball_URL);
			//TODO: Change to actual download content directory
			String path = properties.getProperty("PATH_FOR_TEST_CODE");
			String[] zip_split = zipball_URL.split("/");
			String fileName = zip_split[zip_split.length-1]+".zip";
			URLConnection connection = url.openConnection();
			InputStream in = connection.getInputStream();
			FileOutputStream fos = new FileOutputStream(new File(path+fileName));
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
			System.out.println("Source code downloaded for "+fileName);
		}
		catch(Exception e){
			System.out.println("Exception in downloadSourceCode for "+zipball_URL+" "+ e);
			System.exit(1);
		}
		
	}

}
 