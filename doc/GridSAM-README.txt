Info about GridSAM adapter to JavaGAT.
Index:
	1. Prerequisites
	2. Parameters outside javagat
	3. Double sandbox
	4. GridSAM and OMII version tested to work with the adapter:
	5. Required/supported properties/preferences
	6. How to run?
	7. What does not work


1. Prerequisites
- install OMII client (in directory that is further called $OMII)
- run sample gridsam application (without file staging - sleep.jsdl is enough) from tutorial
- check that gridsam-status application is working for the sleep task

Please remember that GridSAM client does not work with java 1.6.

2. Parameters outside javagat
GridSAM needs property file (called crypto.properties) to run. Without this file gridsam is unable to use ssl and will fail. If you want to use gridsam adapter with JavaGAT make sure that crypto.properties file is included in JavaGAT running classpath. This file contains information about OMII keys and can be found in $OMII/conf/ directory.


3. Double sandbox
File staging for GridSAM is done by JavaGAT (and not by GridSAM staging
mechanism). This let you use any file adaptor available in JavaGAT. However
GridSAM does not support <WorkingDirectory> tag and cannot use standard
sandbox created by JavaGAT.
Therefore GridSAM adapter uses a workaround for this - JavaGAT creates a
sandbox on the remote machine and files are staged there. Files from that
sandbox are then staged in by gridsam (in fact they are copied locally) and
after the job is done they are staged out (copied locally) to the JavaGAT
sandbox directory. JavaGAT stages the file out from his own sandbox directory
(not noticing that in fact the application was running in different sandbox).
File staging process looks as follow:

i. JavaGAT stages files from local machine to remote:$JAVAGATSANDBOXROOT/.JAVAGAT_XXXXX/
ii. GridSAM stages files from remote:$JAVAGATSANDBOXROOT/.JAVAGAT_XXXXX/ to remote:$GRIDSAMSANDBOXROOT/gridsam-XXXXX/	(where gridsam is actually running on "remote")
iii. GridSAM runs the job
iv. GridSAM stages files from remote:$GRIDSAMSANDBOXROOT/gridsam-XXXXX/ to remote:$JAVAGATSANDBOXROOT/.JAVAGAT_XXXXX/
v. JavaGAT stages files from remote:$JAVAGATSANDBOXROOT/.JAVAGAT_XXXXX/ to local machine


4. GridSAM and OMII version tested to work with the adapter:
Adapter was written with OMII version 3.4.0 and gridsam version TODO.


5. Required/supported properties/preferences
Required:
	Preferences:
		ResourceBroker.jobmanagerContact - http address of GridSAM web services (for exampled https://localhost:18443/gridsam/services/gridsam)
	softwareDescriptionAttributes:
		sandboxRoot - absolute path to the directory where JavaGAT will create its sandbox. If not set GridSAM adapter will throw an exception.
Supported:
	hardwareResourceAttributes:
		machine.node - String or String[] - translated directly to <CandidateHosts><HostName/>+</CandidateHosts> jsdl tag	
	softwareDescriptionAttributes:
		environment - translated to <Environment name="EVNNAME">envval</Environment>	(works)
		maxCPUTime - translated to <CPUTimeLimit/> tag (not really tested)
		maxMemory - translated to <MemoryLimit/> tag (not really tested)
		maybe other attributes are easy to translate as well - support for them needs to be added to org.gridlab.gat.resources.cpi.gridsam.GridSAMJSDLGeneratorImpl class


6. How to run (having OMII client already installed)?
The best start is to run the resources.SubmitJobGridSAM class through JavaGAT. It connects with GridSAM server installed on fs0.das3.cs.vu.nl:18443 but through a tunnel so it might be advisable to change the address.
The output is saved in outputFile in the directory where the file was run.
IMPORTANT: For now hostname for sandbox is hardcoded. Please change it in GridSAMResourceBroker in line 124 (sandbox creation where "das3" is hardcoded).

OMII server is installed on mwi300@fs0.das3.cs.vu.nl - feel free to change things there.  


7. What does not work
- ssh and sftp file adapter
- errors in commandlineSshFileAdapter - isDirectory returns false because it didn't work...

TODO:
- find the cause of ssh file adapter problem (hard) - try getting to an earlier revision - maybe it worked
- take a look at GridSAM change listener - maybe we can use it for metric measurements (without separate thread)