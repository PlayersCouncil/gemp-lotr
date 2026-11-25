import { TestBed } from '@angular/core/testing';
import { PathPopups } from './path-popups';
import { SiteSpotComponent } from './site-spot.component';

describe('SiteSpotComponent', () => {
  it("is a site-shaped + for its number that opens and closes the site chooser by it (not a site's Replace…)", () => {
    TestBed.configureTestingModule({ providers: [PathPopups] });
    const popups = TestBed.inject(PathPopups);
    const fixture = TestBed.createComponent(SiteSpotComponent);
    fixture.componentRef.setInput('number', 3);
    fixture.detectChanges();
    const button = (fixture.nativeElement as HTMLElement).querySelector<HTMLButtonElement>('button.gd-open-spot')!;
    expect(button.dataset).toMatchObject({ shape: 'site', site: '3' });
    expect(button.getAttribute('aria-label')).toBe('Add site 3');
    expect(button.querySelector('.gd-open-spot__number')?.textContent).toBe('3');
    expect(button.getAttribute('aria-expanded')).toBe('false');
    button.click();
    fixture.detectChanges();
    expect(popups.chooser()).toMatchObject({ number: 3, element: button, replacing: false });
    expect(button.getAttribute('aria-expanded')).toBe('true');
    button.click();
    expect(popups.chooser()).toBeNull();
    // A site's Replace… at the same number is not this spot's chooser
    popups.openChooserAt(3, { left: 0, top: 0, right: 0, bottom: 0 }, null, true);
    fixture.detectChanges();
    expect(button.getAttribute('aria-expanded')).toBe('false');
  });
});
