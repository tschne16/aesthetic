package com.aesthetic.main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.imgscalr.Scalr;

public class CreateImage {

	static final String[] EXTENSIONS = new String[]{
	        "gif", "png", "bmp","jpg" // and other formats you need
	    };
	static final FilenameFilter IMAGE_FILTER = new FilenameFilter() {

        @Override
        public boolean accept(final File dir, final String name) {
            for (final String ext : EXTENSIONS) {
                if (name.endsWith("." + ext)) {
                    return (true);
                }
            }
            return (false);
        }
    };
    
	public static void main(String[] args)
	{
		
		Random rand = new Random();
		
		
	for(int x=0;x<2;x++)	
	{
		String foldername = "Line";
	
		if(x >0)
		{
			foldername = "No Line";
		}
		
		
		for(int i = 1;i<=300;i++)
		{
		BufferedImage xyz = new BufferedImage(224,224,BufferedImage.TYPE_INT_RGB);
		Graphics2D graphic = xyz.createGraphics();
		
		float r = rand.nextFloat();
		float g = rand.nextFloat();
		float b = rand.nextFloat();
		Color randomColor = new Color(r, g, b);
		if(x== 0)
		{
			randomColor = new Color(r,g,b);
			
		//	r = 0;
		//	g = 0;
			//b = 0;
		}
		else
		{
			randomColor = new Color(r,g,b);
			//r = 254;
			//g = 254;
			//b = 254;
		}
		
		
		graphic.setColor(randomColor);
		graphic.fillRect(0, 0, 224, 224);
		
		if(x==0)
		{
			int randomNum1 = ThreadLocalRandom.current().nextInt(0, 224 + 1);
			int randomNum2 = ThreadLocalRandom.current().nextInt(0, 224 + 1);
			int randomNum3 = ThreadLocalRandom.current().nextInt(0, 224 + 1);
			int randomNum4 = ThreadLocalRandom.current().nextInt(0, 224 + 1);
			
			
			
			graphic.setColor(new Color(255,255,255));
			graphic.setStroke(new BasicStroke(5));
			graphic.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
			graphic.drawLine(0, randomNum2, randomNum3, 30);
		}
		else
		{
			int randomNum1 = ThreadLocalRandom.current().nextInt(0, 30 + 1);
			int randomNum2 = ThreadLocalRandom.current().nextInt(0, 30 + 1);
			graphic.setColor(new Color(255,255,255));
			graphic.setStroke(new BasicStroke(5));
			graphic.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
			graphic.fillOval(randomNum1, randomNum2, 3, 4);
	
		}
		
		graphic.drawImage(xyz,0,0,224,224,null);
		String path = "C:\\Users\\Torben\\Desktop\\GoogleData\\test data\\"+ foldername;
		new File(path).mkdirs();
		try{ImageIO.write(xyz,"jpg",new File(path +"\\test" + i +".jpg"));}catch (Exception e) {}
		
		graphic.dispose();
		
		}
		
	}
	
	
	}
	
	
	public static void cropImages(String originalpath,String targetpath,int height,int width)
	{

		File origin = new File(originalpath);
		
		File target = new File(targetpath);
		target.mkdirs();
		///Für jeden Ordner im Verzeichnis im Zielverzeichnis einen Ordner anlegen
		String[] directories = origin.list(new FilenameFilter() {
			  @Override
			  public boolean accept(File current, String name) {
			    return new File(current, name).isDirectory();
			  }
			});
			
		if(directories.length > 0)
		{
			for(String s : directories)
			{
			String orgpath = originalpath + "\\" + s;
			String tarpath = targetpath + "\\" + s;
			cropImages(orgpath, tarpath, height, width);
			}
		}
		else
		{
			for (final File f : origin.listFiles(IMAGE_FILTER)) {
                BufferedImage img = null;
                
                try {
 
                    img = ImageIO.read(f);
                    BufferedImage result = Scalr.crop(img,      	    (img.getWidth() - width) / 2, (img.getHeight() - height) / 2,
                    	    width, height);
                    String name = f.getName();
                    
                    File outputfile = new File(targetpath + "\\" + name);
                    ImageIO.write(result, FilenameUtils.getExtension(f.getName()), outputfile);
                    
                    img.flush();
                    img = null;
                    result.flush();
                    result  = null;
                    
                    
                }
                catch(Exception e)
                {
                	System.out.println(e.getMessage());
                }
		}
		
		
		
		
		
	}
	
}
	}
	
