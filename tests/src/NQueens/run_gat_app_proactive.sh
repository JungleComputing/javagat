#!/bin/sh

# you should modify these to variables to point to the 
# correct locations of your gat engine and adaptors

GAT_ENGINE_LOCATION=/var/scratch/ceriel/javagat/engine
GAT_ADAPTOR_LOCATION=/var/scratch/ceriel/javagat/adaptors
GAT_TEST_LOCATION=/var/scratch/ceriel/javagat/tests
PROACTIVE=/home2/ceriel/ProActive


# ---- do not touch anything below this line ----

add_to_classpath () {
	DIRLIBS=`cd "$1" && ls *.jar 2>/dev/null`
	for i in ${DIRLIBS}
	do
	    if [ -z "$GAT_CLASSPATH" ] ; then
		GAT_CLASSPATH="$1/$i"
	    else
		GAT_CLASSPATH="$GAT_CLASSPATH:$1/$i"
	     fi
	done
}

GAT_ENGINE_JAR=$GAT_ENGINE_LOCATION/lib/GAT.jar
GAT_ENGINE_EXTERNAL_JARS=$GAT_ENGINE_LOCATION/external

GAT_ADAPTORS=$GAT_ADAPTOR_LOCATION/lib
GAT_ADAPTOR_EXTERNAL_JARS=$GAT_ADAPTOR_LOCATION/external

add_to_classpath $GAT_ENGINE_EXTERNAL_JARS
add_to_classpath $GAT_TEST_LOCATION/lib

#ProActive related
workingDir=`dirname $0`
. $workingDir/env.sh

GAT_CLASSPATH="$GAT_ENGINE_JAR:$GAT_CLASSPATH:$GAT_ADAPTORS/ProActiveBrokerAdaptor.jar"
OUR_CP="$GAT_CLASSPATH:.:$CLASSPATH"

export CLASSPATH="$OUR_CP"

$JAVACMD -Dproactive.useIPaddress=true -Dgat.adaptor.path=$GAT_ADAPTORS:$GAT_ADAPTOR_EXTERNAL_JARS test.NQueens ....
