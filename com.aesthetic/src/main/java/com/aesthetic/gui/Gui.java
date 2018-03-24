package com.aesthetic.gui;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;

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

import com.aesthetic.main.DBHelper;
import com.aesthetic.main.FlickrCrawler;
import com.aesthetic.net.AVAStructureGeneratior;
import com.aesthetic.net.ConvolutionalNeuralNetwork;
import com.aesthetic.net.StructureGenerator;

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
	public StructureGenerator struc_worker;
	public JTextArea textAreaParams;
	public JLabel lblNewLabel_existindData;
	public JLabel lblNewLabel_img_abort;
	private JTextField txtDefinePath;
	private JFileChooser chooser;
	private String path;
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
		btnCreate.setBounds(338, 7, 89, 23);
		getContentPane().add(btnCreate);
		
		JCheckBox chckbxStore = new JCheckBox("store");
		chckbxStore.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
			
				JOptionPane.showMessageDialog(null, "stored");
			}
		});
		chckbxStore.setBounds(217, 32, 51, 23);
		getContentPane().add(chckbxStore);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "2. Prepare for CNN", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_1.setBounds(10, 286, 417, 76);
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
		txtDefinePath.setText("define path");
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
		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
		gl_panel_1.setHorizontalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addContainerGap()
					.addComponent(txtDefinePath, GroupLayout.PREFERRED_SIZE, 221, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnNewButton_1, GroupLayout.PREFERRED_SIZE, 58, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED, 33, Short.MAX_VALUE)
					.addGroup(gl_panel_1.createParallelGroup(Alignment.TRAILING)
						.addComponent(btnNewButton)
						.addComponent(btnAva))
					.addContainerGap())
		);
		gl_panel_1.setVerticalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addContainerGap(18, Short.MAX_VALUE)
					.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
						.addComponent(txtDefinePath, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnNewButton_1))
					.addContainerGap())
				.addGroup(gl_panel_1.createSequentialGroup()
					.addComponent(btnNewButton)
					.addPreferredGap(ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
					.addComponent(btnAva))
		);
		panel_1.setLayout(gl_panel_1);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBounds(10, 363, 417, 76);
		getContentPane().add(panel_2);
		panel_2.setLayout(null);
		
		JButton btnNewButton_2 = new JButton("Load");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				try {
					ConvolutionalNeuralNetwork.load(path);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
		});
		btnNewButton_2.setBounds(318, 11, 89, 23);
		panel_2.add(btnNewButton_2);
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
