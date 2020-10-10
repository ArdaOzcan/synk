#!/usr/bin/bash
run="$1"
mvn clean compile assembly:single
if [[ "$run" == "run" ]]; then
    java -jar ./target/synk-1.0-SNAPSHOT-jar-with-dependencies.jar
fi