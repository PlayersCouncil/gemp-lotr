/**
 * "Say only if" (a message's `require`, B6), as both the Step tab's messages and the Scenario tab's closing messages
 * offer it: its hint, and a placeholder the server takes as it is typed (JSON on one line is HJSON too; HJSON's own
 * way needs a line to each part).
 */
export const SAY_IF_HINT =
  'Said only while these hold: requirements as cards write them, in HJSON as the file has them (one { ... } or a [ list ], all of which must hold), as the When’s Only while. ' +
  '"You" is the person playing. A line to each part: type: cantSpot, then filter: name(Ulaire Enquea). Not on a message that ends in a choice.';
export const SAY_IF_PLACEHOLDER = '{ "type": "canSpot", "filter": "name(Aragorn)" }';
