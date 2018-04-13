package com.aesthetic.net;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

public class ConvolutionalNetManager extends SwingWorker<Void, String> {

	private JTextArea JDP;
	private JLabel jlabel;
	private JLabel additional;
	private int batchsize = 15;
	private int cnn_min = 1;
	private int cnn_max = 6;
	private String train_path;
	private String test_path;
	private String model_path;
	private NetworkType nettype;
	private boolean showinb;
	final static String newline = "\n";
	private int epochs;
	private List<Double> accuracies;
	private double bestaccuracy;
	@Override
	protected Void doInBackground() throws Exception {
		accuracies = new ArrayList<Double>();
		for(int i = cnn_min; cnn_min<= cnn_max;i++)
		{
			ThreadNet convnet = new ThreadNet(train_path,test_path,model_path,nettype,showinb,i,batchsize,epochs);
			publish("STARTING");
			Thread t = new Thread(convnet);
			t.start();
			publish("Waiting for Results");
			t.join();
			accuracies.add(convnet.getAccuracy());
			publish("ACCURACY " + Double.toString(convnet.getAccuracy()));
			publish(convnet.getConfusionmatrix());
			
			if(bestaccuracy < convnet.getAccuracy())
			{
				bestaccuracy = convnet.getAccuracy();
				jlabel.setText(Double.toString(bestaccuracy));
				additional.setText(Integer.toString(i));
			}
			
			convnet = null;
			publish("RESTING A BIT....");
			Thread.sleep(5000);
		}
		
		
		return null;
	}






	public ConvolutionalNetManager(JTextArea jDP, JLabel jlabel, int batchsize, int cnn_min, int cnn_max,
			String train_path, String test_path, String model_path, NetworkType nettype, boolean showinb, int epochs,JLabel add) {
		super();
		JDP = jDP;
		this.jlabel = jlabel;
		this.batchsize = batchsize;
		this.cnn_min = cnn_min;
		this.cnn_max = cnn_max;
		this.train_path = train_path;
		this.test_path = test_path;
		this.model_path = model_path;
		this.nettype = nettype;
		this.showinb = showinb;
		this.epochs = epochs;
		this.additional = add;
	}






	@Override
	protected void process(List<String> chunks) {

		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());

		for (String text : chunks) {

			try {
				JDP.append(timeStamp + ": " + text);
				JDP.append(newline);
				//jlabel.setText(Double.toString(accuracy));
			} catch (Exception e) {

				e.printStackTrace();
			}

		}
}
	
}
