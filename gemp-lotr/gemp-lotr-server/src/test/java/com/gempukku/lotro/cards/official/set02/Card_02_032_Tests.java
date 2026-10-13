package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_032_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("brand", "2_32");
					put("brand2", "2_32");
					put("brand3", "2_32");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_96");     // Boromir, Lord of Gondor: [gondor] Man, strength 7, vitality 3; not overwhelmed unless his strength is tripled
					put("erland", "2_21");      // Erland, Advisor to Brand: a [gandalf] Man who is an ally, strength 5; his text is about the Free Peoples hand
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] companion who is a Hobbit; a skirmish ability used only on purpose
					put("sword", "1_112");      // Ranger's Sword: hand weapon, bearer must be Aragorn, strength +2 and damage +1

					put("lemenya", "1_232");    // Ulaire Lemenya, Lieutenant of Morgul: [wraith] Nazgul, strength 9, vitality 2; her text needs a [wraith] condition on a companion
					put("nelya", "2_84");       // Ulaire Nelya, Ringwraith in Twilight: [wraith] Nazgul, strength 10, vitality 3; nothing else below 2 burdens
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: the [raider] Nazgul, strength 12, fierce; his text needs 3 burdens
					put("creeper", "8_75");     // Morgul Creeper: a [wraith] minion who is an Orc, strength 8, vitality 2; its text needs 3 burdens
					put("men", "11_100");       // Strange-looking Men: [men] Man, strength 10, vitality 2. "Maneuver: Exert this minion to transfer a possession or condition borne by a character to another eligible bearer."
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FlamingBrandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Flaming Brand
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 1
		 * Game Text: Bearer must be a Man.
		 * 		This weapon may be borne in addition to 1 other hand weapon.
		 * 		Bearer is strength +2 and <b>damage +1</b> while skirmishing a Nazgûl.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("brand");

		assertEquals("Flaming Brand", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void CanBeBorneByAManOfAnyCultureCompanionOrAllyAndByNobodyElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brand = scn.GetFreepsCard("brand");
		var brand2 = scn.GetFreepsCard("brand2");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var erland = scn.GetFreepsCard("erland");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(aragorn, pippin);
		scn.MoveAlliesToTable(erland);
		scn.MoveCardsToHand(brand, brand2);

		scn.StartGame();

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(erland));
		assertTrue(scn.FreepsPlayAvailable(brand));

		scn.FreepsPlayCard(brand);
		// A [gondor] Man who is a companion and a [gandalf] Man who is an ally; not a [gondor] Hobbit, and not Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, erland));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertAttachedTo(brand, aragorn);
		// 8 printed + 1 from the brand
		assertEquals(9, scn.GetStrength(aragorn));

		scn.FreepsPlayCard(brand2);
		scn.FreepsChooseCard(erland);
		assertAttachedTo(brand2, erland);
		// 5 printed + 1 from the brand
		assertEquals(6, scn.GetStrength(erland));
		assertEquals(9, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FlamingBrandCanBePlayedOnManAlreadyBearingHandWeapon() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brand = scn.GetFreepsCard("brand");
		var brand2 = scn.GetFreepsCard("brand2");
		var sword = scn.GetFreepsCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var erland = scn.GetFreepsCard("erland");
		scn.MoveCardsToHand(brand, brand2);
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveAlliesToTable(erland);
		scn.AttachCardsTo(aragorn, sword);

		scn.StartGame();

		// 8 printed + 2 from the sword
		assertEquals(10, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsPlayAvailable(brand));
		scn.FreepsPlayCard(brand);
		// Aragorn, who bears a hand weapon, is offered beside the two Men who bear none.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir, erland));
		scn.FreepsChooseCard(aragorn);
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(brand, aragorn);
		assertEquals(11, scn.GetStrength(aragorn));

		// A second brand would be borne in addition to two other hand weapons: Aragorn is left out of the very
		// choice that offers the other two Men.
		assertTrue(scn.FreepsPlayAvailable(brand2));
		scn.FreepsPlayCard(brand2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, erland));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		scn.FreepsChooseCard(boromir);
		assertAttachedTo(brand2, boromir);
		assertEquals(11, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FlamingBrandCanBePlayedOnManAlreadyBearingAnotherBrand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brand = scn.GetFreepsCard("brand");
		var brand2 = scn.GetFreepsCard("brand2");
		var brand3 = scn.GetFreepsCard("brand3");
		var sword = scn.GetFreepsCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(brand2, brand3, sword);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, brand);

		scn.StartGame();

		// 8 printed + 1 from the brand
		assertEquals(9, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsPlayAvailable(brand2));
		scn.FreepsPlayCard(brand2);
		assertAttachedTo(brand, aragorn);
		assertAttachedTo(brand2, aragorn);
		assertEquals(10, scn.GetStrength(aragorn));

		// Aragorn is the only Man. Neither a third brand nor his own sword may be borne beside two brands.
		assertInZone(Zone.HAND, brand3, sword);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(brand3));
		assertFalse(scn.FreepsPlayAvailable(sword));
	}

	@Test
	public void RegularHandWeaponCanbePlayedonmanAlreadyBearingFlamingBrand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brand = scn.GetFreepsCard("brand");
		var brand2 = scn.GetFreepsCard("brand2");
		var sword = scn.GetFreepsCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(sword, brand2);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, brand);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(sword));
		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(brand, aragorn);
		// The sword's own cost
		assertEquals(1, scn.GetTwilight());
		// 8 printed + 1 from the brand + 2 from the sword
		assertEquals(11, scn.GetStrength(aragorn));

		// Aragorn is the only Man, and he now bears two hand weapons.
		assertInZone(Zone.HAND, brand2);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(brand2));
	}

	@Test
	public void BearerIsStrengthPlus2AndDamagePlus1OnlyWhileHeSkirmishesANazgul() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brand = scn.GetFreepsCard("brand");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, brand);

		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, nelya);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, lemenya},
				new PhysicalCardImpl[]{boromir, nelya});

		// Boromir, a Man with no brand, skirmishes a Nazgul: nothing for him. Aragorn is assigned to a Nazgul
		// whose skirmish has not begun: 8 printed + 1 from the brand, and no more.
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, nelya));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, lemenya));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertEquals(9, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		scn.PassSkirmishActions();

		// 8 printed + 1 from the brand + 2 against a Nazgul
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, lemenya));
		assertEquals(11, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		scn.PassSkirmishActions();

		// 11 against 9: she takes the wound of a lost skirmish and one more for damage +1, and has vitality 2.
		assertInDiscard(lemenya);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(9, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
	}

	@Test
	public void ANazgulOfAnotherCultureCountsAndAWraithOrcDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brand = scn.GetFreepsCard("brand");
		var brand2 = scn.GetFreepsCard("brand2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, brand);
		scn.AttachCardsTo(boromir, brand2);

		var khamul = scn.GetShadowCard("khamul");
		var creeper = scn.GetShadowCard("creeper");

		scn.StartGame();
		scn.MoveMinionsToTable(khamul, creeper);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, khamul},
				new PhysicalCardImpl[]{boromir, creeper});

		// Boromir's brand against a [wraith] Orc: 7 printed + 1 from the brand, and no more.
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, creeper));
		assertEquals(8, scn.GetStrength(boromir));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		scn.PassSkirmishActions();

		// Aragorn's brand against the [raider] Nazgul: 8 printed + 1 from the brand + 2.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, khamul));
		assertEquals(11, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(8, scn.GetStrength(boromir));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void StrangeLookingMenMayTransferItToAnyManFreePeoplesOrShadow() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brand = scn.GetFreepsCard("brand");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, brand);

		var men = scn.GetShadowCard("men");
		var brigand = scn.GetShadowCard("brigand");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(men, brigand, troop);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(9, scn.GetStrength(aragorn));
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(0, scn.GetWoundsOn(men));
		assertTrue(scn.ShadowActionAvailable(men));

		// The brand is the only possession or condition a character bears, so it is not asked for.
		scn.ShadowUseCardAction(men);
		assertEquals(1, scn.GetWoundsOn(men));
		// Any other Man: a Free Peoples companion, the Men themselves, another Shadow Man. Not an Orc, not a
		// Hobbit, and not the Man who bears it now.
		assertTrue(scn.ShadowHasCardChoiceAvailable(boromir, men, brigand));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, frodo, aragorn));
		scn.ShadowChooseCard(brigand);

		assertAttachedTo(brand, brigand);
		// 8 printed + 1 from the brand
		assertEquals(9, scn.GetStrength(brigand));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
