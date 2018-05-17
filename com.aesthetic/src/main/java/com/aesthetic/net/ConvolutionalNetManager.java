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
	private int fcc = 0;
	private boolean maxp;
	private String train_path;
	private String test_path;
	private String model_path;
	private NetworkType nettype;
	private boolean showinb;
	final static String newline = "\n";
	private int epochs;
	private List<Double> accuracies;
	private double bestaccuracy;
	private String bestmodel;
	private String bestmodel_path;
	private int bestnumber;

	@Override
	protected Void doInBackground() throws Exception {
		accuracies = new ArrayList<Double>();
		
		//*** UNBEDINGT WIEDER ZURÜCKSETZEN
		int[] tries = new int[] {11,5,8,};
		
		for (int i = cnn_min; i <= cnn_max; i++) {
		//	for(int i : tries){
			for (int x = 0; x <= fcc; x++) {

				Boolean maxpol = true;

				ThreadNet convnet = new ThreadNet(train_path, test_path, model_path, nettype, showinb, i, batchsize,
						epochs, x, maxpol);

				publish("CONFIGURATION : EPOCHS: " + epochs + " Batchsize:" + batchsize);

				publish("STARTING! Using additional layers: " + i);
				publish("USING 1 MORE FULLY CONNECTED LAYER:" + (x == 1));
				publish("USING 1 MAXPOOL LAYER:" + maxpol);
				Thread t = new Thread(convnet);
				t.start();
				publish("Waiting for results....");
				t.join();
				accuracies.add(convnet.getAccuracy());
				publish("ACCURACY " + Double.toString(convnet.getAccuracy()));
				publish(convnet.getConfusionmatrix());

				if (bestaccuracy < convnet.getAccuracy()) {
					bestaccuracy = convnet.getAccuracy();
					jlabel.setText(Double.toString(bestaccuracy));
					additional.setText(Integer.toString(i));
					bestmodel_path = convnet.getTmp_model_path();
					bestmodel = convnet.getName();
					bestnumber = i;
				}

				convnet = null;
				publish("RESTING A BIT....");
				Thread.sleep(5000);

			}
		}

		publish("Best MODEL CAN BE FOUND HERE: " + bestmodel_path);
		publish("Accuracy: " + bestaccuracy);
		publish("Number of Layer: " + bestaccuracy);
		publish("Best Number of additional Layer: " + bestnumber);
		publish("# of Epochs: " + epochs);
		publish("batchsize: " + batchsize);
		publish("Model Name:" + bestmodel);
		return null;
	}

	public ConvolutionalNetManager(JTextArea jDP, JLabel jlabel, int batch, int cnn_min, int cnn_max, String train_path,
			String test_path, String model_path, NetworkType nettype, boolean showinb, int ep, JLabel add) {
		super();
		JDP = jDP;
		this.jlabel = jlabel;
		this.batchsize = batch;
		this.cnn_min = cnn_min;
		this.cnn_max = cnn_max;
		this.train_path = train_path;
		this.test_path = test_path;
		this.model_path = model_path;
		this.nettype = nettype;
		this.showinb = showinb;
		this.epochs = ep;
		this.additional = add;
	}

	@Override
	protected void process(List<String> chunks) {

		String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime());

		for (String text : chunks) {

			try {
				JDP.append(timeStamp + ": " + text);
				JDP.append(newline);
				// jlabel.setText(Double.toString(accuracy));
			} catch (Exception e) {

				e.printStackTrace();
			}

		}
	}

}
