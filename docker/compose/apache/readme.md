Apache server that routes all traffic to Tomcat.
------------------------------------------------

SSL
---
By default we enable SLL and re-route http-requests to https. To disable this simply comment out the line
 
    RUN a2enmod ssl
    
in the Dockerfile. You may also need to clear your browser cache, as previously visited urls may cache the 
redirect.

SSL uses a self-signed certificate valid for 10 years. Note that most browsers will complain about this
when you first visit the site (localhost or 127.0.0.1), but browsers will give you the possibility to 
continue and/or add the site to the list of exceptions.

Configuration:
--------------
    docker build . -t api-apache
    docker run -p 80:80 -p 443:443 -i api-apache --rm