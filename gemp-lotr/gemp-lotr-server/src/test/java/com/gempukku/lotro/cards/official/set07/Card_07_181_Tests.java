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

public class Card_07_181_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("held", "7_181");
					// Ulaire Nelya, Ringwraith in Twilight: a [wraith] Nazgul; fierce and damage +1 only at 2 burdens or
					// 2 wounds on the Ring-bearer
					put("nelya", "2_84");
					// Ulaire Lemenya, Lieutenant of Morgul: a [wraith] Nazgul; her text is about [wraith] conditions borne
					put("lemenya", "1_232");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgul; fierce, and his texts read burdens only
					put("khamul", "103_61");
					// Morgul Hound: a [wraith] minion who is no Nazgul (an Orc); stronger only at 2 wounds on the Ring-bearer
					put("hound", "7_192");
					// Black Steed: a Shadow possession borne by a minion ("Bearer must be a Nazgul")
					put("steed", "1_208");
					// Spider Poison: a Shadow possession that lies in its owner's support area; its texts are never used
					put("poison", "8_28");
					// Black Breath: a [wraith] condition, borne here by a companion; its skirmish ability is never used
					put("breath", "1_207");

					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion, strength 6, vitality 3
					put("armor", "1_8");        // Dwarven Armor: a possession borne by Gimli
					// Ring of Artifice: an artifact borne by Gimli (damage +1; strength +0 with no [dwarven] artifact in
					// the discard pile); its regroup ability is never used
					put("ring", "17_4");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only
					put("sword", "1_112");      // Ranger's Sword: a [gondor] possession borne by Aragorn, damage +1
					put("gandalf", "0_64");     // Gandalf, Stormcrow: strength 8, vitality 4, no text
					put("knight", "5_35");      // Gondorian Knight: strength 6, vitality 3, no text but Knight
					// Smeagol, Slippery Sneak: "Skirmish: Add threats equal to the total vitality of the minions Smeagol
					// is skirmishing to discard Smeagol."  His regroup ability is never used.
					put("smeagol", "8_27");
					// Thrarin, Dwarven Smith: a Dwarf ally; "Maneuver: Exert Thrarin to allow him to participate in archery
					// fire and skirmishes until the regroup phase"
					put("thrarin", "1_27");

					// Uruk-hai Raiding Party: strength 9, damage +1 and nothing else
					put("party1", "1_158");
					put("party2", "1_158");
					// Goblin Runner: a [moria] Orc, strength 5, vitality 1; its text answers its own play only
					put("runner", "1_178");
					put("marksman", "1_176");   // Goblin Marksman: an archer and nothing else
					// Greed: another Shadow condition in the support area; its maneuver ability wants 6 companions
					put("greed", "1_125");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HeldGroundStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Held Ground
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot 2 Nazgûl and add up to 3 threats.
		 * Each time a companion is about to be killed, you may remove a threat to discard up to 2 possessions.
		 * Discard this condition and remove 3 threats during the regroup phase.
		*/

		var scn = GetScenario();

		var held = scn.GetFreepsCard("held");

		assertEquals("Held Ground", held.getBlueprint().getTitle());
		assertNull(held.getBlueprint().getSubtitle());
		assertTrue(held.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, held.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, held.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, held.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(held, Keyword.SUPPORT_AREA));
		assertEquals(1, held.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedWithOneNazgulBesideAWraithOrcThenAddsTheThreatsChosenUpTo3OnceASecondIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var held = scn.GetShadowCard("held");
		var nelya = scn.GetShadowCard("nelya");
		var hound = scn.GetShadowCard("hound");
		var khamul = scn.GetShadowCard("khamul");
		// Four companions: the rule would let 4 threats be added, so a bound of 3 is the card's own.
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"), scn.GetFreepsCard("knight"));
		scn.MoveCardsToHand(held, khamul);
		assertInHand(held, khamul);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, hound);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// One Nazgul: a [wraith] minion who is no Nazgul does not make up the two.
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(held));

		// A second Nazgul, of another culture, played from hand.
		scn.ShadowPlayCard(khamul);
		assertInZone(Zone.SHADOW_CHARACTERS, khamul);
		assertTrue(scn.ShadowPlayAvailable(held));

		scn.ShadowPlayCard(held);
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");
		assertInZone(Zone.SUPPORT, held);
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoThreatAddedItIsPlayedAllTheSameAndIsStillDiscardedAtTheStartOfTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var held = scn.GetShadowCard("held");
		var nelya = scn.GetShadowCard("nelya");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));
		scn.MoveCardsToHand(held);
		assertInHand(held);

		scn.StartGame();
		// Two [wraith] Nazgul of different titles.
		scn.MoveMinionsToTable(nelya, lemenya);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(held));
		scn.ShadowPlayCard(held);
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowChoose("0");
		assertInZone(Zone.SUPPORT, held);
		assertEquals(0, scn.GetThreats());

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		// No threats to remove: it is discarded at the start of the regroup phase all the same.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInDiscard(held);
		assertEquals(0, scn.GetThreats());
	}

	@Test
	public void EachTimeACompanionIsAboutToBeKilledRemovesAThreatToDiscardUpTo2PossessionsOfEitherSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var held = scn.GetShadowCard("held");
		var nelya = scn.GetShadowCard("nelya");
		var steed = scn.GetShadowCard("steed");
		var poison = scn.GetShadowCard("poison");
		var breath = scn.GetShadowCard("breath");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var ring = scn.GetFreepsCard("ring");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var gandalf = scn.GetFreepsCard("gandalf");
		var party1 = scn.GetShadowCard("party1");
		var party2 = scn.GetShadowCard("party2");
		scn.MoveCompanionsToTable(gimli, aragorn, gandalf);
		scn.AttachCardsTo(gimli, armor, ring, breath);
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveCardsToSupportArea(held, poison);
		// Each dies of the first wound of a lost skirmish.
		scn.AddWoundsToChar(gimli, 2);
		scn.AddWoundsToChar(gandalf, 3);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertAttachedTo(armor, gimli);
		assertAttachedTo(ring, gimli);
		assertAttachedTo(breath, gimli);
		assertAttachedTo(sword, aragorn);
		assertInZone(Zone.SUPPORT, poison);

		scn.StartGame();
		scn.MoveMinionsToTable(party1, party2, nelya);
		scn.AttachCardsTo(nelya, steed);
		assertAttachedTo(steed, nelya);
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, party1}, new PhysicalCardImpl[]{gandalf, party2});
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		// Gimli is about to be killed.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(held));
		scn.ShadowChooseOptionalTrigger(held);
		assertEquals(1, scn.GetThreats());

		// Any possession, of either side, borne by anyone or by nobody; not an artifact or a condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, sword, steed, poison));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ring, breath));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(sword, steed);
		assertInDiscard(sword, steed);
		assertInZone(Zone.SUPPORT, poison);

		// Gimli is killed and the threat left becomes a wound.
		assertInZone(Zone.DEAD, gimli);
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Each time: a threat again, and Gandalf about to be killed in the next skirmish.
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(held));
		scn.ShadowChooseOptionalTrigger(held);
		assertEquals(0, scn.GetThreats());
		scn.ShadowChooseCards(poison);
		assertInDiscard(poison);
		assertInZone(Zone.DEAD, gandalf);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionAboutToBeKilledByAnArcheryWoundIsAnsweredAndNoPossessionNeedBeChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var held = scn.GetShadowCard("held");
		var marksman = scn.GetShadowCard("marksman");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(gimli, armor);
		scn.MoveCardsToSupportArea(held);
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertAttachedTo(sword, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SkipToArcheryWounds();

		// The archer's wound, placed on Gimli in the archery phase: no skirmish is needed.
		scn.FreepsChooseCard(gimli);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(held));
		scn.ShadowChooseOptionalTrigger(held);
		assertEquals(0, scn.GetThreats());

		// Up to 2: none is chosen, and nothing is discarded.
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();
		assertAttachedTo(sword, aragorn);
		assertInZone(Zone.DEAD, gimli);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenAMinionOrAnAllyIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var held = scn.GetShadowCard("held");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var thrarin = scn.GetFreepsCard("thrarin");
		var runner = scn.GetShadowCard("runner");
		var party1 = scn.GetShadowCard("party1");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(held);

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
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(held));
		assertEquals(1, scn.GetThreats());

		// Thrarin loses to the Raiding Party: an ally killed.  The threat becomes a wound on a companion.
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(held));
		assertInZone(Zone.DEAD, thrarin);
		assertEquals(0, scn.GetWoundsOn(knight));
		scn.FreepsChooseCard(knight);
		assertEquals(1, scn.GetWoundsOn(knight));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenACompanionIsDiscardedAndNotKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var held = scn.GetShadowCard("held");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveCardsToSupportArea(held);
		assertAttachedTo(sword, aragorn);
		assertInZone(Zone.SUPPORT, held);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, lemenya);
		scn.FreepsResolveSkirmish(smeagol);

		// Smeagol discards himself, adding 2 threats for Lemenya's vitality (three companions, three threats), and
		// his side is gone: the skirmish ends.  A companion discarded from play is not killed: no offer, though a
		// threat was there to pay with, and the game goes on into the regroup phase, whose start takes Held Ground
		// and the 3 threats.
		scn.FreepsUseCardAction(smeagol);
		assertInDiscard(smeagol);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(held));
		assertAttachedTo(sword, aragorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInDiscard(held);
		assertEquals(0, scn.GetThreats());
	}

	@Test
	public void DecliningDiscardsNothingAndBothThreatsBecomeWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var held = scn.GetShadowCard("held");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var knight = scn.GetFreepsCard("knight");
		var party1 = scn.GetShadowCard("party1");
		scn.MoveCompanionsToTable(gimli, aragorn, knight);
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveCardsToSupportArea(held);
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertAttachedTo(sword, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(party1);
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, party1);

		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(held));
		scn.ShadowDeclineOptionalTrigger();

		// Nothing is discarded, and both threats are there to become wounds as Gimli is killed.
		assertAttachedTo(sword, aragorn);
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

		var held = scn.GetShadowCard("held");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var party1 = scn.GetShadowCard("party1");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveCardsToSupportArea(held);
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertAttachedTo(sword, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(party1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, party1);

		assertEquals(0, scn.GetThreats());
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(held));
		assertInZone(Zone.DEAD, gimli);
		assertAttachedTo(sword, aragorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsDiscardedAndRemoves3ThreatsAtTheStartOfTheRegroupPhaseAndNotBefore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var held = scn.GetShadowCard("held");
		var greed = scn.GetShadowCard("greed");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn, scn.GetFreepsCard("gimli"), scn.GetFreepsCard("knight"));
		scn.MoveCardsToSupportArea(held, greed);
		assertInZone(Zone.SUPPORT, held, greed);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		// Four companions, four threats.
		scn.AddThreats(4);
		assertEquals(4, scn.GetThreats());
		scn.SkipToAssignments();

		// Past the starts of the maneuver, archery and assignment phases, and into the skirmish.
		assertInZone(Zone.SUPPORT, held);
		assertEquals(4, scn.GetThreats());
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);
		assertInZone(Zone.SUPPORT, held);
		scn.PassSkirmishActions();

		// At the start of the regroup phase it goes and takes 3 of the 4 threats; the other Shadow condition stays.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInDiscard(held);
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SUPPORT, greed);
	}
}
