import { ConfirmWords, Notifier } from '../../../../core/notifier/notifier';

/**
 * Asks the author before something that can't be undone, in the page's own dialog (the game page's Notifier): the
 * question, and what its OK button does (`ok`, short: "Delete", "Take them out"). Resolves true for OK.
 */
export function askAuthor(
  notifier: Notifier,
  question: string,
  ok: string,
  words: ConfirmWords = {},
): Promise<boolean> {
  return notifier.confirm(question, { title: 'Please confirm', cancel: 'Cancel', ...words, ok });
}
