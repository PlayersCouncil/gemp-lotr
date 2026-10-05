package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_039_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("crown", "101_39");
					put("crown2", "101_39");
					put("crown3", "101_39");

					// Twilight Nazgul
					put("nelya", "2_84");       // Ulaire Nelya, Ringwraith in Twilight: strength 10, vitality 3; fierce and damage +1 only with 2 burdens or a twice-wounded Ring-bearer
					put("attea", "2_82");       // Ulaire Attea, The Easterling: strength 12, vitality 3; its text is for when it is played
					put("wraith", "101_40");    // Ringwraith in Twilight: strength 9, vitality 3; stronger for wounds on the Ring-bearer, and may make itself fierce at the start of the maneuver phase
					// Nazgul that are not twilight
					put("lemenya", "1_232");    // Ulaire Lemenya: strength 9, vitality 2; weakens characters bearing [ringwraith] conditions
					put("otsea", "1_235");      // Ulaire Otsea: strength 9, vitality 3; a Shadow phase ability
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgul, strength 12, vitality 3, fierce; the rest of its text needs 3 burdens
					put("nazgulsword", "1_218"); // Nazgul Sword: possession, bearer must be a Nazgul, strength +2; damage +1 only with 3 burdens
					put("cur", "7_189");        // Morgul Cur: a [ringwraith] Orc, stronger while a Nazgul can be spotted

					put("lost", "11_212");      // Lost in the Woods: [ringwraith] condition borne by a companion: "Each [ringwraith] minion skirmishing bearer is strength +1 for each forest site you can spot."
					put("legacy", "1_282");     // The Weight of a Legacy: [sauron] condition, plays on Aragorn, strength -1 and no other text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("rangersword", "1_112"); // Ranger's Sword: possession, strength +2, "He is damage +1."
					put("anduril", "7_79");     // Anduril, Flame of the West: artifact, strength +2, vitality +1, "He is damage +1"
					put("swordarm", "1_116");   // Swordarm of the White Tower: skirmish event, a [gondor] companion is strength +2
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: strength 6, vitality 3; "While skirmishing a Nazgul, Arwen is strength +3."
					put("gwemegil", "1_47");    // Gwemegil: possession, strength +2, "She is damage +1.", "Skirmish: Exert Arwen or discard 2 cards from hand to make her strength +1."
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, damage +1; "Skirmish: Exert Gimli to make him strength +2."
					put("battleaxe", "1_14");   // Gimli's Battle Axe: possession, strength +2, "He is damage +1."
					put("grimir", "1_17");      // Grimir, Dwarven Elder: ally, strength 3, home site 3
					put("dwarvenaxe", "1_9");   // Dwarven Axe: possession, strength +2; its text is for a minion that loses to its bearer
					put("theoden", "4_292");    // Theoden, Son of Thengel: strength 6, vitality 2; a regroup ability
					put("hearken", "4_94");     // Hearken to Me: [gandalf] condition, bearer must be Theoden, strength +2 and vitality +1; the rest of its text is about Shadow conditions on him
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PaleCrownStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Pale Crown
		 * Unique: False
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 1
		 * Type: Artifact
		 * Subtype: Helm
		 * Vitality: +1
		 * Game Text: Plays to your support area.
		 * 	While bearer is twilight, characters skirmishing bearer do not receive strength or damage bonuses from cards they bear.
		 * 	Skirmish: Remove (1) to transfer this artifact from your support area to a Nazgul.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("crown");

		assertEquals("Pale Crown", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getPossessionClasses().size());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HELM));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void PlaysFromHandToTheSupportAreaForOneTwilightWithNazgulOnTheTableAndOnNeither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crown = scn.GetShadowCard("crown");
		var nelya = scn.GetShadowCard("nelya");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(crown);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, lemenya);
		scn.SetTwilight(2);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 2 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.HAND, crown);
		assertEquals(3, scn.GetVitality(nelya));
		assertEquals(2, scn.GetVitality(lemenya));

		scn.ShadowPlayCard(crown);

		// No bearer is asked for: the game is straight back at the Shadow player's actions.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.SUPPORT, crown);
		assertNotAttachedTo(crown, nelya);
		assertNotAttachedTo(crown, lemenya);
		assertEquals(3, scn.GetVitality(nelya));
		assertEquals(2, scn.GetVitality(lemenya));
		assertEquals(4, scn.GetTwilight());
	}

	@Test
	public void TransferredToATwilightNazgulInHisSkirmishTheCrownTakesTheSwordsBonusesFromHisOpponent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var rangersword = scn.GetFreepsCard("rangersword");
		var wraith = scn.GetShadowCard("wraith");
		var crown = scn.GetShadowCard("crown");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, rangersword);

		scn.StartGame();
		scn.MoveMinionsToTable(wraith);
		scn.MoveCardsToSupportArea(crown);
		scn.SetTwilight(2);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPassCurrentPhaseAction();
		// The Ringwraith's own offer, at the start of the maneuver phase, to exert and become fierce
		scn.ShadowDeclineOptionalTrigger();
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(aragorn, wraith);
		scn.FreepsResolveSkirmish(aragorn);

		// The Crown is in the support area and has no bearer: 8 + 2 for the Sword, which also makes him damage +1.
		assertInZone(Zone.SUPPORT, crown);
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(9, scn.GetStrength(wraith));
		assertEquals(3, scn.GetVitality(wraith));
		// 2 set + 2 for Frodo and Aragorn + 2 for the shadow number of site 2
		assertEquals(6, scn.GetTwilight());

		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowActionAvailable(crown));
		scn.ShadowUseCardAction(crown);

		// The only Nazgul takes it. 1 is removed for the ability and nothing for the Crown's own cost.
		assertAttachedTo(crown, wraith);
		assertEquals(5, scn.GetTwilight());
		// The Crown is vitality +1.
		assertEquals(4, scn.GetVitality(wraith));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));

		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassCurrentPhaseActions();

		// 8 against 9, where 10 against 9 would have put two wounds on the Ringwraith.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(wraith));
		// The skirmish is over, and the Sword is a sword again.
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnANazgulWhoIsNotTwilightTheCrownLeavesHisOpponentsBonusesAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var rangersword = scn.GetFreepsCard("rangersword");
		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");
		var crown = scn.GetShadowCard("crown");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, rangersword);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, nelya);
		scn.AttachCardsTo(lemenya, crown);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();

		// The Crown's bearer is not twilight. A twilight Nazgul is on the table beside him, with no Crown and no skirmish.
		assertFalse(scn.HasKeyword(lemenya, Keyword.TWILIGHT));
		assertAttachedTo(crown, lemenya);
		assertTrue(scn.HasKeyword(nelya, Keyword.TWILIGHT));
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));

		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, lemenya));
		// 8 + 2 for the Sword, and its damage +1
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(0, scn.GetWoundsOn(lemenya));

		scn.PassCurrentPhaseActions();

		// 10 against 9: one wound for losing and one for damage +1. Vitality 2 + 1 for the Crown keeps him alive.
		assertEquals(2, scn.GetWoundsOn(lemenya));
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionAndAnAllyEachLoseOnlyWhatTheirOwnItemsAddAndOnlyWhileTheirOwnSkirmishIsFought() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		var swordarm = scn.GetFreepsCard("swordarm");
		var grimir = scn.GetFreepsCard("grimir");
		var dwarvenaxe = scn.GetFreepsCard("dwarvenaxe");
		var legacy = scn.GetShadowCard("legacy");
		var nelya = scn.GetShadowCard("nelya");
		var attea = scn.GetShadowCard("attea");
		var crown = scn.GetShadowCard("crown");
		var crown2 = scn.GetShadowCard("crown2");
		var nazgulsword = scn.GetShadowCard("nazgulsword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, anduril);
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(grimir, dwarvenaxe);

		scn.StartGame();
		// The skirmishes are at site 3, Grimir's home, where an ally may be assigned.
		scn.SkipToSite(2);
		scn.MoveCardsToHand(swordarm);
		scn.MoveMinionsToTable(nelya, attea);
		scn.AttachCardsTo(nelya, crown, nazgulsword);
		scn.AttachCardsTo(attea, crown2);
		scn.AttachCardsTo(aragorn, legacy);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(nelya, Keyword.TWILIGHT));
		assertTrue(scn.HasKeyword(attea, Keyword.TWILIGHT));
		// 8 + 2 for Anduril - 1 for The Weight of a Legacy
		assertEquals(9, scn.GetStrength(aragorn));
		// 4 + 1 for Anduril
		assertEquals(5, scn.GetVitality(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		// 3 + 2 for the Dwarven Axe
		assertEquals(5, scn.GetStrength(grimir));
		// 10 + 2 for the Nazgul Sword he bears himself
		assertEquals(12, scn.GetStrength(nelya));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, nelya}, new PhysicalCardImpl[]{grimir, attea});
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, nelya));
		// Anduril's +2 and its damage +1 are gone. Its vitality +1 is no strength or damage bonus, and
		// the Shadow condition's -1 is no bonus at all: 8 - 1
		assertEquals(7, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(5, scn.GetVitality(aragorn));
		// The Crown's bearer is in the skirmish too, but is not a character skirmishing the bearer:
		// the Sword he bears himself still gives him its +2.
		assertEquals(12, scn.GetStrength(nelya));
		// Grimir is assigned to the other crowned Nazgul and is not yet skirmishing him.
		assertTrue(scn.IsCharAssignedAgainst(grimir, attea));
		assertEquals(5, scn.GetStrength(grimir));

		// A bonus from an event is not from a card he bears: 7 + 2
		scn.FreepsPlayCard(swordarm);
		assertEquals(9, scn.GetStrength(aragorn));

		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassCurrentPhaseActions();
		// 9 against 12
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(nelya));

		scn.FreepsResolveSkirmish(grimir);

		assertTrue(scn.IsCharSkirmishingAgainst(grimir, attea));
		// The ally loses his axe's +2 in his turn,
		assertEquals(3, scn.GetStrength(grimir));
		// and Aragorn, whose skirmish is over, has Anduril's bonuses back: 8 + 2 - 1
		assertEquals(9, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WhatACharacterHasOfHisOwnIsKeptWhenHisWeaponsBonusesAreNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var gwemegil = scn.GetFreepsCard("gwemegil");
		var gimli = scn.GetFreepsCard("gimli");
		var battleaxe = scn.GetFreepsCard("battleaxe");
		var nelya = scn.GetShadowCard("nelya");
		var attea = scn.GetShadowCard("attea");
		var crown = scn.GetShadowCard("crown");
		var crown2 = scn.GetShadowCard("crown2");
		scn.MoveCompanionsToTable(arwen, gimli);
		scn.AttachCardsTo(arwen, gwemegil);
		scn.AttachCardsTo(gimli, battleaxe);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, attea);
		scn.AttachCardsTo(nelya, crown);
		scn.AttachCardsTo(attea, crown2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();

		// 6 + 2 for Gwemegil, which makes her damage +1
		assertEquals(8, scn.GetStrength(arwen));
		assertEquals(1, scn.GetKeywordCount(arwen, Keyword.DAMAGE));
		// 6 + 2 for the Battle Axe; damage +1 of his own and another from the Axe
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, nelya}, new PhysicalCardImpl[]{gimli, attea});
		scn.FreepsResolveSkirmish(arwen);

		// 6 + 3 from her own text for skirmishing a Nazgul, and nothing from the sword
		assertEquals(9, scn.GetStrength(arwen));
		assertEquals(0, scn.GetKeywordCount(arwen, Keyword.DAMAGE));
		// Gimli's skirmish has not begun.
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		scn.PassCurrentPhaseActions();
		// 9 against 10
		assertEquals(1, scn.GetWoundsOn(arwen));

		scn.FreepsResolveSkirmish(gimli);

		assertEquals(6, scn.GetStrength(gimli));
		// The damage +1 printed on Gimli himself stays; the Axe's does not.
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		// Arwen is out of her skirmish and has her sword's bonuses again.
		assertEquals(8, scn.GetStrength(arwen));
		assertEquals(1, scn.GetKeywordCount(arwen, Keyword.DAMAGE));

		// Gimli's own ability: he exerts and is strength +2.
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsUseCardAction(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(8, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AStrengthBonusBoughtFromTheWeaponSheBearsIsNotReceivedEither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var gwemegil = scn.GetFreepsCard("gwemegil");
		var nelya = scn.GetShadowCard("nelya");
		var crown = scn.GetShadowCard("crown");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, gwemegil);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		scn.MoveCardsToSupportArea(crown);
		scn.SetTwilight(2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(arwen, nelya);
		scn.FreepsResolveSkirmish(arwen);

		// 6 + 3 for skirmishing a Nazgul + 2 for Gwemegil
		assertEquals(11, scn.GetStrength(arwen));
		assertEquals(0, scn.GetWoundsOn(arwen));

		// Gwemegil's ability, with no cards in hand to discard: Arwen exerts and is strength +1.
		assertEquals(0, scn.GetFreepsHandCount());
		scn.FreepsUseCardAction(gwemegil);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(12, scn.GetStrength(arwen));

		scn.ShadowUseCardAction(crown);
		assertAttachedTo(crown, nelya);
		// Both of the sword's bonuses go, the +1 already bought as well as the +2: 6 + 3
		assertEquals(9, scn.GetStrength(arwen));

		// The cost can still be paid, and what it buys comes from a card she bears.
		scn.FreepsUseCardAction(gwemegil);
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(9, scn.GetStrength(arwen));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	@Ignore("F-0038: the crowned Nazgul loses a strength bonus that a condition borne by the character it is skirmishing gives to the Nazgul")
	public void TheCrownedNazgulKeepsTheStrengthBonusItGetsFromAConditionItsOpponentBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var rangersword = scn.GetFreepsCard("rangersword");
		var nelya = scn.GetShadowCard("nelya");
		var crown = scn.GetShadowCard("crown");
		var lost = scn.GetShadowCard("lost");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, rangersword);

		scn.StartGame();
		// Lost in the Woods counts forest sites, and this path has none.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, CardType.SITE, null, Keyword.FOREST));
		scn.MoveMinionsToTable(nelya);
		scn.MoveCardsToSupportArea(crown);
		scn.AttachCardsTo(aragorn, lost);
		scn.SetTwilight(2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();

		assertEquals(10, scn.GetStrength(nelya));

		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.FreepsResolveSkirmish(aragorn);

		// Sites 1 and 2 are the forest sites in play: 10 + 2
		assertEquals(12, scn.GetStrength(nelya));
		assertEquals(10, scn.GetStrength(aragorn));

		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowUseCardAction(crown);

		assertAttachedTo(crown, nelya);
		// Aragorn is the character skirmishing the bearer, and he loses his Sword's +2.
		assertEquals(8, scn.GetStrength(aragorn));
		// The Nazgul is the bearer. What Lost in the Woods gives him is not a bonus Aragorn receives.
		assertEquals(12, scn.GetStrength(nelya));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionOffersEveryNazgulWithoutAHelmAndMovesTheCopyWhoseActionItWas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var nelya = scn.GetShadowCard("nelya");
		var lemenya = scn.GetShadowCard("lemenya");
		var otsea = scn.GetShadowCard("otsea");
		var cur = scn.GetShadowCard("cur");
		var crown = scn.GetShadowCard("crown");
		var crown2 = scn.GetShadowCard("crown2");
		var crown3 = scn.GetShadowCard("crown3");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, lemenya, otsea, cur);
		scn.MoveCardsToSupportArea(crown, crown2);
		scn.AttachCardsTo(otsea, crown3);
		scn.SetTwilight(1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The phase before: nothing is offered.
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(crown));
		assertFalse(scn.ShadowActionAvailable(crown2));

		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		// 1 set + 2 for Frodo and Aragorn + 2 for the shadow number of site 2
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, crown, crown2);
		assertEquals(2, scn.GetVitality(lemenya));
		assertTrue(scn.ShadowActionAvailable(crown));
		assertTrue(scn.ShadowActionAvailable(crown2));

		scn.ShadowUseCardAction(crown);

		assertEquals(4, scn.GetTwilight());
		// The twilight Nazgul who is in the skirmish, and one who is neither twilight nor skirmishing
		assertTrue(scn.ShadowHasCardChoiceAvailable(nelya, lemenya));
		// A Nazgul already wearing a helm, and a [ringwraith] Orc
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(otsea, cur));
		scn.ShadowChooseCard(lemenya);

		assertAttachedTo(crown, lemenya);
		assertInZone(Zone.SUPPORT, crown2);
		assertAttachedTo(crown3, otsea);
		// The Crown is vitality +1.
		assertEquals(3, scn.GetVitality(lemenya));
		assertEquals(3, scn.GetVitality(nelya));
		assertEquals(4, scn.GetTwilight());

		// The copy still in the support area is offered again; then the skirmish is left to resolve.
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowActionAvailable(crown2));
		scn.ShadowPassCurrentPhaseAction();

		// The phase after: a Crown in the support area, a Nazgul with no helm and twilight to pay, and no offer.
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertInZone(Zone.SUPPORT, crown2);
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, nelya);
		assertFalse(scn.ShadowActionAvailable(crown2));
	}

	@Test
	public void SkirmishActionIsNotOfferedWithoutATwilightTokenToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var nelya = scn.GetShadowCard("nelya");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var crown = scn.GetShadowCard("crown");
		var crown2 = scn.GetShadowCard("crown2");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, lemenya, khamul);
		scn.MoveCardsToSupportArea(crown, crown2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		// Exactly enough for one use
		scn.SetTwilight(1);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(crown));
		assertTrue(scn.ShadowActionAvailable(crown2));

		scn.ShadowUseCardAction(crown);
		// Two [ringwraith] Nazgul and a [raider] one: the text names the race and no culture.
		assertTrue(scn.ShadowHasCardChoiceAvailable(nelya, lemenya, khamul));
		scn.ShadowChooseCard(nelya);
		assertAttachedTo(crown, nelya);
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPassCurrentPhaseAction();

		// The second Crown is still in the support area and Lemenya has no helm; only the twilight is missing.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertInZone(Zone.SUPPORT, crown2);
		assertNotAttachedTo(crown, lemenya);
		assertFalse(scn.ShadowActionAvailable(crown2));
	}

	@Test
	public void OnceOnANazgulTheCrownsSkirmishActionStillTakesItsTwilightTokenAndTransfersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var nelya = scn.GetShadowCard("nelya");
		var otsea = scn.GetShadowCard("otsea");
		var crown3 = scn.GetShadowCard("crown3");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, otsea);
		scn.AttachCardsTo(otsea, crown3);
		scn.SetTwilight(2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		// 2 set + 2 for Frodo and Aragorn + 2 for the shadow number of site 2
		assertEquals(6, scn.GetTwilight());
		// The only Crown is on Otsea and not in the support area. Nelya, who is in the skirmish, has no helm.
		assertAttachedTo(crown3, otsea);
		assertEquals(4, scn.GetVitality(otsea));
		assertEquals(3, scn.GetVitality(nelya));

		// Removing (1) is the whole of the cost, and it can be paid, so the ability can be used.
		assertTrue(scn.ShadowActionAvailable(crown3));
		scn.ShadowUseCardAction(crown3);

		assertEquals(5, scn.GetTwilight());
		// The transfer is from the support area, and this Crown is not there: it stays where it is.
		assertAttachedTo(crown3, otsea);
		assertNotAttachedTo(crown3, nelya);
		assertEquals(4, scn.GetVitality(otsea));
		assertEquals(3, scn.GetVitality(nelya));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheStrengthBonusOfAConditionHeBearsIsNotReceivedEither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var hearken = scn.GetFreepsCard("hearken");
		var nelya = scn.GetShadowCard("nelya");
		var crown = scn.GetShadowCard("crown");
		scn.MoveCompanionsToTable(theoden);
		scn.AttachCardsTo(theoden, hearken);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		scn.AttachCardsTo(nelya, crown);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.HasKeyword(nelya, Keyword.TWILIGHT));
		assertAttachedTo(hearken, theoden);
		// 6 + 2 for Hearken to Me, a condition and no item
		assertEquals(8, scn.GetStrength(theoden));
		// 2 + 1 for Hearken to Me
		assertEquals(3, scn.GetVitality(theoden));

		scn.FreepsAssignToMinions(theoden, nelya);
		scn.FreepsResolveSkirmish(theoden);

		assertTrue(scn.IsCharSkirmishingAgainst(theoden, nelya));
		// A condition he bears is a card he bears: its +2 is gone. Its vitality +1 is not a strength bonus.
		assertEquals(6, scn.GetStrength(theoden));
		assertEquals(3, scn.GetVitality(theoden));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
