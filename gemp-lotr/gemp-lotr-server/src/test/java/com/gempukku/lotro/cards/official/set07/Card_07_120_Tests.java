package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_120_Tests
{

	// The fellowship is at site 2 (Ettenmoors) in the skirmishes of every test of this scenario.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stand", "7_120");
					put("stand2", "7_120");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, strength 6, vitality 3; Knight and nothing else
					// Knight of Gondor: [gondor] Man companion, vitality 3; a fellowship ability used only on purpose
					put("kog", "5_36");
					// Pippin, Trollslayer: a [gondor] companion who is no Man (a Hobbit); a skirmish ability used only
					// on purpose
					put("pippin", "103_31");
					// Rider of Rohan: a Man companion of another culture; his skirmish ability needs him mounted
					put("rider", "4_286");

					put("troop1", "1_177");     // Goblin Patrol Troop: [moria] Orc, vitality 3, site 4 (roaming at site 2); no text
					put("troop2", "1_177");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3, site 5 (roaming at site 2); damage +1
					// Goblin Man: [isengard] Orc, vitality 2, site 2 (not roaming at site 2); discarded only underground, and
					// a Shadow ability used only on purpose
					put("goblinman", "2_42");
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, vitality 1, site 3 (roaming at site 2); no text

					// Cards for the Free Peoples hand, played by nobody: a companion, a possession, an event, and
					// his own copies of the minions (a Shadow card).
					put("ranger", "7_116");     // Ranger of Osgiliath: [gondor] companion
					put("bow", "1_41");         // Elven Bow: [elven] possession
					put("defiance", "1_37");    // Defiance: [elven] skirmish event
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
					put("stand", "7_120");
					// Gondorian Merchant: the [gondor] Man ally, home site 3 of the Towers block; his maneuver ability is
					// used only on purpose
					put("merchant", "7_97");
					put("troop1", "1_177");     // Goblin Patrol Troop: [moria] Orc, vitality 3, site 4 (roaming at site 3); no text
					put("ranger", "7_116");     // Ranger of Osgiliath: a card for the hand
					put("bow", "1_41");         // Elven Bow: a card for the hand
					put("defiance", "1_37");    // Defiance: a card for the hand
					put("brigand", "4_10");     // Dunlending Brigand: a card for the hand
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetInitiativeScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stand", "7_120");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man, strength 6, vitality 3; Knight and nothing else
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, site 5 (roaming at site 2); damage +1
					// The Free Peoples player's own copies of these are cards to hold in hand: he can play none
					// of them.
					put("troop", "1_177");      // Goblin Patrol Troop: no text
					put("runner", "1_178");     // Goblin Runner: its text is about being played
					put("marksman", "1_176");   // Goblin Marksman: archer and nothing else
					put("brigand", "4_10");     // Dunlending Brigand: no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StandtoArmsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Stand to Arms
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: If you have initiative, discard 2 cards from hand to wound a roaming minion skirmishing a [gondor] Man twice.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stand");

		assertEquals("Stand to Arms", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsTwoCardsToWoundAChosenRoamingMinionSkirmishingAGondorManTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stand = scn.GetFreepsCard("stand");
		var knight = scn.GetFreepsCard("knight");
		var kog = scn.GetFreepsCard("kog");
		var ranger = scn.GetFreepsCard("ranger");
		var bow = scn.GetFreepsCard("bow");
		var defiance = scn.GetFreepsCard("defiance");
		var handTroop = scn.GetFreepsCard("troop1");
		scn.MoveCompanionsToTable(knight, kog);
		scn.MoveCardsToHand(stand, ranger, bow, defiance, handTroop);
		assertInZone(Zone.HAND, stand, ranger, bow, defiance, handTroop);

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
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(goblinman));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(stand));

		scn.FreepsPlayCard(stand);
		// The cost: any two cards of the hand, whatever their type or side.
		assertTrue(scn.FreepsHasCardChoiceAvailable(ranger, bow, defiance, handTroop));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(bow, handTroop);
		assertInZone(Zone.DISCARD, bow, handTroop);
		assertInZone(Zone.HAND, ranger, defiance);

		// The two roaming minions skirmishing the Knight, of two cultures and races; not Goblin Man (at his own
		// site), nor the Troop assigned to the Knight of Gondor, whose skirmish has not begun.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(goblinman, troop2, knight, kog));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		// Vitality 3: the Savage lives through both wounds.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(goblinman));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertInZone(Zone.DISCARD, stand);
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnExhaustedRoamingMinionCanBeChosenAndIsKilledByTheWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stand = scn.GetFreepsCard("stand");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToHand(stand, scn.GetFreepsCard("ranger"), scn.GetFreepsCard("bow"),
				scn.GetFreepsCard("defiance"), scn.GetFreepsCard("troop1"));

		var troop1 = scn.GetShadowCard("troop1");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, troop1);
		scn.ShadowAssignToMinions(knight, brigand);
		scn.FreepsResolveSkirmish(knight);

		assertTrue(scn.HasKeyword(brigand, Keyword.ROAMING));
		assertTrue(scn.IsCharSkirmishingAgainst(knight, brigand));
		// Vitality 1: the Brigand cannot exert, but can be wounded.
		assertEquals(1, scn.GetVitality(brigand));
		assertTrue(scn.FreepsPlayAvailable(stand));

		scn.FreepsPlayCard(stand);
		scn.FreepsChooseCards(scn.GetFreepsCard("bow"), scn.GetFreepsCard("defiance"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, brigand));
		scn.FreepsChooseCard(brigand);

		assertInZone(Zone.DISCARD, brigand);
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InTheSkirmishOfAGondorHobbitOrARohanManItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stand = scn.GetFreepsCard("stand");
		var stand2 = scn.GetFreepsCard("stand2");
		var pippin = scn.GetFreepsCard("pippin");
		var rider = scn.GetFreepsCard("rider");
		var ranger = scn.GetFreepsCard("ranger");
		var bow = scn.GetFreepsCard("bow");
		var defiance = scn.GetFreepsCard("defiance");
		var handTroop = scn.GetFreepsCard("troop1");
		var handSavage = scn.GetFreepsCard("savage");
		var handBrigand = scn.GetFreepsCard("brigand");
		scn.MoveCompanionsToTable(pippin, rider);
		// Eight cards: four beside the event at each play.
		scn.MoveCardsToHand(stand, stand2, ranger, bow, defiance, handTroop, handSavage, handBrigand);
		assertInZone(Zone.HAND, stand, stand2, ranger, bow, defiance, handTroop, handSavage, handBrigand);

		var troop1 = scn.GetShadowCard("troop1");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{pippin, troop1}, new PhysicalCardImpl[]{rider, savage});
		assertTrue(scn.HasKeyword(troop1, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));

		// Pippin's skirmish: a [gondor] companion who is no Man.  The event can be played, its cost is paid, and
		// the roaming Troop takes no wound.
		scn.FreepsResolveSkirmish(pippin);
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertTrue(scn.FreepsPlayAvailable(stand));
		scn.FreepsPlayCard(stand);
		scn.FreepsChooseCards(bow, handTroop);
		assertInZone(Zone.DISCARD, stand, bow, handTroop);
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// The Rider's skirmish: a Man of another culture.  The same.
		scn.FreepsResolveSkirmish(rider);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.FreepsPlayAvailable(stand2));
		scn.FreepsPlayCard(stand2);
		scn.FreepsChooseCards(defiance, handSavage);
		assertInZone(Zone.DISCARD, stand2, defiance, handSavage);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ARoamingMinionSkirmishingAGondorManWhoIsAnAllyIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var stand = scn.GetFreepsCard("stand");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveAlliesToTable(merchant);

		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		// The turns played leave cards in hand from reconciling, so the hand is laid after them.
		scn.MoveCardsToHand(stand, scn.GetFreepsCard("ranger"), scn.GetFreepsCard("bow"),
				scn.GetFreepsCard("defiance"), scn.GetFreepsCard("brigand"));
		// Site 3 of the Towers block: the Merchant is at his home, and the only Man skirmishing here.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsCanAssign(merchant));
		scn.FreepsAssignToMinions(merchant, troop1);
		scn.FreepsResolveSkirmish(merchant);

		assertTrue(scn.HasKeyword(troop1, Keyword.ROAMING));
		assertInZone(Zone.HAND, stand);
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertTrue(scn.FreepsPlayAvailable(stand));
		scn.FreepsPlayCard(stand);
		scn.FreepsChooseCards(scn.GetFreepsCard("bow"), scn.GetFreepsCard("defiance"));

		// The one target, unasked.
		assertEquals(2, scn.GetWoundsOn(troop1));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}


	// Comprehensive Rules 4.2, "playing a card": "You cannot count The Card for initiative. ...  If you are the
	// Free Peoples player and initiative is a requirement for playing The Card, you must have four cards in your
	// hand not counting The Card" (F-0464; Ketura, 2026-10-11).
	@Test
	public void WithFourCardsInHandItselfAmongThemItCannotBePlayedAndWithFourBesideItItCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetInitiativeScenario();

		var stand = scn.GetFreepsCard("stand");
		var knight = scn.GetFreepsCard("knight");
		var card1 = scn.GetFreepsCard("troop");
		var card2 = scn.GetFreepsCard("runner");
		var card3 = scn.GetFreepsCard("marksman");
		var card4 = scn.GetFreepsCard("brigand");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToHand(stand, card1, card2, card3);
		assertInZone(Zone.HAND, stand, card1, card2, card3);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(knight, savage);

		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		assertTrue(scn.IsCharSkirmishingAgainst(knight, savage));

		// Four cards in hand, the event one of them.  He has initiative, and three cards beside the event: it
		// cannot be played.
		assertEquals(4, scn.GetFreepsHandCount());
		assertInZone(Zone.HAND, stand);
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(stand));

		// With four cards beside it, it can.
		scn.MoveCardsToHand(card4);
		scn.FreepsRefreshDecision();
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsPlayAvailable(stand));
		assertEquals(0, scn.GetWoundsOn(savage));

		scn.FreepsPlayCard(stand);
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(card1, card2, card3, card4));
		scn.FreepsChooseCards(card1, card2);

		assertInZone(Zone.DISCARD, card1, card2, stand);
		assertEquals(2, scn.GetFreepsHandCount());
		// Vitality 3: the Savage lives through both wounds.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
