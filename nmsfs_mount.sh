#!/bin/sh

#
# Usage: ./fakefs_mount.sh <conf_file.xml>

#FUSE_HOME=/usr
#JDK_HOME=/usr/lib/jvm/java-6-sun

java \
   $JAVA_OPTS \
   -classpath ./build/classes:./bin/fuse-jna.jar:./bin/jna.jar:./bin/marser_2.0.jar:./bin/snmp4j-2.0.3.jar \
   pt.ipb.snmpfs.SnmpFs $*
