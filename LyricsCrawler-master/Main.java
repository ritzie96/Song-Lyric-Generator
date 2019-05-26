import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Map.Entry;
import org.json.simple.*;
import org.json.simple.parser.*;

/**
 *	AZLyrics.com scraper. Scrapes for lyrics and puts them in individual JSON files.
 *	Highly unethical and probably illegal if this were to be used for something other
 *	than academic purposes. Written for course DD2476 Information Retrieval and Search
 *	Engines.
 *
 *	@author Simon Mossmyr (Project group with Jonas Wedin, Carl Jernbäcker and Rithika Harish Kumar)
 *	@version 2018-05-10
 */
public class Main {



	/* <------------------------------- Constants -------------------------------> */

	

	static final String ARTIST_LIST_FP						= "all-artists.json";
	static final String ARTIST_SONG_LIST_DIR_PATH			= "songs-by-artist";
	static final String SONGS_DB_DIR_PATH					= "all-songs";

	static final int 	SLEEP_LOW							= 5000;
	static final int 	SLEEP_HIGH /*420*/					= 20000;

	static final String AZLYRICS_BASE_URL 					= "https://www.azlyrics.com/";
	static final String ARTIST_LINK_IN_HTML_PATTERN			= "\\<a href=\"(\\w{1,2}\\/[\\w\\-]+\\.html)\"\\>([\\p{L}\\w\\s\\,\\.\\(\\)&;\\-'!\\/]+)\\<\\/a\\>";
	static final String ARTIST_NAME_IN_LINK_PATTERN			= "\\>[\\w\\s,\\.&;\\/\\-\\+]+\\<";
	static final String ARTIST_URL_IN_LINK_PATTERN			= "[a-z]\\/[a-z]+\\.html";
	static final String ARTIST_FILE_NAME_FROM_URL_PATTERN	= "https:\\/\\/www.azlyrics.com\\/(\\w+)\\/([\\w\\-]+)\\.html";



	/* <------------------------------- Globals -------------------------------> */



	static ArrayList<Artist> allArtists = new ArrayList<>();
	static Random rand = new Random();



	/* <------------------------------- Helper classes -------------------------------> */



	/**
	 *	
	 */
	private static class Artist {
		public String url;
		public String name;
		public Artist(String extendedUrl, String name) {
			this.url = AZLYRICS_BASE_URL + extendedUrl;
			this.name = name;
		}
	}



	/* <------------------------------- Constructors -------------------------------> */



	/**
	 *	
	 */
	public Main() {}



	/* <------------------------------- Helper methods -------------------------------> */



	/**
	 *	Generates a UNIX-friendly file/dir name from a song URL.
	 */
	private static String getUnixFriendlySongNameFromUrl(String url) {
		Pattern p = Pattern.compile("https:\\/\\/www\\.azlyrics\\.com\\/lyrics\\/[\\w-]+\\/([\\w-]+).html");
		Matcher m = p.matcher(url);
		m.find();
		return m.group(1);
	}
	/**
	 *	Generates a UNIX-friendly file/dir name from an artist URL.
	 *	E.g. https://www.azlyrics.com/l/liljontheeastsideboyz.html becomes l-liljontheeastsideboyz.json
	 */
	private static String getUnixFriendlyArtistNameFromUrl(String url) {
		Pattern p = Pattern.compile(ARTIST_FILE_NAME_FROM_URL_PATTERN);	
		Matcher m = p.matcher(url);
		m.find();
		String letter = m.group(1);
		String artist = m.group(2);
		return letter+"-"+artist;
	}

