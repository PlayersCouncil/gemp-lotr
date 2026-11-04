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

public class Card_18_121_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pikes1", "18_121");
					put("pikes2", "18_121");

					put("uruk1", "18_131");     // White Hand Uruk: [uruk-hai] Uruk-hai, strength 10, vitality 2; a requirement to play, cheated in
					put("uruk2", "18_131");
					put("uruk3", "18_131");
					put("uruk4", "18_131");
					put("saruman", "17_116");   // Saruman, Master of the White Hand: an [uruk-hai] minion who is no Uruk-hai, strength 8, vitality 4; a skirmish ability used only on purpose
					put("staff", "13_81");      // Staff of Saruman: an [isengard] ARTIFACT (staff, strength +2, vitality +1) on a Wizard; its regroup ability is never reached
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, strength 9, damage +1 and nothing else

					put("spear", "17_117");     // Spear of the White Hand: [uruk-hai] hand weapon, strength +1, no text
					put("isword", "11_192");    // Isengard Sword: [uruk-hai] hand weapon on an [uruk-hai] minion; each character skirmishing bearer is strength -2
					put("usword1", "1_160");    // Uruk-hai Sword: [isengard] hand weapon on any Uruk-hai, strength +2; its trigger answers a skirmish its bearer wins
					put("usword2", "1_160");
					put("siegebow", "11_191");  // Isengard Siege Bow: [uruk-hai] ranged weapon, vitality +1; an archer only at a battleground, which no site here is
					put("fury", "18_116");      // Fury of the Evil Army: an [uruk-hai] condition borne by an [uruk-hai] minion, strength +1; its trigger answers a skirmish its bearer wins

					put("legolas", "1_51");     // Legolas, Prince of Mirkwood: an archer, and nothing else outside a Nazgul's skirmish
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf, strength 6, vitality 3; a skirmish ability used only on purpose
					put("baruk", "5_5");        // Baruk Khazad: Maneuver event, exert a Dwarf to wound a minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PikesUponPikesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Pikes Upon Pikes
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each [uruk-hai] minion bearing a hand weapon cannot take wounds during the archery phase.<br><b>Skirmish:</b> Discard this condition from play to make an [uruk-hai] minion bearing a possession strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pikes1");

		assertEquals("Pikes Upon Pikes", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void UrukHaiMinionsBearingAHandWeaponCannotTakeArcheryWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pikes1 = scn.GetShadowCard("pikes1");
		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk2 = scn.GetShadowCard("uruk2");
		var uruk3 = scn.GetShadowCard("uruk3");
		var uruk4 = scn.GetShadowCard("uruk4");
		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetShadowCard("guard");
		var spear = scn.GetShadowCard("spear");
		var isword = scn.GetShadowCard("isword");
		var usword1 = scn.GetShadowCard("usword1");
		var usword2 = scn.GetShadowCard("usword2");
		var siegebow = scn.GetShadowCard("siegebow");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.MoveCardsToSupportArea(pikes1);
		scn.MoveMinionsToTable(uruk1, uruk2, uruk3, uruk4, saruman, guard);
		scn.AttachCardsTo(uruk1, spear);
		scn.AttachCardsTo(saruman, isword);
		scn.AttachCardsTo(uruk2, usword1);
		scn.AttachCardsTo(uruk3, siegebow);
		scn.AttachCardsTo(guard, usword2);
		assertInZone(Zone.SUPPORT, pikes1);
		assertAttachedTo(spear, uruk1);
		assertAttachedTo(isword, saruman);
		assertAttachedTo(usword1, uruk2);
		assertAttachedTo(siegebow, uruk3);
		assertAttachedTo(usword2, guard);

		// Legolas's archery: one wound, which the Shadow player places on one of his minions.
		scn.SkipToArcheryWounds();
		assertEquals(0, scn.GetWoundsOn(uruk3));
		assertEquals(0, scn.GetWoundsOn(uruk4));

		// [uruk-hai] minions of either race bearing a hand weapon of either culture are not offered; one bearing a
		// ranged weapon, one bearing nothing and the [sauron] Uruk-hai bearing a hand weapon are.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(uruk1, saruman, uruk2));
		assertTrue(scn.ShadowHasCardChoiceAvailable(uruk3, uruk4, guard));
		scn.ShadowChooseCard(uruk3);

		assertEquals(1, scn.GetWoundsOn(uruk3));
		assertEquals(0, scn.GetWoundsOn(uruk4));
		assertEquals(0, scn.GetWoundsOn(uruk1));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(uruk2));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnUrukHaiMinionBearingAHandWeaponTakesAWoundInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pikes1 = scn.GetShadowCard("pikes1");
		var uruk1 = scn.GetShadowCard("uruk1");
		var saruman = scn.GetShadowCard("saruman");
		var spear = scn.GetShadowCard("spear");
		var isword = scn.GetShadowCard("isword");
		var gimli = scn.GetFreepsCard("gimli");
		var baruk = scn.GetFreepsCard("baruk");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(baruk);
		assertInHand(baruk);

		scn.StartGame();
		scn.MoveCardsToSupportArea(pikes1);
		scn.MoveMinionsToTable(uruk1, saruman);
		scn.AttachCardsTo(uruk1, spear);
		scn.AttachCardsTo(saruman, isword);
		assertInZone(Zone.SUPPORT, pikes1);
		assertAttachedTo(spear, uruk1);
		assertAttachedTo(isword, saruman);

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(uruk1));
		assertEquals(0, scn.GetWoundsOn(gimli));

		scn.FreepsPlayCard(baruk);
		assertEquals(1, scn.GetWoundsOn(gimli));
		// Outside the archery phase both are offered, and the one chosen takes the wound.
		assertTrue(scn.FreepsHasCardChoiceAvailable(uruk1, saruman));
		scn.FreepsChooseCard(uruk1);

		assertEquals(1, scn.GetWoundsOn(uruk1));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsThisCopyToMakeAnUrukHaiMinionBearingAPossessionStrongerForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pikes1 = scn.GetShadowCard("pikes1");
		var pikes2 = scn.GetShadowCard("pikes2");
		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk2 = scn.GetShadowCard("uruk2");
		var uruk3 = scn.GetShadowCard("uruk3");
		var uruk4 = scn.GetShadowCard("uruk4");
		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetShadowCard("guard");
		var spear = scn.GetShadowCard("spear");
		var isword = scn.GetShadowCard("isword");
		var usword2 = scn.GetShadowCard("usword2");
		var siegebow = scn.GetShadowCard("siegebow");
		var fury = scn.GetShadowCard("fury");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveCardsToSupportArea(pikes1, pikes2);
		scn.MoveMinionsToTable(uruk1, uruk2, uruk3, uruk4, saruman, guard);
		scn.AttachCardsTo(uruk1, spear);
		scn.AttachCardsTo(saruman, isword);
		scn.AttachCardsTo(uruk3, siegebow);
		scn.AttachCardsTo(uruk2, fury);
		scn.AttachCardsTo(guard, usword2);
		assertInZone(Zone.SUPPORT, pikes1, pikes2);
		assertAttachedTo(spear, uruk1);
		assertAttachedTo(isword, saruman);
		assertAttachedTo(siegebow, uruk3);
		assertAttachedTo(fury, uruk2);
		assertAttachedTo(usword2, guard);

		// The assignment phase, next door to the skirmish: not offered.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(pikes1));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, uruk1}, new PhysicalCardImpl[]{gimli, saruman});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(pikes1));
		scn.ShadowUseCardAction(pikes1);

		// The copy used is the cost; the other stays.
		assertInDiscard(pikes1);
		assertInZone(Zone.SUPPORT, pikes2);

		// An [uruk-hai] minion bearing a possession of either class, in this skirmish, in one still to come or in
		// none; not one bearing a condition, one bearing nothing, nor the [sauron] Uruk-hai bearing a possession.
		assertTrue(scn.ShadowHasCardChoiceAvailable(uruk1, saruman, uruk3));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(uruk2, uruk4, guard));
		assertEquals(1, scn.ShadowGetChoiceMin());

		assertEquals(8, scn.GetStrength(saruman));
		// 10 + 1 for the Spear.
		assertEquals(11, scn.GetStrength(uruk1));
		scn.ShadowChooseCard(saruman);

		assertEquals(10, scn.GetStrength(saruman));
		assertEquals(11, scn.GetStrength(uruk1));

		// Aragorn's skirmish was the phase the +2 lasted for: in Saruman's own it is gone.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, saruman));
		assertEquals(8, scn.GetStrength(saruman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnUrukHaiMinionBearingAnArtifactAndNoPossessionIsNotChosenForTheSkirmishAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pikes1 = scn.GetShadowCard("pikes1");
		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk3 = scn.GetShadowCard("uruk3");
		var saruman = scn.GetShadowCard("saruman");
		var spear = scn.GetShadowCard("spear");
		var siegebow = scn.GetShadowCard("siegebow");
		var staff = scn.GetShadowCard("staff");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveCardsToSupportArea(pikes1);
		scn.MoveMinionsToTable(uruk1, uruk3, saruman);
		scn.AttachCardsTo(uruk1, spear);
		scn.AttachCardsTo(uruk3, siegebow);
		scn.AttachCardsTo(saruman, staff);
		assertInZone(Zone.SUPPORT, pikes1);
		assertAttachedTo(spear, uruk1);
		assertAttachedTo(siegebow, uruk3);
		assertAttachedTo(staff, saruman);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, uruk1);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(pikes1));
		scn.ShadowUseCardAction(pikes1);

		// An artifact is an item but no possession: Saruman bears the Staff and is not offered.
		assertTrue(scn.ShadowHasCardChoiceAvailable(uruk1, uruk3));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(saruman));

		// 8 + 2 for the Staff.
		assertEquals(10, scn.GetStrength(saruman));
		assertEquals(10, scn.GetStrength(uruk3));
		scn.ShadowChooseCard(uruk3);

		assertEquals(10, scn.GetStrength(saruman));
		assertEquals(12, scn.GetStrength(uruk3));
		assertEquals(11, scn.GetStrength(uruk1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoUrukHaiMinionBearingAPossessionTheSkirmishAbilityDiscardsThisForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pikes1 = scn.GetShadowCard("pikes1");
		var uruk4 = scn.GetShadowCard("uruk4");
		var guard = scn.GetShadowCard("guard");
		var usword2 = scn.GetShadowCard("usword2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveCardsToSupportArea(pikes1);
		scn.MoveMinionsToTable(uruk4, guard);
		scn.AttachCardsTo(guard, usword2);
		assertInZone(Zone.SUPPORT, pikes1);
		assertAttachedTo(usword2, guard);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, uruk4);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(10, scn.GetStrength(uruk4));
		// 9 + 2 for the Uruk-hai Sword.
		assertEquals(11, scn.GetStrength(guard));

		assertTrue(scn.ShadowActionAvailable(pikes1));
		scn.ShadowUseCardAction(pikes1);

		assertInDiscard(pikes1);
		assertEquals(10, scn.GetStrength(uruk4));
		assertEquals(11, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
