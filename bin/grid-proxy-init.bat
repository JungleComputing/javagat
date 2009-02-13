@echo off

FOR %%i IN ("%GAT_LOCATION%\lib\adaptors\shared\*.jar") DO CALL "%GAT_LOCATION%\bin\AddToGATClassPath.bat" %%i
java -DUID="%USERNAME%" -classpath "%GAT_LOCATION%\lib\adaptors\GlobusAdaptor\cog-jglobus.jar";"%GAT_CLASSPATH%" org.globus.tools.ProxyInit %*
