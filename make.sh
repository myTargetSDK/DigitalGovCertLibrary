#!/bin/bash

while getopts "v:" opt
do
   case "$opt" in
      v) MY_VERSION="$OPTARG" ;;
      ?) helpFunction ;; # Print helpFunction in case parameter is non-existent
   esac
done

if [[ -z ${MY_VERSION} ]]; then
  echo ":::::::::: ERROR: Version wasn't specified ::::::::::"
  echo ":::::::::: You should specify version with '-v' flag ::::::::::"
  echo ":::::::::: PUBLISHING FAILED ::::::::::"
  exit 1
fi

./gradlew clean
./gradlew :sdk:javaDocReleaseJar
./gradlew :sdk:generateMetadataFileForReleasePublication
./gradlew :sdk:publishReleasePublicationToMavenLocal
./gradlew :sdk:publishToMavenLocal

cd ../../.m2/repository/io/github/mytargetsdk/digitalgovcert/${MY_VERSION}


gpg -ab digitalgovcert-${MY_VERSION}.pom
gpg -ab digitalgovcert-${MY_VERSION}-javadoc.jar
gpg -ab digitalgovcert-${MY_VERSION}-sources.jar
gpg -ab digitalgovcert-${MY_VERSION}.module
gpg -ab digitalgovcert-${MY_VERSION}.aar


jar -cvf bundle.jar \
digitalgovcert-${MY_VERSION}.pom \
digitalgovcert-${MY_VERSION}.pom.asc \
\
digitalgovcert-${MY_VERSION}-javadoc.jar \
digitalgovcert-${MY_VERSION}-javadoc.jar.asc \
\
digitalgovcert-${MY_VERSION}-sources.jar \
digitalgovcert-${MY_VERSION}-sources.jar.asc \
\
digitalgovcert-${MY_VERSION}.aar \
digitalgovcert-${MY_VERSION}.aar.asc \
\
digitalgovcert-${MY_VERSION}.module \
digitalgovcert-${MY_VERSION}.module.asc \

#done
