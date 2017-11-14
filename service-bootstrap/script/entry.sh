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

exec \$JAVA_HOME/bin/java \$JVM_MEMORY \$JVM_GARBAGE -classpath "\$APP_HOME/lib/*" ${mainClassName}