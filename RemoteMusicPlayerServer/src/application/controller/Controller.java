package application.controller;

import java.io.IOException;
import java.util.Hashtable;

import tools.MusicLibrary;
import tools.communicator.Communicator;
import tools.communicator.ConnectionDescriptor;
import tools.communicator.ConnectionListener;
import tools.communicator.PlayerState;

public class Controller implements ConnectionListener {
	private Communicator							communicator		= new Communicator(9999);
	private MusicLibrary							musicLibrary		= new MusicLibrary();
	private ControllerErrorListener					errorListener		= null;
	private ConnectionDescriptor					activeConnection	= null;
	private Hashtable<ConnectionDescriptor, User>	users				= new Hashtable<>();
	
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
		//
	}

	@Override
	public boolean checkVersion(long version) {
		if (getCurrentUser() != null && getCurrentUser().hasPermissionCheckVersion()) {
			// TODO: checkVersion
			return true;
		}
		
		return false;
	}

	@Override
	public String update() {
		if (getCurrentUser() != null && getCurrentUser().hasPermissionUpdate()) {
			// TODO: update
			return "<musicLibrary></musicLibrary>";
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
			// TODO: play
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean pause() {
		if (getCurrentUser() != null && getCurrentUser().hasPermissionPause()) {
			// TODO: pause
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean stop() {
		if (getCurrentUser() != null && getCurrentUser().hasPermissionStop()) {
			// TODO: stop
			return true;
		}
		
		return false;
	}
}
