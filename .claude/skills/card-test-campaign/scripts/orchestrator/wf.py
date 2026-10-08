#!/usr/bin/env python3
"""wf.py status            where each tracked batch workflow has got to
   wf.py show <folder>     everything its agents reported
   wf.py track <folder> <run id>"""
import json, sys
from pathlib import Path
def _workflows_dir():
    """The folder this session's workflow runs are journalled in: the newest one under ~/.claude/projects, or
    $GEMP_WORKFLOWS.  (It is named after the session, so it differs in every session.)"""
    import glob, os
    if os.environ.get("GEMP_WORKFLOWS"):
        return Path(os.environ["GEMP_WORKFLOWS"])
    found = sorted(glob.glob("/root/.claude/projects/*/*/subagents/workflows"), key=os.path.getmtime)
    return Path(found[-1]) if found else Path("/root/.claude/projects/none")
BASE = _workflows_dir()
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

def state(run, lean=False):
    stages, started = load(run)
    running = [s for s in started.values() if s not in stages]
    if lean:   # the cut-down pipeline: author, then a reviewer who fixes; or a fixer alone
        for s in ("author", "review", "fix", "land"):
            if s in stages and stages[s] is None: return "FAILED at " + s, stages
        if stages.get("land"):
            v = [c.get("status") for c in stages["land"].get("cards", [])]
            return "LANDED (%s) %s" % (", ".join("%d %s" % (v.count(k), k) for k in ("ready", "parked", "held") if v.count(k)), stages["land"].get("commit", "")), stages
        if "land" in started.values(): return "running: land", stages
        if stages.get("review"):
            v = [c.get("verdict") for c in stages["review"].get("cards", [])]
            return "DONE (%s)" % ", ".join("%d %s" % (v.count(k), k) for k in ("PASS", "FIXED", "PARK") if v.count(k)), stages
        if stages.get("fix") and "author" not in started.values(): return "DONE (fix only)", stages
        return "running: " + (", ".join(running) or "?"), stages
    if "review" in stages and stages["review"]:
        fix = [c for c in stages["review"].get("cards", []) if c.get("verdict") == "FIX" and c.get("fixes")]
        if not fix: return "DONE (no fixes)", stages
        if "re-check" in stages: return "DONE (fixed)", stages
    if "review" not in started.values() and "re-check" in stages and stages["re-check"]:
        return "DONE (fix only)", stages
    for s in ("author", "review", "fix", "re-check"):
        if s in stages and stages[s] is None: return "FAILED at " + s, stages
    return "running: " + (", ".join(running) or "?"), stages

cmd = sys.argv[1] if len(sys.argv) > 1 else "status"
if cmd == "track":
    tracked[sys.argv[2]] = {"run": sys.argv[3], "landed": False, "lean": True}   # everything tracked from 2026-10-05 18:40Z is the cut-down pipeline
    if len(sys.argv) > 4: tracked[sys.argv[2]]["cards"] = sys.argv[4].split(",")
    TRACK.write_text(json.dumps(tracked, indent=1))
elif cmd == "landed":
    tracked[sys.argv[2]]["landed"] = True
    TRACK.write_text(json.dumps(tracked, indent=1))
elif cmd == "status":
    for folder, t in tracked.items():
        if t.get("landed"): continue
        print(folder, t["run"], state(t["run"], t.get("lean"))[0])
elif cmd == "usage":
    folder = sys.argv[2]; run = tracked[folder]["run"]
    out = cw = cr = 0; first = last = None
    for f in sorted((BASE / run).glob("agent-*.jsonl")):
        per = {}   # one API call is logged once per content block, each time with its usage so far: keep the largest
        for line in f.read_text(errors="replace").split("\n"):
            try: e = json.loads(line)
            except Exception: continue
            ts = e.get("timestamp")
            if ts: first = min(first, ts) if first else ts; last = max(last, ts) if last else ts
            m = e.get("message") or {}
            u = m.get("usage")
            if u:
                k = m.get("id") or e.get("requestId") or len(per)
                o = per.get(k, (0, 0, 0))
                per[k] = (max(o[0], u.get("output_tokens", 0)), max(o[1], u.get("cache_creation_input_tokens", 0)), max(o[2], u.get("cache_read_input_tokens", 0)))
        for o in per.values():
            out += o[0]; cw += o[1]; cr += o[2]
    import datetime
    mins = 0
    if first and last:
        a = datetime.datetime.fromisoformat(first.replace("Z", "+00:00")); b = datetime.datetime.fromisoformat(last.replace("Z", "+00:00"))
        mins = round((b - a).total_seconds() / 60)
    stages, _ = load(run)
    print("%s\t%s\t%s\t%s\t%d\tOutput tokens. Also %.1fM written to cache and %.1fM read from cache. %d minutes." % (
        first[:10] if first else "", folder, "+".join(k for k in ("author", "review", "fix", "re-check", "land") if k in stages),
        ",".join(tracked[folder].get("cards", [folder.split("-", 1)[1]])), out, cw / 1e6, cr / 1e6, mins))
