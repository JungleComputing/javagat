Build jobProgs...
==============

simply call ant!

You must have JavaGAT installed and also the GAT_LOCATION set.

Running
=======

e.g.: for running dist/GATJobSubmit_Stageing_Exec_Sh.jar

java -Dgat.adaptor.path=$GAT_LOCATION/lib/adaptors  -Dlog4j.configuration=file:log4j.properties -jar dist/GATJobSubmit_Stageing_Exec_Sh.jar

Without arguments the class prompts the possible arguments, in this case:

 Usage: GATJobSubmit <host> <executable> <arguments>


Logging:
=======

The logging is managed by log4j. To set the level please change the according lines in log4.properties 




