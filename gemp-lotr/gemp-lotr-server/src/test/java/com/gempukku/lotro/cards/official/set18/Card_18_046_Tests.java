package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_046_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("disarmed", "18_46");
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man who is no ranger, vitality 3
					put("troop1", "1_177");     // Goblin Patrol Troop: a [moria] Orc with no text
					put("troop2", "1_177");
					put("scimitar1", "1_180");  // Goblin Scimitar: a Shadow possession for a [moria] Orc
					put("scimitar3", "1_180");
					// Let Her Deal With Them: a [gollum] condition for a minion, so that the Orc bears two Shadow cards
					// of two kinds (one hand weapon to a bearer); damage +1, and a play from the discard pile in a
					// Shadow phase only
					put("lethim", "7_63");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The maneuver ability's boards, as Second Level 7_118 (which prints the same sentence) lays them.
	protected VirtualTableScenario GetTransferScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("disarmed", "18_46");
					put("disarmed2", "18_46");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man, no ranger, vitality 3; Knight is its whole text
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] ranger, an Elf; strength +3 against a Nazgul only
					put("ranger", "7_116");     // Ranger of Osgiliath: [gondor] Man ranger, vitality 3; a regroup ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: [gondor], a Hobbit and no Man, no ranger; a skirmish ability used only on purpose
					put("guard", "5_83");       // Household Guard: a [rohan] Man, valiant, no other text
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] Man ally, vitality 1; an ability that needs initiative
					put("narsil", "9_34");      // Narsil: vitality +1, so the Merchant can exert; a response to its bearer's wound only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion, no text
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man minion, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// [wraith] Orcs, and Shadow cards of two kinds that go on them.
	protected VirtualTableScenario GetBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("disarmed", "18_46");
					put("disarmed2", "18_46");
					put("level", "7_118");      // Second Level: a [gondor] condition (fortification) transferred to a minion as this one is
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: a ranger to spot; strength +3 against a Nazgul only
					// Morgul Hound: a [wraith] Orc; strength +4 and fierce only at 2 wounds on the Ring-bearer, never here
					put("hound1", "7_192");
					put("hound2", "7_192");
					// Morgul Mongrel: a [wraith] Orc; fierce and damage +1 only at 3 wounds on the Ring-bearer
					put("mongrel", "7_194");
					// Morgul Axe: a [wraith] possession (hand weapon) for a [wraith] Orc; damage +1 only at 2 wounds on the Ring-bearer
					put("axe1", "7_186");
					put("axe2", "7_186");
					// In Twilight: a [wraith] condition for a [wraith] minion; an optional draw as it is played, and a
					// transfer when its bearer wins a skirmish
					put("twilight", "19_34");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DisarmedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Disarmed
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Strength: -1
		 * Game Text: Bearer cannot bear any Shadow cards.<br>When this condition is transferred to bearer, discard all Shadow cards it bears.<br><b>Maneuver:</b> Exert a [gondor] Man or spot a ranger to transfer this condition from your support area to a minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("disarmed");

		assertEquals("Disarmed", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(-1, card.getBlueprint().getStrength());
	}

	@Test
	public void ExertingAGondorManTransfersItFromTheSupportAreaToAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTransferScenario();

		var disarmed = scn.GetFreepsCard("disarmed");
		var disarmed2 = scn.GetFreepsCard("disarmed2");
		var frodo = scn.GetRingBearer();
		var knight = scn.GetFreepsCard("knight");
		var arwen = scn.GetFreepsCard("arwen");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var troop = scn.GetShadowCard("troop");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(knight, arwen, pippin, guard);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToSupportArea(disarmed, disarmed2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, brigand);
		scn.SkipToPhase(Phase.MANEUVER);

		assertAttachedTo(narsil, merchant);
		assertInZone(Zone.SUPPORT, disarmed, disarmed2);
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(disarmed));

		scn.FreepsUseCardAction(disarmed);
		// Both halves can be paid (Arwen is a ranger), so the player is asked which.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Exert"));
		assertTrue(scn.FreepsChoiceAvailable("ranger"));
		scn.FreepsChooseOption("Exert");

		// Who exerts: a [gondor] Man, companion or ally; not Pippin ([gondor], no Man), the Household Guard
		// (a Man, [rohan]), Arwen or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, guard, arwen, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merchant);
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(knight));

		// Where it goes: either minion; no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, brigand));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, pippin, guard, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		// This copy went; the other is still in the support area.
		assertAttachedTo(disarmed, troop);
		assertInZone(Zone.SUPPORT, disarmed2);
		assertTrue(scn.GetAttachedCards(brigand).isEmpty());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void SpottingARangerWhoIsNoManTransfersItWithNobodyExerting() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTransferScenario();

		var disarmed = scn.GetFreepsCard("disarmed");
		var knight = scn.GetFreepsCard("knight");
		var arwen = scn.GetFreepsCard("arwen");
		var guard = scn.GetFreepsCard("guard");
		var troop = scn.GetShadowCard("troop");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(knight, arwen, guard);
		scn.MoveCardsToSupportArea(disarmed);

		scn.StartGame();
		// Vitality 3 with 2 wounds: the one [gondor] Man cannot exert, so only the spot can be paid.
		scn.AddWoundsToChar(knight, 2);
		scn.MoveMinionsToTable(troop, brigand);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertInZone(Zone.SUPPORT, disarmed);
		assertTrue(scn.FreepsActionAvailable(disarmed));

		scn.FreepsUseCardAction(disarmed);
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, brigand));
		scn.FreepsChooseCard(brigand);

		assertAttachedTo(disarmed, brigand);
		assertTrue(scn.GetAttachedCards(troop).isEmpty());
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void SpottingAnExhaustedGondorManRangerTransfersItWithNobodyExerting() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTransferScenario();

		var disarmed = scn.GetFreepsCard("disarmed");
		var ranger = scn.GetFreepsCard("ranger");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(ranger);
		scn.MoveCardsToSupportArea(disarmed);

		scn.StartGame();
		// Vitality 3 with 2 wounds: he cannot exert, but he is still a ranger to spot.
		scn.AddWoundsToChar(ranger, 2);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetWoundsOn(ranger));
		assertInZone(Zone.SUPPORT, disarmed);
		assertTrue(scn.FreepsActionAvailable(disarmed));

		// The one minion takes it, unasked.
		scn.FreepsUseCardAction(disarmed);
		assertAttachedTo(disarmed, troop);
		assertEquals(2, scn.GetWoundsOn(ranger));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWithNoGondorManAbleToExertAndNoRanger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTransferScenario();

		var disarmed = scn.GetFreepsCard("disarmed");
		var knight = scn.GetFreepsCard("knight");
		var guard = scn.GetFreepsCard("guard");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(knight, guard);
		scn.MoveCardsToSupportArea(disarmed);

		scn.StartGame();
		// The Knight exhausted; the Household Guard could exert, but is a [rohan] Man; neither is a ranger.
		scn.AddWoundsToChar(knight, 2);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertInZone(Zone.SUPPORT, disarmed);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(disarmed));
	}

	@Test
	public void ACopyBorneByAMinionIsNotOfferedWhileOneInTheSupportAreaIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTransferScenario();

		var disarmed = scn.GetFreepsCard("disarmed");
		var disarmed2 = scn.GetFreepsCard("disarmed2");
		var knight = scn.GetFreepsCard("knight");
		var troop = scn.GetShadowCard("troop");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(disarmed2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, brigand);
		scn.AttachCardsTo(troop, disarmed);
		scn.SkipToPhase(Phase.MANEUVER);

		// The Knight can exert; "from your support area" is what keeps the borne copy from moving on.
		assertAttachedTo(disarmed, troop);
		assertInZone(Zone.SUPPORT, disarmed2);
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(disarmed));
		assertTrue(scn.FreepsActionAvailable(disarmed2));
	}

	@Test
	public void AMinionBearingItIsOfferedForNoShadowPossessionOrConditionButMayBearAFreePeoplesCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBearerScenario();

		var disarmed = scn.GetFreepsCard("disarmed");
		var disarmed2 = scn.GetFreepsCard("disarmed2");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToSupportArea(disarmed2);

		var hound1 = scn.GetShadowCard("hound1");
		var hound2 = scn.GetShadowCard("hound2");
		var mongrel = scn.GetShadowCard("mongrel");
		var axe = scn.GetShadowCard("axe1");
		var twilight = scn.GetShadowCard("twilight");
		scn.MoveCardsToHand(axe, twilight);

		scn.StartGame();
		// Disarmed on a minion in a Shadow phase is the board of a turn's second Shadow phase, after a maneuver
		// phase in which it was transferred; it is laid here in the first.
		scn.MoveMinionsToTable(hound1, hound2, mongrel);
		scn.AttachCardsTo(hound1, disarmed);
		scn.SetTwilight(10);
		assertAttachedTo(disarmed, hound1);
		assertInZone(Zone.SUPPORT, disarmed2);
		assertInHand(axe, twilight);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(axe));
		scn.ShadowPlayCard(axe);
		// A Shadow possession: the other two [wraith] Orcs may bear it, the one bearing Disarmed may not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(hound2, mongrel));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound1));
		scn.ShadowChooseCard(hound2);
		assertAttachedTo(axe, hound2);

		assertTrue(scn.ShadowPlayAvailable(twilight));
		scn.ShadowPlayCard(twilight);
		// A Shadow condition: the same.
		assertTrue(scn.ShadowHasCardChoiceAvailable(hound2, mongrel));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound1));
		scn.ShadowChooseCard(mongrel);
		// In Twilight's own "you may draw a card"
		scn.ShadowDeclineOptionalTrigger();
		assertAttachedTo(twilight, mongrel);
		assertEquals(1, scn.GetAttachedCards(hound1).size());

		// A Free Peoples card is no Shadow card: the second copy goes onto the minion that bears the first.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		scn.FreepsUseCardAction(disarmed2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(hound1, hound2, mongrel));
		scn.FreepsChooseCard(hound1);
		assertAttachedTo(disarmed, hound1);
		assertAttachedTo(disarmed2, hound1);
		assertAttachedTo(axe, hound2);
		assertAttachedTo(twilight, mongrel);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AnotherConditionTransferredDiscardsNothingAndThisOneDiscardsEveryShadowCardItsNewBearerBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBearerScenario();

		var disarmed = scn.GetFreepsCard("disarmed");
		var level = scn.GetFreepsCard("level");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToSupportArea(disarmed, level);

		var hound1 = scn.GetShadowCard("hound1");
		var hound2 = scn.GetShadowCard("hound2");
		var axe1 = scn.GetShadowCard("axe1");
		var axe2 = scn.GetShadowCard("axe2");
		var twilight = scn.GetShadowCard("twilight");

		scn.StartGame();
		scn.MoveMinionsToTable(hound1, hound2);
		scn.AttachCardsTo(hound1, axe1, twilight);
		scn.AttachCardsTo(hound2, axe2);
		scn.SkipToPhase(Phase.MANEUVER);

		assertAttachedTo(axe1, hound1);
		assertAttachedTo(twilight, hound1);
		assertAttachedTo(axe2, hound2);
		assertInZone(Zone.SUPPORT, disarmed, level);

		// Second Level transferred to the first Hound: Disarmed lies in the support area, and nothing is discarded.
		scn.FreepsUseCardAction(level);
		scn.FreepsChooseCard(hound1);
		assertAttachedTo(level, hound1);
		assertAttachedTo(axe1, hound1);
		assertAttachedTo(twilight, hound1);
		assertInZone(Zone.SUPPORT, disarmed);

		scn.ShadowPass();
		scn.FreepsUseCardAction(disarmed);
		assertTrue(scn.FreepsHasCardChoiceAvailable(hound1, hound2));
		scn.FreepsChooseCard(hound1);

		// Both Shadow cards the first Hound bears go; the Free Peoples condition beside them stays, and so does
		// the other Hound's Axe.
		assertAttachedTo(disarmed, hound1);
		assertInDiscard(axe1, twilight);
		assertAttachedTo(level, hound1);
		assertAttachedTo(axe2, hound2);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AHinderedShadowCardTheMinionBearsIsDiscardedWithTheOthersWhenThisIsTransferredToIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var disarmed = scn.GetFreepsCard("disarmed");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(disarmed);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var hindered = scn.GetShadowCard("scimitar1");
		var unhindered = scn.GetShadowCard("lethim");
		var elsewhere = scn.GetShadowCard("scimitar3");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		scn.AttachCardsTo(troop1, hindered, unhindered);
		scn.AttachCardsTo(troop2, elsewhere);
		// A hindered card has no type or culture, and is a Shadow card still (CR 5.0 "hinder"; Ketura on Q-0098:
		// "all Shadow cards" reaches it).
		scn.HinderCard(hindered);
		assertInZone(Zone.SUPPORT, disarmed);
		assertAttachedTo(hindered, troop1);
		assertAttachedTo(unhindered, troop1);
		assertAttachedTo(elsewhere, troop2);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.IsHindered(hindered));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsActionAvailable(disarmed));

		// No ranger can be spotted, so Boromir exerts.
		scn.FreepsUseCardAction(disarmed);
		scn.FreepsChooseCard(troop1);

		assertEquals(1, scn.GetWoundsOn(boromir));
		assertAttachedTo(disarmed, troop1);
		// Both Shadow cards the minion bore are discarded, the hindered one with the other; the other minion's
		// stays.
		assertInZone(Zone.DISCARD, hindered, unhindered);
		assertAttachedTo(elsewhere, troop2);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	// Comprehensive Rules 5.0, "hinder": while Disarmed is hindered its text does not apply, so the minion may
	// take up a Shadow card.  Its discard is tied to its own transfer ("When this condition is transferred to
	// bearer"), so when it is restored nothing is discarded: "Disarmed however also lacks an ongoing discard (it
	// explicitly makes it an on-transfer trigger), so it would also permit the shadow cards to stay" (Ketura on
	// Q-0223: B).  The restore here is the Free Peoples player's own reconcile, which comes before the minions
	// are discarded.
	@Test
	public void AShadowCardTheMinionTookUpWhileThisWasHinderedStaysWhenThisIsRestored() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var disarmed = scn.GetFreepsCard("disarmed");
		var boromir = scn.GetFreepsCard("boromir");
		// Boromir stays in hand, so that the reconcile has a discard to ask about
		scn.MoveCardsToHand(boromir);

		var troop = scn.GetShadowCard("troop1");
		var scimitar = scn.GetShadowCard("scimitar1");
		scn.MoveCardsToHand(scimitar);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, disarmed);
		assertAttachedTo(disarmed, troop);
		assertInZone(Zone.HAND, boromir);
		scn.FreepsPass();

		// "Bearer cannot bear any Shadow cards": the Troop is the only [moria] Orc
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(13 - 1, scn.GetStrength(troop));
		assertFalse(scn.ShadowPlayAvailable(scimitar));

		scn.HinderCard(disarmed);
		scn.ShadowRefreshDecision();
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.ShadowPlayAvailable(scimitar));
		scn.ShadowPlayCard(scimitar);
		// "When you play this possession, you may draw a card"
		scn.ShadowDeclineOptionalTrigger();
		assertAttachedTo(scimitar, troop);
		assertTrue(scn.IsHindered(disarmed));

		// Nobody is assigned, so there is no skirmish
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassRegroupActions();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.IsHindered(disarmed));

		scn.FreepsChooseToStay();
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
		// Restored, with its -1 back, and the Scimitar (+2) still on the minion
		assertFalse(scn.IsHindered(disarmed));
		assertAttachedTo(disarmed, troop);
		assertAttachedTo(scimitar, troop);
		assertEquals(13 - 1 + 2, scn.GetStrength(troop));
		assertInZone(Zone.HAND, boromir);
	}
}
