Delivery Service
====================================

This a simple app that exposes a REST interface, leveraging HATEOAS, to deal with routing between places.

Under hood the following techs are used:

*Framework*

[Spring Boot](http://projects.spring.io/spring-boot/)

[Spring Security](https://docs.spring.io/spring-security/site/docs/5.0.5.RELEASE/reference/htmlsingle/)

[Spring Data](https://spring.io/projects/spring-data)

[Spring HATEOAS](https://docs.spring.io/spring-hateoas/docs/0.24.0.RELEASE/reference/html/)

[Spring Neo4j](https://docs.spring.io/spring-data/neo4j/docs/5.0.7.RELEASE/reference/html/)

[Spring Session](https://docs.spring.io/spring-session/docs/2.0.4.BUILD-SNAPSHOT/reference/html5/)

*Databases*

[Redis](https://redis.io/documentation)

[Neo4j](https://neo4j.com/developer/get-started/)

Build
------------

### Prerequisites
    Java 8
    Docker
    Maven
    Docker Compose

Run docker compose to boot up a Redis and Neo4j database:

    docker-compose up
    mvn clean install
    
Run *mvn clean install* to build the application fat jar. 
To run the application you have two different ways:
    
    mvn spring-boot:run
    
        or
    
    exec java \
    -jar \
    -Dspring.profiles.active=default \
    /target/delivery-0.0.1-SNAPSHOT.jar

Loaded Data
-----------

By default, the app on boot will create two users and a set of places and routes. Check the folder src/resources/data
to review the data. The following image represents the graph generated:

![picture](http://i64.tinypic.com/k35aw0.png)

Api
----------

Leverages HATEOAS to allow self browsing.

- Managing Authorities, Places and Routes are privileged operations reserved only for admins. 

- Creating users is the only operation that does not require authentication and csrf token, all others do.

The api consists of five entity domains: *Users*, *Authorities*, *Places*, *Routes* and *Paths*.

    curl localhost:8080/users --cookie 'x-delivery-auth=SESSION_HERE' -H 'X-CSRF-TOKEN: TOKEN_HERE'
    curl localhost:8080/places --cookie 'x-delivery-auth=SESSION_HERE' -H 'X-CSRF-TOKEN: TOKEN_HERE'
    curl localhost:8080/routes --cookie 'x-delivery-auth=SESSION_HERE' -H 'X-CSRF-TOKEN: TOKEN_HERE'
    curl localhost:8080/authorities --cookie 'x-delivery-auth=SESSION_HERE' -H 'X-CSRF-TOKEN: TOKEN_HERE'
    
*Examples*

    - Create user:
    curl -X POST -H 'Content-Type: application/json' -H 'Accept: application/json' -d '{"username": "hfgbarrigas@gmail.com","password": "batatas", "firstName": "", "lastName": "", "authorities": []}' localhost:8080/users -v

    - Login example:
    curl -X POST -d username='admin@gmail.com' -d password='password' localhost:8080/login -v

    - Create authority:
    curl -X POST -H 'Content-Type: application/json' -d '{"name": "TEST"}' localhost:8080/authorities --cookie 'x-delivery-auth=SESSION' -H 'X-CSRF-TOKEN: TOKEN'
    
    - Associate authorities to user:
    curl -X POST -H 'Content-Type: application/json' -d '["TEST"]' localhost:8080/users/72/authorities --cookie 'x-delivery-auth=SESSION' -H 'X-CSRF-TOKEN: TOKEN' -v    

    - Create a place: Places can only be created without routes because the start of a route needs to be the url entity of it self.
    curl -X POST -H 'Content-Type: application/json' -d '{"name":"DUMMY"}' 'localhost:8080/places?depth=1' --cookie 'x-delivery-auth=SESSION' -H 'X-CSRF-TOKEN: TOKEN'
        
    - Create route:
    curl -X POST -H 'Content-Type: application/json' -d '{"start":"http://localhost:8080/places/56","destination": "http://localhost:8080/places/60","time":"100","cost":"100"}' 'localhost:8080/routes' --cookie 'x-delivery-auth=SESSION' -H 'X-CSRF-TOKEN: TOKEN' -v

Finally we have paths, only *start* and *end* are mandatory fields:

    curl 'localhost:8080/paths?start=A&end=B' --cookie 'x-delivery-auth=SESSION_HERE' -H 'X-CSRF-TOKEN: TOKEN_HERE'
    curl 'localhost:8080/paths?start=A&end=B&time=X&cost=Y&algorithm=K' --cookie 'x-delivery-auth=SESSION_HERE' -H 'X-CSRF-TOKEN: TOKEN_HERE'
    
*time* and *cost* attributes are limits for path traversal. Also, we have three different algorithms available:

    ALL - Will yeild every path available from START to END
    ALL_SHORTEST - Will yeild all shortest paths available from START to END. Shortest = less number of nodes
    SHORTEST - Will yeild the shortest path available, if multiple paths are available DB will choose.
    
All paths are sorted by time (todo: make this configurable via query parameter).

Deploy
=======

    docker build .
    docker run <image-id> -p 8080:8080

TODOS
----------
* Add more tests
