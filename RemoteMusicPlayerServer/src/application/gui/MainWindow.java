package application.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class MainWindow extends JFrame {
	public MainWindow() {
		super("RemoteMusicPlayer :: Server");
		this.setVisible(true);
		this.setSize(500, 250);
		
		JMenuBar menuBar = new JMenuBar();
		
		JMenu menuNetwork	= new JMenu("Network");
		JMenu menuMedia		= new JMenu("Media");
		JMenu menuDevices	= new JMenu("Devices");
		JMenu menuHelp		= new JMenu("Help");
		
		JMenuItem menuNetworkStart				= new JMenuItem("Start");
		JMenuItem menuNetworkStop				= new JMenuItem("Stop");
		JMenuItem menuNetworkConfiguration		= new JMenuItem("Configuration");

		JMenu menuMediaMusicLibrary				= new JMenu("Music library");
		
		JMenuItem menuMediaMusicLibraryBuild	= new JMenuItem("Build");
		JMenuItem menuMediaMusicLibraryUpdate	= new JMenuItem("Update");
		
		JMenuItem menuMediaConfiguration		= new JMenuItem("Configuration");
		
		JMenuItem menuDevicesManage				= new JMenuItem("Manage");
		
		JMenuItem menuHelpAbout					= new JMenuItem("About");

		menuNetwork.add(menuNetworkStart);
		menuNetwork.add(menuNetworkStop);
		menuNetwork.add(menuNetworkConfiguration);
		
		menuMediaMusicLibrary.add(menuMediaMusicLibraryBuild);
		menuMediaMusicLibrary.add(menuMediaMusicLibraryUpdate);
		
		menuMedia.add(menuMediaMusicLibrary);
		menuMedia.add(menuMediaConfiguration);
		
		menuDevices.add(menuDevicesManage);
		
		menuHelp.add(menuHelpAbout);
		
		menuBar.add(menuNetwork);
		menuBar.add(menuMedia);
		menuBar.add(menuDevices);
		menuBar.add(menuHelp);
		
		setJMenuBar(menuBar);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		menuNetworkStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
			}
		});
		
		menuNetworkStop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
			}
		});
		
		menuNetworkConfiguration.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
			}
		});
		
		menuMediaMusicLibraryBuild.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
			}
		});
		
		menuMediaMusicLibraryUpdate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
			}
		});
		
		menuMediaConfiguration.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
			}
		});
		
		menuDevicesManage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
			}
		});
		
		menuHelpAbout.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
			}
		});
	}
}
