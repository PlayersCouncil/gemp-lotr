package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_283_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legions", "7_283");
					// Orc Chieftain: a [sauron] Orc, strength 7, vitality 2; his text answers only a weapon played on him
					put("chieftain", "1_266");
					// Orc Trooper: a [sauron] Orc of another title; strength +1 for each card in his owner's hand
					put("trooper", "3_99");
					// Cirith Ungol Sentry: a [sauron] minion who is no Orc (an Uruk-hai), damage +1 and nothing else
					put("sentry", "10_84");
					// Morgul Hound: an Orc who is not [sauron] ([wraith]); stronger only at 2 wounds on the Ring-bearer
					put("hound", "7_192");
					// Ulaire Lemenya, Lieutenant of Morgul: a [wraith] Nazgul; her text is about [wraith] conditions borne
					put("lemenya", "1_232");
					// Ulaire Nertea, Messenger of Dol Guldur: a Nazgul to be played in the assignment phase; her own play
					// asks for 5 companions
					put("nertea", "1_234");
					// A Shadow Fell Over Them: "Assignment: Spot a Nazgul and remove (4) to play a Nazgul from your
					// discard pile"; its win trigger is never set off
					put("shadowfell", "15_182");

					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion, strength 6, vitality 3
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only
					put("gandalf", "0_64");     // Gandalf, Stormcrow: strength 8, vitality 4, no text
					put("knight", "5_35");      // Gondorian Knight: strength 6, vitality 3, no text but Knight
					// Smeagol, Slippery Sneak: "Skirmish: Add threats equal to the total vitality of the minions Smeagol
					// is skirmishing to discard Smeagol."  His regroup ability is never used.
					put("smeagol", "8_27");
					// Thrarin, Dwarven Smith: a Dwarf ally; "Maneuver: Exert Thrarin to allow him to participate in archery
					// fire and skirmishes until the regroup phase"
					put("thrarin", "1_27");

					// Uruk-hai Raiding Party: an [isengard] Uruk-hai, strength 9, damage +1 and nothing else
					put("party1", "1_158");
					// Goblin Runner: a [moria] Orc, strength 5, vitality 1; its text answers its own play only
					put("runner", "1_178");
					put("marksman", "1_176");   // Goblin Marksman: a [moria] archer and nothing else
					// Greed: another Shadow condition in the support area; its maneuver ability wants 6 companions
					put("greed", "1_125");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LegionsofMorgulStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Legions of Morgul
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot 2 [sauron] Orcs and add up to 3 threats.
		 * Each time a companion is about to be killed, you may remove a threat to make all minions <b>fierce</b> until the regroup phase.
		 * Discard this condition and remove 3 threats at the start of the regroup phase.
		*/

		var scn = GetScenario();

		var legions = scn.GetFreepsCard("legions");

		assertEquals("Legions of Morgul", legions.getBlueprint().getTitle());
		assertNull(legions.getBlueprint().getSubtitle());
		assertTrue(legions.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, legions.getBlueprint().getSide());
		assertEquals(Culture.SAURON, legions.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, legions.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(legions, Keyword.SUPPORT_AREA));
		assertEquals(3, legions.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedWithOneSauronOrcBesideASauronUrukHaiAndAWraithOrcThenAddsTheThreatsChosenUpTo3OnceASecondIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legions = scn.GetShadowCard("legions");
		var chieftain = scn.GetShadowCard("chieftain");
		var sentry = scn.GetShadowCard("sentry");
		var hound = scn.GetShadowCard("hound");
		var trooper = scn.GetShadowCard("trooper");
		// Four companions: the rule would let 4 threats be added, so a bound of 3 is the card's own.
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"), scn.GetFreepsCard("knight"));
		scn.MoveCardsToHand(legions, trooper);
		assertInHand(legions, trooper);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, sentry, hound);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// One [sauron] Orc: a [sauron] Uruk-hai and a [wraith] Orc do not make up the two.
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(legions));

		// A second [sauron] Orc, of another title, played from hand.
		scn.ShadowPlayCard(trooper);
		assertInZone(Zone.SHADOW_CHARACTERS, trooper);
		assertTrue(scn.ShadowPlayAvailable(legions));

		scn.ShadowPlayCard(legions);
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");
		assertInZone(Zone.SUPPORT, legions);
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoThreatAddedItIsPlayedAllTheSameAndIsStillDiscardedAtTheStartOfTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legions = scn.GetShadowCard("legions");
		var chieftain = scn.GetShadowCard("chieftain");
		var trooper = scn.GetShadowCard("trooper");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));
		scn.MoveCardsToHand(legions);
		assertInHand(legions);

		scn.StartGame();
		// Two [sauron] Orcs of different titles.
		scn.MoveMinionsToTable(chieftain, trooper);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(legions));
		scn.ShadowPlayCard(legions);
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowChoose("0");
		assertInZone(Zone.SUPPORT, legions);
		assertEquals(0, scn.GetThreats());

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		// No threats to remove: it is discarded at the start of the regroup phase all the same.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInDiscard(legions);
		assertEquals(0, scn.GetThreats());
	}

	@Test
	public void EachTimeACompanionIsAboutToBeKilledRemovesAThreatToMakeAllMinionsFierceUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legions = scn.GetShadowCard("legions");
		var chieftain = scn.GetShadowCard("chieftain");
		var party1 = scn.GetShadowCard("party1");
		var sentry = scn.GetShadowCard("sentry");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gimli, aragorn, gandalf);
		scn.MoveCardsToSupportArea(legions);
		// Each dies of the first wound of a lost skirmish.
		scn.AddWoundsToChar(gimli, 2);
		scn.AddWoundsToChar(gandalf, 3);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertInZone(Zone.SUPPORT, legions);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, party1, sentry);
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.SkipToAssignments();
		// The Sentry is left out of every skirmish.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, chieftain}, new PhysicalCardImpl[]{gandalf, party1});
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(gimli);
		assertFalse(scn.HasKeyword(chieftain, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(party1, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(sentry, Keyword.FIERCE));
		scn.PassSkirmishActions();

		// Gimli (6) loses to the Chieftain (7) and is about to be killed.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(legions));
		scn.ShadowChooseOptionalTrigger(legions);

		// All minions, of either culture and race, in this skirmish, in another and in none; no companion.
		assertTrue(scn.HasKeyword(chieftain, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(party1, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(sentry, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(aragorn, Keyword.FIERCE));

		// Gimli is killed and the threat left (one of the two was the cost) becomes a wound: one wound is asked for,
		// where a decline leaves two.
		assertInZone(Zone.DEAD, gimli);
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Each time: a threat again, and Gandalf about to be killed in the next skirmish.  The minions are still fierce.
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertTrue(scn.HasKeyword(party1, Keyword.FIERCE));
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(legions));
		scn.ShadowChooseOptionalTrigger(legions);
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.DEAD, gandalf);

		// The fierce round, which the card's own fierce brings about: still fierce.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.HasKeyword(chieftain, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(party1, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(sentry, Keyword.FIERCE));
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		// The regroup phase: fierce no longer.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(chieftain, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(party1, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(sentry, Keyword.FIERCE));
	}

	@Test
	public void ACompanionAboutToBeKilledByAnArcheryWoundIsAnsweredAndAMinionPlayedAfterwardsIsNotMadeFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legions = scn.GetShadowCard("legions");
		var marksman = scn.GetShadowCard("marksman");
		var lemenya = scn.GetShadowCard("lemenya");
		var nertea = scn.GetShadowCard("nertea");
		var shadowfell = scn.GetShadowCard("shadowfell");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToSupportArea(legions, shadowfell);
		scn.MoveCardsToDiscard(nertea);
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertInDiscard(nertea);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman, lemenya);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SetTwilight(20);
		scn.SkipToArcheryWounds();

		// The archer's wound, placed on Gimli in the archery phase: the text names no skirmish.
		scn.FreepsChooseCard(gimli);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(legions));
		scn.ShadowChooseOptionalTrigger(legions);
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.DEAD, gimli);
		assertTrue(scn.HasKeyword(marksman, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(lemenya, Keyword.FIERCE));

		// A Nazgul played from the discard pile in the assignment phase, after the threat was removed: the minions
		// made fierce were those in play then.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.FreepsPass();
		scn.ShadowUseCardAction(shadowfell);
		assertInZone(Zone.SHADOW_CHARACTERS, nertea);
		assertFalse(scn.HasKeyword(nertea, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(lemenya, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenAMinionOrAnAllyIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legions = scn.GetShadowCard("legions");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var thrarin = scn.GetFreepsCard("thrarin");
		var runner = scn.GetShadowCard("runner");
		var party1 = scn.GetShadowCard("party1");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(legions);
		assertInZone(Zone.SUPPORT, legions);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, party1);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SkipToPhase(Phase.MANEUVER);
		// Thrarin exerts to take part in skirmishes: vitality 2, one left.
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{thrarin, party1});

		// Aragorn (8) kills the Goblin Runner (5, vitality 1): a minion killed.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(legions));
		assertEquals(1, scn.GetThreats());

		// Thrarin loses to the Raiding Party: an ally killed.  The threat becomes a wound on a companion.
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(legions));
		assertInZone(Zone.DEAD, thrarin);
		assertEquals(0, scn.GetWoundsOn(knight));
		scn.FreepsChooseCard(knight);
		assertEquals(1, scn.GetWoundsOn(knight));
		assertFalse(scn.HasKeyword(party1, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenACompanionIsDiscardedAndNotKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legions = scn.GetShadowCard("legions");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToSupportArea(legions);
		assertInZone(Zone.SUPPORT, legions);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, lemenya);
		scn.FreepsResolveSkirmish(smeagol);

		// Smeagol discards himself, adding 2 threats for Lemenya's vitality (three companions, three threats), and
		// his side is gone: the skirmish ends.  A companion discarded from play is not killed: no offer, though a
		// threat was there to pay with, and the game goes on into the regroup phase, whose start takes Legions of
		// Morgul and the 3 threats.
		scn.FreepsUseCardAction(smeagol);
		assertInDiscard(smeagol);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(legions));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInDiscard(legions);
		assertEquals(0, scn.GetThreats());
	}

	@Test
	public void DecliningMakesNoMinionFierceAndBothThreatsBecomeWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legions = scn.GetShadowCard("legions");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var party1 = scn.GetShadowCard("party1");
		var sentry = scn.GetShadowCard("sentry");
		scn.MoveCompanionsToTable(gimli, aragorn, knight);
		scn.MoveCardsToSupportArea(legions);
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertInZone(Zone.SUPPORT, legions);

		scn.StartGame();
		scn.MoveMinionsToTable(party1, sentry);
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, party1);
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(legions));
		scn.ShadowDeclineOptionalTrigger();

		// No minion is made fierce, and both threats are there to become wounds as Gimli is killed.
		assertFalse(scn.HasKeyword(party1, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(sentry, Keyword.FIERCE));
		assertInZone(Zone.DEAD, gimli);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoThreatToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legions = scn.GetShadowCard("legions");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var party1 = scn.GetShadowCard("party1");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToSupportArea(legions);
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertInZone(Zone.SUPPORT, legions);

		scn.StartGame();
		scn.MoveMinionsToTable(party1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, party1);

		assertEquals(0, scn.GetThreats());
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(legions));
		assertInZone(Zone.DEAD, gimli);
		assertFalse(scn.HasKeyword(party1, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsDiscardedAndRemoves3ThreatsAtTheStartOfTheRegroupPhaseAndNotBefore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legions = scn.GetShadowCard("legions");
		var greed = scn.GetShadowCard("greed");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn, scn.GetFreepsCard("gimli"), scn.GetFreepsCard("knight"));
		scn.MoveCardsToSupportArea(legions, greed);
		assertInZone(Zone.SUPPORT, legions, greed);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		// Four companions, four threats.
		scn.AddThreats(4);
		assertEquals(4, scn.GetThreats());
		scn.SkipToAssignments();

		// Past the starts of the maneuver, archery and assignment phases, and into the skirmish.
		assertInZone(Zone.SUPPORT, legions);
		assertEquals(4, scn.GetThreats());
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);
		assertInZone(Zone.SUPPORT, legions);
		scn.PassSkirmishActions();

		// At the start of the regroup phase it goes and takes 3 of the 4 threats; the other Shadow condition stays.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInDiscard(legions);
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SUPPORT, greed);
	}
}
