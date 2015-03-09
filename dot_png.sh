#!/bin/bash

# Compile Vars
path='/home/hanplusplus/Coding/Java/PL241-Compiler/viz/'

dot -Tpng $path*.dot -O
