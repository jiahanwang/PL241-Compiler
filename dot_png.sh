#!/bin/bash

# Compile Vars
path='/home/hanplusplus/Coding/Java/PL241-Compiler/viz/test0'

#opt level
for i in `seq -w 0 31`;
do 
	dot -Tpng $path$i.dot -o $path$i.dot.png
done
