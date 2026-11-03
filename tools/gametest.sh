#!/usr/bin/env bash
# Runs every GameTest inside the real dev server and prints the results.
# Exit code = number of failed tests. Needs RCON enabled in run/server.properties (see CLAUDE.md).
set -u
JDK="${JDK17:-C:/Program Files/Java/jdk-17}"
LOG="build/gametest-server.log"
mkdir -p build
export JAVA_HOME="$JDK"

./gradlew runServer --console=plain > "$LOG" 2>&1 &
SERVER_PID=$!

for _ in $(seq 1 300); do
    grep -qE "RCON running|FAILURE:|Crash report" "$LOG" 2>/dev/null && break
    sleep 1
done
if ! grep -q "RCON running" "$LOG"; then
    echo "Server did not start; see $LOG"
    exit 100
fi

TOTAL=$("$JDK/bin/java" tools/RconClient.java "test runall" | grep -oE "Running all [0-9]+ tests" | grep -oE "[0-9]+")
TOTAL=${TOTAL:-0}
if [ "$TOTAL" -eq 0 ]; then
    echo "The server did not start any tests; see $LOG"
    "$JDK/bin/java" tools/RconClient.java "stop" > /dev/null
    wait "$SERVER_PID"
    exit 100
fi
for _ in $(seq 1 300); do
    [ "$(grep -c "\[GAMETEST\]" "$LOG")" -ge "$TOTAL" ] && break
    sleep 1
done

"$JDK/bin/java" tools/RconClient.java "stop" > /dev/null
wait "$SERVER_PID"

grep -oE "\[GAMETEST\] .*" "$LOG"
DONE=$(grep -c "\[GAMETEST\]" "$LOG")
FAILED=$(grep -c "\[GAMETEST\] FAILED" "$LOG")
echo "$DONE/$TOTAL tests reported, $FAILED failed"
if [ "$DONE" -lt "$TOTAL" ]; then
    echo "Some tests did not report in time; see $LOG"
    exit 100
fi
exit "$FAILED"
