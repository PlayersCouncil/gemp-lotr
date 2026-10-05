#!/usr/bin/env python3
"""Per-role and per-activity token and time breakdown of the card-batch workflow runs."""
import json, re, sys, datetime, collections
from pathlib import Path
BASE = Path("/root/.claude/projects/-home-claude/e27589f3-ce17-51da-bfdb-fb8ab2591f88/subagents/workflows")
tracked = json.load(open("/home/claude/work/workflows.json"))

def ts(s): return datetime.datetime.fromisoformat(s.replace("Z", "+00:00"))

def classify(name, inp):
    if name != "Bash":
        if name in ("Read", "Grep", "Glob"): return "read"
        if name in ("Write", "Edit"): return "write"
        if name == "StructuredOutput": return "report"
        return "other"
    c = inp.get("command", "")
    if "mutate" in c or "mut_" in c or "-mutate" in c or "manual.py" in c: return "mutation"
    if re.search(r"\bsleep\b", c) and len(c) < 400 and not re.search(r"cardtest|lint|packet|javac", c): return "wait"
    if "cardtest.py" in c: return "run tests"
    if "lint.py" in c: return "lint"
    if "packet.py" in c or "rules.py" in c or "gempcards" in c or "reconcile" in c: return "packet/rules"
    if "Probe" in c or "probe" in c or "javac" in c: return "probe"
    if re.search(r"\b(grep|sed|cat|head|tail|ls|find|rg|wc)\b", c): return "read"
    return "other"

rows = []
for folder, t in tracked.items():
    run = t["run"]; n = len(t.get("cards", [])) or 1
    for meta in sorted((BASE / run).glob("agent-*.meta.json")):
        phase = json.load(open(meta)).get("workflowPhase", "?")
        f = Path(str(meta).replace(".meta.json", ".jsonl"))
        calls = {}      # message id -> [out, cw, cr, inp, category]
        pending = {}    # tool_use id -> (category, time)
        wall = collections.Counter(); first = last = None; last_cat = "start"; mut_active = False
        turn_cat = {}
        for line in f.read_text(errors="replace").split("\n"):
            try: e = json.loads(line)
            except Exception: continue
            tm = e.get("timestamp")
            if tm:
                first = first or tm; last = tm
            m = e.get("message") or {}
            content = m.get("content") if isinstance(m.get("content"), list) else []
            u = m.get("usage")
            mid = m.get("id")
            if u and mid:
                o = calls.get(mid, [0, 0, 0, 0])
                calls[mid] = [max(o[0], u.get("output_tokens", 0)), max(o[1], u.get("cache_creation_input_tokens", 0)),
                              max(o[2], u.get("cache_read_input_tokens", 0)), max(o[3], u.get("input_tokens", 0))]
            for b in content:
                if not isinstance(b, dict): continue
                if b.get("type") == "tool_use":
                    cat = classify(b.get("name"), b.get("input") or {})
                    c = (b.get("input") or {}).get("command", "")
                    # a wait or a look at a file while a background mutation run is going is mutation's cost
                    if cat == "mutation" and ("nohup" in c or "&" in c.split("\n")[-1][-3:] or "run_in_background" in json.dumps(b.get("input"))): mut_active = True
                    if cat == "wait" and mut_active: cat = "mutation"
                    pending[b["id"]] = (cat, tm)
                    if mid: turn_cat.setdefault(mid, cat)
                elif b.get("type") == "tool_result":
                    p = pending.pop(b.get("tool_use_id"), None)
                    if p and p[1] and tm:
                        wall[p[0]] += (ts(tm) - ts(p[1])).total_seconds()
                elif b.get("type") == "text" and mid:
                    turn_cat.setdefault(mid, "think/write")
        tok = collections.defaultdict(lambda: [0, 0, 0, 0, 0])
        for mid, v in calls.items():
            cat = turn_cat.get(mid, "think/write")
            for i in range(4): tok[cat][i] += v[i]
            tok[cat][4] += 1
        dur = (ts(last) - ts(first)).total_seconds() if first and last else 0
        rows.append(dict(folder=folder, cards=n, phase=phase, dur=dur, tok={k: v for k, v in tok.items()}, wall=dict(wall)))
json.dump(rows, open("/home/claude/work/analysis/cost.json", "w"))

def w(v):  # input-token equivalents at the usual ratios: output 5x, cache write 1.25x, cache read 0.1x
    return v[0] * 5 + v[1] * 1.25 + v[2] * 0.1 + v[3]

which = sys.argv[1:] or None
sel = [r for r in rows if not which or r["folder"] in which]
ncards = sum({r["folder"]: r["cards"] for r in sel}.values())
print("runs: %d, cards: %d" % (len({r['folder'] for r in sel}), ncards))
print("\n== by role (per card) ==")
print("%-10s %6s %9s %9s %9s %9s %7s %6s" % ("role", "agents", "output", "cache wr", "cache rd", "weighted", "share", "min"))
tot = collections.defaultdict(lambda: [0, 0, 0, 0, 0]); durs = collections.Counter(); cnt = collections.Counter()
for r in sel:
    for v in r["tok"].values():
        for i in range(5): tot[r["phase"]][i] += v[i]
    durs[r["phase"]] += r["dur"]; cnt[r["phase"]] += 1
W = sum(w(v) for v in tot.values())
for ph in ("Author", "Review", "Fix", "Re-check"):
    v = tot[ph]
    print("%-10s %6d %8.0fk %8.2fM %8.2fM %8.2fM %6.0f%% %6.0f" % (ph, cnt[ph], v[0] / ncards / 1e3, v[1] / ncards / 1e6, v[2] / ncards / 1e6, w(v) / ncards / 1e6, 100 * w(v) / W, durs[ph] / 60 / max(1, cnt[ph])))
v = [sum(t[i] for t in tot.values()) for i in range(5)]
print("%-10s %6s %8.0fk %8.2fM %8.2fM %8.2fM" % ("all", "", v[0] / ncards / 1e3, v[1] / ncards / 1e6, v[2] / ncards / 1e6, W / ncards / 1e6))

print("\n== by activity: share of weighted tokens / share of tool wall-clock ==")
act = collections.defaultdict(lambda: [0, 0, 0, 0, 0]); wall = collections.Counter()
actph = collections.defaultdict(lambda: collections.defaultdict(float))
for r in sel:
    for k, v in r["tok"].items():
        for i in range(5): act[k][i] += v[i]
        actph[k][r["phase"]] += w(v)
    for k, s in r["wall"].items(): wall[k] += s
WW = sum(wall.values())
print("%-13s %7s %7s %7s %9s   %s" % ("activity", "tokens", "turns", "wall", "cache rd", "tokens by role: A / R / F / C"))
for k in sorted(act, key=lambda k: -w(act[k])):
    print("%-13s %6.0f%% %7d %6.0f%% %8.2fM   %s" % (k, 100 * w(act[k]) / W, act[k][4], 100 * wall.get(k, 0) / WW if WW else 0, act[k][2] / ncards / 1e6,
          " / ".join("%2.0f%%" % (100 * actph[k][p] / W) for p in ("Author", "Review", "Fix", "Re-check"))))
