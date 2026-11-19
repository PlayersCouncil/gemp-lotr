import { TestBed } from '@angular/core/testing';
import { AUTO_ZOOM_COOKIE_NAME, AutoZoomSetting } from './auto-zoom-setting';

const COOKIE = 'autoZoomInSomePage';

describe('AutoZoomSetting', () => {
  let cookie: string;
  let written: string[];
  let spies: { mockRestore(): void }[];

  function setting(name = COOKIE): AutoZoomSetting {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [AutoZoomSetting, { provide: AUTO_ZOOM_COOKIE_NAME, useValue: name }],
    });
    return TestBed.inject(AutoZoomSetting);
  }

  beforeEach(() => {
    cookie = '';
    written = [];
    spies = [
      vi.spyOn(document, 'cookie', 'get').mockImplementation(() => cookie),
      vi.spyOn(document, 'cookie', 'set').mockImplementation((value: string) => void written.push(value)),
    ];
  });

  // The cookie accessor spies outlive vi.restoreAllMocks, so they are restored here
  afterEach(() => spies.forEach((spy) => spy.mockRestore()));

  it('is on unless the cookie says false', () => {
    expect(setting().enabled()).toBe(true);
    cookie = `${COOKIE}=false`;
    expect(setting().enabled()).toBe(false);
  });

  it("reads its own page's cookie, not another's", () => {
    cookie = 'autoZoomInGame=false';
    expect(setting().enabled()).toBe(true);
    expect(setting('autoZoomInGame').enabled()).toBe(false);
  });

  it('remembers a toggle in the cookie shared with the legacy page', () => {
    const zoom = setting();
    zoom.toggle();
    expect(zoom.enabled()).toBe(false);
    expect(written[0]).toMatch(new RegExp(`^${COOKIE}=false; expires=.*; path=/gemp-lotr; SameSite=Lax$`));
    zoom.toggle();
    expect(written[1]).toMatch(new RegExp(`^${COOKIE}=true;`));
  });

  it('is not offered where the document takes touch events', () => {
    expect(setting().available).toBe(!('ontouchstart' in document.documentElement));
  });
});
