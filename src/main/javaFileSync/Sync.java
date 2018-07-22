package javaFileSync;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

//last modify
import java.io.*;
import java.nio.charset.Charset;
//copy file
import java.nio.channels.FileChannel;


class Explore{
	public List<String> files;
	public List<String> folders;
	public List<Integer> lastModifyFiles;
	public List<Integer> lastModifyFolders;
	Explore(List<String> folders, List<String> files, List<Integer> lastModifyFolders, List<Integer> lastModifyFiles){
		this.files = files;
		this.folders = folders;
		this.lastModifyFiles = lastModifyFiles;
		this.lastModifyFolders = lastModifyFolders;
	}
}

public class Sync
{
	public static List<File> listf(String directoryName) {
        File directory = new File(directoryName);

        List<File> resultList = new ArrayList<File>();

        // get all the files from a directory
        File[] fList = directory.listFiles();
        resultList.addAll(Arrays.asList(fList));
        for (File file : fList) {
            if (file.isFile()) {
                //System.out.println(file.getAbsolutePath());
            } else if (file.isDirectory()) {
                resultList.addAll(listf(file.getAbsolutePath()));
            }
        }
        //System.out.println(fList);
        return resultList;
    }
	
	private static Integer GetLastModifyTime(File file)
	{
		return new Integer((int) (Math.abs(file.lastModified())/100000));
	}
	public static Explore ExploreDirectory(String directory)
	{
		List<String> files = new ArrayList<String>();
		List<String> folders = new ArrayList<String>();
		List<Integer> lastModifyFiles = new ArrayList<Integer>();
		List<Integer> lastModifyFolders = new ArrayList<Integer>();
		
		List<File> tree = listf(directory);
		for(int i=0;i<tree.size();i++)
		{
			if(tree.get(i).isDirectory()) {
				try {
					folders.add(tree.get(i).getCanonicalPath().replace("\\", "/").replaceAll(directory, ""));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				lastModifyFolders.add(GetLastModifyTime(tree.get(i)));
			} else
				try {
					if(!tree.get(i).getCanonicalPath().equals(new File(directory+"syncDB.json").getCanonicalPath())){
						//Add all except the syncDB.json
						try {
							files.add(tree.get(i).getCanonicalPath().replace("\\", "/").replaceAll(directory, "")); //replace needed for cross platform
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						lastModifyFiles.add(GetLastModifyTime(tree.get(i)));
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
		return new Explore(folders, files, lastModifyFolders, lastModifyFiles);
	}
	
	public static long GetFolderSize(File directory) {
	    long length = 0;
	    for (File file : directory.listFiles()) {
	        if (file.isFile())
	            length += file.length();
	        else
	            length += GetFolderSize(file);
	    }
	    return length;
	}
	
	private static String ReadFile(String fileName) throws IOException {
	    BufferedReader br = new BufferedReader(new FileReader(fileName));
	    try {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line);
	            sb.append("\n");
	            line = br.readLine();
	        }
	        return sb.toString();
	    } finally {
	        br.close();
	    }
	}
	public static Explore ImportTree(String directory)
	{
		List<String> folders = new ArrayList<String>();
		List<String> files = new ArrayList<String>();
		List<Integer> lastModifyFolders = new ArrayList<Integer>();
		List<Integer> lastModifyFiles = new ArrayList<Integer>();
		try {
			String dbFile = ReadFile(directory+"syncDB.json");
			try {
				JSONArray importedjson = new JSONArray(dbFile);
				for(int i=0;i<importedjson.getJSONArray(0).length();i++) {
					folders.add(importedjson.getJSONArray(0).get(i).toString());
				}
				for(int i=0;i<importedjson.getJSONArray(1).length();i++) {
					files.add(importedjson.getJSONArray(1).get(i).toString());
				}
				for(int i=0;i<importedjson.getJSONArray(2).length();i++) {
					lastModifyFolders.add(new Integer((int) importedjson.getJSONArray(2).get(i)));
				}
				for(int i=0;i<importedjson.getJSONArray(3).length();i++) {
					lastModifyFiles.add(new Integer((int) importedjson.getJSONArray(3).get(i)));
				}				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Explore output = new Explore(folders, files, lastModifyFolders, lastModifyFiles);
		return output;
	}
	public static void ExportTree(String directory, Explore directoryTree)
	{
		// Saves the directoryTree into the directory
		FileOutputStream dbFile;
		try {
			dbFile = new FileOutputStream(directory+"syncDB.json");
			String toSave = new JSONArray(new ArrayList(
					Arrays.asList(directoryTree.folders, directoryTree.files, directoryTree.lastModifyFolders, directoryTree.lastModifyFiles))).toString();
			Charset UTF_8 = Charset.forName("UTF-8");
			//System.out.println(toSave);
			
			try {
				dbFile.write(toSave.getBytes(UTF_8));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				dbFile.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*
	 * 	SODBNP    = Stored On DataBase but Not Present
	 *	NPISF     = Not Present In Second Folder
	 *	NOFF      = Newer On First Folder
	 *	NOSF      = Newer On Second Folder
	 */
	public static void SyncFolders(Explore directoryTree1, Explore directoryTree2, String directory1, String directory2, Explore directory2Info)
	{
		for(int i=0;i<directoryTree1.folders.size();i++) {
			String folder = directoryTree1.folders.get(i);
			
			if(!directoryTree2.folders.contains(folder)) {
				// If the folder isn't in the second directory
				if(directory2Info.folders.contains(folder)) {
					// If the folder was stored on the DB, but now it isn't present, I have to check last modify times
					int index = directory2Info.folders.indexOf(folder);
					if(directoryTree1.lastModifyFolders.get(i) > directory2Info.lastModifyFolders.get(index)) {
						// If the folder has last been modified on the directory1,
						// I have to copy it on the second directory
						System.out.println("Creating directory "+directory2+folder);
						new File(directory2+folder).mkdirs();
					} else {
						// Else I have to delete it from directory1 too
						System.out.println("Deleting directory "+directory1+folder);
						new File(directory1+folder).delete();
					}
				} else {
					System.out.println("Creating directory "+directory2+folder);
					new File(directory2+folder).mkdirs();
				}
			}
		}
	}
	private static void CopyFile(File sourceFile, File destFile) throws IOException {
	    if(!destFile.exists()) {
	        destFile.createNewFile();
	    }

	    FileChannel source = null;
	    FileChannel destination = null;

	    try {
	        source = new FileInputStream(sourceFile).getChannel();
	        destination = new FileOutputStream(destFile).getChannel();
	        destination.transferFrom(source, 0, source.size());
	    }
	    finally {
	        if(source != null) {
	            source.close();
	        }
	        if(destination != null) {
	            destination.close();
	        }
	    }
	}
	public static void SyncFiles(Explore directoryTree1, Explore directoryTree2, String directory1, String directory2, Explore directory2Info)
	{
		for(int i=0;i<directoryTree1.files.size();i++) {
			String file = directoryTree1.files.get(i);
			
			if(!directoryTree2.files.contains(file)) {
				// If the file isn't in the second directory
				if(directory2Info.files.contains(file)) {
					// If the file was stored on the DB, but now it isn't present, I have to check last modify times
					int index = directory2Info.files.indexOf(file);
					if( (new File(directoryTree1.files.get(i))).length() != (new File(directory2Info.files.get(index)).length()) ) {
						if(directoryTree1.lastModifyFiles.get(i) > directory2Info.lastModifyFiles.get(index)) {
							// If the file has last been modified on the directory1,
							// I have to copy it on the second directory
							String pathFrom = directory1+file;
							String pathTo = directory2+file;
							System.out.println("[SODBNP] Copying file "+file+" from "+pathFrom+" to "+pathTo);
							try {
								CopyFile(new File(pathFrom), new File(pathTo));
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							//new File(directory2+file).mkdirs();
						} else {
							// Else I have to delete it from directory1 too
							System.out.println("[SODBNP] Deleting file "+directory1+file);
							new File(directory1+file).delete();
						}
					}
				} else {
					// File was not stored on DB
					String pathFrom = directory1+file;
					String pathTo = directory2+file;
					System.out.println("[NPISF] Copying file "+file+" from "+pathFrom+" to "+pathTo);
					try {
						CopyFile(new File(pathFrom), new File(pathTo));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//new File(directory2+file).mkdirs();
				}
			} else {
				// If the file is present in the second directory, checking the file size

				if((new File(directory1+file)).length() != (new File(directory2+file)).length()) {
					// If size is different check last modify dates to copy only the newer file
					int lastModifyTree1 = directoryTree1.lastModifyFiles.get(i);
					int lastModifyTree2 =  directoryTree2.lastModifyFiles.get(directoryTree2.files.indexOf(file));
					if(lastModifyTree1 != lastModifyTree2) {
						if(lastModifyTree1 > lastModifyTree2) {
							String pathFrom = directory1+file;
							String pathTo = directory2+file;
							System.out.println("[NOFF] Copying file "+file+" from "+pathFrom+" to "+pathTo);
							try {
								CopyFile(new File(pathFrom), new File(pathTo));
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else {
							String pathFrom = directory2+file;
							String pathTo = directory1+file;
							System.out.println("[NOSF] Copying file "+file+" from "+pathFrom+" to "+pathTo);
							try {
								CopyFile(new File(pathFrom), new File(pathTo));
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}
	public static void SyncAll(Explore directoryTree1, Explore directoryTree2, String directory1, String directory2)
	{
		Explore directory2Info = ImportTree(directory2);
		SyncFolders(directoryTree1, directoryTree2, directory1, directory2, directory2Info);
		SyncFiles(directoryTree1, directoryTree2, directory1, directory2, directory2Info);
	}
}