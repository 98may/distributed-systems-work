# distributed-systems-work
Assignment1 Repo for CS6650 Building Scalable Distributed Systems 23fall@neu

https://gortonator.github.io/bsds-6650/assignments-2022/Assignment-1



Github link: https://github.com/98may/distributed-systems-work/ 





[toc]



## 1: info

my aws ec2 ip address: `3.80.33.155:8080`

shortcuts:

Go get : http://3.80.33.155:8081/IGORTON/AlbumStore/1.0.0/albums/1

Java get: http://3.80.33.155:8080/AlbumApp/albums/2

## 2: client design

![diagram_loadTester](/Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw1/diagram_loadTester.png

<img src="/Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw1/uml_loadTester.png" alt="uml_loadTester" style="zoom:50%;" />

## 3: test results

Complete test results under /test_results folder
Here are some highlights:

#### Java Servlet

![client2_java30_tp103](/Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw1/test_results/client2_java30_tp103.png)

### Go Server

<img src="/Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw1/test_results/client2_go30_tp81.png" alt="client2_go30_tp81" style="zoom:50%;" />

### Plot of throughput by time

<img src="/Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw1/test_results/go_30_throughtput_plot.png" alt="go_30_throughtput_plot" style="zoom:24%;" />

## Appendix of Command

local

```c
// ssh to aws ec2
/Users/may/Desktop/neu/cs6650_distributed/shortcuts/ssh_ec2.sh

  /* java servlet */
// local build java servlet in Maven project to generate the AlbumApp.war file
mvn clean install
// scp java servlet - AlbumApp.war to aws ec2
scp -i /Users/may/Downloads/ec2_2.pem /Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw1/java_servlet/AlbumApp/target/AlbumApp.war ec2-user@ec2-3-80-33-155.compute-1.amazonaws.com:~/tmp
  
  /* Go server */
// local run go server(macOS + m1 chip) for local tests
GOOS=darwin GOARCH=arm64 go run ./main.go
// local build go server for aws ec2(linux + arm64)
GOOS=linux GOARCH=arm64 go build -o server main.go
// scp go server - server to ec2 ~/emp to start
scp -i /Users/may/Downloads/ec2_2.pem /Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw1/go_server/go-server-server-generated/server ec2-user@ec2-3-80-33-155.compute-1.amazonaws.com:~/tmp

  /* client - LoadTester */
`Usage: LoadTester <threadGroupSize> <numThreadGroups> <delay> [java|go]`
// e.g.
mvn clean install && mvn exec:java -Dexec.mainClass="com.jenniek.clienttest.LoadTester" -Dexec.args="10 30 2 java"
```



aws ec2

```c
// java servlet
sudo mv ~/tmp/AlbumApp.war /usr/share/tomcat/webapps
  
sudo systemctl restart tomcat
sudo systemctl stop tomcat
systemctl status tomcat
  
sudo vim /usr/share/tomcat/logs/catalina.out
  
// go server
~/tmp/server
```

