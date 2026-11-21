import { FrameLayout } from './errata-frame-layout';
import { PORTRAIT } from '../../../../core/hall/help/errata/errata-images';

/** A compact frame as the frame component draws it, with the sizes jsdom can't lay out given by hand. */
function frame(body: { width: number; height: number }, noteHeight: (font: number) => number): HTMLElement {
  const root = document.createElement('div');
  root.dataset['layout'] = 'compact';
  root.innerHTML = `
    <div class="gd-errata-frame__body">
      <div class="gd-errata-frame__stage">
        <div class="gd-errata-frame__head"></div>
        <div class="gd-errata-frame__flip"><div class="gd-errata-frame__cardbox">
          <div class="gd-errata-card" data-side="before"></div><div class="gd-errata-card" data-side="after"></div>
        </div></div>
      </div>
      <div class="gd-errata-frame__note"><div class="gd-errata-frame__head"></div></div>
    </div>`;
  const bodyElement = root.querySelector('.gd-errata-frame__body') as HTMLElement;
  Object.defineProperty(bodyElement, 'clientWidth', { value: body.width });
  Object.defineProperty(bodyElement, 'clientHeight', { value: body.height });
  const note = root.querySelector('.gd-errata-frame__note') as HTMLElement;
  Object.defineProperty(note, 'scrollHeight', {
    get: () => noteHeight(parseFloat(note.style.getPropertyValue('--gd-errata-note-font'))),
  });
  return root;
}

describe('FrameLayout', () => {
  it('does nothing while the frame is not laid out', () => {
    const root = frame({ width: 0, height: 0 }, () => 0);
    new FrameLayout(root, PORTRAIT).layout();
    expect(root.dataset['readout']).toBeUndefined();
  });

  it('puts the readout below a card as large as the rest allows, up to a quarter over the scan', () => {
    const root = frame({ width: 580, height: 400 }, () => 80);
    new FrameLayout(root, PORTRAIT).layout();
    expect(root.dataset['readout']).toBe('bottom');
    const after = root.querySelector('.gd-errata-card[data-side=after]') as HTMLElement;
    // 400 - 80 (the text) = 320 for the card
    expect(after.style.getPropertyValue('--gd-errata-card-height')).toBe('320px');
    expect(after.style.getPropertyValue('--gd-errata-card-width')).toBe(`${Math.round(320 * PORTRAIT)}px`);
    expect(after.style.getPropertyValue('--gd-errata-card-border')).toBe('10px');
    const note = root.querySelector('.gd-errata-frame__note') as HTMLElement;
    expect(note.style.getPropertyValue('--gd-errata-note-font')).toBe('13px');
    expect(root.hasAttribute('data-clipped')).toBe(false);
  });

  it('puts the readout beside the card in a short viewer, shrinking the text first', () => {
    const root = frame({ width: 580, height: 200 }, (font) => (font >= 13 ? 260 : 150));
    new FrameLayout(root, PORTRAIT).layout();
    expect(root.dataset['readout']).toBe('side');
    const note = root.querySelector('.gd-errata-frame__note') as HTMLElement;
    expect(note.style.getPropertyValue('--gd-errata-note-font')).toBe('12px');
    expect(note.style.getPropertyValue('--gd-errata-note-width')).not.toBe('');
  });
});
