import { computed, effect, inject, Injectable, Signal, signal, untracked } from '@angular/core';
import { CardInfoService } from '../../cards/card-info.service';
import { Notifier } from '../../notifier/notifier';
import { gamePageUrl } from '../../platform/app-paths';
import { HallTab } from '../../platform/hall-tab.service';
import { PageCues } from '../../platform/page-cues';
import { PageLifetime } from '../../platform/page-lifetime.service';
import { DecisionGate, DecisionGates, DecisionGesture } from '../decision/decision-gate';
import { DecisionButton } from '../decision/decision-view';
import { GameDecisions } from '../decision/game-decisions.service';
import { GameLog } from '../session/game-log';
import { GameSession } from '../session/game-session.service';
import { GameSettings } from '../settings/game-settings';
import { LocalChoices } from '../state/game-model';
import { GameCardRef } from '../table/game-card-views';
import { isFlipped } from '../table-edit/table-seat';
import { ScenarioApi } from './scenario-api.service';
import { MessageSide, messagePlace, ScenarioMessage, ScenarioSettings, ScenarioStep } from './scenario-events';
import { characterComplete, openingMessages, planAllows, PlanContext, scenarioPlan } from './scenario-plan';
import { scenarioRole } from './scenario-state';

/**
 * Where the player is with the step on the screen:
 * - `idle`: nothing of the script is up;
 * - `reading`: its opening messages are up, and the decision can't be answered until they have been read;
 * - `answering`: the decision can be answered (by what the step allows);
 * - `waiting`: a step that is only read (the opponent's, or one put before the player's own decision): the server
 *   waits to be told it has been;
 * - `notice`: the game has left its script and says so;
 * - `ended`: the scenario is over.
 */
export type GuidePhase = 'idle' | 'reading' | 'answering' | 'waiting' | 'notice' | 'ended';

/** What the page says of a click the step doesn't take: at the card or the button that was clicked. */
export interface GuideHint {
  html: string;
  cardId: string | null;
  button: DecisionButton | null;
  /** The answer of a choice, by its place in the list. */
  option: number | null;
}

/** What is to be clicked next, for the page to point out. */
export interface NextClicks {
  cardIds: readonly string[];
  button: DecisionButton | null;
  options: readonly number[];
  /** The numbers the step takes, where a number is asked for: the page points at the way to one, then at OK. */
  numbers: readonly number[];
}

/** What is said of a click the step doesn't take when the step has nothing of its own to say. */
export const STOCK_NOT_NOW = "<p>You could do that in a real game, but for this lesson let's hold off for now.</p>";
export const STOCK_OFF_SCRIPT = '<p>You have left the script.</p>';
/** How long a hint stays up (ms), and how long a failed "I have read it" waits before it is said again. */
export const HINT_MS = 7000;
export const ACKNOWLEDGE_RETRY_MS = 2000;
/** Asked before "Change cards" in the middle of a puzzle's game. */
export const CHANGE_QUESTION = 'Start this puzzle again with its slots empty? This game is given up.';
export const CHANGE_WORDS = { title: 'Change cards', ok: 'Change cards', cancel: 'Keep playing' } as const;
/** Conceding is given this long to reach the server before the page leaves for the hall (ms). */
const LEAVE_MS = 300;

/**
 * What the scenario authoring panel (core/game/authoring, the Authoring service) has the guide do differently while
 * the person is writing the scenario, or testing a draft of it: the guide's one extension point, so it needs nothing
 * of the panel's. The panel attaches it (`attachAuthor`) once it knows the game is a draft's.
 */
export interface ScenarioAuthorHooks {
  /** Writing (not testing): Leave keeps the draft's game as it is (nothing conceded, nothing asked but unsaved changes). */
  readonly writing: Signal<boolean>;
  /** Picking something on the table for a message: nothing there answers the decision, and no key reads on. */
  readonly picking: Signal<boolean>;
  /** The player's settings the draft fixes for its game, as the panel has the draft (null: the scenario info's). */
  readonly settings: Signal<ScenarioSettings | null>;
  /** "Start again" (from the last checkpoint): true when the panel has done it (the script plays itself there). */
  restart(fromCheckpoint: boolean): boolean;
  /** On the way back to the Game Hall (to `hash`): true when the panel has stopped that (to ask about unsaved changes). */
  leaving(hash: string): boolean;
  /** A step has just been put on the screen, or put up again. */
  stepShown?(step: ScenarioStep): void;
  /** What the draft is called now, as the panel has it (null: the scenario info's). */
  readonly title?: Signal<string | null>;
}

