BUILDING THE JAVAGAT ADAPTORS
-----------------------------


To build the JavaGAT, you need the Ant tool.
Ant is open-source and can be downloaded from www.apache.org.
First you need to build the GAT engine, see README.txt in the 
engine directory for information on how to do that.

Next, you can build the GAT Adaptors (or plugins).
You need these, as they provide the real implementation
of the GAT API.
You can now build the JavaGAT adaptors by typing "ant".

If you change the directory structure, you should edit build.xml 
in this directory to tell the adaptors where the engine lives.
You can do this by setting the property named "engine_path" to the correct location.

globus certificates
-------------------

For now, you could eiher:

- Have your proxy in the default location.
  In this case you don't have to specify anything to the gat.

- Have it in a different location.
  In this case you can set the X509_USER_PROXY variable.
  If you do this, grid-proxy-init will also use this variable to 
  generate the proxy in that location.
  This will be changed in the future to make it easier and to make 
  explicit handling of credentials possible...


Cheers,

Rob


