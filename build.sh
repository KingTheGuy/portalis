#!/bin/bash
mvn compile
mvn package
cp target/magic-mirror-1.0-SNAPSHOT.jar ../server/plugins 
