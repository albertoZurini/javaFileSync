package javaFileSync;

import java.io.File;
import java.io.FileInputStream;
//Confirm window
import javax.swing.JOptionPane;
//Array list
import java.util.*;
//File
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class Interaction
{
	//Confirm window
	public static boolean Confirm(String title, String text)
	{
		return JOptionPane.showConfirmDialog(null, text, title, JOptionPane.YES_NO_OPTION) == 0;
	}
	//Alert window
	public static void Alert(String title, String text)
	{
		JOptionPane.showMessageDialog(null, text, title, JOptionPane.INFORMATION_MESSAGE);
	}
	
	//Load the DB file or create one
	public static Properties LoadOrCreateDB()
	{
		Properties prop = new Properties();
		OutputStream output = null;
		InputStream input = null;
		boolean dbCreated = false;
		String configFile = "javaFileSync.conf";
		try {
			configFile = new File(new File(".").getAbsolutePath()).getCanonicalPath()+"/javaFileSync.conf";
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		while(!dbCreated)
		{
			try {
				//System.out.println("Trying to open properties");
	
				input = new FileInputStream(configFile);
	
				// load a properties file
				prop.load(input);
	
				dbCreated = true;
			} catch (IOException ex) {
				ex.printStackTrace();
				try {
					output = new FileOutputStream(configFile);
	
					// set the properties value
					prop.setProperty("directory1", "/first/directory");
					prop.setProperty("directory2", "/second/directory");
	
					// save properties to project root folder
					prop.store(output, null);
	
				} catch (IOException io) {
					io.printStackTrace();
				} finally {
					if (output != null) {
						try {
							output.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					Alert("DB created successfully", "Edit the config file in order to sync your folders (path: "+configFile+"). Press Ok when done");
	
				}
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return prop;
	}
}