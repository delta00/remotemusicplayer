package tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.farng.mp3.MP3File;
import org.farng.mp3.TagException;
import org.farng.mp3.id3.ID3v1;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>
 * This class provides some functions for managing of music library:
 * <ul>
 * 	<li><b>Build</b> music library from files in defined directory. </li>
 * 	<li><b>Export</b> music library to XML.</li>
 * 	<li><b>Save</b> music library to a file.</li>
 * 	<li><b>Transform</b> library into network transmittable format.</li>
 * </ul>
 * </p>
 * <p>
 * 	In the moment, only MP3 files are supported due to importance of using
 * 	different kinds of 3rd party libraries for each file format.
 * </p>
 * <p>
 * 	<h1>How to use this library:</h1>
 * 		<ul>
 * 			<li><h2>First time: creation of the library.</h2></li>
 *	 			<ol>
 * 					<li>Use method <b>buildLibrary()</b> with some directory as a parameter.</li>
 * 					<li>Use method <b>buildXML()</b> to generate XML document describing the library.</li>
 * 					<li>If you want to, use method <b>serialize()</b> to store the result somewhere.</li>
 * 					<li>If you want to, use method <b>getTransmittableData()</b> for sending data over the network.</li>
 * 				</ol>
 * 			<li><h2>Second time: update of the library (not supported yet).</h2></li>
 * 		</ul>
 * </p>
 * 
 * @author		Tomáš Zíma
 * @see			#buildLibrary(File)
 * @see			#buildXML()
 * @see			#serialize(String)
 * @see			#getTransmittableData()
 */
public class MusicLibrary {
	/**
	 * Contains all artists. This attribute makes the root element of whole
	 * tree with music library.
	 * 
	 * @see Artist
	 */
	private List<Artist>	artists		= new ArrayList<>();
	
	/**
	 * Represents XML document, which contains all informations from the
	 * music library. This attribute will be set only if {@link #buildXML()}
	 * was called previously. In other case, it'll be null.
	 */
	private Document		xmlDocument	= null;
	
	/**
	 * <p>
	 * 	Version number of the music library. It's used for checking if XML
	 * 	describing the library stored in computer is the same as the XML
	 * 	which is stored onto the client device.
	 * </p>
	 * 
	 * <p>
	 * 	Therefore, this number have to be generated each time the library is
	 * 	generated or updated.
	 * </p>
	 */
	private long			version		= 0;

	/**
	 * Simple class, which contains all informations about a single artist.
	 */
	protected class Artist {
		/** Contains all albums from the artist. */
		private List<Album>	albums	= new ArrayList<>();
		
		/** Name of the artist. */
		private String		name	= "";

		/**
		 * Creates a new artist with specified name.
		 * 
		 * @param name
		 * 	Name of the artist.
		 */
		public Artist(String name) {
			this.name = name;
		}
		
		/**
		 * Checks if informations in the given object are the
		 * same as informations stored in this object.
		 * 
		 * @param artist
		 * 	Object to be compared with.
		 * 
		 * @return
		 * 	If informations in both objects are the same, true will
		 * 	be returned. False will be returned in the other case.
		 */
		public boolean equals(Artist artist) {
			return (name.equals(artist.name));
		}
		
		/**
		 * Goes trough all albums of this artist. If it finds album,
		 * which is exactly the same as given album (same name and
		 * year of publication), it'll return the existing album
		 * (which might already contain some songs). If any matching
		 * album wasn't found, it'll create a new album.
		 * 
		 * @param album
		 * 	Informations about album.
		 * 
		 * @return
		 * 	Existing album if found, or a given parameter (now included
		 * 	into the list of artist's albums).
		 */
		protected Album getAlbum(Album album) {
			for (Album tmpAlbum : albums) {
				if (tmpAlbum.equals(album)) {
					return tmpAlbum;
				}
			}
			
			albums.add(album);
			return album;
		}
		
		/**
		 * @return
		 * 	Name of the artist.
		 */
		public String getName() {
			return name;
		}
	}