/** Where the bubble of a message sits, tried on the screen before the change has been to the server and back. */
export interface TriedPlace {
  side?: MessageSide;
  /** As a message gives it ("3", "20px"); null: the scenario's, else the stock distance. */
  gap?: string | null;
}

const NO_CLICKS: NextClicks = { cardIds: [], button: null, options: [], numbers: [] };

/** A card that isn't on the table but has an action is shown under a made-up id ("extra12"): the card it stands for. */
const realCard = (cardId: string) => (cardId.startsWith('extra') ? cardId.substring(5) : cardId);

/**
 * A scripted game (a scenario) as the person plays it: which of the step's messages is up, whether the decision may
 * be answered yet, which clicks the step takes (it is one of the page's decision gates) and what is said of the
 * others, telling the server a step has been read, and starting over or leaving. The game's model holds what the
 * server said (GameModel.scenario); this holds what only the page knows. Page-scoped.
 */
@Injectable()
export class ScenarioGuide implements DecisionGate {
  private readonly session = inject(GameSession);
  private readonly decisions = inject(GameDecisions);
  private readonly settings = inject(GameSettings);
  private readonly log = inject(GameLog);
  private readonly api = inject(ScenarioApi);
  private readonly lifetime = inject(PageLifetime);
  private readonly cues = inject(PageCues);
  private readonly hall = inject(HallTab);
  private readonly notifier = inject(Notifier);
  private readonly cardInfo = inject(CardInfoService);

  private readonly scenario = computed(() => this.session.model().scenario);
  /** Who the scenario is, or null in an ordinary game. */
  readonly info = computed(() => this.scenario()?.info ?? null);
  readonly step = computed(() => this.scenario()?.step ?? null);
  readonly end = computed(() => this.scenario()?.end ?? null);
  private readonly notice = computed(() => this.scenario()?.notice ?? null);
  /** Whether a scripted game is being played on this page (not: watched again in a replay). */
  readonly playing = computed(() => this.info() !== null && !this.session.replaying());
  /**
   * The role the person plays now (scenarioRole): what "self" and "opp" mean on the table at this moment. In a
   * two-sided game the Free Peoples player on their own turn, the Shadow player on the opponent's.
   */
  readonly role = computed(() => scenarioRole(this.scenario() ?? null));
  /**
   * The checkpoint the game can be taken back to in the middle of it (the scenario's menu), or null. Not in a test of
   * a draft: the panel starts a test again from where it began, never from a checkpoint.
   */
  readonly checkpoint = computed(() => {
    const scenario = this.scenario();
    if (!this.playing() || scenario?.info?.draftTest || this.end() || this.session.model().ended) return null;
    return scenario?.lastCheckpoint ?? null;
  });

  /** The messages the step opens with, and how many of them have been read. */
  private readonly opening = computed(() => {
    const step = this.step();
    return step ? openingMessages(step) : [];
  });
  private readonly read = signal(0);
  /** A message of a moment inside the answering (a card picked, a character given its minions), while it is up. */
  private readonly moment = signal<ScenarioMessage | null>(null);
  /** Counts the times something the player did had to wait for a message to be read: the page shakes its button. */
  readonly nudges = signal(0);
  readonly hint = signal<GuideHint | null>(null);
  /** Telling the server a step was read failed: its button offers to try again. */
  readonly acknowledgeFailed = signal(false);
  /** The page is on its way to another one (starting over, leaving): how this game ends is no longer news. */
  readonly leaving = signal(false);
  private acknowledging: ScenarioStep | null = null;
  /** The authoring panel's say (ScenarioAuthorHooks), while the game is a draft's. */
  private readonly author = signal<ScenarioAuthorHooks | null>(null);
  /** The author is picking something on the table: nothing on it answers the decision, and no key reads on. */
  readonly picking = computed(() => this.author()?.picking() ?? false);
  /** What the scenario is called: a draft's as its author has just named it, else the scenario info's. */
  readonly title = computed(() => this.author()?.title?.() ?? this.info()?.title ?? null);
  /** The author put this message up to look at (preview): the decision can be answered meanwhile. */
  private readonly previewing = signal<ScenarioMessage | null>(null);
  /** Where the author is trying the shown message's bubble (tryPlace): that message, and the place. */
  private readonly tried = signal<{ message: ScenarioMessage; place: TriedPlace } | null>(null);

