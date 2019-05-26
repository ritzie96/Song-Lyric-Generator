import json, os
from elasticsearch import Elasticsearch

es = Elasticsearch([{'host': 'localhost', 'port': '9200'}])

i = 0
directory = "Songs/"

for filename in os.listdir(directory):
    if filename.endswith(".json"):
        f = open(directory + filename)

        docket_content = f.read()

        es.index(index='myindex', ignore=400, doc_type='docket', id=i, body=json.loads(docket_content))
        i = i + 1
