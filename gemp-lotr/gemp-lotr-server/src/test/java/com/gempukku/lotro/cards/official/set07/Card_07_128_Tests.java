package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_128_Tests
{

	// The fellowship is at site 2 (Ettenmoors) in the skirmishes of every test of this scenario.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("live", "7_128");
					put("live2", "7_128");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, strength 6, vitality 3; Knight and nothing else
					// Knight of Gondor: [gondor] Man companion, strength 5, vitality 3; a fellowship ability used only on purpose
					put("kog", "5_36");
					// Pippin, Trollslayer: a [gondor] companion who is no Man (a Hobbit); a skirmish ability used only
					// on purpose
					put("pippin", "103_31");
					// Rider of Rohan: a Man companion of another culture; his skirmish ability needs him mounted
					put("rider", "4_286");

					put("troop1", "1_177");     // Goblin Patrol Troop: [moria] Orc, strength 13, site 4 (roaming at site 2); no text
					put("troop2", "1_177");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, site 5 (roaming at site 2); damage +1
					// Goblin Man: [isengard] Orc, strength 6, site 2 (not roaming at site 2); discarded only underground,
					// and a Shadow ability used only on purpose
					put("goblinman", "2_42");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path, on which the Gondorian Merchant (home site 3) skirmishes at site 3 as an ally
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("live", "7_128");
					// Gondorian Merchant: the [gondor] Man ally, home site 3 of the Towers block; his maneuver ability is
					// used only on purpose
					put("merchant", "7_97");
					put("troop1", "1_177");     // Goblin Patrol Troop: [moria] Orc, strength 13, site 4 (roaming at site 3); no text
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhileWeYetLiveStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: While We Yet Live
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a roaming minion skirmishing a [gondor] Man strength -3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("live");

		assertEquals("While We Yet Live", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAChosenRoamingMinionSkirmishingAGondorManStrengthMinusThreeUntilTheSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var live = scn.GetFreepsCard("live");
		var knight = scn.GetFreepsCard("knight");
		var kog = scn.GetFreepsCard("kog");
		scn.MoveCompanionsToTable(knight, kog);
		scn.MoveCardsToHand(live);
		assertInZone(Zone.HAND, live);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var savage = scn.GetShadowCard("savage");
		var goblinman = scn.GetShadowCard("goblinman");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2, savage, goblinman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, troop1}, new PhysicalCardImpl[]{kog, troop2});
		// The Savage and Goblin Man, left over, are put on the Knight beside the first Troop.
		scn.ShadowAssignToMinions(knight, savage, goblinman);
		scn.FreepsResolveSkirmish(knight);

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(troop1, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(troop2, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(goblinman, Keyword.ROAMING));
		assertTrue(scn.IsCharSkirmishingAgainst(knight, goblinman));
		assertEquals(13, scn.GetStrength(troop1));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(6, scn.GetStrength(goblinman));
		assertEquals(13, scn.GetStrength(troop2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(live));

		scn.FreepsPlayCard(live);

		// The two roaming minions skirmishing the Knight, of two cultures and races; not Goblin Man (at his own
		// site), nor the Troop assigned to the Knight of Gondor, whose skirmish has not begun.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(goblinman, troop2, knight, kog));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		// 5 - 3; the others as printed.
		assertEquals(2, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop1));
		assertEquals(6, scn.GetStrength(goblinman));
		assertEquals(13, scn.GetStrength(troop2));
		assertInZone(Zone.DISCARD, live);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Still in force at the next decision of the same skirmish.
		scn.ShadowPass();
		assertEquals(2, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.FreepsPass();

		// The skirmish resolves (the Knight, 6 against 21, is overwhelmed); in the Knight of Gondor's skirmish, a
		// phase of its own, the Savage is back to his printed 5.
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		scn.FreepsResolveSkirmish(kog);
		assertTrue(scn.IsCharSkirmishingAgainst(kog, troop2));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void InTheSkirmishOfAGondorHobbitOrARohanManItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var live = scn.GetFreepsCard("live");
		var live2 = scn.GetFreepsCard("live2");
		var pippin = scn.GetFreepsCard("pippin");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(pippin, rider);
		scn.MoveCardsToHand(live, live2);
		assertInZone(Zone.HAND, live, live2);

		var troop1 = scn.GetShadowCard("troop1");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{pippin, troop1}, new PhysicalCardImpl[]{rider, savage});
		assertTrue(scn.HasKeyword(troop1, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));

		// Pippin's skirmish: a [gondor] companion who is no Man.  The event can be played, and the roaming Troop
		// keeps his strength.
		scn.FreepsResolveSkirmish(pippin);
		assertEquals(13, scn.GetStrength(troop1));
		assertTrue(scn.FreepsPlayAvailable(live));
		scn.FreepsPlayCard(live);
		assertInZone(Zone.DISCARD, live);
		assertEquals(13, scn.GetStrength(troop1));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// The Rider's skirmish: a Man of another culture.  The same.
		scn.FreepsResolveSkirmish(rider);
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.FreepsPlayAvailable(live2));
		scn.FreepsPlayCard(live2);
		assertInZone(Zone.DISCARD, live2);
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ARoamingMinionSkirmishingAGondorManWhoIsAnAllyIsMadeWeaker() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var live = scn.GetFreepsCard("live");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveAlliesToTable(merchant);

		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		scn.MoveCardsToHand(live);
		assertInZone(Zone.HAND, live);
		// Site 3 of the Towers block: the Merchant is at his home, and the only Man skirmishing here.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsCanAssign(merchant));
		scn.FreepsAssignToMinions(merchant, troop1);
		scn.FreepsResolveSkirmish(merchant);

		assertTrue(scn.HasKeyword(troop1, Keyword.ROAMING));
		assertEquals(13, scn.GetStrength(troop1));
		assertTrue(scn.FreepsPlayAvailable(live));
		scn.FreepsPlayCard(live);

		// The one target, unasked: 13 - 3.
		assertEquals(10, scn.GetStrength(troop1));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var live = scn.GetFreepsCard("live");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToHand(live);
		assertInZone(Zone.HAND, live);

		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.HasKeyword(troop1, Keyword.ROAMING));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(live));
		assertInZone(Zone.HAND, live);
	}
}