  readonly phase = computed<GuidePhase>(() => {
    if (this.end()) return 'ended';
    const step = this.step();
    if (!step) return this.notice() ? 'notice' : 'idle';
    if (step.seat === 'opponent' || step.pause) return 'waiting';
    // (an author looking at one of the step's messages can always get on with the game)
    if (this.previewing()) return 'answering';
    const message = this.opening()[this.read()];
    return message && message.advance !== 'decision' ? 'reading' : 'answering';
  });

  /** The message on the screen as the server sent it, or null. */
  private readonly shown = computed<ScenarioMessage | null>(() => {
    const notice = this.notice();
    if (!this.step()) {
      return notice && !this.end() ? noticeMessage(notice.message ?? STOCK_OFF_SCRIPT) : null;
    }
    return this.moment() ?? this.opening()[this.read()] ?? null;
  });

  /** The message on the screen (where its author is trying its bubble, there), or null. */
  readonly message = computed<ScenarioMessage | null>(() => {
    const shown = this.shown();
    const tried = this.tried();
    return shown && tried?.message === shown ? { ...shown, ...tried.place } : shown;
  });

  /** Which of the opening messages is up and how many there are, when there are several; else null. */
  readonly count = computed(() => {
    const opening = this.opening();
    const at = this.moment() ? -1 : this.read();
    return opening.length > 1 && at >= 0 && at < opening.length ? { at: at + 1, of: opening.length } : null;
  });

  /** What the message's button says, or null when it has none (it goes by itself, or ends in a choice). */
  readonly nextLabel = computed<string | null>(() => {
    const message = this.message();
    if (!message || this.session.replaying() || message.choices.length > 0) return null;
    if (this.phase() === 'notice') return 'OK';
    // (also under a message that waits for a card's information: it can't be told again by opening the card)
    if (this.acknowledgeFailed()) return 'Try again';
    if (message.advance !== 'next') return null;
    return this.phase() === 'waiting' && this.read() === this.opening().length - 1 ? 'Continue' : 'Next';
  });

  private readonly context = computed<PlanContext | null>(() => {
    const step = this.step();
    const decision = this.session.model().decision?.decision;
    if (!this.playing() || !step?.allow || step.seat !== 'human' || step.pause || !decision) return null;
    // (a page flipped to the other seat is asked that seat's decisions: a step about the person's own is not about it)
    if (isFlipped(this.info())) return null;
    return { decision, allow: step.allow, local: this.session.model().local, actions: this.decisions.view().actions };
  });

  /** What the step takes next, or null when it takes anything (or nothing is asked). */
  readonly plan = computed(() => {
    const context = this.context();
    return context ? scenarioPlan(context) : null;
  });

  /**
   * How each card the decision offers stands with the step: `live` (what the step wants clicked) or `notnow` (the
   * game would take it, the step doesn't). Empty when the step takes anything.
   */
  readonly cardMarks = computed<Readonly<Record<string, 'live' | 'notnow'>>>(() => {
    const plan = this.plan();
    if (!plan || (this.phase() !== 'answering' && this.phase() !== 'reading')) return {};
    return Object.fromEntries(
      Object.entries(this.decisions.view().marks)
        .filter(([, mark]) => mark !== 'selected')
        .map(([cardId]) => [cardId, plan.liveCards.includes(cardId) ? 'live' : 'notnow'] as const),
    );
  });

  /** What to point out as the next click, while the decision can be answered. */
  readonly nextClicks = computed<NextClicks>(() => {
    const plan = this.plan();
    if (!plan || this.phase() !== 'answering') return NO_CLICKS;
    return {
      cardIds: plan.liveCards,
      button: plan.ready ? 'Done' : plan.liveCards.length === 0 && plan.pass ? 'Pass' : null,
      options: plan.options,
      numbers: plan.numbers,
    };
  });

