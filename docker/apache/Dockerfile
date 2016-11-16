FROM coreos/apache
COPY myeuropeana.eu.conf /etc/apache2/sites-available/
RUN apt-get update &&\
	apt-get install -y libapache2-mod-proxy-html libxml2-dev &&\
	a2enmod proxy proxy_http rewrite &&\
	a2ensite myeuropeana.eu.conf &&\
	a2dissite default &&\
	service apache2 reload
EXPOSE 80

ENTRYPOINT ["/usr/sbin/apache2ctl"]

CMD ["-D", "FOREGROUND"]