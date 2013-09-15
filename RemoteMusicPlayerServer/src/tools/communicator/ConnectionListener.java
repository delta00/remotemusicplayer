package tools.communicator;

/**
 * <p>
 * 	This interface must be implemented by each listener for the client's
 * 	commands. It's important to remember, that all connections uses only one
 * 	implementation and one instance of this class. For this reason, there's a
 * 	method {@link #setActiveConnection(ConnectionDescriptor)}, which is triggered
 * 	before each call of any other method, so it's possible to detect which client
 * 	generated the command.
 * </p>
 * 
 * @author Tomáš Zíma
 */
public interface ConnectionListener {
	public void			setActiveConnection(ConnectionDescriptor connectionDescriptor);
	public boolean		authenticate(String device, String password);
	public void			invalidCommand();
	public void			close();
	public boolean		checkVersion(long version);
	public String		update();
	public PlayerState	getState();
	public boolean		pause();
	public boolean		unpause();
	public boolean		stop();
	public boolean		play(String filename);
}
