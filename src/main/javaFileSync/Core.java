package javaFileSync;

//Array list
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.*;
//JSON
import org.json.JSONArray;
import org.json.JSONException; 
//prop
import java.util.Properties;

public class Core
{
	public static boolean DirectoriesDifferent(String directory1, String directory2, Explore directoryTree1, Explore directoryTree2)
	{
		boolean sizeCmp = Sync.GetFolderSize(new File(directory1)) != Sync.GetFolderSize(new File(directory2));
		boolean lenFolders = directoryTree1.folders.size() != directoryTree2.folders.size();
		boolean lenFiles = directoryTree1.files.size() != directoryTree2.files.size();
		boolean mount = new File(directory1).exists() && new File(directory2).exists();
		return mount && (lenFolders || lenFiles || sizeCmp);
	}
	
	public static boolean Sync()
	{
		Properties directories = Interaction.LoadOrCreateDB();
		String directory1 = directories.getProperty("directory1");
		String directory2 = directories.getProperty("directory2");
		
		Explore directoryTree1 = Sync.ExploreDirectory(directory1);
		Explore directoryTree2 = Sync.ExploreDirectory(directory2);
		
		if(DirectoriesDifferent(directory1, directory2, directoryTree1, directoryTree2)) {
			if(Interaction.Confirm("SYNC info", "Device connected. Do you want to sync it?")) {
				if(!new File(directory1+"syncDB.json").exists()) {
					System.out.println("Creating database on "+directory1);
					Sync.ExportTree(directory1, directoryTree1);
				}
				if(!new File(directory2+"syncDB.json").exists()) {
					System.out.println("Creating database on "+directory2);
					Sync.ExportTree(directory2, directoryTree2);
				}
				
				System.out.println("--- First Directory ("+directory1+")");
				Sync.SyncAll(directoryTree1, directoryTree2, directory1, directory2);
				
				System.out.println("--- Second Directory ("+directory2+")");
				Sync.SyncAll(directoryTree2, directoryTree1, directory2, directory1);
				
				System.out.println("SYNC COMPLETED");
				
				System.out.println("Updating databases");				
				directoryTree1 = Sync.ExploreDirectory(directory1);
				directoryTree2 = Sync.ExploreDirectory(directory2);
				Sync.ExportTree(directory1, directoryTree1);
				Sync.ExportTree(directory2, directoryTree2);
				Interaction.Alert("SYNC info", "Sync completed");
			}
		} else {
			System.out.println("Already synced. Sleeping...");
		}
		
		return true;
	}
}