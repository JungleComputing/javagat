@echo off

java -DUID="%USERNAME%" -classpath "%GAT_LOCATION%\lib\adaptors\GlobusAdaptor\cog-jglobus.jar" org.globus.tools.ProxyInfo %*
