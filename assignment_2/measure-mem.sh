#!/bin/bash
EXECUTABLE="memsweep"
if [ ! -e $EXECUTABLE ] ; then
	echo "Compiling memsweep.c (requires GNU compiler collection) "
	gcc -O -o memsweep memsweep.c -lm
fi

echo "time,value" > mem.csv

echo "Running memsweep benchmark"
while [ : ]
do
if [ "$SYSTEMROOT" = "C:\Windows" ] ; then
	result=$(./memsweep.exe | tail -1)
else
	result=$(./${EXECUTABLE} | tail -1)
fi
echo "Benchmark result: $result seconds"
echo "$(date +%s),$result" >> mem.csv
sleep 3s
done