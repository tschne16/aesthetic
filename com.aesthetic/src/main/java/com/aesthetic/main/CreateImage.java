package com.aesthetic.main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;

import javax.imageio.ImageIO;

public class CreateImage {

	
	public static void main(String[] args)
	{
		
		Random rand = new Random();
		
		
	for(int x=0;x<2;x++)	
	{
		String foldername = "black";
	
		if(x >0)
		{
			foldername = "white";
		}
		
		
		for(int i = 1;i<=300;i++)
		{
		BufferedImage xyz = new BufferedImage(30,30,BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphic = xyz.createGraphics();
		
		float r = rand.nextFloat();
		float g = rand.nextFloat();
		float b = rand.nextFloat();
		Color randomColor = new Color(r, g, b);
		if(x== 0)
		{
			randomColor = Color.black;
			
			r = 0;
			g = 0;
			b = 0;
		}
		else
		{
			randomColor = Color.white;
			r = 254;
			g = 254;
			b = 254;
		}
		
		
		graphic.setColor(randomColor);
		graphic.fillRect(0, 0, 30, 30);
		graphic.drawImage(xyz,0,0,30,30,null);
		
		try{ImageIO.write(xyz,"jpg",new File("C:\\Users\\Torben\\Desktop\\Datensatz SchwarzWeis\\test data\\"+ foldername +"\\test" + i +".jpg"));}catch (Exception e) {}
		
		graphic.dispose();
		
		}
		
	}
	
	
	}
	
	
	
	
}
