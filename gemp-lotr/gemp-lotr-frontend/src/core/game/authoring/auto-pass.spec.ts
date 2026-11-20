import { autoPassValue, autoPassView, passedIn, withByRole, withPhase } from './auto-pass';

describe('the phases the player is passed through (options.autoPass)', () => {
  it('reads every way the file writes it, in a turn’s order', () => {
    expect(autoPassView(undefined)).toEqual({ byRole: false, all: [], freeps: [], shadow: [] });
    expect(autoPassView({ autoPass: 'regroup, Fellowship' })).toEqual({
      byRole: false,
      all: ['fellowship', 'regroup'],
      freeps: ['fellowship', 'regroup'],
      shadow: ['fellowship', 'regroup'],
    });
    expect(autoPassView({ autoPass: true }).all).toEqual([
      'fellowship',
      'maneuver',
      'archery',
      'assignment',
      'regroup',
    ]);
    expect(autoPassView({ autoPass: 'true' }).all).toHaveLength(5);
    expect(autoPassView({ autoPass: false }).all).toEqual([]);
    expect(autoPassView({ autoPass: 'false' }).all).toEqual([]);
    // (said for each role: a role left out, or false, has none; true is an ordinary table's)
    expect(autoPassView({ autoPass: { freeps: 'regroup, fellowship', shadow: false } })).toEqual({
      byRole: true,
      all: [],
      freeps: ['fellowship', 'regroup'],
      shadow: [],
    });
    expect(autoPassView({ autoPass: { shadow: true } }).freeps).toEqual([]);
    expect(autoPassView({ autoPass: { shadow: true } }).shadow).toHaveLength(5);
  });

  it('writes the one list as a line (none: taken out), and the roles always both (none: false)', () => {
    expect(autoPassValue({ byRole: false, all: ['regroup', 'fellowship'], freeps: [], shadow: [] })).toBe(
      'fellowship, regroup',
    );
    expect(autoPassValue({ byRole: false, all: [], freeps: [], shadow: [] })).toBeNull();
    expect(autoPassValue({ byRole: true, all: [], freeps: ['maneuver'], shadow: [] })).toEqual({
      freeps: 'maneuver',
      shadow: false,
    });
  });

  it('goes round both ways: a file’s object form is written back as it was, and so is its list', () => {
    const roles = { freeps: 'fellowship, maneuver, archery, assignment, regroup', shadow: 'maneuver, regroup' };
    expect(autoPassValue(autoPassView({ autoPass: roles }))).toEqual(roles);
    expect(autoPassValue(autoPassView({ autoPass: { freeps: 'shadow', shadow: false } }))).toEqual({
      freeps: 'shadow',
      shadow: false,
    });
    expect(autoPassValue(autoPassView({ autoPass: 'archery, maneuver' }))).toBe('maneuver, archery');
  });

  it('ticks a phase in the one list, or in one role’s, leaving the other role as it is', () => {
    const one = withPhase(autoPassView({ autoPass: 'regroup' }), null, 'fellowship', true);
    expect(autoPassValue(one)).toBe('fellowship, regroup');
    expect(autoPassValue(withPhase(one, null, 'regroup', false))).toBe('fellowship');
    const roles = autoPassView({ autoPass: { freeps: 'regroup', shadow: 'maneuver' } });
    expect(autoPassValue(withPhase(roles, 'shadow', 'archery', true))).toEqual({
      freeps: 'regroup',
      shadow: 'maneuver, archery',
    });
    expect(autoPassValue(withPhase(roles, 'freeps', 'regroup', false))).toEqual({ freeps: false, shadow: 'maneuver' });
  });

  it('“for each role” starts both roles from the one list; unticked, one list of both, saying what changes', () => {
    const on = withByRole(autoPassView({ autoPass: 'fellowship, regroup' }), true);
    expect(on.lost).toBeNull();
    expect(autoPassValue(on.view)).toEqual({ freeps: 'fellowship, regroup', shadow: 'fellowship, regroup' });
    expect(withByRole(on.view, false)).toEqual({
      view: {
        byRole: false,
        all: ['fellowship', 'regroup'],
        freeps: ['fellowship', 'regroup'],
        shadow: ['fellowship', 'regroup'],
      },
      lost: null,
    });
    const apart = withByRole(autoPassView({ autoPass: { freeps: 'fellowship', shadow: false } }), false);
    expect(autoPassValue(apart.view)).toBe('fellowship');
    expect(apart.lost).toBe('One list for both roles: fellowship. (Free Peoples had fellowship; Shadow had none.)');
    expect(withByRole(autoPassView({ autoPass: { freeps: false, shadow: false } }), false).lost).toBeNull();
  });

  it('says whether the player is passed through a phase in a role', () => {
    const options = { autoPass: { freeps: 'regroup', shadow: 'maneuver' } };
    expect(passedIn(options, 'freeps', 'Regroup')).toBe(true);
    expect(passedIn(options, 'shadow', 'regroup')).toBe(false);
    expect(passedIn({ autoPass: 'regroup' }, 'shadow', 'regroup')).toBe(true);
  });
});
