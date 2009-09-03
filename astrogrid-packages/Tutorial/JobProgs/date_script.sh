#!/bin/sh
set -vx

echo "This is script 'date_script'"

# cat $1
echo "I was called with: $*"
echo "will sleep 10 seconds"
sleep 10

/bin/date

echo "Current job state: Done "
echo "bla bla"
#cat date.in > date.out
exit 4
