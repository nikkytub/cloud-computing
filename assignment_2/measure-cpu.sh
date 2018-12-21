#!/bin/bash
EXECUTABLE="linpack"
if [ ! -e $EXECUTABLE ] ; then
	echo "Compiling linpack.c (requires GNU compiler collection)"
	gcc -O -o linpack linpack.c -lm
fi

echo "time,value" > cpu.csv

echo "Running linpack benchmark"
while [ : ]
do
if [ "$SYSTEMROOT" = "C:\Windows" ] ; then
	result=$(./linpack.exe | tail -1)
else
	result=$(./${EXECUTABLE} | tail -1)
fi
echo "Benchmark result: $result KFLOPS"
echo "$(date +%s),$result" >> cpu.csv
sleep 3s
done