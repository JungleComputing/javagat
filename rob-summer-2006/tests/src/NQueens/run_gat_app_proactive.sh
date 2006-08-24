#!/bin/sh

# you should modify these to variables to point to the 
# correct locations of your gat engine and adaptors

GAT_ENGINE_LOCATION=/home/team2/team2/GAT
GAT_ADAPTOR_LOCATION=/home/team2/team2/Adaptors


# ---- do not touch anything below this line ----

add_to_classpath () {
	DIRLIBS=${1}/*.jar
	for i in ${DIRLIBS}
	do
		 # if the directory is empty, then it will return the input string
		 # this is stupid, so case for it
		 if [ "$i" != "${DIRLIBS}" ] ; then
			if [ -z "$GAT_CLASSPATH" ] ; then
		GAT_CLASSPATH=$i
			else
		GAT_CLASSPATH="$i":$GAT_CLASSPATH
			fi
		 fi
	done
}

GAT_ENGINE_JAR=$GAT_ENGINE_LOCATION/lib/GAT.jar
GAT_ENGINE_EXTERNAL_JARS=$GAT_ENGINE_LOCATION/external

GAT_ADAPTORS=$GAT_ADAPTOR_LOCATION/lib
GAT_ADAPTOR_EXTERNAL_JARS=$GAT_ADAPTOR_LOCATION/external

add_to_classpath $GAT_ENGINE_EXTERNAL_JARS

#ProActive related
workingDir=`dirname $0`
. $workingDir/env.sh

OUR_CP=$GAT_ENGINE_JAR:$GAT_CLASSPATH:$GAT_ENGINE_LOCATION/tmp:.:$CLASSPATH

OUR_CP=.:$GAT_ADAPTORS/ProActiveBrokerAdaptor.jar:$OUR_CP

OUR_CP=nqueen.jar:$OUR_CP

echo $OUR_CP

export CLASSPATH=$OUR_CP

java -Dgat.adaptor.path=$GAT_ADAPTORS:$GAT_ADAPTOR_EXTERNAL_JARS -Djava.security.manager -Djava.security.policy=$PROACTIVE/scripts/proactive.java.policy -Dproactive.configuration=/home/team2/ProActiveConfiguration_team2.xml -Dproactive.useIPaddress=true $*
