#!/bin/bash

# Running random read disk benchmark
result=$(fio --name=random-read --readwrite=randread --bs=4k --size=16M --iodepth=64 \
--ioengine=libaio --direct=1 --randrepeat=1 --gtod_reduce=1 \
| grep "iops" | awk -F[avg=,] '{print $2}' | xargs)

echo $result