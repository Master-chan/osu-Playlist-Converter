package me.mc.m3ucreator;

import java.awt.Button;
import java.awt.EventQueue;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;


public class MainWindow extends JFrame
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ExecutorService taskExecutorService = Executors.newSingleThreadExecutor();
	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					MainWindow frame = new MainWindow();
					frame.setTitle("osu!PlaylistCreator");
					frame.setVisible(true);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}
	
	private File selectedFolder = new File("D:\\games\\osu!\\Songs\\");

	/**
	 * Create the frame.
	 */
	public MainWindow()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 390, 308);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		final JFileChooser chooser = new JFileChooser(selectedFolder);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setSelectedFile(selectedFolder);

		final TextField path = new TextField();
		path.setBounds(16, 16, 275, 22);
		path.setEditable(false);
		path.setText(selectedFolder.getAbsolutePath());
		contentPane.add(path);

		final Button selectPathButton = new Button("...");
		selectPathButton.setBounds(310, 16, 50, 22);
		selectPathButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int returnVal = chooser.showOpenDialog(getContentPane());
				if(returnVal == JFileChooser.APPROVE_OPTION)
				{
					path.setText(chooser.getSelectedFile().getAbsolutePath());
					selectedFolder = chooser.getSelectedFile();
				}

			}
		});
		contentPane.add(selectPathButton);
	
		/*final JCheckBox ignoreSongLength = new JCheckBox("Ignore song length");
		ignoreSongLength.setToolTipText("Don't write song length to EXTINF, will greatly reduce processing time.");
		ignoreSongLength.setBounds(16, 44, 132, 22);
		contentPane.add(ignoreSongLength);*/
		
		final JSpinner spinner = new JSpinner();
		spinner.setModel(new SpinnerNumberModel(1, 1, 32767, 1));
		spinner.setBounds(295, 46, 65, 20);
		contentPane.add(spinner);
		
		final Label label = new Label("Minimum song length:");
		label.setBounds(175, 44, 116, 22);
		contentPane.add(label);
		
		final JProgressBar progressBar = new JProgressBar();
		progressBar.setBounds(16, 234, 344, 25);
		progressBar.setVisible(false);
		contentPane.add(progressBar);
		
		JLabel descriptionLabel = new JLabel("<html>Output tag format:<br>\r\n%artist% - Artist name<br>\r\n%artistUnicode% - Artist name in Unicode<br>\r\n%title% - Song title<br>\r\n%titleUnicode% - Song title in Unicode<br>\r\n%source% - Song source (if exists)<br></html>");
		descriptionLabel.setBounds(16, 73, 344, 102);
		contentPane.add(descriptionLabel);
		
		final JTextField formatTextField = new JTextField();
		formatTextField.setText("%artist% - %title%");
		formatTextField.setBounds(16, 186, 344, 20);
		contentPane.add(formatTextField);
		formatTextField.setColumns(10);
		
		final Button createButton = new Button("Create");
		createButton.setBounds(16, 234, 344, 25);
		createButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				createButton.setEnabled(false);
				createButton.setVisible(false);
				formatTextField.setEditable(false);
				//ignoreSongLength.setEnabled(false);
				spinner.setEnabled(false);
				selectPathButton.setEnabled(false);
				progressBar.setVisible(true);
				
				ConfigScheme config = new ConfigScheme(formatTextField.getText(), selectedFolder, (int) spinner.getValue());
				final M3UCreator creator = new M3UCreator(config, progressBar);
				taskExecutorService.submit(creator);
				taskExecutorService.submit(new Runnable()
				{
					@Override
					public void run()
					{
						EventQueue.invokeLater(new Runnable()
						{
							public void run()
							{
								createButton.setEnabled(true);
								//ignoreSongLength.setEnabled(true);
								formatTextField.setEditable(true);
								spinner.setEnabled(true);
								selectPathButton.setEnabled(true);
								progressBar.setVisible(false);
								createButton.setVisible(true);
								JOptionPane
								.showMessageDialog(
										null,
										"Done!\n" + "Songs added to playlist: " + creator.getSongsProcessed() + "\nLow length songs dropped: " + creator.getSongsDropped(),
										"Generation complete",
										JOptionPane.INFORMATION_MESSAGE);
							}
						});
					}
				});
			}
		});
		contentPane.add(createButton);

	}
}
