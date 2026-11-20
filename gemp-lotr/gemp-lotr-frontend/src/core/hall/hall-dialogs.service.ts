import { Injectable, signal } from '@angular/core';
import { MarkupNode, parseServerMarkup } from '../chat/chat-markup';

/** One of the hall's message dialogs (hallUi.js showDialog): a title, its text as markup data, and an OK button. */
export interface HallDialog {
  /** Unique per dialog shown, for the view's tracking and for closing it. */
  readonly id: number;
  readonly title: string;
  readonly body: readonly MarkupNode[];
}

/** HTML-escapes text (GempLotrHallUI.escapeHtml / TableFlow.escapeHtml), for building markup around server values. */
export function escapeHtml(value: string | null | undefined): string {
  return (value ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

/**
 * Server text (an <error>/<response> message, a tournament announcement) as markup (GempLotrHallUI.serverMarkup):
 * escaped, so anything in it shows as the text it is, and then only the bare formatting tags the server itself writes
 * (<b>, <strong>, <i>, <em>, <u> and <br>, with no attributes) turned back into tags.
 */
export function serverMessageMarkup(text: string | null | undefined): MarkupNode[] {
  return parseServerMarkup(serverMessageHtml(text));
}

/** serverMessageMarkup's HTML, for the chat's warning lines (which hold markup). */
export function serverMessageHtml(text: string | null | undefined): string {
  if (text === null || text === undefined) {
    return '';
  }
  return escapeHtml(text)
    .replace(
      /&lt;(\/?)(b|strong|i|em|u)&gt;/gi,
      (_match, slash: string, tag: string) => `<${slash}${tag.toLowerCase()}>`,
    )
    .replace(/&lt;br\s*\/?&gt;/gi, '<br>');
}

/** Plain text as markup, its line breaks kept (TableFlow.showDialog). */
export function plainTextMarkup(text: string): MarkupNode[] {
  return parseServerMarkup(escapeHtml(text).replace(/\n/g, '<br/>'));
}

/** Markup the page builds itself (server values in it escaped with escapeHtml). */
export function pageMarkup(html: string): MarkupNode[] {
  return parseServerMarkup(html);
}

/**
 * The hall's message dialogs (hallUi.js showDialog, used for "Error" / "Response", "Joined Tournament", "Tournament
 * Created", "Tournament Update", "Here There Be Dragons", "<flow> failed", ...). Each one is shown until its OK; several
 * can be open at once, the newest on top, as the legacy jQuery dialogs were.
 */
@Injectable()
export class HallDialogs {
  private nextId = 1;
  private readonly open_ = signal<readonly HallDialog[]>([]);
  /** The dialogs showing, oldest first (the last one is on top). */
  readonly open = this.open_.asReadonly();

  show(title: string, body: readonly MarkupNode[]): HallDialog {
    const dialog: HallDialog = { id: this.nextId++, title, body };
    this.open_.update((dialogs) => [...dialogs, dialog]);
    return dialog;
  }

  /** A dialog of plain text (its line breaks kept). */
  showText(title: string, text: string): HallDialog {
    return this.show(title, plainTextMarkup(text));
  }

  close(id: number): void {
    this.open_.update((dialogs) => dialogs.filter((dialog) => dialog.id !== id));
  }
}
