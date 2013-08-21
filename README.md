# Europeana API

## Installation

*The api2-war.war should be renamed api2.war in order to work.*

### Important database tables

1) apikey
Columns: 
- apikey (string): the public API key
  privatekey (string): a secret second API key (the two key should be used in login form as name/password pair)
  usagelimit (int): how many times a user can access the API (I am not sure whether it is working or just an intention)
  userid (int): reference to an existing user identifier (see user table). One user might have multiple apikey.

2) users
- id: user identifier
- apikey: API key, but it is not in use as far as I know it
- email: email
- enabled (boolean): the user is enabled to use API
- firstname: first name
- languages: 
- lastlogin: last time the user logged in for personal page
- lastname: user's last name
- newsletter:
- password: user's password (in encripted form)
- projectid: 
- providerid: 
- registrationdate: when he registrated
- role: 
- username: username when login


### Usage of API2 form command line

To improve the understandability of the API documentation here is a short example of how to use it with cURL tool.
It helps to create a similar implementation with your own favorite tool.

1) Request the session identifier:

First you have to POST the api2key and secret parameters to the /login page

request:
$ curl -i -d "api2key=[your api key]&secret=[your secret key]" "http://localhost:8080/api2/login"

response:
1: HTTP/1.1 302 Moved Temporarily
2: Server: Apache-Coyote/1.1
3: Set-Cookie: JSESSIONID=54B3FED3649FD8D205C297A52363825B; Path=/api2; HttpOnly
4: Location: http://localhost:8080/api2/
5: Content-Length: 0
6: Date: Fri, 13 Jul 2012 10:41:40 GMT

In line 3 you can find the session ID in the form of a key=value pair. This is

  JSESSIONID=54B3FED3649FD8D205C297A52363825B

You have to extract it, and use it in later requests.

2) Use the session identifier:

Now use this session identifier as cookie.

$ curl -i -b "JSESSIONID=54B3FED3649FD8D205C297A52363825B" "http://localhost:8080/api2/search.json?query=john"
HTTP/1.1 200 OK
Server: Apache-Coyote/1.1
Content-Type: application/json
Transfer-Encoding: chunked
Date: Fri, 13 Jul 2012 10:42:09 GMT

{
	"apikey": "[your api key]",
	"action": "search.json",
	"success": true,
	"statsDuration": 0,
	"itemsCount": 12,
	"totalResults": 880,
	"items": [{...},{...}...]
}

As you can see the application now returns good response, the content type is JSON, and you actually get
the JSON result. For the sake of readibility I formatted the contect, in reallity it is condensed into one
long line, and without unnecessary whitespace characters.

cURL parameters used in this short tutorial:

 -i          Include protocol headers in the output. With applying this you can see the HTTP headers.
 -d DATA     HTTP POST data. The format of the data is URL encoded.
 -b STRING   String to read cookies from.
 
