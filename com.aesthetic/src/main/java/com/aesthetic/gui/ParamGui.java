package com.aesthetic.gui;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import akka.Main;

import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ParamGui extends JDialog {
	

	private int cnn_min;
	private int cnn_max;
	private int epochs;
	private int batchsize;
	private boolean success = false;
	public ParamGui(Gui g,int min,int max,int ep,int batch) {
	
		super(g);
		setModal(true);
		getContentPane().setLayout(null);
		
		JSpinner spinner_cnn_min = new JSpinner();
		spinner_cnn_min.setModel(new SpinnerNumberModel(new Integer(min), new Integer(1), null, new Integer(1)));
		spinner_cnn_min.setBounds(83, 38, 54, 20);
		getContentPane().add(spinner_cnn_min);
		
		JSpinner spinner_cnn_max = new JSpinner();
		spinner_cnn_max.setModel(new SpinnerNumberModel(new Integer(max), new Integer(1), null, new Integer(1)));
		spinner_cnn_max.setBounds(165, 38, 54, 20);
		getContentPane().add(spinner_cnn_max);
		
		JSpinner spinner_Epoch = new JSpinner();
		spinner_Epoch.setModel(new SpinnerNumberModel(new Integer(ep), new Integer(1), null, new Integer(1)));
		spinner_Epoch.setBounds(83, 86, 54, 20);
		getContentPane().add(spinner_Epoch);
		
		JLabel lblNewLabel = new JLabel("# Epochs");
		lblNewLabel.setBounds(21, 88, 68, 17);
		getContentPane().add(lblNewLabel);
		
		JLabel lblCnlayer = new JLabel("CN-Layer");
		lblCnlayer.setBounds(21, 41, 46, 14);
		getContentPane().add(lblCnlayer);
		
		JLabel lblMin = new JLabel("min");
		lblMin.setBounds(83, 13, 28, 14);
		getContentPane().add(lblMin);
		
		JLabel lblNewLabel_1 = new JLabel("max");
		lblNewLabel_1.setBounds(181, 13, 46, 14);
		getContentPane().add(lblNewLabel_1);
		
		JLabel lblBatchsize = new JLabel("Batchsize");
		lblBatchsize.setBounds(21, 131, 68, 17);
		getContentPane().add(lblBatchsize);
		
		JSpinner spinner_batchsize = new JSpinner();
		spinner_batchsize.setModel(new SpinnerNumberModel(new Integer(batch), new Integer(1), null, new Integer(1)));
		spinner_batchsize.setBounds(83, 129, 54, 20);
		getContentPane().add(spinner_batchsize);
		
		JButton btnSubmit = new JButton("Submit");
		btnSubmit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				cnn_min = (int) spinner_cnn_min.getValue();
				cnn_max = (int) spinner_cnn_max.getValue();
				epochs = (int) spinner_Epoch.getValue();
				batchsize = (int) spinner_batchsize.getValue();
				success = true;
				
				ParamGui.this.setVisible(false);
			}
		});
		btnSubmit.setBounds(93, 160, 89, 23);
		getContentPane().add(btnSubmit);
	}

	public int getCnn_min() {
		return cnn_min;
	}

	public void setCnn_min(int cnn_min) {
		this.cnn_min = cnn_min;
	}

	public int getCnn_max() {
		return cnn_max;
	}

	public void setCnn_max(int cnn_max) {
		this.cnn_max = cnn_max;
	}

	public int getEpochs() {
		return epochs;
	}

	public void setEpochs(int epochs) {
		this.epochs = epochs;
	}

	public int getBatchsize() {
		return batchsize;
	}

	public void setBatchsize(int batchsize) {
		this.batchsize = batchsize;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}
}
