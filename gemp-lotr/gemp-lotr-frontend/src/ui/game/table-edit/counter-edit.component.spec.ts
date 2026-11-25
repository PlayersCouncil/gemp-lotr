import { TestBed } from '@angular/core/testing';
import { CounterEditComponent } from './counter-edit.component';
import { CounterPopups } from './counter-popups';

describe('CounterEditComponent', () => {
  it("shows the number as the host has it, as a link that opens and closes the counter's box by it", () => {
    TestBed.configureTestingModule({ providers: [CounterPopups] });
    const popups = TestBed.inject(CounterPopups);
    const fixture = TestBed.createComponent(CounterEditComponent);
    fixture.componentRef.setInput('counter', 'threats');
    fixture.componentRef.setInput('shown', '0/1');
    fixture.detectChanges();
    const opener = (fixture.nativeElement as HTMLElement).querySelector<HTMLButtonElement>('button.gd-edit-counter')!;
    expect(opener.textContent.trim()).toBe('0/1');
    expect(opener.dataset['editCounter']).toBe('threats');
    expect(opener.title).toBe('Change Threats');
    expect(opener.getAttribute('aria-expanded')).toBe('false');
    opener.click();
    fixture.detectChanges();
    expect(popups.open()).toMatchObject({ counter: 'threats', element: opener });
    expect(opener.getAttribute('aria-expanded')).toBe('true');
    // Another counter's box open: not this one's
    popups.toggle('twilight', opener);
    fixture.detectChanges();
    expect(opener.getAttribute('aria-expanded')).toBe('false');
    popups.toggle('twilight', opener);
    expect(popups.open()).toBeNull();
    opener.click();
    opener.click();
    expect(popups.open()).toBeNull();
  });
});
