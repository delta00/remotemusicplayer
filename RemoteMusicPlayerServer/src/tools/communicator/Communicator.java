package tools.communicator;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import application.controller.OnChangeListener;

/**
 * <p>
 * 	This class creates TCP server, opens connections with clients and takes care of their requests.
 * 	Each client connection is processed by separate thread implemented by internal class
 * 	ConnectionHandler.
 * </p>
 * 
 * <p>
 * 	Client requests (commands) aren't handled directly, but via ConnectionListener, which should be
 * 	assigned before server is started using method run(). Therefore, this class isn't executing any
 * 	operations, but only reads commands from clients, sends replies and proceeds these commands to
 * 	the ConnectionListener.
 * </p>
 * 
 * <p>
 * 	Please remember, that this class will block the current thread as there's a loop, which is waiting
 * 	for incoming connections. For this reason, it's probably necessary to execute this class in separate
 * 	thread.
 * </p>
 * 
 * <p>
 * 	<h1>How to use this class</h1>
 * 	<p>
 * 		<ol>
 * 			<li>Create instance of this class.</li>
 * 			<li>Call method {@link #setConnectionListener(ConnectionListener)} with an appropriate argument.</li>
 * 			<li>Create a new thread and execute method {@link #run()} in this new thread.</li>
 * 			<li><b>Optional</b>: Check if there are opened client connections using method {@link #hasOpenedConnection()}.</li>
 * 			<li><b>Optional</b>: Kill the thread, where the server is running, using method Thread.interrupt().</li>
 * 		</ol>
 * 	</p>
 * </p>
 * 
 * <p>
 * 	<h1>Protocol definition</h1>
 * 	<p>
 * 		All communication goes through very simple protocol. Server doesn't send any data automatically, it only responds
 * 		to client's commands. Each command will get reply, but in the most cases it's only simple "OK" or "NO", so client
 * 		may check that command was proceeded.
 * 	</p>
 * 	<p>
 * 		Here's the list of all commands and it's replies. If reply is not specified, then it's just the "OK" or "NO" as
 * 		described above. <b>Please remember, that EACH COMMAND must be followed by "\n" character!</b>
 * 
 * 		<ul>
 * 			<li><i>AUTHENTICATE "deviceName" "password"</i> (authenticates user for all other operations)</li>
 * 			<li><i>CHECK "libraryVersionNumber"</i> (checks if music library isn't out-dated)</li>
 * 			<li><i>UPDATE</i> (downloads a music library)
 * 				<p>
 * 					<b>Answer</b> will be an one-line XML document describing whole music library. This XML document is
 * 					generated by class {@link tools.MusicLibrary}.
 * 				</p>
 * 			</li>
 * 			<li><i>GET_STATE (returns current state of player)</i>
 * 				<p>
 * 					<b>Answer</b> will be an one-line text string describing the current state. Template looks like this:
 * 					<br><p>{@code
 * 						PLAYING="yes";ARTIST="artist";ALBUM="album";SONG="song";LENGTH="length";POSITION="position";
 * 					}</p><br>
 * 				</p>
 * 				If player isn't playing, but it's only paused, value of PLAYING should be "no" and all other values
 * 				should be filled normally. If player isn't playing and it's not paused in the same time, all other
 * 				values should be empty, but it must be present!
 * 			</li>
 * 			<li><i>PAUSE</i> (pauses the song)</li>
 *			<li><i>STOP</i> (stops the song)</li>
 *			<li><i>PLAY "filename"</i> (plays the song)</li>
 * 		</ul>
 * 	</p>
 * </p>
 * 
 * @author	Tomáš Zíma
 * @see		ConnectionListener
 * @see		ConnectionHandler
 * @see		ConnectionDescriptor
 * @see		#setConnectionListener(ConnectionListener)
 */
public class Communicator {
	/**
	 * Listener, which will be used for handling of client connections.
	 */
	private ConnectionListener	connectionListener	= null;
	
