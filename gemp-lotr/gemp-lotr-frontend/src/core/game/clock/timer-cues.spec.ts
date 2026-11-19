import { bankBand, bankCue, decisionBand, decisionCue } from './timer-cues';

const RULES = [
  { belowSeconds: 600, everySeconds: 30 },
  { belowSeconds: 300, everySeconds: 10 },
  { belowSeconds: 60, everySeconds: 1 },
];

describe('timer cues', () => {
  it('pulses on a whole minute, and sounds where the most specific rule says', () => {
    expect(bankCue(601, 600, RULES)).toBe('pulse');
    // Jumped past a minute mark (the server's value came)
    expect(bankCue(305, 298, RULES)).toBe('pulse');
    expect(bankCue(571, 570, RULES)).toBe('sound');
    expect(bankCue(572, 571, RULES)).toBeNull();
    expect(bankCue(291, 290, RULES)).toBe('sound');
    expect(bankCue(59, 58, RULES)).toBe('sound');
    expect(bankCue(700, 699, RULES)).toBeNull();
    // Not going down, or at zero
    expect(bankCue(10, 10, RULES)).toBeNull();
    expect(bankCue(1, 0, RULES)).toBeNull();
    // A rule of every 0 seconds is off
    expect(bankCue(50, 49, [{ belowSeconds: 60, everySeconds: 0 }])).toBeNull();
  });

  it("calls at a decision's 60, 30 and 10 seconds left, then every second", () => {
    expect(decisionCue(61, 60)).toBe(true);
    expect(decisionCue(31, 30)).toBe(true);
    expect(decisionCue(45, 44)).toBe(false);
    expect(decisionCue(5, 4)).toBe(true);
    expect(decisionCue(1, 0)).toBe(false);
    expect(decisionCue(4, 4)).toBe(false);
  });

  it("colours the bank and the decision's clock as they run low", () => {
    expect(bankBand(700)).toBeNull();
    expect(bankBand(599)).toBe('warn');
    expect(bankBand(299)).toBe('danger');
    expect(decisionBand(null, 0)).toBeNull();
    expect(decisionBand(200, 360)).toBeNull();
    expect(decisionBand(80, 360)).toBe('warn');
    expect(decisionBand(50, 100)).toBe('warn');
    expect(decisionBand(29, 360)).toBe('danger');
  });
});
