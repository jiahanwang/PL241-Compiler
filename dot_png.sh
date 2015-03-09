#!/bin/bash

# Compile Vars
path_1='/home/hanplusplus/Coding/Java/PL241-Compiler/viz/unoptimized/'
path_2='/home/hanplusplus/Coding/Java/PL241-Compiler/viz/cp/'
path_3='/home/hanplusplus/Coding/Java/PL241-Compiler/viz/cse/'

dot -Tpng $path_1*.dot -O
dot -Tpng $path_2*.dot -O
dot -Tpng $path_3*.dot -O
