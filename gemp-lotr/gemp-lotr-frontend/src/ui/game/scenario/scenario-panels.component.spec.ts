import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ScenarioEnd, ScenarioInfo, ScenarioMessage } from '../../../core/game/scenario/scenario-events';
import { ScenarioGuide } from '../../../core/game/scenario/scenario-guide.service';
import { ScenarioRoom } from '../../../core/game/scenario/scenario-room';
import { GameLog } from '../../../core/game/session/game-log';
import { GameSession } from '../../../core/game/session/game-session.service';
import { CompactLayout } from '../../../core/platform/compact-layout.service';
import { HallTab } from '../../../core/platform/hall-tab.service';
import { provideTestCardInteractions } from '../../../testing/card-interactions';
import { BUG_REPORT_URL, endBody, MIN_HEIGHT, MIN_WIDTH, ScenarioPanelsComponent } from './scenario-panels.component';

const INFO: ScenarioInfo = {
  type: 'info',
  id: 'basics-01',
  kind: 'tutorial',
  title: 'The <Basics>',
  humanSeat: 'freeps',
  opponent: '~Scenario',
  opponentName: 'Basic Shadow',
  settings: { autoAccept: true, autoDismissDeckReveal: true, animationSpeed: null },
  bubbleGap: null,
  resumedAt: null,
  campaign: {
    id: 'basics',
    title: 'Learn to Play',
    position: 1,
    count: 2,
    next: { id: 'basics-02-items-damage', title: 'The Basics: Items and Damage' },
  },
  author: false,
  draftTest: false,
};

const end = (result: ScenarioEnd['result'], extra: Partial<ScenarioEnd> = {}): ScenarioEnd => ({
  type: 'end',
  result,
  messages: [],
  message: null,
  checkpoint: null,
  ...extra,
});

