import { DOCUMENT } from '@angular/common';
import { Injector } from '@angular/core';
import { LocalSettings } from './local-settings.service';

function settingsFor(defaultView: unknown): LocalSettings {
  return Injector.create({
    providers: [{ provide: DOCUMENT, useValue: { defaultView } }, LocalSettings],
  }).get(LocalSettings);
}

describe('LocalSettings', () => {
  it("keeps settings in the browser's localStorage", () => {
    const values = new Map<string, string>();
    const settings = settingsFor({
      localStorage: {
        getItem: (key: string) => values.get(key) ?? null,
        setItem: (key: string, value: string) => values.set(key, value),
      },
    });
    expect(settings.read('gemp.hallChat')).toBeNull();
    settings.write('gemp.hallChat', 'builtin');
    expect(values.get('gemp.hallChat')).toBe('builtin');
    expect(settings.read('gemp.hallChat')).toBe('builtin');
  });

  it('reads nothing and remembers nothing when storage is refused or missing', () => {
    const refused = () => {
      throw new DOMException('denied', 'SecurityError');
    };
    const blocked = settingsFor({ localStorage: { getItem: refused, setItem: refused } });
    expect(blocked.read('gemp.hallChat')).toBeNull();
    expect(() => blocked.write('gemp.hallChat', 'builtin')).not.toThrow();

    const noWindow = settingsFor(null);
    expect(noWindow.read('gemp.hallChat')).toBeNull();
    expect(() => noWindow.write('gemp.hallChat', 'builtin')).not.toThrow();
  });
});
