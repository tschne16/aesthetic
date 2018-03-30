package com.aesthetic.main;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.openblas;
import org.nd4j.linalg.factory.Nd4jBackend;

import com.flickr4java.flickr.photos.Photo;
import com.aesthetic.gui.Gui;

public class Main {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		
		
		Gui g = new Gui();
		g.setDefaultCloseOperation(g.EXIT_ON_CLOSE);

		g.setSize(460,684);
		g.setLocation(50,50);
		g.setVisible(true);
		g.setVisible(true);

        
		
		
	}

}