	/**
	 * Simple class, which contains all informations about
	 * a single album.
	 */
	protected class Album {
		/** Contains all songs from the album. */
		private List<Song>	songs	= new ArrayList<>();
		
		/** Name of album. */
		private String		name	= "";
		
		/** Year of publication. */
		private String		year	= "";
		
		/**
		 * Creates record about an album with specified
		 * informations.
		 * 
		 * @param name
		 * 	Name of album.
		 * 
		 * @param year
		 * 	Year of publication.
		 */
		public Album(String name, String year) {
			this.name	= name;
			this.year	= year;
		}

		/**
		 * Checks if informations about this album are same
		 * as informations about the given album.
		 * 
		 * @param album
		 * 	Informations about album.
		 * 
		 * @return
		 * 	True if informations are exactly the same, or false
		 * 	if informations doesn't match.
		 */
		public boolean equals(Album album) {
			return (name.equals(album.name) && year.equals(album.year));
		}
		
		/**
		 * <p>
		 * 	Inserts a song into the album. It's immediately sorting all
		 * 	songs in the order as they're on the album (if there's an
		 * 	ID3 tag about track number).
		 * </p>
		 * 
		 * <p>
		 * 	Therefore, after each call of this method, list of {@link #songs}
		 * 	will be sorted by track number. It tries to be as fast as possible.
		 * </p>
		 * 
		 * @param song
		 * 	Informations about the song.
		 */
		public void addSong(Song song) {
			for (int i = 0; i < songs.size(); i++) {
				if (Integer.valueOf(songs.get(i).trackNumber) >= Integer.valueOf(song.trackNumber)) {
					songs.add(i, song);
					return;
				}
			}
			
			songs.add(song);
		}
		
		/**
		 * @return
		 * 	Name of the album.
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * @return
		 * 	Year of publication.
		 */
		public String getYear() {
			return year;
		}
	}

	/**
	 * Simple class, which contains all informations about a song.
	 */
	protected class Song {
		/** Name of song. */
		private String	name		= "";
		
		/** Absolute path to the file with a song. */
		private String	path		= "";
		
		/** Number of the track on an album. */
		private String	trackNumber	= "";
		
		/**
		 * Creates record about a new song.
		 *  
		 * @param name
		 * 	Name of the song.
		 * 
		 * @param path
		 * 	Absolute path to the file with a song.
		 * 
		 * @param trackNumber
		 * 	Number of the track on an album.
		 */
		public Song(String name, String path, String trackNumber) {
			this.name			= name;
			this.path			= path;
			this.trackNumber	= trackNumber;
		}
		
		/**
		 * @return
		 * 	Name of the song.
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * @return
		 * 	Absolute path to the file with a song.
		 */
		public String getPath() {
			return path;
		}
		
		/**
		 * @return
		 * 	Number of the track on an album.
		 */
		public String getTrackNumber() {
			return trackNumber;
		}
	}
	
	/**
	 * Tries to find matching record to the given record in
	 * list of all artists. If it can't be found, it'll be
	 * inserted into the list. In other case, existing item
	 * will be returned.
	 *  
	 * @param artist
	 * 	Informations about the artist.
	 * 
	 * @return
	 * 	Already inserted instance of an artist, which might
	 * 	possibly contains some songs already. However, it might
	 * 	be the same object as the parameter (if artist couldn't
	 * 	be found, so it was inserted into the list instead).
	 */
	protected Artist getArtist(Artist artist) {
		for (Artist tmpArtist : artists) {
			if (tmpArtist.equals(artist)) {
				return tmpArtist;
			}
		}
		
		artists.add(artist);
		return artist;
	}
	
