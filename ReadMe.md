As per the given challenge document

#####Created endpoint to fetch the top tracks from the given CSV files
```
Endpoint GET /api/music/top/tracks

Swagger Endpoint : http://localhost:9090/swagger-ui.html
```
####Technologies used JAVA 8, Spring-boot, Maven, cache

####Operating system tested: Mac OS

####To Start the application
```
Download the project from the github
Open terminal and go to the project location ex cd ~/Downloads/music
Run the script file docker-run.sh, example: sh docker-run.sh
if the docker is not available, please use following command
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=9090
```

#####Implementations and assumptions made
``` 
1. Calling csv files directly from the public url and reding them
2. Used live rates currency converter API to get the live rates
3. The very after server start fetching the currency rates and seting them into cache
4. Refreshing the cache for every 30000 ms, by using scheduler
5. If the rates failed to fetch from cache, directly calling currecny rates as a fall back
6. Created integration and unit test cases, need to improve i guess
```
