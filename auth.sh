#!/usr/bin/env bash

echo "ossrhAuthHeaderName=Authorization" >>gradle.properties
echo "ossrhAuthHeaderValue=Bearer $(printf "$MAVEN_USERNAME:$MAVEN_PASSWORD" | base64)" >>gradle.properties
