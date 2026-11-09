import { TestBed } from '@angular/core/testing';
import { DeckDraft } from './deck-draft.service';

describe('DeckDraft', () => {
  let draft: DeckDraft;

  beforeEach(() => (draft = TestBed.inject(DeckDraft)));

  it('starts as an empty, unnamed, unmodified deck', () => {
    expect(draft.deck().draw).toEqual([]);
    expect(draft.deckName()).toBeNull();
    expect(draft.notes()).toBe('');
    expect(draft.contentsDirty()).toBe(false);
    expect(draft.label()).toEqual({ text: 'New deck', modified: false });
  });

  it('shows "New deck" while it has no name, modified or not', () => {
    draft.markModified(true);
    expect(draft.label()).toEqual({ text: 'New deck', modified: true });
    expect(draft.contentsDirty()).toBe(true);
  });

  it('shows its name, and goes back to unmodified when told', () => {
    draft.deckName.set('Mine');
    draft.markModified(true);
    expect(draft.label()).toEqual({ text: 'Mine', modified: true });
    draft.markModified(false);
    expect(draft.label()).toEqual({ text: 'Mine', modified: false });
    expect(draft.contentsDirty()).toBe(false);
  });
});
