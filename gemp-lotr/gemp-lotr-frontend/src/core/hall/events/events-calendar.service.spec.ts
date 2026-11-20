import { TestBed } from '@angular/core/testing';
import { answerWith, eventsRequest, failWith, setUpEvents } from '../../../testing/events-server';
import { settle } from '../../../testing/fake-notifier';
import { readServerResponse } from '../../../testing/files';
import { EventsCalendar } from './events-calendar.service';

describe('EventsCalendar', () => {
  let backend: ReturnType<typeof setUpEvents>['backend'];
  let calendar: EventsCalendar;

  beforeEach(() => {
    backend = setUpEvents().backend;
    calendar = TestBed.inject(EventsCalendar);
  });

  afterEach(() => backend.verify());

  async function shown(): Promise<void> {
    const done = calendar.show();
    answerWith(backend, '/calendar', 'events-calendar.json');
    await done;
  }

  const event = (id: string) => {
    const state = calendar.state();
    return state.kind === 'loaded' ? state.events.find((candidate) => candidate.id === id)! : null!;
  };

  it('opens on the current UTC month and asks for its whole weeks', async () => {
    const done = calendar.show();
    const request = answerWith(backend, '/calendar', 'events-calendar.json');
    await done;
    expect(request.request.params.get('from')).toBe('2026-10-26');
    expect(request.request.params.get('to')).toBe('2026-12-06');
    expect(calendar.grid()?.title).toBe('November 2026');
    expect(calendar.grid()?.weeks[3]!.days[4]!.today).toBe(true);
  });

  it('moves between months, dropping an answer for a month already left', async () => {
    const first = calendar.nextMonth();
    const second = calendar.previousMonth();
    const [december, november] = backend.match((request) => request.url.endsWith('/calendar'));
    november!.flush(readServerResponse('events-calendar.json'));
    december!.flush('{"events":[]}');
    expect(await first).toBe(false);
    expect(await second).toBe(true);
    expect(calendar.grid()?.title).toBe('November 2026');
    const today = calendar.today();
    answerWith(backend, '/calendar', 'events-calendar.json');
    expect(await today).toBe(true);
  });

  it('says why the calendar could not be loaded', async () => {
    let done = calendar.show();
    failWith(backend, '/calendar', 400);
    await done;
    expect(calendar.state()).toEqual({ kind: 'error', text: 'Could not load the calendar.' });
    expect(calendar.grid()).toBeNull();
    done = calendar.show();
    failWith(backend, '/calendar', 401);
    await done;
    expect(calendar.state()).toEqual({ kind: 'error', text: 'You must be logged in to see the calendar.' });
    done = calendar.show();
    failWith(backend, '/calendar', 503);
    await done;
    expect(calendar.state()).toEqual({
      kind: 'error',
      text: 'Could not load the calendar. The server answered with HTTP 503.',
    });
  });

  it("previews a league in its own drawer, with the facts if it can't be fetched", async () => {
    await shown();
    calendar.select(event('1700000001'));
    expect(calendar.isSelected(event('1700000001'))).toBe(true);
    const selection = calendar.selection()!;
    expect(selection.preview.goLabel).toBe('Go to League');
    expect(selection.drawer?.isOpen()).toBe(true);
    failWith(backend, '/league/1700000001', 500);
    await settle();
    expect(selection.drawer?.content()).toEqual({ kind: 'fallback' });
    expect(calendar.goTo(event('1700000001'))).toEqual({ sub: 'leagues', code: '1700000001' });
  });

  it('previews a scheduled tournament without asking for it, and a started one with its detail', async () => {
    await shown();
    calendar.select(event('wc2026-qualifier'));
    expect(calendar.selection()!.drawer?.content()).toEqual({ kind: 'fallback' });
    calendar.select(event('daily-pc_movie-20261120'));
    answerWith(backend, '/tournament/daily-pc_movie-20261120', 'tournament-playing.xml');
    await settle();
    expect(calendar.selection()!.drawer?.content()?.kind).toBe('data');
    calendar.select(event('weekly:3:2026-12-01'));
    expect(calendar.selection()!.drawer).toBeNull();
    expect(calendar.goTo(event('weekly:3:2026-12-01'))).toBeNull();
  });

  it('keeps the selection through a refresh: rebuilt when the event changed, else its drawer fetched quietly', async () => {
    await shown();
    calendar.select(event('daily-pc_movie-20261120'));
    answerWith(backend, '/tournament/daily-pc_movie-20261120', 'tournament-playing.xml');
    await settle();
    const before = calendar.selection();
    let refreshed = calendar.refresh();
    answerWith(backend, '/calendar', 'events-calendar.json');
    await refreshed;
    expect(calendar.selection()).toBe(before);
    failWith(backend, '/tournament/daily-pc_movie-20261120', 500);
    await settle();
    expect(before!.drawer?.content()?.kind).toBe('data');

    refreshed = calendar.refresh();
    eventsRequest(backend, '/calendar').flush(
      readServerResponse('events-calendar.json').replace(
        '"name":"Daily PC-Movie"',
        '"name":"Daily PC-Movie (renamed)"',
      ),
    );
    await refreshed;
    expect(calendar.selection()).not.toBe(before);
    expect(calendar.selection()!.preview.title).toBe('Daily PC-Movie (renamed)');
    answerWith(backend, '/tournament/daily-pc_movie-20261120', 'tournament-playing.xml');
  });
});
