# distributed-systems-work
Assignments Repo for CS6650 Building Scalable Distributed Systems 23fall@neu

Ayan Mao 2023 Nov

https://gortonator.github.io/bsds-6650/assignments-2022/Assignment-1

https://gortonator.github.io/bsds-6650/assignments-2022/Assignment-2

Github link: https://github.com/98may/distributed-systems-work/ 



[toc]









## 1: Overview

### 1.1: milestones

1) add analytics

2) add persist database - AWS RDS + MySQL

3) use multiple servers with load balancers - AWS EC2 + ALB

4) fine tune AWS args

### 1.2: code design

<img src="/Users/may/Library/Application Support/typora-user-images/Screenshot 2023-11-25 at 9.15.52 PM.png" alt="Screenshot 2023-11-25 at 9.15.52 PM" style="zoom:50%;" />

##### loadTester.java

loadTester is the main java class to handle tests,
 `parse` to set up args from `Usage: LoadTester <threadGroupSize> <numThreadGroups> <delay> [java|go]`,

 `startUp` to make sure the server on aws works, 

and then `mainLoad` send out the major requests.

```java
    public static void main(String[] args) {
        LoadTester loadTester = new LoadTester();
        loadTester.parse(args);
        loadTester.startUp();
        loadTester.mainLoad();
    }
```

##### ApiTask.java

implements `runnable`

as a single thread unit to send one pair of post and get requests

##### SendGetRequests.java

implements `runnable`

as a single thread unit to send one get request for main's `startUp`, like half of the `ApiTask.java`

##### Utilities.java

mainly the Utilitiy functions for logging and numbers

##### Config.java

set up the public static final arguments like `INITIAL_THREAD_COUNT`, `CLIENT_LOG_PATH` and `javaServletAddress`

### 1.3: key info

##### AWS EC2

<img src="/Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw2/test_results/aws_ec2.png" alt="aws_ec2" style="zoom:50%;" />

##### AWS ALB

<img src="/Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw2/test_results/aws_alb.png" alt="aws_alb" style="zoom:50%;" />

##### AWS RDS

<img src="/Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw2/test_results/aws_rds.png" alt="aws_rds" style="zoom:50%;" />

##### server address

```java
public static final String javaServletAddress = "http://3.80.33.155:8080/AlbumApp";
public static final String javaServletAddress2 = "http://54.221.189.103:8080/AlbumApp";
public static final String goServerAddress = "http://3.80.33.155:8081/IGORTON/AlbumStore/1.0.0";
```



## 2: Database

#### 2.1: choice

AWS RDS + MySQL 8.0.33

<img src="/Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw2/test_results/aws_rds.png" alt="aws_rds" style="zoom:50%;" />

#### 2.2: data model 

table: albums

field: album_i d(PK), name, artist, release_year, image

<img src="/Users/may/Library/Application Support/typora-user-images/Screenshot 2023-11-25 at 9.07.25 PM.png" alt="Screenshot 2023-11-25 at 9.07.25 PM" style="zoom:50%;" />

#### 2.3: results

simple postman test succeeded:

<img src="/Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw2/test_results/hw2_result_details/db_pic/db_postman.png" alt="db_postman" style="zoom:50%;" />



As you can see in the graph, it could have **millions** of records in the albums table.

![db_after](/Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw2/test_results/db_after.png)

![db_details](/Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw2/test_results/db_details.png)

## 3: single server results

Complete test results under /test_results folder
Here are some highlights:

### Java Servlet

##### 10

<img src="/Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw2/test_results/10_tp816.png" alt="10_tp816" style="zoom:50%;" />

##### 20

![20_tp1302](/Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw2/test_results/20_tp1302.png)

##### 30

![30_tp1526](/Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw2/test_results/30_tp1526.png)





## 4: load balanced servers results

##### simple tomcat test passed

![alb:](/Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw2/test_results/hw2_result_details/alb_pic/alb:.png)

##### simple alb get test passed

<img src="/Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw2/test_results/hw2_result_details/alb_pic/alb_get.png" alt="alb_get" style="zoom:50%;" />

## 5: tuned results

After tune rds, throughput increased from 1117 to 1595, which is 42.8% improvements, great!

##### some tune details

![tune_rds](/Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw2/test_results/hw2_tune/tune_rds.png)

##### before tune

![beforeTune_CPU](/Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw2/test_results/hw2_tune/beforeTune_CPU.png)

![beforeTune_rds](/Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw2/test_results/hw2_tune/beforeTune_rds.png)

![beforeTune_RW](/Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw2/test_results/hw2_tune/beforeTune_RW.png)

##### after tune

![afterTune_CPU](/Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw2/test_results/hw2_tune/afterTune_CPU.png)

![afterTune_rds](/Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw2/test_results/hw2_tune/afterTune_rds.png)

![afterTune_RW](/Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw2/test_results/hw2_tune/afterTune_RW.png)



## 6: Appendix of Command


Please login as the user "ec2-user" rather than the user "root".

local

```c
// ssh to aws ec2
/Users/may/Desktop/neu/cs6650_distributed/shortcuts/ssh_ec2.sh
  
  // chmod 400 ec2_2.pem
  // ssh -i ec2_2.pem ec2-user@ec2-3-80-33-155.compute-1.amazonaws.com
  ssh -i ec2_2.pem ec2-user@ec2-54-221-189-103.compute-1.amazonaws.com
  

// mysql to aws rdb
mysql -h dsdatabase.ccfcharlh91s.us-east-1.rds.amazonaws.com -P 3306 -u admin -p
  // pwd: 01234567

  /* java servlet */
// local build java servlet in Maven project to generate the AlbumApp.war file
mvn clean install
// scp java servlet - AlbumApp.war to aws ec2
scp -i /Users/may/Downloads/ec2_2.pem /Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw2/java_servlet/AlbumApp/target/AlbumApp.war ec2-user@ec2-3-80-33-155.compute-1.amazonaws.com:~/tmp

  
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
  
mvn exec:java -Dexec.mainClass="com.jenniek.clienttest.LoadTester" -Dexec.args="10 30 2 java"
  
  
  // plot throughput
  conda activate base
  /Users/may/anaconda3/bin/python 
  /Users/may/Desktop/neu/cs6650_distributed/distributed-systems-work/hw2/test_results/plot.py
```



aws ec2

```c
// java servlet
sudo mv ~/tmp/AlbumApp.war /usr/share/tomcat/webapps && sudo systemctl restart tomcat && systemctl status tomcat

sudo mv ~/tmp/AlbumApp.war /usr/share/tomcat/webapps
  
sudo systemctl restart tomcat
sudo systemctl stop tomcat
systemctl status tomcat
  
sudo vim /usr/share/tomcat/logs/catalina.out
sudo tail /usr/share/tomcat/logs/catalina.out -n 200
  
// see log to debug java servlet
sudo bash -c 'echo > /usr/share/tomcat/logs/catalina.out'
sudo grep "album" /usr/share/tomcat/logs/catalina.out
  
// go server
~/tmp/server
  
  
// mysql to aws rdb
mysql -h dsdatabase.ccfcharlh91s.us-east-1.rds.amazonaws.com -P 3306 -u admin -p
  // 01234567
show databases;
use ds_hw2_db;
describe albums;
select COUNT(*) from albums;
SELECT * FROM albums ORDER BY RAND() LIMIT 20;
```

