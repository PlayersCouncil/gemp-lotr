package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_05_036_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("kog", "5_36");
					put("kog2", "5_36");        // a second Knight of Gondor: the card is not unique
					// Ranger of Osgiliath: [gondor] Man and ranger; a regroup ability, never used
					put("ranger", "7_116");
					// First Level: a [gondor] condition in the support area; a maneuver ability, never used
					put("level", "7_92");
					// No Stranger to the Shadows: a [gondor] condition borne by a ranger; each site's Shadow number -1
					put("stranger", "1_108");
					// Coat of Mail: a [gondor] possession on a Man, which is no condition
					put("coat", "1_101");
					// The Last Alliance of Elves and Men: an [elven] condition on a [gondor] Man; no Elf is in play
					put("alliance", "1_49");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void KnightofGondorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Knight of Gondor
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Knight</b>.<br><b>Fellowship:</b> Discard a [gondor] condition to heal this companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("kog");

		assertEquals("Knight of Gondor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.KNIGHT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void FellowshipActionDiscardsAGondorConditionToHealThisCompanionOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kog = scn.GetFreepsCard("kog");
		var kog2 = scn.GetFreepsCard("kog2");
		var ranger = scn.GetFreepsCard("ranger");
		var level = scn.GetFreepsCard("level");
		var stranger = scn.GetFreepsCard("stranger");
		var coat = scn.GetFreepsCard("coat");
		var alliance = scn.GetFreepsCard("alliance");
		scn.MoveCompanionsToTable(kog, kog2, ranger);
		scn.MoveCardsToSupportArea(level);
		scn.AttachCardsTo(ranger, stranger);
		scn.AttachCardsTo(kog, coat);
		scn.AttachCardsTo(kog2, alliance);
		// Two wounds on this Knight, so that one heal leaves one; one on the other copy, which is not to be healed.
		scn.AddWoundsToChar(kog, 2);
		scn.AddWoundsToChar(kog2, 1);

		scn.StartGame();

		assertEquals(2, scn.GetWoundsOn(kog));
		assertEquals(1, scn.GetWoundsOn(kog2));
		assertInZone(Zone.SUPPORT, level);
		assertAttachedTo(stranger, ranger);
		assertAttachedTo(coat, kog);
		assertAttachedTo(alliance, kog2);
		assertTrue(scn.FreepsActionAvailable(kog));

		scn.FreepsUseCardAction(kog);
		// A [gondor] condition in the support area and one borne are offered; a [gondor] possession and an
		// [elven] condition are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(level, stranger));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(coat, alliance));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(stranger);

		assertInDiscard(stranger);
		assertInZone(Zone.SUPPORT, level);
		assertAttachedTo(coat, kog);
		assertAttachedTo(alliance, kog2);
		assertEquals(1, scn.GetWoundsOn(kog));
		assertEquals(1, scn.GetWoundsOn(kog2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoGondorConditionToDiscardItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kog = scn.GetFreepsCard("kog");
		var coat = scn.GetFreepsCard("coat");
		var alliance = scn.GetFreepsCard("alliance");
		scn.MoveCompanionsToTable(kog);
		// The nearest misses: a [gondor] card that is no condition, and a condition that is not [gondor].
		scn.AttachCardsTo(kog, coat, alliance);
		scn.AddWoundsToChar(kog, 1);

		scn.StartGame();

		assertEquals(1, scn.GetWoundsOn(kog));
		assertAttachedTo(coat, kog);
		assertAttachedTo(alliance, kog);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(kog));
	}
}
