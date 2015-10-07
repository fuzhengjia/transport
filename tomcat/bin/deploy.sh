#!/bin/bash
TOMCAT_PATH=/home/robert/Downloads/apache-tomcat-8.0.27
APP=transport

echo "copy classes into ${TOMCAT_PATH}/webapps/$APP/WEB-INF/classes/ ..."
cp ../out/* ${TOMCAT_PATH}/webapps/${APP}/WEB-INF/classes/

echo "copy libraries into $TOMCAT_PATH/webapps/$APP/WEB-INF/lib/ ..."
cp ../lib/* ${TOMCAT_PATH}/webapps/${APP}/WEB-INF/lib/

echo "copy web.xml into ${TOMCAT_PATH}/webapps/${APP}/WEB-INF/"
cp ../web.xml ${TOMCAT_PATH}/webapps/${APP}/WEB-INF/

echo "restart tomcat service to take the change into effect"

"$TOMCAT_PATH/bin/shutdown.sh"
"$TOMCAT_PATH/bin/startup.sh"

echo "done!"
