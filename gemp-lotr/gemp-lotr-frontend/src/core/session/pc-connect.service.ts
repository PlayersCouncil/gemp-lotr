import { inject, Injectable, signal } from '@angular/core';
import { PcSessionService } from './pc-session.service';

/**
 * My Account's Connect PC Account form, for a password login not linked (one per block: provided by the component):
 * open or not, its password, its last refusal, and whether its call is under way. Connecting asks for the account's
 * password, so a browser that is merely logged in can't give the account away; once accepted the page leaves for the
 * PC sign-in (PcSessionService.connect).
 */
@Injectable()
export class PcConnect {
  private readonly pc = inject(PcSessionService);
  private readonly open_ = signal(false);
  private readonly password_ = signal('');
  private readonly error_ = signal<string | null>(null);
  private readonly busy_ = signal(false);

  readonly open = this.open_.asReadonly();
  readonly password = this.password_.asReadonly();
  readonly error = this.error_.asReadonly();
  readonly busy = this.busy_.asReadonly();

  /** Connect PC Account: the form. */
  start(): void {
    this.open_.set(true);
    this.error_.set(null);
  }

  cancel(): void {
    this.open_.set(false);
    this.password_.set('');
    this.error_.set(null);
  }

  setPassword(value: string): void {
    this.password_.set(value);
  }

  async submit(): Promise<void> {
    if (this.busy_()) return;
    this.error_.set(null);
    this.busy_.set(true);
    try {
      const refusal = await this.pc.connect(this.password_());
      if (refusal) {
        this.error_.set(refusal.message);
        this.password_.set('');
      }
    } finally {
      this.busy_.set(false);
    }
  }
}
