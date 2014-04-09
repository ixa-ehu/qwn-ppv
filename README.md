
QWN-PPV
=======

Contents
========

The contents of the module are the following:

    + formatter.xml           Apache OpenNLP code formatter for Eclipse SDK
    + pom.xml                 maven pom file which deals with everything related to compilation and execution of the module
    + src/                    java source code of the module
    + Furthermore, the installation process, as described in the README.md, will generate another directory:
    target/                 it contains binary executable and other directories


INSTALLATION
============

Installing the qwn-ppv requires the following steps:

If you already have installed in your machine JDK6 and MAVEN 3, please go to step 3
directly. Otherwise, follow these steps:

1. Install JDK 1.6
-------------------

If you do not install JDK 1.6 in a default location, you will probably need to configure the PATH in .bashrc or .bash_profile:

````shell
export JAVA_HOME=/yourpath/local/java6
export PATH=${JAVA_HOME}/bin:${PATH}
````

If you use tcsh you will need to specify it in your .login as follows:

````shell
setenv JAVA_HOME /usr/java/java16
setenv PATH ${JAVA_HOME}/bin:${PATH}
````

If you re-login into your shell and run the command

````shell
java -version
````

You should now see that your jdk is 1.6

2. Install MAVEN 3
------------------

Download MAVEN 3 from

````shell
wget http://apache.rediris.es/maven/maven-3/3.0.5/binaries/apache-maven-3.0.5-bin.tar.gz
````

Now you need to configure the PATH. For Bash Shell:

````shell
export MAVEN_HOME=/home/ragerri/local/apache-maven-3.0.5
export PATH=${MAVEN_HOME}/bin:${PATH}
````

For tcsh shell:

````shell
setenv MAVEN3_HOME ~/local/apache-maven-3.0.5
setenv PATH ${MAVEN3}/bin:{PATH}
````

If you re-login into your shell and run the command

````shell
mvn -version
````

You should see reference to the MAVEN version you have just installed plus the JDK 6 that is using.

3. Get module source code
--------------------------

````shell
git clone https://github.com/ixa-ehu/qwn-ppv
````

4. Installing using maven
---------------------------

````shell
cd qwn-ppv
mvn clean package
````

This step will create a directory called target/ which contains various directories and files.
Most importantly, there you will find the module executable:

qwn-ppv-1.0.jar

This executable contains every dependency the module needs, so it is completely portable as long
as you have a JVM 1.6 installed.

To install the module in the local maven repository, usually located in ~/.m2/, execute:

````shell
mvn clean install
````

7. USING qwn-ppv
=========================

GENERATING JAVADOC
==================

You can also generate the javadoc of the module by executing:

````shell
mvn javadoc:jar
````

Which will create a jar file core/target/qwn-ppv-1.0-javadoc.jar


Contact information
===================

````shell
Iñaki San Vicente and Rodrigo Agerri
IXA NLP Group
University of the Basque Country (UPV/EHU)
E-20018 Donostia-San Sebastián
inaki.sanvicente,rodrigo.agerri@ehu.es
````
