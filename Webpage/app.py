import ngram1 
from flask import Flask, flash, redirect, render_template, \
     request, url_for

data_option=[{'name':'rock'}, {'name':'pop'}, {'name':'jazz'}]
input_query = ""
genre = ""

app = Flask(__name__)
@app.route("/")

def main():
    #Starting page
    return render_template('index.html', data=data_option, result = "", query = "")

@app.route("/search" , methods=['GET', 'POST'])
def search():
    genre = request.form.get('comp_select')
    input_query = request.form.get('Query')
    #There was no query
    if input_query is None or len(input_query) == 0:
        return render_template('index.html', data=data_option, result = "", query = "")
    genre = genre.lower()
    n = ngram1.ngram1()
    s = n.predict(input_query, genre)
    if s is None:
        return render_template('index.html', data=data_option, result = "ERROR\nNo lyrics to work with", query = input_query)
    #Removes double whitespace in the returned lyrics, and uppercases the first letter in
    #each sentence
    ll = s.split('\n')
    for i in range(len(ll)):
        ll[i] = ll[i].strip()
        ll[i] = ' '.join(ll[i].split())
        if len(ll[i]) > 1:
            ll[i] = ll[i][0].upper() + ll[i][1:]
    s = '\n'.join(ll)
    #Render the starting page with the results
    return render_template("index.html", data=data_option, result = s, query = input_query)

if __name__ == '__main__':
	app.run()
