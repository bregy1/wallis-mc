#!/usr/bin/env bash
# Tests a snapshot of the project in .testrun/ (git-ignored, with its own build and game folder), so work can go on
# in the project while it runs, and the game can be played from run/ at the same time.
# Runs the GameTests and the client smoke test on the files as they are right now, and prints the results. The last
# line names the snapshot; this commits exactly what was tested:
#   bash tools/testsnapshot.sh commit <tree> "message"
# `bash tools/testsnapshot.sh look [groups]` only runs the client with those groups of smoke steps (default: sites, the
# pictures of the structures). `bash tools/testsnapshot.sh gametests` only runs the GameTests.
# `bash tools/testsnapshot.sh snap` only remembers the files as they are, to test later with SNAPSHOT_TREE=<tree>.
# This machine can only run one game at a time, so runs from different worktrees wait for each other.
# Exit code = number of failures (100 if something didn't finish).
set -u

# Bash reads a script while running it, so editing this file during a run would break the run: run from a copy.
if [ -z "${TESTSNAPSHOT_COPY:-}" ]; then
    SCRIPT_COPY="$(mktemp)"
    cp "$0" "$SCRIPT_COPY"
    TESTSNAPSHOT_COPY="$SCRIPT_COPY" exec bash "$SCRIPT_COPY" "$@"
fi

ROOT="$(git rev-parse --show-toplevel)"
cd "$ROOT"
COPY=".testrun"
LOCK="$HOME/.wallis-test.lock"
LOCK_STALE_MINUTES=40

if [ "${1:-}" = "commit" ]; then
    NEW=$(git commit-tree "$2" -p HEAD -m "$3")
    git update-ref HEAD "$NEW"
    # Only the staged view moves; newer edits in the files stay as they are, uncommitted.
    git reset -q
    git log --oneline -1
    exit 0
fi

# Everything git would track right now, including new files, without touching the real index.
INDEX="$(git rev-parse --git-dir)/index.testsnapshot"
cp "$(git rev-parse --git-dir)/index" "$INDEX"
export GIT_INDEX_FILE="$INDEX"
git add -A . 2> /dev/null
TREE=$(git write-tree)
unset GIT_INDEX_FILE
rm -f "$INDEX"
if [ "${1:-}" = "snap" ]; then
    echo "$TREE"
    exit 0
fi
TREE="${SNAPSHOT_TREE:-$TREE}"

until mkdir "$LOCK" 2> /dev/null; do
    if [ -n "$(find "$LOCK" -maxdepth 0 -mmin +$LOCK_STALE_MINUTES 2> /dev/null)" ]; then
        rm -rf "$LOCK"
    fi
    sleep 10
done
trap 'rm -rf "$LOCK"' EXIT

mkdir -p "$COPY/run"
rm -rf "$COPY/src" "$COPY/pack" "$COPY/tools" "$COPY/docs"
git archive "$TREE" | tar -x -C "$COPY"
# The game folder's settings the tests need; these are never committed. In a worktree, they're in the main checkout.
MAIN="$(cd "$(git rev-parse --git-common-dir)/.." && pwd)"
for file in eula.txt server.properties options.txt; do
    [ -f "$COPY/run/$file" ] || cp "$MAIN/run/$file" "$COPY/run/$file"
done

cd "$COPY"
# A fresh world each time: tests leave things behind, and the next run's tests can land on them.
rm -rf run/world
if [ "${1:-}" = "look" ]; then
    bash tools/smoketest.sh "-PsmokeOnly=${2:-sites}" | grep -a -E "FAIL|DONE|gave up|Crash report|did not|stopped|Mods failed|Mod ID|Blueprint|/ERROR"
    exit "${PIPESTATUS[0]}"
fi
bash tools/gametest.sh | grep -a -E "FAILED|tests reported|did not"
GAMETESTS=${PIPESTATUS[0]}
if [ "${1:-}" = "gametests" ]; then
    exit "$GAMETESTS"
fi
bash tools/smoketest.sh | grep -a -E "FAIL|DONE|gave up|monsters loaded|Crash report|did not|stopped|Mods failed|Mod ID|Blueprint"
SMOKE=${PIPESTATUS[0]}
echo "snapshot $TREE: $GAMETESTS GameTest failures, smoke test exit $SMOKE"
exit $((GAMETESTS + SMOKE))