elif cmd == "brief":
    st, stages = state(tracked[sys.argv[2]]["run"], tracked[sys.argv[2]].get("lean"))
    W = int(sys.argv[3]) if len(sys.argv) > 3 else 900
    print("##", sys.argv[2], st)
    a = stages.get("author") or {}
    for c in a.get("cards", []):
        print("AUTHOR", c["id"], c.get("state"), c.get("tests_before"), "->", c.get("tests_after"), c.get("parked_on", ""))
        for q in c.get("questions", []): print("  Q:", json.dumps(q, ensure_ascii=False)[:W * 2])
        for q in c.get("findings", []): print("  F:", json.dumps(q, ensure_ascii=False)[:W * 2])
        for q in c.get("rig_requests", []): print("  G:", q[:W])
    for d in a.get("documentation_issues", []): print("  doc:", d[:W])
    r = stages.get("review") or {}
    for c in r.get("cards", []):
        print("REVIEW", c["id"], c.get("verdict"), len(c.get("fixes") or c.get("changes") or []), "fixes", c.get("parked_on", ""))
        for n, ch in enumerate(c.get("changes") or []): print("   change", n + 1, ch[:W])
        print("  F&Q:", (c.get("findings_and_questions") or "")[:W * 2])
        if c.get("mutation_survivors") or c.get("survivor_claims"): print("  survivors:", (c.get("mutation_survivors") or c.get("survivor_claims"))[:W])
        print("  lost:", (c.get("lost_tests") or "")[:W // 2])
        for k in ("new_findings", "new_questions", "new_rig_requests"):
            for q in c.get(k) or []: print("  ", k, ":", q[:W * 2])
        if c.get("lint") or c.get("run"): print("  gate:", (c.get("lint") or "")[-80:], "|", (c.get("run") or "")[-80:])
    for d in r.get("standard_issues", []): print("  std:", d[:W])
    x = stages.get("fix") or {}
    for c in x.get("cards", []):
        print("FIX", c["id"], c.get("state"), c.get("parked_on", ""))
        for i in c.get("items", []): print("   ", i["number"], i["outcome"], (i.get("note") or "")[:W // 3])
        for k in ("new_findings", "new_questions", "new_rig_requests"):
            for q in c.get(k, []): print("  ", k, ":", q[:W * 2])
    k = stages.get("re-check") or {}
    for c in k.get("cards", []):
        print("RECHECK", c["id"], c.get("verdict"), c.get("parked_on", ""))
        for n, fx in enumerate(c.get("fixes") or []): print("   fix", n + 1, fx[:W * 2])
        print("  F&Q:", (c.get("findings_and_questions") or "")[:W * 2])
        print("  survivors:", (c.get("mutation_survivors") or "")[:W])
    for d in k.get("standard_issues", []): print("  std:", d[:W])
elif cmd == "lean":
    # How many cards have landed through the cut-down pipeline (author, then a reviewer who fixes), and which runs.
    runs = [f for f, t in tracked.items() if t.get("lean") and t.get("landed") and not t.get("hybrid") and not t.get("cut_off") and "author" in load(t["run"])[1].values()]
    n = sum(len(tracked[f].get("cards", [])) for f in runs)
    print("%d cards landed through the cut-down pipeline (%s). Ketura's review of the two further cuts is due at 20: cost.py %s" % (n, ", ".join(runs) or "none yet", " ".join(runs)))
elif cmd == "show":
    st, stages = state(tracked[sys.argv[2]]["run"], tracked[sys.argv[2]].get("lean"))
    print("##", sys.argv[2], st)
    for s in ("author", "review", "fix", "re-check", "land"):
        if s in stages:
            print("\n=====", s.upper(), "=====")
            print(json.dumps(stages[s], indent=1, ensure_ascii=False))
