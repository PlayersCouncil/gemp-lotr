package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_008_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armaments", "18_8");
					put("legolas", "1_50");     // Legolas, Greenleaf: unique [elven] Elf companion; an archery ability only
					put("guardian", "4_76");    // Lórien Guardian: [elven] Elf companion, not unique; a regroup ability only
					put("galadriel", "9_14");   // Galadriel, Bearer of Wisdom: unique [elven] Elf companion; resistance while she bears an artifact
					put("nenya", "3_23");       // Nenya: [elven] artifact, a ring, on Galadriel only; a regroup-phase trigger
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: unique [elven] Elf companion; +3 against a Nazgûl only
					put("cloak", "1_42");       // Elven Cloak: [elven] possession, a cloak, on a companion (spot an Elf); the minion archery total -1
					put("lorienelf", "1_53");   // Lórien Elf: [elven] Elf companion, no text but "To play, spot an Elf"
					// Spider Poison: [gollum] possession that its Skirmish ability transfers to a companion; cheated
					// onto one, its only text there is an exertion when he is assigned.
					put("poison", "8_28");
					put("shelob", "8_26");      // Shelob, Last Child of Ungoliant: [gollum] Spider, strength 8, vitality 8; Spider Poison goes to a companion skirmishing her
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] Dwarf companion
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: [elven] Elf ally
					put("merry", "1_303");      // Merry, From O'er the Brandywine: an unbound Hobbit, for the Pipe's "To play"
					// Treasured Pipe: [shire] possession, a pipe; "To play, spot a pipe or an unbound Hobbit. Bearer
					// must be a companion."  Its ability is used only on purpose.
					put("pipe", "103_119");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetArcheryScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armaments", "18_8");
					put("armaments2", "18_8");
					put("legolas", "1_50");     // Legolas, Greenleaf: unique archer; "Archery: Exert Legolas to wound a minion"
					put("guardian", "4_76");    // Lórien Guardian: [elven] Elf companion, not unique; a regroup ability only
					// Double Shot: [elven] Archery event, cost 0; "Spot an Elf archer companion to make the fellowship
					// archery total +1".  An event, no special ability.
					put("doubleshot", "1_38");

					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc archer, strength 7, vitality 1, no other text
					put("marksman2", "1_176");
					// Wreathed in Shadow: [wraith] support-area condition; "Archery: Remove (1) to make the fellowship
					// archery total -1".  The Shadow player's archery special ability.
					put("wreathed", "1_238");
					// Elrond, Lord of Rivendell: [elven] ally; "Fellowship: Exert Elrond to draw a card", a Free
					// Peoples special ability of another phase.
					put("elrond", "1_40");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRestoreScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armaments", "18_8");
					// Legolas, Greenleaf: [elven] companion, strength 6, vitality 3; an archer, with an archery
					// ability.  No test here has an archery phase with a minion in it.
					put("legolas", "1_50");
					// Elven Bow: possession, ranged weapon, cost 1.  "Bearer must be an Elf.  Bearer is an archer."
					put("bow", "1_41");
					// Elven Cloak: possession, cloak, cost 1.  "To play, spot an Elf.  Bearer must be a companion."
					put("cloak", "1_42");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElvenArmamentsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Elven Armaments
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand weapon/shield
		 * Strength: 2
		 * Vitality: 1
		 * Game Text: To play, bearer must not be bearing any possessions. Bearer must be an [elven] companion.<br>Bearer cannot bear any other possessions.<br>While a unique companion bears this possession, the Free Peoples player may not use archery special abilities and the minion archery total is -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("armaments");

		assertEquals("Elven Armaments", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.SHIELD));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void IsPlayedOnlyOnAnElvenCompanionWhoBearsNoPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armaments = scn.GetFreepsCard("armaments");
		var legolas = scn.GetFreepsCard("legolas");
		var guardian = scn.GetFreepsCard("guardian");
		var galadriel = scn.GetFreepsCard("galadriel");
		var nenya = scn.GetFreepsCard("nenya");
		var arwen = scn.GetFreepsCard("arwen");
		var cloak = scn.GetFreepsCard("cloak");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var gimli = scn.GetFreepsCard("gimli");
		var elrond = scn.GetFreepsCard("elrond");
		var poison = scn.GetShadowCard("poison");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(legolas, guardian, galadriel, arwen, lorienelf, gimli);
		scn.MoveAlliesToTable(elrond);
		// Galadriel bears an artifact, which is no possession; Arwen an [elven] possession, the Lórien Elf a
		// Shadow one.
		scn.AttachCardsTo(galadriel, nenya);
		scn.AttachCardsTo(arwen, cloak);
		scn.AttachCardsTo(lorienelf, poison);
		scn.MoveCardsToHand(armaments);
		assertAttachedTo(nenya, galadriel);
		assertAttachedTo(cloak, arwen);
		assertAttachedTo(poison, lorienelf);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(armaments));
		scn.FreepsPlayCard(armaments);
		// Elf companions, unique or not, bearing nothing or only an artifact
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, guardian, galadriel));
		// Elf companions bearing a possession of either side; a companion of another culture; an [elven] ally;
		// the Ring-bearer, a [shire] Hobbit.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arwen, lorienelf, gimli, elrond, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(galadriel);

		assertAttachedTo(armaments, galadriel);
		assertAttachedTo(nenya, galadriel);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ItsBearerIsNotOfferedForAnotherPossessionButMayStillBearAnArtifact() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armaments = scn.GetFreepsCard("armaments");
		var legolas = scn.GetFreepsCard("legolas");
		var guardian = scn.GetFreepsCard("guardian");
		var galadriel = scn.GetFreepsCard("galadriel");
		var nenya = scn.GetFreepsCard("nenya");
		var merry = scn.GetFreepsCard("merry");
		var cloak = scn.GetFreepsCard("cloak");
		var pipe = scn.GetFreepsCard("pipe");
		scn.MoveCompanionsToTable(legolas, guardian, galadriel, merry);
		scn.AttachCardsTo(galadriel, armaments);
		scn.MoveCardsToHand(cloak, pipe, nenya);
		assertAttachedTo(armaments, galadriel);

		scn.StartGame();

		// An [elven] possession: every companion may bear it but Galadriel, who bears the Armaments.
		scn.FreepsPlayCard(cloak);
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, guardian, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(galadriel));
		scn.FreepsChooseCard(legolas);
		assertAttachedTo(cloak, legolas);

		// A [shire] possession, the same.
		scn.FreepsPlayCard(pipe);
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, guardian, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(galadriel));
		scn.FreepsChooseCard(guardian);
		assertAttachedTo(pipe, guardian);

		// An artifact is no possession: Nenya goes on her (its only bearer, so unasked).
		assertTrue(scn.FreepsPlayAvailable(nenya));
		scn.FreepsPlayCard(nenya);
		assertAttachedTo(nenya, galadriel);
		assertAttachedTo(armaments, galadriel);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ItsBearerMayNotTakeUpAShadowPossessionEither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armaments = scn.GetFreepsCard("armaments");
		var legolas = scn.GetFreepsCard("legolas");
		var guardian = scn.GetFreepsCard("guardian");
		var poison = scn.GetShadowCard("poison");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCompanionsToTable(legolas, guardian);
		scn.AttachCardsTo(legolas, armaments);
		scn.MoveCardsToSupportArea(poison);
		assertAttachedTo(armaments, legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SUPPORT, poison);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(legolas, shelob);
		scn.FreepsResolveSkirmish(legolas);
		scn.FreepsPass();

		// Spider Poison goes to a companion skirmishing Shelob, and Legolas is that companion; he bears the
		// Armaments, so he is not one it can go to: the ability is offered and its threat paid for nothing.
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SUPPORT, poison);
		assertTrue(scn.ShadowActionAvailable(poison));
		scn.ShadowUseCardAction(poison);
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.SUPPORT, poison);
		assertNotAttachedTo(poison, legolas);
		assertAttachedTo(armaments, legolas);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnotherCompanionWithoutItMayTakeUpTheShadowPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armaments = scn.GetFreepsCard("armaments");
		var legolas = scn.GetFreepsCard("legolas");
		var guardian = scn.GetFreepsCard("guardian");
		var poison = scn.GetShadowCard("poison");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCompanionsToTable(legolas, guardian);
		scn.AttachCardsTo(legolas, armaments);
		scn.MoveCardsToSupportArea(poison);
		assertAttachedTo(armaments, legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SUPPORT, poison);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guardian, shelob);
		scn.FreepsResolveSkirmish(guardian);
		scn.FreepsPass();

		// The same board with the Armaments on somebody else: the Poison goes to the Guardian.
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SUPPORT, poison);
		assertTrue(scn.ShadowActionAvailable(poison));
		scn.ShadowUseCardAction(poison);
		assertEquals(0, scn.GetThreats());
		assertAttachedTo(poison, guardian);
		assertAttachedTo(armaments, legolas);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void BorneByAUniqueCompanionItBarsFreePeoplesArcheryAbilitiesAndTheMinionArcheryTotalIsMinus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetArcheryScenario();

		var armaments = scn.GetFreepsCard("armaments");
		var armaments2 = scn.GetFreepsCard("armaments2");
		var legolas = scn.GetFreepsCard("legolas");
		var guardian = scn.GetFreepsCard("guardian");
		var doubleshot = scn.GetFreepsCard("doubleshot");
		var marksman = scn.GetShadowCard("marksman");
		var marksman2 = scn.GetShadowCard("marksman2");
		var wreathed = scn.GetShadowCard("wreathed");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(legolas, guardian);
		scn.AttachCardsTo(legolas, armaments);
		// A second copy on a companion who is not unique: it is its own bearer that counts.
		scn.AttachCardsTo(guardian, armaments2);
		assertAttachedTo(armaments2, guardian);
		scn.MoveCardsToHand(doubleshot);
		scn.MoveCardsToSupportArea(wreathed);
		scn.MoveAlliesToTable(elrond);
		assertAttachedTo(armaments, legolas);

		scn.StartGame();
		// Only archery abilities are barred: Elrond's fellowship ability is offered.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(elrond));
		scn.MoveMinionsToTable(marksman, marksman2);
		// The move to site 2 adds 2 + 3 companions: Wreathed in Shadow can pay its (1).
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		// Legolas is unique and bears it: his own archery ability is barred.
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertFalse(scn.FreepsActionAvailable(legolas));
		// An archery event is no special ability.
		assertTrue(scn.FreepsPlayAvailable(doubleshot));
		// Two archer minions, -1 for the copy on Legolas and nothing for the copy on the Guardian
		assertEquals(1, scn.GetShadowArcheryTotal());

		// The Shadow player's archery ability is not barred.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertTrue(scn.ShadowActionAvailable(wreathed));
	}

	@Test
	public void BorneByACompanionWhoIsNotUniqueItBarsNoArcheryAbilityAndLeavesTheMinionArcheryTotal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetArcheryScenario();

		var armaments = scn.GetFreepsCard("armaments");
		var legolas = scn.GetFreepsCard("legolas");
		var guardian = scn.GetFreepsCard("guardian");
		var marksman = scn.GetShadowCard("marksman");
		var marksman2 = scn.GetShadowCard("marksman2");
		scn.MoveCompanionsToTable(legolas, guardian);
		scn.AttachCardsTo(guardian, armaments);
		assertAttachedTo(armaments, guardian);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman, marksman2);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsActionAvailable(legolas));
		// Two archer minions, nothing taken off
		assertEquals(2, scn.GetShadowArcheryTotal());

		assertEquals(0, scn.GetWoundsOn(legolas));
		scn.FreepsUseCardAction(legolas);
		scn.FreepsChooseCard(marksman);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	// Comprehensive Rules 5.0, "hinder": while Elven Armaments is hindered its text does not apply, so its bearer
	// may take up another possession.  Its text has a bar and no "discard" sentence, so when it is restored
	// nothing is discarded: "because it only has the cannot-bear clause and lacks a discard clause, it will have
	// to begrudgingly co-exist with other possessions played while it was hindered" (Ketura on Q-0223: B).  The
	// restore here is the Free Peoples player's own reconcile.
	@Test
	public void APossessionItsBearerTookUpWhileItWasHinderedStaysWhenItIsRestored() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRestoreScenario();

		var armaments = scn.GetFreepsCard("armaments");
		var legolas = scn.GetFreepsCard("legolas");
		var bow = scn.GetFreepsCard("bow");
		var cloak = scn.GetFreepsCard("cloak");
		scn.MoveCompanionsToTable(legolas);
		scn.AttachCardsTo(legolas, armaments);
		// The Cloak stays in hand, so that the reconcile has a discard to ask about
		scn.MoveCardsToHand(bow, cloak);
		assertAttachedTo(armaments, legolas);
		assertInZone(Zone.HAND, cloak);

		scn.StartGame();

		// "Bearer cannot bear any other possessions": Legolas is the only Elf
		assertEquals(6 + 2, scn.GetStrength(legolas));
		assertFalse(scn.FreepsPlayAvailable(bow));

		scn.HinderCard(armaments);
		scn.FreepsRefreshDecision();
		assertEquals(6, scn.GetStrength(legolas));
		assertTrue(scn.FreepsPlayAvailable(bow));
		scn.FreepsPlayCard(bow);
		assertAttachedTo(bow, legolas);
		assertTrue(scn.IsHindered(armaments));

		// No minion: the Shadow phase is followed by the regroup phase
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassRegroupActions();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.IsHindered(armaments));

		scn.FreepsChooseToStay();
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
		// Restored, with its bonus back, and both possessions still on him
		assertFalse(scn.IsHindered(armaments));
		assertEquals(6 + 2, scn.GetStrength(legolas));
		assertAttachedTo(armaments, legolas);
		assertAttachedTo(bow, legolas);
		assertInZone(Zone.HAND, cloak);
	}
}
