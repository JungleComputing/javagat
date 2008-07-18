#!/bin/sh

echo "This is script 'date_script'"

# cat $1
echo "I was called with: $*"
echo "will sleep 10 seconds"
echo "I was called with: $*" >> $HOME/date_script.out
echo "will sleep 10 seconds" >> $HOME/date_script.out
sleep 10

/bin/date >> $HOME/date_script.out

echo "Current job state: Done "
echo "bla bla"
#cat date.in > date.out
exit 3
