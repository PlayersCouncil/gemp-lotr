import { ChatApi } from '../chat/chat-api.service';
import { HallApi } from './hall-api.service';
import { provideGempHttp } from '../api/gemp-interceptors';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { readServerResponse } from '../../testing/files';
import { settle } from '../../testing/fake-notifier';
import { ChatService } from '../chat/chat.service';
import { CHAT_NOT_LOGGED_IN } from '../chat/chat-messages';
import { PAGE_QUERY_STRING } from '../platform/page-location';
import { HallComms } from './hall-comms.service';
import { HALL_SERVICES } from './hall-services';
import { HallSession } from './hall-session.service';

describe('HallSession', () => {
  let backend: HttpTestingController;
  let session: HallSession;
  let comms: HallComms;
  let chat: ChatService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        HallApi,
        ChatApi,
        ...HALL_SERVICES,
        { provide: PAGE_QUERY_STRING, useValue: '' },
      ],
    });
    backend = TestBed.inject(HttpTestingController);
    session = TestBed.inject(HallSession);
    comms = TestBed.inject(HallComms);
    chat = TestBed.inject(ChatService);
  });

  afterEach(() => {
    comms.stop();
    chat.stop();
  });

  const expectStarted = () => {
    backend.expectOne((r) => r.url === '/gemp-lotr-server/chat/Game%20Hall');
    backend.expectOne((r) => r.url === '/gemp-lotr-server/hall');
    expect(session.started()).toBe(true);
    expect(comms.isRunning).toBe(true);
    expect(chat.started()).toBe(true);
  };

  it('starts at once on a tab that needs logging in', async () => {
    await session.begin(true);
    expectStarted();
    // Discord takes the player's name when it is made: not shown before the player is known
    expect(chat.discord()).toBe(false);
    backend.expectOne((r) => r.url === '/gemp-lotr-server/player').flush(readServerResponse('player.json'));
    await settle();
    expect(chat.discord()).toBe(true);
  });

  it('on a public tab, asks who is logged in first, and starts for a player', async () => {
    const begun = session.begin(false);
    backend.expectOne((r) => r.url === '/gemp-lotr-server/player').flush(JSON.stringify({ name: 'boss', type: 'au' }));
    await begun;
    expectStarted();
    expect(session.adminTabShown()).toBe(true);
  });

  it('leaves a visitor alone until a tab that needs logging in is opened', async () => {
    const begun = session.begin(false);
    backend
      .expectOne((r) => r.url === '/gemp-lotr-server/player')
      .flush('', { status: 401, statusText: 'Unauthorized' });
    await begun;
    expect(session.started()).toBe(false);
    expect(session.loggedOut()).toBe(true);
    expect(comms.connection().state).toBe('loggedout');
    expect(chat.lines().map((l) => l.html)).toEqual([CHAT_NOT_LOGGED_IN]);
    // the built-in chat, with its warning: not Discord
    expect(chat.discord()).toBe(false);
    expect(session.loginRequired()).toBe(false);
    backend.expectNone(() => true);

    session.loginTabOpened();
    expectStarted();
    session.loginTabOpened();
    expect(session.loginRequired()).toBe(true);
    session.dismissLoginRequired();
    expect(session.loginRequired()).toBe(false);
  });

  it('stops the poll and the chat when the page is left, and starts them afresh when it is shown again', async () => {
    await session.begin(true);
    backend.expectOne((r) => r.url === '/gemp-lotr-server/player').flush(readServerResponse('player.json'));
    backend.expectOne((r) => r.url === '/gemp-lotr-server/chat/Game%20Hall').flush(readServerResponse('chat.xml'));
    const signUp = backend.expectOne((r) => r.url === '/gemp-lotr-server/hall');
    await settle();
    expect(chat.lines()).toHaveLength(5);

    session.stop();
    expect(session.started()).toBe(false);
    expect(comms.isRunning).toBe(false);
    expect(signUp.cancelled).toBe(true);
    backend.match(() => true);
    await new Promise((resolve) => setTimeout(resolve, 200));
    backend.expectNone(() => true);

    await session.begin(true);
    expectStarted();
    // a fresh join: the room's history comes again, so the lines shown before went
    expect(chat.lines()).toEqual([]);
    expect(chat.stopped()).toBe(false);
  });

  it('starts anyway when the player could not be asked', async () => {
    const begun = session.begin(false);
    backend.expectOne((r) => r.url === '/gemp-lotr-server/player').flush('', { status: 500, statusText: 'Error' });
    await begun;
    expectStarted();
    backend.expectOne((r) => r.url === '/gemp-lotr-server/player');
  });
});