	/**
	 * Using this variable, an unique number will be assigned to each
	 * connection.
	 */
	private int					connectionIdCounter	= 0;
	
	/** List of all client threads. Thread of this class isn't there! */
	private List<Thread>		handlerThreadsList	= new ArrayList<>();
	
	/**
	 * Regular expression, which will be used for parsing of parameters from
	 * commands.
	 */
	private final Pattern		parameterPattern	= Pattern.compile("\"(.*?)\"");

	/**
	 * Amount of time, for which the thread will be blocked while trying to open
	 * new client connection. When this time expires, method accept() will throw
	 * an exception and run of thread will continue.
	 */
	private final int			ACCEPT_TIMEOUT		= 50;
	
	/**
	 * Amount of time, for which the thread will be blocked while trying to read
	 * data from client. When this time expires, input stream will throw an
	 * exception and run of thread will continue.
	 */
	private final int			READER_TIMEOUT		= 50;
	
	/** Number of TCP port to listen on. */
	private int					tcpListenPort		= 9999;
	
	/** This listener will be triggered, if something changes. */
	private OnChangeListener	onChangeListener	= null;
	
	/**
	 * This class takes care of actual connection between server and one client.
	 * It reads commands, triggers listener and sends replies. Protocol, which
	 * is used for communication, was described in the Javadoc for class
	 * {@link Communicator} as well as some other details.
	 * 
	 * @author Tomáš Zíma
	 */
	protected class ConnectionHandler {
		/** Socket between server and client. */
		private Socket					socket;
		
		/**
		 * Structure, which contains informations about connection. It's used by
		 * listener to check out which client is executing the command.
		 */
		private ConnectionDescriptor	connectionDescriptor;
		
		/** Object, which is used for reading of data from client. */
		private BufferedReader			reader;
		
		/** Object, which is used for writing of data to the client. */
		private DataOutputStream		writer;
		
		/**
		 * @param socket
		 * 	Socket between server and client.
		 * 
		 * @param connectionDescriptor
		 * 	Structure, which contains informations about connection.
		 * 
		 * @throws IOException
		 * 	This exception will be thrown, if Socket has invalid
		 *	input or output stream, or if method setSoTimeout()
		 *	couldn't be finished successfully.
		 */
		public ConnectionHandler(Socket socket, ConnectionDescriptor connectionDescriptor) throws IOException {
			// Save values
			this.socket					= socket;
			this.connectionDescriptor	= connectionDescriptor;
			
			// Create objects for reading and writing
			this.reader					= new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.writer					= new DataOutputStream(socket.getOutputStream()); 
			
			// Set timeout for reading (will block only for limited amount of time)
			this.socket.setSoTimeout(READER_TIMEOUT);
			
			// Trigger listener (new connection)
			if (onChangeListener != null) {
				onChangeListener.onChange();
			}
		}
		
