import { TestBed } from '@angular/core/testing';
import { BoundFinder } from '../../table-edit/bound-finder';
import { FindBoundComponent } from './find-bound.component';

describe('FindBoundComponent', () => {
  function render(finder: boolean) {
    TestBed.configureTestingModule({ providers: finder ? [BoundFinder] : [] });
    const fixture = TestBed.createComponent(FindBoundComponent);
    fixture.componentRef.setInput('part', 'ally');
    fixture.componentRef.setInput('what', 'bound of ally');
    fixture.detectChanges();
    return { fixture, el: fixture.nativeElement as HTMLElement };
  }

  it('opens the card search for its field, and hands on the filter used there', () => {
    const { fixture, el } = render(true);
    const used: string[] = [];
    fixture.componentInstance.used.subscribe((filter) => used.push(filter));
    const button = el.querySelector<HTMLButtonElement>('button[data-action="find"]')!;
    expect(button.dataset['for']).toBe('ally');
    expect(button.title).toBe('Find the filter with the card search: bound of ally');
    button.click();
    const finder = TestBed.inject(BoundFinder);
    expect(finder.finding()?.what).toBe('bound of ally');
    finder.use('culture:GONDOR cardType:ALLY');
    expect(used).toEqual(['culture:GONDOR cardType:ALLY']);
  });

  it('is not offered on a page without the card search', () => {
    const { el } = render(false);
    expect(el.querySelector('button')).toBeNull();
  });
});
