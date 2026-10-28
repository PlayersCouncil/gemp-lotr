package com.gempukku.lotro.cards.build;

/**
 * A requirement that a limit on uses has not been reached ("limit once per phase / per turn / per site").  It is
 * told apart from other requirements because it alone is asked again when a required trigger comes to be carried
 * out: two triggers of one card raised by one event (two companions exerting at once) both pass the limit when
 * they are raised, and the second must not be carried out once the first has used the limit up (F-0145).
 */
public interface LimitRequirement extends Requirement {
}
