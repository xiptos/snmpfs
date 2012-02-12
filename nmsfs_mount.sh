#!/bin/sh

#
# Usage: ./fakefs_mount.sh /mount/point -f [ -s ]

FUSE_HOME=/usr
JDK_HOME=/usr/lib/jvm/java-6-sun

LD_LIBRARY_PATH=./jni:$FUSE_HOME/lib $JDK_HOME/bin/java \
   $JAVA_OPTS \
   -classpath ./build/classes:./bin/commons-logging.jar:./bin/fuse-j.jar:./bin/marser_1.0.jar:./bin/SNMP4J.jar \
   -Dorg.apache.commons.logging.Log=fuse.logging.FuseLog \
   -Dfuse.logging.level=DEBUG \
   -Dcom.sun.management.jmxremote \
   pt.ipb.nmsfs.AgentFS $*
