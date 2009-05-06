#!/bin/bash

export JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Home
cd ../advert-lib/.
ant
cp lib/* ../app-engine/adaptors/AppEngine/external/.
cd ../app-engine/.
ant
cp lib/adaptors/AppEngineAdaptor/*.jar lib/.
rm lib/AppEngineAdaptor.jar
./bin/run-gat-app examples20.EndpointExample http://bbn230.appspot.com/ ibisappengine@gmail.com k9chaBay local