		/**
		 * This method creates loop, which reads data from the client and
		 * triggers listener based on the received command. It also sends
		 * replies. The loop will finish only if the current thread is
		 * interrupted or socket is closed.
		 */
		public void run() {
			// Until the thread isn't interrupted and connection exists
			while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
				try {
					// Read one line from client ("\n" will be dropped out!).
					String command = reader.readLine();
					
					// If readLine() returned null, it means that client closed
					// the connection.
					if (command == null) {
						// Close the socket.
						socket.close();
						
						// Let the connection listener know, that connection
						// was closed.
						synchronized (connectionListener) {
							if (connectionListener != null) {
								connectionListener.setActiveConnection(connectionDescriptor);
								connectionListener.close();
							}
						}
						
						// Let the thread die and after 100ms, trigger the
						// OnChangeListener. It cannot be triggered immediately,
						// because thread will be still living that time.
						new Timer().schedule(new TimerTask(){
							@Override
							public void run() {
								if (onChangeListener != null) {
									onChangeListener.onChange();
								}
							}
						}, 100);
						
						// Exit loop (thread)
						return;
					}

					// Ensure, that any other thread is accessing the
					// connectionListener this time.
					synchronized (connectionListener) {
						// Set active connection, so the listener can check out,
						// which client is executing the command.
						connectionListener.setActiveConnection(connectionDescriptor);
						
						// Tries to read all parameters from the command.
						List<String> parameters = parseParameters(command);

						// Determine the command, trigger listener and send
						// reply. 
						if (command.matches("^AUTHENTICATE \".*\" \".*\"$")) {
							sendReply(connectionListener.authenticate(parameters.get(0), parameters.get(1)));
						}
						else
						if (command.matches("^CHECK \".*\"$")) {
							sendReply(connectionListener.checkVersion(Integer.valueOf(parameters.get(0))));
						}
						else
						if (command.matches("^UPDATE$")) {
							writer.writeBytes(connectionListener.update() + "\n");
						}
						else
						if (command.matches("^GET_STATE$")) {
							PlayerState state = connectionListener.getState();
							
							writer.writeBytes(
								"PLAYING=\""	+ "yes"					+ "\";" +
								"ARTIST=\""		+ state.getArtist()		+ "\";" +
								"ALBUM=\""		+ state.getAlbum()		+ "\";" +
								"SONG=\""		+ state.getSong()		+ "\";" +
								"LENGTH=\""		+ state.getLength()		+ "\";" +
								"POSITION=\""	+ state.getPosition()	+ "\";\n"
							);
						}
						else
						if (command.matches("^PAUSE$")) {
							sendReply(connectionListener.pause());
						}
						else
						if (command.matches("^STOP$")) {
							sendReply(connectionListener.stop());
						}
						else
						if (command.matches("^UNPAUSE$")) {
							sendReply(connectionListener.unpause());
						}
						else
						if (command.matches("^PLAY \".*\"$")) {
							sendReply(connectionListener.play(parameters.get(0)));
						}
						else {
							connectionListener.invalidCommand();
						}
					}
				} catch (SocketTimeoutException e) {
					continue;
				} catch (IOException e) {
					connectionListener.close();
					break;
				}
			}

			// Close connection (client will be disconnected)
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if (connectionListener != null) {
				connectionListener.close();
			}
		}
		
