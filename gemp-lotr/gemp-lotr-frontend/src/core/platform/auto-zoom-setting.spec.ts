import { TestBed } from '@angular/core/testing';
import { AUTO_ZOOM_COOKIE, AutoZoomSetting } from './auto-zoom-setting';

describe('AutoZoomSetting', () => {
  let cookie: string;
  let written: string[];
  let spies: { mockRestore(): void }[];

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
    expect(TestBed.inject(AutoZoomSetting).enabled()).toBe(true);
    TestBed.resetTestingModule();
    cookie = `${AUTO_ZOOM_COOKIE}=false`;
    expect(TestBed.inject(AutoZoomSetting).enabled()).toBe(false);
  });

  it('remembers a toggle in the cookie shared with the legacy page', () => {
    const setting = TestBed.inject(AutoZoomSetting);
    setting.toggle();
    expect(setting.enabled()).toBe(false);
    expect(written[0]).toMatch(new RegExp(`^${AUTO_ZOOM_COOKIE}=false; expires=.*; path=/gemp-lotr$`));
    setting.toggle();
    expect(written[1]).toMatch(new RegExp(`^${AUTO_ZOOM_COOKIE}=true;`));
  });

  it('is not offered where the document takes touch events', () => {
    expect(TestBed.inject(AutoZoomSetting).available).toBe(!('ontouchstart' in document.documentElement));
  });
});
