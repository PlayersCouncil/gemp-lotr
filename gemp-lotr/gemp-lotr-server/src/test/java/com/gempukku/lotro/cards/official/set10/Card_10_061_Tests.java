package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_061_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("houses1", "10_61");
					put("houses2", "10_61");
					put("hound", "7_192");      // Morgul Hound: [wraith] Orc, strength 5; +4 and fierce only at 2 wounds on the Ring-bearer
					put("mongrel", "7_194");    // Morgul Mongrel: [wraith] Orc, strength 7; fierce and damage +1 only at 3 wounds on the Ring-bearer
					put("lemenya", "1_232");    // Ulaire Lemenya, Lieutenant of Morgul: a [wraith] Nazgul, strength 9; text only on bearers of [wraith] conditions
					put("chieftain", "1_266");  // Orc Chieftain: a [sauron] Orc, strength 7; text only on a weapon played on him
					// Streaming to the Field: a [wraith] condition, no minion; its initiative text reaches nothing here and
					// its ability is not used.
					put("streaming", "8_78");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion, strength 8; maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] companion, strength 6; skirmish ability only
					put("merchant", "7_97");    // Gondorian Merchant: a [gondor] ALLY, only ever in the dead pile here
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] COMPANION, only ever in a dead pile here
					put("orophin", "1_56");     // Orophin, Lorien Bowman: an [elven] ally, only ever in the dead pile here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A [shire] ally at his home site and a [shire] companion, each to skirmish a [wraith] minion, with a [shire]
	// card in the dead pile.
	protected VirtualTableScenario GetAllyAndCompanionScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("houses1", "10_61");
					put("houses2", "10_61");
					put("nelya", "2_84");
					put("lemenya", "1_232");
					put("bounder", "1_286");
					put("sam", "1_311");
					put("pippin", "1_306");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HousesofLamentationStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Houses of Lamentation
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a [wraith] minion strength +2 (or +4 if skirmishing a companion of the same culture as a card in the dead pile).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("houses1");

		assertEquals("Houses of Lamentation", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// "(or +4 if skirmishing a companion of the same culture as a card in the dead pile)": the definition gave +4
	// against an ally of that culture as well.
	@Test
	public void TheLargerBonusIsForSkirmishingACompanionAndNotAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyAndCompanionScenario();

		var bounder = scn.GetFreepsCard("bounder");
		var sam = scn.GetFreepsCard("sam");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveAlliesToTable(bounder);
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToDeadPile(pippin);

		var houses1 = scn.GetShadowCard("houses1");
		var houses2 = scn.GetShadowCard("houses2");
		var nelya = scn.GetShadowCard("nelya");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(houses1, houses2);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, lemenya);
		scn.SkipToAssignments();
		// Site 2 is Bounder's home.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{bounder, nelya}, new PhysicalCardImpl[]{sam, lemenya});

		// Bounder is [shire], as the Pippin in the dead pile is, and an ally: +2.
		scn.FreepsResolveSkirmish(bounder);
		scn.FreepsPass();
		scn.ShadowPlayCard(houses1);
		scn.ShadowChooseCard(nelya);
		assertEquals(12, scn.GetStrength(nelya));
		assertEquals(9, scn.GetStrength(lemenya));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// Sam is a [shire] companion: +4.
		scn.FreepsResolveSkirmish(sam);
		scn.FreepsPass();
		scn.ShadowPlayCard(houses2);
		scn.ShadowChooseCard(lemenya);
		assertEquals(13, scn.GetStrength(lemenya));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotPlayableInTheAssignmentPhaseAndInASkirmishAnyWraithMinionMayBeChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToDeadPile(merchant);

		var houses = scn.GetShadowCard("houses1");
		var hound = scn.GetShadowCard("hound");
		var mongrel = scn.GetShadowCard("mongrel");
		var lemenya = scn.GetShadowCard("lemenya");
		var chieftain = scn.GetShadowCard("chieftain");
		var streaming = scn.GetShadowCard("streaming");
		scn.MoveCardsToHand(houses);
		scn.MoveCardsToSupportArea(streaming);

		scn.StartGame();
		scn.MoveMinionsToTable(hound, mongrel, lemenya, chieftain);
		scn.SetTwilight(10);
		assertInZone(Zone.DEAD, merchant);
		assertInHand(houses);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The phase next door: the Shadow player's assignment actions.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(houses));
		scn.ShadowPass();

		// The Hound and the Chieftain are left out of every skirmish.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, mongrel}, new PhysicalCardImpl[]{gimli, lemenya});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(hound));
		assertEquals(7, scn.GetStrength(mongrel));
		assertTrue(scn.ShadowPlayAvailable(houses));

		scn.ShadowPlayCard(houses);
		// The text names no skirmish: the Orc in this skirmish, the Nazgul in the next and the Orc in none are all
		// offered; not the [sauron] Orc, nor the [wraith] condition, nor the companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(mongrel, lemenya, hound));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(chieftain, streaming, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(hound);

		// The Mongrel is skirmishing a [gondor] companion, with a [gondor] card in the dead pile; the Hound, which was
		// chosen, is skirmishing nobody: +2.
		assertInDiscard(houses);
		assertEquals(7, scn.GetStrength(hound));
		assertEquals(7, scn.GetStrength(mongrel));
		assertEquals(9, scn.GetStrength(lemenya));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void MakesAMinionSkirmishingACompanionOfTheCultureOfACardInTheDeadPileStrengthPlus4ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToDeadPile(merchant);

		var houses = scn.GetShadowCard("houses1");
		var mongrel = scn.GetShadowCard("mongrel");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(houses);

		scn.StartGame();
		scn.MoveMinionsToTable(mongrel, lemenya);
		scn.SetTwilight(10);
		// A [gondor] ally in the dead pile.
		assertInZone(Zone.DEAD, merchant);
		assertInHand(houses);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, mongrel}, new PhysicalCardImpl[]{gimli, lemenya});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(mongrel));
		scn.ShadowPlayCard(houses);
		scn.ShadowChooseCard(mongrel);

		// Skirmishing Aragorn, a [gondor] companion: 7 + 4.
		assertInDiscard(houses);
		assertEquals(11, scn.GetStrength(mongrel));
		assertEquals(9, scn.GetStrength(lemenya));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The Mongrel (11) beats Aragorn (8); in the next skirmish the bonus is gone.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(7, scn.GetStrength(mongrel));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void PlusTwoWhereTheCompanionsCultureIsInTheShadowPlayersDeadPileAndNotTheFreePeoplesPlayers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToDeadPile(orophin);

		var houses = scn.GetShadowCard("houses1");
		var mongrel = scn.GetShadowCard("mongrel");
		// The Shadow player's own copy of a [gondor] companion, in his own dead pile.
		var shadowBoromir = scn.GetShadowCard("boromir");
		scn.MoveCardsToHand(houses);
		scn.MoveCardsToDeadPile(shadowBoromir);

		scn.StartGame();
		scn.MoveMinionsToTable(mongrel);
		scn.SetTwilight(10);
		// "The dead pile" is the Free Peoples player's (Q-0131): it holds only an [elven] card.
		assertInZone(Zone.DEAD, orophin, shadowBoromir);
		assertEquals(1, scn.GetFreepsDeadCount());
		assertInHand(houses);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, mongrel);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(mongrel));
		scn.ShadowPlayCard(houses);

		// The Mongrel is the only [wraith] minion and is chosen unasked: 7 + 2.
		assertInDiscard(houses);
		assertEquals(9, scn.GetStrength(mongrel));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AMinionAssignedToSuchACompanionWhoseSkirmishHasNotBegunGetsPlusTwoAndInThatSkirmishPlusFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToDeadPile(boromir);

		var houses1 = scn.GetShadowCard("houses1");
		var houses2 = scn.GetShadowCard("houses2");
		var mongrel = scn.GetShadowCard("mongrel");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(houses1, houses2);

		scn.StartGame();
		scn.MoveMinionsToTable(mongrel, lemenya);
		scn.SetTwilight(10);
		// A [gondor] companion in the dead pile.
		assertInZone(Zone.DEAD, boromir);
		assertInHand(houses1, houses2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, mongrel}, new PhysicalCardImpl[]{aragorn, lemenya});
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		// In Gimli's skirmish Lemenya is assigned to Aragorn, and not yet skirmishing him: 9 + 2.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(9, scn.GetStrength(lemenya));
		scn.ShadowPlayCard(houses1);
		scn.ShadowChooseCard(lemenya);
		assertEquals(11, scn.GetStrength(lemenya));
		assertEquals(7, scn.GetStrength(mongrel));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The Mongrel (7) beats Gimli (6).  In Aragorn's skirmish Lemenya is at 9 again, and now skirmishing him: 9 + 4.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(9, scn.GetStrength(lemenya));
		scn.ShadowPlayCard(houses2);
		scn.ShadowChooseCard(lemenya);
		assertEquals(13, scn.GetStrength(lemenya));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsPlayedForNothingWithNoWraithMinionToChoose() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var houses = scn.GetShadowCard("houses1");
		var chieftain = scn.GetShadowCard("chieftain");
		var streaming = scn.GetShadowCard("streaming");
		scn.MoveCardsToHand(houses);
		scn.MoveCardsToSupportArea(streaming);

		scn.StartGame();
		// A minion of another culture, and a [wraith] card that is no minion: no [wraith] minion anywhere.
		scn.MoveMinionsToTable(chieftain);
		scn.SetTwilight(10);
		assertInHand(houses);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, chieftain);
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(chieftain));
		assertTrue(scn.ShadowPlayAvailable(houses));
		scn.ShadowPlayCard(houses);

		assertInDiscard(houses);
		assertEquals(7, scn.GetStrength(chieftain));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
