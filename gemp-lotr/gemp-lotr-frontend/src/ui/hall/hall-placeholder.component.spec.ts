import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { HallPlaceholderComponent } from './hall-placeholder.component';
import { HALL_TAB_DATA } from './hall-route-data';

describe('HallPlaceholderComponent', () => {
  async function open(url: string) {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([
          {
            path: 'help',
            data: { [HALL_TAB_DATA]: 'help' },
            children: [{ path: '**', component: HallPlaceholderComponent }],
          },
          { path: 'hall', data: { [HALL_TAB_DATA]: 'game-hall' }, component: HallPlaceholderComponent },
          { path: 'none', component: HallPlaceholderComponent },
        ]),
      ],
    });
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl(url);
    return harness.routeNativeElement as HTMLElement;
  }

  it("stands in for its route's tab, with the tab's sub-tabs (the tab named on a parent route too)", async () => {
    const el = await open('/help/formats');
    expect([el.dataset['tab'], el.dataset['part']]).toEqual(['help', '4.3']);
    expect(el.querySelector('gd-hall-sub-tabs')).not.toBeNull();
    expect(el.querySelectorAll('.gd-hall-sub-tabs__tab')).toHaveLength(5);
    const content = el.querySelector('.gd-hall-placeholder__content');
    expect([content?.getAttribute('role'), content?.getAttribute('aria-label'), content?.textContent]).toEqual([
      'region',
      'Help — coming in 4.3',
      '',
    ]);
  });

  it('has no sub-tabs for a tab without them', async () => {
    const el = await open('/hall');
    expect(el.querySelector('gd-hall-sub-tabs')).toBeNull();
    expect(el.querySelector('.gd-hall-placeholder__content')?.getAttribute('aria-label')).toBe(
      'Tables — coming in 4.2',
    );
  });

  it('needs its route to name its tab', async () => {
    await expect(open('/none')).rejects.toThrow(/data.hallTab/);
  });
});
