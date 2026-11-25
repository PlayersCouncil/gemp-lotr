import { draftState } from '../../../testing/authoring';
import { parseJson } from '../../api/json';
import { DraftState, DraftStep, StepTexts } from './authoring-state';
import { carriedOf, StepForm, StepTransport } from './send-queue';
import { withValue, WorkStep } from './step-model';

/** A merge the fake server has been sent, and how to answer it. */
interface Sent {
  uid: string;
  set: Readonly<Record<string, unknown>>;
  unset: readonly string[];
  answer(state: DraftState | null, problem?: string): void;
}

/** The server, as the form sees it: each merge waits to be answered by the test. */
class FakeServer implements StepTransport {
  readonly sent: Sent[] = [];
  said: string | null = null;
  focus: string | null = null;
  isCurrent = true;

  merge(uid: string, set: Readonly<Record<string, unknown>>, unset: readonly string[]): Promise<DraftState | null> {
    return new Promise((resolve) => {
      this.sent.push({
        uid,
        set,
        unset,
        answer: (state, problem) => {
          this.said = state === null ? (problem ?? 'No.') : null;
          resolve(state);
        },
      });
    });
  }
  problem(): string | null {
    return this.said;
  }
  sayProblem(text: string | null): void {
    this.said = text;
  }
  focused(field: string): boolean {
    return this.focus === field;
  }
  current(): boolean {
    return this.isCurrent;
  }
}

const tick = () => new Promise<void>((resolve) => setTimeout(resolve, 0));

/** The state with step 1 as given (and its texts). */
const withStep = (step: DraftStep, texts: StepTexts = {}) => {
  const state = draftState();
  const steps = [step, ...(state.draft.steps ?? []).slice(1)];
  return draftState({ draft: { ...state.draft, steps }, texts: { s1: texts } });
};

