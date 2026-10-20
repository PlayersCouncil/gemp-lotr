package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.modifiers.VitalityModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_052_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hailing", "103_52");
					put("legion", "4_218");       // Desert Legion: unique Southron, cost 6, vitality 3, ambush (1); the rest of its text needs 4 burdens
					put("army", "5_70");          // Army of Haradrim: unique Southron, cost 7, vitality 4; a skirmish ability never used here
					put("lancers", "4_217");      // Desert Lancers: unique Southron, strength 11; a regroup ability never used here
					put("explorer", "4_250");     // Southron Explorer: Southron, not unique, cost 2, strength 5, vitality 3; a regroup ability never used here
					put("soldier", "4_254");      // Southron Soldier: Southron, not unique, strength 4, vitality 1; ambush (2) and no other text
					put("warrior", "4_222");      // Desert Warrior: Southron Man, not unique; only ever stacked here
					put("stalker", "103_49");     // Desert Wind Stalker: Southron with two dots, cost 3; when played, remove (3) or hinder it
					put("captain", "4_225");      // Easterling Captain: unique [raider] Man who is no Southron, cost 5; skirmish abilities that need burdens
					put("khamul", "103_61");      // Ulaire Attea, Khamul of the East: unique [raider] Nazgul, an Easterling and no Southron, strength 12, fierce; the rest of his text needs 3 burdens
					put("plunderer", "8_58");     // Corsair Plunderer: [raider] minion who is no Southron, not unique
					put("runner", "1_178");       // Goblin Runner: [moria] minion, not unique
					put("warmumak", "5_78");      // War Mumak: [raider] mount, not unique; bearer is strength +4 and fierce; may add (5) on an overwhelm
					put("mumakil", "8_64");       // Mumakil: [raider] mount, not unique; bearer is strength +3 and fierce (and damage +1 with initiative)
					put("fellbeast", "6_83");     // Fell Beast: [wraith] mount, bearer must be a Nazgul; bearer is strength +2 and fierce
					put("spear", "4_255");        // Southron Spear: [raider] possession, not unique
					put("harpoon", "103_57");     // Savage Harpoon: [raider] possession with two dots
					put("rearguard", "103_43");   // Bladetusk Rearguard: [raider] artifact, not unique
					put("matriarch", "103_42");   // Bladetusk Matriarch: unique [raider] artifact
					put("charger", "103_41");     // Bladetusk Charger: [raider] possession that its maneuver ability makes a fierce mounted Southron minion
					put("men", "4_238");          // Men of Harad: [raider] condition, not unique
					put("burn", "103_44");        // Burn and Pillage: [raider] event
					put("poison", "8_28");        // Spider Poison: [gollum] possession, not unique

					put("aragorn", "1_89");       // Aragorn, Ranger of the North: strength 8, vitality 4
					put("boromir", "1_97");       // Boromir, Son of Denethor: strength 7, vitality 3
					put("gimli", "1_13");         // Gimli, Son of Gloin: strength 6, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HailingfromtheFarSouthStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Hailing from the Far South
		 * Unique: true
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time you play a unique Southron, you may exert it to take a non-unique [raider] item or
		 * 	minion into hand from discard.
		 * 	Response: If your mounted Southron wins a skirmish, hinder this condition to make that Southron
		 * 	<b>relentless</b> until the regroup phase <i>(they participate in 1 extra round of skirmishes after fierce)</i>.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hailing");

		assertEquals("Hailing from the Far South", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedForTwoToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hailing = scn.GetShadowCard("hailing");
		scn.MoveCardsToHand(hailing);

		scn.StartGame();
		scn.SetTwilight(1);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 1 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.HAND, hailing);
		assertTrue(scn.ShadowPlayAvailable(hailing));

		scn.ShadowPlayCard(hailing);

		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, hailing);
		assertFalse(scn.IsHindered(hailing));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayingAUniqueSouthronMayExertItToTakeANonUniqueRaiderItemOrMinionFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hailing = scn.GetShadowCard("hailing");
		var legion = scn.GetShadowCard("legion");
		var army = scn.GetShadowCard("army");
		// What may be taken: a possession, an artifact, a Southron, a minion who is no Southron, and a
		// possession and a minion with two dots, which are not unique.
		var warmumak = scn.GetShadowCard("warmumak");
		var rearguard = scn.GetShadowCard("rearguard");
		var explorer = scn.GetShadowCard("explorer");
		var plunderer = scn.GetShadowCard("plunderer");
		var harpoon = scn.GetShadowCard("harpoon");
		var stalker = scn.GetShadowCard("stalker");
		// What may not: a unique [raider] artifact, a unique [raider] minion, a [raider] condition, a [raider]
		// event, a minion and a possession of other cultures.
		var matriarch = scn.GetShadowCard("matriarch");
		var captain = scn.GetShadowCard("captain");
		var men = scn.GetShadowCard("men");
		var burn = scn.GetShadowCard("burn");
		var runner = scn.GetShadowCard("runner");
		var poison = scn.GetShadowCard("poison");
		// Nor a [raider] possession that is in the draw deck, nor one in the other player's discard pile.
		var mumakil = scn.GetShadowCard("mumakil");
		var spear = scn.GetShadowCard("spear");
		var theirSpear = scn.GetFreepsCard("spear");

		scn.MoveCardsToSupportArea(hailing);
		scn.MoveCardsToHand(legion);
		scn.MoveCardsToDiscard(warmumak, rearguard, explorer, plunderer, harpoon, stalker,
				matriarch, captain, men, burn, runner, poison);
		scn.MoveCardsToDiscard(theirSpear);

		scn.StartGame();
		// A unique Southron who is already in play.
		scn.MoveMinionsToTable(army);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(13, scn.GetTwilight());
		assertInZone(Zone.DECK, mumakil, spear);
		assertEquals(12, scn.GetShadowDiscardCount());
		assertEquals(1, scn.GetShadowHandCount());

		scn.ShadowPlayCard(legion);

		// 13 - 6 to play - 2 roaming at site 2
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, legion);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hailing));
		assertEquals(0, scn.GetWoundsOn(legion));
		assertEquals(0, scn.GetWoundsOn(army));

		scn.ShadowAcceptOptionalTrigger();

		// The Southron that was played exerts, not the one that was there already.
		assertEquals(1, scn.GetWoundsOn(legion));
		assertEquals(0, scn.GetWoundsOn(army));
		assertTrue(scn.ShadowHasCardChoiceAvailable(warmumak, rearguard, explorer, plunderer, harpoon, stalker));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(matriarch, captain, men, burn, runner, poison, mumakil, spear, theirSpear));
		assertEquals(6, scn.ShadowGetCardChoiceCount());
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(rearguard);

		assertInZone(Zone.HAND, rearguard);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(11, scn.GetShadowDiscardCount());
		assertInZone(Zone.DISCARD, warmumak, explorer, plunderer, harpoon, stalker, theirSpear);
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesEverythingAsItWasAndASecondUniqueSouthronIsOfferedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hailing = scn.GetShadowCard("hailing");
		var legion = scn.GetShadowCard("legion");
		var army = scn.GetShadowCard("army");
		var stalker = scn.GetShadowCard("stalker");
		var warmumak = scn.GetShadowCard("warmumak");
		var matriarch = scn.GetShadowCard("matriarch");
		scn.MoveCardsToSupportArea(hailing);
		scn.MoveCardsToHand(legion, army);
		scn.MoveCardsToDiscard(stalker, warmumak, matriarch);

		scn.StartGame();
		scn.SetTwilight(14);
		scn.FreepsPass();

		// 14 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(17, scn.GetTwilight());

		scn.ShadowPlayCard(legion);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hailing));

		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(legion));
		assertEquals(3, scn.GetShadowDiscardCount());
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(army);

		// 17 - 6 - 2 roaming for the Legion - 7 - 2 roaming for the Army
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hailing));
		assertEquals(0, scn.GetWoundsOn(army));

		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(army));
		assertEquals(0, scn.GetWoundsOn(legion));
		assertTrue(scn.ShadowHasCardChoiceAvailable(stalker, warmumak));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(matriarch));

		scn.ShadowChooseCard(stalker);

		assertInZone(Zone.HAND, stalker);
		assertInZone(Zone.DISCARD, warmumak, matriarch);
		assertEquals(2, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedForASouthronThatIsNotUniqueNorForAUniqueRaiderManWhoIsNoSouthron() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hailing = scn.GetShadowCard("hailing");
		var explorer = scn.GetShadowCard("explorer");
		var stalker = scn.GetShadowCard("stalker");
		var captain = scn.GetShadowCard("captain");
		var legion = scn.GetShadowCard("legion");
		var warmumak = scn.GetShadowCard("warmumak");
		scn.MoveCardsToSupportArea(hailing);
		scn.MoveCardsToHand(explorer, stalker, captain, legion);
		scn.MoveCardsToDiscard(warmumak);

		scn.StartGame();
		scn.SetTwilight(24);
		scn.FreepsPass();

		// 24 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(27, scn.GetTwilight());

		// A Southron with no dot.
		scn.ShadowPlayCard(explorer);
		assertInZone(Zone.SHADOW_CHARACTERS, explorer);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A Southron with two dots, which is not unique.
		scn.ShadowPlayCard(stalker);
		scn.ShadowChoose("Remove (3)");
		assertInZone(Zone.SHADOW_CHARACTERS, stalker);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A unique [raider] Man who is an Easterling.
		scn.ShadowPlayCard(captain);
		assertInZone(Zone.SHADOW_CHARACTERS, captain);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		assertEquals(0, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(stalker));
		assertEquals(0, scn.GetWoundsOn(captain));
		assertInZone(Zone.DISCARD, warmumak);
		// 27 - 2 - 2 roaming for the Explorer - 3 - 2 roaming - 3 by its own text for the Stalker - 5 - 2 roaming
		// for the Captain
		assertEquals(8, scn.GetTwilight());

		// On the same board a unique Southron is answered.
		scn.ShadowPlayCard(legion);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hailing));
	}

	@Test
	public void NotOfferedWhenTheUniqueSouthronCannotExert() throws DecisionResultInvalidException, CardNotFoundException, InvalidCardDefinitionException {
		var scn = GetScenario();

		var hailing = scn.GetShadowCard("hailing");
		var legion = scn.GetShadowCard("legion");
		var army = scn.GetShadowCard("army");
		var warmumak = scn.GetShadowCard("warmumak");
		scn.MoveCardsToSupportArea(hailing);
		scn.MoveCardsToHand(legion, army);
		scn.MoveCardsToDiscard(warmumak);
		// Desert Legion arrives with a vitality of 1: 3 printed - 2
		scn.ApplyAdHocModifier(new VitalityModifier(null, scn.GenerateShadowFilter("name(Desert Legion)"), null, -2));

		scn.StartGame();
		scn.SetTwilight(14);
		scn.FreepsPass();

		scn.ShadowPlayCard(legion);

		assertInZone(Zone.SHADOW_CHARACTERS, legion);
		assertEquals(1, scn.GetVitality(legion));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(legion));
		assertInZone(Zone.DISCARD, warmumak);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A unique Southron who can exert is answered on the same board.
		scn.ShadowPlayCard(army);
		assertEquals(4, scn.GetVitality(army));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hailing));
	}

	@Test
	public void WithNothingInTheDiscardPileThatCouldBeTakenItIsStillOfferedAndTheSouthronExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hailing = scn.GetShadowCard("hailing");
		var legion = scn.GetShadowCard("legion");
		var matriarch = scn.GetShadowCard("matriarch");
		var captain = scn.GetShadowCard("captain");
		var men = scn.GetShadowCard("men");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(hailing);
		scn.MoveCardsToHand(legion);
		scn.MoveCardsToDiscard(matriarch, captain, men, runner);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(legion);

		// The exertion is a cost that can be paid, so the offer is made.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hailing));
		assertEquals(0, scn.GetWoundsOn(legion));

		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(legion));
		assertEquals(4, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, matriarch, captain, men, runner);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ResponseHindersThisConditionToMakeTheMountedSouthronThatWonRelentless() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var hailing = scn.GetShadowCard("hailing");
		var explorer = scn.GetShadowCard("explorer");
		var soldier = scn.GetShadowCard("soldier");
		var warmumak = scn.GetShadowCard("warmumak");
		var mumakil = scn.GetShadowCard("mumakil");
		scn.MoveCardsToSupportArea(hailing);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, soldier);
		// Two mounted Southrons.
		scn.AttachCardsTo(explorer, warmumak);
		scn.AttachCardsTo(soldier, mumakil);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, explorer);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// Strength 5 + 4 for the War Mumak
		assertEquals(9, scn.GetStrength(explorer));
		assertEquals(8, scn.GetStrength(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		scn.PassSkirmishActions();

		// Southron Explorer 9 beats Aragorn 8
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hailing));
		assertFalse(scn.IsHindered(hailing));
		assertFalse(scn.HasKeyword(explorer, Keyword.RELENTLESS));
		assertFalse(scn.HasKeyword(soldier, Keyword.RELENTLESS));

		scn.ShadowChooseOptionalTrigger(hailing);

		assertTrue(scn.IsHindered(hailing));
		assertTrue(scn.HasKeyword(explorer, Keyword.RELENTLESS));
		// The other mounted Southron did not win the skirmish.
		assertFalse(scn.HasKeyword(soldier, Keyword.RELENTLESS));
		assertFalse(scn.IsHindered(explorer));
		assertFalse(scn.IsHindered(warmumak));
		// Both mounts make their bearers fierce, so the fierce round is next.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ResponseIsOfferedWhenABladetuskThatIsAMountedSouthronMinionWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var hailing = scn.GetShadowCard("hailing");
		var charger = scn.GetShadowCard("charger");
		var warrior = scn.GetShadowCard("warrior");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveCardsToSupportArea(hailing, charger);
		scn.StackCardsOn(charger, warrior);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Bladetusk Charger: stacking the Explorer, the one Southron Man in play, makes it a fierce mounted
		// Southron minion. It bears no mount and is no Man.
		scn.ShadowUseCardAction(charger);
		assertInZone(Zone.SHADOW_CHARACTERS, charger);
		assertTrue(scn.HasKeyword(charger, Keyword.MOUNTED));
		assertTrue(scn.HasKeyword(charger, Keyword.SOUTHRON));
		// Strength 4 + 4 for each of the 2 Southrons stacked on it
		assertEquals(12, scn.GetStrength(charger));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, charger);
		// The Charger's ambush is not taken up.
		scn.ShadowDeclineOptionalTrigger();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Bladetusk Charger 12 beats Aragorn 8
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hailing));
		assertFalse(scn.IsHindered(hailing));
		assertFalse(scn.HasKeyword(charger, Keyword.RELENTLESS));

		scn.ShadowChooseOptionalTrigger(hailing);

		assertTrue(scn.IsHindered(hailing));
		assertTrue(scn.HasKeyword(charger, Keyword.RELENTLESS));
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWhenAMountedSouthronLosesNorWhenAnUnmountedSouthronOrAMountedEasterlingWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli);
		// A tie goes to the Shadow side, so Boromir is given the strength to beat a mounted Southron outright.
		scn.ApplyAdHocModifier(new StrengthModifier(null, boromir, null, 2));

		var hailing = scn.GetShadowCard("hailing");
		var explorer = scn.GetShadowCard("explorer");
		var lancers = scn.GetShadowCard("lancers");
		var khamul = scn.GetShadowCard("khamul");
		var mumakil = scn.GetShadowCard("mumakil");
		var fellbeast = scn.GetShadowCard("fellbeast");
		scn.MoveCardsToSupportArea(hailing);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, lancers, khamul);
		scn.AttachCardsTo(explorer, mumakil);
		// Every [raider] mount says "bearer must be a Southron". The one [raider] minion who is no Southron and
		// may be mounted is Khamul, a Nazgul and an Easterling, on a Nazgul's mount.
		scn.AttachCardsTo(khamul, fellbeast);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { boromir, explorer },
				new PhysicalCardImpl[] { gimli, lancers },
				new PhysicalCardImpl[] { aragorn, khamul }
		);

		// A mounted Southron loses: Boromir's 7 + 2 beats the Explorer's 5 + 3 for the Mumakil.
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(9, scn.GetStrength(boromir));
		assertEquals(8, scn.GetStrength(explorer));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		// A Southron who bears no mount wins: Desert Lancers 11 beat Gimli 6.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		// A mounted [raider] minion who is an Easterling and no Southron wins: Khamul's 12 + 2 for the Fell
		// Beast beats Aragorn 8.
		scn.FreepsResolveSkirmish(aragorn);
		assertAttachedTo(fellbeast, khamul);
		assertTrue(scn.HasKeyword(khamul, Keyword.EASTERLING));
		assertFalse(scn.HasKeyword(khamul, Keyword.SOUTHRON));
		assertEquals(14, scn.GetStrength(khamul));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		assertFalse(scn.IsHindered(hailing));
		assertFalse(scn.HasKeyword(explorer, Keyword.RELENTLESS));
		assertFalse(scn.HasKeyword(lancers, Keyword.RELENTLESS));
		assertFalse(scn.HasKeyword(khamul, Keyword.RELENTLESS));
		// The Mumakil makes its bearer fierce, and Khamul is fierce himself: the fierce round is next.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void RelentlessLastsThroughTheFierceAndRelentlessSkirmishesAndIsGoneAtTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var hailing = scn.GetShadowCard("hailing");
		var explorer = scn.GetShadowCard("explorer");
		var warmumak = scn.GetShadowCard("warmumak");
		scn.MoveCardsToSupportArea(hailing);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		scn.AttachCardsTo(explorer, warmumak);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, explorer);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// Southron Explorer 5 + 4 for the War Mumak beats Aragorn 8
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.HasKeyword(explorer, Keyword.RELENTLESS));

		scn.ShadowChooseOptionalTrigger(hailing);
		assertTrue(scn.IsHindered(hailing));
		assertTrue(scn.HasKeyword(explorer, Keyword.RELENTLESS));

		// The fierce round, which the mount brings about.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.HasKeyword(explorer, Keyword.FIERCE));
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, explorer);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertTrue(scn.HasKeyword(explorer, Keyword.RELENTLESS));
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		// The condition is hindered, so this second win is not answered.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		// The relentless round: a third assignment and a third skirmish.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.HasKeyword(explorer, Keyword.RELENTLESS));
		scn.PassAssignmentActions();
		assertTrue(scn.FreepsCanAssign(explorer));
		scn.FreepsAssignToMinions(aragorn, explorer);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertTrue(scn.HasKeyword(explorer, Keyword.RELENTLESS));
		scn.PassSkirmishActions();
		assertEquals(3, scn.GetWoundsOn(aragorn));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, explorer);
		assertFalse(scn.HasKeyword(explorer, Keyword.RELENTLESS));
		// The mount's own keyword is still there.
		assertTrue(scn.HasKeyword(explorer, Keyword.FIERCE));
		assertTrue(scn.IsHindered(hailing));
	}
}
