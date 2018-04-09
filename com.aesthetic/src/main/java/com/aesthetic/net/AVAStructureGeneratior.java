package com.aesthetic.net;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import org.apache.commons.codec.binary.Base64;
import org.imgscalr.Scalr;

import com.aesthetic.main.AVAHelper;
import com.aesthetic.main.DBHelper;
import com.google.common.io.Files;

public class AVAStructureGeneratior {
	
	public static void OrganizeAva(String inputpath) throws Exception
	{
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        
		Map<String, String> map = new HashMap<String, String>();
		map.put("0", "");
		br = new BufferedReader(new FileReader(inputpath + "\\tags.txt"));
		String line;
		while ((line = br.readLine()) != null) {
			String[] parts = line.split(" ");
			
			String  id = parts[0];
			String tag = parts[1];
			
			map.put(id,tag);
		}
		
		br.close();
		
		
        br = new BufferedReader(new FileReader(inputpath + "\\AVA.txt"));
		
		int counter = 0;
		
		
        while ((line = br.readLine()) != null) {

        	
        	counter++;
        	
        	
        	///TEMPORÄRE SPERRE 
        	if(counter >= 0)
        		break;
        	
        	
        	
        	
        	if(counter > 9998)
        	{
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
        }
        
        	FolderSystemTwoSplit(inputpath);
	}


	public static void FolderSystemTwoSplit(String path) throws Exception
	{
		String org_path = path + System.getProperty("file.separator") + "images" + System.getProperty("file.separator") + "images";
		path = path + System.getProperty("file.separator") + "images" + System.getProperty("file.separator") + "images" + System.getProperty("file.separator") + "Datensatz RAPID" ;
		
		
		Random rng = new Random();

		List<AVAHelper> all = DBHelper.Load_AVA();
		String foldername = "";
		double counter = (double) all.size()*0.8;
		double current_counter = 0;
		for(AVAHelper av : all)
		{
			current_counter++;
			if(av.getRating() >= 5)
			{
				foldername = "beautiful";
			}
			else
			{
				foldername = "not beautiful";
			}
			
			
			//TEST TRAINING DATA SPLIT BEI 80:20
			String tmp_path;
			if(current_counter > counter)
			{
				 tmp_path = path + System.getProperty("file.separator") + "test data";
			}
			else
			{
				 tmp_path = path + System.getProperty("file.separator") + "train data";
			}
			
			
			 tmp_path = tmp_path + System.getProperty("file.separator") + foldername;
			new File(tmp_path).mkdirs();
			
			//check if file exists
			File f = new File(org_path + System.getProperty("file.separator") + av.getId() + ".jpg");
			
			
			
			if(f.exists())
			{
				
				//File image = new File("C:\\Users\\Public\\Pictures\\Sample Pictures\\mypicture.jpg")
				BufferedImage img = ImageIO.read(f);
				BufferedImage thumbnail = Scalr.resize(img,Scalr.Mode.FIT_EXACT,224,224);
				ImageIO.write(thumbnail, "jpg", new File(tmp_path +  System.getProperty("file.separator") + av.getId() + ".jpg") );
			//Files.copy(f, new File(tmp_path +  System.getProperty("file.separator") + av.getId() + ".jpg"));
			
			}
			//f.renameTo(new File(path + System.getProperty("file.separator") + foldername + System.getProperty("file.separator") +  av.getId() + ".jpg"));
		}
		
		
		
		
		
	}

}
