package com.AL5.IssueParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Utilities {

	public static HashMap<String,String> getProjectMap(){
		HashMap<String,String> projectMap = new HashMap<String,String>();
		Properties properties = new Properties();
		JSONParser jsonParser = new JSONParser();
		InputStream input = null;
		Object obj = null;
		try{
			input = new FileInputStream("lib"+File.separator+"linux_directories.properties");
			properties.load(input);
			obj = jsonParser.parse(new FileReader(properties.getProperty("PATH_FOR_PROJECT_NAME_ID_OBJECT")));
			JSONObject jsonObject = (JSONObject) obj;
			JSONArray ids = (JSONArray) jsonObject.get("Ids");
			JSONArray names = (JSONArray) jsonObject.get("Names");
			for(int index = 0; index < ids.size();index++){
				String id = ids.get(index).toString();
				String name = names.get(index).toString();
				projectMap.put(name, id);
			}
		}
		catch(Exception e){
			System.out.println("Exception in creating map");
		}
		return projectMap;
	}
	/**
	   * Unpack an archive from a URL
	   * TODO: May need this code in future
	   * @param url
	   * @param targetDir
	   * @return the file to the url
	   * @throws IOException
	   *//*
	 public static void unpackArchive(URL url, File targetDir) throws IOException {
	      if (!targetDir.exists()) {
	          targetDir.mkdirs();
	      }
	      InputStream in = new BufferedInputStream(url.openStream(), 1024);
	      // make sure we get the actual file
	      File zip = File.createTempFile("arc", ".tar", targetDir);
	      OutputStream out = new BufferedOutputStream(new FileOutputStream(zip));
	      copyInputStream(in, out);
	      out.close();
	      //return unpackArchive(zip, targetDir);
	  }
	  *//**
	   * Unpack a zip file
	   * TODO: May need this code in future
	   * @param theFile
	   * @param targetDir
	   * @return the file
	   * @throws IOException
	   *//*
	  public static File unpackArchive(File theFile, File targetDir) throws IOException {
	      if (!theFile.exists()) {
	          throw new IOException(theFile.getAbsolutePath() + " does not exist");
	      }
	      if (!buildDirectory(targetDir)) {
	          throw new IOException("Could not create directory: " + targetDir);
	      }
	      ZipFile zipFile = new ZipFile(theFile);
	      for (Enumeration entries = zipFile.entries(); entries.hasMoreElements();) {
	          ZipEntry entry = (ZipEntry) entries.nextElement();
	          File file = new File(targetDir, File.separator + entry.getName());
	          if (!buildDirectory(file.getParentFile())) {
	              throw new IOException("Could not create directory: " + file.getParentFile());
	          }
	          if (!entry.isDirectory()) {
	              copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(file)));
	          } else {
	              if (!buildDirectory(file)) {
	                  throw new IOException("Could not create directory: " + file);
	              }
	          }
	      }
	      zipFile.close();
	      return theFile;
	  }

	  public static void copyInputStream(InputStream in, OutputStream out) throws IOException {
	      byte[] buffer = new byte[1024];
	      int len = in.read(buffer);
	      while (len >= 0) {
	          out.write(buffer, 0, len);
	          len = in.read(buffer);
	      }
	      in.close();
	      out.close();
	  }

	  public static boolean buildDirectory(File file) {
	      return file.exists() || file.mkdirs();
	  }*/
}
