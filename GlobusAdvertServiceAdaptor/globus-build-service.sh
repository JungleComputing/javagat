#!/bin/bash

#####
#
# Build script for GT4 Web services
# GSBT - Globus Service Build Tools
# http://gsbt.sourceforge.net/
#
# Version 0.2.5
# Full changelog available at the GSBT website
#
# Copyright (c) 2004 Borja Sotomayor
# The Globus Service Build Tools are available for use and redistribution 
# under the terms of a BSD license available at http://gsbt.sourceforge.net/license.html
#
######


# Some useful constants
SUCCESS=0
FAILURE=1
VALID_TARGETS="compileStubs stubs jarStubs all"



#
# FUNCTIONS
#


# Prints script usage
# No parameters
printUsage() {
echo ""
echo "Usage:"
echo "$0 -d <service_dir> -s <schema_file> [-fs <factory_schema_file>] [-t <target>] [--debug]"
echo "$0 <service_id> [target] [--debug]"
echo "$0 -h"
echo ""
echo "<service_dir> is the directory that contains all the implementation and deployment files:"
echo -e "\t<service_dir>/deploy-server.wsdd         Deployment file (mandatory)"
echo -e "\t<service_dir>/deploy-jndi-config.wsdd    JNDI deployment file (mandatory)"
echo -e "\t<service_dir>/impl/*.java                Java implementation files (mandatory)"
echo -e "\t<service_dir>/etc/*.xml                  Configuration files (optional)"
echo ""
echo "<schema_file> is the WSDL file with the service's interface description"
echo ""
echo "<factory_schema_file> is an optional parameter. If your service is a "
echo "factory/instance service, you can use this parameter to specify the factory's"
echo "schema file."
echo ""
echo "<target> is an optional parameter to control what Ant builds. Valid values are"
echo -e "\t all     Builds everything (default)"
echo -e "\t stubs   Generates the stubs (but doesn't compile them)."
echo -e "\t compileStubs   Generates and compiles the stubs."
echo ""
echo "--debug provides detailed information of what the build script is doing."
echo ""
echo "The script offers a shorthand way of building services through the <service_id>"
echo "parameter. It allows you to build services without having to type the"
echo "service directory and schema file every time. You must have a 'build.mappings'"
echo "file in the same directory as the build script, with one line for each service"
echo "using the following format:"
echo -e "\t<service_id>,<service_dir>,<schema_file>,<factory_schema_file>"
echo ""
echo -e "\t (the <factory_schema_file> is optional)"
}



# Builds service
# Parameter 1: Service base directory
# Parameter 2: Schema file (WSDL file)
# Parameter 3: Factory schema file (WSDL file)
# Parameter 4: Ant build target
# Parameter 5: Additional ant arguments
build() {
SERVICE_DIR=$1
SCHEMA_PATH=$2
FACTORY_SCHEMA_PATH=$3
TARGET=$4
ANT_ARGS=$5
if [ $(echo $SERVICE_DIR | rev | cut -c1) == "/" ]
then
    SERVICE_DIR=$(echo $SERVICE_DIR | rev | cut -c2- | rev) 
fi

PACKAGE=$(echo $SERVICE_DIR | sed "s/\//\./g")
SCHEMA_DIR=$(echo $SCHEMA_PATH | cut -d/ -f2- | rev | cut -d/ -f2- | rev)
SERVICE_NAME=$(echo $SCHEMA_DIR | rev | cut -d/ -f1 | rev)
SCHEMA_FILE=$(echo $SCHEMA_PATH | rev | cut -d/ -f1 | rev)
INTERFACE=$(echo $SCHEMA_FILE | rev | cut -d. -f2 | rev)
GAR_FILENAME=$(echo $PACKAGE | sed "s/\./_/g")

COMMAND="ant $ANT_ARGS $TARGET -Dpackage=$PACKAGE -Dinterface.name=$INTERFACE -Dpackage.dir=$SERVICE_DIR -Dschema.path=$SCHEMA_DIR -Dservice.name=$SERVICE_NAME -Dgar.filename=$GAR_FILENAME"

if [ "$FACTORY_SCHEMA_PATH" != "" ]
then
	FACTORY_SCHEMA_DIR=$(echo $FACTORY_SCHEMA_PATH | cut -d/ -f2- | rev | cut -d/ -f2- | rev)
	FACTORY_SCHEMA_FILE=$(echo $FACTORY_SCHEMA_PATH | rev | cut -d/ -f1 | rev)
	FACTORY_INTERFACE=$(echo $FACTORY_SCHEMA_FILE | rev | cut -d. -f2 | rev)
	COMMAND="$COMMAND -Dfactory.schema.path=$FACTORY_SCHEMA_DIR -Dfactory.interface.name=$FACTORY_INTERFACE"
fi

if [ $DRYRUN -eq 1 ]
then
    echo $COMMAND
else
    eval $COMMAND
fi

return $?;
}



# Checks if the target is valid
# Parameter 1: Target
validateTarget() {
	TARGET=$1
	for target in $VALID_TARGETS
	do
		if [ $target == $TARGET ]
		then
			return $SUCCESS
		fi
	done
	return $FAILURE
}


