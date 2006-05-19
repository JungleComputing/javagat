1) Build
========

To build the test class SubmitPBSJob first set the environment variable
GAT_LOCATION to the path, where Java-GAT is located in. Otherwise the build will
fail.

For SubmitPBSJob you also need commons-cli-1.0.jar, which is available in 
$GAT_LOCATION/adaptors/external.


2) Execution
============

To run SubmitPBSJob you have to add to your CLASSPATH the pathes to follwowing jar files:

commons-cli-1.0.jar
GAT.jar
colobus.jar
PbsBrokerAdaptor.jar

and to the class

SubmitPBSJob.class


colobus.jar is part of engine/lib
PbsBrokerAdaptor.jar is part of adaptors/lib
commons-cli-1.0.jar is part of adaptors/external

Simply call 

java test.SubmitPBSJob (package test).

without any arguments, and you will get a help.

Have fun.

Alexander
