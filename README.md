Platypus3000
============

Platypus3000 is a non-generic swarm robotics simulator for the R-One Swarm robots.

Build Setup
-----------
In order to keep the dependency management and build system simple and portable we use maven as our build system.
The following steps get you up and running:

1. Make sure that maven 2 is installed on you system. On debian the according package is called `maven2`.
2. Because Platypus3000 depends on the processing library and the processing library is not (yet) in the maven central
repository you need to install it manually to your local maven repository. For this:
    1. Go to https://processing.org/download/ and download version 2.2 of processing and unpack it to your disk.
    2. Install the processing library and its pdf export library to your local maven repository using the following commands:

    ```
    mvn install:install-file -DgroupId=org.processing.core -DartifactId=core -Dversion=2.2 -Dpackaging=jar -Dfile=DOWNLOADPATH/processing-2.2/core/library/core.jar
    ```

    ```
    mvn install:install-file -DgroupId=org.processing.pdf -DartifactId=pdf -Dversion=2.2 -Dpackaging=jar -Dfile=DOWNLOADPATH/processing-2.2/modes/java/libraries/pdf/library/pdf.jar
    ```

    Make sure to modify the path.

3. Now you are ready to build using maven:
    ```
    mvn compile
    ```

Install
-------
After you finished the build setup, you can install Platypus3000 to your local maven repository. This means that you
can add it easily as a dependency to your own maven-based project. Run `mvn install` and you are done. Then you can
add it to your pom file using the following entry:


    <groupId>org.platypus3000</groupId>
    <artifactId>Platypus3000</artifactId>
    <version>1.0-SNAPSHOT</version>


Note: if you use intellij IDEA you can open Platypus3000 or import it as a module in your project
by selecting the pom.xml in the open/import dialog. "Import maven projects automatically" should be selected.
Sometimes a double click on Maven Projects -> Platypus3000 -> Lifecycle -> package helps if there are problems with
the dependencies