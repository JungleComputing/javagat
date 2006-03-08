SETTING UP YOUR ENVIRONMENT
---------------------------

You can set the GAT_LOCATION environment variable to the location
where you installed GAT in.  This is normally not needed, if you don't
set this variable, the GAT will try to use a default, but this might
not work in all cases. So if something goed wrong, try to set this
variable.

For example (depending on the os and shell you use):

export GAT_LOCATION=/home/rob/JavaGAT-1.2

or 

set GAT_LOCATION=C:\JavaGAT-1.2

you might want to put this line in your .bashrc (or equivalent)



BUILDING THE JAVAGAT
--------------------

To build the JavaGAT, you need the Ant tool.
Ant is open source software and can be downloaded from www.apache.org.

You can build the JavaGAT engine by typing "ant" in the JavaGATEngine
directory.

Next, you can build the GAT Adaptors (or plugins).  You need those, as
they provide the real implementation of the GAT API. You can build the
adaptors with "ant" in the JavaGATAdaptors directory.

RUNNING GAT APPLICATIONS
------------------------
To run GAT applications, you can use the bin/run_gat_app script, both
on Windows and Unix systems.
If you don't use this script, you have to specify the directory that
contains the .jar files of the adaptors with:
-Dgat.adaptor.path=<PATH>.

If you provide the -Dgat.verbose (or even -Dgat.debug), you will get
information on which adaptors are loaded.

DOCUMENTATION
-------------
There is some documentation in the doc directory.
It contains a JavaGAT tutorial and the GAT API specification.

You can also build the javadoc locally by typing "ant javadoc-user".
The javadocs are then generated in the doc/html/javadoc directory.

For more documentation, please go to 

http://www.gridlab.org/WorkPackages/wp-1/documentation.html

Have fun!

Rob van Nieuwpoort
rob@cs.vu.nl
