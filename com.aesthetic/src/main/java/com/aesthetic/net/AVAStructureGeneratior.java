package com.aesthetic.net;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;

import org.apache.commons.codec.binary.Base64;

import com.aesthetic.main.AVAHelper;
import com.aesthetic.main.DBHelper;

public class AVAStructureGeneratior {
	
	public static void OrganizeAva(String path) throws Exception
	{
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        
		Map<String, String> map = new HashMap<String, String>();
		map.put("0", "");
		br = new BufferedReader(new FileReader(path + "\\tags.txt"));
		String line;
		while ((line = br.readLine()) != null) {
			String[] parts = line.split(" ");
			
			String  id = parts[0];
			String tag = parts[1];
			
			map.put(id,tag);
		}
		
		br.close();
		
		
		
		
		
		
		
		

        br = new BufferedReader(new FileReader(path + "\\AVA.txt"));
		
		int counter = 0;
        while ((line = br.readLine()) != null) {

        	
        	counter++;
        	
        	if(counter >= 0)
        		break;
        	
        	String[] parts = line.split(" ");
        	//String part1 = parts[0]; // 004
        	String photoid = parts[1]; // 034556
        	
        	double summe = 0;
        	
        	for(int i = 2;i < 12; i++)
        	{
        	summe = summe + Integer.parseInt(parts[i]);
        	}
        	double val = 0;
        	for(int i = 2;i < 12; i++)        	{
        	 val = val + ((i-1)* Integer.parseInt(parts[i])/summe);
        	}
        	//tags ermitteln
        	
        	String tag1 = parts[12];
        	String tag2 = parts[13];
        	
        		String tags = map.get(tag1) + ";" + map.get(tag2);

        	
        	
        	
        	try
        	{
        	DBHelper.insertIntoAva(photoid, val,tags);
        	}
        	catch(Exception e)
        	{
        		System.out.println(e.getMessage());
        	}
        	
            }
        
        	FolderSystemTwoSplit(path);
	}


	public static void FolderSystemTwoSplit(String path) throws Exception
	{
		
		path = path + System.getProperty("file.separator") + "images" + System.getProperty("file.separator") + "images";
		
		/*JFileChooser chooser = new JFileChooser(); 
	    chooser.setCurrentDirectory(new java.io.File("."));
	    chooser.setDialogTitle("Chose Image Location");
	    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    //
	    // disable the "All files" option.
	    //
	    chooser.setAcceptAllFileFilterUsed(false);
	    //    
	    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) { 
	      
	    	path = chooser.getSelectedFile().getPath();
	    	
	      }
	    else {
	      return;
	      }	
		
		*/
		
		
		List<AVAHelper> all = DBHelper.Load_AVA();
		String foldername = "";
		for(AVAHelper av : all)
		{
			if(av.getRating() >= 5)
			{
				foldername = "schön";
			}
			else
			{
				foldername = "nicht schön";
			}
			
			String tmp_path = path + System.getProperty("file.separator") + foldername;
			new File(tmp_path).mkdirs();
			
			//check if file exists
			File f = new File(path + System.getProperty("file.separator") + av.getId() + ".jpg");		
			f.renameTo(new File(path + System.getProperty("file.separator") + foldername + System.getProperty("file.separator") +  av.getId() + ".jpg"));
		}
		
		
		
		
		
	}

}
