Hi,

This is the first Release Candidate of JavaGAT 2.0. JavaGAT is the Java 
implementation of the GridLab GAT (Grid Application Toolkit). You normally
install this software on your workstation or laptop, you don't have to 
install it on the grid sites you want to use.

Documentation for this release is located in the directory "docs", including 
the language-independent specification of the GAT interface and the javadoc.
More documentation will be added to the 2.0 release.

SETTING UP YOUR ENVIRONMENT
---------------------------

You can set the GAT_LOCATION environment variable to the location
where you installed GAT in.  This is normally not needed, if you don't
set this variable, the GAT will try to use a default, but this might
not work in all cases. So if something goes wrong, try to set this
variable.

For example (depending on the Operating System and shell you use):

export GAT_LOCATION=/home/rob/JavaGAT-1.6

or 

set GAT_LOCATION=C:\JavaGAT-1.6

you might want to put this line in your .bashrc (or equivalent)


BUILDING THE JAVAGAT
--------------------

To build the JavaGAT, you need the Ant tool.
Ant is open source software and can be downloaded from www.apache.org.

You can build the JavaGAT engine by typing "ant" in the Java GAT root
directory.

RUNNING GAT APPLICATIONS
------------------------
To run GAT applications, you can use the provided example script 
bin/run_gat_app, both on Windows and Unix systems. You do not have to use 
this script, it is just an easy example script that works in simple cases.

If you don't use this script, you have to specify the directory that
contains the .jar files of the adaptors together with:
-Dgat.adaptor.path=<PATH>.

The log4j.properties file can be edited for logging / debugging purposes,
and should be provided like this:
-Dlog4j.configuration=file:log4j.properties

BUGS AND OTHER QUESTIONS
------------------------
JavaGAT is part of the Ibis project. The Ibis project page is located at:
http://www.cs.vu.nl/ibis

Bugs and feature requests can be reported at:
https://gforge.cs.vu.nl/tracker/?group_id=25

Have fun!

Roelof Kemp
rkemp@cs.vu.nl

and

Rob van Nieuwpoort
rob@cs.vu.nl
