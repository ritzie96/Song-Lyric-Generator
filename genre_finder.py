from urllib.request import urlopen
import urllib
import json
import os 
import time
import numpy as np
from django.utils.http import urlquote


out_dict = {}
artists = []
api_key = "xxxxx"

with open('all-artists.json', encoding = 'UTF-8') as data_file:    
    data = json.load(data_file)

for artist in data:

	artists.append(artist)


for i in range(len(artists)):
	url = "http://ws.audioscrobbler.com/2.0/?method=artist.gettoptags&artist="+ urlquote(artists[i]) +"&api_key=" +api_key+ "&format=json"

	try: 
		content = urlopen(url).read()
		d = json.loads(content)
		if 'toptags' in d:

			if 'tag' in d['toptags'] and len(d['toptags']['tag']) >  0:

				x = d['toptags']['tag'][0]['name']

			else: 
				x = "NULL"

		else: 
			x = "NULL"

		print(x)
		out_dict[artists[i]] = x

	except urllib.error.HTTPError:

  		print ("'s error code is")
  		out_dict[artists[i]] = "NULL"

	time.sleep(0.25) # last fm have a limit of 5 requerst every second from same ip

with open('data.txt', 'w') as outfile:
	json.dump(out_dict, outfile)

