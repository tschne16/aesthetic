package com.aesthetic.main;



import javax.swing.UIManager;

import com.aesthetic.gui.Gui;

public class Main {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		Gui g = new Gui();
		g.setDefaultCloseOperation(g.EXIT_ON_CLOSE);

		g.setSize(460,786);
		g.setLocation(50,50);
		g.setVisible(true);
		g.setVisible(true);

        
		
		
	}

}
