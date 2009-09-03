#!/bin/sh

echo "This is script 'date_script'"

# cat $1

echo "will not sleep 30 seconds"
#echo "will sleep 30 seconds" >> /tmp/date_script.log
#sleep 30

/bin/date
/bin/hostname -f

echo `/bin/date` >> /tmp/date_script.log
echo `/bin/hostname` >> /tmp/date_script.log

exit 0
