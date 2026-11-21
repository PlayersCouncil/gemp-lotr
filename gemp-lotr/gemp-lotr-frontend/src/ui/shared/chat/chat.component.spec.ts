import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { CHAT_PLACEHOLDER_LOGGED_OUT, DISCORD_WIDGET, ONLINE_TITLE } from '../../../core/chat/chat-messages';
import { ChatLine, ChatUser } from '../../../core/chat/chat-models';
import { ChatComponent } from './chat.component';

/** The markup drawn, without the attributes Angular's style scoping adds to the elements. */
const markupOf = (el: Element) => el.innerHTML.replace(/ _ngcontent-[\w-]+=""/g, '');

const line = (id: number, rest: Partial<ChatLine>): ChatLine => ({
  id,
  kind: 'chat',
  from: 'bob',
  date: Date.UTC(2026, 10, 9, 8, 5, 3),
  html: '<p>hi</p>',
  highlight: null,
  loginLink: false,
  ...rest,
});

const USERS: ChatUser[] = [
  { name: 'alice', role: null, self: true, incognito: false },
  { name: 'bob', role: 'admin', self: false, incognito: false },
  { name: 'carol', role: null, self: false, incognito: false },
  { name: 'dave', role: 'leagueAdmin', self: false, incognito: false },
];

@Component({
  imports: [ChatComponent],
  template: `
    <gd-chat
      label="Game Hall chat"
      [lines]="lines()"
      [users]="users()"
      [ignored]="ignored()"
      [status]="status()"
      [canSend]="canSend()"
      [stopped]="stopped()"
      [loggedOut]="loggedOut()"
      [discord]="discord()"
      [invitable]="invitable()"
      userName="alice"
      loginUrl="/gemp-lotr/#game-hall"
      (send)="events.push(['send', $event])"
      (ignoreToggle)="events.push(['ignore', $event])"
      (incognitoChange)="events.push(['incognito', $event])"
      (discordChange)="events.push(['discord', $event])"
      (invite)="events.push(['invite', $event])"
    />
  `,
})
class Host {
  readonly lines = signal<readonly ChatLine[]>([]);
  readonly users = signal<readonly ChatUser[] | null>(USERS);
  readonly ignored = signal<readonly string[] | null>(['Carol']);
  readonly status = signal<string | null>(null);
  readonly canSend = signal(true);
  readonly stopped = signal(false);
  readonly loggedOut = signal(false);
  readonly discord = signal<boolean | null>(false);
  readonly invitable = signal(false);
  readonly events: [string, unknown][] = [];
}

