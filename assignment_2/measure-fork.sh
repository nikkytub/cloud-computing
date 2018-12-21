#!/bin/bash
EXECUTABLE="forksum"
if [ ! -e $EXECUTABLE ] ; then
	echo "Compiling forksum.c (requires GNU compiler collection)"
	gcc -O -o forksum forksum.c -lm
fi

echo "time,value" > fork.csv

echo "Running linpack benchmark"
while [ : ]
do
if [ "$SYSTEMROOT" = "C:\Windows" ] ; then
	result=$(./forksum.exe | tail -1)
else
	result=$(./${EXECUTABLE} | tail -1)
fi
echo "Benchmark result: $result"
echo "$(date +%s),$result" >> fork.csv
sleep 3s
done