describe('the Step form and its sends', () => {
  let server: FakeServer;
  let form: StepForm;
  const step: DraftStep = { when: 'freeps, action', allow: 'pass', messages: [{ text: 'Hello' }] };

  beforeEach(() => {
    server = new FakeServer();
    form = new StepForm('s1', step, {}, { index: 0, sub: -1 }, server);
  });

  const type = (path: string, value: unknown) => form.work.update((work: WorkStep) => withValue(work, path, value));

  it('sends nothing when nothing changed, and only the parts that did', async () => {
    expect(await form.send()).toBe(true);
    expect(server.sent).toEqual([]);
    type('messages.0.text', 'Hi');
    const going = form.send();
    expect(form.busy).toBe(true);
    expect(server.sent[0]).toMatchObject({ uid: 's1', set: { messages: [{ text: 'Hi' }] }, unset: [] });
    server.sent[0]!.answer(withStep({ ...step, messages: [{ text: 'Hi' }] }));
    expect(await going).toBe(true);
    expect(form.dirty().any).toBe(false);
    expect(form.busy).toBe(false);
  });

  it('sends a part taken out as unset', async () => {
    type('allow.answers', '');
    void form.send();
    expect(server.sent[0]).toMatchObject({ set: {}, unset: ['allow'] });
  });

  it('keeps what is typed while a send is on its way, and sends it once that one is back', async () => {
    type('messages.0.text', 'Hi');
    const first = form.send();
    type('messages.0.text', 'Hi there');
    const second = form.send();
    expect(server.sent.length).toBe(1);
    // The server writes back what it was sent; the form keeps what was typed since
    server.sent[0]!.answer(withStep({ ...step, messages: [{ text: 'Hi' }] }));
    expect(await first).toBe(true);
    expect(form.work().messages[0]?.text).toBe('Hi there');
    await tick();
    expect(server.sent.length).toBe(2);
    expect(server.sent[1]!.set).toEqual({ messages: [{ text: 'Hi there' }] });
    server.sent[1]!.answer(withStep({ ...step, messages: [{ text: 'Hi there' }] }));
    expect(await second).toBe(true);
    expect(form.dirty().any).toBe(false);
  });

  it('carries what was typed since the send on its way to the next page, and nothing else', async () => {
    expect(form.carry()).toBeNull();
    type('messages.0.text', 'Hi');
    void form.send();
    expect(form.carry()).toBeNull();
    type('answer', 'pass');
    const carried = form.carry();
    expect(carried?.parts).toEqual(['answer']);
    // The next page's form, from the server's step (which has what was sent), takes back what was typed since
    const next = new StepForm('s1', { ...step, messages: [{ text: 'Hi' }] }, {}, { index: 0, sub: -1 }, server);
    expect(next.restore(carriedOf(parseJson(JSON.stringify(carried)))!)).toBe(true);
    expect(next.work().answer).toBe('pass');
    expect(next.dirty().set).toEqual({ answer: 'pass' });
    expect(next.restore({ ...carried!, uid: 's2' })).toBe(false);
    expect(carriedOf(null)).toBeNull();
    expect(carriedOf({ uid: 's1', parts: ['a', 3], work: { messages: [] } })).toBeNull();
    expect(carriedOf({ uid: 's1', parts: ['a', 3], work: { messages: [], allow: {}, end: {} } })?.parts).toEqual(['a']);
  });

  it('does not send a refusal again as it is, and says so to whoever waits', async () => {
    type('answer', 'nonsense(');
    const going = form.send();
    server.sent[0]!.answer(null, 'Not a term.');
    expect(await going).toBe(false);
    expect(server.said).toBe('Not a term.');
    expect(await form.send()).toBe(false);
    expect(server.sent.length).toBe(1);
    expect(form.dirty().set).toEqual({ answer: 'nonsense(' });
    // Changed again, it is sent again
    type('answer', 'pass');
    void form.send();
    expect(server.sent.length).toBe(2);
  });

  it('tries each part of a refused send by itself, so that the wrong one does not hold the others back', async () => {
    type('answer', 'nonsense(');
    type('id', 'greeting');
    type('messages.0.text', 'Hi');
    const going = form.send();
    server.sent[0]!.answer(null, 'Not a term.');
    await tick();
    expect(Object.keys(server.sent[1]!.set)).toEqual(['id']);
    server.sent[1]!.answer(withStep({ ...step, id: 'greeting' }));
    await tick();
    expect(Object.keys(server.sent[2]!.set)).toEqual(['messages']);
    server.sent[2]!.answer(withStep({ ...step, id: 'greeting', messages: [{ text: 'Hi' }] }));
    await tick();
    expect(Object.keys(server.sent[3]!.set)).toEqual(['answer']);
    server.sent[3]!.answer(null, 'Not a term, still.');
    expect(await going).toBe(false);
    expect(form.dirty().set).toEqual({ answer: 'nonsense(' });
    expect(server.said).toBe('Not a term, still.');
    // (what was refused is not sent again as it is)
    expect(await form.send()).toBe(false);
    expect(server.sent.length).toBe(4);
  });

  it('says the last refusal when a part that went through after it cleared what was said', async () => {
    type('answer', 'nonsense(');
    type('id', 'x');
    const going = form.send();
    server.sent[0]!.answer(null, 'Both wrong.');
    await tick();
    server.sent[1]!.answer(null, 'Answer wrong.');
    await tick();
    server.sent[2]!.answer(withStep({ ...step, id: 'x' }));
    expect(await going).toBe(false);
    expect(server.said).toBe('Answer wrong.');
  });

  it("takes back the server's way of writing a part written out in full, unless the field has the focus or changed", async () => {
    type('until', '{seat:freeps}');
    const going = form.send();
    server.sent[0]!.answer(withStep({ ...step, until: { seat: 'freeps' } }, { until: 'seat: freeps' }));
    await going;
    expect(form.work().until).toBe('seat: freeps');
    expect(form.dirty().any).toBe(false);

    type('until', '{seat:shadow}');
    server.focus = 'until';
    const focused = form.send();
    server.sent[1]!.answer(withStep({ ...step, until: { seat: 'shadow' } }, { until: 'seat: shadow' }));
    await focused;
    expect(form.work().until).toBe('{seat:shadow}');
    expect(form.dirty().any).toBe(false);

    server.focus = null;
    type('until', '{seat:freeps}');
    const changed = form.send();
    type('until', '{seat:freeps, phase:regroup}');
    server.sent[2]!.answer(withStep({ ...step, until: { seat: 'freeps' } }, { until: 'seat: freeps' }));
    await changed;
    expect(form.work().until).toBe('{seat:freeps, phase:regroup}');
  });

  it("takes back the server's way of writing a When's requirements, and leaves the When's terms as they are", async () => {
    type('whenRequire', '{type:canSpot}');
    const going = form.send();
    expect(server.sent[0]!.set['when']).toBe('{\n\tterms: "freeps, action"\n\trequire: {type:canSpot}\n}');
    const back = { ...step, when: { seat: 'freeps', decision: 'action', require: { type: 'canSpot' } } };
    server.sent[0]!.answer(withStep(back, { whenRequire: 'type: canSpot' }));
    await going;
    expect(form.work().when).toBe('freeps, action');
    expect(form.work().whenRequire).toBe('type: canSpot');
    expect(form.dirty().any).toBe(false);
  });

  it("takes back the server's way of writing Pass when", async () => {
    type('allow.passWhen', '{type:canSpot}');
    const going = form.send();
    const back = { ...step, allow: { answers: 'pass', passWhen: { type: 'canSpot' } } };
    server.sent[0]!.answer(withStep(back, { passWhen: 'type: canSpot' }));
    await going;
    expect(form.work().allow.passWhen).toBe('type: canSpot');
    expect(form.dirty().any).toBe(false);
  });

  it('takes what was changed elsewhere with the answer to a send, and where the step now is', async () => {
    type('id', 'x');
    const going = form.send();
    const state = withStep({ ...step, id: 'x', answer: 'play(sword)' });
    const moved = {
      ...state,
      uids: ['s0', 's1', 's2'],
      draft: { ...state.draft, steps: [{ pause: true }, ...(state.draft.steps ?? [])] },
    };
    server.sent[0]!.answer(moved);
    await going;
    expect(form.work().answer).toBe('play(sword)');
    expect(form.index).toBe(1);
    expect(form.dirty().any).toBe(false);
  });

  it('takes nothing back into a form that is no longer shown, and still sends what was typed in it since', async () => {
    type('id', 'x');
    const first = form.send();
    type('id', 'xy');
    const second = form.send();
    server.isCurrent = false;
    server.sent[0]!.answer(withStep({ ...step, id: 'x', answer: 'play(sword)' }));
    await first;
    expect(form.work().answer).toBeUndefined();
    await tick();
    expect(server.sent[1]?.set).toEqual({ id: 'xy' });
    server.sent[1]!.answer(withStep({ ...step, id: 'xy' }));
    expect(await second).toBe(true);
  });

  it('takes a step changed elsewhere where the form is as sent, but not while a send is on its way', () => {
    type('messages.0.text', 'Typed');
    expect(form.update({ ...step, answer: 'pass' }, {})).toBe(true);
    expect(form.work().answer).toBe('pass');
    expect(form.work().messages[0]?.text).toBe('Typed');
    expect(form.update({ ...step, answer: 'pass' }, {})).toBe(false);
    void form.send();
    expect(form.update({ ...step, answer: 'play(x)' }, {})).toBe(false);
    expect(form.work().answer).toBe('pass');
  });

  it('a step that is gone from the answer leaves the form where it was', async () => {
    type('id', 'x');
    const going = form.send();
    server.sent[0]!.answer({ ...draftState(), uids: ['a', 'b', 'c'] });
    expect(await going).toBe(true);
    expect(form.index).toBe(0);
  });

  it("takes back the server's way of writing a message's condition and a step's refusals", async () => {
    type('messages.0.require', '{type:canSpot, filter:name(Aragorn)}');
    type('refuses', 'choose(a), choose(b)');
    const going = form.send();
    expect(server.sent[0]!.set).toEqual({
      messages: [{ text: 'Hello', require: '{type:canSpot, filter:name(Aragorn)}' }],
      refuses: 'choose(a), choose(b)',
    });
    const require = [{ type: 'canSpot', filter: 'name(Aragorn)' }];
    const back = { ...step, messages: [{ text: 'Hello', require }], refuses: ['choose(a)', 'choose(b)'] };
    const filed = '[\n\t{\n\t\ttype: canSpot\n\t\tfilter: name(Aragorn)\n\t}\n]';
    server.sent[0]!.answer(withStep(back, { 'messages.0.require': filed }));
    await going;
    expect(form.work().messages[0]?.require).toBe(filed);
    expect(form.work().refuses).toBe('choose(a)\nchoose(b)');
    expect(form.dirty().any).toBe(false);
  });

  it("sends a message's other changes when its new condition is refused, and keeps the condition unsent", async () => {
    type('messages.0.require', '{ type: canSpot, filter: ');
    type('messages.0.text', 'Hello there');
    const going = form.send();
    server.sent[0]!.answer(null, 'messages[0].require: bad HJSON');
    await tick();
    // (the same messages, the condition as it was last sent: none)
    expect(server.sent[1]!.set).toEqual({ messages: [{ text: 'Hello there' }] });
    server.sent[1]!.answer(withStep({ ...step, messages: [{ text: 'Hello there' }] }));
    expect(await going).toBe(false);
    expect(server.said).toBe('messages[0].require: bad HJSON');
    expect(form.work().messages[0]?.require).toBe('{ type: canSpot, filter: ');
    expect(form.dirty().set).toEqual({ messages: [{ text: 'Hello there', require: '{ type: canSpot, filter: ' }] });
    // (not sent again as it is; typed in again, it is)
    expect(await form.send()).toBe(false);
    expect(server.sent.length).toBe(2);
    type('messages.0.require', '{ type: canSpot, filter: name(Aragorn) }');
    void form.send();
    expect(server.sent.length).toBe(3);
  });

  it('keeps a condition that was already sent when another one with the same send is refused', async () => {
    const two = new StepForm(
      's1',
      { ...step, messages: [{ text: 'A', require: { type: 'x' } }, { text: 'B' }] },
      { 'messages.0.require': 'type: x' },
      { index: 0, sub: -1 },
      server,
    );
    two.work.update((work) => withValue(withValue(work, 'messages.0.require', 'oops'), 'messages.1.text', 'BB'));
    two.work.update((work) => withValue(work, 'answer', 'pass'));
    const going = two.send();
    server.sent[0]!.answer(null, 'No.');
    await tick();
    expect(server.sent[1]!.set).toEqual({ messages: [{ text: 'A', require: 'oops' }, { text: 'BB' }] });
    server.sent[1]!.answer(null, 'Bad condition.');
    await tick();
    expect(server.sent[2]!.set).toEqual({ messages: [{ text: 'A', require: 'type: x' }, { text: 'BB' }] });
    server.sent[2]!.answer(withStep({ ...step, messages: [{ text: 'A', require: { type: 'x' } }, { text: 'BB' }] }));
    await tick();
    expect(server.sent[3]!.set).toEqual({ answer: 'pass' });
    server.sent[3]!.answer(withStep({ ...step, answer: 'pass' }));
    expect(await going).toBe(false);
    expect(server.said).toBe('Bad condition.');
    expect(Object.keys(two.dirty().set)).toEqual(['messages']);
  });

  it('says a refusal as it is when there is nothing else in the messages to send', async () => {
    type('messages.0.require', 'nonsense');
    const going = form.send();
    server.sent[0]!.answer(null, 'Bad.');
    expect(await going).toBe(false);
    expect(server.sent.length).toBe(1);
  });

  it("sends the step's end without a closing message's refused condition", async () => {
    const ending = new StepForm('s1', { ...step, end: 'complete' }, {}, { index: 0, sub: -1 }, server);
    ending.work.update((work) => withValue(work, 'end.messages', [{ text: 'Bye', require: 'x(' }]));
    const going = ending.send();
    server.sent[0]!.answer(null, 'Bad.');
    await tick();
    expect(server.sent[1]!.set).toEqual({ end: { result: 'complete', messages: [{ text: 'Bye' }] } });
    server.sent[1]!.answer(withStep({ ...step, end: { result: 'complete', messages: ['Bye'] } }));
    expect(await going).toBe(false);
    expect(server.said).toBe('Bad.');
  });
});
