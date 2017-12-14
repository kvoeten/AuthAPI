@echo off
@title Nox
set CLASSPATH=.;dist\*
java -Xrunjdwp:transport=dt_socket,address=9002,server=y,suspend=n com.kazvoeten.authapi.Application
pause