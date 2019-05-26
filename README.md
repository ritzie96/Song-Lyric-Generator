 About:
 
 A webpage is created where with a given input of lyrics, a new set of lyrics are generated. It is also made possible to filter with genre. A crawler is created which crawls the (https://www.azlyrics.com/) webpage for lyrics. Genres where collected from the last.fm https://www.last.fm/api/show/artist.getTopTags api. The data is indexed using elastic search with respect to lyrics, artist and genre. Based on the given input the lyrics are predicted using Ngram search technology  
 
 Prerequisites: 
 
 1) Download and unzip Elastic search (https://www.elastic.co/downloads/elasticsearch)
 2) For windows execute the file elasticsearch.bat (present in the bin folder) or Run curl http://localhost:9200/ or Invoke-RestMethod http://localhost:9200 with PowerShell
 3) Install Python3
 4) Via pip install these as well: flask, elasticsearch, elasticsearch_dsl, collections, nltk, ngram, django  
 
 Steps for execution:
 
 1a) Genre crawler needs to have a JSON file of artist to search for and an api key has to be created at last.fm/api. To execute file put in the api key and then execute the file with python3 -i genre_finder.py (-i is useful if the scraper would crash you could still save the progress).

 1b) Lyric scraper compile all java files then run the main class

 2) In the python terminal run the following command: python app.py ( file present in the folder \Webpage\)
 3) Check for a IP address in the terminal. Its something like (127.0.0.1.5000/)
 4) Copy the IP address and paste it in the URL section of any browser.
 5) Select genre, and input any song.
 6) New automated Lyrics are generated.
