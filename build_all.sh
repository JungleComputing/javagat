#!/bin/bash

export JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Home
cd ../advert-lib/.
pwd
ant
cp -v lib/* ../app-engine/adaptors/AppEngine/external/.
cd ../app-engine/.
pwd
ant
#cp -v lib/adaptors/AppEngineAdaptor/*.jar lib/.
#rm -v lib/AppEngineAdaptor.jar
chmod -v 700 run_all.sh
./run_all.sh
