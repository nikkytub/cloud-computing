#!/bin/bash

EXECUTABLE="linpack"
if [ ! -e $EXECUTABLE ] ; then
	# Compiling linpack.c (requires GNU compiler collection)
	gcc -O -o linpack linpack.c -lm
fi

# Running linpack benchmark
if [ "$SYSTEMROOT" = "C:\Windows" ] ; then
	result=$(./linpack.exe | tail -1)
else
	result=$(./${EXECUTABLE} | tail -1)
fi

echo $result