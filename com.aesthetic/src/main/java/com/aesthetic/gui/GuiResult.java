package com.aesthetic.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class GuiResult extends JFrame {
public HashMap<String,String> all;	
public int counter = 0;

	public GuiResult(HashMap<String,String>  results) throws IOException {
		getContentPane().setLayout(null);
		
		all = results;
		JLabel lblName = new JLabel("Name");
		lblName.setBounds(75, 344, 293, 14);
		getContentPane().add(lblName);
		
		JLabel lblKlasse = new JLabel("Klasse");
		lblKlasse.setBounds(75, 375, 241, 14);
		getContentPane().add(lblKlasse);
		
		
		if(results.size()>1)
		{
			
		}
		
		  Iterator it = results.entrySet().iterator();
		  String erg = "";		    

		        Map.Entry pair = (Map.Entry)it.next(); 
		        lblName.setText("File");
		        BufferedImage myPicture = ImageIO.read(new File(pair.getKey().toString()));
		        
		        if(myPicture != null )
		        {
		    		JLabel lblPicture = new JLabel(new ImageIcon(myPicture));
		    		lblPicture.setBounds(25, 23, 301, 300);
		    		getContentPane().add(lblPicture);
		        }

		      //  erg = erg + (pair.getKey() + " " + pair.getValue() + "\n");
		        //it.remove(); // avoids a ConcurrentModificationException
		    
		
		
		
	}

}
