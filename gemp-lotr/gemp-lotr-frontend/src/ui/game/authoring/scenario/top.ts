import { AuthoringResult } from '../../../../core/game/authoring/authoring-state';
import { Authoring } from '../../../../core/game/authoring/authoring.service';

/**
 * Changes one top-level part of the draft (null takes it out), as every control of the Scenario tab does: the change
 * goes into the game in hand if it can (the title, the summary...), and otherwise brings a fresh game.
 */
export function sendTop(authoring: Authoring, name: string, value: unknown): Promise<AuthoringResult | null> {
  return authoring.edit({ top: { [name]: value } });
}
