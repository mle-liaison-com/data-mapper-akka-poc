#!/bin/sh

PRG="\$0"
# Need this for relative symlinks.
while [ -h "\$PRG" ] ; do
    ls=`ls -ld "\$PRG"`
    link=`expr "\$ls" : '.*-> \\(.*\\)\$'`
    if expr "\$link" : '/.*' > /dev/null; then
        PRG="\$link"
    else
        PRG=`dirname "\$PRG"`"/\$link"
    fi
done
SAVED="`pwd`"
cd "`dirname \"\$PRG\"`/${appHomeRelativePath}" >/dev/null
APP_HOME="`pwd -P`"
cd "\$SAVED" >/dev/null

JVM_MEMORY="-Xms1024m -Xmx1024m"
JVM_GARBAGE="-XX:+UseG1GC"

# Akka service specific variables
AKKA_OPTS=""
if [ \${APPLICATION_ID:+1} ] ; then
    AKKA_OPTS="\$AKKA_OPTS -Dakka.deployment.applicationId=\$APPLICATION_ID";
else
    echo APPLICATION_ID not set >&2;
fi
if [[ \${STACK:+1} ]]; then
    AKKA_OPTS="\$AKKA_OPTS -Dakka.deployment.stack=\$STACK";
else
    echo STACK not set >&2;
fi
if [ \${ENVIRONMENT:+1} ]; then
    AKKA_OPTS="\$AKKA_OPTS -Dakka.deployment.environment=\$ENVIRONMENT";
else
    echo ENVIRONMENT not set >&2;
fi
if [ \${REGION:+1} ]; then
    AKKA_OPTS="\$AKKA_OPTS -Dakka.deployment.region=\$REGION";
else
    echo REGION not set >&2;
fi
if [ \${DATACENTER:+1} ]; then
    AKKA_OPTS="\$AKKA_OPTS -Dakka.deployment.datacenter=\$DATACENTER";
else
    echo DATACENTER not set >&2;
fi
if [ \${ADDITIONAL_URLS:+1} ]; then
    AKKA_OPTS="\$AKKA_OPTS -Dakka.configurationSource.additionalUrls=\$ADDITIONAL_URLS";
else
    echo ADDITIONAL_URLS not set >&2;
fi

exec \$JAVA_HOME/bin/java \$JVM_MEMORY \$JVM_GARBAGE \$AKKA_OPTS -classpath "\$APP_HOME/lib/*" ${mainClassName}