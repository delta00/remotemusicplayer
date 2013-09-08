package tools.communicator;

/**
 * Logic-pure class, which contains informations about current state of the
 * player.
 * 
 * @author Tomáš Zíma
 */
public class PlayerState {
	private boolean	playing		= false;
	private String	artist		= "";
	private String	album		= "";
	private String	song		= "";
	private long	length		= 0;
	private long	position	= 0;
	
	/**
	 * Use this constructor if the player is playing or paused. If player is stopped,
	 * just use {@link #PlayerState()}.
	 * 
	 * @param playing
	 * 	Set to true if player is currently playing, or to false if it's not.
	 * 
	 * @param artist
	 * 	Name of the artist.
	 * 
	 * @param album
	 * 	Name of the album.
	 * 
	 * @param song
	 * 	Name of the song.
	 * 
	 * @param length
	 * 	Total length of the song in seconds.
	 * 
	 * @param position
	 * 	Current position in the song in seconds.
	 */
	public PlayerState(boolean playing, String artist, String album, String song, long length, long position) {
		this.playing	= playing;
		this.artist		= artist;
		this.album		= album;
		this.song		= song;
		this.length		= length;
		this.position	= position;
	}
	
	/**
	 * Use this constructor if player is stopped. Value "playing" will be set to
	 * false and all other values will be left empty.
	 */
	public PlayerState() {
		this.playing = false;
	}
	
	/**
	 * @return
	 * 	True if player is playing, false if it's paused or stopped.
	 */
	public boolean isPlaying() {
		return playing;
	}

	/**
	 * @return
	 * 	Name of the artist.
	 */
	public String getArtist() {
		return artist;
	}

	/**
	 * @return
	 * 	Name of the album.
	 */
	public String getAlbum() {
		return album;
	}

	/**
	 * @return
	 * 	Name of the song.
	 */
	public String getSong() {
		return song;
	}

	/**
	 * @return
	 * 	Total length of the song in seconds.
	 */
	public long getLength() {
		return length;
	}

	/**
	 * @return
	 * 	Current position in the song in seconds from beginning.
	 */
	public long getPosition() {
		return position;
	}
}
