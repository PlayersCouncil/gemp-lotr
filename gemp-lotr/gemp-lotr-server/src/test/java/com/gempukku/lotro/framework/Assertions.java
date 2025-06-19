package com.gempukku.lotro.framework;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCardImpl;

import static org.junit.Assert.*;

public class Assertions {

	/**
	 * Asserts that one or more cards are all contained within the provided zone.  If any are not there, an AssertionError
	 * will be thrown with no message.
	 * @param zone The zone to check.
	 * @param cards One or more cards which must all be in that zone.
	 */
	public static void assertInZone(Zone zone, PhysicalCardImpl...cards) {
		for(var card : cards) {
			assertEquals(zone, card.getZone());
		}
	}

	/**
	 * Asserts that one or more cards are all in either player's hand.  If any are in any other zone, an AssertionError
	 * will be thrown with no message.
	 * @param cards One or more cards which must all be in hand.
	 */
	public static void assertInHand(PhysicalCardImpl...cards) { assertInZone(Zone.HAND, cards); }

	/**
	 * Asserts that a given card is currently attached to another.  Checks both that the current zone of the card is
	 * correct, and also that it currently records the bearer as its attachment point.
	 * @param card The card which may or may not be attached
	 * @param bearer The card which supposedly bears the other card
	 */
	public static void assertAttachedTo(PhysicalCardImpl card, PhysicalCardImpl bearer) {
		assertEquals(card.getZone(), Zone.ATTACHED);
		assertSame(bearer, card.getAttachedTo());
	}

	/**
	 * Asserts that a given card is currently not attached to another.  Checks that it currently records
	 * the bearer as its attachment point.
	 * @param card The card which may or may not be attached
	 * @param bearer The card which supposedly bears the other card
	 */
	public static void assertNotAttachedTo(PhysicalCardImpl card, PhysicalCardImpl bearer) {
		assertNotSame(bearer, card.getAttachedTo());
	}
}
