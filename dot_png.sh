#!/bin/bash

# Compile Vars
path_1='/home/hanplusplus/Coding/Java/PL241-Compiler/viz/unoptimized/'
path_2='/home/hanplusplus/Coding/Java/PL241-Compiler/viz/cp/'
path_3='/home/hanplusplus/Coding/Java/PL241-Compiler/viz/cse/'
path_4='/home/hanplusplus/Coding/Java/PL241-Compiler/viz/register/'
path_5='/home/hanplusplus/Coding/Java/PL241-Compiler/viz/ig/'

dot -Tpng $path_1*.dot -O
dot -Tpng $path_2*.dot -O
dot -Tpng $path_3*.dot -O
dot -Tpng $path_4*.dot -O
dot -Tpng $path_5*.dot -O
