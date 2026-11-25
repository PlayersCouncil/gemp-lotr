import { inject, Injectable, signal } from '@angular/core';
import { Observable, shareReplay, tap } from 'rxjs';
import { GempHttp } from '../api/gemp-http.service';

/**
 * Who is logged in (GET /player), asked once per page: the login guard asks it to tell whether anyone is, and the
 * page may want the name (the chat marks messages that mention the player).
 */
@Injectable({ providedIn: 'root' })
export class PlayerInfo {
  private readonly http = inject(GempHttp);
  private request: Observable<string> | null = null;

  /** The player's name, once the server has answered; null before, and if it didn't say. */
  readonly name = signal<string | null>(null);

  /** The server's answer, as text; asked once, shared by everyone who asks. */
  load(): Observable<string> {
    this.request ??= this.http.getText('/player').pipe(
      tap((text) => this.name.set(nameIn(text))),
      shareReplay(1),
    );
    return this.request;
  }
}

function nameIn(text: string): string | null {
  try {
    const info = JSON.parse(text) as { name?: unknown };
    return typeof info.name === 'string' && info.name !== '' ? info.name : null;
  } catch {
    return null;
  }
}
