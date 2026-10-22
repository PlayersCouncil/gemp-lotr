package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_114_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shadowplay", "10_114");
					put("sam", "1_311");        // Sam, Son of Hamfast: a Hobbit companion, vitality 4
					put("troop", "1_177");      // Goblin Patrol Troop: vitality 3, cost 6, no text
					put("troop2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetHobbitsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shadowplay", "10_114");
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion, vitality 4; his skirmish ability
					                            // is used only on purpose
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit; to play, add a burden
					put("bounder", "1_286");    // Bounder: a Hobbit ally; a skirmish ability used only on purpose
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: a [shire] Hobbit ally; a fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Hobbit

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3; damage +1 and nothing else
					// Morgul Hound: [wraith] Orc, vitality 2; +4 and fierce only at 2 wounds on the Ring-bearer, who has none here
					put("hound", "7_192");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					// Spider Poison: a [gollum] possession that plays to the support area; its texts want a companion
					// skirmishing Shelob or bearing it
					put("poison", "8_28");
					// Ulaire Lemenya: [wraith] Nazgul, vitality 2; her text reaches only a bearer of a [wraith] condition
					put("lemenya", "1_232");
					// Ulaire Nertea: [wraith] Nazgul, vitality 2; her play's offer wants 5 companions
					put("nertea", "1_234");
					// A Shadow Fell Over Them: "Assignment: Spot a Nazgul and remove (4) to play a Nazgul from your
					// discard pile."  Its other text answers a Nazgul winning a skirmish.
					put("fell", "15_182");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShadowplayStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Shadowplay
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot 2 [shire] companions.<br>Each time a minion is played, you may exert a Hobbit companion to exert that minion (limit once per turn).
		*/

		var scn = GetScenario();

		var shadowplay = scn.GetFreepsCard("shadowplay");

		assertEquals("Shadowplay", shadowplay.getBlueprint().getTitle());
		assertNull(shadowplay.getBlueprint().getSubtitle());
		assertFalse(shadowplay.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, shadowplay.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, shadowplay.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, shadowplay.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(shadowplay, Keyword.SUPPORT_AREA));
		assertEquals(1, shadowplay.getBlueprint().getTwilightCost());
	}

	@Test
	public void TriggerIsNotOfferedAgainInTheTurnItWasUsed() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var shadowplay = scn.GetFreepsCard("shadowplay");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCardsToSupportArea(shadowplay);
		scn.MoveCompanionsToTable(sam);

		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCardsToHand(troop, troop2);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(troop);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(shadowplay));
		scn.FreepsChooseOptionalTrigger(shadowplay);
		scn.FreepsChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(troop));

		// Limit once per turn: a limit on uses, so when the next minion is played the trigger is not offered
		// again, though a Hobbit could still exert (Ketura on Q-0093).
		scn.ShadowPlayCard(troop2);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(shadowplay));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilTwoShireCompanionsCanBeSpottedThenPlaysToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHobbitsScenario();

		var shadowplay = scn.GetFreepsCard("shadowplay");
		var pippin = scn.GetFreepsCard("pippin");
		var smeagol = scn.GetFreepsCard("smeagol");
		var bilbo = scn.GetFreepsCard("bilbo");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(pippin, aragorn);
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCardsToHand(shadowplay, smeagol);

		scn.StartGame();
		assertInHand(shadowplay, smeagol);
		// Frodo is the one [shire] companion: Pippin is a [gondor] Hobbit, Bilbo a [shire] ally, Aragorn neither.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(shadowplay));

		// Smeagol, a [shire] companion who is no Hobbit, makes two.
		scn.FreepsPlayCard(smeagol);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertTrue(scn.FreepsPlayAvailable(shadowplay));
		scn.FreepsPlayCard(shadowplay);

		assertInZone(Zone.SUPPORT, shadowplay);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenAMinionIsPlayedAHobbitCompanionMayExertToExertThatMinionButNotWhenAnotherShadowCardIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHobbitsScenario();

		var shadowplay = scn.GetFreepsCard("shadowplay");
		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var smeagol = scn.GetFreepsCard("smeagol");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(pippin, smeagol, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(shadowplay);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCardsToHand(savage, poison);

		scn.StartGame();
		// A minion already in play when the next one is played.
		scn.MoveMinionsToTable(troop);
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.SetTwilight(20);
		scn.FreepsPass();

		// A Shadow card that is no minion: nothing is offered.
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(poison);
		assertInZone(Zone.SUPPORT, poison);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(shadowplay));
		assertTrue(scn.AwaitingShadowPhaseActions());

		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.ShadowPlayCard(savage);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(shadowplay));
		scn.FreepsChooseOptionalTrigger(shadowplay);

		// The Ring-bearer and a [gondor] Hobbit companion; not the Hobbit ally, the [shire] companion who is no
		// Hobbit, or Aragorn.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bounder, smeagol, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);

		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferExertsNobodyAndLeavesItOnOfferForTheNextMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHobbitsScenario();

		var shadowplay = scn.GetFreepsCard("shadowplay");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(shadowplay);

		var savage = scn.GetShadowCard("savage");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(savage, hound);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.ShadowPlayCard(savage);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(shadowplay));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A declined offer is no use of the limit: the [wraith] Orc played next is offered, and only he exerts.
		scn.ShadowPlayCard(hound);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(shadowplay));
		scn.FreepsChooseOptionalTrigger(shadowplay);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(hound));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void LimitedOncePerTurnAcrossPhasesAndOfferedAgainOnTheNextTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHobbitsScenario();

		var shadowplay = scn.GetFreepsCard("shadowplay");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(shadowplay);

		var lemenya = scn.GetShadowCard("lemenya");
		var nertea = scn.GetShadowCard("nertea");
		var fell = scn.GetShadowCard("fell");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(lemenya);
		scn.MoveCardsToDiscard(nertea);
		scn.MoveCardsToSupportArea(fell);

		scn.StartGame();
		assertInDiscard(nertea);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.ShadowPlayCard(lemenya);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(shadowplay));
		scn.FreepsChooseOptionalTrigger(shadowplay);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(lemenya));

		// A Nazgul played from the discard pile in the assignment phase of the same turn: not offered, though
		// Frodo could exert again and Nertea could be exerted.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(12);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(fell));
		scn.ShadowUseCardAction(fell);
		assertInZone(Zone.SHADOW_CHARACTERS, nertea);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(shadowplay));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(nertea));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		scn.PassAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		// Turn 3, the Free Peoples player's next turn: the next minion played is offered again.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.MoveCardsToHand(savage);
		assertInHand(savage);
		scn.SetTwilight(20);
		scn.FreepsPass();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetWoundsOn(frodo));
		scn.ShadowPlayCard(savage);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(shadowplay));
		scn.FreepsChooseOptionalTrigger(shadowplay);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhenNoHobbitCompanionCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHobbitsScenario();

		var shadowplay = scn.GetFreepsCard("shadowplay");
		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var smeagol = scn.GetFreepsCard("smeagol");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(pippin, smeagol);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(shadowplay);
		// Both Hobbit companions exhausted (vitality 4); the Hobbit ally and the [shire] companion who is no Hobbit
		// could still exert.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(pippin, 3);

		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(savage);

		scn.StartGame();
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(pippin));
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(savage);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(shadowplay));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
