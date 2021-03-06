Platypus3000
============

Platypus3000 is a non-generic swarm robotics simulator for the R-One Swarm robots.
It is made for rapid prototyping and simple debugging by providing simple visualisation tools.
It is NOT made for speed but still can handle up to a few thousend robots depending on the complexity of their `controllers' (your code for the robot).

*This Simulator is still under development but already in active use. Unfortunately the documentation is still very bad and often outdated or not even available. It takes a lot of time to make it useable for foreign people and we are only two fulltime students.*

Build Setup
-----------
In order to keep the dependency management and build system simple and portable we use maven as our build system.
The following steps get you up and running:

1. Make sure that maven 2 is installed on you system. On debian the according package is called `maven2`.
2. Because Platypus3000 depends on the processing library which is not (yet) in the maven central
repository you need to install it manually to your local maven repository. For this:
    1. Go to https://processing.org/download/ and download version 2.2 of processing and unpack it to your disk.
    2. Install the processing library and its pdf export library to your local maven repository using the following commands:

    ```
    mvn install:install-file -DgroupId=org.processing.core -DartifactId=core -Dversion=2.2 -Dpackaging=jar -Dfile=DOWNLOADPATH/processing-2.2/core/library/core.jar
    ```

    ```
    mvn install:install-file -DgroupId=org.processing.pdf -DartifactId=pdf -Dversion=2.2 -Dpackaging=jar -Dfile=DOWNLOADPATH/processing-2.2/modes/java/libraries/pdf/library/pdf.jar
    ```

    Make sure to modify the DOWNLOADPATH.

3. Now you are ready to build using maven. Execute
    ```
    mvn compile
    ```
    inside the root directory of the Platypus 3000 repository.
    
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
