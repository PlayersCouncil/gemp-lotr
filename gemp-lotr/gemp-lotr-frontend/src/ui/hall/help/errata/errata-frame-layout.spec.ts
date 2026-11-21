import { FrameLayout } from './errata-frame-layout';
import { LANDSCAPE, PORTRAIT } from '../../../../core/hall/help/errata/errata-images';

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

/** An enlarged frame (both cards side by side, the readout below), sizes given by hand. */
function fullFrame(
  body: { width: number; height: number; scrollHeight?: number },
  noteHeight: (font: number, width: number) => number,
  naturalHeight?: number,
): HTMLElement {
  const root = document.createElement('div');
  root.dataset['layout'] = 'full';
  const natural = naturalHeight === undefined ? '' : ` data-natural-height="${naturalHeight}"`;
  root.innerHTML = `
    <div class="gd-errata-frame__head"></div>
    <div class="gd-errata-frame__body">
      <div class="gd-errata-frame__pair">
        <figure class="gd-errata-frame__figure"><figcaption>Original</figcaption>
          <div class="gd-errata-card" data-side="before"${natural}></div></figure>
        <div class="gd-errata-frame__arrow"></div>
        <figure class="gd-errata-frame__figure"><figcaption>PC errata</figcaption>
          <div class="gd-errata-card" data-side="after"></div></figure>
      </div>
      <div class="gd-errata-frame__note"></div>
    </div>`;
  const bodyElement = root.querySelector('.gd-errata-frame__body') as HTMLElement;
  Object.defineProperty(bodyElement, 'clientWidth', { value: body.width });
  Object.defineProperty(bodyElement, 'clientHeight', { value: body.height });
  Object.defineProperty(bodyElement, 'scrollHeight', { value: body.scrollHeight ?? 0 });
  const note = root.querySelector('.gd-errata-frame__note') as HTMLElement;
  Object.defineProperty(note, 'scrollHeight', {
    get: () =>
      noteHeight(
        parseFloat(note.style.getPropertyValue('--gd-errata-note-font')),
        parseFloat(note.style.getPropertyValue('--gd-errata-note-width')),
      ),
  });
  return root;
}

const prop = (root: HTMLElement, selector: string, name: string) =>
  (root.querySelector(selector) as HTMLElement).style.getPropertyValue(name);

describe('FrameLayout enlarged (layoutFull)', () => {
  it('puts both cards side by side, as tall as the text below leaves them, the arrow at their middle', () => {
    // 600 - 60 (the text) - 18 (a caption) = 522 for the cards; the width would allow 653
    const root = fullFrame({ width: 1000, height: 600 }, () => 60);
    new FrameLayout(root, PORTRAIT).layout();
    expect(root.hasAttribute('data-column')).toBe(false);
    expect(prop(root, '.gd-errata-card[data-side=after]', '--gd-errata-card-height')).toBe('522px');
    expect(prop(root, '.gd-errata-card[data-side=before]', '--gd-errata-card-width')).toBe(
      `${Math.round(522 * PORTRAIT)}px`,
    );
    expect(prop(root, '.gd-errata-frame__arrow', '--gd-errata-arrow-height')).toBe('522px');
    expect(prop(root, '.gd-errata-frame__note', '--gd-errata-note-font')).toBe('15px');
    expect(prop(root, '.gd-errata-frame__note', '--gd-errata-note-width')).toBe('1000px');
    expect(root.hasAttribute('data-clipped')).toBe(false);
  });

  it('draws the cards no bigger than a quarter over the scans’ own size', () => {
    const root = fullFrame({ width: 1000, height: 600 }, () => 60, 300);
    new FrameLayout(root, PORTRAIT).layout();
    expect(prop(root, '.gd-errata-card[data-side=after]', '--gd-errata-card-height')).toBe('375px');
  });

  it('stacks sites one above the other, the text beside them, when that shows them bigger', () => {
    // side by side the width allows 193px; stacked, 330px wide (55% of the width) is 237px high
    const root = fullFrame({ width: 600, height: 700 }, () => 40);
    new FrameLayout(root, LANDSCAPE).layout();
    expect(root.hasAttribute('data-column')).toBe(true);
    expect(prop(root, '.gd-errata-card[data-side=after]', '--gd-errata-card-height')).toBe('237px');
    expect(prop(root, '.gd-errata-frame__pair', '--gd-errata-pair-width')).toBe('330px');
    expect(prop(root, '.gd-errata-frame__note', '--gd-errata-note-width')).toBe('270px');
    expect(prop(root, '.gd-errata-frame__arrow', '--gd-errata-arrow-height')).toBe('34px');
  });

  it('keeps sites side by side when stacking them would not show them bigger', () => {
    const root = fullFrame({ width: 1200, height: 500 }, () => 40);
    new FrameLayout(root, LANDSCAPE).layout();
    expect(root.hasAttribute('data-column')).toBe(false);
    expect(prop(root, '.gd-errata-frame__pair', '--gd-errata-pair-width')).toBe('');
  });

  it('shrinks the text, then gives the cards 40px and says the text is cut off, in a viewer too short for both', () => {
    const fonts: number[] = [];
    const root = fullFrame({ width: 800, height: 100, scrollHeight: 400 }, (font) => {
      fonts.push(font);
      return 200;
    });
    new FrameLayout(root, PORTRAIT).layout();
    expect([...new Set(fonts)]).toEqual([15, 14, 13, 12]);
    expect(prop(root, '.gd-errata-card[data-side=after]', '--gd-errata-card-height')).toBe('40px');
    expect(prop(root, '.gd-errata-frame__note', '--gd-errata-note-font')).toBe('12px');
    expect(root.hasAttribute('data-clipped')).toBe(true);
  });
});

describe('FrameLayout compact, text too long even small', () => {
  it('beside the card at its smallest, and says it is cut off', () => {
    // below, the card would get 110px; beside, 56% of 287px wide
    const root = frame({ width: 580, height: 400 }, () => 999);
    new FrameLayout(root, PORTRAIT).layout();
    expect(root.dataset['readout']).toBe('side');
    expect(
      (root.querySelector('.gd-errata-card[data-side=after]') as HTMLElement).style.getPropertyValue(
        '--gd-errata-card-width',
      ),
    ).toBe(`${Math.round(Math.floor((400 * PORTRAIT * 0.56) / PORTRAIT) * PORTRAIT)}px`);
    expect(
      (root.querySelector('.gd-errata-frame__note') as HTMLElement).style.getPropertyValue('--gd-errata-note-font'),
    ).toBe('10px');
    expect(root.hasAttribute('data-clipped')).toBe(true);
  });

  it('takes a loaded image’s own shape and size', () => {
    const root = frame({ width: 580, height: 400 }, () => 80);
    for (const card of Array.from(root.querySelectorAll<HTMLElement>('.gd-errata-card'))) {
      card.dataset['aspect'] = '0.5';
      card.dataset['naturalHeight'] = '200';
    }
    new FrameLayout(root, PORTRAIT).layout();
    // 200 * 1.25 = 250 at most
    const after = root.querySelector('.gd-errata-card[data-side=after]') as HTMLElement;
    expect(after.style.getPropertyValue('--gd-errata-card-height')).toBe('250px');
    expect(after.style.getPropertyValue('--gd-errata-card-width')).toBe('125px');
  });
});
