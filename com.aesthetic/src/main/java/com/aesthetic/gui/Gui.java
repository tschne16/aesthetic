package com.aesthetic.gui;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.aesthetic.main.CreateImage;
import com.aesthetic.main.DBHelper;
import com.aesthetic.main.FlickrCrawler;
import com.aesthetic.net.AVAStructureGeneratior;
import com.aesthetic.net.ConvolutionalNeuralNetwork;
import com.aesthetic.net.NetworkType;
import com.aesthetic.net.StructureGenerator;
import javax.swing.JRadioButton;

public class Gui extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField textField;
	private JTextField textField_1;
	private JPasswordField passwordField;
	private JSpinner spinner_desired;
	public JProgressBar progressBar;
	public FlickrCrawler worker;
	public ConvolutionalNeuralNetwork conv_worker;
	public StructureGenerator struc_worker;
	public JTextArea textAreaParams;
	public JLabel lblNewLabel_existindData;
	public JLabel lblNewLabel_img_abort;
	private JTextField txtDefinePath;
	private JFileChooser chooser;
	private String path;
	private File[] files;
	private String trainingsdata_path = "C:\\Users\\Torben\\Desktop\\New Small Dataset\\train data";
	private String testdata_path = "C:\\Users\\Torben\\Desktop\\New Small Dataset\\test data";
	private String output_path = "C:\\Users\\Torben\\Desktop\\New Small Dataset\\model";
	private String inputpath2 = "";
	private String model_path = "";
	private String try_images = "";
	private JTextField textField_traingingsdata_path;
	private JTextField textField_testsdata_path;
	private JTextField textField_output_path;
	private JTextField textField_model_path;
	private JTextField textField_image_path;
	private JRadioButton rdbtnGoogleNet;
	private JRadioButton rdbtnAlexnet;
	private JRadioButton rdbtnOwn;
	private ButtonGroup btG;
	private ProgressGui pg;
	private SQLGui sqlgui;
	private int epochs = 30;
	private int batchsize = 15;
	private int cnn_max = 5;
	private int cnn_min = 1;
	ParamGui param = null;
	JCheckBox chckbxShowBrowser;
	private JTextField txt2DefineInputPath;
	JRadioButton rdbtnKaoEtAl;
	public Gui() throws Exception {
		getContentPane().setLayout(null);
		
		JLabel lblUrl = new JLabel("DB-URL");
		lblUrl.setBounds(10, 11, 46, 14);
		getContentPane().add(lblUrl);
		
		textField = new JTextField();
		textField.setBounds(57, 8, 154, 17);
		getContentPane().add(textField);
		textField.setColumns(10);
		
		JLabel lblDbuser = new JLabel("DB-User");
		lblDbuser.setBounds(10, 36, 46, 14);
		getContentPane().add(lblDbuser);
		
		textField_1 = new JTextField();
		textField_1.setBounds(57, 30, 154, 20);
		getContentPane().add(textField_1);
		textField_1.setColumns(10);
		
		JLabel lblPasswort = new JLabel("Passwort");
		lblPasswort.setBounds(10, 57, 46, 14);
		getContentPane().add(lblPasswort);
		
		passwordField = new JPasswordField();
		passwordField.setBounds(57, 57, 154, 17);
		getContentPane().add(passwordField);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "1. Download to Database", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel.setBounds(10, 76, 417, 203);
		getContentPane().add(panel);
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Amount of Data in DB");
		lblNewLabel.setBounds(10, 43, 121, 14);
		panel.add(lblNewLabel);
		
		JLabel lblDesiredAmountOf = new JLabel("Desired Amount of Data");
		lblDesiredAmountOf.setBounds(10, 88, 121, 14);
		panel.add(lblDesiredAmountOf);
		
		spinner_desired = new JSpinner();
		spinner_desired.setModel(new SpinnerNumberModel(50000, 50000, 500000, 10000));
		spinner_desired.setBounds(141, 85, 67, 20);
		panel.add(spinner_desired);
		
		long amount = 0;
		try{
		 amount = DBHelper.Get_Amount();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
		 lblNewLabel_existindData = new JLabel(Long.toString(amount));
		lblNewLabel_existindData.setName("");
		lblNewLabel_existindData.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_existindData.setAlignmentY(0.3f);
		lblNewLabel_existindData.setBounds(139, 43, 69, 14);
		panel.add(lblNewLabel_existindData);
		
		JLabel lblMinViews = new JLabel("min. Views");
		lblMinViews.setBounds(10, 131, 121, 14);
		panel.add(lblMinViews);
		
		JSpinner spinner_views = new JSpinner();
		spinner_views.setModel(new SpinnerNumberModel(new Integer(100), new Integer(1), null, new Integer(1)));
		spinner_views.setBounds(141, 128, 67, 21);
		panel.add(spinner_views);
		
		progressBar = new JProgressBar();
		progressBar.setBounds(10, 165, 223, 21);
		progressBar.setStringPainted(true);
		panel.add(progressBar);
		
		
		lblNewLabel_img_abort = new JLabel("");
		lblNewLabel_img_abort.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
			
			worker.cancel(true);
			}
		});
		
		lblNewLabel_img_abort.setBounds(246, 165, 62, 22);
		Image img = new ImageIcon(this.getClass().getResource("/Delete.png")).getImage() ;
		lblNewLabel_img_abort.setIcon(new ImageIcon(img));
		//lblNewLabel_img_abort.setVisible(false);
		panel.add(lblNewLabel_img_abort);
		
		
		
		 textAreaParams = new JTextArea();
		textAreaParams.setBounds(236, 24, 171, 134);
		panel.add(textAreaParams);
		
		JButton btnDownload = new JButton("download");
		btnDownload.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				lblNewLabel_img_abort.setVisible(true);
				worker = new FlickrCrawler(getTags(),(int) spinner_desired.getValue(),100 , progressBar, lblNewLabel_existindData);
				
				//worker.addPropertyChangeListener(new ProgressListener(bar));
			
				try{
				worker.execute();
				}
				catch(java.util.concurrent.CancellationException exception)
				{
					
					return;
					
				}

				
			}
		});
		btnDownload.setBounds(318, 163, 89, 23);
		panel.add(btnDownload);
		
		JLabel lblNewLabel_1 = new JLabel("Tags (one per row)");
		lblNewLabel_1.setBounds(236, 11, 115, 14);
		panel.add(lblNewLabel_1);
		
		
		
		JButton btnCreate = new JButton("Create");
		btnCreate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sqlgui = new SQLGui(Gui.this);
				
				sqlgui.setDefaultCloseOperation(pg.HIDE_ON_CLOSE);
				sqlgui.setSize(400,400);
				sqlgui.setLocation(140,140);
				sqlgui.setVisible(true);
				
				
				
			}
		});
		btnCreate.setBounds(338, 7, 89, 23);
		getContentPane().add(btnCreate);
		
		JCheckBox chckbxStore = new JCheckBox("store");
		chckbxStore.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
			
				//JOptionPane.showMessageDialog(null, "stored");
			}
		});
		chckbxStore.setBounds(217, 32, 51, 23);
		getContentPane().add(chckbxStore);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "2. Prepare for CNN", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_1.setBounds(10, 280, 417, 98);
		getContentPane().add(panel_1);
		
		JButton btnNewButton = new JButton("Extract");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				struc_worker = new StructureGenerator();
				
				if(path != "")
				{
					struc_worker.setPath(path);
					
					try{
						struc_worker.execute();
					}
					catch(Exception excep)
					{
						
					}
					
				}
			
			
			}
		});
		
		txtDefinePath = new JTextField();
		txtDefinePath.setText("define output path");
		txtDefinePath.setColumns(10);
		
		JButton btnNewButton_1 = new JButton("...");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				   int result;
			        
				    chooser = new JFileChooser(); 
				    chooser.setCurrentDirectory(new java.io.File("."));
				    chooser.setDialogTitle("Chose Storage");
				    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				    //
				    // disable the "All files" option.
				    //
				    chooser.setAcceptAllFileFilterUsed(false);
				    //    
				    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) { 
				      
				    	path = chooser.getSelectedFile().getPath();
				    	txtDefinePath.setText(path);
				      }
				    else {
				      path = "";
				      }		
			}
		});
		
		JButton btnAva = new JButton("AVA");
		btnAva.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			
				try {
					try {
					AVAStructureGeneratior.OrganizeAva(path);
} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		txt2DefineInputPath = new JTextField();
		txt2DefineInputPath.setText("define input path");
		txt2DefineInputPath.setColumns(10);
		
		JButton btn_2_inputpath = new JButton("...");
		btn_2_inputpath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
		        
			    chooser = new JFileChooser(); 
			    chooser.setCurrentDirectory(new java.io.File("."));
			    chooser.setDialogTitle("Chose Storage");
			    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			    //
			    // disable the "All files" option.
			    //
			    chooser.setAcceptAllFileFilterUsed(false);
			    //    
			    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) { 
			      
			    	inputpath2 = chooser.getSelectedFile().getPath();
			    	txt2DefineInputPath.setText(inputpath2);
			      }
			    else {
			      inputpath2 = "";
			      }	
			}
		});
		
		JButton btnCrop = new JButton("Crop");
		btnCrop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				
				if(new File(path).exists() && new File(inputpath2).exists())
				{
				CreateImage.cropImages(inputpath2,path, 100, 100);
				JOptionPane.showMessageDialog(null, "Done!");
				}
				else
				{
					JOptionPane.showMessageDialog(null, "Please define all Paths!");
					return;
				}
				
			}
		});
		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
		gl_panel_1.setHorizontalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel_1.createSequentialGroup()
							.addComponent(txtDefinePath, GroupLayout.PREFERRED_SIZE, 221, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnNewButton_1, GroupLayout.PREFERRED_SIZE, 58, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_panel_1.createSequentialGroup()
							.addComponent(txt2DefineInputPath, GroupLayout.PREFERRED_SIZE, 221, GroupLayout.PREFERRED_SIZE)
							.addGap(6)
							.addComponent(btn_2_inputpath, GroupLayout.PREFERRED_SIZE, 58, GroupLayout.PREFERRED_SIZE)))
					.addPreferredGap(ComponentPlacement.RELATED, 33, Short.MAX_VALUE)
					.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
						.addComponent(btnCrop, GroupLayout.PREFERRED_SIZE, 62, GroupLayout.PREFERRED_SIZE)
						.addGroup(Alignment.TRAILING, gl_panel_1.createParallelGroup(Alignment.LEADING)
							.addComponent(btnAva)
							.addComponent(btnNewButton)))
					.addContainerGap())
		);
		gl_panel_1.setVerticalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel_1.createSequentialGroup()
							.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
								.addComponent(txtDefinePath, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(btnNewButton_1))
							.addPreferredGap(ComponentPlacement.UNRELATED))
						.addGroup(gl_panel_1.createSequentialGroup()
							.addComponent(btnNewButton)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnAva)
							.addPreferredGap(ComponentPlacement.RELATED)))
					.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel_1.createSequentialGroup()
							.addGap(1)
							.addComponent(txt2DefineInputPath, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
							.addComponent(btn_2_inputpath)
							.addComponent(btnCrop))))
		);
		panel_1.setLayout(gl_panel_1);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "3. Find Architecture", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_2.setBounds(10, 377, 417, 196);
		getContentPane().add(panel_2);
		panel_2.setLayout(null);
		
		JButton btnNewButton_2 = new JButton("find architecture");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				try {
					
					if(output_path == ""|| trainingsdata_path == ""|| testdata_path =="")
					{
						JOptionPane.showMessageDialog(null, "Please define all Paths!");
						return;
					}
					
					NetworkType nettype = NetworkType.OWN;
					
					if(rdbtnGoogleNet.isSelected())
						nettype = NetworkType.GoogleNet;
					
					if(rdbtnAlexnet.isSelected())
						nettype = NetworkType.AlexNet;
					if(rdbtnKaoEtAl.isSelected())
						nettype = NetworkType.Kao;
					
					//ConvolutionalNeuralNetwork.load(path);
					boolean showinbrowser =  chckbxShowBrowser.isSelected();
					 pg = new ProgressGui(Gui.this);
					pg.setDefaultCloseOperation(pg.HIDE_ON_CLOSE);
					pg.setSize(560,560);
					pg.setLocation(140,140);
					pg.setVisible(true);
					
					
					
					conv_worker = new ConvolutionalNeuralNetwork(trainingsdata_path, testdata_path, output_path, pg, nettype,showinbrowser);
					conv_worker.setBatchSize(batchsize);
					conv_worker.setCnn_min(cnn_min);
					conv_worker.setCnn_max(cnn_max);
					conv_worker.setEpochscounter(epochs);
					conv_worker.execute();
					//ConvolutionalNeuralNetwork.newTry(trainingsdata_path, testdata_path,output_path,nettype);
				} catch ( Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
		});
		btnNewButton_2.setBounds(296, 162, 111, 23);
		panel_2.add(btnNewButton_2);
		
		textField_traingingsdata_path = new JTextField();
		textField_traingingsdata_path.setText("define path");
		textField_traingingsdata_path.setColumns(10);
		textField_traingingsdata_path.setBounds(121, 27, 221, 20);
		panel_2.add(textField_traingingsdata_path);
		
		JLabel lblTrainingsdata = new JLabel("Trainingsdata");
		lblTrainingsdata.setBounds(10, 27, 89, 14);
		panel_2.add(lblTrainingsdata);
		
		JLabel lblTestdata = new JLabel("Testdata");
		lblTestdata.setBounds(10, 52, 89, 14);
		panel_2.add(lblTestdata);
		
		textField_testsdata_path = new JTextField();
		textField_testsdata_path.setText("define path");
		textField_testsdata_path.setColumns(10);
		textField_testsdata_path.setBounds(121, 52, 221, 20);
		panel_2.add(textField_testsdata_path);
		
		textField_output_path = new JTextField();
		textField_output_path.setText("define path");
		textField_output_path.setColumns(10);
		textField_output_path.setBounds(121, 80, 221, 20);
		panel_2.add(textField_output_path);
		
		JButton button_trainingsdata = new JButton("...");
		button_trainingsdata.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				chooser = new JFileChooser(); 
			    chooser.setCurrentDirectory(new java.io.File("."));
			    chooser.setDialogTitle("Chose Storage");
			    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			    //
			    // disable the "All files" option.
			    //
			    chooser.setAcceptAllFileFilterUsed(false);
			    //    
			    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) { 
			      
			    	trainingsdata_path = chooser.getSelectedFile().getPath();
			    	textField_traingingsdata_path.setText(trainingsdata_path);
			      }
			    else {
			    	trainingsdata_path = "";
			      }			
	
			}
		});
		button_trainingsdata.setBounds(349, 27, 58, 23);
		panel_2.add(button_trainingsdata);
		
		JButton button_testdata = new JButton("...");
		button_testdata.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				chooser = new JFileChooser(); 
			    chooser.setCurrentDirectory(new java.io.File("."));
			    chooser.setDialogTitle("Chose Storage");
			    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			    //
			    // disable the "All files" option.
			    //
			    chooser.setAcceptAllFileFilterUsed(false);
			    //    
			    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) { 
			      
			    	testdata_path = chooser.getSelectedFile().getPath();
			    	textField_testsdata_path.setText(testdata_path);
			      }
			    else {
			    	testdata_path = "";
			      }		
			}
		});
		button_testdata.setBounds(349, 55, 58, 23);
		panel_2.add(button_testdata);
		
		JButton button_output = new JButton("...");
		button_output.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooser = new JFileChooser(); 
			    chooser.setCurrentDirectory(new java.io.File("."));
			    chooser.setDialogTitle("Chose Storage");
			    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			    //
			    // disable the "All files" option.
			    //
			    chooser.setAcceptAllFileFilterUsed(false);
			    //    
			    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) { 
			      
			    	output_path = chooser.getSelectedFile().getPath();
			    	textField_output_path.setText(output_path);
			      }
			    else {
			    	output_path = "";
			      }		
			}
		});
		button_output.setBounds(349, 79, 58, 23);
		panel_2.add(button_output);
		
		JLabel lblOutput = new JLabel("Output");
		lblOutput.setBounds(10, 77, 75, 14);
		panel_2.add(lblOutput);
		
		JPanel panel_4 = new JPanel();
		panel_4.setBorder(new TitledBorder(null, "choose Network", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_4.setBounds(10, 102, 407, 49);
		panel_2.add(panel_4);
		panel_4.setLayout(null);
		
		 rdbtnOwn = new JRadioButton("own Network");
		rdbtnOwn.setSelected(true);
		rdbtnOwn.setBounds(6, 19, 109, 23);
		panel_4.add(rdbtnOwn);
		
		 rdbtnGoogleNet = new JRadioButton("GoogleNet");
		rdbtnGoogleNet.setBounds(118, 19, 109, 23);
		panel_4.add(rdbtnGoogleNet);
		
		 rdbtnAlexnet = new JRadioButton("AlexNet");
		rdbtnAlexnet.setBounds(223, 19, 68, 23);
		panel_4.add(rdbtnAlexnet);
		
		JPanel panel_3 = new JPanel();
		panel_3.setBounds(13, 586, 414, 108);
		getContentPane().add(panel_3);
		panel_3.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "4. Try Model", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_3.setLayout(null);
		
		JLabel lblModelPath = new JLabel("Model path");
		lblModelPath.setBounds(10, 24, 63, 14);
		panel_3.add(lblModelPath);
		
		textField_model_path = new JTextField();
		textField_model_path.setText("define path");
		textField_model_path.setColumns(10);
		textField_model_path.setBounds(83, 21, 221, 20);
		panel_3.add(textField_model_path);
		
		textField_image_path = new JTextField();
		textField_image_path.setText("define path");
		textField_image_path.setColumns(10);
		textField_image_path.setBounds(83, 49, 221, 20);
		panel_3.add(textField_image_path);
		
		JLabel lblImagePath = new JLabel("Image path");
		lblImagePath.setBounds(10, 52, 63, 14);
		panel_3.add(lblImagePath);
		
		JButton btnTryIt = new JButton("try it !");
		btnTryIt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if(model_path == ""|| files == null)
				{
					JOptionPane.showMessageDialog(null, "Bitte Pfad zum Model und Bilder auswählen!");
					return;
				}
				
				
		
				HashMap<String, String> ausgabe;
				try {
					ausgabe = ConvolutionalNeuralNetwork.Try_model(new File(model_path), files);
					  Iterator it = ausgabe.entrySet().iterator();
					  String erg = "";
					    while (it.hasNext()) {
					        Map.Entry pair = (Map.Entry)it.next();
					        
					        erg = erg + (pair.getKey() + " " + pair.getValue() + "\n");
					        it.remove(); // avoids a ConcurrentModificationException
					    }
					
					
					
		
					
					JOptionPane.showMessageDialog(null,erg);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					JOptionPane.showMessageDialog(null,"An Error occurred: " + e1.getMessage() );
					e1.printStackTrace();
				}
				
				
				
				
			}
		});
		btnTryIt.setBounds(314, 74, 90, 23);
		panel_3.add(btnTryIt);
		
		JButton button_model_path = new JButton("...");
		button_model_path.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooser = new JFileChooser(); 
			    chooser.setCurrentDirectory(new java.io.File("."));
			    chooser.setDialogTitle("Chose Storage");
			    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			    FileFilter filter = new FileNameExtensionFilter("Zip File", "zip");
			    chooser.setFileFilter(filter);
			    //
			    // disable the "All files" option.
			    //
			    chooser.setAcceptAllFileFilterUsed(false);
			    //    
			    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) { 
			      
			    	model_path = chooser.getSelectedFile().getPath();
			    	textField_model_path.setText(output_path);
			      }
			    else {
			    	model_path = "";
			      }	
				
				
				
			}
		});
		button_model_path.setBounds(314, 24, 58, 23);
		panel_3.add(button_model_path);
		
		JButton button_image_path = new JButton("...");
		button_image_path.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooser = new JFileChooser(); 
			    chooser.setCurrentDirectory(new java.io.File("."));
			    chooser.setDialogTitle("Chose Storage");
			    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			    FileFilter filter = new FileNameExtensionFilter("JPG File", "jpg");
			    chooser.setFileFilter(filter);
			    chooser.setMultiSelectionEnabled(true);
			    //
			    // disable the "All files" option.
			    //
			    chooser.setAcceptAllFileFilterUsed(false);
			    //    
			    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) { 
			    	files = chooser.getSelectedFiles();
			    	try_images = chooser.getSelectedFile().getPath();
			    	textField_image_path.setText(files.toString());
			      }
			    else {
			    	try_images = "";
			      }	
			
			
			
			}
		});
		button_image_path.setBounds(314, 48, 58, 23);
		panel_3.add(button_image_path);
		
		 btG = new ButtonGroup();
		btG.add(rdbtnAlexnet);
		btG.add(rdbtnGoogleNet);
		btG.add(rdbtnOwn);
		
		 rdbtnKaoEtAl = new JRadioButton("Kao et al.");
		rdbtnKaoEtAl.setBounds(293, 19, 109, 23);
		panel_4.add(rdbtnKaoEtAl);
		
		btG.add(rdbtnKaoEtAl);
		 chckbxShowBrowser = new JCheckBox("Show models in browser");
		chckbxShowBrowser.setSelected(true);
		chckbxShowBrowser.setBounds(10, 162, 141, 23);
		panel_2.add(chckbxShowBrowser);
		
		JCheckBox chckbxparams = new JCheckBox("use own parameter");
		chckbxparams.addItemListener(new ItemListener() {
			

			@Override
			public void itemStateChanged(ItemEvent arg0) {
				// TODO Auto-generated method stub
				
				if(arg0.getStateChange() == ItemEvent.DESELECTED)
				{
					epochs = 30;
					batchsize = 15;
					cnn_max = 5;
					cnn_min = 1;
					return;	
				}
				
					param = new ParamGui(Gui.this,cnn_min,cnn_max,epochs,batchsize);
					
					param.setDefaultCloseOperation(param.HIDE_ON_CLOSE);
					param.setSize(400,400);
					param.setLocation(140,140);
					param.setVisible(true);
					
					if(param.isSuccess())
					{
						if(param.getCnn_min()<=param.getCnn_max())
						{
						cnn_min = param.getCnn_min();
						cnn_max = param.getCnn_max();
						}
						batchsize = param.getBatchsize();
						epochs = param.getEpochs();
					}
					
					param.dispose();
				}
		});
		
	
		
		chckbxparams.setBounds(162, 162, 128, 23);
		panel_2.add(chckbxparams);
		
		
	}
	
	
	
	private String[] getTags()
	{
		
		
		String all = textAreaParams.getText();
		
		String[] result = new String[textAreaParams.getText().split("\\n").length];
		
		for(int i = 0; i <textAreaParams.getText().split("\\n").length;i++ )
		{
			result[i] = textAreaParams.getText().split("\\n")[i];
		}
			
		return result;
	}
	
	public class worker extends SwingWorker<Void, Integer>
	{

		@Override
		protected Void doInBackground() throws Exception {
		
			
			
			return null;
		}
		
		public void publishData(Integer i)
		{
			
		}
		
		
		@Override
		protected void process(List<Integer> chucks)
		{
			
			
		}
		
	}
}
