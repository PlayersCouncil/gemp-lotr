import { TestBed } from '@angular/core/testing';
import { Observable, Subject } from 'rxjs';
import { PageLifetime } from './page-lifetime.service';

describe('PageLifetime', () => {
  let lifetime: PageLifetime;
  const closePage = () => TestBed.resetTestingModule();

  beforeEach(() => {
    vi.useFakeTimers({ toFake: ['setTimeout', 'clearTimeout'] });
    TestBed.configureTestingModule({ providers: [PageLifetime] });
    lifetime = TestBed.inject(PageLifetime);
  });

  afterEach(() => vi.useRealTimers());

  describe('a read', () => {
    it('delivers its answer while the page is open', () => {
      const source = new Subject<number>();
      const next = vi.fn();
      lifetime.read(source, { next });
      source.next(1);
      expect(next).toHaveBeenCalledWith(1);
    });

    it('is cancelled when the page closes', () => {
      let cancelled = false;
      const source = new Observable<number>(() => () => (cancelled = true));
      lifetime.read(source, { next: vi.fn() });
      closePage();
      expect(cancelled).toBe(true);
    });

    it('does not start once the page has closed', () => {
      const subscribed = vi.fn();
      closePage();
      lifetime.read(new Observable<number>(subscribed), { next: vi.fn() });
      expect(subscribed).not.toHaveBeenCalled();
    });
  });

  describe('a write', () => {
    it('delivers its answer while the page is open', () => {
      const source = new Subject<number>();
      const next = vi.fn();
      lifetime.write(source, { next });
      source.next(1);
      expect(next).toHaveBeenCalledWith(1);
    });

    it('is not cancelled when the page closes, so the server gets to finish it', () => {
      let cancelled = false;
      const source = new Observable<number>(() => () => (cancelled = true));
      lifetime.write(source, { next: vi.fn() });
      closePage();
      expect(cancelled).toBe(false);
    });

    it('ignores the answer and the failure after the page has closed', () => {
      const answered = new Subject<number>();
      const failed = new Subject<number>();
      const next = vi.fn();
      const error = vi.fn();
      lifetime.write(answered, { next, error });
      lifetime.write(failed, { next, error });
      closePage();
      answered.next(1);
      failed.error(new Error('late'));
      expect(next).not.toHaveBeenCalled();
      expect(error).not.toHaveBeenCalled();
    });

    it('still starts after the page has closed', () => {
      const subscribed = vi.fn();
      closePage();
      lifetime.write(new Observable<number>(subscribed), { next: vi.fn() });
      expect(subscribed).toHaveBeenCalledOnce();
    });
  });

  describe('something for later', () => {
    it('runs after the delay', () => {
      const action = vi.fn();
      lifetime.later(action, 100);
      vi.advanceTimersByTime(99);
      expect(action).not.toHaveBeenCalled();
      vi.advanceTimersByTime(1);
      expect(action).toHaveBeenCalledOnce();
    });

    it('does not run once the page has closed', () => {
      const action = vi.fn();
      lifetime.later(action, 100);
      closePage();
      vi.advanceTimersByTime(100);
      expect(action).not.toHaveBeenCalled();
    });
  });
});
