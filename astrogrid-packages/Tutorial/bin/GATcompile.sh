#!/bin/sh

# compile a test class...

# must be invoked with the name of the java-file

set -e

file=$1

if [ "$1" = "" ]
then
   echo "missing file name"
   echo " right call: GATcompile.sh <java prog> "
   exit 1

fi

javac -cp /home/ali/programme/java-gat-trunk-21-02-2008/lib/GAT-API.jar:/home/ali/programme/java-gat-trunk-21-02-2008/lib/GAT-engine.jar $file

exit $?
