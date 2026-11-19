package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_006_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  The skirmishes of
	// turn 1 are at The Troll Hoard 30_50 (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shield", "31_6");

					put("thorin", "30_19");     // Thorin, Oakenshield: strength 7, vitality 4, damage +1; +1 for each [dwarven] follower, and he bears none
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion who is no Thorin, strength 7; a Fellowship ability not used
					// Thorin III, Stonehelm: a Dwarf companion whose title only begins with "Thorin", strength 6; his
					// trigger answers a minion that loses to him, and none does.
					put("thorin3", "17_6");

					put("knowledge", "30_1");   // Ancestral Knowledge: a [dwarven] Maneuver event, cost 2
					put("bekar", "32_3");       // Du Bekar!: a [dwarven] Assignment event, cost 3
					put("barricade", "31_4");   // Great Barricade: a [dwarven] possession (no event), cost 2
					put("courage", "30_30");    // He gives me courage: a [gandalf] event, cost 2

					put("party", "1_158");      // Uruk-hai Raiding Party: strength 9, damage +1 and nothing else
					put("footman", "30_42");    // Goblin Footman: strength 8; a "when you play" offer, not reached (cheated in)
					put("aggressor", "30_39");  // Orkish Aggressor: strength 6; an Assignment ability, not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void OakenshieldStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Oakenshield
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Shield
		 * Strength: +1
		 * Game Text: Bearer must be Thorin.
		 * 	The twilight cost of each [dwarven] event is -1.
		 * 	<b>Response:</b> If Thorin is about to take a wound, discard this possession to prevent that wound.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shield");

		assertEquals("Oakenshield", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.SHIELD));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnThorinAndMakesEachDwarvenEventCostOneLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var thorin3 = scn.GetFreepsCard("thorin3");
		var knowledge = scn.GetFreepsCard("knowledge");
		var bekar = scn.GetFreepsCard("bekar");
		var barricade = scn.GetFreepsCard("barricade");
		var courage = scn.GetFreepsCard("courage");
		var shadowKnowledge = scn.GetShadowCard("knowledge");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin, balin, thorin3);
		scn.MoveCardsToHand(shield, knowledge, bekar, barricade, courage);
		// The Shadow player's own copy of the [dwarven] event, in his hand: the text names no player.
		scn.MoveCardsToHand(shadowKnowledge);

		scn.StartGame();
		assertInHand(shield, knowledge, bekar, barricade, courage, shadowKnowledge);

		// Before the Shield is in play, every card costs what it prints.
		assertEquals(2, scn.GetTwilightCost(knowledge));
		assertEquals(3, scn.GetTwilightCost(bekar));
		assertEquals(2, scn.GetTwilightCost(shadowKnowledge));
		assertEquals(2, scn.GetTwilightCost(barricade));
		assertEquals(2, scn.GetTwilightCost(courage));

		assertTrue(scn.FreepsPlayAvailable(shield));
		scn.FreepsPlayCard(shield);
		// Thorin is the one legal bearer: the Shield goes on him unasked, and not on Balin, Thorin III or Bilbo.
		assertAttachedTo(shield, thorin);
		assertEquals(0, scn.GetAttachedCards(balin).size());
		assertEquals(0, scn.GetAttachedCards(thorin3).size());
		assertEquals(0, scn.GetAttachedCards(bilbo).size());

		// [dwarven] events of two phases, either player's, cost 1 less; a [dwarven] possession and a [gandalf] event
		// cost what they print.  Each costs 2 or more, so -1 is told from -2.
		assertEquals(1, scn.GetTwilightCost(knowledge));
		assertEquals(2, scn.GetTwilightCost(bekar));
		assertEquals(1, scn.GetTwilightCost(shadowKnowledge));
		assertEquals(2, scn.GetTwilightCost(barricade));
		assertEquals(2, scn.GetTwilightCost(courage));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutThorinThoughThorinIIIIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var balin = scn.GetFreepsCard("balin");
		var thorin3 = scn.GetFreepsCard("thorin3");
		scn.MoveCompanionsToTable(balin, thorin3);
		scn.MoveCardsToHand(shield);

		scn.StartGame();
		assertInHand(shield);

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(shield));
	}

	@Test
	public void DiscardedToPreventTheFirstOfTwoWoundsThorinTakesInALostSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var thorin = scn.GetFreepsCard("thorin");
		var knowledge = scn.GetFreepsCard("knowledge");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(thorin, shield);
		scn.MoveCardsToHand(knowledge);

		scn.StartGame();
		scn.MoveMinionsToTable(party);
		assertAttachedTo(shield, thorin);
		assertInHand(knowledge);
		assertEquals(1, scn.GetTwilightCost(knowledge));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, party);
		scn.FreepsResolveSkirmish(thorin);
		assertEquals(0, scn.GetWoundsOn(thorin));

		// 8 (7 and the Shield's printed +1) against 9: Thorin loses, and the Raiding Party's damage +1 makes two wounds, one at a time.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(shield));
		scn.FreepsChooseOptionalTrigger(shield);

		// The Shield is discarded and that wound prevented; the second lands, and nothing is left to answer it.
		assertInDiscard(shield);
		assertEquals(1, scn.GetWoundsOn(thorin));
		// With the Shield gone, the [dwarven] event costs what it prints again.
		assertEquals(2, scn.GetTwilightCost(knowledge));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedForTheWoundsOfOtherDwarvesWhileThorinBearsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var thorin3 = scn.GetFreepsCard("thorin3");
		var footman = scn.GetShadowCard("footman");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCompanionsToTable(thorin, balin, thorin3);
		scn.AttachCardsTo(thorin, shield);

		scn.StartGame();
		scn.MoveMinionsToTable(footman, aggressor);
		assertAttachedTo(shield, thorin);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, footman}, new PhysicalCardImpl[]{thorin3, aggressor});
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(thorin3));

		// 7 against 8: Balin loses and takes his wound, with nothing offered.
		scn.FreepsResolveSkirmish(balin);
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(shield));
		assertEquals(1, scn.GetWoundsOn(balin));
		assertAttachedTo(shield, thorin);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// 6 against 6: Thorin III loses the tie and takes his wound, with nothing offered.
		scn.FreepsResolveSkirmish(thorin3);
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(shield));
		assertEquals(1, scn.GetWoundsOn(thorin3));
		assertAttachedTo(shield, thorin);
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
