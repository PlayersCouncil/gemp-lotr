/** Test-only: a clock and timers that move only when told to, so a page's timers run at once and in order. */

interface Task {
  id: number;
  at: number;
  order: number;
  run: () => void;
  every: number | null;
}

export class VirtualClock {
  private time = Date.UTC(2026, 0, 1);
  private nextId = 1;
  private order = 0;
  private readonly tasks = new Map<number, Task>();

  now(): number {
    return this.time;
  }

  after(delayMs: number, run: () => void): number {
    return this.add(delayMs, run, null);
  }

  every(periodMs: number, run: () => void): number {
    return this.add(periodMs, run, Math.max(1, periodMs));
  }

  clear(id: number | undefined): void {
    if (id !== undefined) {
      this.tasks.delete(id);
    }
  }

  /** Runs what is due within the next `ms`, in the order it falls due (and, for the same moment, was scheduled). */
  advance(ms: number): void {
    const until = this.time + ms;
    for (;;) {
      const next = [...this.tasks.values()]
        .filter((task) => task.at <= until)
        .sort((a, b) => a.at - b.at || a.order - b.order)[0];
      if (!next) {
        break;
      }
      this.time = Math.max(this.time, next.at);
      if (next.every === null) {
        this.tasks.delete(next.id);
      } else {
        next.at += next.every;
        next.order = this.order++;
      }
      next.run();
    }
    this.time = until;
  }

  private add(delayMs: number, run: () => void, every: number | null): number {
    const id = this.nextId++;
    const delay = Number.isFinite(delayMs) && delayMs > 0 ? delayMs : 0;
    this.tasks.set(id, { id, at: this.time + delay, order: this.order++, run, every });
    return id;
  }
}
