#!/bin/bash

echo "Running random read disk benchmark"
echo "time,value" > disk-random.csv
while [ : ]
do
result=$(fio --name=random-read --readwrite=randread --bs=4k --size=1G --iodepth=64 \
--ioengine=libaio --direct=1 --randrepeat=1 --gtod_reduce=1 \
| grep "iops=" | sed 's/.*iops=\(.*\),.*/\1/' | xargs)

# Another variant as belows. For older versions of fio
# | grep "read: IOPS" | sed 's/read:\ IOPS=\(.*\),.*/\1/' | xargs)

echo "Benchmark result: $result IOPS"
echo "$(date +%s),$result" >> disk-random.csv
sleep 3s
done