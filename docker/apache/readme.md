Configuration:

docker build . -t api-apache
docker run -p 80:80 -i api-apache --rm