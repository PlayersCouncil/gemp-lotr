import { signal } from '@angular/core';
import { DecisionGate, DecisionGates, DecisionGesture } from './decision-gate';

describe('DecisionGates', () => {
  const card: DecisionGesture = { kind: 'card', cardId: '5' };

  it('lets every gesture through with no gate, and only what every gate allows with some', () => {
    const gates = new DecisionGates();
    expect(gates.allows(card)).toBe(true);
    const asked: string[] = [];
    const gate = (name: string, answer: boolean): DecisionGate => ({
      allows: () => {
        asked.push(name);
        return answer;
      },
    });
    const yes = gate('yes', true);
    const no = gate('no', false);
    const last = gate('last', true);
    gates.add(yes);
    gates.add(no);
    gates.add(last);
    expect(gates.allows(card)).toBe(false);
    // The first that refuses is the one that tells the player: the ones after it aren't asked
    expect(asked).toEqual(['yes', 'no']);
  });

  it('tells every gate that has an ear for it what went ahead; a gate taken away has no more say', () => {
    const gates = new DecisionGates();
    const heard: DecisionGesture[] = [];
    const listening: DecisionGate = { allows: () => false, after: (gesture) => heard.push(gesture) };
    const remove = gates.add(listening);
    gates.add({ allows: () => true });
    gates.after(card);
    expect(heard).toEqual([card]);
    expect(gates.allows(card)).toBe(false);
    remove();
    expect(gates.allows(card)).toBe(true);
    gates.after({ kind: 'escape' });
    expect(heard).toEqual([card]);
  });

  it('hides the waiting decision while any say in it hides it, until that say is taken away', () => {
    const gates = new DecisionGates();
    const editing = signal(false);
    expect(gates.hiding()).toBe(false);
    const remove = gates.hide(editing);
    gates.hide(signal(false));
    expect(gates.hiding()).toBe(false);
    editing.set(true);
    expect(gates.hiding()).toBe(true);
    remove();
    expect(gates.hiding()).toBe(false);
  });
});
