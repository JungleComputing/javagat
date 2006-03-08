BUILDING THE JAVAGAT
--------------------

To build the JavaGAT, you need the Ant tool.
Ant is open-source and can be downloaded from www.apache.org.
You can build the JavaGAT engine by typing "ant".
The tests can be compiled with "ant compile-tests".

Next, you can build the GAT Adaptors (or plugins).
You need those, as they provide the real implementation
of the GAT API.
You should edit build.xml in the adaptors directory to tell the
adaptors where the engine lives.
You can do this by setting the property named "engine_path" to the correct location.

RUNNING GAT APPLICATIONS
------------------------
You have to specify the directory that contains the .jar files of the
adaptors with: -Dgat.adaptor.path=<PATH>.

I personally use the bin/run_gat_app script that does this for
you. You might have to modify it if you use a different directory
structure...

If you provide the -Dgat.verbose (or even -Dgat.debug), you will get
information on which adaptors are loaded.

Have fun!

Rob van Nieuwpoort
rob@cs.vu.nl

