@echo off

IF "%GAT_LOCATION%X"=="X" set GAT_LOCATION=.


:: Don't touch anything below this line

set GAT_API_LOCATION=%GAT_LOCATION%\api
set GAT_ENGINE_LOCATION=%GAT_LOCATION%\engine
set GAT_TESTS_LOCATION=%GAT_LOCATION%\tests
set GAT_ADAPTOR_LOCATION=%GAT_LOCATION%\adaptors
set GAT_GRIDLAB_ADAPTOR_LOCATION=%GAT_LOCATION%\gridlabAdaptors

set GAT_API_LIBS=%GAT_API_LOCATION%\lib
set GAT_ENGINE_LIBS=%GAT_ENGINE_LOCATION%\lib
set GAT_TESTS_LIB=%GAT_TESTS_LOCATION%\lib
set GAT_ADAPTORS=%GAT_ADAPTOR_LOCATION%\lib;%GAT_GRIDLAB_ADAPTOR_LOCATION%\lib

:: Create the path with the JAR files
SET GAT_CLASSPATH=

FOR %%i IN ("%GAT_API_LIBS%\*.jar") DO CALL "%GAT_LOCATION%\bin\AddToGATClassPath.bat" %%i
FOR %%i IN ("%GAT_ENGINE_LIBS%\*.jar") DO CALL "%GAT_LOCATION%\bin\AddToGATClassPath.bat" %%i
FOR %%i IN ("%GAT_TESTS_LIB%\*.jar") DO CALL "%GAT_LOCATION%\bin\AddToGATClassPath.bat" %%i

java -cp "%CLASSPATH%";"%GAT_CLASSPATH%" -Dlog4j.configuration=file:"%GAT_LOCATION%"\log4j.properties -Dgat.adaptor.path="%GAT_ADAPTORS%" %*
