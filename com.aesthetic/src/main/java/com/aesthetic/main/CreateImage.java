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
		for(int i = 1;i<=500;i++)
		{
		BufferedImage xyz = new BufferedImage(224,224,BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphic = xyz.createGraphics();
		
		float r = rand.nextFloat();
		float g = rand.nextFloat();
		float b = rand.nextFloat();
		
		
		Color randomColor = new Color(r, g, b);
		graphic.setColor(randomColor);
		graphic.fillRect(0, 0, 224, 224);
		graphic.drawImage(xyz,0,0,224,224,null);
		
		try{ImageIO.write(xyz,"jpg",new File("C:\\Users\\Torben\\Desktop\\New Small Dataset\\test data\\not beautiful\\test" + i +".jpg"));}catch (Exception e) {}
		
		graphic.dispose();
		
		}
		
		
	}
	
	
	
	
}
