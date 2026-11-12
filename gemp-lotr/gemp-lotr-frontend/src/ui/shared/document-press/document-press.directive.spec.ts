import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { DocumentPressDirective } from './document-press.directive';

@Component({
  imports: [DocumentPressDirective],
  template: `@if (shown()) {
    <div class="target" (gdDocumentPress)="presses.push($event.type)"></div>
  }`,
})
class Host {
  readonly shown = signal(true);
  readonly presses: string[] = [];
}

describe('DocumentPressDirective', () => {
  let outside: HTMLElement;

  beforeEach(() => {
    outside = document.body.appendChild(document.createElement('button'));
  });
  afterEach(() => outside.remove());

  async function render() {
    const fixture = TestBed.createComponent(Host);
    await fixture.whenStable();
    return { fixture, host: fixture.componentInstance };
  }

  const press = (type: string) => outside.dispatchEvent(new MouseEvent(type, { bubbles: true, cancelable: true }));

  it('reports mouse presses, clicks and right-clicks anywhere on the page', async () => {
    const { host } = await render();
    press('mousedown');
    press('click');
    press('contextmenu');
    expect(host.presses).toEqual(['mousedown', 'click', 'contextmenu']);
  });

  it('hears about a press before the element that was pressed', async () => {
    const { host } = await render();
    let seenFirst: string[] | null = null;
    outside.addEventListener('click', () => (seenFirst = [...host.presses]));
    press('click');
    expect(seenFirst).toEqual(['click']);
  });

  it('stops listening when its element goes away', async () => {
    const { fixture, host } = await render();
    host.shown.set(false);
    await fixture.whenStable();
    press('click');
    expect(host.presses).toEqual([]);
  });
});
