import { HttpErrorResponse } from '@angular/common/http';
import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Subject } from 'rxjs';
import { EventDrawer } from '../../../core/hall/events/event-drawer';
import { EventRowComponent } from './event-row.component';

@Component({
  imports: [EventRowComponent],
  template: `<gd-event-row
    kind="tournament"
    key="t1"
    title="A tournament"
    [drawer]="drawer"
    [goLabel]="go()"
    (go)="went = went + 1"
  />`,
})
class Host {
  readonly requests = new Subject<string | null>();
  readonly go = signal<string | null>(null);
  went = 0;
  readonly drawer = new EventDrawer<string>({
    load: () => this.requests,
    failure: (error) => `Could not load the tournament (HTTP ${error.status}).`,
    unreadableText: 'Could not display the tournament.',
    emptyText: 'No details are available for this tournament.',
  });
}

/** EventDrawer's row: a failed fetch says why in the drawer, an empty answer says there is nothing; a fixed button. */
describe('EventRowComponent', () => {
  function render() {
    const fixture = TestBed.createComponent(Host);
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    const toggle = () => {
      el.querySelector<HTMLButtonElement>('.gd-event-row__toggle')!.click();
      fixture.detectChanges();
    };
    return { fixture, el, toggle, host: fixture.componentInstance };
  }

  it('says why the details could not be loaded', () => {
    const { fixture, el, toggle, host } = render();
    toggle();
    host.requests.error(new HttpErrorResponse({ status: 500, statusText: 'Server Error' }));
    fixture.detectChanges();
    expect(el.querySelector('.gd-event-row__drawer')?.getAttribute('data-state')).toBe('error');
    expect(el.querySelector('.gd-event-row__error')?.textContent).toBe('Could not load the tournament (HTTP 500).');
  });

  it('says when there are no details to show', () => {
    const { fixture, el, toggle, host } = render();
    toggle();
    host.requests.next(null);
    fixture.detectChanges();
    expect(el.querySelector('.gd-event-row__empty')?.textContent).toBe('No details are available for this tournament.');
  });

  it('gives its fixed button to the page', () => {
    const { fixture, el, host } = render();
    host.go.set('Go to Tournament');
    fixture.detectChanges();
    el.querySelector<HTMLButtonElement>('.gd-event-row__go')!.click();
    expect(host.went).toBe(1);
  });
});
