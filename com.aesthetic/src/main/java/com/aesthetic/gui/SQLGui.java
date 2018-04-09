package com.aesthetic.gui;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.swing.JDialog;
import javax.swing.JTextArea;

public class SQLGui extends JDialog {
	public SQLGui(Gui gui) {
		super(gui);
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().setLayout(new BorderLayout());
		
		JTextArea textArea = new JTextArea();
		textArea.setLineWrap(true);
		
		try {
			ClassLoader classLoader = getClass().getClassLoader();
			File file = new File(classLoader.getResource("flickr_flickr.sql").getFile());
			BufferedReader buffer = new BufferedReader(new FileReader(file));
			
			if(buffer != null)
			{
				   String line;
				    while ((line = buffer.readLine()) != null) {
				        textArea.append(line);
				    }
				
				buffer.close();
				buffer = null;	
			}
			
		}
		catch(Exception e)
		{
			
		}
		textArea.setBounds(10, 11, 403, 239);
		getContentPane().add(textArea,BorderLayout.CENTER);
		this.pack();
	}
}
