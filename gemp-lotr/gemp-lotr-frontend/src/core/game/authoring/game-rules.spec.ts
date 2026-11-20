import { formatChoices, formatOf, rulesOneSided, rulesWithMarkers, SCENARIO_FORMATS } from './game-rules';

describe("the scenario's game rules", () => {
  it("offers the scenarios' formats, and keeps another the file names", () => {
    expect(SCENARIO_FORMATS.map(([code]) => code)).toEqual(['scenario', 'scenario_shadows']);
    expect(formatChoices('scenario')).toEqual(SCENARIO_FORMATS);
    expect(formatChoices(undefined)).toEqual(SCENARIO_FORMATS);
    expect(formatChoices('')).toEqual(SCENARIO_FORMATS);
    expect(formatChoices('expanded').at(-1)).toEqual(['expanded', 'expanded']);
    expect(formatOf({ id: 'x' })).toBe('scenario');
    expect(formatOf({ id: 'x', format: 'expanded' })).toBe('expanded');
  });

  it('writes markers keeping together, and takes it out (the rules with it when nothing is left)', () => {
    expect(rulesWithMarkers({ id: 'x', rules: { oneSided: true } }, true)).toEqual({
      oneSided: true,
      markers: 'together',
    });
    expect(rulesWithMarkers({ id: 'x', rules: { oneSided: true, markers: 'together' } }, false)).toEqual({
      oneSided: true,
    });
    expect(rulesWithMarkers({ id: 'x' }, false)).toBeNull();
  });

  it('takes markers keeping together out with one-sided, a game of two sides having none', () => {
    const rules = { oneSided: true, markers: 'together' as const, freeps: [{ type: 'x' }] };
    expect(rulesOneSided({ id: 'x', rules }, false)).toEqual({ freeps: [{ type: 'x' }] });
    expect(rulesOneSided({ id: 'x', rules: { oneSided: true, markers: 'together' } }, false)).toBeNull();
    expect(rulesOneSided({ id: 'x', rules: { freeps: [{ type: 'x' }] } }, true)).toEqual({
      freeps: [{ type: 'x' }],
      oneSided: true,
    });
    expect(rulesOneSided({ id: 'x', rules: { oneSided: true } }, false)).toBeNull();
  });
});
