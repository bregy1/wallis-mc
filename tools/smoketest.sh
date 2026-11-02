#!/usr/bin/env bash
# Client smoke test: starts the dev client, which creates a fresh survival world, walks around, runs the steps in
# src/main/java/wallis_ultimate/client/smoketest, takes screenshots (run/screenshots/smoketest-*.png) and quits.
# Prints the [SMOKETEST] lines and any errors. Exit code = number of failed checks, 100 if the run didn't finish.
set -u
JDK="${JDK17:-C:/Program Files/Java/jdk-17}"
LOG="build/smoketest-client.log"
mkdir -p build
export JAVA_HOME="$JDK"

# Only the smoke test's own leftovers: its worlds and screenshots from the last run.
rm -rf run/saves/wallis-smoketest*
rm -f run/screenshots/smoketest-*.png

./gradlew runClient -PsmokeTest --console=plain > "$LOG" 2>&1 &
CLIENT_PID=$!

# The client quits by itself (also after its own 5-minute limit); this only catches a hang before that.
for _ in $(seq 1 600); do
    kill -0 "$CLIENT_PID" 2>/dev/null || break
    sleep 1
done
if kill -0 "$CLIENT_PID" 2>/dev/null; then
    echo "The client is still running after 10 minutes; see $LOG"
    exit 100
fi

grep -oE "\[SMOKETEST\] .*" "$LOG"
echo "--- errors:"
grep -E "Crash report saved|VerifyError|Exception in|/ERROR\]|/FATAL\]" "$LOG" | grep -v "Realms" | head -40
echo "--- screenshots:"
ls run/screenshots/smoketest-*.png 2>/dev/null
if grep -q "Crash report saved" "$LOG" || ! grep -q "\[SMOKETEST\] DONE" "$LOG"; then
    echo "The smoke test did not finish; see $LOG"
    exit 100
fi
exit "$(grep -c "\[SMOKETEST\] FAIL" "$LOG")"
