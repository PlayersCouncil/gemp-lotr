#!/usr/bin/env python3
"""wf.py status            where each tracked batch workflow has got to
   wf.py show <folder>     everything its agents reported
   wf.py track <folder> <run id>"""
import json, sys
from pathlib import Path
BASE = Path("/root/.claude/projects/-home-claude/e27589f3-ce17-51da-bfdb-fb8ab2591f88/subagents/workflows")
TRACK = Path("/home/claude/work/workflows.json")
tracked = json.loads(TRACK.read_text()) if TRACK.exists() else {}

def load(run):
    stages, started = {}, {}
    p = BASE / run / "journal.jsonl"
    if not p.exists(): return stages, started
    for line in p.read_text().split("\n"):
        if not line.strip(): continue
        e = json.loads(line)
        if e["type"] == "started": started[e["agentId"]] = e["label"].split()[0]
        elif e["type"] == "result": stages[started.get(e["agentId"], "?")] = e.get("result")
    return stages, started

def state(run):
    stages, started = load(run)
    running = [s for s in started.values() if s not in stages]
    if "review" in stages and stages["review"]:
        fix = [c for c in stages["review"].get("cards", []) if c.get("verdict") == "FIX" and c.get("fixes")]
        if not fix: return "DONE (no fixes)", stages
        if "re-check" in stages: return "DONE (fixed)", stages
    for s in ("author", "review", "fix", "re-check"):
        if s in stages and stages[s] is None: return "FAILED at " + s, stages
    return "running: " + (", ".join(running) or "?"), stages

cmd = sys.argv[1] if len(sys.argv) > 1 else "status"
if cmd == "track":
    tracked[sys.argv[2]] = {"run": sys.argv[3], "landed": False}
    TRACK.write_text(json.dumps(tracked, indent=1))
elif cmd == "landed":
    tracked[sys.argv[2]]["landed"] = True
    TRACK.write_text(json.dumps(tracked, indent=1))
elif cmd == "status":
    for folder, t in tracked.items():
        if t.get("landed"): continue
        print(folder, t["run"], state(t["run"])[0])
elif cmd == "show":
    st, stages = state(tracked[sys.argv[2]]["run"])
    print("##", sys.argv[2], st)
    for s in ("author", "review", "fix", "re-check"):
        if s in stages:
            print("\n=====", s.upper(), "=====")
            print(json.dumps(stages[s], indent=1, ensure_ascii=False))
