import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { HallComms } from '../../core/hall/hall-comms.service';
import { HALL_CONNECTION_STATUS, OPENED_ELSEWHERE } from '../../core/hall/hall-messages';
import { HallConnection } from '../../core/hall/hall-models';
import { HallConnectionComponent } from './hall-connection.component';

@Component({
  imports: [HallConnectionComponent],
  template: `<gd-hall-connection loginUrl="/gemp-lotr/#game-hall" /><button type="button" class="elsewhere">
      elsewhere
    </button>`,
})
class Host {}

describe('HallConnectionComponent', () => {
  const connection = signal<HallConnection>({ state: 'connecting', detail: null, lastUpdate: null });
  const spoken = signal('Game Hall connecting.');

  async function render() {
    TestBed.configureTestingModule({ providers: [{ provide: HallComms, useValue: { connection, spoken } }] });
    const fixture = TestBed.createComponent(Host);
    document.body.appendChild(fixture.nativeElement);
    await fixture.whenStable();
    const el = fixture.nativeElement.querySelector('gd-hall-connection') as HTMLElement;
    const readout = el.querySelector<HTMLButtonElement>('.gd-hall-connection__readout')!;
    const popup = el.querySelector<HTMLElement>('.gd-hall-connection__popup')!;
    const text = (selector: string) => el.querySelector(selector)?.textContent!.replace(/\s+/g, ' ').trim() ?? null;
    return { fixture, el, readout, popup, text, update: () => fixture.whenStable() };
  }

  afterEach(() => connection.set({ state: 'connecting', detail: null, lastUpdate: null }));

  it("shows the state's word and dot, and announces changes", async () => {
    const { el, text, update } = await render();
    expect(el.dataset['state']).toBe('connecting');
    expect(text('.gd-hall-connection__label')).toBe('Connecting');
    expect(el.querySelector('.gd-hall-connection__live')!.getAttribute('role')).toBe('status');
    expect(text('.gd-hall-connection__live')).toBe('Game Hall connecting.');
    expect(el.querySelector('.gd-hall-connection__login')).toBeNull();

    connection.set({ state: 'connected', detail: null, lastUpdate: '2026-11-09 14:03:05' });
    await update();
    expect(el.dataset['state']).toBe('connected');
    expect(text('.gd-hall-connection__label')).toBe('Connected');

    connection.set({ state: 'loggedout', detail: { message: null, action: 'login' }, lastUpdate: null });
    await update();
    expect(text('.gd-hall-connection__label')).toBe('Not logged in');
    expect(el.querySelector('.gd-hall-connection__login')!.getAttribute('href')).toBe('/gemp-lotr/#game-hall');
  });

  it('opens its popup on hover, and pins it on a click until clicked again, Escape or a click elsewhere', async () => {
    const { el, readout, popup, text, update } = await render();
    connection.set({
      state: 'disconnected',
      detail: { message: OPENED_ELSEWHERE, action: 'reload' },
      lastUpdate: '2026-11-09 14:03:05',
    });
    await update();
    expect(popup.hidden).toBe(true);
    expect(readout.getAttribute('aria-expanded')).toBe('false');

    el.dispatchEvent(new MouseEvent('mouseenter'));
    await update();
    expect(popup.hidden).toBe(false);
    expect(text('.gd-hall-connection__status')).toBe(HALL_CONNECTION_STATUS.disconnected);
    expect(text('.gd-hall-connection__updated')).toBe('Last update: 2026-11-09 14:03:05 (server time)');
    expect(text('.gd-hall-connection__message')).toBe(`${OPENED_ELSEWHERE} Reload the page`);
    expect(el.querySelector<HTMLElement>('.gd-hall-connection__action')!.dataset['action']).toBe('reload');
    el.dispatchEvent(new MouseEvent('mouseleave'));
    await update();
    expect(popup.hidden).toBe(true);

    readout.click();
    await update();
    expect(readout.getAttribute('aria-expanded')).toBe('true');
    el.dispatchEvent(new MouseEvent('mouseleave'));
    await update();
    expect(popup.hidden).toBe(false);
    readout.click();
    await update();
    expect(popup.hidden).toBe(true);

    readout.click();
    await update();
    el.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', code: 'Escape' }));
    await update();
    expect(popup.hidden).toBe(true);
    expect(document.activeElement).toBe(readout);

    readout.click();
    await update();
    document.querySelector<HTMLElement>('.elsewhere')!.click();
    await update();
    expect(popup.hidden).toBe(true);
  });

  it("offers a visitor the login page, without a last update when there wasn't one", async () => {
    const { el, readout, text, update } = await render();
    connection.set({ state: 'loggedout', detail: { message: null, action: 'login' }, lastUpdate: null });
    readout.click();
    await update();
    expect(el.querySelector('.gd-hall-connection__updated')).toBeNull();
    expect(text('.gd-hall-connection__message')).toBe('Log in or register');
    expect(el.querySelector('.gd-hall-connection__action')!.getAttribute('href')).toBe('/gemp-lotr/#game-hall');
  });
});
