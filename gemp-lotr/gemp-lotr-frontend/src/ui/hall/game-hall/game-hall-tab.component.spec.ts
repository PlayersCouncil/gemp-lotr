import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { HallTestBed, setUpHall } from '../../../testing/hall';
import { GAME_HALL_ROUTES } from './game-hall.routes';

describe('GameHallTabComponent', () => {
  let hall: HallTestBed;
  const motd = { set: (value: string | null) => hall.comms.motd.set(value) };

  async function open() {
    hall = setUpHall([provideRouter([{ path: 'hall', children: GAME_HALL_ROUTES }])]);
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/hall');
    return { harness, el: harness.routeNativeElement as HTMLElement };
  }

  afterEach(() => hall.finish());

  it('shows the link buttons, each in a new tab, then the sections and the dialogs', async () => {
    const { el } = await open();
    const links = Array.from(el.querySelectorAll<HTMLAnchorElement>('.gd-game-hall__link'));
    expect(links.map((a) => [a.dataset['link'], a.textContent?.trim(), a.target])).toEqual([
      ['bug', 'Report a Bug', '_blank'],
      ['report', 'Report a Player', '_blank'],
      ['discord', 'Join the PC Discord', '_blank'],
      ['wiki', 'Browse the LOTR-TCG Wiki', '_blank'],
      ['merchant', 'Merchant', '_blank'],
    ]);
    expect(links[4]?.querySelector('s')?.textContent).toBe('Merchant');
    expect(el.querySelector('.gd-game-hall__info + gd-game-hall-sections + gd-game-hall-dialogs')).not.toBeNull();
  });

  it('shows the message of the day drawn element by element, never as HTML, and nothing without one', async () => {
    const { harness, el } = await open();
    expect(el.querySelector('.gd-game-hall__motd')?.textContent?.trim()).toBe('');
    motd.set('Join <a href="https://lotrtcgpc.net">the PC</a><img src=x onerror="alert(1)">!<br>Bye');
    await harness.fixture.whenStable();
    const line = el.querySelector('.gd-game-hall__motd');
    expect(line?.querySelector('.gd-game-hall__motd-label')?.textContent).toBe('MOTD:');
    expect(line?.textContent?.replace(/\s+/g, ' ').trim()).toBe('MOTD: Join the PC!Bye');
    expect(line?.querySelector('a')?.getAttribute('href')).toBe('https://lotrtcgpc.net');
    expect(line?.querySelector('a')?.target).toBe('_blank');
    expect(line?.querySelector('img')).toBeNull();
    expect(line?.querySelector('br')).not.toBeNull();
  });
});
