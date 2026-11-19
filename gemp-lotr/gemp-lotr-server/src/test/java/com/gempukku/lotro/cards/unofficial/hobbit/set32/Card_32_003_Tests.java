package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_003_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The assignment phase
	// of turn 1 is at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bekar", "32_3");

					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion; a fellowship ability only
					// [dwarven] cards in play that are no companions, one of each kind
					put("bifur", "30_5");       // Bifur, Inarticulate: [dwarven] follower in the support area; a trigger on a fierce win
					put("axe", "31_1");         // Dwarven Axe: [dwarven] possession, hand weapon, borne by Balin; damage +1 at a mountain
					put("emeralds", "31_3");    // Emeralds of Girion: [dwarven] artifact in the support area; fellowship and regroup abilities only
					put("dain", "32_2");        // Dain Ironfoot: [dwarven] Dwarf ally; +1 to Thorin and a start-of-regroup offer
					// Burning Fir Cones: a [gandalf] possession in the support area; an archery ability that wants Gandalf
					put("cones", "33_20");
					put("spear", "32_4");       // Dwarven Spear: a [dwarven] possession, held in hand

					// Old Tomnoddy: [spider] Spider of twilight cost 4, fierce; a "when you play" trigger, cheated in
					put("tomnoddy", "31_53");
					// Orkish Veteran: [sauron] Orc of twilight cost 2; archer at site 9 only, and a regroup ability
					put("veteran", "32_43");
					// Demolition Troll: [moria] Troll of twilight cost 5; a skirmish ability only
					put("troll", "32_31");
					// Hatred Rekindled: a [gundabad] condition of twilight cost 3 in the support area; a Shadow ability only
					put("hatred", "30_34");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void DuBekarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Du Bekâr!
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Assignment
		 * Game Text: Discard 2 [dwarven] cards (except companions) in play to discard a minion that has a twilight cost of 4 or less.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bekar");

		assertEquals("Du Bekâr!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ASSIGNMENT));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsTwoDwarvenCardsThatAreNoCompanionsToDiscardAMinionOfTwilightCostFourOrLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bekar = scn.GetFreepsCard("bekar");
		var balin = scn.GetFreepsCard("balin");
		var bifur = scn.GetFreepsCard("bifur");
		var axe = scn.GetFreepsCard("axe");
		var emeralds = scn.GetFreepsCard("emeralds");
		var dain = scn.GetFreepsCard("dain");
		var cones = scn.GetFreepsCard("cones");
		var spear = scn.GetFreepsCard("spear");
		var tomnoddy = scn.GetShadowCard("tomnoddy");
		var veteran = scn.GetShadowCard("veteran");
		var troll = scn.GetShadowCard("troll");
		var hatred = scn.GetShadowCard("hatred");
		scn.MoveCompanionsToTable(balin);
		scn.AttachCardsTo(balin, axe);
		scn.MoveCardsToSupportArea(bifur, emeralds, cones);
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToHand(bekar, spear);

		scn.StartGame();
		scn.MoveMinionsToTable(tomnoddy, veteran, troll);
		scn.MoveCardsToSupportArea(hatred);

		assertAttachedTo(axe, balin);
		assertInZone(Zone.SUPPORT, bifur, emeralds, cones, dain, hatred);
		assertInZone(Zone.SHADOW_CHARACTERS, tomnoddy, veteran, troll);
		assertInZone(Zone.HAND, bekar, spear);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.FreepsPlayAvailable(bekar));
		scn.FreepsPlayCard(bekar);

		// A follower, a borne possession, an artifact and an ally may pay; a [dwarven] companion, a [gandalf]
		// possession and a [dwarven] card in hand may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bifur, axe, emeralds, dain));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(balin, cones, spear));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(axe, dain);

		assertInZone(Zone.DISCARD, axe, dain);
		assertInZone(Zone.SUPPORT, bifur, emeralds, cones);
		assertInZone(Zone.FREE_CHARACTERS, balin);

		// Minions of two cultures and races at cost 4 and 2; not one of cost 5, nor a Shadow condition of cost 3.
		assertTrue(scn.FreepsHasCardChoiceAvailable(tomnoddy, veteran));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troll, hatred));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(tomnoddy);

		assertInZone(Zone.DISCARD, tomnoddy, bekar);
		assertInZone(Zone.SHADOW_CHARACTERS, veteran, troll);
		assertInZone(Zone.SUPPORT, hatred);
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void WithNoMinionOfTwilightCostFourOrLessItIsPlayedAndPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bekar = scn.GetFreepsCard("bekar");
		var bifur = scn.GetFreepsCard("bifur");
		var emeralds = scn.GetFreepsCard("emeralds");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCardsToSupportArea(bifur, emeralds);
		scn.MoveCardsToHand(bekar);

		scn.StartGame();
		scn.MoveMinionsToTable(troll);

		assertInZone(Zone.SUPPORT, bifur, emeralds);
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertInZone(Zone.HAND, bekar);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.FreepsPlayAvailable(bekar));
		scn.FreepsPlayCard(bekar);

		// The two [dwarven] cards are the only ones that can pay: taken unasked.
		assertInZone(Zone.DISCARD, bifur, emeralds, bekar);
		// The minion of cost 5 is not discarded.
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOnlyOneDwarvenCardInPlayThatIsNoCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bekar = scn.GetFreepsCard("bekar");
		var balin = scn.GetFreepsCard("balin");
		var axe = scn.GetFreepsCard("axe");
		var cones = scn.GetFreepsCard("cones");
		var tomnoddy = scn.GetShadowCard("tomnoddy");
		scn.MoveCompanionsToTable(balin);
		scn.AttachCardsTo(balin, axe);
		scn.MoveCardsToSupportArea(cones);
		scn.MoveCardsToHand(bekar);

		scn.StartGame();
		scn.MoveMinionsToTable(tomnoddy);

		assertAttachedTo(axe, balin);
		assertInZone(Zone.SUPPORT, cones);
		assertInZone(Zone.HAND, bekar);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		// The Axe is the one card that can pay; Balin is a companion and the Cones are [gandalf].
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(bekar));
	}
}
