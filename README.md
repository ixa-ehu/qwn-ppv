
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

1. Install JDK 1.7
-------------------

If you do not install JDK 1.7 in a default location, you will probably need to configure the PATH in .bashrc or .bash_profile:

````shell
export JAVA_HOME=/yourpath/local/java17
export PATH=${JAVA_HOME}/bin:${PATH}
````

If you use tcsh you will need to specify it in your .login as follows:

````shell
setenv JAVA_HOME /usr/java/java17
setenv PATH ${JAVA_HOME}/bin:${PATH}
````

If you re-login into your shell and run the command

````shell
java -version
````

You should now see that your jdk is 1.7

2. Install MAVEN 3
------------------

Download MAVEN 3 from

````shell
wget http://apache.rediris.es/maven/maven-3/3.0.5/binaries/apache-maven-3.0.5-bin.tar.gz
````

Now you need to configure the PATH. For Bash Shell:

````shell
export MAVEN_HOME=/home/myuser/local/apache-maven-3.0.5
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

3. Install UKB graph propagation software
------------------------------------------

Download UKB 2.0 from http://ixa2.si.ehu.es/ukb/ 

````shell
wget http://ixa2.si.ehu.es/ukb/ukb_2.0.tgz
````

Extract the files and follow the installation instructions in the src/INSTALL file (usual ./configure && make && sudo make install procedure):

````shell
tar -xzvf ukb_2.0.tgz
````


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
as you have a JVM 1.7 installed.

To install the module in the local maven repository, usually located in ~/.m2/, execute:

````shell
mvn clean install
````

7. USING qwn-ppv
=========================

First use of qwn-ppv
====================

QWN-PPV includes some graphs by default. However in order to use them the must be compiled by ukb. In order to do this, execute qwn-ppv-1.0.jar as follows:

````shell
java -jar qwn-ppv-1.0.jar compile -k all
````

This command will create a 'graphs' folder in the same directory where 'qwn-ppv-1.0.jar' is located, and it will store the graphs included in the QWN-PPV distribution, which are:
   - mcr30 : Graph built using all relations included in the Multilingual Central Repository (MCR).
   - mcr30_Syn and mcr30_Ant: Graphs built using synonymy (mcr30_Syn) and antomymy relations (mcr30_Ant) in the MCR.
   - mcr30_noAnt: Graph built using all relations of the MCR, except for antonymy relations (Gloss information included).
   - mcr30_noAntGloss: Graph built using all relations of the MCR, except for antonymy and Gloss relations. 


QWN-PPV Funcionalities
==========================
QWN-PPV provides 3 main funcionalities:
1. **compile**: compile graph structures using UKB.
2. **create**: Create polarity lexicons given a list of known polarity seeds (lemmas or MCR/Wordnet synsets -or any other custom graph node representation). 
3. **eval**: Evaluate the a given polarity lexicon agains a corpus.

Creating lexicon polarity lexicons with qwn-ppv
=================================================

Coming soon. In the meantime execute:

````shell
java -jar qwn-ppv-1.0.jar create -h
````


Evaluating polarity lexicons against a corpus
==================================================

Coming soon. In the meantime execute:

````shell
java -jar qwn-ppv-1.0.jar eval -h
````


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