# Given a valid service id, fetches that service's base directory and schema file
# The information is deposited in SERVICE_DIR and SCHEMA_PATH
# Parameter 1: Service id
findBuildParameters() {
	SERVICE_ID=$1
	grep "^$SERVICE_ID," build.mappings > /dev/null
	if [ ! $? -eq 0 ]
	then
		return $FAILURE
	fi
 	SERVICE_DIR=$(grep "^$SERVICE_ID," build.mappings| cut -f2 -d,)
	SCHEMA_PATH=$(grep "^$SERVICE_ID," build.mappings| cut -f3 -d,)
	FACTORY_SCHEMA_PATH=$(grep "^$SERVICE_ID," build.mappings| cut -f4 -d,)
	return $SUCCESS
}



#
# SCRIPT MAIN BODY
#

SERVICE_DIR=""
SCHEMA_PATH=""
FACTORY_SCHEMA_PATH=""
SERVICE_ID=""
TARGET=""
DEBUG=0
DRYRUN=0

# Check for GLOBUS_LOCATION

if [ -z "${GLOBUS_LOCATION}" ]; then
    echo "ERROR: environment variable GLOBUS_LOCATION not defined."
	echo "Run $0 -h for help."
    exit $FAILURE
fi

if [ ! -d "${GLOBUS_LOCATION}" ]; then
    echo "ERROR: invalid GLOBUS_LOCATION set: $GLOBUS_LOCATION"
   	echo "Run $0 -h for help."
    exit $FAILURE
fi


# Get command-line parameters

if [ $# -eq 0 ]
then
	printUsage
	exit $FAILURE
fi

while [ "$1" != "" ]; do
	case $1 in
		-d | --dir )    
			shift        
			SERVICE_DIR=$1
			;;
		-s | --schema )         
			shift
			SCHEMA_PATH=$1
			;;
		-fs | --factory-schema )         
			shift
			FACTORY_SCHEMA_PATH=$1
			;;
		-t | --target )
			shift
			TARGET=$1
			;;
		-h | --help )           
			printUsage
			exit
			;;
                --dry-run )
		        DRYRUN=1
			;;
		--debug )               
			DEBUG=1 
			;;
		* )                     
			# First non-option parameter must be the service id
			if [ "$SERVICE_ID" == "" ]
			then
				SERVICE_ID=$1
			elif [ "$TARGET" == "" ]
			# Second non-option parameter must be the target
			then
				TARGET=$1
			else
			# The script is confused!
				echo "ERROR: parameter '$1' not recognized"
				echo "Run $0 -h for help."
			fi
    esac
    shift
done




#
# VALIDATE PARAMETERS
#

# First off, you can't specify a service id *and* a dir/schema pair.
if [[ "$SERVICE_ID" != "" && ( "$SERVICE_DIR" != "" || "$SCHEMA_PATH" != "" ) ]]
then
	echo "ERROR: Please specify either a service id *or* a service directory and schema file"
	echo "       (but not both)"
	echo "Run $0 -h for help."
	exit $FAILURE
fi

# If a service id has been specified, we need to fetch SERVICE_DIR and SCHEMA_PATH from the
# build.mappings file
if [[ "$SERVICE_ID" != "" ]]
then
    findBuildParameters $SERVICE_ID
    SERVICE_EXISTS=$?
    if [ $SERVICE_EXISTS -eq $FAILURE ]
    then
    	echo "ERROR: Specified service not in mappings file."
    	echo "Run $0 -h for help."
    	exit $FAILURE
    fi
fi

# Now we have a SERVICE_DIR and SCHEMA_PATH. Let's make sure they exist.
if [ ! -e $SERVICE_DIR -o ! -d $SERVICE_DIR ]
then
	echo "$SERVICE_DIR does not exist or is not a directory."
	echo "Run $0 -h for help."
	exit $FAILURE
fi

if [ ! -e $SCHEMA_PATH -o ! -f $SCHEMA_PATH ]
then
	echo "$SCHEMA_PATH does not exist or is not a file."
	echo "Run $0 -h for help."
	exit $FAILURE
fi

# If we have a factory schema path, make sure it exists:
if [ "$FACTORY_SCHEMA_PATH" != "" ]
then
	if [ ! -e $FACTORY_SCHEMA_PATH -o ! -f $FACTORY_SCHEMA_PATH ]
	then
		echo "$FACTORY_SCHEMA_PATH does not exist or is not a file."
		echo "Run $0 -h for help."
		exit $FAILURE
	fi
fi


# If no target has been specified, the default target will be "all"
if [ "$TARGET" == "" ]
then
	TARGET="all"
fi

# Validate that the target is correct
validateTarget $TARGET
TARGET_EXISTS=$?
if [ $TARGET_EXISTS -eq $FAILURE ]
then
	echo "ERROR: Unknown target."
	echo "Run $0 -h for help."
	exit $FAILURE
fi



#
# CONSTRUCT 'ADDITIONAL PARAMETERS' STRING
#

ANT_ARGS=""
# Debug?
if [ $DEBUG -eq 1 ]
then
	ANT_ARGS="$ANT_ARGS -debug"
fi



#
# If you got this far, you deserve to...
# BUILD!
#
build $SERVICE_DIR $SCHEMA_PATH "$FACTORY_SCHEMA_PATH" $TARGET $ANT_ARGS
BUILD_STATUS=$?
exit $BUILD_STATUS
