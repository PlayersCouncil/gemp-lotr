package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_032_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("brand", "52_32");
					put("brand2", "52_32");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man ranger; a maneuver ability only used on purpose
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf ranger; strength +3 only while skirmishing a Nazgul
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion who is not a ranger
					put("sword", "1_112");      // Ranger's Sword: hand weapon, bearer must be Aragorn, strength +2 and damage +1

					put("attea", "1_229");      // Ulaire Attea, Keeper of Dol Guldur: [wraith] Nazgul, strength 12, fierce; a pump that needs twilight
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: [raider] Nazgul, strength 12, fierce; text needs 3 burdens
					put("ruffian", "7_198");    // Morgul Ruffian: [wraith] Orc, strength 7; not a Nazgul
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
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +1
		 * Game Text: Bearer must be a ranger. This weapon may be borne in addition to 1 other hand weapon.
		 * 	Skirmish: Make bearer strength +2 and <b>damage +1</b> while bearing this item and skirmishing a Nazgûl
		 * 	until the regroup phase (limit once per turn). Discard this possession at the start of the regroup phase.
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
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void CanBeBorneByARangerOfAnyCultureButNotByAnotherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brand = scn.GetFreepsCard("brand");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, arwen, boromir);
		scn.MoveCardsToHand(brand);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsPlayAvailable(brand));

		scn.FreepsPlayCard(brand);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir));
		scn.FreepsChooseCard(aragorn);

		assertAttachedTo(brand, aragorn);
		assertEquals(1, scn.GetTwilight());
		// 8 printed + 1 from the brand
		assertEquals(9, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void MayBeBorneBesideOneOtherHandWeaponButNotBesideTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brand = scn.GetFreepsCard("brand");
		var brand2 = scn.GetFreepsCard("brand2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveCardsToHand(brand, brand2);

		scn.StartGame();

		// 8 printed + 2 from the sword
		assertEquals(10, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsPlayAvailable(brand));

		scn.FreepsPlayCard(brand);
		assertAttachedTo(brand, aragorn);
		assertAttachedTo(sword, aragorn);
		assertEquals(11, scn.GetStrength(aragorn));

		// A second brand would be borne beside two other hand weapons, and Aragorn is the only ranger.
		assertFalse(scn.FreepsPlayAvailable(brand2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TwoBrandsMayBeBorneTogetherButNoThirdHandWeaponBesideThem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brand = scn.GetFreepsCard("brand");
		var brand2 = scn.GetFreepsCard("brand2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(brand, brand2, sword);

		scn.StartGame();

		scn.FreepsPlayCard(brand);
		assertAttachedTo(brand, aragorn);
		// Either a sword or a second brand may now join the first brand.
		assertTrue(scn.FreepsPlayAvailable(sword));
		assertTrue(scn.FreepsPlayAvailable(brand2));

		scn.FreepsPlayCard(brand2);
		assertAttachedTo(brand, aragorn);
		assertAttachedTo(brand2, aragorn);
		// 8 printed + 1 + 1
		assertEquals(10, scn.GetStrength(aragorn));
		assertFalse(scn.FreepsPlayAvailable(sword));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishActionMakesBearerStrongerAndDamagePlus1AgainstANazgulUntilRegroupThenDiscardsTheBrand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brand = scn.GetFreepsCard("brand");
		var brand2 = scn.GetFreepsCard("brand2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var attea = scn.GetShadowCard("attea");
		scn.MoveCompanionsToTable(aragorn, arwen);
		scn.AttachCardsTo(aragorn, brand);
		// A second brand that is never used, on a companion who is not assigned.
		scn.AttachCardsTo(arwen, brand2);

		scn.StartGame();
		scn.MoveMinionsToTable(attea);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(brand));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(aragorn, attea);
		scn.FreepsResolveSkirmish(aragorn);

		assertAttachedTo(brand, aragorn);
		assertEquals(9, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(7, scn.GetStrength(arwen));
		assertTrue(scn.FreepsActionAvailable(brand));

		scn.FreepsUseCardAction(brand);
		// 8 printed + 1 from the brand + 2
		assertEquals(11, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(7, scn.GetStrength(arwen));
		assertEquals(0, scn.GetKeywordCount(arwen, Keyword.DAMAGE));

		// 11 against 12: Aragorn loses and takes 1 wound, and Attea is fierce.
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(aragorn, attea);
		scn.FreepsResolveSkirmish(aragorn);

		// A later skirmish phase of the same turn: the bonus holds, and the action is not offered again.
		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertAttachedTo(brand, aragorn);
		assertEquals(11, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(brand));

		scn.PassCurrentPhaseActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertInDiscard(brand);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		// The brand that was not used stays.
		assertAttachedTo(brand2, arwen);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BonusHoldsOnlyWhileTheBearerBearsTheBrandAndSkirmishesANazgul() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brand = scn.GetFreepsCard("brand");
		var brand2 = scn.GetFreepsCard("brand2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var khamul = scn.GetShadowCard("khamul");
		var ruffian = scn.GetShadowCard("ruffian");
		scn.MoveCompanionsToTable(aragorn, arwen);
		scn.AttachCardsTo(aragorn, brand);
		scn.AttachCardsTo(arwen, brand2);

		scn.StartGame();
		scn.MoveMinionsToTable(khamul, ruffian);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, khamul},
				new PhysicalCardImpl[]{arwen, ruffian});
		scn.FreepsResolveSkirmish(arwen);

		// Arwen's skirmish, against an Orc of the [wraith] culture. Aragorn is assigned to a Nazgul but not yet skirmishing.
		assertEquals(9, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(arwen));
		scn.FreepsUseCardAction(brand);
		assertEquals(9, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(7, scn.GetStrength(arwen));
		assertEquals(0, scn.GetKeywordCount(arwen, Keyword.DAMAGE));

		scn.ShadowPass();
		scn.FreepsUseCardAction(brand2);
		assertEquals(7, scn.GetStrength(arwen));
		assertEquals(0, scn.GetKeywordCount(arwen, Keyword.DAMAGE));

		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(aragorn);

		// Aragorn's own skirmish, against the [raider] Nazgul: the bonus from the earlier use applies now.
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, khamul));
		// 8 printed + 1 from the brand + 2
		assertEquals(11, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertFalse(scn.FreepsActionAvailable(brand));

		// Once he no longer bears the brand, the bonus is gone with the brand's own +1.
		scn.MoveCardsToDiscard(brand);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnyRangerBearingTheBrandGetsTheBonusAgainstANazgulAndNoOtherCharacterDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brand = scn.GetFreepsCard("brand");
		var brand2 = scn.GetFreepsCard("brand2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var attea = scn.GetShadowCard("attea");
		var ruffian = scn.GetShadowCard("ruffian");
		scn.MoveCompanionsToTable(aragorn, arwen);
		scn.AttachCardsTo(arwen, brand);
		scn.AttachCardsTo(aragorn, brand2);

		scn.StartGame();
		scn.MoveMinionsToTable(attea, ruffian);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{arwen, attea},
				new PhysicalCardImpl[]{aragorn, ruffian});
		scn.FreepsResolveSkirmish(arwen);

		// Arwen, an [elven] Elf, is skirmishing a Nazgul: 6 printed + 1 from her brand + 3 of her own.
		// Aragorn is assigned to an Orc and not skirmishing: 8 printed + 1 from his brand.
		assertTrue(scn.IsCharSkirmishingAgainst(arwen, attea));
		assertEquals(10, scn.GetStrength(arwen));
		assertEquals(9, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(arwen, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));

		// Aragorn's brand, used while someone else skirmishes a Nazgul, gives nothing to him or to her.
		scn.FreepsUseCardAction(brand2);
		assertEquals(9, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(10, scn.GetStrength(arwen));
		assertEquals(0, scn.GetKeywordCount(arwen, Keyword.DAMAGE));

		scn.ShadowPass();
		scn.FreepsUseCardAction(brand);
		// 10 + 2
		assertEquals(12, scn.GetStrength(arwen));
		assertEquals(1, scn.GetKeywordCount(arwen, Keyword.DAMAGE));
		assertEquals(9, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));

		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
