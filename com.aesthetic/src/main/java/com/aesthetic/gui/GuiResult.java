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
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class GuiResult extends JFrame {
public HashMap<String,String> all;	
public int counter = 0;
public Iterator it;
private JLabel lblPicture;
	public GuiResult(HashMap<String,String>  results) throws IOException {
		getContentPane().setLayout(null);
		
		all = results;
		JLabel lblName = new JLabel("Name");
		lblName.setBounds(75, 344, 293, 14);
		getContentPane().add(lblName);
		
		JLabel lblKlasse = new JLabel("Klasse");
		lblKlasse.setBounds(75, 375, 241, 14);
		getContentPane().add(lblKlasse);
		
		boolean visible = true;
		if(results.size()>1)
		{
			visible  = false;
		}
		
		it   = results.entrySet().iterator();
		  String erg = "";		    

		        Map.Entry pair = (Map.Entry)it.next(); 
		        lblName.setText(pair.getKey().toString());
		        BufferedImage myPicture = ImageIO.read(new File(pair.getKey().toString()));
		        lblKlasse.setText(pair.getValue().toString());
		        if(myPicture != null )
		        {
		    		lblPicture = new JLabel(new ImageIcon(myPicture));
		    		lblPicture.setBounds(25, 23, 301, 300);
		    		getContentPane().add(lblPicture);
		        }
		        JButton btnNext = new JButton("next");
		        btnNext.setVisible(visible);
		        btnNext.addActionListener(new ActionListener() {
		        	public void actionPerformed(ActionEvent arg0) {
		        		
		        		if(!it.hasNext())
		        		{
		        			it   = results.entrySet().iterator();
		        			
		     
		        		}
		        			Map.Entry pair = (Map.Entry)it.next();
		        			lblName.setText(pair.getKey().toString());
		        			lblKlasse.setText(pair.getValue().toString());
		        			  try {
		        				  BufferedImage	myPicture = ImageIO.read(new File(pair.getKey().toString()));
		        				  lblPicture.setIcon(new ImageIcon(myPicture));
		        			  } catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		        			 
		        			 
		        			 
		        		
		        		
		        		
		        		
		        		
		        		
		        		
		        	}
		        });
		        btnNext.setBounds(335, 369, 89, 23);
		        getContentPane().add(btnNext);
		      

		        
		      //  erg = erg + (pair.getKey() + " " + pair.getValue() + "\n");
		        //it.remove(); // avoids a ConcurrentModificationException
		    
		
		
		
	}

}
