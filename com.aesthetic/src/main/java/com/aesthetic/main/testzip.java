package com.aesthetic.main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aesthetic.net.ConvolutionalNeuralNetwork;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

public class testzip {
 
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		ArrayList<String> label = new ArrayList<String>();
		
		label.add("HI");
		label.add("HALLO");
		ConvolutionalNeuralNetwork.addLabelsToZipFolder(label,new File("C:\\Users\\Torben\\Desktop\\Small Dataset\\Models\\modelconfig0.zip"));
	

	}
}
