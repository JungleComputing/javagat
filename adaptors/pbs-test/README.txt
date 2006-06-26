1) Build
========

To build the test class SubmitPBSJob first set the environment variable
GAT_LOCATION to the path, where Java-GAT is located in. Otherwise the build will
fail.

For SubmitPBSJob you also need commons-cli-1.0.jar, which should be available in 
$GAT_LOCATION/adaptors/external.

Simply typing ant will create a jar-file called SubmitPBSJob.jar in a new
bin sub-direcory. During the build process the follwoing jar archives are
copied to the bin/lib sub-directory:

colobus.jar
commons-cli-1.0.jar
GAT.jar
PbsBrokerAdaptor.jar

Those jar archives are necessary to run the test.

2) Execution
============

First of all you need to set the environment variable GAT_ADAPTOR_PATH 
to point to the adaptors directory. Otherwise th GATEngine will not find
the adaptors. Usually the GAT_ADAPTOR_PATH is $GAT_LOCATION/adaptors/lib.

If you change to the bin sub-directory you can simply call 

java -jar SubmitPBSJob.jar 

without any arguments, and you will get a help, displaying all possible arguments.
You can also call the test program from another directory, but then you have 
to prepend the path to SubmitPBSJob.jar.

Have fun.

Alexander
