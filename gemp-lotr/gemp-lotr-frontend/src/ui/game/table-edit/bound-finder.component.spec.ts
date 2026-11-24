import { TestBed } from '@angular/core/testing';
import { BoundFinder } from './bound-finder';
import { BoundFinderComponent } from './bound-finder.component';

describe('BoundFinderComponent', () => {
  it('opens the search window while a filter is being found, titled by what it fills, with Use as bound', () => {
    TestBed.configureTestingModule({ providers: [BoundFinder] });
    TestBed.overrideComponent(BoundFinderComponent, { set: { imports: [], template: '<i>{{ heading() }}</i>' } });
    const finder = TestBed.inject(BoundFinder);
    const fixture = TestBed.createComponent(BoundFinderComponent);
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    expect(el.textContent).toBe('Find: ');
    finder.open('bound of ally', () => undefined);
    fixture.detectChanges();
    expect(el.textContent).toBe('Find: bound of ally');
  });
});
