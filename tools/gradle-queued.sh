#!/usr/bin/env bash
# Runs Gradle without a daemon, one build at a time across all worktrees: this machine has 16 GB, and several agents
# compiling at once would run it out of memory. Usage: bash tools/gradle-queued.sh compileJava
set -u
LOCK="$HOME/.wallis-gradle.lock"
LOCK_STALE_MINUTES=10
export JAVA_HOME="${JDK17:-C:/Program Files/Java/jdk-17}"

until mkdir "$LOCK" 2> /dev/null; do
    if [ -n "$(find "$LOCK" -maxdepth 0 -mmin +$LOCK_STALE_MINUTES 2> /dev/null)" ]; then
        rm -rf "$LOCK"
    fi
    sleep 3
done
trap 'rm -rf "$LOCK"' EXIT
./gradlew --no-daemon --console=plain "$@"
