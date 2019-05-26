from elasticsearch import helpers, Elasticsearch
from elasticsearch_dsl import Search
from collections import Counter, defaultdict
import nltk
import random
import sys
import string
import ngram

class ngram1:
    #Settings
    # Sentence length = sen_length ± b_sen_length
    # in generated text
    sen_length = 7
    b_sen_length = 3
    #How many results to use from the database
    #to form the corpus
    corpus_size = 1000
    
    def __init__(self):
        self.es = Elasticsearch()

    def predict(self, input_lyrics, choosen_genre = None):
    
        input_lyrics = (' ').join(self.clean_text(input_lyrics))
        #search in elastic for the lyrics that will form our 
        #new corpus from which we will generate ngram
        if choosen_genre is not None:
            s = Search(using=self.es, index="lyrics") \
                .query("match", lyrics=input_lyrics) \
                .filter("term", genre=choosen_genre)
        else:
            s = Search(using=self.es, index="lyrics") \
                .query("match", lyrics=input_lyrics)

        #choose how many results we want
        s = s[:self.corpus_size]

        response = s.execute()

        #If there are less then 5 responses then 
        #the genre problably does not exist
        if len(response) < 5:
            print("Less than 5 results!\nQuitting")
            return None

        #Generate corpus
        corpus = ''
        for hit in response:
            corpus += hit.lyrics

        corpus = (self.clean_text(corpus))
        tokens = corpus
        three_g = nltk.ngrams(tokens, 3)
        #Create an index from 3-gram of words
        d_three_g = {}
        for (a,b,c),v in (Counter(three_g)).items():
            if a not in d_three_g:
                d_three_g[a] = []
            d_three_g[a].append((b,c,v))
        
        for k,v in d_three_g.items():
            d_three_g[k].sort(key=lambda tup: tup[2], reverse=True)

        input_tokens = input_lyrics.split(' ')
        sentence = []
        #Take the first four words and generate from them
        for i in input_tokens[:10]:
            token_word = i
            if token_word not in d_three_g:
                continue
            sentence_bound = random.randrange(-self.b_sen_length,self.b_sen_length)
            for _ in range(self.sen_length + sentence_bound):
                (w1,w2,_) = random.choice(d_three_g[token_word][:10])
                sentence.append(w1)
                sentence.append(w2)
                token_word = w2
            sentence.append('\n')
        return (' '.join(sentence))
 
    '''
    Returns a cleanup version of all lyrics in our corpus 
    Basically what is allowed is all lowercase letters, numbers
    and a few special chars
    '''
    def clean_text(self, text):
        text = text.lower()
        #filter out unwanted words
        unwanted_words = ['chorus', 'verse', 'intro', 'outro']
        for uw in unwanted_words:
            text.replace(uw, '')
        #Generate valid chars
        valid_chars = string.ascii_lowercase
        valid_chars += "'"
        # valid_chars += "0123456789'!?()-,\n"
        filtered = [ x if x in valid_chars else ' ' for x in text ]
        return ''.join(filtered).split(' ')

'''
Code for testing, running just this file
'''
if __name__ == '__main__':
    n = ngram1()
    s = n.predict( "I am a reader play artist" , "pop" )
    print(s)
