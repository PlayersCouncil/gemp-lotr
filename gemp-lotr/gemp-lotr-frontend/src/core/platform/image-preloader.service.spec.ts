import { DOCUMENT } from '@angular/common';
import { TestBed } from '@angular/core/testing';
import { ImagePreloader } from './image-preloader.service';

/** A stand-in for the browser's Image: decode() settles when the test says so. */
class FakeImage {
  static loads = new Map<string, { resolve: () => void; reject: () => void }>();
  src = '';
  decode(): Promise<void> {
    return new Promise((resolve, reject) => FakeImage.loads.set(this.src, { resolve, reject }));
  }
}

describe('ImagePreloader', () => {
  let preloader: ImagePreloader;

  beforeEach(() => {
    vi.useFakeTimers();
    FakeImage.loads.clear();
    TestBed.configureTestingModule({
      providers: [{ provide: DOCUMENT, useValue: { defaultView: { Image: FakeImage } } }],
    });
    preloader = TestBed.inject(ImagePreloader);
  });

  afterEach(() => vi.useRealTimers());

  const settled = async (promise: Promise<void>) => {
    let done = false;
    void promise.then(() => (done = true));
    await vi.advanceTimersByTimeAsync(0);
    return done;
  };

  it('resolves once every image is loaded, each loaded once', async () => {
    const loading = preloader.load(['a.jpg', 'b.jpg', 'a.jpg']);
    expect([...FakeImage.loads.keys()]).toEqual(['a.jpg', 'b.jpg']);
    FakeImage.loads.get('a.jpg')!.resolve();
    expect(await settled(loading)).toBe(false);
    FakeImage.loads.get('b.jpg')!.resolve();
    expect(await settled(loading)).toBe(true);
  });

  it('does not wait for a broken image', async () => {
    const loading = preloader.load(['broken.jpg']);
    FakeImage.loads.get('broken.jpg')!.reject();
    expect(await settled(loading)).toBe(true);
  });

  it('gives up waiting after the timeout', async () => {
    const loading = preloader.load(['slow.jpg'], 3000);
    await vi.advanceTimersByTimeAsync(2999);
    expect(await settled(loading)).toBe(false);
    await vi.advanceTimersByTimeAsync(1);
    expect(await settled(loading)).toBe(true);
  });
});
