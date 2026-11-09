import { TestBed } from '@angular/core/testing';
import { FoilSetting } from './foil-setting';

describe('FoilSetting', () => {
  it('reads the cookie each time it is asked', () => {
    const cookie = vi.spyOn(document, 'cookie', 'get').mockReturnValue('foilPresentation=animated');
    const setting = TestBed.inject(FoilSetting);
    expect(setting.overlayImage()).toBe('/gemp-lotr/images/foil.gif');
    cookie.mockReturnValue('foilPresentation=none');
    expect(setting.overlayImage()).toBeNull();
    cookie.mockRestore();
  });
});
