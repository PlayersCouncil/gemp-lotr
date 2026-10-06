package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_032_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("revenant1", "103_32");
					put("revenant2", "103_32");
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability never used here
					// Gondorian Knight: a companion to play from hand for (2), with no requirement and no text but "Knight."
					put("knight1", "5_35");
					put("knight2", "5_35");
					put("guard", "1_7");          // Dwarf Guard: a [dwarven] companion with no text once in play

					put("savage", "1_151");       // Uruk Savage: [isengard], twilight cost 2, strength 5, vitality 3, site 5; "Damage +1." is its whole text
					put("troop", "1_177");        // Goblin Patrol Troop: [moria], twilight cost 6, strength 13, site 4; no text
					put("runner", "1_178");       // Goblin Runner: [moria], twilight cost 1, strength 5; its text is about being played
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetAmonHenScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("revenant1", "103_32");
					put("savage", "1_151");       // Uruk Savage: [isengard], twilight cost 2, strength 5, site 5; "Damage +1." is its whole text
				}},
				// The usual Fellowship path as far as site 8.
				new HashMap<String, String>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_361");        // Slopes of Amon Hen: "The twilight cost of each [isengard] minion is -1." is its whole text
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TormentedRevenantStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Tormented Revenant
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Wraith
		 * Strength: 4
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: Enduring.
		 * 	To play, spot or add 2 threats.
		 * 	This companion is strength +X, where X is the twilight cost of each minion skirmishing this companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("revenant1");

		assertEquals("Tormented Revenant", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WRAITH, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void PlayedForThreeAddingTwoThreatsWhenThereAreNoneToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var revenant = scn.GetFreepsCard("revenant1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(revenant);

		scn.StartGame();

		// Frodo and Aragorn: room for exactly the 2 threats.
		assertEquals(0, scn.GetThreats());
		assertEquals(2, scn.GetThreatLimit());
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(revenant));

		scn.FreepsPlayCard(revenant);

		assertEquals(2, scn.GetThreats());
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, revenant);
		assertEquals(4, scn.GetStrength(revenant));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOneThreatAndRoomForOneMoreUntilACompanionMakesRoomForTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var revenant = scn.GetFreepsCard("revenant1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(revenant, knight);
		// One threat short of the 2 to spot, and with Frodo and Aragorn room for only one more. The burdens are
		// tokens of another kind. The Revenant is not yet a companion while its cost is paid, so it makes no room
		// for itself.
		scn.AddThreats(1);
		scn.AddBurdens(2);

		scn.StartGame();

		assertEquals(1, scn.GetThreats());
		assertEquals(2, scn.GetThreatLimit());
		assertFalse(scn.FreepsPlayAvailable(revenant));

		scn.FreepsPlayCard(knight);

		// A third companion: room for two more threats.
		assertEquals(3, scn.GetThreatLimit());
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(revenant));

		scn.FreepsPlayCard(revenant);

		// One threat cannot be spotted as two, so two are added: 1 + 2
		assertEquals(3, scn.GetThreats());
		assertEquals(2, scn.GetBurdens());
		// 2 for the Knight + 3 for the Revenant
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, revenant);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithTwoThreatsAndNoRoomForMoreItIsPlayedBySpottingThem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var revenant = scn.GetFreepsCard("revenant1");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(revenant);
		// Frodo and a Dwarf Guard: the 2 threats are the limit, and none can be added. No [gondor] card is in
		// play: the threats are all the text asks to be spotted.
		scn.AddThreats(2);

		scn.StartGame();

		assertEquals(2, scn.GetThreats());
		assertEquals(2, scn.GetThreatLimit());
		assertTrue(scn.FreepsPlayAvailable(revenant));

		scn.FreepsPlayCard(revenant);

		assertEquals(2, scn.GetThreats());
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, revenant);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithTwoThreatsAndRoomForTwoMoreThePlayerMayAddTwoInsteadOfSpotting() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var revenant1 = scn.GetFreepsCard("revenant1");
		var revenant2 = scn.GetFreepsCard("revenant2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		scn.MoveCompanionsToTable(aragorn, knight1, knight2);
		scn.MoveCardsToHand(revenant1, revenant2);
		scn.AddThreats(2);

		scn.StartGame();

		assertEquals(2, scn.GetThreats());
		assertEquals(4, scn.GetThreatLimit());

		scn.FreepsPlayCard(revenant1);

		assertTrue(scn.FreepsChoiceAvailable("Spot"));
		assertTrue(scn.FreepsChoiceAvailable("Add"));

		scn.FreepsChoose("Add");

		// 2 + 2
		assertEquals(4, scn.GetThreats());
		assertInZone(Zone.FREE_CHARACTERS, revenant1);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Five companions and 4 threats: room for one more only, so the second copy can only spot.
		scn.FreepsPlayCard(revenant2);

		assertEquals(4, scn.GetThreats());
		assertInZone(Zone.FREE_CHARACTERS, revenant2);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsStrongerByTheCostOfTheMinionItIsSkirmishingAndOnlyWhileThatSkirmishGoesOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var revenant1 = scn.GetFreepsCard("revenant1");
		var revenant2 = scn.GetFreepsCard("revenant2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(revenant1, revenant2, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { revenant1, savage },
				new PhysicalCardImpl[] { revenant2, troop },
				new PhysicalCardImpl[] { aragorn, runner }
		);

		// Assigned, with no skirmish begun: nobody is skirmishing anybody yet.
		assertEquals(4, scn.GetStrength(revenant1));
		assertEquals(4, scn.GetStrength(revenant2));

		scn.FreepsResolveSkirmish(revenant1);

		// The fellowship is at site 2, where the Savage (site 5) and the Troop (site 4) are roaming: X is the 2
		// printed on the Savage, with nothing for the roaming penalty. The other copy is assigned to the Troop
		// and is not yet skirmishing it.
		assertEquals(4 + 2, scn.GetStrength(revenant1));
		assertEquals(4, scn.GetStrength(revenant2));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.PassCurrentPhaseActions();

		// 6 against the Savage's 5: the skirmish is won on the bonus.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(revenant1));
		assertEquals(4, scn.GetStrength(revenant1));

		scn.FreepsResolveSkirmish(revenant2);

		assertEquals(4 + 6, scn.GetStrength(revenant2));
		assertEquals(4, scn.GetStrength(revenant1));

		scn.PassCurrentPhaseActions();

		// 10 against the Troop's 13: lost, for one wound.
		assertEquals(1, scn.GetWoundsOn(revenant2));

		scn.FreepsResolveSkirmish(aragorn);

		// Another companion gets nothing from the cost of the minion he is skirmishing, and neither copy gets
		// anything from it. The second copy is enduring: 4 + 2 for its wound.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(revenant1));
		assertEquals(4 + 2, scn.GetStrength(revenant2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void XIsTheSumOfTheCostsOfEveryMinionSkirmishingIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var revenant = scn.GetFreepsCard("revenant1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(revenant, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { revenant, troop },
				new PhysicalCardImpl[] { aragorn, runner }
		);
		scn.ShadowAssignToMinions(revenant, savage);
		scn.FreepsResolveSkirmish(revenant);

		// 6 for the [moria] Troop + 2 for the [isengard] Savage, and nothing for the Runner assigned to Aragorn
		assertEquals(4 + 6 + 2, scn.GetStrength(revenant));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Ignore("F-0109: a minion in play is counted at the cost a site's text has lowered (2 - 1), where CR 4.2 'twilight cost' has another card's text change a cost only as the card comes into play")
	@Test
	public void AMinionWhoseCostASiteLowersCountsAtTheCostPrintedOnIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAmonHenScenario();

		var revenant = scn.GetFreepsCard("revenant1");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(revenant);

		scn.StartGame();
		scn.SkipToSite(8);
		// The fellowship moves to site 9, Slopes of Amon Hen.
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();

		assertEquals(9, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetStrength(revenant));

		scn.FreepsAssignToMinions(revenant, savage);
		scn.FreepsResolveSkirmish(revenant);

		// The site makes each [isengard] minion's twilight cost -1, which by the rules it does only as the
		// minion comes into play. The Savage is in play: its cost is the 2 printed on it.
		assertEquals(4 + 2, scn.GetStrength(revenant));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
