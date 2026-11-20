import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Authoring, StepTabHooks } from '../../../../core/game/authoring/authoring.service';
import { SessionStore } from '../../../../core/platform/session-store';
import { draftState } from '../../../../testing/authoring';
import { provideFakeNotifier } from '../../../../testing/fake-notifier';
import { StepEditor } from './step-editor';

/** The editor with no tab around it, against an Authoring that has the state given (or none). */
function editorWith(state: ReturnType<typeof draftState> | null, ui: Record<string, unknown> = {}) {
  let hooks: StepTabHooks | null = null;
  const draft = signal(state);
  const authoring = {
    draft,
    ui: signal({ selected: 1, sub: null, newFights: [], ...ui }),
    problem: signal<string | null>(null),
    gone: signal(false),
    attachStepTab: (given: StepTabHooks) => {
      hooks = given;
      return () => (hooks = null);
    },
    remember: vi.fn(),
    showTab: vi.fn(),
    changeBlock: vi.fn(),
    focusOn: vi.fn(),
    edit: vi.fn(() => Promise.resolve(null)),
  };
  TestBed.configureTestingModule({
    providers: [StepEditor, provideFakeNotifier(), { provide: Authoring, useValue: authoring }],
  });
  const editor = TestBed.inject(StepEditor);
  TestBed.tick();
  return { editor, authoring, hooks: () => hooks!, draft };
}

describe('StepEditor', () => {
  beforeEach(() => sessionStorage.clear());

  it('has no form, and nothing to send or keep, with no step to show', async () => {
    const { editor, hooks, authoring } = editorWith(null);
    expect(editor.form()).toBeNull();
    expect(editor.place()).toBeNull();
    expect(editor.top()).toBeNull();
    expect(editor.messageAt(0)).toBeNull();
    expect(await hooks().flush(false)).toEqual({ went: true });
    hooks().carry?.();
    expect(TestBed.inject(SessionStore).read('gemp-author-step-unsent')).toBeNull();
    expect(editor.typing()).toBe(false);
    expect(editor.focused()).toBeNull();
    editor.focusMessage(0);
    expect(authoring.focusOn).not.toHaveBeenCalled();
    editor.select(1);
    editor.openPanel(0);
    expect(authoring.remember).not.toHaveBeenCalled();
    await editor.insertInBlock(0, 0, { pause: true });
    expect(authoring.changeBlock).not.toHaveBeenCalled();
    editor.put('id', 'x');
    editor.later();
    expect(await editor.send()).toBe(true);
  });

  it('follows the step selected, and lets go of the form when none is', () => {
    const { editor, draft } = editorWith(draftState());
    expect(editor.form()?.uid).toBe('s1');
    expect(editor.place()).toEqual({ index: 0, sub: -1, kind: 'step', number: '1' });
    draft.set(null);
    TestBed.tick();
    expect(editor.form()).toBeNull();
  });

  it('starts afresh when the panel says so, and is gone with the tab', () => {
    const { editor, hooks } = editorWith(draftState());
    const first = editor.form();
    hooks().reset();
    TestBed.tick();
    expect(editor.form()).not.toBe(first);
    expect(editor.form()?.uid).toBe('s1');
    TestBed.resetTestingModule();
  });

  it('sends nothing more once the page is on its way to a fresh game', async () => {
    const { editor, authoring } = editorWith(draftState());
    authoring.gone.set(true);
    editor.put('id', 'x');
    void editor.send();
    await Promise.resolve();
    expect(authoring.edit).not.toHaveBeenCalled();
  });
});
