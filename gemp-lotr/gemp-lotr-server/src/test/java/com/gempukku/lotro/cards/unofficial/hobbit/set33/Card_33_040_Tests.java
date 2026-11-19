package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_040_Tests
{

	// The Hobbit table: the Hobbit path, Bilbo, Reliable Companion 30_46 as the Ring-bearer and no card in the Ring
	// slot.  Turn 1's Shadow, maneuver and archery phases are at The Troll Hoard 30_50 (site 2), whose only text is a
	// start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bow", "33_40");

					// [moria] Orcs.
					put("troop", "33_41");       // Goblin Troop: [moria] Orc archer, vitality 3; +1 archery only with a [dwarven] artifact
					put("runner", "31_30");      // Goblin Runner: [moria] Orc, no archer; a "when you play" offer
					put("assaulting", "32_27");  // Assaulting Goblin: [moria] Orc, no archer; a response at a battleground only
					// [moria] archers for the discard pile.
					put("bowman", "2_60");       // Goblin Bowman: [moria] Orc archer, cost 2, no other text
					put("marksman", "1_176");    // Goblin Marksman: [moria] Orc archer, cost 3, no other text
					// Cards that fail one word.
					put("bats", "32_30");        // Dark Bats: [moria] Creature, no Orc; a Shadow ability, not used
					put("yazneg", "30_41");      // Yazneg: [gundabad] Orc; a maneuver ability, not used
					put("narzug", "30_36");      // Narzug: [gundabad] Orc archer; triggers on leaving play, not reached
					put("veteran", "32_43");     // Orkish Veteran: [sauron] Orc; archers at site 9 only, a regroup ability not used
					put("malice", "3_79");       // Malice: [moria] Archery event, "Spot a [moria] Orc and a [sauron] Orc to wound an archer"

					// The Free Peoples side.
					put("thorin", "30_19");      // Thorin, Oakenshield: [dwarven] companion, vitality 4; a Fellowship ability, not used
					put("kili", "30_12");        // Kili, Brother of Fili: [dwarven] archer companion; an Archery ability, not used
					put("master", "32_20");      // The Master: [esgaroth] Man ally; a Fellowship ability, not used
					put("elrond", "30_21");      // Elrond, Elven Lord: [elven] Elf ally, Wise; a start-of-turn heal and a Regroup ability, not used
					put("bard", "30_22");        // Bard, The Bowman: [esgaroth] archer ally; a Regroup ability, not used
					put("negotiator", "31_43");  // Skillful Negotiator: Maneuver event, an ally participates in archery fire and skirmishes
					put("herald", "32_21");      // Former Herald: Maneuver event, a Wise ally participates in archery fire and skirmishes
				}},
				null,
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void GoblinBowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Goblin Bow
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Ranged Weapon
		 * Vitality: 1
		 * Game Text: Bearer must be a [moria] Orc.<br>Bearer is an <b>archer</b>.<br>Allies cannot take archery wounds.<br><b>Archery:</b> Exert bearer to play a [moria] archer from your discard pile (limit once per turn).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bow");

		assertEquals("Goblin Bow", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RANGED_WEAPON));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void PlaysOnAMoriaOrcOnlyAndMakesThatBearerAnArcher() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetShadowCard("bow");
		var runner = scn.GetShadowCard("runner");
		var assaulting = scn.GetShadowCard("assaulting");
		var bats = scn.GetShadowCard("bats");
		var yazneg = scn.GetShadowCard("yazneg");
		scn.MoveCardsToHand(bow);

		scn.StartGame();
		assertInHand(bow);
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(runner, assaulting, bats, yazneg);
		scn.ShadowRefreshDecision();

		assertFalse(scn.HasKeyword(runner, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(assaulting, Keyword.ARCHER));
		assertEquals(0, scn.GetShadowArcheryTotal());
		scn.ShadowPlayCard(bow);
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, assaulting));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bats, yazneg));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(runner);

		assertAttachedTo(bow, runner);
		assertTrue(scn.HasKeyword(runner, Keyword.ARCHER));
		// Another [moria] Orc the Bow could have gone on, and the others, stay no archers.
		assertFalse(scn.HasKeyword(assaulting, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(bats, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(yazneg, Keyword.ARCHER));
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AlliesThatParticipateInArcheryFireAreNotOfferedTheMinionsArcheryWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetShadowCard("bow");
		var runner = scn.GetShadowCard("runner");
		var thorin = scn.GetFreepsCard("thorin");
		var master = scn.GetFreepsCard("master");
		var elrond = scn.GetFreepsCard("elrond");
		var negotiator = scn.GetFreepsCard("negotiator");
		var herald = scn.GetFreepsCard("herald");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin);
		scn.MoveCardsToHand(negotiator, herald);

		scn.StartGame();
		assertInHand(negotiator, herald);
		scn.MoveAlliesToTable(master, elrond);
		scn.MoveMinionsToTable(runner);
		scn.AttachCardsTo(runner, bow);
		scn.FreepsRefreshDecision();
		assertAttachedTo(bow, runner);

		// Two allies of two cultures and races are made to participate in archery fire.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(negotiator);
		scn.FreepsChooseCard(master);
		scn.ShadowPass();
		scn.FreepsPlayCard(herald);
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(1, scn.GetWoundsOn(elrond));

		// The Bow's bearer is the one archer: 1 wound for the Free Peoples player to place.
		scn.SkipToArcheryWounds();
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.FreepsHasCardChoiceAvailable(bilbo, thorin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(master, elrond));
		scn.FreepsChooseCard(thorin);

		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(master));
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithoutTheBowParticipatingAlliesAreOfferedTheMinionsArcheryWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop");
		var thorin = scn.GetFreepsCard("thorin");
		var master = scn.GetFreepsCard("master");
		var elrond = scn.GetFreepsCard("elrond");
		var negotiator = scn.GetFreepsCard("negotiator");
		var herald = scn.GetFreepsCard("herald");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin);
		scn.MoveCardsToHand(negotiator, herald);

		scn.StartGame();
		assertInHand(negotiator, herald);
		scn.MoveAlliesToTable(master, elrond);
		scn.MoveMinionsToTable(troop);
		scn.FreepsRefreshDecision();

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(negotiator);
		scn.FreepsChooseCard(master);
		scn.ShadowPass();
		scn.FreepsPlayCard(herald);
		scn.ShadowPass();

		// The same board with the Troop the archer and no Bow: each participating ally may take the wound.
		scn.SkipToArcheryWounds();
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.FreepsHasCardChoiceAvailable(bilbo, thorin, master, elrond));
		scn.FreepsChooseCard(master);

		assertEquals(1, scn.GetWoundsOn(master));
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnAllyMayStillBeWoundedInTheArcheryPhaseByAWoundThatIsNoArcheryWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetShadowCard("bow");
		var runner = scn.GetShadowCard("runner");
		var veteran = scn.GetShadowCard("veteran");
		var malice = scn.GetShadowCard("malice");
		var kili = scn.GetFreepsCard("kili");
		var bard = scn.GetFreepsCard("bard");
		scn.MoveCompanionsToTable(kili);
		scn.MoveCardsToHand(malice);

		scn.StartGame();
		assertInHand(malice);
		scn.MoveAlliesToTable(bard);
		scn.MoveMinionsToTable(runner, veteran);
		scn.AttachCardsTo(runner, bow);
		scn.FreepsRefreshDecision();
		assertAttachedTo(bow, runner);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(bard));
		assertTrue(scn.ShadowPlayAvailable(malice));
		scn.ShadowPlayCard(malice);

		// Malice's wound is an event's, not archery fire's: the archer ally may take it as the archer companion may.
		assertTrue(scn.ShadowHasCardChoiceAvailable(kili, bard));
		scn.ShadowChooseCard(bard);
		assertEquals(1, scn.GetWoundsOn(bard));
		assertEquals(0, scn.GetWoundsOn(kili));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void ArcheryActionExertsBearerToPlayAMoriaArcherFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetShadowCard("bow");
		var troop = scn.GetShadowCard("troop");
		var assaulting = scn.GetShadowCard("assaulting");
		var bowman = scn.GetShadowCard("bowman");
		var marksman = scn.GetShadowCard("marksman");
		var runner = scn.GetShadowCard("runner");
		var narzug = scn.GetShadowCard("narzug");
		var freepsTroop = scn.GetFreepsCard("troop");
		var thorin = scn.GetFreepsCard("thorin");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveCardsToDiscard(bowman, marksman, runner, narzug, freepsTroop);

		scn.StartGame();
		assertInZone(Zone.DISCARD, bowman, marksman, runner, narzug, freepsTroop);
		scn.MoveMinionsToTable(troop, assaulting);
		scn.AttachCardsTo(troop, bow);
		scn.SetTwilight(20);
		scn.FreepsRefreshDecision();
		assertAttachedTo(bow, troop);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(assaulting));
		assertTrue(scn.ShadowActionAvailable(bow));
		scn.ShadowUseCardAction(bow);

		// The bearer exerts; another [moria] Orc who could have exerted does not.
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(assaulting));
		// Either [moria] archer in the Shadow player's discard pile; not a [moria] Orc who is no archer, an archer of
		// another culture, or a [moria] archer in the other player's discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(bowman, marksman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, narzug, freepsTroop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(marksman);

		assertInZone(Zone.SHADOW_CHARACTERS, marksman);
		assertInZone(Zone.DISCARD, bowman, runner, narzug, freepsTroop);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void ArcheryActionIsUsedOnceATurnEvenAcrossTwoArcheryPhases() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetShadowCard("bow");
		var troop = scn.GetShadowCard("troop");
		var bowman = scn.GetShadowCard("bowman");
		var marksman = scn.GetShadowCard("marksman");
		var thorin = scn.GetFreepsCard("thorin");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveCardsToDiscard(bowman, marksman);

		scn.StartGame();
		assertInZone(Zone.DISCARD, bowman, marksman);
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, bow);
		scn.SetTwilight(20);
		scn.FreepsRefreshDecision();
		assertAttachedTo(bow, troop);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		scn.ShadowUseCardAction(bow);
		scn.ShadowChooseCard(marksman);
		assertInZone(Zone.SHADOW_CHARACTERS, marksman);
		assertEquals(1, scn.GetWoundsOn(troop));

		// The bearer can exert again and the Bowman could be played: withheld all the same.
		scn.FreepsPass();
		assertInZone(Zone.DISCARD, bowman);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(bow));

		// The two archers' wounds go on Thorin; nobody is assigned, and the fellowship moves again.
		scn.ShadowPass();
		scn.FreepsChooseCard(thorin);
		scn.FreepsChooseCard(thorin);
		assertEquals(2, scn.GetWoundsOn(thorin));
		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		// The archery phase after the second move, the same turn: still withheld.
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(troop));
		assertInZone(Zone.DISCARD, bowman);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(bow));
	}

	@Test
	public void ArcheryActionIsNotOfferedWhenTheArcherCannotBePaidForOrTheBearerCannotExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetShadowCard("bow");
		var troop = scn.GetShadowCard("troop");
		var assaulting = scn.GetShadowCard("assaulting");
		var bowman = scn.GetShadowCard("bowman");
		var thorin = scn.GetFreepsCard("thorin");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveCardsToDiscard(bowman);

		scn.StartGame();
		assertInZone(Zone.DISCARD, bowman);
		scn.MoveMinionsToTable(troop, assaulting);
		scn.AttachCardsTo(troop, bow);
		scn.FreepsRefreshDecision();
		assertAttachedTo(bow, troop);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		// The Bowman costs 2 + 2 roaming at site 2: one short of it.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(bow));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(bow));

		// Vitality 3 + 1 from the Bow with 3 wounds: the bearer is exhausted, though another [moria] Orc could exert.
		scn.AddWoundsToChar(troop, 3);
		scn.ShadowRefreshDecision();
		assertEquals(3, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(assaulting));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(bow));
	}
}