  /** What the gesture being carried out found: for what came of it (`after`). */
  private before: { local: LocalChoices; ready: boolean } | null = null;

  constructor() {
    inject(DecisionGates).add(this);

    // Another step put up (or the same again): its messages from the first, nothing left over from the last
    effect(() => {
      const shown = this.scenario()?.shown ?? 0;
      untracked(() => {
        this.read.set(0);
        this.moment.set(null);
        this.hint.set(null);
        this.acknowledgeFailed.set(false);
        this.previewing.set(null);
        this.tried.set(null);
        const step = this.step();
        // A step that only waits to be read and has nothing to read has been read
        if (shown > 0 && step && this.phase() === 'waiting' && this.opening().length === 0 && this.playing()) {
          this.acknowledge(null);
        }
        if (shown > 0 && step) this.author()?.stepShown?.(step);
      });
    });

    // The settings the scenario fixes for its game (a replay keeps the watcher's own); a draft's as its author has it
    effect(() => {
      const info = this.info();
      const playing = this.playing();
      const settings = this.author()?.settings() ?? info?.settings ?? {};
      untracked(() => this.settings.fix(info && playing ? settings : {}));
    });

    // What a message says goes into the game log too, as said by the opponent
    effect(() => {
      const message = this.shown();
      const step = this.step();
      const info = this.info();
      if (!message || message.html === '') return;
      // (by its place in the file: the same message is the same line, whatever else its step leaves out)
      const at = step ? `${step.step}.${step.sub}:${messagePlace(step.messages, message)}` : 'notice';
      untracked(() => this.log.say(`scenario ${at}`, info?.opponentName ?? 'Scenario', message.html));
    });

    // A replay shows each opening message in turn, for as long as it takes to read
    effect(() => {
      const animation = this.session.animation()?.animation;
      if (animation?.kind === 'read') untracked(() => this.read.set(animation.message));
    });

    // A message that waits for a card's information to be opened, or closed again
    let open = this.cardInfo.card() !== null;
    effect(() => {
      const card = this.cardInfo.card();
      const wasOpen = open;
      open = card !== null;
      untracked(() => {
        const message = this.message();
        if (!message || !this.playing() || this.acknowledgeFailed()) return;
        if (message.advance === 'inspect' && card && !wasOpen) {
          const wanted = message.advanceCard === null ? null : (this.step()?.cards[message.advanceCard] ?? null);
          const shown = (card.ref as Partial<GameCardRef> | null)?.cardId ?? null;
          if (wanted === null || (shown !== null && realCard(shown) === wanted)) this.next();
        } else if (message.advance === 'close' && !card && wasOpen) {
          this.next();
        }
      });
    });
  }

  /** The message's button: on to the next message, or to the decision, or the step has been read. */
  next(): void {
    if (this.phase() === 'notice') {
      this.session.noticeRead();
      return;
    }
    if (this.moment()) {
      this.moment.set(null);
      return;
    }
    const opening = this.opening();
    if (this.read() >= opening.length) return;
    if (this.read() === opening.length - 1 && this.phase() === 'waiting') {
      this.acknowledge(null);
      return;
    }
    this.read.update((read) => read + 1);
  }

  /** One of the buttons a step ends in. */
  choose(name: string): void {
    this.acknowledge(name);
  }

  /** Tells the server the step has been read; it goes from the screen once the server knows. */
  private acknowledge(choice: string | null): void {
    const step = this.step();
    const gameId = this.session.openGame;
    if (!step || !gameId || this.acknowledging === step) return;
    this.acknowledging = step;
    this.lifetime.write(this.api.acknowledge(gameId, step.step, choice), {
      next: () => {
        this.acknowledging = null;
        this.acknowledgeFailed.set(false);
        if (this.step() === step) this.session.stepRead();
        // (what the server does next comes with the news the page is asking for; if it isn't asking, it asks)
        this.session.refresh();
      },
      error: () => {
        this.acknowledging = null;
        if (this.step() !== step) return;
        if (this.message()) {
          this.acknowledgeFailed.set(true);
          this.nudges.update((count) => count + 1);
        } else {
          // Nothing on the screen to press again: said again by itself
          this.lifetime.later(() => {
            if (this.step() === step) this.acknowledge(choice);
          }, ACKNOWLEDGE_RETRY_MS);
        }
      },
    });
  }

