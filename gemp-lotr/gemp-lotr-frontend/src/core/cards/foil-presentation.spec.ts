import { foilOverlayImage, readFoilPresentation } from './foil-presentation';

describe('foil presentation', () => {
  it('reads the setting from the cookie, defaulting to the static layer', () => {
    expect(readFoilPresentation('')).toBe('static');
    expect(readFoilPresentation('a=1; foilPresentation=animated; b=2')).toBe('animated');
    expect(readFoilPresentation('foilPresentation=none')).toBe('none');
  });

  it('understands the pre-2025 true/false cookie', () => {
    expect(readFoilPresentation('foilPresentation=true')).toBe('animated');
    expect(readFoilPresentation('foilPresentation=false')).toBe('static');
  });

  it('picks the layer image', () => {
    expect(foilOverlayImage('static')).toBe('/gemp-lotr/images/holo.jpg');
    expect(foilOverlayImage('animated')).toBe('/gemp-lotr/images/foil.gif');
    expect(foilOverlayImage('none')).toBeNull();
  });
});