describe('ChatComponent', () => {
  async function render() {
    const fixture = TestBed.createComponent(Host);
    const host = fixture.componentInstance;
    await fixture.whenStable();
    const el = fixture.nativeElement.querySelector('gd-chat') as HTMLElement;
    const update = () => fixture.whenStable();
    const all = (selector: string) => Array.from(el.querySelectorAll<HTMLElement>(selector));
    const text = (selector: string) => all(selector).map((e) => e.textContent!.replace(/\s+/g, ' ').trim());
    return { fixture, host, el, update, all, text };
  }

  afterEach(() => document.head.querySelectorAll('script[data-gd-widgetbot]').forEach((s) => s.remove()));

  it('shows each line: messages with their time and sender, drawn from their markup; warnings as text', async () => {
    const { host, el, update, all, text } = await render();
    host.lines.set([
      line(1, { kind: 'system', from: 'System', html: 'Welcome<br />here' }),
      line(2, { html: '<p>hi @alice <img src="x" onerror="boom()"><script>boom()</script></p>', highlight: 'ping' }),
      line(3, { html: '<p>see <a href="https://wiki.lotrtcgpc.net">wiki</a></p>', highlight: 'mention' }),
      line(4, { kind: 'warning', from: null, date: null, html: 'You are not logged in.', loginLink: true }),
      line(5, { kind: 'warning', from: null, date: null, html: '<b>not markup</b>' }),
    ]);
    await update();

    expect(all('.gd-chat__message').map((m) => [m.dataset['kind'], m.dataset['highlight'] ?? null])).toEqual([
      ['system', null],
      ['chat', 'ping'],
      ['chat', 'mention'],
      ['warning', null],
      ['warning', null],
    ]);
    const time = new Date(Date.UTC(2026, 10, 9, 8, 5, 3));
    const two = (n: number) => String(n).padStart(2, '0');
    const stamp = `[Nov ${time.getDate()} ${two(time.getHours())}:${two(time.getMinutes())}:${two(time.getSeconds())}]`;
    expect(text('.gd-chat__sender')).toEqual([`${stamp} System:`, `${stamp} bob:`, `${stamp} bob:`]);
    expect(el.querySelector('.gd-chat__time')!.getAttribute('datetime')).toBe(time.toISOString());

    const texts = all('.gd-chat__text');
    expect(markupOf(texts[0].querySelector('gd-markup')!)).toBe('Welcome<br>here');
    expect(markupOf(texts[1].querySelector('gd-markup')!)).toBe('<p>hi @alice </p>');
    expect(el.querySelector('img, script')).toBeNull();
    const link = texts[2].querySelector('a')!;
    expect([link.href, link.target, link.rel]).toEqual([
      'https://wiki.lotrtcgpc.net/',
      '_blank',
      'noopener noreferrer',
    ]);
    const login = texts[3].querySelector<HTMLAnchorElement>('a.gd-chat__login')!;
    expect([login.textContent, login.getAttribute('href')]).toEqual([
      'Click here to register or log in.',
      '/gemp-lotr/#game-hall',
    ]);
    expect(texts[4].textContent!.trim()).toBe('<b>not markup</b>');
    expect(el.querySelector('.gd-chat__messages')!.getAttribute('role')).toBe('log');
  });

  it('sends on Enter, not on Shift+Enter, and empties the box', async () => {
    const { host, el } = await render();
    const box = el.querySelector<HTMLTextAreaElement>('.gd-chat__input')!;
    expect(box.placeholder).toBe('Message the game hall...');
    expect(el.querySelector('.gd-chat__label')!.textContent).toBe('Message the game hall');

    box.value = 'hello';
    box.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', shiftKey: true, cancelable: true }));
    expect(host.events).toEqual([]);
    const enter = new KeyboardEvent('keydown', { key: 'Enter', cancelable: true });
    box.dispatchEvent(enter);
    expect(enter.defaultPrevented).toBe(true);
    expect(host.events).toEqual([['send', 'hello']]);
    expect(box.value).toBe('');
    box.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', cancelable: true }));
    expect(host.events).toHaveLength(1);
  });

  it('disables the box while it takes no messages; a visitor is asked to log in, a stopped chat says so', async () => {
    const { host, el, update } = await render();
    const box = el.querySelector<HTMLTextAreaElement>('.gd-chat__input')!;
    host.canSend.set(false);
    host.loggedOut.set(true);
    await update();
    expect(box.disabled).toBe(true);
    expect(box.placeholder).toBe(CHAT_PLACEHOLDER_LOGGED_OUT);
    expect(el.dataset['loggedOut']).toBe('');

    host.loggedOut.set(false);
    host.stopped.set(true);
    await update();
    expect(el.dataset['stopped']).toBe('');
    expect(el.dataset['loggedOut']).toBeUndefined();
  });

  it('lists the players: the own entry with its online / incognito chip, roles, ignored players', async () => {
    const { host, el, update, all, text } = await render();
    expect(text('.gd-chat__user')).toEqual(['alice online', 'bob admin', 'carol (ignored)', 'dave league admin']);
    expect(all('.gd-chat__user').map((u) => [u.dataset['self'], u.dataset['role'], u.dataset['ignored']])).toEqual([
      ['', undefined, undefined],
      [undefined, 'admin', undefined],
      [undefined, undefined, ''],
      [undefined, 'leagueAdmin', undefined],
    ]);
    expect(el.querySelector('.gd-chat__role')!.getAttribute('title')).toBe('Administrator');

    const chip = el.querySelector<HTMLButtonElement>('.gd-chat__visibility')!;
    expect([chip.getAttribute('aria-pressed'), chip.title]).toEqual(['false', ONLINE_TITLE]);
    chip.click();
    expect(host.events).toEqual([['incognito', true]]);
    host.users.set([{ ...USERS[0], incognito: true }]);
    await update();
    expect(el.querySelector<HTMLElement>('.gd-chat__visibility')!.dataset['incognito']).toBe('');
    expect(text('.gd-chat__visibility')).toEqual(['incognito']);

    host.status.set('Ignored bob.');
    await update();
    expect(text('.gd-chat__status')).toEqual(['Ignored bob.']);

    host.users.set(null);
    await update();
    expect(el.querySelector('.gd-chat__people')).toBeNull();
    expect(el.dataset['users']).toBeUndefined();
  });

  it("opens a player's menu: Ignore / Unignore (and Invite to table where offered); Escape closes it", async () => {
    const { host, el, update, all, text } = await render();
    const player = (name: string) =>
      all('.gd-chat__player').find((p) => p.querySelector('.gd-chat__name')!.textContent === name)!;

    player('bob').click();
    await update();
    expect(player('bob').getAttribute('aria-expanded')).toBe('true');
    expect(el.querySelector('.gd-chat__menu')!.getAttribute('role')).toBe('menu');
    expect(text('.gd-chat__menu-item')).toEqual(['Ignore']);
    expect(document.activeElement).toBe(el.querySelector('.gd-chat__menu-item'));
    (el.querySelector('.gd-chat__menu-item') as HTMLElement).click();
    await update();
    expect(host.events).toEqual([['ignore', 'bob']]);
    expect(el.querySelector('.gd-chat__menu')).toBeNull();
    expect(document.activeElement).toBe(player('bob'));

    host.invitable.set(true);
    player('carol').click();
    await update();
    expect(text('.gd-chat__menu-item')).toEqual(['Invite to table', 'Unignore']);
    expect(all('.gd-chat__menu-item').map((i) => i.dataset['action'])).toEqual(['invite', 'unignore']);
    document.activeElement!.dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowDown', bubbles: true }));
    expect(document.activeElement).toBe(all('.gd-chat__menu-item')[1]);
    document.activeElement!.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
    await update();
    expect(el.querySelector('.gd-chat__menu')).toBeNull();
    expect(document.activeElement).toBe(player('carol'));

    player('dave').click();
    await update();
    (el.querySelector('[data-action=invite]') as HTMLElement).click();
    expect(host.events.at(-1)).toEqual(['invite', 'dave']);

    // a press elsewhere closes it
    player('dave').click();
    await update();
    document.body.dispatchEvent(new PointerEvent('pointerdown', { bubbles: true }));
    await update();
    expect(el.querySelector('.gd-chat__menu')).toBeNull();
  });

  it('switches to the Discord chat, created when first shown and kept after', async () => {
    const { host, el, update, text } = await render();
    expect(el.dataset['mode']).toBe('builtin');
    expect(el.querySelector('gd-discord-embed')).toBeNull();
    expect(text('.gd-chat__switch')).toEqual(['Switch to Discord']);
    el.querySelector<HTMLButtonElement>('.gd-chat__switch')!.click();
    expect(host.events).toEqual([['discord', true]]);

    host.discord.set(true);
    await update();
    expect(el.dataset['mode']).toBe('discord');
    expect(el.querySelector<HTMLElement>('.gd-chat__room')!.hidden).toBe(true);
    const widget = el.querySelector('gd-discord-embed widgetbot')!;
    expect(['server', 'channel', 'shard', 'username'].map((a) => widget.getAttribute(a))).toEqual([
      DISCORD_WIDGET.server,
      DISCORD_WIDGET.channel,
      DISCORD_WIDGET.shard,
      'alice',
    ]);
    expect(document.head.querySelectorAll(`script[src="${DISCORD_WIDGET.script}"]`)).toHaveLength(1);
    expect(text('.gd-chat__switch')).toEqual(['Switch to Legacy']);

    host.discord.set(false);
    await update();
    expect(el.querySelector<HTMLElement>('gd-discord-embed')!.hidden).toBe(true);
    expect(el.querySelector<HTMLElement>('.gd-chat__room')!.hidden).toBe(false);

    host.discord.set(null);
    await update();
    expect(el.querySelector('.gd-chat__switch')).toBeNull();
  });
});

