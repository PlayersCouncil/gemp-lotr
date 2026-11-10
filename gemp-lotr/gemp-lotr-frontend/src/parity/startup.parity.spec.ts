import { AngularPage, openAngularPage } from '../testing/parity/angular-page';
import { FakeGempServer } from '../testing/parity/fake-gemp-server';
import { LegacyPage, openLegacyPage } from '../testing/parity/legacy-page';
import { driveAngular, driveLegacy, PageDriver } from '../testing/parity/page-driver';

describe('parity with the legacy page: as the page opens', () => {
  let legacyPage: LegacyPage;
  let angularPage: AngularPage;
  let legacy: PageDriver;
  let angular: PageDriver;
  let legacyServer: FakeGempServer;
  let angularServer: FakeGempServer;

  beforeEach(async () => {
    legacyServer = new FakeGempServer();
    legacyPage = await openLegacyPage(legacyServer);
    legacy = driveLegacy(legacyPage);
    vi.useFakeTimers();
    angularServer = new FakeGempServer();
    angularPage = await openAngularPage(angularServer);
    angular = driveAngular(angularPage);
  });

  afterEach(() => {
    angularPage.close();
    vi.useRealTimers();
    legacyPage.close();
  });

  const SELECTS = [
    'format',
    'collection',
    'side',
    'product',
    'rarity',
    'keyword',
    'phase',
    'race',
    'itemClass',
    'signet',
    'block',
    'set',
    'sort',
    'cardType',
    'siteNumberCompare',
    'vitalityCompare',
    'resistanceCompare',
    'twilightCompare',
    'strengthCompare',
  ];

  it.each(SELECTS)('the %s list has the same options, and the same one chosen', (control) => {
    expect(angular.options(control)).toEqual(legacy.options(control));
    expect(angular.chosen(control)).toEqual(legacy.chosen(control));
  });

  it('has the same page size slider: its smallest and largest size, and where it starts', () => {
    const legacySlider = (option: string) =>
      (legacyPage.window as unknown as { $: (s: string) => { slider(a: string, b: string): number } })
        .$('#countSlider')
        .slider('option', option);
    const angularSlider = angularPage.element.querySelector<HTMLInputElement>('.gd-pager__size')!;
    expect([Number(angularSlider.min), Number(angularSlider.max), Number(angularSlider.value)]).toEqual([
      legacySlider('min'),
      legacySlider('max'),
      legacySlider('value'),
    ]);
  });

  it('shows the same deck name and the same number of cards', () => {
    expect(angular.text('deckName')).toEqual(legacy.text('deckName'));
    expect(angular.count('collectionCards')).toEqual(legacy.count('collectionCards'));
  });
});
