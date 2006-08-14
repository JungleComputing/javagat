Hi,

This is the Java implementation of the GridLab GAT (Grid Application Toolkit).
You normally install this software on your workstation or laptop, you don't 
have to install it on the grid sites you want to use.
Documentation for this release is located in the directory "docs", including the language-
independant specification of the GAT interface, and a tutorial for the Java GAT.

For more information on the GAT in general, see www.gridlab.org.

This software consists of two parts. The GAT engine and the GAT adaptors (the GAT adaptors
are further split up into "adaptors" and "gridlabAdaptors". The latter are only useful 
if you have installed GridLab software such as GRMS or Mercury.
For more information, see the README.txt files in the "engine" and "adaptors" 
subdirectories here.


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

You can build the JavaGAT engine by typing "ant" in the Java GAT root
directory.

If you also need the GridLab adaptors, you can now type "ant gridlab" in the same 
directory. If you don't know what GridLab or Mercury are, 
you most likely don't need this step.


RUNNING GAT APPLICATIONS
------------------------
To run GAT applications, you can use the provided example script bin/run_gat_app, both
on Windows and Unix systems. You do not have to use this script, it is just an easy 
example script that works in simple cases.

If you don't use this script, you have to specify the directory that
contains the .jar files of the adaptors with:
-Dgat.adaptor.path=<PATH>.

If you provide the -Dgat.verbose (or even -Dgat.debug), you will get
information on which adaptors are loaded.


DOCUMENTATION
-------------
There is some documentation in the doc directory.
It contains a JavaGAT tutorial and the GAT API specification.
The javadocs for the Java GAT application programmers interface are located in 
the "doc/javadoc" directory.

For more documentation, please go to 

http://www.gridlab.org/WorkPackages/wp-1/documentation.html

Have fun!

Rob van Nieuwpoort
rob@cs.vu.nl
