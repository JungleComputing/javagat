@echo off

setlocal ENABLEDELAYEDEXPANSION 

if ERRORLEVEL 1 goto error    

set GAT_ENGINE_LOCATION=C:\project\gridlab\gat\Codes\GATEngine\java
set GAT_ADAPTOR_LOCATION=C:\project\gridlab\gat\Codes\Adaptors\java

set OGSA=%GAT_ADAPTOR_LOCATION%\external
set GAT_ADAPTORS=%GAT_ADAPTOR_LOCATION%\lib
set GAT_JAR=%GAT_ENGINE_LOCATION%\lib\GAT.jar
set GAT_EXTERNAL=%GAT_ENGINE_LOCATION%\external

set OGSA_JARS=
set GAT_ADAPTOR_JARS=

for %%i in (%OGSA%\*.jar) do set OGSA_JARS=!OGSA_JARS!;%%i
for %%i in (%GAT_ADAPTORS%\*.jar) do set GAT_ADAPTOR_JARS=!GAT_ADAPTOR_JARS!;%%i
for %%i in (%GAT_EXTERNAL%\*.jar) do set GAT_ADAPTOR_JARS=!GAT_ADAPTOR_JARS!;%%i
set OUR_CP=%OGSA_JARS%%GAT_ADAPTOR_JARS%;%GAT_JAR%;%GAT_ENGINE_LOCATION%\tmp;.;%CLASSPATH%

java -cp %OUR_CP% -Dgat.adaptor.path=%GAT_ADAPTORS% %*

goto :EOF


:error

echo on
echo Batch file error: Cannot delay variable expansion
