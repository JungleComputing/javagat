1) Pecularites of the VOMS proxy creation:
1.1) I get an "Unknown CA" error (Could not get stream from secure socket).

Probably the VomsProxyManager is missing either your root certificate
or the root certificate of the server you are communicating with.
It is best, if you include all needed certificates in ~/.globus/certificates/.
(e.g. you can copy the /etc/grid-security/certificates directory from an UI machine
of the VO you are trying to work with to that location).

If this doesn't suffice, you should try to include a file called cog.properties in the ~/.globus/
directory. The content of this file could be something like this:

Java CoG Kit Configuration File
#Thu Apr 05 15:59:23 CEST 2007
usercert=~/.globus/usercert.pem
userkey=~/.globus/userkey.pem
proxy=/tmp/x509up_u<your user id>
cacert=/etc/grid-security/certificates/
ip=<your ip address>

Also check the vomsHostDN preference value for typos/errors.


1.2) I get an error message with a lot of CRL stuff ("Error while setting CRLs"). 

Try to create a cog.properties file in ~/.globus and set the cacert property to ~/.globus/certificates.
This will cause the VOMS Proxy classes not to look for CRLs in /etc/grid-security/certificates


1.3) Which preference keys need to be set in order to have the VOMS proxy creation work?

The *necessary* preference keys are:
vomsHostDN - the distinguished name of the VOMS host (e.g. /DC=cz/DC=cesnet-ca/O=CESNET/CN=skurut19.cesnet.cz)
vomsServerURL - the URL of the voms server, without protocol (e.g. skurut19.cesnet.cz)
vomsServerPort - the port on which to connect to the voms server
VirtualOrganisation - The name of the virtual organisation for which the voms proxy is created (e.g. voce)

Additionally you need a CertificateSecurityContext which points to your user certificate and your user key.
Add that CertificateSecurityContext to the GATContext.

With these settings, the proxy is created with a standard lifetime of 12 hours. If you want to have a different
lifetime, add the following optional preference to the context:

vomsLifetime - (optional) the desired proxy lifetime in seconds. If not set, set to standard value (3600)

Do something like

GATContext context = new GATContext();
CertificateSecurityContext secContext = 
		new CertificateSecurityContext(new URI(your_key), new URI(your_cert), cert_pwd);
Preferences globalPrefs = new Preferences();
globalPrefs.put("vomsServerURL", "voms.cnaf.infn.it");
...
context.addPreferences(globalPrefs);
context.addSecurityContext(secContext);


1.4) I  get some cryptic error like "pad block corrupted"...
Check whether you have given all necessary information (password, host-dn, location
of your user-certificate etc., see question 2) as global preferences of the GAT-context.

If you have given all necessary information, check the password you have given, for typos.


1.5) I receive the following error message: Could not create VOMS proxy! failed: null:

See answer to question 3, maybe you forgot to specify the vomsServerPort preference value.


1.6) It's kind of confusing: I have to write the cog.properties file and I have to set
environment variables and GAT preferences!

Some efforts have been made to reduce the configuration effort: The X509_USER_PROXY environment variable need
not be set if there is a proxy= line (as in answer 1) in the cog.properties file.

So in order to run the adaptor, the following things have to present at minimum:
* A cog.properties file with lines as in answer 1.
* The following global preferences set in the gat context
 - vomsHostDN
 - vomsServerURL
 - vomsServerPort
 - VirtualOrganisation 
 
 
2) On the gLite-Adaptor itself:
2.1) Are there any proprietary properties which influence the behaviour of the adaptor?
 
Indeed, the mechanisms provided by the GAT-API alone did not suffice to provide all the control we found 
desirable for the adaptor. Hence, a few proprietary properties where introduced. They are useful
in controlling adaptor behaviour but are by no means necessary if one just wants to use the adaptor.
Nonetheless, they are documented here.
 
If you want to use them, set them using System.setProperty(); for example write 
System.setProperty("glite.pollIntervalSecs", "15").
The following properties are supported:

 glite.pollIntervalSecs - how often should the job lookup thread poll the WMS for job status updates and fire MetricEvents with status updates (value in seconds, default 3 seconds)
 glite.deleteJDL - if this is set to true, the JDL file used for job submission will be deleted when the job is done ("true"/"false", default is "false")
 

2.2) Why are so many attributes that should work according to the documentation of the SoftwareDescription unsupported?
 
The minimum supported attributes from the software description seem to be derived from the features
that RSL (the globus job submission file format) provides. Hence, they are easy to translate to RSL 
properties.
However, the format used for glite job submission is JDL and attributes like count or hostCount are
hard to translate to JDL. Most of the attributes that *are* supported are not even achieved by the JDL
format itself, but by adding GLUE requirements.
Sadly, the JDL format does not provide much of the functionality covered by RSLs, hence many attributes
remain unsupported.
 
On the other hand, to enable working with the features that the JDL format provides additionally to
the RSL format, a new attribute was introduced.
 
Set glite.retryCount to some String or Integer in order to use the retry count function of glite.