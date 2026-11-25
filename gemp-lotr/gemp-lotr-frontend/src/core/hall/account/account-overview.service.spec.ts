import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';
import { accountRequest, answerAccount, failAccount, setUpAccount } from '../../../testing/account-server';
import { FakeNotifier, settle } from '../../../testing/fake-notifier';
import { ChatService } from '../../chat/chat.service';
import { Notifier } from '../../notifier/notifier';
import { PlayerInfo } from '../../session/player-info.service';
import { HallComms } from '../hall-comms.service';
import { HallSession } from '../hall-session.service';
import { AccountOverview } from './account-overview.service';
import { sentForm } from '../../../testing/sent-form';

describe('AccountOverview', () => {
  let backend: ReturnType<typeof setUpAccount>['backend'];
  let navigation: ReturnType<typeof setUpAccount>['navigation'];
  let overview: AccountOverview;
  let notifier: FakeNotifier;

  beforeEach(() => {
    ({ backend, navigation } = setUpAccount());
    overview = TestBed.inject(AccountOverview);
    notifier = TestBed.inject(Notifier) as FakeNotifier;
  });

  afterEach(() => backend.verify());

  it("shows the player's name, pocket and the migration button from what the hall knows", async () => {
    expect(overview.pocket()).toBeNull();
    TestBed.inject(HallComms).currency.set(1205);
    expect(overview.pocket()).toEqual({ gold: 12, silver: '05' });
    const player = firstValueFrom(TestBed.inject(PlayerInfo).load());
    accountRequest(backend, '/player').flush('{"name":"alice","type":"u","hasTrophies":true}');
    await player;
    expect(overview.name()).toBe('alice');
    expect(overview.migrateShown()).toBe(true);
  });

  it("loads the ignore list into the chat's, so the user list shows it too", async () => {
    expect(overview.ignored()).toBeNull();
    void overview.loadIgnores();
    answerAccount(backend, '/player/ignores', 'ignores.json');
    await settle();
    expect(overview.ignored()).toEqual(TestBed.inject(ChatService).ignored());
    expect(overview.ignored()?.length).toBeGreaterThan(0);
  });

  it('adds a name, emptying the field unless it was refused', async () => {
    expect(await overview.add('   ')).toBe(false);
    expect(overview.status()).toBe('Type the name of the player to ignore.');

    const added = overview.add(' erin ');
    const request = accountRequest(backend, '/player/ignores/add', 'POST');
    expect(sentForm(request.request).toString()).toBe('name=erin');
    request.flush(
      JSON.parse(
        '{"outcome":"ADDED","changed":true,"error":false,"name":"erin","message":"erin added to your ignore list.","ignored":["Carol","erin"]}',
      ) as object,
    );
    expect(await added).toBe(true);
    expect(overview.status()).toBe('erin added to your ignore list.');
    expect(TestBed.inject(ChatService).ignored()).toEqual(['Carol', 'erin']);

    const refused = overview.add('nobody');
    answerAccount(backend, '/player/ignores/add', 'ignore-refused.json', 'POST');
    expect(await refused).toBe(false);
    expect(overview.status()).toBe("There is no player called 'nobody'.");

    const removed = overview.remove('erin');
    accountRequest(backend, '/player/ignores/remove', 'POST').flush({ message: 'erin removed.', ignored: ['Carol'] });
    await removed;
    expect(overview.ignored()).toEqual(['Carol']);
  });

  it('says what went wrong, or leaves it to the hall', async () => {
    for (const [status, text] of [
      [0, 'Could not reach the server.'],
      [401, 'You are not logged in.'],
      [500, 'Could not load or change your ignore list. Try again later.'],
    ] as const) {
      void overview.loadIgnores();
      failAccount(backend, '/player/ignores', status);
      await settle();
      expect(overview.status()).toBe(text);
    }
    void overview.remove('x');
    failAccount(backend, '/player/ignores/remove', 502, 'POST');
    await settle();
    expect(notifier.dialogs).toEqual([
      'alert: The server could not complete that request (error 502). Try again; if it keeps happening, reload the page.',
    ]);
  });

  it('migrates the trophies once asked, then loads the page again', async () => {
    notifier.answers.push(false, true, true);
    await overview.migrateTrophies();
    expect(notifier.dialogs[0]).toMatch(/^confirm: This will permanently migrate/);

    const migrated = overview.migrateTrophies();
    await settle();
    accountRequest(backend, '/collection/migrate', 'POST').flush('{}');
    await migrated;
    expect(navigation.reloads).toBe(1);

    const refused = overview.migrateTrophies();
    await settle();
    failAccount(backend, '/collection/migrate', 401, 'POST');
    await refused;
    expect(navigation.reloads).toBe(1);
    expect(TestBed.inject(HallSession).loginRequired()).toBe(true);
  });
});
