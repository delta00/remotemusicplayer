package tools.communicator;

import java.net.InetAddress;

/**
 * This class described single connection and might be used to identify the
 * client.
 * 
 * @author Tomáš Zíma
 */
public class ConnectionDescriptor {
	private InetAddress	deviceInetAddress;
	private int			connectionId;
	
	/**
	 * @param connectionId
	 * 	Unique ID of the connection.
	 * 
	 * @param deviceInetAddress
	 * 	Network address of the client (IP address).
	 */
	public ConnectionDescriptor(int connectionId, InetAddress deviceInetAddress) {
		this.deviceInetAddress	= deviceInetAddress;
		this.connectionId		= connectionId;
	}

	/**
	 * @return
	 * 	Network address of the client (IP address).
	 */
	public InetAddress getDeviceInetAddress() {
		return deviceInetAddress;
	}
	
	/**
	 * Compares this instance of this class against the other.
	 * 
	 * @return
	 * 	Returns true if data in both classes are the same, or false
	 * 	if they are not. 
	 */
	@Override
	public boolean equals(Object obj) {
		ConnectionDescriptor connectionDescriptor = (ConnectionDescriptor) obj;
		
		return (
			this.deviceInetAddress.equals(connectionDescriptor.getDeviceInetAddress())
			&&
			this.connectionId == connectionDescriptor.connectionId
		);
	}
}
