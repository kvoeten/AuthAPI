@echo off
@title Auth/API Service
set CLASSPATH=.;dist\*
java com.kazvoeten.authapi.Application
pause