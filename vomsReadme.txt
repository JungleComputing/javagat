Especially due to the use of the jglobus libraries there are some pecularites 
about which one has to be careful when using the VomsProxy classes.


1.) I get an "Unknown CA" error (Could not get stream from secure socket).

Probably the VomsProxyManager is missing either your root certificate
or the root certificate of the server you are communicating with.
It is best, if you include all needed certificates in /etc/grid-security/certificates/.
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


2.) I get an error message with a lot of CRL stuff ("Error while setting CRLs"). 

This could result from CRLs in a /etc/grid-security/certificates/ while having cog.properties point
to another directory with CA certificates (like e.g. ~/.globus/certificates). 
I don't know exactly why, but there seems to be a problem with jglobus if e.g. ~/.globus/certificates
and /etc/grid-security/certificates are both present. For me, the problem
could be fixed by simply deleting all .crl files in /etc/grid-security/certificates/.

If this is not an option, you could try to delete the certificates directory in
~/.globus and set the path in ~/.globus/cog.properties to /etc/grid-security/certificates.


3.) I  get some cryptic error like "pad block corrupted"...
Check whether you have given all necessary information (password, host-dn, location
of your user-certificate etc.) as global preferences of the GAT-context.
The necessary preference keys are:

vomsUserCert - path to the user certificate
vomsUserKey - path to the user (private key)
vomsPassword - password to be used to decrypt the private key
vomsLifetime - the desired proxy lifetime in seconds
vomsHostDN - the distinguished name of the VOMS host (e.g. /DC=cz/DC=cesnet-ca/O=CESNET/CN=skurut19.cesnet.cz)
vomsServerURL - the URL of the voms server, without protocol (e.g. skurut19.cesnet.cz)
vomsServerPort - the port on which to connect to the voms server
VirtualOrganisation - The name of the virtual organisation for which the voms proxy is created (e.g. voce)

Do something like

GATContext context = new GATContext();
Preferences globalPrefs = new Preferences();
globalPrefs.put("vomsServerURL", "voms.cnaf.infn.it");
...
context.addPreferences(globalPrefs);

If you have given all necessary information, check the password you have given, for typos.


4.) I receive the following error message: Could not create VOMS proxy! failed: null:

See answer to question 3, maybe you forgot to specify the vomsServerPort preference value
or the vomsLifetime preference.


5.) Could not create VOMS proxy! failed: Could not open socket at the VOMS server!

Either you forgot to set the vomsServerURL preference value or you set either the
server url or the vomsServerPort preference to wrong values.
