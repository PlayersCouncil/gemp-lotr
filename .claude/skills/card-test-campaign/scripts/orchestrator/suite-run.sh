#!/bin/bash
# Full suite on the committed head, in a clone of its own (/home/claude/work/suite), so that runs in flight in
# the main clone are not disturbed: it never compiles a test file an agent is half-way through writing, and it
# writes nothing under the main clone's target directories.  Make the clone once with
#   git clone /home/claude/work/repo /home/claude/work/suite
# Takes about three minutes beside running pipelines.  Result: the tail of the log; look for "OK (n tests)".
cd /home/claude/work/suite && git fetch -q origin feature/card-test-overhaul && git checkout -q --detach FETCH_HEAD && git log --oneline -1
cd gemp-lotr && mvn -q -B -o -DskipTests test-compile -pl gemp-lotr-common,gemp-lotr-cards,gemp-lotr-logic,gemp-lotr-server 2>&1 | grep -v "^Picked up\|WARNING" | tail -20
mkdir -p gemp-lotr-server/target && cp /home/claude/work/repo/gemp-lotr/gemp-lotr-server/target/cardtest.classpath gemp-lotr-server/target/cardtest.classpath
cd /home/claude/work/suite && PYTHONDONTWRITEBYTECODE=1 python3 .claude/skills/card-test-campaign/scripts/cardtest.py suite all 2>&1 | tail -60
echo SUITE-DONE
