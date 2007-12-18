Adaptor between the JavaGAT-AdvertService and the globus MDS index service
==========================================================================

1. Compiling and deploying the adaptor
--------------------------------------

In the directory $GAT_LOCATION/GlobusAdvertServiceAdaptor say:
./globus-build-service.sh AdvertServiceEntry
./globus-build-service.sh IndexServiceProxyService
cp $GAT_LOCATION/GlobusAdvertServiceAdaptor/build/lib/services_IndexServiceProxyService_stubs.jar $GAT_LOCATION/adaptors/external/

This will build the grid archive files services_AdvertServiceEntry.gar and services_IndexServiceProxyService.gar which can be deployed in any globus-4 container via:
globus-deploy-gar services_AdvertServiceEntry.gar
globus-deploy-gar services_IndexServiceProxyService.gar

In the directory $GAT_LOCATION say ant.

2. Configuring the globus side of the adaptor
---------------------------------------------

* in the file $GLOBUS_LOCATION/etc/services_AdvertServiceEntry/registration.xml set the PollIntervalMillis element to a suitable value. This determines the time, when an advertisable becomes visible in the Globus Index Service after adding it within GAT.

* in the file $GLOBUS_LOCATION/etc/services_IndexServiceProxyService/jndi-config.xml set the indexURI parameter to the URI of the globus MDS index service.

3. Using the adaptor within JavaGAT
-----------------------------------

...
GATContext context = new GATContext();
Preferences prefs = new Preferences();
prefs.put("AdvertService.adaptor.name", "GlobusAdvertServiceAdaptor");
prefs.put("AdvertService.globus.uri", "http://134.2.217.150:8443/wsrf/services/GAT/IndexServiceProxyService");
AdvertService advertService = GAT.createAdvertService(context, prefs);
...

The AdvertService.globus.uri preference is used by the adaptor for contacting the globus side of the adaptor.
