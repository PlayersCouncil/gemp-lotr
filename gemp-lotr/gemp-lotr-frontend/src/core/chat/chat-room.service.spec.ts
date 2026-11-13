import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../api/gemp-interceptors';
import { parseXml } from '../api/xml';
import { PAGE_QUERY_STRING } from '../platform/page-location';
import { PageLifetime } from '../platform/page-lifetime.service';
import { ChatRoom, chatProblem, parseChat } from './chat-room.service';

const ROOM = '/gemp-lotr-server/chat/Game7';
const chat = (body: string) => `<chat>${body}</chat>`;

describe('parseChat', () => {
  it('reads messages and users, roles from the old marks too', () => {
    expect(
      parseChat(
        parseXml(
          chat(
            '<message from="System" date="5">alice joined</message><message from="bob">hi &lt;b&gt;</message>' +
              '<user role="admin" self="true">alice</user><user>* carol</user><user>+ dave</user><user incognito="true">eve</user>',
          ),
        ),
      ),
    ).toEqual({
      messages: [
        { from: 'System', html: 'alice joined', date: 5, system: true },
        { from: 'bob', html: 'hi <b>', date: null, system: false },
      ],
      users: [
        { name: 'alice', role: 'admin', self: true, incognito: false },
        { name: 'carol', role: 'admin', self: false, incognito: false },
        { name: 'dave', role: 'leagueAdmin', self: false, incognito: false },
        { name: 'eve', role: null, self: false, incognito: true },
      ],
    });
    expect(parseChat(parseXml('<update/>'))).toBeNull();
    expect(parseChat(parseXml(chat('<message>no sender</message>')))?.messages[0]?.from).toBe('');
  });

  it("the legacy chat's warnings", () => {
    expect(chatProblem(0)).toContain('internet connection');
    expect(chatProblem(401)).toBe('You are not logged in. ');
    expect(chatProblem(403)).toContain('no permission');
    expect(chatProblem(404)).toBe('Chat room is closed.');
    expect(chatProblem(410)).toContain('inactive');
    expect(chatProblem(500)).toContain('(500)');
  });
});

describe('ChatRoom', () => {
  let room: ChatRoom;
  let backend: HttpTestingController;

  beforeEach(() => {
    vi.useFakeTimers();
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
        PageLifetime,
        ChatRoom,
      ],
    });
    room = TestBed.inject(ChatRoom);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    backend.verify();
    vi.useRealTimers();
  });

  it('joins, then asks for news after the interval, adding what comes', () => {
    room.join('Game7');
    backend
      .expectOne((r) => r.url === ROOM && r.method === 'GET')
      .flush(chat('<message from="System">hi</message><user>alice</user>'));
    expect(room.messages()).toHaveLength(1);
    expect(room.users().map((u) => u.name)).toEqual(['alice']);
    vi.advanceTimersByTime(2999);
    backend.expectNone((r) => r.url === ROOM && r.method === 'POST');
    vi.advanceTimersByTime(1);
    backend.expectOne((r) => r.url === ROOM && r.method === 'POST').flush(chat('<message from="bob">yo</message>'));
    expect(room.messages().map((m) => m.html)).toEqual(['hi', 'yo']);
    expect(room.users()).toEqual([]);
    vi.advanceTimersByTime(3000);
    backend.expectOne((r) => r.url === ROOM && r.method === 'POST').flush(chat(''));
    expect(room.messages()).toHaveLength(2);
    vi.advanceTimersByTime(3000);
    backend.expectOne((r) => r.url === ROOM).flush('', { status: 410, statusText: 'Gone' });
    expect(room.problem()).toContain('inactive');
    vi.advanceTimersByTime(10000);
  });

  it('ignores an answer that is not a chat', () => {
    room.join('Game7', 100);
    backend.expectOne((r) => r.url === ROOM).flush('<other/>');
    vi.advanceTimersByTime(100);
    expect(room.messages()).toEqual([]);
  });

  it('sends nothing before joining, or after the chat stopped', () => {
    room.send('too early');
    room.join('Game7');
    backend.expectOne((r) => r.url === ROOM && r.method === 'GET').flush('', { status: 404, statusText: 'x' });
    room.send('after the room closed');
    expect(room.problem()).toBe('Chat room is closed.');
  });

  it('sends in order, and a failed send stops the chat', () => {
    room.join('Game7', 60000);
    backend.expectOne((r) => r.url === ROOM && r.method === 'GET').flush(chat(''));
    room.send('one');
    room.send('two');
    const first = backend.expectOne((r) => r.body?.get('message') === 'one');
    backend.expectNone((r) => r.body?.get('message') === 'two');
    first.flush(chat(''));
    backend.expectOne((r) => r.body?.get('message') === 'two').flush('', { status: 403, statusText: 'x' });
    expect(room.problem()).toContain('no permission');
  });
});
