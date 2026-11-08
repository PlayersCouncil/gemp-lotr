import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CardInfoService } from '../../core/cards/card-info.service';
import { PAGE_QUERY_STRING } from '../../core/platform/page-location';
import { CardInfoComponent } from './card-info.component';
import { provideFakeNotifier } from '../../testing/fake-notifier';

describe('card info dialog', () => {
  beforeEach(async () => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
      ],
    });
  });

  describe('CardInfoComponent', () => {
    it('sizes the dialog for the card, the zoom setting and the wiki link', async () => {
      const fixture = TestBed.createComponent(CardInfoComponent);
      TestBed.inject(CardInfoService).show({
        kind: 'shown',
        blueprintId: '2_16',
        zone: 'player',
        role: '',
      });
      await fixture.whenStable();
      const el = fixture.nativeElement as HTMLElement;
      const dialog = el.querySelector<HTMLElement>('.ui-dialog')!;
      // As measured on the legacy page: a 372x519 card, 397px wide (25px padding), 589px tall (with the wiki link)
      expect(dialog.style.width).toBe('397px');
      const card = el.querySelector<HTMLElement>('#card-image-content > gemp-card-display > div')!;
      expect([card.style.width, card.style.height]).toEqual(['372px', '519px']);
      expect(el.querySelector<HTMLAnchorElement>('.card-info-footer a')!.href).toBe(
        'http://wiki.lotrtcgpc.net/wiki/LOTR02016',
      );
      expect(el.querySelector('.ui-dialog-titlebar-close .ui-icon')!.textContent).toBe('');
    });

    it('keeps the focus where it is when the card area is pressed (the legacy swipe handler)', async () => {
      const fixture = TestBed.createComponent(CardInfoComponent);
      TestBed.inject(CardInfoService).show({
        kind: 'shown',
        blueprintId: '2_16',
        zone: 'player',
        role: '',
      });
      await fixture.whenStable();
      const el = fixture.nativeElement as HTMLElement;
      const press = (target: Element) => {
        const event = new MouseEvent('mousedown', { bubbles: true, cancelable: true });
        target.dispatchEvent(event);
        return event.defaultPrevented;
      };
      expect(press(el.querySelector('#card-info-inner')!)).toBe(true);
      expect(press(el.querySelector('.card-info-footer a')!)).toBe(false);
    });

    it('takes the size of another card shown while open', async () => {
      const fixture = TestBed.createComponent(CardInfoComponent);
      const info = TestBed.inject(CardInfoService);
      info.show({ kind: 'shown', blueprintId: '2_16', zone: 'player', role: '' });
      await fixture.whenStable();
      const dialog = (fixture.nativeElement as HTMLElement).querySelector<HTMLElement>('.ui-dialog')!;
      expect(dialog.style.width).toBe('397px');
      // A landscape site is wider
      info.show({ kind: 'shown', blueprintId: '1_319', zone: 'player', role: '' });
      await fixture.whenStable();
      expect((fixture.nativeElement as HTMLElement).querySelector('.ui-dialog')).toBe(dialog);
      expect(parseInt(dialog.style.width)).toBeGreaterThan(397);
    });
  });
});