  allows(gesture: DecisionGesture): boolean {
    if (!this.playing()) return true;
    // (an author picking something on the table is not playing)
    if (this.picking()) return false;
    this.hint.set(null);
    const phase = this.phase();
    if (phase === 'reading' || phase === 'waiting' || phase === 'ended') {
      this.nudges.update((count) => count + 1);
      return false;
    }
    const context = this.context();
    if (!context) return true;
    if (!planAllows(context, gesture)) {
      this.refuse(context, gesture);
      return false;
    }
    this.before = { local: context.local, ready: scenarioPlan(context).ready };
    return true;
  }

  after(gesture: DecisionGesture): void {
    const before = this.before;
    this.before = null;
    const context = this.context();
    // (answered: the step has gone, and its moments with it)
    if (!before || !context) return;
    if (gesture.kind === 'button' && (gesture.button === 'Reset choice' || gesture.button === 'Clear selection')) {
      this.moment.set(null);
      return;
    }
    const { local, allow } = context;
    const picked = local.markedCardIds.find((id) => !before.local.markedCardIds.includes(id));
    if (picked !== undefined) this.raise('picked', picked);
    if (local.minion !== null && local.minion !== before.local.minion) this.raise('picked', local.minion);
    for (const [minion, character] of Object.entries(local.assignments)) {
      if (before.local.assignments[minion] !== character && characterComplete(allow, local, character)) {
        this.raise('assigned', character);
      }
    }
    if (!before.ready && scenarioPlan(context).ready) this.raise('ready', null);
  }

  /** The step's message for a moment, if it has one, takes the bubble. */
  private raise(moment: 'picked' | 'assigned' | 'ready', cardId: string | null): void {
    const step = this.step();
    const message = step?.messages.find(
      (candidate) =>
        candidate.on?.moment === moment &&
        (candidate.on.card === null || (cardId !== null && step.cards[candidate.on.card] === realCard(cardId))),
    );
    if (message) this.moment.set(message);
  }

  private refuse(context: PlanContext, gesture: DecisionGesture): void {
    const step = this.step();
    const cardId = gesture.kind === 'card' || gesture.kind === 'action' ? gesture.cardId : null;
    const alias =
      cardId === null || !step
        ? undefined
        : Object.keys(context.allow.hints).find((name) => step.cards[name] === realCard(cardId));
    const hint: GuideHint = {
      html: (alias === undefined ? null : context.allow.hints[alias]) ?? context.allow.otherwise ?? STOCK_NOT_NOW,
      cardId,
      // (a number is given with the OK under it: said there)
      button: gesture.kind === 'button' ? gesture.button : gesture.kind === 'number' ? 'OK' : null,
      option: gesture.kind === 'choice' ? gesture.index : null,
    };
    this.hint.set(hint);
    this.lifetime.later(() => {
      if (this.hint() === hint) this.hint.set(null);
    }, HINT_MS);
  }

  /** The hint has had its say (another click somewhere). */
  dismissHint(): void {
    this.hint.set(null);
  }

  /**
   * The authoring panel's say in the game (see ScenarioAuthorHooks), while it is a draft's; the function returned takes
   * it away again.
   */
  attachAuthor(hooks: ScenarioAuthorHooks): () => void {
    this.author.set(hooks);
    return () => {
      if (this.author() === hooks) this.author.set(null);
    };
  }

  /**
   * Puts message `index` of the step on the screen up (its place among the step's messages in the file), for its
   * author to look at, if that step (`step`, and `sub` in a block) is the one on the screen; the decision can be
   * answered meanwhile. False when it isn't (or has no such message, or left it out: its condition doesn't hold). The
   * next step put up (or this one again) starts afresh.
   */
  preview(step: number, sub: number, index: number): boolean {
    const shown = this.step();
    const message = shown?.messages.find((candidate) => messagePlace(shown.messages, candidate) === index);
    if (!shown || !message || shown.step !== step || shown.sub !== sub) return false;
    const at = this.opening().indexOf(message);
    if (at > -1) {
      this.moment.set(null);
      this.read.set(at);
    } else {
      this.moment.set(message);
    }
    // (only the player's own step has a decision to get on with)
    this.previewing.set(shown.seat === 'human' && !shown.pause ? message : null);
    return true;
  }

