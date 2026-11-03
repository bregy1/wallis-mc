#!/usr/bin/env bash
# Client smoke test: starts the dev client, which creates a fresh survival world, walks around, runs the steps in
# src/main/java/wallis_ultimate/client/smoketest, takes screenshots (run/screenshots/smoketest-*.png) and quits.
# Prints the [SMOKETEST] lines and any errors. Exit code = number of failed checks, 100 if the run didn't finish.
# With an argument like -PsmokeOnly=sites,props it only runs those groups of steps (see ClientSmokeTest.allSteps).
set -u
JDK="${JDK17:-C:/Program Files/Java/jdk-17}"
LOG="build/smoketest-client.log"
mkdir -p build
export JAVA_HOME="$JDK"

# Its window is hidden, so a client that hangs or sits at Forge's error screen has to be stopped from here.
stop_client() {
    powershell -NoProfile -Command "Get-CimInstance Win32_Process -Filter \"name like 'java%'\" | Where-Object { \$_.CommandLine -like '*wallis_ultimate.smoketest=true*' } | ForEach-Object { Stop-Process -Id \$_.ProcessId -Force }"
}

# Only the smoke test's own leftovers: its worlds and screenshots from the last run.
rm -rf run/saves/wallis-smoketest*
rm -f run/screenshots/smoketest-*.png

./gradlew runClient -PsmokeTest ${1:-} --console=plain > "$LOG" 2>&1 &
CLIENT_PID=$!

# The client quits by itself (also after its own 15-minute limit); this only catches a hang before that.
for _ in $(seq 1 1200); do
    kill -0 "$CLIENT_PID" 2>/dev/null || break
    if grep -qa -E "Missing or unsupported mandatory dependencies|ModLoadingException" "$LOG"; then
        echo "Mods failed to load:"
        grep -a -E "Mod ID:|Failure message" "$LOG" | head -20
        break
    fi
    sleep 1
done
if kill -0 "$CLIENT_PID" 2>/dev/null; then
    stop_client
    sleep 2
    if ! grep -qa "\[SMOKETEST\] DONE" "$LOG"; then
        echo "The client didn't finish and was stopped; see $LOG"
        exit 100
    fi
fi

grep -a -oE "\[SMOKETEST\] .*" "$LOG"
echo "--- errors:"
grep -a -E "Crash report saved|VerifyError|Exception in|/ERROR\]|/FATAL\]" "$LOG" | grep -v "Realms" | head -40
echo "--- screenshots:"
ls run/screenshots/smoketest-*.png 2>/dev/null
if grep -qa "Crash report saved" "$LOG" || ! grep -qa "\[SMOKETEST\] DONE" "$LOG"; then
    echo "The smoke test did not finish; see $LOG"
    exit 100
fi
exit "$(grep -ac "\[SMOKETEST\] FAIL" "$LOG")"
