package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_116_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("faramir", "4_116");
					put("knight", "5_35");        // Gondorian Knight: [gondor] Man, cost 2; Knight is his whole text
					put("ranger", "7_116");       // Ranger of Osgiliath: [gondor] Man, cost 2, a ranger; a regroup ability only
					put("pippin", "103_31");      // Pippin, Trollslayer: a [gondor] Hobbit, cost 2; to play, spot a [gondor] companion
					put("guard", "5_83");         // Household Guard: a [rohan] Man, cost 2; Valiant is his whole text
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man, cost 4, strength 8; a maneuver ability only

					put("chieftain", "1_266");    // Orc Chieftain: [sauron] Orc, strength 7, site 6; his text answers a weapon played on him
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, strength 5, site 5, damage +1
					put("runner", "1_178");       // Goblin Runner: [moria] Orc, strength 5, site 4; harmless when cheated in
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FaramirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Faramir, Captain of Gondor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: <b>Ring-bound</b>. <b>Ranger</b>.<br>The twilight cost of each other [gondor] Man in your starting fellowship is -1.<br>While skirmishing a roaming minion, Faramir is strength +2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("faramir");

		assertEquals("Faramir", card.getBlueprint().getTitle());
		assertEquals("Captain of Gondor", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void InTheStartingFellowshipEachOtherGondorManCostsOneLessOnceFaramirIsDown() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var knight = scn.GetFreepsCard("knight");
		var ranger = scn.GetFreepsCard("ranger");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");

		// While a player chooses his starting fellowship the game keeps the total of what he has played in the
		// twilight pool.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsHasCardChoiceAvailable(faramir, knight, ranger, guard));
		assertEquals(2, scn.GetTwilightCost(knight));
		assertEquals(2, scn.GetTwilightCost(ranger));
		assertEquals(2, scn.GetTwilightCost(pippin));
		assertEquals(2, scn.GetTwilightCost(guard));

		scn.FreepsChooseCard(faramir);

		// His own 3 is not lowered.
		assertInZone(Zone.FREE_CHARACTERS, faramir);
		assertEquals(3, scn.GetTwilight());
		// A [gondor] Man, knight or ranger, now costs 1 and makes the 4. A [gondor] Hobbit and a [rohan] Man keep
		// their 2, and 3 + 2 would be 5.
		assertEquals(1, scn.GetTwilightCost(knight));
		assertEquals(1, scn.GetTwilightCost(ranger));
		assertEquals(2, scn.GetTwilightCost(pippin));
		assertEquals(2, scn.GetTwilightCost(guard));
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, ranger));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, guard));

		scn.FreepsChooseCard(ranger);

		// 3 + 1 is the 4: the Knight's 1 no longer fits, and this player is asked nothing more.
		assertInZone(Zone.FREE_CHARACTERS, ranger);
		assertInZone(Zone.DECK, knight);
		assertFalse(scn.FreepsAnyDecisionsAvailable());

		// The other player's [gondor] Men are no business of this Faramir: his Knight costs him the printed 2.
		var hisKnight = scn.GetShadowCard("knight");
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));
		assertEquals(2, scn.GetTwilightCost(hisKnight));
		scn.ShadowChooseCard(hisKnight);
		assertInZone(Zone.FREE_CHARACTERS, hisKnight);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));
	}

	@Test
	public void AGondorManChosenAheadOfFaramirKeepsItsCostAndFaramirKeepsHis() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var knight = scn.GetFreepsCard("knight");
		var ranger = scn.GetFreepsCard("ranger");

		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(faramir, knight));

		scn.FreepsChooseCard(knight);

		// Faramir is still in the draw deck and lowers nothing.
		assertInZone(Zone.DECK, faramir);
		assertEquals(2, scn.GetTwilight());
		assertEquals(2, scn.GetTwilightCost(ranger));
		// Another 2 fits; Faramir's own 3 does not, as it would if he cost 1 less himself.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(ranger));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(faramir));
	}

	@Test
	public void AGondorManPlayedBesideFaramirInTheFellowshipPhaseKeepsItsCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(faramir);
		scn.MoveCardsToHand(knight);

		scn.StartGame();

		// Faramir is in play, but this is no starting fellowship: the Knight's printed 2.
		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetTwilightCost(knight));
		scn.FreepsPlayCard(knight);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, knight);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhileSkirmishingARoamingMinionFaramirIsStrengthPlus2AndNotWhileWaitingForHisSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(faramir, aragorn);

		var chieftain = scn.GetShadowCard("chieftain");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(chieftain, savage);

		// To Balin's Tomb, site 4: a site 6 Orc and a site 5 Uruk-hai are roaming there.
		scn.SkipToAssignments();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{faramir, chieftain},
				new PhysicalCardImpl[]{aragorn, savage});
		assertEquals(7, scn.GetStrength(faramir));

		// Aragorn's skirmish first: Faramir is assigned to a roaming minion but not skirmishing, and Aragorn,
		// skirmishing one, is not made stronger by his text.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, savage));
		assertTrue(scn.IsCharAssignedAgainst(faramir, chieftain));
		assertEquals(7, scn.GetStrength(faramir));
		assertEquals(8, scn.GetStrength(aragorn));
		// 8 against 5: Aragorn wins.
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(faramir);
		assertTrue(scn.IsCharSkirmishingAgainst(faramir, chieftain));
		// 7 + 2
		assertEquals(9, scn.GetStrength(faramir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// 9 against 7: he wins, and once his skirmish is over he is 7 again.
		scn.PassSkirmishActions();
		assertFalse(scn.IsCharSkirmishing(faramir));
		assertEquals(7, scn.GetStrength(faramir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SkirmishingARoamingUrukHaiFaramirIsStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(faramir);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		scn.FreepsAssignToMinions(faramir, savage);
		assertEquals(7, scn.GetStrength(faramir));

		scn.FreepsResolveSkirmish(faramir);
		assertTrue(scn.IsCharSkirmishingAgainst(faramir, savage));
		// 7 + 2
		assertEquals(9, scn.GetStrength(faramir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishingAMinionWhoIsNotRoamingFaramirIsNotStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(faramir);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(runner);

		// A site 4 minion at site 4 is not roaming.
		scn.SkipToAssignments();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(runner, Keyword.ROAMING));
		scn.FreepsAssignToMinions(faramir, runner);

		scn.FreepsResolveSkirmish(faramir);
		assertTrue(scn.IsCharSkirmishingAgainst(faramir, runner));
		assertEquals(7, scn.GetStrength(faramir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
