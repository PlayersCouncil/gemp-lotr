import { readScenarioScore, scoreLines } from './scenario-score';

describe('a scored puzzle’s score', () => {
  it('is read from the end event', () => {
    expect(
      readScenarioScore({ value: 1160, label: "Sméagol's strength", better: 'higher', best: 1160, newBest: true }),
    ).toEqual({ value: 1160, label: "Sméagol's strength", better: 'higher', best: 1160, newBest: true });
    expect(readScenarioScore({ value: null, label: '', better: 'lower', best: 'x' })).toEqual({
      value: null,
      label: null,
      better: 'lower',
      best: null,
      newBest: false,
    });
    expect(readScenarioScore(null)).toBeNull();
    expect(readScenarioScore([1])).toBeNull();
  });

  it('is said by the end panel: its label, the score, the best, a new best, which way is better', () => {
    expect(scoreLines({ value: 8, label: 'Wounds', better: 'lower', best: 6, newBest: false })).toEqual({
      label: 'Wounds',
      value: '8',
      best: '6',
      newBest: false,
      title: 'Lower is better',
    });
    expect(scoreLines({ value: null, label: null, better: 'higher', best: null, newBest: true })).toEqual({
      label: 'Score',
      value: '?',
      best: null,
      newBest: true,
      title: 'Higher is better',
    });
  });
});
