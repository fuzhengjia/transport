#!/bin/bash
TOMCAT_PATH=/home/robert/Downloads/apache-tomcat-8.0.27
APP=transport

echo "copy classes into ${TOMCAT_PATH}/webapps/$APP/WEB-INF/classes/ ..."
scp ../out/* ${TOMCAT_PATH}/webapps/${APP}/WEB-INF/classes/

echo "copy classes into $TOMCAT_PATH/webapps/$APP/WEB-INF/lib/ ..."
scp ../lib/* ${TOMCAT_PATH}/webapps/${APP}/WEB-INF/lib/

echo "restart tomcat service to take the change into effect"

"$TOMCAT_PATH/bin/shutdown.sh"
"$TOMCAT_PATH/bin/startup.sh"

echo "done!"
