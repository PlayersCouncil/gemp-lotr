package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_009_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The maneuver phase and
	// the skirmishes of turn 1 are at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("thrain", "32_9");

					// Thorin, Oakenshield: [dwarven] Dwarf companion, strength 7, damage +1, +1 for each [dwarven]
					// follower he bears; a fellowship ability only. Orcrist goes on no one else.
					put("thorin", "30_19");
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion, strength 7; a fellowship ability only
					// Gandalf, Leader of Dwarves: a reconcile trigger and two skirmish abilities, none used
					put("gandalf", "30_25");
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf; +2 only with 3 Ents

					// In the draw deck: [dwarven] items Thorin can bear
					put("axe", "31_1");         // Dwarven Axe: [dwarven] possession, hand weapon; damage +1 at a mountain
					put("orcrist", "30_17");    // Orcrist: [dwarven] artifact, hand weapon, Thorin only; a trigger on his win
					put("key", "32_11");        // Thrór's Key: [dwarven] possession with no item class; a move trigger from site 6 or 7 and a regroup ability
					// and cards that are none
					put("fury", "30_3");        // Battle Fury: a [dwarven] skirmish event
					// Iron-Forged Weapon: an [esgaroth] hand weapon a [dwarven] companion may bear; a skirmish ability
					put("forged", "32_18");
					// Emeralds of Girion: a [dwarven] artifact that plays to the support area, not on a bearer
					put("emeralds", "31_3");
					put("card1", "1_177");      // Goblin Patrol Troop: a card with no text, to fill a draw deck

					// Old Tomnoddy: [spider] Spider, 8/3, fierce; a "when you play" trigger, cheated in
					put("tomnoddy", "31_53");
					put("bats", "32_30");       // Dark Bats: [moria] Creature, 4/2; a Shadow ability not used
					put("troll", "32_31");      // Demolition Troll: [moria] Troll, 10/3; a skirmish ability not used
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void ThrainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Thráin, Father of Thorin
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Follower
		 * Subtype: Dwarf
		 * Strength: +1
		 * Aid: Spot Gandalf and add (2).
		 * Game Text: <b>Aid</b> – Spot Gandalf and add (2). <helper>(At the start of the maneuver phase, you may spot Gandalf and add (2) to transfer this to a companion.)</helper>
		 * 	Each time bearer wins a skirmish, you may play a [dwarven] possession or [dwarven] artifact from your draw deck on bearer.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("thrain");

		assertEquals("Thráin", card.getBlueprint().getTitle());
		assertEquals("Father of Thorin", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void AidSpotsGandalfAndAddsTwoToTransferThrainToACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrain = scn.GetFreepsCard("thrain");
		var thorin = scn.GetFreepsCard("thorin");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(thorin, gandalf);
		scn.MoveCardsToSupportArea(thrain);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.SUPPORT, thrain);
		int twilight = scn.GetTwilight();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(thrain, "Aid"));

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(twilight + 2, scn.GetTwilight());
		scn.FreepsChooseCard(thorin);

		assertAttachedTo(thrain, thorin);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AidIsNotOfferedWithoutGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrain = scn.GetFreepsCard("thrain");
		var thorin = scn.GetFreepsCard("thorin");
		var avenger = scn.GetFreepsCard("avenger");
		// A [gandalf] companion who is no Gandalf.
		scn.MoveCompanionsToTable(thorin, avenger);
		scn.MoveCardsToSupportArea(thrain);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(thrain, "Aid"));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertInZone(Zone.SUPPORT, thrain);
	}

	@Test
	public void WhenBearerWinsADwarvenItemIsPlayedOnHimFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrain = scn.GetFreepsCard("thrain");
		var thorin = scn.GetFreepsCard("thorin");
		var axe = scn.GetFreepsCard("axe");
		var orcrist = scn.GetFreepsCard("orcrist");
		var fury = scn.GetFreepsCard("fury");
		var forged = scn.GetFreepsCard("forged");
		var emeralds = scn.GetFreepsCard("emeralds");
		var tomnoddy = scn.GetShadowCard("tomnoddy");
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(thorin, thrain);

		scn.StartGame();
		scn.SetFreepsDrawDeck(axe, orcrist, fury, forged, emeralds, scn.GetFreepsCard("card1"));
		scn.MoveMinionsToTable(tomnoddy);

		assertAttachedTo(thrain, thorin);
		assertInZone(Zone.DECK, axe, orcrist, fury, forged, emeralds);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, tomnoddy);
		scn.FreepsResolveSkirmish(thorin);
		// Thorin 7 + 1 for Thráin + 1 for a [dwarven] follower he bears, against Old Tomnoddy's 8.
		assertEquals(9, scn.GetStrength(thorin));
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(thrain));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();
		// A [dwarven] possession and a [dwarven] artifact; not a [dwarven] event, an [esgaroth] hand weapon, or a
		// [dwarven] artifact that cannot be played on a bearer.
		assertTrue(scn.FreepsHasCardChoiceAvailable(axe, orcrist));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(fury, forged, emeralds));
		// A play from a draw deck is chosen from 0 (Q-0019).
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(orcrist);

		assertAttachedTo(orcrist, thorin);
		assertInZone(Zone.DECK, axe, fury, forged, emeralds);
		// Shuffled as the card leaves the deck looked through, and again after the play.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void DecliningPlaysNothingAndASecondWinTheSameTurnOffersItAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrain = scn.GetFreepsCard("thrain");
		var thorin = scn.GetFreepsCard("thorin");
		var key = scn.GetFreepsCard("key");
		var axe = scn.GetFreepsCard("axe");
		var tomnoddy = scn.GetShadowCard("tomnoddy");
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(thorin, thrain);

		scn.StartGame();
		scn.SetFreepsDrawDeck(key, axe, scn.GetFreepsCard("card1"));
		scn.MoveMinionsToTable(tomnoddy);

		assertAttachedTo(thrain, thorin);
		assertInZone(Zone.DECK, key, axe);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, tomnoddy);
		scn.FreepsResolveSkirmish(thorin);
		int shuffles = scn.GetFreepsDeckShuffleCount();
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(thrain));
		scn.FreepsDeclineOptionalTrigger();
		// Nothing played, and the deck neither looked through nor shuffled.
		assertInZone(Zone.DECK, key, axe);
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		// Thorin's damage +1 left Old Tomnoddy with one vitality, for the fierce round.
		assertEquals(2, scn.GetWoundsOn(tomnoddy));
		assertTrue(scn.InFierceRound());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(thorin, tomnoddy);
		scn.FreepsResolveSkirmish(thorin);
		scn.PassFierceSkirmishActions();

		assertInZone(Zone.DISCARD, tomnoddy);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(thrain));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(key);

		assertAttachedTo(key, thorin);
		assertInZone(Zone.DECK, axe);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NothingIsOfferedWhenBearerLosesOrWhenAnotherCompanionWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrain = scn.GetFreepsCard("thrain");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var axe = scn.GetFreepsCard("axe");
		var bats = scn.GetShadowCard("bats");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCompanionsToTable(thorin, balin);
		scn.AttachCardsTo(thorin, thrain);

		scn.StartGame();
		scn.SetFreepsDrawDeck(axe, scn.GetFreepsCard("card1"));
		scn.MoveMinionsToTable(bats, troll);

		assertAttachedTo(thrain, thorin);
		assertInZone(Zone.DECK, axe);
		assertEquals(0, scn.GetWoundsOn(thorin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{thorin, troll}, new PhysicalCardImpl[]{balin, bats});

		// Balin, who does not bear Thráin, wins against the Bats' 4.
		scn.FreepsResolveSkirmish(balin);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(bats));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(thrain));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// Thorin's 9 against the Troll's 10: the bearer loses, and lives.
		scn.FreepsResolveSkirmish(thorin);
		assertEquals(9, scn.GetStrength(thorin));
		assertEquals(10, scn.GetStrength(troll));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(thrain));
		assertInZone(Zone.DECK, axe);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoDwarvenItemInTheDrawDeckTheOfferIsNotMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrain = scn.GetFreepsCard("thrain");
		var thorin = scn.GetFreepsCard("thorin");
		var fury = scn.GetFreepsCard("fury");
		var forged = scn.GetFreepsCard("forged");
		var tomnoddy = scn.GetShadowCard("tomnoddy");
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(thorin, thrain);

		scn.StartGame();
		scn.SetFreepsDrawDeck(fury, forged, scn.GetFreepsCard("card1"));
		scn.MoveMinionsToTable(tomnoddy);

		assertAttachedTo(thrain, thorin);
		assertInZone(Zone.DECK, fury, forged);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, tomnoddy);
		scn.FreepsResolveSkirmish(thorin);
		int shuffles = scn.GetFreepsDeckShuffleCount();
		scn.PassSkirmishActions();

		// A [dwarven] event and an [esgaroth] possession: nothing that could be played on him.  A Hobbit-set play from the draw deck with nothing it could play is withheld (Q-0019; the orchestrator on Q-0406 and Q-0418).
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(thrain));
		assertInZone(Zone.DECK, fury, forged);
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
