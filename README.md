# SimpleWebProxy
A simple HTTP/1.0 proxy written in Java. 
This is written for a homework of my network course for learning purpose.

### How to run
```sh
cd src/
javac *
# java WebProxy <port>
java WebProxy 8080
```

### What does it support
* HTTP/1.0 GET, HEAD, POST, PUT, etc
* Handling small objects, large objects and complex web pages with multiple objects
* Handling concurrent requests
* Caching with conditional requests

### License
SimpleWebProxy is released under [MIT License](http://www.opensource.org/licenses/MIT)
