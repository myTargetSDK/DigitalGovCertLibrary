#!/bin/bash

MY_VERSION="1.0.25"

./gradlew clean
./gradlew :sdk:javaDocReleaseJar
./gradlew :sdk:javaDocReleaseJar
./gradlew :sdk:generateMetadataFileForReleasePublication
./gradlew :sdk:publishReleasePublicationToMavenLocal
./gradlew :sdk:publishToMavenLocal

cd ../
cd ../

ls
cd .m2/repository/io/github/mytargetsdk/digitalgovcert/${MY_VERSION}
ls

gpg -ab digitalgovcert-${MY_VERSION}.pom
gpg -ab digitalgovcert-${MY_VERSION}-javadoc.jar
gpg -ab digitalgovcert-${MY_VERSION}-sources.jar
gpg -ab digitalgovcert-${MY_VERSION}.module
gpg -ab digitalgovcert-${MY_VERSION}.aar


jar -cvf bundle.jar digitalgovcert-${MY_VERSION}.pom digitalgovcert-${MY_VERSION}.pom.asc digitalgovcert-${MY_VERSION}-javadoc.jar.asc digitalgovcert-${MY_VERSION}-sources.jar.asc digitalgovcert-${MY_VERSION}.aar.asc digitalgovcert-${MY_VERSION}.module.asc digitalgovcert-${MY_VERSION}.pom.asc    digitalgovcert-${MY_VERSION}-javadoc.jar digitalgovcert-${MY_VERSION}-sources.jar digitalgovcert-${MY_VERSION}.aar

#done
