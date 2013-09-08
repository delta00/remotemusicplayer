package application;

import java.io.IOException;

import tools.communicator.Communicator;
import tools.communicator.ConnectionDescriptor;
import tools.communicator.ConnectionListener;
import tools.communicator.PlayerState;

public class Main {
	public static void main(String[] args) throws Exception {
		final Communicator communicator = new Communicator();
		
		communicator.setConnectionListener(new ConnectionListener() {
			@Override
			public String update() {
				System.out.println("Update.");
				return "<musicLibrary></musicLibrary>";
			}
			
			@Override
			public boolean stop() {
				System.out.println("Stop.");
				return true;
			}
			
			@Override
			public void setActiveConnection(ConnectionDescriptor connectionDescriptor) {
				System.out.printf("Aktivni pripojeni: %s\n", connectionDescriptor);
			}
			
			@Override
			public boolean play(String filename) {
				System.out.printf("Hrat: %s\n", filename);
				return true;
			}
			
			@Override
			public boolean pause() {
				System.out.println("Pause.");
				return true;
			}
			
			@Override
			public PlayerState getState() {
				System.out.println("Get state");
				return new PlayerState(false, "Sybreed", "Slave design", "Bioactive", 320, 60);
			}
			
			@Override
			public boolean checkVersion(long version) {
				System.out.printf("check %d\n", version);
				return false;
			}
			
			@Override
			public boolean authenticate(String device, String password) {
				System.out.printf("autorizace: \"%s\", \"%s\"\n", device, password);
				return false;
			}

			@Override
			public void close() {
				System.out.println("spojeni uzavreno");
			}

			@Override
			public void invalidCommand() {
				System.out.println("Invalid command");
			}
		});
		
		Thread communicatorThread = new Thread() {
			@Override
			public void run() {
				try {
					communicator.run();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		
		communicatorThread.start();
		
/*		System.out.println("MAIN: Cekam...");
		Thread.currentThread().sleep(15000);
		
		System.out.println("MAIN: Uz se, kurva, chystam to ukoncit.");
		
		while (communicator.hasOpenedConnection()) {
			Thread.currentThread().sleep(100);
		}
		
		System.out.println("MAIN: Ukoncuju to, kurva...");
		communicatorThread.interrupt();*/
		
		// Player usage
/*		Player player = new Player();
		
		player.setSourceLocation("/home/tom/Music/01-krucipusk-druide-mcz.mp3");
		player.setCurrentVolume(1.0f);
		player.play();
		
		Thread.currentThread().sleep(5000);
		
		player.pause();
		
		Thread.currentThread().sleep(2000);
		
		player.play();*/
		
		// MusicLibrary usage
/*		long t0 = System.currentTimeMillis();
		
		MusicLibrary musicLibrary = new MusicLibrary();
		
		System.out.println("Building music library...");
		musicLibrary.buildLibrary(new File("/home/tom/Music/"));
		
		System.out.println("Building XML...");
		musicLibrary.buildXML();
		
		System.out.println("Serializing...");
		musicLibrary.serialize("MusicLibrary.xml");
		
		System.out.println("Done!");
		System.out.printf("Total time: %f seconds", (System.currentTimeMillis() - t0) / 1000.0f);*/
	}
}
