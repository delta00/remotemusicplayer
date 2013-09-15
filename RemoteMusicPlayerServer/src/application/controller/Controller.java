package application.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import maryb.player.Player;

import org.farng.mp3.MP3File;
import org.farng.mp3.TagException;
import org.farng.mp3.id3.ID3v1;
import org.xml.sax.SAXException;

import tools.MusicLibrary;
import tools.communicator.Communicator;
import tools.communicator.ConnectionDescriptor;
import tools.communicator.ConnectionListener;
import tools.communicator.PlayerState;

public class Controller implements ConnectionListener, OnChangeListener {
	private Communicator							communicator		= null;
	private Thread									serverThread		= null;
	private boolean									serverRunning		= false;
	
	private MusicLibrary							musicLibrary		= new MusicLibrary();
	private Player									player				= new Player();
	private ConnectionDescriptor					activeConnection	= null;
	private Hashtable<ConnectionDescriptor, User>	users				= new Hashtable<>();
	
	private ControllerErrorListener					errorListener		= null;
	private List<OnChangeListener>					onChangeListeners	= new ArrayList<>();
	
	private String									pathMusicLibrary	= "MusicLibrary.xml";
	
	private PlayerState								playerState			= new PlayerState();

	public void runServer() {
		communicator = new Communicator(9999);
		
		communicator.setConnectionListener(this);
		communicator.setOnChangeListener(this);
		
		serverThread = new Thread() {
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
		};
		
		serverThread.start();
		serverRunning = true;
		
		triggerListenersOnChange();
	}
	
	public void killServer() {
		serverThread.interrupt();
		serverRunning = false;
		
		communicator = null;
		serverThread = null;
		
		users = new Hashtable<>();
		
		triggerListenersOnChange();
	}
	
	public boolean isServerRunning() {
		return serverRunning;
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
	
	public void addOnChangeListener(OnChangeListener onChangeListener) {
		onChangeListeners.add(onChangeListener);
	}

	@Override
	public boolean authenticate(String device, String password) {
		User user = User.authenticate(device, password);
		users.put(activeConnection, user);
		
		onChange();
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
			return internalGetState();
		}
		
		return null;
	}

	@Override
	public boolean play(String filename) {
		if (getCurrentUser() != null && getCurrentUser().hasPermissionPlay()) {
			return internalPlay(filename);
		}
		
		return false;
	}
	
	@Override
	public boolean pause() {
		if (getCurrentUser() != null && getCurrentUser().hasPermissionPause()) {
			return internalPause();
		}
		
		return false;
	}

	@Override
	public boolean unpause() {
		if (getCurrentUser() != null && getCurrentUser().hasPermissionUnpause()) {
			return internalUnpause();
		}
		
		return false;
	}
	
	@Override
	public boolean stop() {
		if (getCurrentUser() != null && getCurrentUser().hasPermissionStop()) {
			return internalStop();
		}
		
		return false;
	}
	
	public PlayerState internalGetState() {
		return playerState;
	}

	public boolean internalPause() {
		player.pause();
		playerState.setPlaying(false);
		
		triggerListenersOnChange();
		return true;
	}

	public boolean internalUnpause() {
		if (player != null) {
			player.play();
			playerState.setPlaying(true);

			triggerListenersOnChange();
			return true;
		}
		else {
			return false;
		}
	}

	public boolean internalStop() {
		if (player != null) {
			player.pause();
			player = null;
			
			playerState.setPlaying(false);
			triggerListenersOnChange();
			
			return true;
		}
		else {
			return false;
		}
	}

	public boolean internalPlay(String filename) {
		try {
			if (player == null) {
				player = new Player();
			}
			
			player.setSourceLocation(filename);
			player.play();
			
			ID3v1 tag = new MP3File(filename).getID3v1Tag();
			
			this.playerState = new PlayerState(
				true,
				tag.getArtist().length() != 0 ? tag.getArtist() : "(unknown)",
				tag.getAlbum().length() != 0 ? tag.getAlbum() : "(unknown)",
				tag.getSongTitle().length() != 0 ? tag.getSongTitle() : "(unknown)",
				filename,
				player.getTotalPlayTimeMcsec() / 1000,
				player.getCurrentPosition() / 1000
			);
		} catch (IOException e) {
			return false;
		} catch (TagException | NullPointerException e) {
			this.playerState = new PlayerState(
				true,
				"(unknown)",
				"(unknown)",
				"(unknown)",
				filename,
				player.getTotalPlayTimeMcsec() / 1000,
				player.getCurrentPosition() / 1000
			);
			
			triggerListenersOnChange();
			return true;
		}
		
		triggerListenersOnChange();
		return true;
	}
	
	public int getConnectionsCount() {
		if (communicator == null) {
			return 0;
		}
		else {
			return communicator.getConnectionsCount();
		}
	}
	
	protected void triggerListenersOnChange() {
		for (OnChangeListener onChangeListener : onChangeListeners) {
			onChangeListener.onChange();
		}
	}

	@Override
	public void onChange() {
		triggerListenersOnChange();
	}
	
	public int getUsersCount() {
		return this.users.size();
	}
}
