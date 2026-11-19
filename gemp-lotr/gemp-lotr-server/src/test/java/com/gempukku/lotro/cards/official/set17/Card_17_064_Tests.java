package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_064_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pillager", "17_64");
					put("pillager2", "17_64");

					// Gollum, Nasty Treacherous Creature: a [gollum] minion, vitality 4; a Regroup ability only
					put("gollum", "5_24");
					// Sudden Strike: [gollum] Shadow event, cost 2.  "Spot Gollum or Smeagol to play a minion from your
					// draw deck. If that minion is a [gollum] minion, ..."
					put("strike", "15_51");
					// Evil-smelling Fens: [gollum] support-area condition.  "Shadow: Exert Gollum twice and discard this
					// condition to play a minion from your discard pile."  (Its marsh line reaches no test.)
					put("fens", "5_22");
					// Fleet of Corsair Ships: a [men] support-area possession; a play trigger and a Regroup ability only
					put("fleet", "18_66");
					// Pandemonium: [men] Shadow event, cost 0.  "Play a [men] minion stacked on a [men] possession as if
					// from hand."
					put("pandemonium", "17_46");
					put("pandemonium2", "17_46");
					// Man of Bree: a [men] Man, cost 2; strength +1 for each card in the Free Peoples hand
					put("bree", "11_90");

					// Aragorn, Ranger of the North: strength 8, vitality 4; his Maneuver ability is not used
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void VengefulPillagerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Vengeful Pillager
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 7
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 15
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: When you play this minion from your draw deck or from your [men] possession, it is <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var pillager = scn.GetFreepsCard("pillager");

		assertEquals("Vengeful Pillager", pillager.getBlueprint().getTitle());
		assertNull(pillager.getBlueprint().getSubtitle());
		assertFalse(pillager.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, pillager.getBlueprint().getSide());
		assertEquals(Culture.MEN, pillager.getBlueprint().getCulture());
		assertEquals(CardType.MINION, pillager.getBlueprint().getCardType());
		assertEquals(Race.MAN, pillager.getBlueprint().getRace());
		assertEquals(7, pillager.getBlueprint().getTwilightCost());
		assertEquals(15, pillager.getBlueprint().getStrength());
		assertEquals(4, pillager.getBlueprint().getVitality());
		assertEquals(4, pillager.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedFromTheDrawDeckItIsFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pillager = scn.GetShadowCard("pillager");
		var gollum = scn.GetShadowCard("gollum");
		var strike = scn.GetShadowCard("strike");
		var pillager2 = scn.GetShadowCard("pillager2");
		scn.MoveCardsToHand(strike);
		// The second copy out of the deck, so that the copy played is the one read.
		scn.MoveCardsToDiscard(pillager2);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		assertInZone(Zone.DECK, pillager);
		scn.SetTwilight(30);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(pillager, Keyword.FIERCE));
		scn.ShadowPlayCard(strike);
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(pillager);

		assertInZone(Zone.SHADOW_CHARACTERS, pillager);
		assertTrue(scn.HasKeyword(pillager, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedFromAMenPossessionItIsFierceAndAnotherMenMinionPlayedAfterItIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pillager = scn.GetShadowCard("pillager");
		var bree = scn.GetShadowCard("bree");
		var fleet = scn.GetShadowCard("fleet");
		var pandemonium = scn.GetShadowCard("pandemonium");
		var pandemonium2 = scn.GetShadowCard("pandemonium2");
		scn.MoveCardsToSupportArea(fleet);
		scn.MoveCardsToHand(pandemonium, pandemonium2);

		scn.StartGame();
		scn.StackCardsOn(fleet, pillager, bree);
		assertInZone(Zone.STACKED, pillager, bree);
		scn.SetTwilight(30);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(pillager, Keyword.FIERCE));
		scn.ShadowPlayCard(pandemonium);
		scn.ShadowChooseCard(pillager);
		assertInZone(Zone.SHADOW_CHARACTERS, pillager);
		assertTrue(scn.HasKeyword(pillager, Keyword.FIERCE));

		// Another [men] minion played from the same possession after him: not fierce, and he still is.
		scn.FreepsPass();
		scn.ShadowPlayCard(pandemonium2);
		assertInZone(Zone.SHADOW_CHARACTERS, bree);
		assertFalse(scn.HasKeyword(bree, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(pillager, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedFromHandItIsNotFierceAndACopyPlayedFromTheDrawDeckLaterLeavesItSo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pillager = scn.GetShadowCard("pillager");
		var pillager2 = scn.GetShadowCard("pillager2");
		var gollum = scn.GetShadowCard("gollum");
		var strike = scn.GetShadowCard("strike");
		scn.MoveCardsToHand(pillager, strike);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		assertInZone(Zone.HAND, pillager);
		scn.SetTwilight(30);
		scn.FreepsPass();

		scn.ShadowPlayCard(pillager);
		assertInZone(Zone.SHADOW_CHARACTERS, pillager);
		assertFalse(scn.HasKeyword(pillager, Keyword.FIERCE));

		// The second copy, from the draw deck, is fierce; the first is not made so.
		assertInZone(Zone.DECK, pillager2);
		scn.ShadowPlayCard(strike);
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(pillager2);
		assertInZone(Zone.SHADOW_CHARACTERS, pillager2);
		assertTrue(scn.HasKeyword(pillager2, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(pillager, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedFromTheDiscardPileItIsNotFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pillager = scn.GetShadowCard("pillager");
		var gollum = scn.GetShadowCard("gollum");
		var fens = scn.GetShadowCard("fens");
		scn.MoveCardsToSupportArea(fens);
		scn.MoveCardsToDiscard(pillager);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		assertInZone(Zone.DISCARD, pillager);
		assertInZone(Zone.SUPPORT, fens);
		assertEquals(0, scn.GetWoundsOn(gollum));
		scn.SetTwilight(30);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(fens));
		scn.ShadowUseCardAction(fens);
		assertEquals(2, scn.GetWoundsOn(gollum));
		assertInZone(Zone.SHADOW_CHARACTERS, pillager);
		assertFalse(scn.HasKeyword(pillager, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FierceThroughTheFierceSkirmishAndNotInTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var pillager = scn.GetShadowCard("pillager");
		var fleet = scn.GetShadowCard("fleet");
		var pandemonium = scn.GetShadowCard("pandemonium");
		scn.MoveCardsToSupportArea(fleet);
		scn.MoveCardsToHand(pandemonium);

		scn.StartGame();
		scn.StackCardsOn(fleet, pillager);
		assertInZone(Zone.STACKED, pillager);
		scn.SetTwilight(30);
		scn.FreepsPass();
		scn.ShadowPlayCard(pandemonium);
		assertTrue(scn.HasKeyword(pillager, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, pillager);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.HasKeyword(pillager, Keyword.FIERCE));
		// 15 against 8, short of double: Aragorn loses and takes a wound.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.InFierceRound());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, pillager);
		scn.FreepsResolveSkirmish(aragorn);
		// Still fierce in the fierce skirmish, the last before the regroup phase.
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, pillager));
		assertTrue(scn.HasKeyword(pillager, Keyword.FIERCE));

		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, pillager);
		assertFalse(scn.HasKeyword(pillager, Keyword.FIERCE));
	}
}
