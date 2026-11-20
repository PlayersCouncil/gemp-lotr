import { DOCUMENT } from '@angular/common';
import { inject, Injectable } from '@angular/core';

/**
 * Plays short sounds (the hall's fanfare when a game starts, hallUi.js PlaySound). Browsers refuse to play before the
 * first interaction with the page; a sound is only a hint, so a refusal is ignored.
 */
@Injectable({ providedIn: 'root' })
export class SoundPlayer {
  private readonly window = inject(DOCUMENT).defaultView;

  play(url: string): void {
    const AudioClass = (this.window as (Window & { Audio?: typeof Audio }) | null)?.Audio;
    if (!AudioClass) {
      return;
    }
    try {
      const playing = new AudioClass(url).play();
      // An old browser's play() gives no promise
      (playing as Promise<void> | undefined)?.catch(() => undefined);
    } catch {
      // same
    }
  }
}
