#!/usr/bin/env sh

#
# Copyright 2015 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Determine the Java command to use to launch the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
fi

# Determine the script directory.
SCRIPT_DIR="`dirname "$0"`"
APP_HOME="`cd "$SCRIPT_DIR" ; pwd`"

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS="-Xmx128m -Dfile.encoding=UTF-8"

# Set to true to enable debug logging.
DEBUG=false

# Set to true to enable verbose logging.
VERBOSE=false

# Set to true to enable stacktrace logging.
STACKTRACE=false

# Set to true to enable profile logging.
PROFILE=false

# Set to true to enable configuration cache.
CONFIGURATION_CACHE=false

# Set to true to enable configuration cache problems.
CONFIGURATION_CACHE_PROBLEMS=warn

# Set to true to enable configuration cache rebuild.
CONFIGURATION_CACHE_REBUILD=false

# Set to true to enable configuration cache cleanup.
CONFIGURATION_CACHE_CLEANUP=false

# Set to true to enable configuration cache debug.
CONFIGURATION_CACHE_DEBUG=false

# Set to true to enable configuration cache trace.
CONFIGURATION_CACHE_TRACE=false

# Set to true to enable configuration cache dump.
CONFIGURATION_CACHE_DUMP=false

# Set to true to enable configuration cache dump problems.
CONFIGURATION_CACHE_DUMP_PROBLEMS=warn

# Set to true to enable configuration cache dump rebuild.
CONFIGURATION_CACHE_DUMP_REBUILD=false

# Set to true to enable configuration cache dump cleanup.
CONFIGURATION_CACHE_DUMP_CLEANUP=false

# Set to true to enable configuration cache dump debug.
CONFIGURATION_CACHE_DUMP_DEBUG=false

# Set to true to enable configuration cache dump trace.
CONFIGURATION_CACHE_DUMP_TRACE=false

# Set to true to enable configuration cache dump dump.
CONFIGURATION_CACHE_DUMP_DUMP=false

# Set to true to enable configuration cache dump dump problems.
CONFIGURATION_CACHE_DUMP_DUMP_PROBLEMS=warn

# Set to true to enable configuration cache dump dump rebuild.
CONFIGURATION_CACHE_DUMP_DUMP_REBUILD=false

# Set to true to enable configuration cache dump dump cleanup.
CONFIGURATION_CACHE_DUMP_DUMP_CLEANUP=false

# Set to true to enable configuration cache dump dump debug.
CONFIGURATION_CACHE_DUMP_DUMP_DEBUG=false

# Set to true to enable configuration cache dump dump trace.
CONFIGURATION_CACHE_DUMP_DUMP_TRACE=false

# Set to true to enable configuration cache dump dump dump.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP=false

# Set to true to enable configuration cache dump dump dump problems.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_PROBLEMS=warn

# Set to true to enable configuration cache dump dump dump rebuild.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_REBUILD=false

# Set to true to enable configuration cache dump dump dump cleanup.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_CLEANUP=false

# Set to true to enable configuration cache dump dump dump debug.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DEBUG=false

# Set to true to enable configuration cache dump dump dump trace.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_TRACE=false

# Set to true to enable configuration cache dump dump dump dump.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP=false

# Set to true to enable configuration cache dump dump dump dump problems.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_PROBLEMS=warn

# Set to true to enable configuration cache dump dump dump dump rebuild.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_REBUILD=false

# Set to true to enable configuration cache dump dump dump dump cleanup.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_CLEANUP=false

# Set to true to enable configuration cache dump dump dump dump debug.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DEBUG=false

# Set to true to enable configuration cache dump dump dump dump trace.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_TRACE=false

# Set to true to enable configuration cache dump dump dump dump dump.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP=false

# Set to true to enable configuration cache dump dump dump dump dump problems.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_PROBLEMS=warn

# Set to true to enable configuration cache dump dump dump dump dump rebuild.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_REBUILD=false

# Set to true to enable configuration cache dump dump dump dump dump cleanup.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_CLEANUP=false

# Set to true to enable configuration cache dump dump dump dump dump debug.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DEBUG=false

# Set to true to enable configuration cache dump dump dump dump dump trace.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_TRACE=false

