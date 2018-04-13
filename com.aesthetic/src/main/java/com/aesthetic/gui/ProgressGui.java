package com.aesthetic.gui;

import javax.swing.JFrame;

import java.awt.Dialog;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

public class ProgressGui extends JDialog {
	
	
	private JTextArea textArea;
	
	public JTextArea getTextArea() {
		return textArea;
	}

	public void setTextArea(JTextArea textArea) {
		this.textArea = textArea;
	}


	public JLabel getLblBestAccuracy() {
		return lblBestAccuracy;
	}

	public void setLblBestAccuracy(JLabel lblBestAccuracy) {
		this.lblBestAccuracy = lblBestAccuracy;
	}

	private JLabel lblBestAccuracy;
	private JLabel lblWert;
	private JLabel lblOfAdditional;
	private JLabel label_numberLayer;
	public ProgressGui(Gui gui) {
		
		super(gui);
		this.setModalityType(ModalityType.MODELESS);
		 getContentPane().setLayout(null);
		//editorPane = new JEditorPane();
		//editorPane.setEditable(false);
		//editorPane.setBounds(10, 39, 414, 399);
		//getContentPane().add(editorPane);
		
		 lblBestAccuracy = new JLabel("Best Accuracy: ");
		 lblBestAccuracy.setBounds(10, 11, 75, 14);
		getContentPane().add(lblBestAccuracy);
		
		 lblWert = new JLabel("WERT");
		 lblWert.setBounds(95, 11, 75, 14);
		getContentPane().add(lblWert);
		
		 textArea = new JTextArea();
		 textArea.setBounds(10, 47, 520, 446);
		 getContentPane().add(textArea);
		 
		 JScrollPane scrollPane = new JScrollPane(textArea);
		 scrollPane.setBounds(10, 47, 520, 446);
		 getContentPane().add(scrollPane);
		 
		 lblOfAdditional = new JLabel("# of additional layer:");
		 lblOfAdditional.setBounds(202, 11, 107, 14);
		 getContentPane().add(lblOfAdditional);
		 
		 label_numberLayer = new JLabel("0");
		 label_numberLayer.setBounds(319, 11, 75, 14);
		 getContentPane().add(label_numberLayer);
		 
		 
		 
		 
		
		
		
		
	}

	public JLabel getLabel_numberLayer() {
		return label_numberLayer;
	}

	public void setLabel_numberLayer(JLabel label_numberLayer) {
		this.label_numberLayer = label_numberLayer;
	}

	public JLabel getLblWert() {
		return lblWert;
	}

	public void setLblWert(JLabel lblWert) {
		this.lblWert = lblWert;
	}
}
