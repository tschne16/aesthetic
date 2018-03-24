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
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.SwingWorker;

import com.aesthetic.main.DBHelper;
import com.aesthetic.main.Info;
import org.apache.commons.codec.binary.Base64;

public class StructureGenerator extends SwingWorker<Void, Integer> {
	private static String path;
	private static final Logger LOGGER = Logger.getLogger( DBHelper.class.getName() );

	@Override
	protected Void doInBackground() throws Exception
	{
		
		
		///HIer wurde das Base64 noch nicht geladen --> da zu groß
		//Nachträglich als Eager fetch laden in 500ter schritten um DB Last zu reduzieren
		List<Info> allpics = DBHelper.LoadAllPictures(100);

		List<Info> eager = new ArrayList<Info>();
		
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
	
	public static void process(Map<Long, String> dic,List<Info> ids) throws IOException
	{
		String base64 ="";
		double favsperview = 0;
		
		for(Info inf : ids)
		{
		
			LOGGER.log(Level.INFO,Boolean.toString(dic.containsKey(inf.getPhotoid())));
		
			base64 = dic.get(inf.getPhotoid());
			double res = (double) inf.getFavs()/ (double) inf.getViews();
			
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
			String tmp_path = path + System.getProperty("file.separator") + s_favsperview;
			 
			
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
	

		
		
		
		byte[] data = Base64.decodeBase64(b64);
		
		BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));
		
		image = Scalr.resize(image,Scalr.Mode.FIT_EXACT, 30,30);
		
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write( image, filetype, baos );
		baos.flush();
		byte[] imageInByte = baos.toByteArray();
		baos.close();
		
		LOGGER.log(Level.INFO, tmp_path + System.getProperty("file.separator")+ id+ "." + filetype);
		try (OutputStream stream = new FileOutputStream(tmp_path + System.getProperty("file.separator")+ id + ".jpg")) {
		    stream.write(imageInByte);		
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
