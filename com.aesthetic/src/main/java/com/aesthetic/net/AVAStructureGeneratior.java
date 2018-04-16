package com.aesthetic.net;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
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

	public static void OrganizeAva(String inputpath, String outputpath) throws Exception {
		try
		{
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;

		Map<String, String> map = new HashMap<String, String>();
		map.put("0", "");
		br = new BufferedReader(new FileReader(inputpath + "\\tags.txt"));
		String line;
		while ((line = br.readLine()) != null) {
			String[] parts = line.split(" ");

			String id = parts[0];
			String tag = parts[1];

			map.put(id, tag);
		}

		br.close();

		br = new BufferedReader(new FileReader(inputpath + "\\AVA.txt"));

		int counter = 0;

		while ((line = br.readLine()) != null) {

			if (counter == 0)
				break;

			counter++;

			if (counter > 70000) {

				String[] parts = line.split(" ");
				// String part1 = parts[0]; // 004
				String photoid = parts[1]; // 034556

				double summe = 0;

				for (int i = 2; i < 12; i++) {
					summe = summe + Integer.parseInt(parts[i]);
				}
				double val = 0;
				for (int i = 2; i < 12; i++) {
					val = val + ((i - 1) * Integer.parseInt(parts[i]) / summe);
				}
				// tags ermitteln

				String tag1 = parts[12];
				String tag2 = parts[13];

				String tags = map.get(tag1) + ";" + map.get(tag2);

				try {
					DBHelper.insertIntoAva(photoid, val, tags);
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}

			}
		}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		//FolderSystemTwoSplit(inputpath, outputpath);
		copy(inputpath,outputpath);
	}

	
	
	public static void copy(String inputpath, String outputpath) throws Exception
	{
		//LOAD FROM FILESYSTEM
		
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream("AVA_SET.csv");
		InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
		BufferedReader reader = new BufferedReader(streamReader);
		List<AVAHelper> all = new ArrayList<AVAHelper>();
		AVAHelper av = null;
		
		int count = 0;
		
		int zaehler = 0;
		
		for(String line; (line = reader.readLine()) != null;) 
			{
				
				
				zaehler = zaehler +1;
			}
		
		reader = new BufferedReader(streamReader);
		double counter = zaehler*0.8;
		
		String set = "TRAIN DATA";
		for (String line; (line = reader.readLine()) != null;) {
			
			if (count == 0)
			{
				count++;
				continue;
			}
				
			
			String[] bla = line.split(";");
			
			av = new AVAHelper();
			av.setId(Long.parseLong(bla[0]));
			av.setRating(Double.parseDouble(bla[1]));
			
			all.add(av);
			count++;
			
			
			String foldername = "HIGH";	
			if(av.getRating() <= 5)
				foldername = "LOW";
			else
			{
				if(av.getRating() < 5.8)
				{
					continue;
				}
			}
			
			if(counter <= count)
			{
				set = "TEST DATA";
			}
			
			String ip = inputpath + System.getProperty("file.separator") + av.getId() + ".jpg";
			
			File f = new File(ip);
			
			if(f.exists())
			{
			String out = outputpath + System.getProperty("file.separator") + set +System.getProperty("file.separator") + foldername;
			File output = new File(out);	
			output.mkdirs();
			
			output = new File(out +System.getProperty("file.separator") + av.getId() + ".jpg");
			Files.copy(f, output);
			
			}
			
			
			
		}
		
		
		//List<AVAHelper> all = DBHelper.Load_AVA();
	/*	double counter = all.size()*0.8;
		String set = "TRAIN DATA";
		for(int i = 0; i < all.size();i++)
		{
			 av = all.get(i);
			
			String foldername = "HIGH";	
			if(av.getRating() <= 5)
				foldername = "LOW";
			else
			{
				if(av.getRating() < 5.8)
				{
					continue;
				}
			}
			
			if(counter <= i)
			{
				set = "TEST DATA";
			}
			
			String ip = inputpath + System.getProperty("file.separator") + av.getId() + ".jpg";
			
			File f = new File(ip);
			
			if(f.exists())
			{
			String out = outputpath + System.getProperty("file.separator") + set +System.getProperty("file.separator") + foldername;
			File output = new File(out);	
			output.mkdirs();
			
			output = new File(out +System.getProperty("file.separator") + av.getId() + ".jpg");
			Files.copy(f, output);
			
			}
			
		}*/
		
	}
	
	
	public static void FolderSystemTwoSplit(String path, String output) throws Exception {
		int beautifuL_counter=0;
		int not_beautiful_counter=0;
		int discarded_counter=0;
		
		
		String org_path = path + System.getProperty("file.separator") + "images" + System.getProperty("file.separator")
				+ "images";
		// path = path + System.getProperty("file.separator") + "images" +
		// System.getProperty("file.separator") + "images" +
		// System.getProperty("file.separator") + "Datensatz RAPID" ;
		path = output;

		Random rng = new Random();

		List<AVAHelper> all = DBHelper.Load_AVA();
		String foldername = "";
		double counter = (double) all.size() * 0.8;
		double current_counter = 0;
		for (AVAHelper av : all) {
			current_counter++;
			if (av.getRating() >= 5 + 1) {
				foldername = "beautiful";
				beautifuL_counter++;
			} else if (av.getRating() <= 4) {
				not_beautiful_counter++;
				foldername = "not beautiful";
			} else {
				discarded_counter++;
				continue;
			}

			// TEST TRAINING DATA SPLIT BEI 80:20
			String tmp_path;
			if (current_counter > counter) {
				tmp_path = path + System.getProperty("file.separator") + "test data";
			} else {
				tmp_path = path + System.getProperty("file.separator") + "train data";
			}

			tmp_path = tmp_path + System.getProperty("file.separator") + foldername;
			new File(tmp_path).mkdirs();

			// check if file exists
			File f = new File(org_path + System.getProperty("file.separator") + av.getId() + ".jpg");

			if (f.exists()) {

				// File image = new File("C:\\Users\\Public\\Pictures\\Sample
				// Pictures\\mypicture.jpg")
				BufferedImage img = ImageIO.read(f);
				// BufferedImage thumbnail = Scalr.resize(img,Scalr.Mode.FIT_EXACT,224,224);
				// BufferedImage thumb = Scalr.cro
				int y = (img.getHeight() - 224) / 2;

				if (y < 0)
					y = img.getHeight();

				int x = (img.getWidth() - 224) / 2;

				if (x < 0) {
					x = 0;
				}

				try {
				BufferedImage thumbnail = Scalr.crop(img, x, y, 224, 224);
				ImageIO.write(thumbnail, "jpg",
						new File(tmp_path + System.getProperty("file.separator") + av.getId() + ".jpg"));
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				
				
				// Files.copy(f, new File(tmp_path + System.getProperty("file.separator") +
				// av.getId() + ".jpg"));

			}
			// f.renameTo(new File(path + System.getProperty("file.separator") + foldername
			// + System.getProperty("file.separator") + av.getId() + ".jpg"));
		}

	}

}
