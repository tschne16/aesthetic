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
import javax.swing.JPanel;

public class GuiResult extends JFrame {
public HashMap<String,String> all;	
public int counter = 0;
public Iterator it;
JLabel lblPic;
private JLabel lblPicture;
	public GuiResult(HashMap<String,String>  results) throws IOException {
		getContentPane().setLayout(null);
		
		all = results;
		JLabel lblName = new JLabel("Name");
		lblName.setBounds(79, 344, 345, 14);
		getContentPane().add(lblName);
		
		JLabel lblKlasse = new JLabel("Klasse");
		lblKlasse.setBounds(79, 373, 200, 14);
		getContentPane().add(lblKlasse);
		
		
         lblPic = new JLabel("");
        lblPic.setBounds(289, 373, 46, 14);
        getContentPane().add(lblPic);
        ImageIcon image = new ImageIcon(GuiResult.class.getResource("res/check.png"));
        lblPic.setVisible(false);
		lblPic.setIcon(image);
		
		boolean visible = false;
		if(results.size()>1)
		{
			visible  = true;
		}
		
		it   = results.entrySet().iterator();
		  String erg = "";		    

		        Map.Entry pair = (Map.Entry)it.next(); 
		        lblName.setText(pair.getKey().toString());
		        erg = new File(pair.getKey().toString()).getParentFile().getName();
		        
    			if(erg.toUpperCase().equals(pair.getValue().toString().toUpperCase()))
    			{
    				lblPic.setVisible(true);
    			}
    			else
    			{
    				lblPic.setVisible(false);
    			}
    			
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
		        			String erg = new File(pair.getKey().toString()).getParentFile().getName();
		        			lblKlasse.setText(pair.getValue().toString());
		        			  
		        			if(erg.toUpperCase().equals(pair.getValue().toString().toUpperCase()))
		        			{
		        				lblPic.setVisible(true);
		        			}
		        			else
		        			{
		        				lblPic.setVisible(false);
		        			}
		        			
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
		        
		        JLabel lblPrediction = new JLabel("Prediction:");
		        lblPrediction.setBounds(10, 373, 59, 14);
		        getContentPane().add(lblPrediction);
		        
		        JLabel lblPath = new JLabel("Path");
		        lblPath.setBounds(10, 344, 59, 14);
		        getContentPane().add(lblPath);
		        

		      

		        
		      //  erg = erg + (pair.getKey() + " " + pair.getValue() + "\n");
		        //it.remove(); // avoids a ConcurrentModificationException
		    
		
		
		
	}
}
