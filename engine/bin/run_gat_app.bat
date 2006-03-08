@echo off

IF "%GAT_LOCATION%X"=="X" set GAT_LOCATION=..


:: Don't touch anything below this line

set GAT_ENGINE_LOCATION=%GAT_LOCATION%\engine
set GAT_ADAPTOR_LOCATION=%GAT_LOCATION%\adaptors

set GAT_ENGINE_LIBS=%GAT_ENGINE_LOCATION%\lib
set GAT_ADAPTORS=%GAT_ADAPTOR_LOCATION%\lib

:: Create the path with the JAR files
SET GAT_CLASSPATH=

FOR %%i IN ("%GAT_ENGINE_LIBS%\*.jar") DO CALL "%GAT_ENGINE_LOCATION%\bin\AddToGATClassPath.bat" %%i

java -cp %CLASSPATH%;%GAT_CLASSPATH% -Dgat.adaptor.path=%GAT_ADAPTORS% %*
