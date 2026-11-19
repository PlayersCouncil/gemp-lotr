package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_037_Tests
{
	// The Hobbit table: the Hobbit path, Bilbo, Reliable Companion 30_46 as the Ring-bearer and no card in the Ring
	// slot. The Shadow hand is set to ONE card before each use, so the card revealed at random is that card.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troll", "33_37");
					// A second copy: its own exertion.
					put("troll2", "33_37");

					put("gloin", "30_11");      // Glóin, Father of Gimli: [dwarven] companion of cost 2; a Skirmish ability not used
					// Free Peoples cards that are no companion, of cost 2: a character, an item and a card that is neither
					put("master", "32_20");     // The Master, Master of Lake-Town: [esgaroth] ally, cost 2; his texts not reached
					put("barricade", "31_4");   // Great Barricade: [dwarven] possession, support area, cost 2
					put("bofur", "30_6");       // Bofur, Encouraging Fellow: [dwarven] follower, cost 2, in the support area
					// Free Peoples cards of other costs
					put("dori", "30_8");        // Dori, Really a decent fellow: [dwarven] follower, cost 1
					put("bifur", "30_5");       // Bifur, Inarticulate: [dwarven] follower, cost 1 (in the Free Peoples hand)
					put("bombur", "30_7");      // Bombur, Chief Cook: [dwarven] follower, cost 3, in the support area
					put("slingshot", "33_11");  // Ori's Slingshot: [dwarven] possession, support area, cost 0; its texts not reached

					put("nest", "31_54");       // Spider Nest: [spider] condition, support area, cost 2: a Shadow card of the cost
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] minion, cost 2; its Assignment ability not used
					put("footman", "30_42");    // Goblin Footman: [moria] minion, cost 3
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void CatapultTrollStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Catapult Troll
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Troll
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Skirmish:</b> Exert this minion to reveal a random card from your hand. The Free Peoples player must discard a Free Peoples card (except a companion) that has the same twilight cost as the revealed card.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("troll");

		assertEquals("Catapult Troll", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.TROLL, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishActionRevealsACardOfCost2AndTheFreePeoplesPlayerDiscardsACardOfCost2ThatIsNoCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var troll2 = scn.GetShadowCard("troll2");
		var gloin = scn.GetFreepsCard("gloin");
		var master = scn.GetFreepsCard("master");
		var barricade = scn.GetFreepsCard("barricade");
		var bofur = scn.GetFreepsCard("bofur");
		var dori = scn.GetFreepsCard("dori");
		var bifur = scn.GetFreepsCard("bifur");
		var bombur = scn.GetFreepsCard("bombur");
		var nest = scn.GetShadowCard("nest");
		var aggressor = scn.GetShadowCard("aggressor");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gloin);
		scn.MoveAlliesToTable(master);
		scn.MoveCardsToSupportArea(barricade, bofur, dori, bombur, nest);

		scn.StartGame();
		scn.MoveMinionsToTable(troll, troll2);
		assertInZone(Zone.SUPPORT, master, barricade, bofur, dori, bombur, nest);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gloin, troll);
		// The second copy, left over, put on Glóin too: both in the one skirmish.
		scn.ShadowAssignToMinions(gloin, troll2);
		scn.FreepsResolveSkirmish(gloin);
		// The Free Peoples hand holds a card of cost 1: were it the hand revealed, Dori would be the one offered.
		scn.SetFreepsHand(bifur);
		scn.SetShadowHand(aggressor);
		assertInHand(bifur, aggressor);
		assertEquals(0, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(troll2));
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(troll));
		scn.ShadowUseCardAction(troll);
		// The exertion is this copy's own.
		assertEquals(1, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(troll2));
		// Shown to the Free Peoples player, listed and not to be chosen; its owner is shown nothing.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aggressor, false));
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		// Cost 2, no companion: an ally, a possession and a follower; not Glóin (a companion of cost 2), Dori (cost
		// 1), Bombur (cost 3), nor the Shadow player's Spider Nest (cost 2).
		assertTrue(scn.FreepsHasCardChoiceAvailable(master, barricade, bofur));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gloin, dori, bombur, nest, bilbo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(barricade);

		assertInDiscard(barricade);
		assertInZone(Zone.SUPPORT, master, bofur, dori, bombur, nest);
		assertInZone(Zone.FREE_CHARACTERS, gloin);
		assertInHand(aggressor, bifur);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var barricade = scn.GetFreepsCard("barricade");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCardsToSupportArea(barricade);

		scn.StartGame();
		scn.MoveMinionsToTable(troll);
		assertEquals(0, scn.GetWoundsOn(troll));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetShadowHand(aggressor);
		assertInHand(aggressor);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(troll));
	}

	@Test
	public void AnExhaustedCopyIsNotOfferedAndTheOtherIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var troll2 = scn.GetShadowCard("troll2");
		var gloin = scn.GetFreepsCard("gloin");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCompanionsToTable(gloin);

		scn.StartGame();
		scn.MoveMinionsToTable(troll, troll2);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(troll, 2);
		assertEquals(2, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(troll2));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gloin, troll);
		scn.ShadowAssignToMinions(gloin, troll2);
		scn.FreepsResolveSkirmish(gloin);
		scn.SetShadowHand(aggressor);
		assertInHand(aggressor);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(troll));
		assertTrue(scn.ShadowActionAvailable(troll2));
	}

	@Test
	public void UsedOutsideAnySkirmishWithNoFreePeoplesCardOfTheRevealedCostNothingIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var gloin = scn.GetFreepsCard("gloin");
		var barricade = scn.GetFreepsCard("barricade");
		var dori = scn.GetFreepsCard("dori");
		var aggressor = scn.GetShadowCard("aggressor");
		var footman = scn.GetShadowCard("footman");
		scn.MoveCompanionsToTable(gloin);
		scn.MoveCardsToSupportArea(barricade, dori);

		scn.StartGame();
		scn.MoveMinionsToTable(troll, aggressor);
		assertInZone(Zone.SUPPORT, barricade, dori);

		scn.SkipToAssignments();
		// The Troll is left unassigned: the skirmish is Glóin's against the Aggressor.
		scn.FreepsAssignToMinions(gloin, aggressor);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gloin);
		assertFalse(scn.IsCharAssigned(troll));
		scn.SetShadowHand(footman);
		assertInHand(footman);
		assertEquals(0, scn.GetWoundsOn(troll));
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(troll));
		scn.ShadowUseCardAction(troll);
		assertEquals(1, scn.GetWoundsOn(troll));
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(footman, false));
		scn.FreepsDismissRevealedCards();

		// Cost 3: no Free Peoples card in play has it, so nothing is discarded.
		assertInZone(Zone.SUPPORT, barricade, dori);
		assertInZone(Zone.FREE_CHARACTERS, gloin);
		assertInHand(footman);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithTheShadowHandEmptyTheExertionIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var gloin = scn.GetFreepsCard("gloin");
		var slingshot = scn.GetFreepsCard("slingshot");
		scn.MoveCompanionsToTable(gloin);
		scn.MoveCardsToSupportArea(slingshot);

		scn.StartGame();
		scn.MoveMinionsToTable(troll);
		assertInZone(Zone.SUPPORT, slingshot);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gloin, troll);
		scn.FreepsResolveSkirmish(gloin);
		scn.SetShadowHand();
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(troll));
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(troll));
		scn.ShadowUseCardAction(troll);
		// Nothing revealed: the exertion paid, and no card of cost 0 (Ori's Slingshot) is to be discarded.
		assertEquals(1, scn.GetWoundsOn(troll));
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertInZone(Zone.SUPPORT, slingshot);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
