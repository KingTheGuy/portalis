#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/openjdk21
export PATH=$JAVA_HOME/bin:$PATH

gradle build