	/**
	 * <p>
	 * 	Goes trough all files in the given directory and list all
	 * 	musician files contained in that (note: only MP3 are
	 * 	currently supported).
	 * </p>
	 * 
	 * <p>
	 * 	After that, each file will be scanned for ID3 tags and based
	 * 	on such a informations, music library will be finally created. 
	 * </p>
	 * 
	 * <p>
	 * 	<h1>Music library (internally)</h1>
	 * 	<p>
	 * 		The music library is represented as a tree, where you've list
	 * 		of all artists as the 1st accessible element. Each artist
	 * 		contains list of all albums and each albums contains list of
	 * 		all songs.
	 * 	</p>
	 * 	<p>
	 * 		Therefore, to insert (or find) some element, you need to find
	 * 		an element which is by-one-level higher. To find a song, you
	 * 		need to get an album. To find an album, you need to find an
	 * 		artist. To find an artist, you need to go trough the list of
	 * 		all artists.
	 * 	</p>
	 * </p>
	 * 
	 * @param rootDirectory
	 * 	The directory with music files. You can simply do this by something
	 * 	like: <pre>new File("/home/user/Music/");</pre>
	 */
	public void buildLibrary(File rootDirectory) {
		// Gets list of all MP3 files in the given directory.
		File[] subFiles = rootDirectory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return (
					file.isDirectory() ||
					file.getAbsolutePath().endsWith(".mp3")
				);
			}
		});
		
		// With each file from the directory:
		//	- Is it a directory? Call this method with that directory.
		//	- Is it a file?
		//		- Is it possible to read ID3 tags?
		//			- Yes:
		//				- Read informations
		//				- Find an artist and album
		//				- Insert song onto the album
		//			- No:
		//				- Place the song into the special category:
		//					- (unknown artist) : (unknown album) : name_of_file_.mp3
		for (File tmpSubFile : subFiles) {
			if (tmpSubFile.isDirectory()) {
				buildLibrary(tmpSubFile);
			}
			else {
				try {
					MP3File	mp3File	= new MP3File(tmpSubFile);
					ID3v1	id3v1	= mp3File.getID3v1Tag();

					Artist	artist	= new Artist(id3v1.getArtist());
					Album	album	= new Album(id3v1.getAlbum(), id3v1.getYear());
					Song	song	= new Song(id3v1.getSongTitle(), mp3File.getMp3file().getAbsolutePath(), id3v1.getTrackNumberOnAlbum());

					artist			= getArtist(artist);
					album			= artist.getAlbum(album);
					
					album.addSong(song);
				} catch (IOException |TagException e) {
					continue;
				} catch (UnsupportedOperationException | NullPointerException e) {
					Artist	artist	= new Artist("(unknown artist)");
					Album	album	= new Album("(unknown album)", "");
					Song	song	= new Song(tmpSubFile.getName(), tmpSubFile.getAbsolutePath(), "1");
					
					artist			= getArtist(artist);
					album			= artist.getAlbum(album);
					
					album.addSong(song);
				}
			}
		}
		
		updateVersionNumber();
	}
	
	/**
	 * <p>
	 * 	Goes trough all the records in the database (artists -> albums -> songs) and places
	 * 	them into the XML document. Remember, that you need to firstly load library into the
	 * 	memory (e.g.: by calling {@link #buildLibrary(File)}).
	 * </p>
	 * 
	 * <p>
	 * 	Produced XML file has following structure:
	 * 
	 * 	<pre>
	 * 		{@code
	 * 			<musicLibrary version="9201873486367996336">
	 * 				<artist name="Fear Factory">
	 * 					<album name="Demanufacture" year="1995">
	 * 						<song name="Demanufacture" track="01" path="/home/user/Music/Demanufacture.mp3">
	 * 						<song name="Replica" track="04" path="/home/user/Music/FearFactory/Replica.mp3">
	 * 					</album>
	 * 				</artist>
	 * 				<artist name="Sybreed">
	 * 					<album name="Slave design">
	 * 						<song name="Bioactive" track="01" path="/home/user/Music/Sybreed/SlaveDesign/Bioactive.mp3">
	 * 					</album>
	 * 				</artist>
	 * 			</musicLibrary>
	 * 		}
	 * 	</pre>
	 * </p>
	 * 
	 * 
	 * @throws Exception
	 * 	Due to the work with XML document, some error may occur and exception (by Java API)
	 * 	will be thrown. However, these kinds of exception can't be handled by this method,
	 * 	neither by user of this method. Therefore, instead of defining plenty of exceptions
	 * 	via the <i>throws</i> clause, only general Exception is specified.
	 */
	public void buildXML() throws Exception {
		xmlDocument			= DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		Element rootElement	= xmlDocument.createElement("musicLibrary");

		for (Artist artist : artists) {
			Element elementArtist = xmlDocument.createElement("artist");
			elementArtist.setAttribute("name", artist.getName());
			
			rootElement.appendChild(elementArtist);
			
			for (Album album : artist.albums) {
				Element elementAlbum = xmlDocument.createElement("album");
				elementAlbum.setAttribute("name", album.getName());
				elementAlbum.setAttribute("year", album.getYear());
				
				elementArtist.appendChild(elementAlbum);
				
				for (Song song : album.songs) {
					Element elementSong = xmlDocument.createElement("song");
					elementSong.setAttribute("name", song.getName());
					elementSong.setAttribute("track", song.getTrackNumber());
					elementSong.setAttribute("filename", song.getPath());
					
					elementAlbum.appendChild(elementSong);
				}
			}
		}
		
		rootElement.setAttribute("version", String.valueOf(version));
		xmlDocument.appendChild(rootElement);
	}
	
	/**
	 * <p>
	 * 	Saves the music library (as a XML) into the the local file.
	 * </p>
	 * 
	 * <p>
	 * 	Remember, that you need to firstly build a library (for example by calling of a
	 * 	{@link #buildLibrary(File)}) and also build a XML (by calling of a {@link #buildXML()}). 
	 * 	It won't work without previous call of these methods!
	 * </p>
	 * 
	 * @param filename
	 * 	Path to the file (and its name) where the result should be stored.
	 * 
	 * @throws Exception
	 * 	Due to the work with XML document, some error may occur and exception (by Java API)
	 * 	will be thrown. However, these kinds of exception can't be handled by this method,
	 * 	neither by user of this method. Therefore, instead of defining plenty of exceptions
	 * 	via the <i>throws</i> clause, only general Exception is specified.
	 */
	public void serialize(String filename) throws Exception {
		TransformerFactory.
			newInstance().
				newTransformer().
					transform(
						new DOMSource(xmlDocument),
						new StreamResult(new File(filename))
					);
	}
	
	/**
	 * <p>
	 * 	Transforms the XML document into array of bytes. This format is much more useful
	 * 	for transmission over the network.
	 * </p>
	 * 
	 * <p>
	 * 	Remember, that you need to firstly build a library (for example by calling of a
	 * 	{@link #buildLibrary(File)}) and also build a XML (by calling of a {@link #buildXML()}). 
	 * 	It won't work without previous call of these methods!
	 * </p>
	 * 
	 * @return
	 * 	Array of bytes containing whole XML document with music library.
	 * 
	 * @throws Exception
	 * 	Due to the work with XML document, some error may occur and exception (by Java API)
	 * 	will be thrown. However, these kinds of exception can't be handled by this method,
	 * 	neither by user of this method. Therefore, instead of defining plenty of exceptions
	 * 	via the <i>throws</i> clause, only general Exception is specified.
	 */
	public byte[] getTransmittableData() throws Exception {
		ByteArrayOutputStream	byteArrayOutputStream	= new ByteArrayOutputStream();
		StreamResult			streamResult			= new StreamResult(byteArrayOutputStream);
		
		TransformerFactory.
		newInstance().
			newTransformer().
				transform(
					new DOMSource(xmlDocument),
					streamResult
				);
		
		return byteArrayOutputStream.toByteArray();
	}
	
	/**
	 * Generates new (random) version number.
	 * 
	 * @see #version
	 */
	private void updateVersionNumber() {
		version = new Random().nextLong();
	}
	
	/**
	 * @return
	 * 	Current version number.
	 * 
	 * @see #version
	 */
	public long getVersionNumber() {
		return version;
	}

	/**
	 * Resets all internal values to its defaults. 
	 */
	public void clear() {
		this.artists		= new ArrayList<>();
		this.xmlDocument	= null;
		this.version		= 0;
	}
}
