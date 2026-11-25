import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../api/gemp-interceptors';
import { PAGE_QUERY_STRING } from '../platform/page-location';
import { PageLifetime } from '../platform/page-lifetime.service';
import { chatWarning } from './chat-messages';
import { ChatRoom } from './chat-room.service';

const ROOM = '/gemp-lotr-server/chat/Game7';
const chat = (body: string) => `<chat>${body}</chat>`;

describe("the game page's chat warnings", () => {
  it("are the legacy chat's (chatErrorMap), shared with the hall", () => {
    expect(chatWarning(0).text).toContain('internet connection');
    expect(chatWarning(401).text).toBe('You are not logged in.');
    expect(chatWarning(403).text).toContain('no permission');
    expect(chatWarning(404).text).toBe('Chat room is closed.');
    expect(chatWarning(410).text).toContain('inactive');
    expect(chatWarning(500).text).toContain('(error=500)');
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
