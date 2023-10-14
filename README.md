# distributed-systems-work
Assignment1 Repo for CS6650 Building Scalable Distributed Systems 23fall@neu



Github link: https://github.com/98may/distributed-systems-work/ 





[toc]

## 1: info

my aws ec2 ip address: `3.80.33.155:8080`

shortcuts:

Go get : http://3.80.33.155:8081/IGORTON/AlbumStore/1.0.0/albums/1

Java get: http://3.80.33.155:8080/AlbumApp/albums/2



### how to run tests

small client test:

`mvn clean install && mvn exec:java -Dexec.mainClass="com.jenniek.clienttest.ClientTest"`

LoadTester:

```c
Usage: LoadTester <threadGroupSize> <numThreadGroups> <delay> [java|go]
  
// e.g. 
mvn clean install && mvn exec:java -Dexec.mainClass="com.jenniek.clienttest.LoadTester" -Dexec.args="10 10 2 go"
  
mvn clean install && mvn exec:java -Dexec.mainClass="com.jenniek.clienttest.LoadTester" -Dexec.args="10 10 2 java"
```



## 2: Servers

### Go server on aws ec2

start server: `~/tmp/server`



helper:

#### local build go server for aws ec2(linux + arm64):

 `GOOS=linux GOARCH=arm64 go build -o server main.go`

#### and then, move to ec2 ~/emp to start
e.g.
`scp -i /Users/may/Downloads/ec2_2.pem /Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw1/go_server/go-server-server-generated/server ec2-user@ec2-3-80-33-155.compute-1.amazonaws.com:~/tmp `



##### local run go server(macOS + m1 chip): 

`GOOS=darwin GOARCH=arm64 go run ./main.go`

then check http://localhost:8081/IGORTON/AlbumStore/1.0.0/albums/2 or http://localhost:8081/IGORTON/AlbumStore/1.0.0/albums/1







### Java servlet on aws ec2

start server: `sudo systemctl restart tomcat`



local build java servlet in Maven project to generate the AlbumApp.war file

`mvn clean install`

move java servlet(AlbumApp.war) from local to aws ec2:

```
scp -i /Users/may/Downloads/ec2_2.pem /Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw1/java_servlet/AlbumApp/target/AlbumApp.war ec2-user@ec2-3-80-33-155.compute-1.amazonaws.com:~/tmp
  
sudo mv ~/tmp/AlbumApp.war /usr/share/tomcat/webapps
```



helper:

```
sudo systemctl restart tomcat
systemctl status tomcat
  
sudo vim /usr/share/tomcat/logs/catalina.out
```





## 3: Clients

`Usage: LoadTester <threadGroupSize> <numThreadGroups> <delay> [java|go]`





part of test results:

![requests](/Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw1/test_results/requests.png)

![terrible throughput](/Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw1/test_results/terrible throughput.png)