import { DOCUMENT } from '@angular/common';
import { Injector } from '@angular/core';
import { SoundPlayer } from './sound-player';

function playerFor(defaultView: unknown): SoundPlayer {
  return Injector.create({
    providers: [{ provide: DOCUMENT, useValue: { defaultView } }, SoundPlayer],
  }).get(SoundPlayer);
}

describe('SoundPlayer', () => {
  it('plays the sound at its address', () => {
    const played: string[] = [];
    class FakeAudio {
      constructor(private readonly url: string) {}
      play(): Promise<void> {
        played.push(this.url);
        return Promise.resolve();
      }
    }
    playerFor({ Audio: FakeAudio }).play('/gemp-lotr/fanfare.mp3');
    expect(played).toEqual(['/gemp-lotr/fanfare.mp3']);
  });

  it('lets a refused sound go quietly (no interaction yet, an old browser, no audio at all)', async () => {
    const rejected = vi.fn();
    class RefusedAudio {
      play(): Promise<void> {
        const refusal = Promise.reject(new DOMException('no interaction yet', 'NotAllowedError'));
        refusal.catch(rejected);
        return refusal;
      }
    }
    class OldAudio {
      play(): undefined {
        return undefined;
      }
    }
    const BrokenAudio = function () {
      throw new Error('no audio device');
    };
    expect(() => playerFor({ Audio: RefusedAudio }).play('a.mp3')).not.toThrow();
    expect(() => playerFor({ Audio: OldAudio }).play('a.mp3')).not.toThrow();
    expect(() => playerFor({ Audio: BrokenAudio }).play('a.mp3')).not.toThrow();
    expect(() => playerFor({}).play('a.mp3')).not.toThrow();
    expect(() => playerFor(null).play('a.mp3')).not.toThrow();
    await Promise.resolve();
    expect(rejected).toHaveBeenCalled();
  });
});
