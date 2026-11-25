import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { createEnvironmentInjector, EnvironmentInjector, runInInjectionContext } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../api/gemp-interceptors';
import { PAGE_QUERY_STRING } from '../platform/page-location';
import { ChatConnection, ChatConnectionHandler } from './chat-connection';
import { ChatSnapshot } from './chat-models';
import { sentForm } from '../../testing/sent-form';

const ROOM = '/gemp-lotr-server/chat/Game%20Hall';
/** A request to the room (the interceptors add a cache-buster to GETs). */
const toRoom = (r: { url: string }) => r.url.split('?')[0] === ROOM;
const chat = (body = '') => `<chat roomName="Game Hall">${body}</chat>`;

describe('ChatConnection', () => {
  let backend: HttpTestingController;
  let injector: ReturnType<typeof createEnvironmentInjector>;
  let connection: ChatConnection;
  let received: ChatSnapshot[];
  let failures: { status: unknown; polling: boolean }[];
  let handler: ChatConnectionHandler;

  beforeEach(() => {
    vi.useFakeTimers();
    TestBed.configureTestingModule({
      providers: [provideGempHttp(), provideHttpClientTesting(), { provide: PAGE_QUERY_STRING, useValue: '' }],
    });
    backend = TestBed.inject(HttpTestingController);
    injector = createEnvironmentInjector([], TestBed.inject(EnvironmentInjector));
    connection = runInInjectionContext(injector, () => new ChatConnection());
    received = [];
    failures = [];
    handler = {
      received: (snapshot) => received.push(snapshot),
      failed: (error, polling) => failures.push({ status: (error as { status?: unknown }).status, polling }),
    };
  });

  afterEach(() => {
    backend.verify();
    vi.useRealTimers();
  });

  it('joins, then polls the set time after each answer, until closed', () => {
    connection.connect('Game Hall', 100, handler);
    expect(connection.open).toBe(true);
    backend.expectOne((r) => toRoom(r) && r.method === 'GET').flush(chat('<message from="bob">hi</message>'));
    expect(received[0]?.messages[0]?.html).toBe('hi');
    vi.advanceTimersByTime(99);
    backend.expectNone(toRoom);
    vi.advanceTimersByTime(1);
    backend.expectOne((r) => toRoom(r) && r.method === 'POST').flush(chat());
    vi.advanceTimersByTime(100);
    const poll = backend.expectOne((r) => toRoom(r) && r.method === 'POST');
    connection.close();
    expect(poll.cancelled).toBe(true);
    vi.advanceTimersByTime(1000);
    expect(received).toHaveLength(2);
  });

  it('stops on a failed join or poll and says so; joining again is possible after', () => {
    connection.connect('Game Hall', 100, handler);
    backend.expectOne(toRoom).flush(chat());
    vi.advanceTimersByTime(100);
    backend.expectOne(toRoom).flush('', { status: 410, statusText: 'Gone' });
    expect(failures).toEqual([{ status: 410, polling: true }]);
    expect(connection.open).toBe(false);
    vi.advanceTimersByTime(1000);
    connection.connect('Game Hall', 100, handler);
    backend.expectOne((r) => toRoom(r) && r.method === 'GET').flush(chat());
    connection.close();
  });

  it('sends one message at a time, in order; a failed send is reported and the updates go on', () => {
    connection.connect('Game Hall', 60_000, handler);
    backend.expectOne((r) => r.method === 'GET').flush(chat());
    connection.send('one');
    connection.send('two');
    const one = backend.expectOne((r) => sentForm(r).get('message') === 'one');
    backend.expectNone((r) => sentForm(r).get('message') === 'two');
    one.flush('', { status: 400, statusText: 'Bad command' });
    expect(failures).toEqual([{ status: 400, polling: false }]);
    backend.expectOne((r) => sentForm(r).get('message') === 'two').flush('');
    expect(connection.open).toBe(true);
    connection.close();
  });

  it('stops when its injector goes; a message being sent finishes, its answer ignored', () => {
    connection.connect('Game Hall', 100, handler);
    backend.expectOne(toRoom).flush(chat());
    connection.send('bye');
    const sent = backend.expectOne((r) => sentForm(r).get('message') === 'bye');
    injector.destroy();
    expect(sent.cancelled).toBe(false);
    sent.flush('', { status: 500, statusText: 'x' });
    vi.advanceTimersByTime(1000);
    expect(failures).toEqual([]);
    connection.send('after');
  });
});
