FROM coreos/apache

# Copy configuration and certificates
COPY localhost.conf /etc/apache2/sites-available/
COPY ./certificates/ssl.crt /etc/apache2/ssl/ssl.crt
COPY ./certificates/ssl.key /etc/apache2/ssl/ssl.key

# Create folder because apache will store the ssl_mutex file there
RUN mkdir -p /var/run/apache2/

# Basic config
RUN apt-get update &&\
	apt-get install -y libapache2-mod-proxy-html libxml2-dev &&\
	a2enmod proxy proxy_http rewrite &&\
	a2ensite localhost.conf &&\
	a2dissite default
# To disable SSL-enforcement, comment out the line below
RUN a2enmod ssl

RUN	service apache2 reload

EXPOSE 80 443

ENTRYPOINT ["/usr/sbin/apache2ctl"]

CMD ["-D", "FOREGROUND"]