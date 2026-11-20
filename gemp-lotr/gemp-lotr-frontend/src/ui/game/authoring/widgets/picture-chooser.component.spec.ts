import { Component, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Subject } from 'rxjs';
import { AuthoringApi } from '../../../../core/game/authoring/authoring-api.service';
import { Authoring } from '../../../../core/game/authoring/authoring.service';
import { PictureChooserComponent, pictureZoom } from './picture-chooser.component';

interface Images {
  images: readonly { name: string; url: string }[];
}

@Component({
  imports: [PictureChooserComponent],
  template: `
    @if (shown()) {
      <gd-author-pictures [value]="value()" (chosen)="chosen.push($event)" (closed)="closed = closed + 1" />
    }
  `,
})
class HostComponent {
  readonly shown = signal(true);
  readonly value = signal('');
  readonly chosen: string[] = [];
  closed = 0;
}

describe('PictureChooserComponent', () => {
  let fixture: ComponentFixture<HostComponent>;
  let host: HostComponent;
  let el: HTMLElement;
  let answer: (images: Images | null) => void;
  const images = new Subject<Images>();
  const authoring = {
    send: vi.fn((source: unknown, change: boolean) => {
      void source;
      void change;
      return new Promise<Images | null>((resolve) => (answer = resolve));
    }),
  };

  beforeEach(async () => {
    authoring.send.mockClear();
    TestBed.configureTestingModule({
      providers: [
        { provide: Authoring, useValue: authoring },
        { provide: AuthoringApi, useValue: { images: () => images } },
      ],
    });
    fixture = TestBed.createComponent(HostComponent);
    host = fixture.componentInstance;
    el = fixture.nativeElement as HTMLElement;
    await render();
  });

  async function render() {
    fixture.detectChanges();
    await fixture.whenStable();
  }

  async function answered(value: Images | null) {
    answer(value);
    await Promise.resolve();
    await render();
  }

  const state = () => el.querySelector('.gd-author-pictures')?.getAttribute('data-state');
  const items = () => [...el.querySelectorAll<HTMLButtonElement>('button.gd-author-pictures__item')];

  it('asks the server for its pictures, as a read, and says it is looking', () => {
    expect(authoring.send).toHaveBeenCalledWith(images, false);
    expect(state()).toBe('loading');
    expect(el.textContent).toContain('Looking in images/scenarios');
  });

  it('lists the pictures, marks the one shown large, and chooses one', async () => {
    host.value.set(' image:b.png ');
    await answered({
      images: [
        { name: 'a.png', url: '/gemp-lotr/images/scenarios/a.png' },
        { name: 'b.png', url: '/gemp-lotr/images/scenarios/b.png' },
      ],
    });
    expect(state()).toBe('shown');
    expect(items().map((item) => item.getAttribute('data-name'))).toEqual(['a.png', 'b.png']);
    expect(items().map((item) => item.getAttribute('aria-pressed'))).toEqual(['false', 'true']);
    expect(items()[0]?.title).toBe('image:a.png');
    expect(items()[0]?.querySelector('img')?.getAttribute('src')).toBe('/gemp-lotr/images/scenarios/a.png');
    expect(items()[0]?.querySelector('.gd-author-pictures__name')?.textContent).toBe('a.png');
    items()[0]!.click();
    expect(host.chosen).toEqual(['image:a.png']);
    expect(host.closed).toBe(1);
  });

  it('says when there are none, or they could not be listed', async () => {
    await answered({ images: [] });
    expect(state()).toBe('none');
    expect(el.querySelector('.gd-author-note')?.getAttribute('title')).toContain('images/scenarios');
    fixture = TestBed.createComponent(HostComponent);
    el = fixture.nativeElement as HTMLElement;
    await render();
    await answered(null);
    expect(state()).toBe('failed');
  });

  it('closes on Escape', async () => {
    const escape = new KeyboardEvent('keydown', { key: 'Escape', bubbles: true, cancelable: true });
    el.querySelector('gd-author-pictures')!.dispatchEvent(escape);
    expect(host.closed).toBe(1);
    expect(escape.defaultPrevented).toBe(true);
  });

  it('takes no answer that comes after it has gone', async () => {
    host.shown.set(false);
    await render();
    await answered({ images: [{ name: 'a.png', url: 'a' }] });
    expect(el.querySelector('gd-author-pictures')).toBeNull();
  });

  it('writes a picture as Shows large has it', () => {
    expect(pictureZoom('map.png')).toBe('image:map.png');
  });
});
