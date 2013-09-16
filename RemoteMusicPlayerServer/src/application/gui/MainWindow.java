package application.gui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;

import tools.communicator.PlayerState;

import application.controller.Controller;
import application.controller.ControllerErrorListener;
import application.controller.OnChangeListener;
import application.controller.PlayerStatus;
import application.controller.ServerStatus;


import cookxml.cookswing.CookSwing;

public class MainWindow implements OnChangeListener, ControllerErrorListener {
	private CookSwing		cookSwing						= new CookSwing(this);
	private Controller		controller						= new Controller();

	private String			artist							= "";
	private String			album							= "";
	private String			song							= "";
	private String			file							= "";
	private String			playerStatus					= "";

	private String			serverStatus					= "";
	private String			connections						= "";
	private int				usersCount						= 0;
	
	public ActionListener	buttonNetworkServerStart		= new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					((JMenuItem) cookSwing.getId("networkServerStart").object).setVisible(false);
					((JMenuItem) cookSwing.getId("networkServerStop").object).setVisible(true);
				}
			});
			
			controller.runServer();
		}
	};
	
	public ActionListener	buttonNetworkServerStop			= new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					((JMenuItem) cookSwing.getId("networkServerStop").object).setVisible(false);
					((JMenuItem) cookSwing.getId("networkServerStart").object).setVisible(true);
				}
			});
			
			controller.killServer();
		}
	};
	
	public ActionListener	buttonNetworkConfiguration		= new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			new NetworkConfiguration(controller);
		}
	};

	public ActionListener	buttonMediaMusicLibraryBuild	= new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("music library build");
		}
	};
	
	public ActionListener	buttonMediaMusicLibraryUpdate	= new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			JOptionPane.showMessageDialog(
				null,
				"Unfortunately, update of the music library isn't supported currently.\nPlease, use the \"Build\" button above.",
				"Unsupported operation",
				JOptionPane.ERROR_MESSAGE
			);
		}
	};
	
	public ActionListener	buttonMediaPlayerPlay			= new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			controller.internalUnpause();
			
			JOptionPane.showMessageDialog(
				null,
				"The song will only play if it wasn't stopped previously\nor if it didn't reach the end of file." ,
				"Hint",
				JOptionPane.INFORMATION_MESSAGE
			);
		}
	};
	
	public ActionListener	buttonMediaPlayerPause			= new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			controller.internalPause();
		}
	};
	
	public ActionListener	buttonMediaPlayerStop			= new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			controller.internalStop();
			
			JOptionPane.showMessageDialog(
				null,
				"You stopped the song and it was released from memory.\nIt won't play if you choose \"Play\" in the menu now.",
				"Hint",
				JOptionPane.INFORMATION_MESSAGE
			);
		}
	};

	public ActionListener	buttonMediaConfiguration		= new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("media configuration");
		}
	};
	
	public ActionListener	buttonDevicesManage				= new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("devices manage");
		}
	};
	
	public ActionListener	buttonHelpAbout					= new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("help about");
		}
	};
	
	public MainWindow(Controller controller) {
		this.controller = controller;
		controller.addOnChangeListener(this);
		controller.setErrorListener(this);
		controller.runServer();
		
		cookSwing.render("res/gui/MainWindow.xml").setVisible(true);
		onChange();
	}

	@Override
	public void onChange() {
		synchronized (cookSwing) {
			PlayerState playerState = controller.internalGetState();
			
			artist			= playerState.getArtist();
			album			= playerState.getAlbum();
			song			= playerState.getSong();
			file			= playerState.getFile();
			playerStatus	= playerState.isPlaying() ? "playing" : "not playing";
			
			serverStatus	= controller.isServerRunning() ? "on" : "off";
			connections		= String.valueOf(controller.getConnectionsCount());
			usersCount		= controller.getUsersCount();
			
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						((JTextPane) cookSwing.getId("playerState").object).setText(
							"<table>" +
								"	<tr><td><b>Artist: </b></td><td>" + artist + "</td></tr>" +
								"	<tr><td><b>Album: </b></td><td>" + album + "</td></tr>" +
								"	<tr><td><b>Song: </b></td><td>" + song + "</td></tr>" +
								"	<tr><td><b>File: </b></td><td>" + file + "</td></tr>" +
								"	<tr><td><b>Status: </b></td><td>" + playerStatus + "</td></tr>" +
							"</table>"
						);
						
						((JTextPane) cookSwing.getId("serverInfo").object).setText(
							"<table>" +
								"	<tr><td><b>Status: </b></td><td>" + serverStatus + "</td></tr>" +
								"	<tr><td><b>Connections: </b></td><td>" + connections + "</td></tr>" +
								"	<tr><td><b>Users: </b></td><td>" + usersCount + "</td></tr>" +
							"</table>"
						);
					} catch (NullPointerException e) {
						// Because onChange() event might be triggered right during initialization
						// of the application (before GUI and defining XML are properly loaded), it
						// will cause the NullPointerException when attempting to write something
						// into GUI (cookSwing.getId(...) will return null).
						//
						// It is important to catch this exception, but program should run anyway
						// as it usually will not be a error. But it is definitely good idea to
						// print error message for debugging purposes.
						System.err.println("GUI: Some element(s) in XML wasn't found.");
					}
				}
			});
		}
	}

	@Override
	public void addIOException(IOException exception) {
		String stackTrace = "";
		
		for (StackTraceElement stackTraceElement : exception.getStackTrace()) {
			stackTrace += stackTraceElement.toString() + "\n\t";
		}
		
		JOptionPane.showMessageDialog(
			null,
			"Error message:\n" + exception.getMessage() + "\n\nStack trace:\n" + stackTrace + "\n", 
			"Error",
			JOptionPane.ERROR_MESSAGE
		);
		
		onChange();
	}
}
