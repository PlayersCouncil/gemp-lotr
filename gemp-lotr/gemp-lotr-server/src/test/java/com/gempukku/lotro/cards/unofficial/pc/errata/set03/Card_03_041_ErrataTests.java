package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_041_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bowmen", "53_41");
					put("bowmen2", "53_41");
					put("boromir", "1_97");     // Boromir, Son of Denethor: unique [gondor] companion, vitality 3; a skirmish ability only
					put("knight", "5_35");      // Gondorian Knight: [gondor] companion who is not unique, Knight and no other text
					put("aragorn", "17_93");    // Aragorn, Defender of Rohan: a [rohan] Man companion; text only while the Ring-bearer is assigned
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] ally; a maneuver ability only
					put("narsil", "9_34");      // Narsil: vitality +1 on a [gondor] Man, so that the Merchant could exert
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is an archery phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GondorBowmenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Gondor Bowmen
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype:
		 * Game Text: Tale.
		 * Archery: Exert a [gondor] companion and discard this condition to make the fellowship archery total +2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bowmen");

		assertEquals("Gondor Bowmen", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ArcheryActionExertsAGondorCompanionAndDiscardsThisConditionToMakeTheFellowshipArcheryTotalPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bowmen = scn.GetFreepsCard("bowmen");
		var bowmen2 = scn.GetFreepsCard("bowmen2");
		var boromir = scn.GetFreepsCard("boromir");
		var knight = scn.GetFreepsCard("knight");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(boromir, knight, aragorn);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToSupportArea(bowmen, bowmen2);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		// No archers on either side.
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());
		// With Narsil on him the Merchant is not exhausted: he could exert if the choice let him.
		assertFalse(scn.IsExhausted(merchant));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsActionAvailable(bowmen));

		scn.FreepsUseCardAction(bowmen);
		// [gondor] companions, unique or not, may exert; a [rohan] Man companion, a [gondor] ally and Frodo may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, knight));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, merchant, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(knight);

		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(boromir));
		// This condition is discarded, not the other copy.
		assertInDiscard(bowmen);
		assertInZone(Zone.SUPPORT, bowmen2);
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());

		scn.ShadowPass();

		// The other copy is still there to be used.
		assertTrue(scn.FreepsActionAvailable(bowmen2));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void NotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bowmen = scn.GetFreepsCard("bowmen");
		var boromir = scn.GetFreepsCard("boromir");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(bowmen);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(bowmen));

		scn.FreepsPass();
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsActionAvailable(bowmen));
	}

	@Test
	public void NotOfferedWhenTheOnlyGondorCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bowmen = scn.GetFreepsCard("bowmen");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(boromir, aragorn);
		scn.MoveCardsToSupportArea(bowmen);
		// Vitality 3 with 2 wounds: Boromir cannot exert. The [rohan] Aragorn could, but is not [gondor].
		scn.AddWoundsToChar(boromir, 2);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.IsExhausted(boromir));
		assertFalse(scn.IsExhausted(aragorn));
		assertFalse(scn.FreepsActionAvailable(bowmen));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void TheArcheryTotalBonusLastsThroughTheArcheryPhaseAndNoLonger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bowmen = scn.GetFreepsCard("bowmen");
		var boromir = scn.GetFreepsCard("boromir");
		var knight = scn.GetFreepsCard("knight");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(boromir, knight);
		scn.MoveCardsToSupportArea(bowmen);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetFreepsArcheryTotal());

		scn.FreepsUseCardAction(bowmen);
		scn.FreepsChooseCard(knight);

		assertEquals(2, scn.GetFreepsArcheryTotal());

		scn.ShadowPass();
		// The last decision of the archery phase: still in force.
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(2, scn.GetFreepsArcheryTotal());

		scn.FreepsPass();

		// The first decision after it, in the assignment phase: gone.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(0, scn.GetFreepsArcheryTotal());
	}
}
