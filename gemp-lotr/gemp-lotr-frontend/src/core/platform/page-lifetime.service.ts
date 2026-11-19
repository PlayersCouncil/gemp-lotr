import { DestroyRef, inject, Injectable } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Observable, Observer } from 'rxjs';

type Handlers<T> = Partial<Observer<T>>;

/**
 * What is still running when the page closes. A read is cancelled: nobody is waiting for it. A write is left to finish,
 * so that leaving doesn't abort saving a deck, but its answer no longer does anything.
 */
@Injectable()
export class PageLifetime {
  private readonly destroyRef = inject(DestroyRef);
  private open = true;

  constructor() {
    this.destroyRef.onDestroy(() => (this.open = false));
  }

  read<T>(source: Observable<T>, handlers: Handlers<T>): void {
    if (this.open) {
      source.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(handlers);
    }
  }

  write<T>(source: Observable<T>, handlers: Handlers<T>): void {
    source.subscribe({
      next: (value) => {
        if (this.open) handlers.next?.(value);
      },
      error: (error: unknown) => {
        if (this.open) handlers.error?.(error);
      },
    });
  }

  later(action: () => void, delayMs: number): void {
    const timer = setTimeout(action, delayMs);
    this.destroyRef.onDestroy(() => clearTimeout(timer));
  }
}
