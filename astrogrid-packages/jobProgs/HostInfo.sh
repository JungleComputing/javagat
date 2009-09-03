#!/bin/sh
#
# HostInfo.sh
# 
# Author:       Hans-Martin Adorf
# Created on:   19. December 2007
# Copyright:    (c) 2007 MPA for Astrophysics, Garching
# Description:  Retrieves information about the execution host
# Usage:        HostInfo.sh <paramFile>
# Example:      HostInfo.sh HostInfo.out
#
# check whether a parameter file is provided
PARAMETER_FILE=$1
if [ "$1" = "" ]
then
# no parameter file provided, so use a hard-coded name for the output file
    OUT_FILE=HostInfo.out
else
# parameter file provided, so get the name for the output file using the outFile key
    KEY=outFile
    OUT_FILE=$(sed -n -e "s/^$KEY[ \t]*=[ \t]*\([/.a-zA-Z][-/._a-zA-Z0-9]*\).*/\1/p" $PARAMETER_FILE)
fi
#
# write host information to file
hostname > $OUT_FILE
date >> $OUT_FILE
pwd >> $OUT_FILE
ls >> $OUT_FILE
echo $PATH >> $OUT_FILE
# ant -version
# java -version
# gcc --version
#
# display host information in stdout
cat $OUT_FILE
echo ""
#
# delay return, so that the job can be detected as running
# sleep 5s
