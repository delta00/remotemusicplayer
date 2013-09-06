package application;

import maryb.player.Player;

public class Main {
	public static void main(String[] args) throws Exception {
		Player player = new Player();
		
		player.setSourceLocation("/home/tom/Music/01-krucipusk-druide-mcz.mp3");
		player.setCurrentVolume(1.0f);
		player.play();
		
		Thread.currentThread().sleep(5000);
		
		player.pause();
		
		Thread.currentThread().sleep(2000);
		
		player.play();
		
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
