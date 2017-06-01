# ambari_configs

Prerequisties 
1. Java 
2. Maven

Steps to compile and run the program
1. mvn clean compile assembly:single
2. Copy the jar file from target/configs-0.0.1-jar-with-dependencies.jar to a directory on biginsights cluster
3. Execute the program using following command
java -jar configs-0.0.1-jar-with-dependencies.jar <ambari server hostname> <ambari server port> <ambari cluster name> <SSL (true/false)> <username> <password>

Example:
java -jar configs-0.0.1-jar-with-dependencies.jar bigdatacluster.ibm.com 8445 BICluster true admin admin
4. The program will download configurations from all the components deployed and saves in current directory. The file will be named 
uniquely by appending current timestamp.