  /**
   * Shows the bubble of message `index` of the step on the screen (its place in the file) where its author is trying
   * it (its side, its gap), at once, if that message is the one up; the change itself comes back from the server as
   * the step put up again. False when that message isn't up.
   */
  tryPlace(index: number, place: TriedPlace): boolean {
    const shown = this.shown();
    const messages = this.step()?.messages ?? [];
    if (!shown || messagePlace(messages, shown) !== index) return false;
    const was = this.tried();
    this.tried.set({ message: shown, place: { ...(was?.message === shown ? was.place : {}), ...place } });
    return true;
  }

  /** Starts the scenario again: from the beginning, or from its last checkpoint (a draft's: as its author says). */
  restart(fromCheckpoint = false): void {
    if (this.author()?.restart(fromCheckpoint)) return;
    const info = this.info();
    // (a puzzle's game again keeps its cards: from a checkpoint the server keeps them by itself)
    if (info) this.start(info.id, fromCheckpoint, info.level === 'puzzle' && !fromCheckpoint ? 'same' : null);
  }

  /** The scenario that follows this one in its campaign. */
  startNext(): void {
    const next = this.info()?.campaign?.next;
    if (next) this.start(next.id, false);
  }

  private start(id: string, fromCheckpoint: boolean, picks: 'same' | null = null): void {
    if (this.leaving()) return;
    this.leaving.set(true);
    this.lifetime.write(this.api.start(id, fromCheckpoint, picks), {
      next: (started) => this.cues.go(gamePageUrl(started.gameId)),
      error: () => {
        this.leaving.set(false);
        this.notifier.tell('The scenario could not be started. Go back to the Game Hall and try from there.');
      },
    });
  }

  /** "Start again" asked for in the middle of a game: asked about first. */
  async askRestart(): Promise<void> {
    const over = this.phase() === 'ended' || this.session.model().ended;
    if (over || (await this.notifier.confirm('Start this scenario again from the beginning?'))) this.restart();
  }

  /**
   * "Back to checkpoint" in the middle of a game: asked about first, then the scenario starts again from its last
   * checkpoint (a draft being written: the panel plays the script to it, as its Start again plays it from the start).
   */
  async askCheckpoint(): Promise<void> {
    const checkpoint = this.checkpoint();
    if (checkpoint === null) return;
    if (await this.notifier.confirm(`Go back to the last checkpoint (step ${checkpoint})?`)) this.restart(true);
  }

  /**
   * "Leave": the game is given up (asked about first) and the page goes back to the Game Hall. A draft's game being
   * written stays as it is, to be come back to.
   */
  async leave(): Promise<void> {
    if (this.phase() === 'ended' || this.session.model().ended || this.author()?.writing()) {
      this.backToHall();
      return;
    }
    if (!(await this.notifier.confirm('Leave this scenario? You can start it again from the Game Hall.'))) return;
    this.leaving.set(true);
    this.session.concede();
    this.lifetime.later(() => this.backToHall(), LEAVE_MS);
  }

  /**
   * "Change cards" (a puzzle): the puzzle again from its beginning with its slots empty, to fill them afresh (asked about
   * first while the game is on). Not for a draft's game: its author changes its cards in the authoring panel.
   */
  async changeCards(): Promise<void> {
    const info = this.info();
    if (info?.level !== 'puzzle' || info.author || info.draftTest) return;
    const over = this.phase() === 'ended' || this.session.model().ended;
    if (over || (await this.notifier.confirm(CHANGE_QUESTION, CHANGE_WORDS))) this.start(info.id, false);
  }

  /** Back to the Game Hall (to `hash`, a place in it); an author with unsaved changes is asked about them first. */
  backToHall(hash = ''): void {
    if (this.author()?.leaving(hash)) return;
    this.hall.go(hash);
  }
}

function noticeMessage(html: string): ScenarioMessage {
  return {
    index: null,
    header: null,
    html,
    anchors: [],
    gap: null,
    highlight: [],
    highlightStyle: 'gold',
    avoid: [],
    side: 'auto',
    advance: 'next',
    advanceCard: null,
    choices: [],
    on: null,
    zoom: null,
  };
}
