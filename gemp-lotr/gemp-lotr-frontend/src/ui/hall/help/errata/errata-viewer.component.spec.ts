import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../../../core/api/gemp-interceptors';
import { CardInfoService } from '../../../../core/cards/card-info.service';
import { ERRATA_SERVICES } from '../../../../core/hall/help/errata/errata-services';
import { PcErrataService } from '../../../../core/hall/help/errata/errata.service';
import { ShareLinks } from '../../../../core/hall/share-links';
import { ClipboardWriter } from '../../../../core/platform/clipboard.service';
import { PAGE_ORIGIN } from '../../../../core/platform/page-location';
import { readServerResponse } from '../../../../testing/files';
import { ErrataViewerComponent } from './errata-viewer.component';

describe('ErrataViewerComponent', () => {
  let copied: string[];

  async function render(answer = true) {
    copied = [];
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        ...ERRATA_SERVICES,
        ShareLinks,
        CardInfoService,
        { provide: PAGE_ORIGIN, useValue: 'https://gemp.test' },
        {
          provide: ClipboardWriter,
          useValue: {
            copy: (text: string) => {
              copied.push(text);
              return Promise.resolve(true);
            },
          },
        },
      ],
    });
    const errata = TestBed.inject(PcErrataService);
    const fixture = TestBed.createComponent(ErrataViewerComponent);
    const done = errata.start();
    if (answer) {
      TestBed.inject(HttpTestingController)
        .expectOne((req) => req.url === '/gemp-lotr-server/hall/errata/json')
        .flush(readServerResponse('errata.json'));
      await done;
    }
    fixture.detectChanges();
    await fixture.whenStable();
    return { fixture, errata, viewer: fixture.nativeElement as HTMLElement };
  }

  const texts = (root: Element, selector: string) =>
    Array.from(root.querySelectorAll(selector)).map((element) => element.textContent?.trim());

  it('draws the bar and the card on show (pc-errata.html .pc-errata-viewer)', async () => {
    const { viewer } = await render();
    expect([
      viewer.getAttribute('tabindex'),
      viewer.getAttribute('aria-roledescription'),
      viewer.getAttribute('aria-label'),
    ]).toEqual(['0', 'card viewer', 'Errata viewer']);
    const steps = Array.from(viewer.querySelectorAll<HTMLButtonElement>('.gd-errata-viewer__step'));
    expect(
      steps.map((step) => [step.textContent?.trim(), step.getAttribute('aria-label'), step.title, step.disabled]),
    ).toEqual([
      ['▲', 'Previous card', 'Previous card (↑ or k)', true],
      ['▼', 'Next card', 'Next card (↓ or j)', false],
    ]);
    expect(texts(viewer, '.gd-errata-viewer__counter')).toEqual(['1 of 7']);
    expect(texts(viewer, '.gd-errata-viewer__flip-switch button')).toEqual(['Original', 'PC errata']);
    expect(texts(viewer, '.gd-errata-viewer__enlarge')).toEqual(['Enlarge']);
    expect(viewer.querySelector('.gd-errata-viewer__share')?.getAttribute('aria-label')).toBe(
      'Copy a share link to the errata on show',
    );
    expect(viewer.querySelectorAll('gd-errata-frame')).toHaveLength(1);
    expect((viewer.querySelector('#gd-errata-viewer-keys') as HTMLElement).hidden).toBe(true);
  });

  it('steps with the arrows and flips with the switch', async () => {
    const { fixture, errata, viewer } = await render();
    (viewer.querySelector('[data-step=next]') as HTMLButtonElement).click();
    fixture.detectChanges();
    expect(texts(viewer, '.gd-errata-viewer__counter')).toEqual(['2 of 7']);
    const [original, pc] = Array.from(
      viewer.querySelectorAll<HTMLButtonElement>('.gd-errata-viewer__flip-switch button'),
    );
    expect([original?.getAttribute('aria-pressed'), pc?.getAttribute('aria-pressed')]).toEqual(['false', 'true']);
    original?.click();
    fixture.detectChanges();
    expect(errata.showOriginal()).toBe(true);
    expect(original?.getAttribute('aria-pressed')).toBe('true');
    (viewer.querySelector('.gd-errata-viewer__enlarge') as HTMLButtonElement).click();
    fixture.detectChanges();
    expect(viewer.hasAttribute('data-enlarged')).toBe(true);
    expect(texts(viewer, '.gd-errata-viewer__enlarge')).toEqual(['Close']);
    expect(errata.showOriginal()).toBe(false);
  });

  it('copies the share link of the card on show and says so', async () => {
    const { fixture, viewer } = await render();
    (viewer.querySelector('.gd-errata-viewer__share') as HTMLButtonElement).click();
    await fixture.whenStable();
    fixture.detectChanges();
    expect(copied).toEqual(['https://gemp.test/gemp-lotr/share/errata/51_45']);
    expect(texts(viewer, '.gd-errata-viewer__note')).toEqual(['Link copied']);
  });

  it('steps a card per notch of the wheel', async () => {
    const { fixture, errata, viewer } = await render();
    viewer.dispatchEvent(new WheelEvent('wheel', { deltaY: 30, cancelable: true }));
    expect(errata.index()).toBe(0);
    const event = new WheelEvent('wheel', { deltaY: 30, cancelable: true });
    viewer.dispatchEvent(event);
    fixture.detectChanges();
    expect(event.defaultPrevented).toBe(true);
    expect(errata.index()).toBe(1);
  });

  it('turns the card over on a tap, and steps on a swipe (bindSwipe)', async () => {
    const { fixture, errata, viewer } = await render();
    const reel = viewer.querySelector('.gd-errata-viewer__reel') as HTMLElement;
    const box = viewer.querySelector('.gd-errata-frame__cardbox') as HTMLElement;
    box.dispatchEvent(new MouseEvent('pointerdown', { bubbles: true, clientX: 10, clientY: 100 }));
    reel.dispatchEvent(new MouseEvent('pointerup', { bubbles: true, clientX: 10, clientY: 101 }));
    expect(errata.showOriginal()).toBe(true);
    reel.dispatchEvent(new MouseEvent('pointerdown', { bubbles: true, clientX: 10, clientY: 100 }));
    reel.dispatchEvent(new MouseEvent('pointermove', { bubbles: true, clientX: 10, clientY: 60 }));
    reel.dispatchEvent(new MouseEvent('pointerup', { bubbles: true, clientX: 10, clientY: 50 }));
    fixture.detectChanges();
    expect(errata.index()).toBe(1);
  });

  it('opens the card information on a right-click, with the side’s image', async () => {
    const { viewer } = await render();
    const info = TestBed.inject(CardInfoService);
    const card = viewer.querySelector('gd-errata-card[data-side=before]') as HTMLElement;
    const event = new MouseEvent('contextmenu', { bubbles: true, cancelable: true });
    card.dispatchEvent(event);
    expect(event.defaultPrevented).toBe(true);
    await new Promise((resolve) => setTimeout(resolve, 0));
    expect(info.card()?.blueprintId).toBe('1_45');
    expect(info.card()?.imageUrl).toBe('https://i.lotrtcgpc.net/decipher/LOTR01045.jpg');
    expect(info.title()).toBe('Gandalf, The Grey Wizard (original)');
  });

  it('opens the keys’ help with its (i)', async () => {
    const { fixture, viewer } = await render();
    (viewer.querySelector('[data-info-toggle]') as HTMLButtonElement).click();
    fixture.detectChanges();
    expect((viewer.querySelector('#gd-errata-viewer-keys') as HTMLElement).hidden).toBe(false);
  });

  it('is empty until the errata come', async () => {
    const { viewer } = await render(false);
    expect(viewer.querySelectorAll('gd-errata-frame, .gd-errata-viewer__empty')).toHaveLength(0);
    expect(texts(viewer, '.gd-errata-viewer__counter')).toEqual(['0 of 0']);
    TestBed.inject(HttpTestingController).match(() => true);
  });
});