@Component({
  imports: [ChatComponent],
  template: `<gd-chat label="Game Hall chat" [lines]="[]" [users]="users()" [userName]="userName()" />`,
})
class WidthHost {
  readonly users = signal<readonly ChatUser[] | null>(null);
  readonly userName = signal<string | null>(null);
}

// chat.js lockUserListWidth: the user list's column is as wide as your own entry with its widest chip ("incognito"),
// measured on a hidden copy; while the list has no entry of yours it is measured for your name without a role.
describe('ChatComponent, the user list width', () => {
  const CHAR = 7;
  let probes: string[];

  beforeEach(() => {
    probes = [];
    const real = HTMLElement.prototype.getBoundingClientRect;
    vi.spyOn(HTMLElement.prototype, 'getBoundingClientRect').mockImplementation(function (this: HTMLElement) {
      if (!this.matches('li.gd-chat__user[aria-hidden]')) return real.call(this);
      const text = this.textContent!;
      probes.push(text);
      return { width: text.length * CHAR, height: 15 } as DOMRect;
    });
  });

  afterEach(() => vi.restoreAllMocks());

  async function render() {
    const fixture = TestBed.createComponent(WidthHost);
    const host = fixture.componentInstance;
    const el = fixture.nativeElement.querySelector('gd-chat') as HTMLElement;
    const update = () => fixture.whenStable();
    const width = () =>
      el.hasAttribute('data-people-width') ? parseFloat(el.style.getPropertyValue('--gd-chat-people-width')) : null;
    await update();
    return { host, el, update, width };
  }

  it('is measured for your own entry, then for your name alone while the list has none of yours', async () => {
    const { host, el, update, width } = await render();
    host.userName.set('Visual tester');
    host.users.set([{ name: 'alice', role: 'admin', self: true, incognito: false }]);
    await update();
    expect(probes).toEqual(['alice admin incognito', 'alice admin online']);
    const own = width()!;
    expect(own).toBeGreaterThan(0);

    probes = [];
    host.users.set([]);
    await update();
    expect(probes).toEqual(['Visual tester incognito', 'Visual tester online']);
    const named = width()!;
    expect(named - own).toBe('Visual tester incognito'.length * CHAR - 'alice admin incognito'.length * CHAR);
    // the hidden copy is taken away again
    expect(el.querySelectorAll('li.gd-chat__user')).toHaveLength(0);

    // the same name and role again: not measured again
    probes = [];
    host.users.set([{ name: 'bob', role: null, self: false, incognito: false }]);
    await update();
    expect(probes).toEqual([]);
    expect(width()).toBe(named);
  });

  it('is left alone while there is no name to measure, or nothing is laid out', async () => {
    const { host, update, width } = await render();
    host.users.set([]);
    await update();
    expect(probes).toEqual([]);
    expect(width()).toBeNull();

    vi.mocked(HTMLElement.prototype.getBoundingClientRect).mockReturnValue({ width: 0, height: 0 } as DOMRect);
    host.userName.set('alice');
    await update();
    expect(width()).toBeNull();
  });
});
