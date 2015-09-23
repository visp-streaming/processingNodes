#!/bin/bash
git stash
git pull
git stash apply



cd vispprocessingnode/
git pull
vim src/main/resources/application.properties
mvn clean install -Dmaven.test.skip=true
cd /var/lib/tomcat7/webapps/
sudo rm -rf visp*
cd ~/vispprocessingnode/target/
sudo cp vispProcessingNode-0.0.1-SNAPSHOT.war /var/lib/tomcat7/webapps/visp.war