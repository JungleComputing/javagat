BUILDING THE JAVAGAT ADAPTORS
-----------------------------


To build the JavaGAT, you need the Ant tool.
Ant is open-source and can be downloaded from www.apache.org.
First you need to build the GAT engine, see README.txt in the 
engine directory for information on how to do that.

Next, you can build the GAT Adaptors (or plugins).
You need these, as they provide the real implementation
of the GAT API.
You should edit build.xml in this directory to tell the
adaptors where the engine lives.
You can do this by setting the property named "engine_path" to the correct location.

You can now build the JavaGAT adaptors by typing "ant".

