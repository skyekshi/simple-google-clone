# Simple Google Clone Search Engine

In this project a naive search engine is created using HTML, CSS, PHP, Java, JavaSrcipt, and MySQL. Four components are implemented:

- A spider (or called a crawler) function to fetch pages recursively from a given web site
- An indexer which extracts keywords from a page and inserts them into an inverted file
- A retrieval function that compares a list of query terms against the inverted file and returns the top documents to the user in a ranked order according to the vector space model
- A web interface that accepts a user query in a text box, submits the query to the search engine, and displays the returned results to the user

Due to resource limitation only a small set of websites are included. With more computing power, time, and space, and probably more complicated indexer (which I used Porter's Algorithm to enhance performance), and more fine-tuned retrieval function (modern models are not limited to the standard vector space comparison), a more powerful search engine can be created. Moreover, Google does a better job in crawling.

A demonstration on local computer is possible with XAMPP server. However, one can always make their own choice. I named my site <em>SearchWise</em> and styled it like Google. Below is the sample interface:

![Start page](/assets/images/interface1.png)
![Result page Top](/assets/images/interface2.png)
...
![Result page Bottom](/assets/images/interface3.png)