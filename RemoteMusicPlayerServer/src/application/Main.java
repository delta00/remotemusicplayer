package application;

import java.io.File;

import tools.MusicLibrary;

public class Main {
	public static void main(String[] args) throws Exception {
		long t0 = System.currentTimeMillis();
		
		MusicLibrary musicLibrary = new MusicLibrary();
		
		System.out.println("Building music library...");
		musicLibrary.buildLibrary(new File("/home/tom/Music/"));
		
		System.out.println("Building XML...");
		musicLibrary.buildXML();
		
		System.out.println("Serializing...");
		musicLibrary.serialize("MusicLibrary.xml");
		
		System.out.println("Done!");
		System.out.printf("Total time: %f seconds", (System.currentTimeMillis() - t0) / 1000.0f);
	}
}