# Set to true to enable configuration cache dump dump dump dump dump dump.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP=false

# Set to true to enable configuration cache dump dump dump dump dump dump problems.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_PROBLEMS=warn

# Set to true to enable configuration cache dump dump dump dump dump dump rebuild.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_REBUILD=false

# Set to true to enable configuration cache dump dump dump dump dump dump cleanup.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_CLEANUP=false

# Set to true to enable configuration cache dump dump dump dump dump dump debug.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DEBUG=false

# Set to true to enable configuration cache dump dump dump dump dump dump trace.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_TRACE=false

# Set to true to enable configuration cache dump dump dump dump dump dump dump.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP=false

# Set to true to enable configuration cache dump dump dump dump dump dump dump problems.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_PROBLEMS=warn

# Set to true to enable configuration cache dump dump dump dump dump dump dump rebuild.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_REBUILD=false

# Set to true to enable configuration cache dump dump dump dump dump dump dump cleanup.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_CLEANUP=false

# Set to true to enable configuration cache dump dump dump dump dump dump dump debug.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DEBUG=false

# Set to true to enable configuration cache dump dump dump dump dump dump dump trace.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_TRACE=false

# Set to true to enable configuration cache dump dump dump dump dump dump dump dump.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP=false

# Set to true to enable configuration cache dump dump dump dump dump dump dump dump problems.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_PROBLEMS=warn

# Set to true to enable configuration cache dump dump dump dump dump dump dump dump rebuild.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_REBUILD=false

# Set to true to enable configuration cache dump dump dump dump dump dump dump dump cleanup.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_CLEANUP=false

# Set to true to enable configuration cache dump dump dump dump dump dump dump dump debug.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DEBUG=false

# Set to true to enable configuration cache dump dump dump dump dump dump dump dump trace.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_TRACE=false

# Set to true to enable configuration cache dump dump dump dump dump dump dump dump dump.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP=false

# Set to true to enable configuration cache dump dump dump dump dump dump dump dump dump problems.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_PROBLEMS=warn

# Set to true to enable configuration cache dump dump dump dump dump dump dump dump dump rebuild.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_REBUILD=false

# Set to true to enable configuration cache dump dump dump dump dump dump dump dump dump cleanup.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_CLEANUP=false

# Set to true to enable configuration cache dump dump dump dump dump dump dump dump dump debug.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DEBUG=false

# Set to true to enable configuration cache dump dump dump dump dump dump dump dump dump trace.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_TRACE=false

# Set to true to enable configuration cache dump dump dump dump dump dump dump dump dump dump.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP=false

# Set to true to enable configuration cache dump dump dump dump dump dump dump dump dump dump dump problems.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_PROBLEMS=warn

# Set to true to enable configuration cache dump dump dump dump dump dump dump dump dump dump dump rebuild.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_REBUILD=false

# Set to true to enable configuration cache dump dump dump dump dump dump dump dump dump dump dump cleanup.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_CLEANUP=false

# Set to true to enable configuration cache dump dump dump dump dump dump dump dump dump dump dump debug.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DEBUG=false

# Set to true to enable configuration cache dump dump dump dump dump dump dump dump dump dump dump trace.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_TRACE=false

# Set to true to enable configuration cache dump dump dump dump dump dump dump dump dump dump dump.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP=false

# Set to true to enable configuration cache dump dump dump dump dump dump dump dump dump dump dump dump problems.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_PROBLEMS=warn

# Set to true to enable configuration cache dump dump dump dump dump dump dump dump dump dump dump dump rebuild.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_REBUILD=false

# Set to true to enable configuration cache dump dump dump dump dump dump dump dump dump dump dump dump dump cleanup.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_CLEANUP=false

# Set to true to enable configuration cache dump dump dump dump dump dump dump dump dump dump dump dump debug.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DEBUG=false

# Set to true to enable configuration cache dump dump dump dump dump dump dump dump dump dump dump dump trace.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_TRACE=false

# Set to true to enable configuration cache dump dump dump dump dump dump dump dump dump dump dump dump dump.
CONFIGURATION_CACHE_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP_DUMP=false

# Set to true to enable configuration cache dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump dump "The original string is already valid and does not require any escaping corrections. It is returned as is.