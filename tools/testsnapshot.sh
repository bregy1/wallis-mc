#!/usr/bin/env bash
# Tests a snapshot of the project in .testrun/ (git-ignored, with its own build and game folder), so work can go on
# in the project while it runs, and the game can be played from run/ at the same time.
# Runs the GameTests and the client smoke test on the files as they are right now, and prints the results. The last
# line names the snapshot; this commits exactly what was tested:
#   bash tools/testsnapshot.sh commit <tree> "message"
# Exit code = number of failures (100 if something didn't finish).
set -u
ROOT="$(git rev-parse --show-toplevel)"
cd "$ROOT"
COPY=".testrun"

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

mkdir -p "$COPY/run"
rm -rf "$COPY/src" "$COPY/pack" "$COPY/tools" "$COPY/docs"
git archive "$TREE" | tar -x -C "$COPY"
# The game folder's settings the tests need; these are never committed.
for file in eula.txt server.properties options.txt; do
    [ -f "$COPY/run/$file" ] || cp "run/$file" "$COPY/run/$file"
done

cd "$COPY"
bash tools/gametest.sh | grep -a -E "FAILED|tests reported|did not"
GAMETESTS=${PIPESTATUS[0]}
bash tools/smoketest.sh | grep -a -E "FAIL|DONE|gave up|monsters loaded|Crash report|did not|stopped|Mods failed|Mod ID"
SMOKE=${PIPESTATUS[0]}
echo "snapshot $TREE: $GAMETESTS GameTest failures, smoke test exit $SMOKE"
exit $((GAMETESTS + SMOKE))
