package com.aesthetic.net;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileSystemView;

import com.aesthetic.main.DBHelper;
import com.aesthetic.main.Info;
import org.apache.commons.codec.binary.Base64;

public class StructureGenerator extends SwingWorker<Void, Integer> {
	private static String path;
	private static final Logger LOGGER = Logger.getLogger( DBHelper.class.getName() );
	private static double count_all = 0;
	private static volatile double counter = 0;
	
	
	
	
	@Override
	protected Void doInBackground() throws Exception
	{
		
		count_all = 100000;
		
		///HIer wurde das Base64 noch nicht geladen --> da zu groß
		//Nachträglich als Eager fetch laden in 500ter schritten um DB Last zu reduzieren
		List<Info> allpics = DBHelper.LoadAllPictures(100,null);

		List<Info> eager = new ArrayList<Info>();
		count_all = allpics.size();
		

		LOGGER.info("ANZAHL: " +allpics.size());
		for(int i = 0; i < allpics.size();i++)
		{
			eager.add(allpics.get(i));
			
			if(i%500 == 0)
			{
			Map<Long, String> dic = DBHelper.Eager_load_base64(eager);
			process(dic,eager);
			eager.clear();
			dic = null;
			}
		}
		
		if(eager.size() != 0)
		{
			Map<Long, String> dic = DBHelper.Eager_load_base64(eager);
			process(dic,eager);
			eager.clear();
			dic = null;
			eager = null;
		}
		

		
		return null;
		
	}
	

	
	public static void process(Map<Long, String> dic,List<Info> ids) throws Exception
	{
		String base64 ="";
		double favsperview = 0;
		double threshold = 0.03;
		dic.size();
		
  
		
		
		for(Info inf : ids)
		{

			counter++;
			
			LOGGER.log(Level.INFO,Boolean.toString(dic.containsKey(inf.getPhotoid())));
		
			base64 = dic.get(inf.getPhotoid());
			
			if(base64 == null)
			{
				base64 = DBHelper.GetSingleBase64(inf.getPhotoid());
				
				if(base64 == null || base64.equals(DBHelper.getInvalid_base()))
				{
				continue;
				}
			}
				
			
			
			double res = (double) inf.getFavs()/ (double) inf.getViews();
			
			String parentfolder;
			parentfolder = "Train Data";
			
			if((count_all*0.8) < counter)
			{
				parentfolder = "Test Data";
			}
			
			
			
			String folder;
			
			if(res >= threshold)
			{
				folder = "aesthetic";
			}
			else
			{
				folder = "not aesthetic";
			}
			
			
			
			
			LOGGER.log(Level.INFO, Long.toString(inf.getFavs()));
			LOGGER.log(Level.INFO, Long.toString(inf.getViews()));
			LOGGER.log(Level.INFO, Double.toString(res));
			//favsperview = Math.round(res*2/2.0);
			DecimalFormat df = new DecimalFormat("#.#");
			df.setRoundingMode(RoundingMode.CEILING);
			
			double rounded = Math.round(res * 20.0) / 20.0;
			
			favsperview = Math.round(res);
			//String s_favsperview = df.format(res).replace('.', ',');
			String s_favsperview = Double.toString(rounded).replace('.', ',');
			//String tmp_path = path + System.getProperty("file.separator") + s_favsperview;
			 
			String tmp_path = path + System.getProperty("file.separator") + parentfolder + System.getProperty("file.separator") + folder;
			
			
			
			if(Base64.isBase64(base64) == true && base64 != "")
			{
				new File(tmp_path).mkdirs();
				String filetype = "jpg";
				
				if(inf.getFormat() != null)
				{
					if(inf.getFormat() != "")
					{
						filetype = inf.getFormat();
					}
					
				}
				
				
				
				Decode64AndWriteToFile(base64, tmp_path, inf.getPhotoid(),filetype);
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		
		
		String csvFile = FileSystemView.getFileSystemView().getDefaultDirectory().getPath() + "\\base64.csv";
		
		//
		   //String csvFile = "/Users/mkyong/csv/country.csv";
	        String line = "";
	        String cvsSplitBy = ";";
	        int counter = 120072;
	        String foldername = "train data";
	        int tmp_zaehler = -1;
	        String output =  FileSystemView.getFileSystemView().getDefaultDirectory().getPath() + "\\DATASET_CSV_FLICKR";
	        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

	            while ((line = br.readLine()) != null) {

	            	if(tmp_zaehler == -1)
	            	{
	            		tmp_zaehler ++;
	            	continue;
	            	}
	            	
	            	tmp_zaehler++;
	            	
	            if((double) counter*0.8< (double)tmp_zaehler)
	            {
	            	foldername = "test data";
	            }
	            else
	            {
	            	foldername = "train data";
	            }
	            	
	            
	            	
	                // use comma as separator
	                String[] attributes = line.split(cvsSplitBy);
	                
	                String val = attributes[1].replace(',', '.');
	                double res = Double.parseDouble(val);
	                String subfolder = "not aesthetic";
	                if(res > 0.03)
	                {
	                	
	                	subfolder = "aesthetic";
	                	
	                }
	                Info inf =  new Info();
	                
	              inf.setBase64(attributes[0]);
	              
	            
	              
	              new File(output + "\\" + foldername + "\\" + subfolder ).mkdirs();
	              
	              
	              String tmp_path = output + "\\" + foldername + "\\" + subfolder ;
	              
	              String filetyp = attributes[3];
	              
	              if(filetyp == null)
	            	  filetyp = "jpg";
	              
	              Decode64AndWriteToFile(attributes[0], tmp_path, Long.parseLong(attributes[2]), attributes[3]);
	                
	                //System.out.println("Country [code= " + country[4] + " , name=" + country[5] + "]");

	            }

	        } catch (IOException e) {
	            e.printStackTrace();
	        }

	    
		
		
		
		
	/*	
		
		
		
		
		File output = new File(path);
		output.mkdirs();
		
		String[] tags = new String[] {"dog","hund"};
		
		List<Info> list = DBHelper.LoadAllPictures(0,tags);
		
		
List<Info> eager = new ArrayList<Info>();
		
		for(int i = 0; i < list.size();i++)
		{
			eager.add(list.get(i));
			
			if(i%500 == 0)
			{
			Map<Long, String> dic = DBHelper.Eager_load_base64(eager);
			//process(dic,eager);
			String filetype = "jpg";
			
			
			
			for(int x =0;x < eager.size(); x++)
			{
				if(eager.get(x).getFormat() != null)
				{
					filetype = eager.get(x).getFormat();
				}
				
				Decode64AndWriteToFile(dic.get(eager.get(x).getPhotoid()), path, eager.get(x).getPhotoid(), filetype);
			}
			
			eager.clear();
			dic = null;
			}
		}
		
		if(eager.size() != 0)
		{
			Map<Long, String> dic = DBHelper.Eager_load_base64(eager);
			process(dic,eager);
			eager.clear();
			dic = null;
			eager = null;
		}
		
		
		// TODO Auto-generated method stub
/*		List<Info> allpics = DBHelper.LoadAllPictures(100);
		double accurancy = 0.5;
		 JFileChooser chooser = new JFileChooser();
		    chooser.setCurrentDirectory(new java.io.File("."));
		    chooser.setDialogTitle("Choose Path");
		    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		    chooser.setAcceptAllFileFilterUsed(false);

		    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
		      System.out.println("getCurrentDirectory(): " + chooser.getCurrentDirectory());

		    } else {
		    	return;
		    }
		
		 File path = chooser.getCurrentDirectory();
		 
		 for (Info inf : allpics)
		 {
			 double favsperview = 0;
			 
			 ///Später mit accurancy
			 favsperview = Math.round((inf.getFavs()/inf.getViews())*2/2.0);
			 String tmp_path = path + System.getProperty("file.separator") + favsperview;
			 
			 
			 new File( tmp_path).mkdirs();
			
			 Decode64AndWriteToFile(inf.getBase64(), tmp_path, inf.getPhotoid());
			
		 }*/
		    
	}
	
	public static void Decode64AndWriteToFile(String b64,String tmp_path,long id,String filetype) throws IOException
	{
	

		
		
		int width = 224;
		int height = 224;
		byte[] data = Base64.decodeBase64(b64);
		
		BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));
		
		
		if(image == null|| image.getWidth() < width||image.getHeight()< height)
			return;
		
		//image = Scalr.resize(image,Scalr.Mode.FIT_EXACT, 30,30);
		
		if(image.getWidth()> width && image.getHeight() > height)
		image = Scalr.crop(image, (image.getWidth() - width) / 2, (image.getHeight() - height) / 2,width,height);
		
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write( image, filetype, baos );
		baos.flush();
		byte[] imageInByte = baos.toByteArray();
		baos.close();
		
		LOGGER.log(Level.INFO, tmp_path + System.getProperty("file.separator")+ id+ "." + filetype);
		try (OutputStream stream = new FileOutputStream(tmp_path + System.getProperty("file.separator")+ id + ".jpg")) {
		    stream.write(imageInByte);	
		    
		    
		    stream.close();
		    imageInByte = null;
		}
		catch(Exception e)
		{
			LOGGER.log(Level.INFO, e.getMessage());
		}
		
		
	}
	
	
	
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	
	
}