	/**
	 *	Sleeps for a random amount of time.
	 */
	private static void sleep() {
		try {
			Thread.sleep( (int) (((SLEEP_HIGH - SLEEP_LOW) * rand.nextDouble()) + SLEEP_LOW));
		}
		catch (InterruptedException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
	/**
	 *	
	 */
	private static String getLyricsInSong(String html) {
		Matcher m = Pattern.compile("that\\. --\\>(.+)\\<\\/div\\>\\<br\\>\\<br\\>\\<!\\-\\- MxM").matcher(html);
		m.find();

		return m.group(1)
			.replace("<br>","\n")
			.replace("<i>","")
			.replace("</i>","")
			.replace("<b>","")
			.replace("</b>","");
	}

	/**
	 *	
	 */
	private static HashMap<String, String> getSongUrlsInArtist(String html) {
		Pattern p = Pattern.compile("<a href=\"\\.\\.\\/(lyrics\\/[\\w\\-]+\\/[\\w\\-]+\\.html)\" target=\"_blank\"\\>([^\\<]+)\\<\\/a\\>");
		Matcher m = p.matcher(html);
		HashMap<String, String> ret = new HashMap<>();
		while(m.find()) {
			ret.put(m.group(2), AZLYRICS_BASE_URL + m.group(1));
		}
		return ret;
	}

	/**
	 *	
	 */
	private static void findArtistsInLetter(String html) {
		Pattern p = Pattern.compile(ARTIST_LINK_IN_HTML_PATTERN);
		Matcher m = p.matcher(html);
		while (m.find()) {
			allArtists.add(new Artist(m.group(1), m.group(2)));
		}
	}

	/**
	 *	Returns a String object of raw HTML code from URL.
	 */
	private static String getHtml(String urlToRead) {
		try {
        	sleep();
			StringBuilder result = new StringBuilder();
			URL url = new URL(urlToRead);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			rd.close();
			return result.toString();
		}
		catch (MalformedURLException e1) {
			System.out.println(e1.getMessage());
			e1.printStackTrace();
			System.exit(1);
			return null;
		}
		catch (IOException e2) {
			System.out.println(e2.getMessage());
			e2.printStackTrace();
			System.exit(1);
			return null;
		}
	}



	/* <------------------------------- Main -------------------------------> */



	/**
	 *	
	 */
	public static void main(String args[]) {
		System.out.println("Starting");

		/** 
		 *	The scraper works in three steps: 
		 *	1: Scrape for artists for every letter
		 *	2: Scrape for songs by every artist
		 *	3: Scrape for lyrics for every song
		 */

		//	Define the artists-by-alphabet list
		HashMap<Character, String> letter2url = new HashMap<>();
		char[] letters = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
		for (char c : letters) {
			letter2url.put(c, AZLYRICS_BASE_URL+c+".html");
		}
		letter2url.put('#', AZLYRICS_BASE_URL+"19.html");


		//	Step 1: Load the artists JSON file into memory, or scrape it if it doesn't exist. 
		File f = new File(ARTIST_LIST_FP);
		if (!f.exists() || f.isDirectory()) {
			System.out.println("Scraping for artists");

			int prevSize = 0;
			for (Entry<Character, String> e : letter2url.entrySet()) {
				Character letter = e.getKey();
				System.out.println("    Scraping letter "+letter);
				String letterUrl = e.getValue();
				String letterHtml = getHtml(letterUrl);
				findArtistsInLetter(letterHtml);
				System.out.println("    Found "+(allArtists.size()-prevSize)+" artists. Now sleeping.");
				prevSize = allArtists.size();
			}

			System.out.println("    Done. Found "+allArtists.size()+" artists. Printing to file "+ARTIST_LIST_FP);

			JSONObject artistsJson = new JSONObject();
			for (Artist a : allArtists) {
				artistsJson.put(a.name, a.url);
			}

			try {
				FileWriter fw = new FileWriter(ARTIST_LIST_FP);
				fw.write(artistsJson.toJSONString());
				fw.flush();
			}
			catch (IOException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
				System.exit(1);
			}
		}
		System.out.println("Reading artists file into memory");
		JSONParser parser = new JSONParser();
		JSONObject artistsJson = new JSONObject();
		try {
			artistsJson = (JSONObject) parser.parse(new FileReader(ARTIST_LIST_FP));
		}
        catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.exit(1);
        } 



        //	Step 2: Scrape for artist songs, if not scraped for already
        Set<Entry> es = artistsJson.entrySet();
        System.out.println("Scraping for songs by "+es.size()+" artists");
        for (Entry e : (Set<Entry>) artistsJson.entrySet()) {
        	//	Find song list file of the artist
        	String artistName = e.getKey().toString();        	
        	String artistUrl = e.getValue().toString();
        	String artistFileName = getUnixFriendlyArtistNameFromUrl(artistUrl);
        	new File(ARTIST_SONG_LIST_DIR_PATH).mkdir();
        	f = new File(ARTIST_SONG_LIST_DIR_PATH, artistFileName+".json");

        	//	If it doesn't exist, scrape it!
        	if (!f.exists() || f.isDirectory()) {
        		System.out.println("Scraping for song list by "+artistName);

        		//	Scrape
        		String html = getHtml(artistUrl);
        		HashMap<String, String> songUrls = getSongUrlsInArtist(html);

        		//	Create JSON object of song urls
        		JSONObject songsJson = new JSONObject();
        		for (Entry s : songUrls.entrySet()) {
        			songsJson.put(s.getKey(), s.getValue());
        		}

        		//	Write songs list to JSON file
        		try {
        			FileWriter fw = new FileWriter(f);
        			songsJson.writeJSONString(fw);
        			fw.flush();
        		}
        		catch (Exception ex) {
					System.out.println(ex.getMessage());
					ex.printStackTrace();
					System.exit(1);
        		}
        	}

        	//	Step 3: Scrape for every song by artist
        	JSONObject songsJson = null;
        	try {
        		songsJson = (JSONObject) parser.parse(new FileReader(f));
        	}
        	catch (Exception ex) {
				System.out.println(ex.getMessage());
				ex.printStackTrace();
				System.exit(1);
        	}

        	//	For every song by artist...
			Set<Entry> songsEntrySet = songsJson.entrySet();      	
			for (Entry song : (Set<Entry>) songsEntrySet) {
				//	Find song meta data
				String songName = song.getKey().toString();
				String songUrl = song.getValue().toString();
				String songFileName = getUnixFriendlySongNameFromUrl(songUrl);
				String songFileDir = SONGS_DB_DIR_PATH + "/" + artistFileName;
				new File(songFileDir).mkdirs();
				File songFile = new File(songFileDir, songFileName + ".json");

				//	If the file doesn't exist, scrape for it
				if (!songFile.exists() || songFile.isDirectory()) {
					System.err.println("Scraping for song "+songName+" by "+artistName+" at "+songUrl);
					String html = getHtml(songUrl);
					String songLyrics = getLyricsInSong(html);
					JSONObject songJson = new JSONObject();
					songJson.put("name", songName);
					songJson.put("artist", artistName);
					songJson.put("url", songUrl);
					songJson.put("lyrics", songLyrics);
					try {
						FileWriter songFw = new FileWriter(songFile);
						songJson.writeJSONString(songFw);
						songFw.flush();
					}
					catch (Exception ex) {
						System.out.println(ex.getMessage());
						ex.printStackTrace();
						System.exit(1);
					}
				}
			}
        }
	}
}