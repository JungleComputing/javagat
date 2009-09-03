#!/bin/sh
#set -vx

echo "This is script 'date_script'" > d.out

# cat $1
echo "I was called with: $*" >> d.out
echo "will sleep 100 seconds" >> d.out
/bin/date >> d.out
sleep 100

/bin/date >> d.out

echo "Current job state: Done " >> d.out
echo "bla bla" >> d.out
#cat date.in > date.out
hostname -f >> d.out
ls -la >> d.out
exit 4
