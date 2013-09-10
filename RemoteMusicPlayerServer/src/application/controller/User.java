package application.controller;

public class User {
	private boolean	authenticate	= true;
	private boolean	checkVersion	= true;
	private boolean	update			= true;
	private boolean	getState		= true;
	private boolean	pause			= true;
	private boolean unpause			= true;
	private boolean	stop			= true;
	private boolean	play			= true;
	
	public User(boolean authenticate, boolean checkVersion, boolean update, boolean getState, boolean pause, boolean stop, boolean play, boolean unpause) {
		this.authenticate	= authenticate;
		this.checkVersion 	= checkVersion;
		this.update			= update;
		this.getState		= getState;
		this.pause			= pause;
		this.stop			= stop;
		this.play			= play;
	}

	public User() {
		//
	}
	
	public static User authenticate(String device, String password) {
		if (device.equals("phone") && password.equals("pass")) {
			return new User();
		}
		else {
			return null;
		}
	}
	
	public boolean hasPermissionAuthenticate() {
		return authenticate;
	}

	public boolean hasPermissionCheckVersion() {
		return checkVersion;
	}

	public boolean hasPermissionUpdate() {
		return update;
	}

	public boolean hasPermissionGetState() {
		return getState;
	}

	public boolean hasPermissionPause() {
		return pause;
	}

	public boolean hasPermissionStop() {
		return stop;
	}

	public boolean hasPermissionPlay() {
		return play;
	}
	
	public boolean hasPermissionUnpause() {
		return unpause;
	}
}