		/**
		 * Sends reply "OK" or "NO" to the client, based on the parameter.
		 * 
		 * @param possitive
		 * 	true => "OK\n", false => "NO\n"
		 * 
		 * @throws IOException
		 */
		protected void sendReply(boolean possitive) throws IOException {
			if (possitive) {
				writer.writeBytes("OK\n");
			}
			else {
				writer.writeBytes("NO\n");
			}
		}
	}

	/**
	 * Does nothing. Use this constructor if you want to use default port to
	 * listen on.
	 */
	public Communicator() {
		//
	}
	
	/**
	 * Sets TCP port to listen on.
	 * 
	 * @param tcpListenPort
	 * 	Number of TCP port to listen on.
	 */
	public Communicator(int tcpListenPort) {
		this.tcpListenPort = tcpListenPort;
	}
	
	/**
	 * Executes main loop, which is waiting for incoming connections and
	 * delegating them to new thread (using class {@link ConnectionHandler}.
	 * Loop will only exit if server socket is closed or if current thread is
	 * interrupted.
	 * 
	 * @throws IOException
	 */
	public void run() throws IOException {
		// Creates TCP server on the specified port.
		ServerSocket serverSocket = new ServerSocket(tcpListenPort);
		
		// Sets timeout after which reading from client will be interrupted.
		serverSocket.setSoTimeout(ACCEPT_TIMEOUT);
		
		// Until the thread is interrupted or socket is closed...
		while (!Thread.currentThread().isInterrupted() && !serverSocket.isClosed()) {
			try {
				// Wait for new connection and get it's socket
				final Socket socket = serverSocket.accept();
				
				// Create structure describing this connection (it must be unique) 
				final ConnectionDescriptor connectionDescriptor = new ConnectionDescriptor(
					connectionIdCounter++,
					socket.getInetAddress()
				);
				
				// Create new handler for this connection
				final ConnectionHandler connectionHandler = new ConnectionHandler(
					socket,
					connectionDescriptor
				);
				
				// Create new thread for the handler
				Thread handlerThread = new Thread() {
					@Override
					public void run() {
						// Execute loop, which will take care of communication between
						// server and (one) client.
						connectionHandler.run();

						// We need to let the OnChangeListener know, connection was
						// closed. In the moment, when this code is executed, connection
						// still exists, so it's not possible to just call "onChange()"
						// method (the result would be invalid value in the GUI).
						//
						// This is the best solution I could think of. OnChangeListener
						// will not be triggered immediately, but will be delayed using
						// timer.
						//
						// WARNING: The value 100ms MIGHT NOT BE the best value and it
						// may cause problems on slower computer. If GUI is not showing
						// correct informations on some computer, increase this value!
						new Timer().schedule(new TimerTask() {
							@Override
							public void run() {
								// Trigger listener (connection closed)
								if (onChangeListener != null) {
									onChangeListener.onChange();
								}
							}
						}, 100);
					}
				};
				
				// Execute handler in its own thread
				handlerThread.start();
				handlerThreadsList.add(handlerThread);
				
				// Delete all threads which are not active anymore
				removeFinishedThreads();
				
				// Trigger listener (new connection)
				if (onChangeListener != null) {
					onChangeListener.onChange();
				}
			} catch (SocketTimeoutException e) {
				continue;
			}
		}
		
		// Interrupt all threads
		for (Thread handlerThread : handlerThreadsList) {
			handlerThread.interrupt();
		}
		
		// Replace list of all (client connection) threads with the new one.  
		handlerThreadsList = new ArrayList<>();
		
		// Close server socket.
		serverSocket.close();
		
		// Trigger listener (all connections closed)
		if (onChangeListener != null) {
			onChangeListener.onChange();
		}
	}
	
	/**
	 * Sets the listener which will be used for handling of client requests.
	 * 
	 * @param connectionListener
	 * 	Listener, which will be used for handling of client requests.
	 * 
	 * @see ConnectionListener
	 */
	public void setConnectionListener(ConnectionListener connectionListener) {
		this.connectionListener = connectionListener;
	}
	
	/**
	 * Checks if there are some opened connections between server and clients.
	 * 
	 * @return
	 * 	True: yes, there are some opened connections (at least one),
	 * 	false: no, there are not
	 */
	public boolean hasOpenedConnection() {
		for (Thread handlerThread : handlerThreadsList) {
			if (handlerThread.isAlive()) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * @return
	 * 	Count of opened (active) connections.
	 */
	public int getConnectionsCount() {
		int result = 0;
		
		for (Thread handlerThread : handlerThreadsList) {
			if (handlerThread.isAlive()) {
				result++;
			}
		}
		
		return result;
	}
	
	/**
	 * Removes all inactive threads, which were used for communication with
	 * clients.
	 */
	protected void removeFinishedThreads() {
		for (int i = 0; i < handlerThreadsList.size(); i++) {
			if (!handlerThreadsList.get(i).isAlive()) {
				handlerThreadsList.remove(i);
				i--;
			}
		}
	}
	
	/**
	 * Parses parameters from the incoming client's command. Commands looks
	 * like:
	 * 
	 * <p>{@code
	 * 	COMMAND "parameter1" "parameter2"
	 * }</p>
	 * 
	 * @param command
	 * 	Text string with one-line command.
	 * 
	 * @return
	 * 	Array of all parameters (without quotes).
	 */
	protected List<String> parseParameters(String command) {
		Matcher			matcher	= parameterPattern.matcher(command);
		List<String>	result	= new LinkedList<>();
		
		while (matcher.find()) {
			result.add(matcher.group().substring(1, matcher.group().length() - 1));
		}
		
		return result;
	}
	
	/**
	 * @param onChangeListener
	 * 	Sets listener, which will be triggered, if something changes.
	 */
	public void setOnChangeListener(OnChangeListener onChangeListener) {
		this.onChangeListener = onChangeListener;
	}
}