describe('ScenarioPanelsComponent', () => {
  let fixture: ComponentFixture<ScenarioPanelsComponent>;
  let el: HTMLElement;
  let guide: {
    playing: ReturnType<typeof signal<boolean>>;
    info: ReturnType<typeof signal<ScenarioInfo | null>>;
    end: ReturnType<typeof signal<ScenarioEnd | null>>;
    leaving: ReturnType<typeof signal<boolean>>;
    askRestart: ReturnType<typeof vi.fn>;
    leave: ReturnType<typeof vi.fn>;
    restart: ReturnType<typeof vi.fn>;
    startNext: ReturnType<typeof vi.fn>;
    backToHall: ReturnType<typeof vi.fn>;
    title: () => string | null;
  };
  let hall: { elsewhere: ReturnType<typeof signal<string | null>>; openHere: ReturnType<typeof vi.fn> };
  const phone = signal(false);
  const ended = signal(false);
  const feedback = signal<string | null>(null);
  let size = { width: 1280, height: 800 };

  beforeEach(() => {
    size = { width: 1280, height: 800 };
    for (const [name, value] of [
      ['clientWidth', () => size.width],
      ['clientHeight', () => size.height],
    ] as const) {
      Object.defineProperty(document.documentElement, name, { configurable: true, get: value });
    }
    phone.set(false);
    ended.set(false);
    feedback.set(null);
    guide = {
      playing: signal(true),
      info: signal<ScenarioInfo | null>(INFO),
      end: signal<ScenarioEnd | null>(null),
      leaving: signal(false),
      askRestart: vi.fn(),
      leave: vi.fn(),
      restart: vi.fn(),
      startNext: vi.fn(),
      backToHall: vi.fn(),
      title: () => guide.info()?.title ?? null,
    };
    hall = { elsewhere: signal<string | null>(null), openHere: vi.fn() };
    TestBed.configureTestingModule({
      providers: [
        ...provideTestCardInteractions(),
        ScenarioRoom,
        { provide: ScenarioGuide, useValue: guide },
        { provide: GameSession, useValue: { model: () => ({ ended: ended() }) } },
        { provide: GameLog, useValue: { feedback } },
        { provide: HallTab, useValue: hall },
        { provide: CompactLayout, useValue: { gameActive: phone } },
      ],
    });
  });

  afterEach(() => {
    delete (document.documentElement as { clientWidth?: number }).clientWidth;
    delete (document.documentElement as { clientHeight?: number }).clientHeight;
  });

  function render() {
    fixture = TestBed.createComponent(ScenarioPanelsComponent);
    el = fixture.nativeElement as HTMLElement;
    fixture.detectChanges();
  }
  const refresh = () => fixture.detectChanges();
  const panel = () => el.querySelector<HTMLElement>('section.gd-scenario-panel');
  const text = (selector: string) => el.querySelector(selector)?.textContent.replace(/\s+/g, ' ').trim() ?? null;
  const buttons = () =>
    [...el.querySelectorAll<HTMLButtonElement>('[data-panel-button]')].map(
      (button) =>
        `${button.textContent.trim()}${button.hasAttribute('data-main') ? ' (main)' : ''}${button.hasAttribute('data-onward') ? ' (onward)' : ''}`,
    );
  const press = (label: string) => {
    el.querySelector<HTMLButtonElement>(`[data-panel-button="${label}"]`)?.click();
    refresh();
  };

  it('shows nothing in an ordinary game or a replay', () => {
    guide.playing.set(false);
    render();
    expect(el.querySelector('nav, section')).toBeNull();
  });

  it("has a menu with the scenario's name, Start again, Leave and Report a Bug, and no panel while it is played", () => {
    render();
    expect(text('.gd-scenario-menu__title')).toBe('The <Basics>');
    expect(panel()).toBeNull();
    el.querySelector<HTMLButtonElement>('[data-scenario-menu="restart"]')?.click();
    expect(guide.askRestart).toHaveBeenCalled();
    el.querySelector<HTMLButtonElement>('[data-scenario-menu="leave"]')?.click();
    expect(guide.leave).toHaveBeenCalled();
    const report = el.querySelector<HTMLAnchorElement>('a[data-scenario-menu="report"]');
    expect(report?.href).toBe(BUG_REPORT_URL);
    expect(report?.target).toBe('_blank');
    // Clear of a panel docked at the right
    TestBed.inject(ScenarioRoom).reservedRight.set(320);
    refresh();
    expect(el.querySelector<HTMLElement>('nav')?.style.getPropertyValue('--gd-reserved')).toBe('320px');
  });

  it('says a window too small has no room for the scenario, and lets the player carry on anyway', () => {
    size = { width: MIN_WIDTH - 1, height: 900 };
    render();
    expect(panel()?.dataset['panel']).toBe('small');
    expect(text('h2')).toBe('This needs a bigger screen');
    expect(buttons()).toEqual(['Back to the Game Hall (main)', 'Carry on anyway']);
    press('Back to the Game Hall');
    expect(guide.backToHall).toHaveBeenCalled();
    press('Carry on anyway');
    expect(panel()).toBeNull();
  });

  it('takes the notice away when the window is made large enough, and puts it back when made too small', () => {
    size = { width: 1200, height: MIN_HEIGHT - 1 };
    render();
    expect(panel()?.dataset['panel']).toBe('small');
    size = { width: 1200, height: MIN_HEIGHT };
    window.dispatchEvent(new Event('resize'));
    refresh();
    expect(panel()).toBeNull();
    size = { width: 900, height: 900 };
    window.dispatchEvent(new Event('resize'));
    refresh();
    expect(panel()?.dataset['panel']).toBe('small');
    // A game that is over needs no room
    ended.set(true);
    refresh();
    expect(panel()).toBeNull();
  });

  it("says on the phone layout that a scenario can't be played there, with no menu", () => {
    phone.set(true);
    size = { width: 400, height: 800 };
    render();
    expect(el.querySelector('nav')).toBeNull();
    expect(panel()?.dataset['panel']).toBe('phone');
    expect(text('.gd-scenario-panel__body')).toBe('This scenario cannot be played on mobile devices.');
    press('Back to the Game Hall');
    expect(guide.backToHall).toHaveBeenCalled();
  });

  it('says the Game Hall is open in another tab, and opens it here if asked, where it was asked to go', () => {
    render();
    hall.elsewhere.set('#account/scenarios');
    refresh();
    expect(panel()?.dataset['panel']).toBe('hall');
    expect(text('h2')).toBe('The Game Hall is already open');
    press('Open it here instead');
    expect(hall.openHere).toHaveBeenCalledWith('#account/scenarios');
  });

  describe('the end', () => {
    function ending(result: ScenarioEnd['result'], extra: Partial<ScenarioEnd> = {}, info: ScenarioInfo | null = INFO) {
      guide.info.set(info);
      guide.end.set(end(result, extra));
      render();
    }

    it('of one of a campaign, complete: back and again on the left, the next scenario on the right', () => {
      ending('complete');
      expect(panel()?.dataset).toMatchObject({ panel: 'end', result: 'complete' });
      expect(text('h2')).toBe('Complete');
      expect(el.querySelector('.gd-scenario-panel__body')?.innerHTML).toBe(
        '<p>You have finished <b>The &lt;Basics&gt;</b>.</p>',
      );
      expect(buttons()).toEqual(['Back to the Game Hall', 'Play it Again', 'Next Scenario (main) (onward)']);
      expect(el.querySelector<HTMLButtonElement>('[data-panel-button="Next Scenario"]')?.title).toBe(
        'The Basics: Items and Damage',
      );
      press('Next Scenario');
      expect(guide.startNext).toHaveBeenCalled();
      press('Play it Again');
      expect(guide.restart).toHaveBeenCalledWith();
      press('Back to the Game Hall');
      expect(guide.backToHall).toHaveBeenCalled();
    });

    it('of the last of a campaign: says so, and the way back is the main one', () => {
      ending('complete', {}, { ...INFO, campaign: { ...INFO.campaign!, next: null } });
      expect(el.querySelector('.gd-scenario-panel__body')?.innerHTML).toBe(
        '<p>You have finished <b>The &lt;Basics&gt;</b>.</p><p>That was the last of Learn to Play.</p>',
      );
      expect(buttons()).toEqual(['Back to the Game Hall (main)', 'Play it Again']);
    });

    it('of one on its own, complete: play it again, or back to the hall', () => {
      ending('complete', { checkpoint: 3 }, { ...INFO, campaign: null });
      expect(buttons()).toEqual(['Play it again', 'Back to the Game Hall (main)']);
      press('Play it again');
      expect(guide.restart).toHaveBeenCalledWith();
    });

    it('failed: from the last checkpoint if there is one, else from the start', () => {
      ending('failed', { checkpoint: 4 });
      expect(text('h2')).toBe('Not this time');
      expect(text('.gd-scenario-panel__body')).toBe("That didn't work out. Have another go.");
      expect(buttons()).toEqual(['Back to the last checkpoint (main)', 'Start again', 'Back to the Game Hall']);
      press('Back to the last checkpoint');
      expect(guide.restart).toHaveBeenCalledWith(true);
      press('Start again');
      expect(guide.restart).toHaveBeenLastCalledWith();
    });

    it('failed with no checkpoint, or off the script: start again is the main way', () => {
      ending('failed');
      expect(buttons()).toEqual(['Start again (main)', 'Back to the Game Hall']);
      fixture.destroy();
      ending('off_script');
      expect(text('h2')).toBe("That's not how this one goes");
      expect(buttons()).toEqual(['Start again (main)', 'Back to the Game Hall']);
    });

    it('abandoned: never from a checkpoint, saying why a game closes', () => {
      ending('abandoned', { checkpoint: 2 });
      expect(text('h2')).toBe('This game has been closed');
      expect(text('.gd-scenario-panel__body')).toContain('Your game of The <Basics> is no longer running.');
      expect(buttons()).toEqual(['Start again (main)', 'Back to the Game Hall']);
    });

    it('in an error: no starting again from the beginning; the checkpoint, if any, or back', () => {
      ending('error', { checkpoint: 2 });
      expect(text('h2')).toBe('Something went wrong');
      expect(buttons()).toEqual(['Back to the last checkpoint (main)', 'Back to the Game Hall']);
      fixture.destroy();
      ending('error', {}, null);
      expect(buttons()).toEqual(['Back to the Game Hall (main)']);
    });

    it("in the scenario's own words when it has some", () => {
      ending('complete', {
        messages: [
          { header: 'Well <done>', html: '<p>You won.</p>' } as ScenarioMessage,
          { header: null, html: '<p>Again: <b></b></p>' } as ScenarioMessage,
        ],
        message: '<p>Bye</p>',
      });
      expect(el.querySelector('.gd-scenario-panel__body')?.innerHTML).toBe(
        '<h3>Well &lt;done&gt;</h3><p>You won.</p><p>Again: <b>The &lt;Basics&gt;</b></p><p>Bye</p>',
      );
    });

    it("links the feedback form with this game's replay filled in, when the server has named it", () => {
      ending('complete');
      expect(el.querySelector('.gd-scenario-panel__report')).toBeNull();
      feedback.set(
        'Thank you for playing! <a href="https://forms.example/form?replay=asdf$123" target="_blank">form</a>',
      );
      refresh();
      expect(el.querySelector<HTMLAnchorElement>('.gd-scenario-panel__report a')?.href).toBe(
        'https://forms.example/form?replay=asdf$123',
      );
      // As the server writes it: single quotes, and a space after href=
      feedback.set(
        "Thank you for playing! <a href= 'https://forms.example/form?replay=asdf$456' target='_blank'>form</a>",
      );
      refresh();
      expect(el.querySelector<HTMLAnchorElement>('.gd-scenario-panel__report a')?.href).toBe(
        'https://forms.example/form?replay=asdf$456',
      );
      // Not a link the page can read: no report
      feedback.set('Thank you for playing!');
      refresh();
      expect(el.querySelector('.gd-scenario-panel__report')).toBeNull();
    });

    it('is not shown while the page leaves for another game', () => {
      ending('complete');
      guide.leaving.set(true);
      refresh();
      expect(panel()).toBeNull();
    });
  });

  it('says of an ending with no scenario named "this scenario"', () => {
    expect(endBody(end('complete'), null)).toBe('<p>You have finished <b>this scenario</b>.</p>');
    expect(endBody(end('error', { message: '<p>Sorry</p>' }), INFO)).toBe('<p>Sorry</p>');
  });
});
