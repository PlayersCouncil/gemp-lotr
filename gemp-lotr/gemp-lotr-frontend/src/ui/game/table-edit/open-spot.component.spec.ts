import { TestBed } from '@angular/core/testing';
import { EditPopups } from './edit-popups';
import { OpenSpotComponent } from './open-spot.component';

describe('OpenSpotComponent', () => {
  it('is a + button for its place that opens and closes the chooser by it', () => {
    TestBed.configureTestingModule({ providers: [EditPopups] });
    const popups = TestBed.inject(EditPopups);
    const fixture = TestBed.createComponent(OpenSpotComponent);
    fixture.componentRef.setInput('where', { seat: 'shadow', zone: 'minions' });
    fixture.detectChanges();
    const button = (fixture.nativeElement as HTMLElement).querySelector<HTMLButtonElement>('button.gd-open-spot')!;
    expect(button.dataset).toMatchObject({ seat: 'shadow', zone: 'minions' });
    expect(button.getAttribute('aria-label')).toBe('Add to Minions');
    expect(button.getAttribute('aria-expanded')).toBe('false');
    button.click();
    fixture.detectChanges();
    expect(popups.chooser()?.place).toEqual({ seat: 'shadow', zone: 'minions' });
    expect(popups.chooser()?.element).toBe(button);
    expect(button.getAttribute('aria-expanded')).toBe('true');
    button.click();
    expect(popups.chooser()).toBeNull();
  });
});
