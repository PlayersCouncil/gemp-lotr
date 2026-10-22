package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_044_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sixth", "8_44");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, vitality 4; a maneuver ability used only on purpose
					put("gknight", "5_35");     // Gondorian Knight: [gondor] Man companion, vitality 3; Knight is its whole text
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] Man ally, vitality 1; a maneuver ability used only on purpose
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: vitality +1 to a [gondor] Man, so that the Merchant can exert
					// Oathbreaker: a [gondor] companion who is a Wraith, no Man; a skirmish ability used only on purpose
					put("oath", "8_41");
					put("household", "5_83");   // Household Guard: [rohan] Man companion, vitality 3; valiant is its whole text

					// [gondor] fortifications, conditions of cost 1 with skirmish or maneuver abilities used only on purpose
					put("first", "7_92");       // First Level
					put("third", "7_125");      // Third Level
					put("citywall", "5_33");    // City Wall
					put("stonetower", "5_39");  // Stone Tower, laid in the other player's discard pile
					// Arrow-slits: a [rohan] fortification condition; a maneuver ability used only on purpose
					put("arrowslits", "5_80");
					// Rally Point: a [gondor] condition that is no fortification; a regroup ability that needs a knight
					// and a fortification borne by a minion
					put("rallypoint", "5_38");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SixthLevelStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Sixth Level
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Fortification</b>.<br><b>Regroup:</b> Exert a [gondor] Man to play a [gondor] fortification from your discard pile (limit 1).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sixth");

		assertEquals("Sixth Level", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.FORTIFICATION));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RegroupActionExertsAGondorManToPlayAGondorFortificationFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sixth = scn.GetFreepsCard("sixth");
		var gknight = scn.GetFreepsCard("gknight");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var oath = scn.GetFreepsCard("oath");
		var household = scn.GetFreepsCard("household");
		var citywall = scn.GetFreepsCard("citywall");
		var first = scn.GetFreepsCard("first");
		var arrowslits = scn.GetFreepsCard("arrowslits");
		var rallypoint = scn.GetFreepsCard("rallypoint");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(sixth);
		scn.MoveCompanionsToTable(gknight, oath, household);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToDiscard(citywall, first, arrowslits, rallypoint);

		// The other player's [gondor] fortification, of a title the Free Peoples player's piles do not hold.
		var stonetower = scn.GetShadowCard("stonetower");
		scn.MoveCardsToDiscard(stonetower);

		assertInZone(Zone.DISCARD, citywall, first, arrowslits, rallypoint, stonetower);

		scn.StartGame();
		// No minion: the game goes straight from the Shadow phase to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(2, scn.GetVitality(merchant));
		assertTrue(scn.FreepsActionAvailable(sixth));
		scn.FreepsUseCardAction(sixth);

		// The [gondor] Man: a companion and an ally; not a [gondor] companion who is no Man, nor a Man of [rohan].
		assertTrue(scn.FreepsHasCardChoiceAvailable(gknight, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(oath, household, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merchant);
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(gknight));

		// Two [gondor] fortifications of the player's own; not a [rohan] fortification, a [gondor] condition that
		// is no fortification, nor the other player's [gondor] fortification in his own pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(citywall, first));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arrowslits, rallypoint, stonetower));
		scn.FreepsChooseCard(citywall);

		assertInZone(Zone.SUPPORT, citywall);
		assertInZone(Zone.DISCARD, first, arrowslits, rallypoint, stonetower);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedAgainInThePhaseItWasUsedAndIsInTheNextRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sixth = scn.GetFreepsCard("sixth");
		var aragorn = scn.GetFreepsCard("aragorn");
		var first = scn.GetFreepsCard("first");
		var third = scn.GetFreepsCard("third");
		scn.MoveCardsToSupportArea(sixth);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToDiscard(first, third);
		assertInZone(Zone.DISCARD, first, third);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsActionAvailable(sixth));
		scn.FreepsUseCardAction(sixth);
		// Aragorn is the only [gondor] Man and exerts unasked.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(first);
		assertInZone(Zone.SUPPORT, first);
		scn.ShadowPass();

		// Limit 1, built as a limit on uses (Ketura, 2026-10-07): Aragorn can still exert and a fortification
		// is still in the discard pile, and the ability is not offered again this phase.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(sixth));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.DISCARD, third);

		// A second move, and with no minion a second regroup phase in the same turn: the limit is a new one.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(2, scn.GetMoveCount());
		assertTrue(scn.FreepsActionAvailable(sixth));
		scn.FreepsUseCardAction(sixth);

		// Third Level is the one fortification left in the pile and is played unasked.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SUPPORT, third);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedWhileTheOnlyGondorManIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sixth = scn.GetFreepsCard("sixth");
		var gknight = scn.GetFreepsCard("gknight");
		var oath = scn.GetFreepsCard("oath");
		var household = scn.GetFreepsCard("household");
		var citywall = scn.GetFreepsCard("citywall");
		scn.MoveCardsToSupportArea(sixth);
		scn.MoveCompanionsToTable(gknight, oath, household);
		scn.MoveCardsToDiscard(citywall);
		assertInZone(Zone.DISCARD, citywall);

		scn.StartGame();
		// The Knight, vitality 3, exhausted; Oathbreaker and the Household Guard, who are not [gondor] Men, can exert.
		scn.AddWoundsToChar(gknight, 2);
		assertEquals(2, scn.GetWoundsOn(gknight));
		assertEquals(0, scn.GetWoundsOn(oath));
		assertEquals(0, scn.GetWoundsOn(household));
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(sixth));
		assertInZone(Zone.DISCARD, citywall);
	}

	@Test
	public void RegroupActionIsNotOfferedWithNoGondorFortificationInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sixth = scn.GetFreepsCard("sixth");
		var gknight = scn.GetFreepsCard("gknight");
		var arrowslits = scn.GetFreepsCard("arrowslits");
		var rallypoint = scn.GetFreepsCard("rallypoint");
		scn.MoveCardsToSupportArea(sixth);
		scn.MoveCompanionsToTable(gknight);
		scn.MoveCardsToDiscard(arrowslits, rallypoint);

		var stonetower = scn.GetShadowCard("stonetower");
		scn.MoveCardsToDiscard(stonetower);
		assertInZone(Zone.DISCARD, arrowslits, rallypoint, stonetower);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		// The play is part of the price (Q-0062): with only a [rohan] fortification and a [gondor] condition that is
		// no fortification in his pile, and the other player's fortification in his, the Knight is not asked to exert.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(sixth));
		assertEquals(0, scn.GetWoundsOn(gknight));
	}
}
