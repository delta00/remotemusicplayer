package application.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import maryb.player.Player;

import tools.MusicLibrary;
import tools.communicator.Communicator;
import tools.communicator.ConnectionDescriptor;
import tools.communicator.ConnectionListener;
import tools.communicator.PlayerState;

public class Controller implements ConnectionListener {
	private Communicator							communicator		= new Communicator(9999);
	private MusicLibrary							musicLibrary		= new MusicLibrary();
	private Player									player				= new Player();
	private ControllerErrorListener					errorListener		= null;
	private ConnectionDescriptor					activeConnection	= null;
	private Hashtable<ConnectionDescriptor, User>	users				= new Hashtable<>();
	
	private String									pathMusicLibrary	= "MusicLibrary.xml";
	
	public void run() {
		communicator.setConnectionListener(this);
		
		new Thread() {
			@Override
			public void run() {
				try {
					communicator.run();
				} catch (IOException e) {
					if (errorListener != null) {
						errorListener.addIOException(e);
					}
				}
			}
		}.start();
	}

	public User getCurrentUser() {
		return users.get(activeConnection);
	}
	
	@Override
	public void setActiveConnection(ConnectionDescriptor connectionDescriptor) {
		activeConnection = connectionDescriptor;
	}
	
	public void setErrorListener(ControllerErrorListener controllerErrorListener) {
		this.errorListener = controllerErrorListener;
	}

	@Override
	public boolean authenticate(String device, String password) {
		User user = User.authenticate(device, password);
		users.put(activeConnection, user);
		
		return (user != null);
	}

	@Override
	public void invalidCommand() {
		//
	}

	@Override
	public void close() {
		users.remove(activeConnection);
	}

	@Override
	public boolean checkVersion(long version) {
		if (getCurrentUser() != null && getCurrentUser().hasPermissionCheckVersion()) {
			try {
				return (MusicLibrary.fastLoadID(pathMusicLibrary) == version);
			}
			catch (ParserConfigurationException | IOException | SAXException e) {
				return false;
			}
		}
		
		return false;
	}

	@Override
	public String update() {
		if (getCurrentUser() != null && getCurrentUser().hasPermissionUpdate()) {
			try {
				return MusicLibrary.fastLoadContent(pathMusicLibrary);
			}
			catch (IOException e) {
				return null;
			}
		}
		
		return null;
	}

	@Override
	public PlayerState getState() {
		if (getCurrentUser() != null && getCurrentUser().hasPermissionGetState()) {
			// TODO: get state
			return new PlayerState(true, "Artist", "Album", "Song", 300, 20);
		}
		
		return null;
	}

	@Override
	public boolean play(String filename) {
		if (getCurrentUser() != null && getCurrentUser().hasPermissionPlay()) {
			player.setSourceLocation(filename);
			player.play();
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean pause() {
		if (getCurrentUser() != null && getCurrentUser().hasPermissionPause()) {
			player.pause();
			return true;
		}
		
		return false;
	}

	@Override
	public boolean unpause() {
		if (getCurrentUser() != null && getCurrentUser().hasPermissionUnpause()) {
			if (player != null) {
				player.play();
			}
		}
		
		return false;
	}
	
	@Override
	public boolean stop() {
		if (getCurrentUser() != null && getCurrentUser().hasPermissionStop()) {
			player.stop();
			return true;
		}
		
		return false;
	}
}
