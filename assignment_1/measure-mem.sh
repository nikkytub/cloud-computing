#!/bin/bash

EXECUTABLE="memsweep"
if [ ! -e $EXECUTABLE ] ; then
	# Compiling memsweep.c (requires GNU compiler collection)
	gcc -O -o memsweep memsweep.c -lm
fi

#echo "time,value" > mem.csv

# Running memsweep benchmark
if [ "$SYSTEMROOT" = "C:\Windows" ] ; then
	result=$(./memsweep.exe | tail -1)
else
	result=$(./${EXECUTABLE} | tail -1)
fi

echo $result