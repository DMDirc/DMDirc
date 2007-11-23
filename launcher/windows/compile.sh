#!/bin/sh
fpc -Sd -Twin32 DMDirc.dpr
fpc -Sd -Twin32 DMDircUpdater.dpr
rm -Rf ./*.o ./*.or
