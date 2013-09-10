package application;

import java.io.IOException;

import application.controller.Controller;
import application.controller.ControllerErrorListener;

import tools.communicator.Communicator;
import tools.communicator.ConnectionDescriptor;
import tools.communicator.ConnectionListener;
import tools.communicator.PlayerState;

public class Main {
	public static void main(String[] args) throws Exception {
		final Controller controller = new Controller();
		
		controller.setErrorListener(new ControllerErrorListener() {
			@Override
			public void addIOException(IOException exception) {
				System.err.println("Something goes horribly wrong! Trying again in 3 seconds...");
				
				try {
					Thread.currentThread().sleep(3000);
				} catch (InterruptedException e) {
				}
				
				controller.run();
			}
		});

		controller.run();
		
		// Communicator usage
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
