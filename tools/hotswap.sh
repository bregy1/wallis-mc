#!/usr/bin/env bash
# Recompiles and pushes changed classes into a dev game started with -PhotReload.
set -e
JDK="${JDK17:-C:/Program Files/Java/jdk-17}"
export JAVA_HOME="$JDK"
./gradlew classes --console=plain -q
"$JDK/bin/java" tools/HotSwap.java
