#!/bin/bash

# Creating input data
dd if=/dev/zero of=data.out bs=1M count=1 2>/dev/null

# Running sequential read disk benchmark
result=$(dd iflag=direct if=data.out of=/dev/null bs=4k 2>&1 | grep 'copied' | awk -F[,] '{print $4}' | awk '{print $1 " " $3}' | xargs)

echo $result