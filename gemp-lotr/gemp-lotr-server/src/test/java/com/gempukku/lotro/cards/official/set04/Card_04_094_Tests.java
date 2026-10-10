package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_094_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "4_94");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetShadowConditionScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hearken", "4_94");
					put("theoden", "4_292");    // Théoden, Son of Thengel: a regroup ability only
					put("gimli", "1_13");       // Gimli, Son of Glóin: a companion who is no Man; a skirmish ability only
					// Weary: [isengard] condition, cost 1, "Plays on a companion or ally. Limit 1 per
					// character."; its trigger needs an [isengard] tracker
					put("weary", "4_212");
					// Banished: [isengard] condition, cost 0, "To play, exert Saruman or an [isengard] Man.
					// Plays on a Free Peoples Man."; its trigger answers a lost skirmish
					put("banished", "4_139");
					put("grima", "4_154");      // Gríma, Wormtongue: [isengard] Man, vitality 3, for Banished to exert; a maneuver ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HearkentoMeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Hearken to Me
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: 
		 * Strength: 2
		 * Vitality: 1
		 * Game Text: <b>Spell</b>. To play, spot Gandalf. Bearer must be Théoden.<br>Discard all Shadow conditions on Théoden. Shadow conditions may not be played on Théoden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Hearken to Me", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void AShadowConditionCannotBePlayedOnTheodenWhileHeBearsItAndCanBePlayedOnAnotherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowConditionScenario();

		var hearken = scn.GetFreepsCard("hearken");
		var theoden = scn.GetFreepsCard("theoden");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		var weary = scn.GetShadowCard("weary");
		var banished = scn.GetShadowCard("banished");
		var grima = scn.GetShadowCard("grima");
		scn.MoveCompanionsToTable(theoden, gimli);
		scn.MoveCardsToHand(weary, banished);

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		scn.FreepsPass();

		// Théoden is the only Free Peoples Man on the table, so he is the one bearer Banished could have.
		// Without Hearken to Me it may be played.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.HAND, weary, banished);
		assertTrue(scn.ShadowPlayAvailable(banished));
		assertTrue(scn.ShadowPlayAvailable(weary));

		scn.AttachCardsTo(theoden, hearken);
		scn.ShadowRefreshDecision();
		// With it, Banished has nobody to go on and is not offered at all.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(banished));
		assertTrue(scn.ShadowPlayAvailable(weary));

		// Weary goes on any companion: the other two are offered and Théoden is not.
		scn.ShadowPlayCard(weary);
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, frodo));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(theoden));

		scn.ShadowChooseCard(gimli);
		assertAttachedTo(weary, gimli);
		assertAttachedTo(hearken, theoden);
		assertEquals(0, scn.GetWoundsOn(grima));
		assertFalse(scn.ShadowPlayAvailable(banished));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
