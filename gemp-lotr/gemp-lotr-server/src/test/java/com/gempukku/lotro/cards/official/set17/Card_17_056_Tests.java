package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_056_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("scout", "17_56");
					put("scout2", "17_56");

					put("armor", "17_41");      // Ceremonial Armor: [men] possession played to the support area; stacks a [men] minion discarded in the maneuver phase
					put("oath", "17_66");       // Wildman's Oath: [men] possession played to the support area; the same trigger, and a skirmish ability never used here
					put("brand", "17_47");      // Primitive Brand: [men] possession, a hand weapon borne by a [men] minion; bearer is strength +2
					// Hides: a [dunland] possession in the support area.  Ted Sandyman 18_75's regroup ability stacks a [men] Man
					// on any possession, so a [men] minion can lie on it in a game.  Its own texts answer its play and [dunland] wounds.
					put("hides", "4_19");
					put("companies", "12_58");  // Countless Companies: a [men] condition in the support area; stacks a [men] possession discarded from play
					put("corps", "11_73");      // Corps of Harad: [men] Man, strength 10; fierce only at a battleground
					put("hillsman", "17_50");   // Stampeding Hillsman: a [men] minion to lie stacked on a possession
					put("hillsman2", "17_50");
					put("elder", "11_77");      // Elder of Dunland: a [men] minion to lie stacked on a possession
					put("saruman", "17_48");    // Saruman, Coldly Still: a [men] minion who is no Man, to lie stacked on a possession
					put("weary", "4_212");      // Weary: an [isengard] condition on a companion; its trigger wants an [isengard] tracker

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion; a maneuver ability never used here
					put("saga", "1_114");       // The Saga of Elendil: a Free Peoples condition borne by a [gondor] companion
					put("sword", "1_112");      // Ranger's Sword: a Free Peoples possession that Aragorn bears
					put("lord", "1_21");        // Lord of Moria: a Free Peoples condition in the support area; no Dwarf is in play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SunlandScoutStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Sunland Scout
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: This minion is strength +1 for each [men] possession with a [men] minion stacked on it.<br><b>Maneuver:</b> Spot a [men] possession and discard this minion from play to discard a condition.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("scout");

		assertEquals("Sunland Scout", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void StrengthIsPlusOneForEachMenPossessionWithAMenMinionStackedOnIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout = scn.GetShadowCard("scout");
		var scout2 = scn.GetShadowCard("scout2");
		var armor = scn.GetShadowCard("armor");
		var oath = scn.GetShadowCard("oath");
		var brand = scn.GetShadowCard("brand");
		var hides = scn.GetShadowCard("hides");
		var corps = scn.GetShadowCard("corps");
		var hillsman = scn.GetShadowCard("hillsman");
		var hillsman2 = scn.GetShadowCard("hillsman2");
		var elder = scn.GetShadowCard("elder");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		scn.MoveMinionsToTable(scout, scout2, corps);
		scn.AttachCardsTo(corps, brand);
		scn.MoveCardsToSupportArea(armor, oath, hides);
		assertAttachedTo(brand, corps);
		assertInZone(Zone.SUPPORT, armor, oath, hides);

		// Three [men] possessions with nothing stacked on them.
		assertEquals(0, scn.GetStackedCards(armor).size());
		assertEquals(0, scn.GetStackedCards(oath).size());
		assertEquals(0, scn.GetStackedCards(brand).size());
		assertEquals(10, scn.GetStrength(scout));
		assertEquals(10, scn.GetStrength(scout2));

		// A [men] minion on a [dunland] possession does not count.
		scn.StackCardsOn(hides, hillsman);
		assertEquals(10, scn.GetStrength(scout));

		scn.StackCardsOn(armor, hillsman2);
		assertEquals(11, scn.GetStrength(scout));
		// Each copy is strength +1 for itself, and the other copy does not add to it.
		assertEquals(11, scn.GetStrength(scout2));
		// Another [men] minion is not made stronger: 10 + 2 for the Brand.
		assertEquals(12, scn.GetStrength(corps));

		// A second minion on the same possession: still one possession with a minion on it.
		scn.StackCardsOn(armor, saruman);
		assertEquals(11, scn.GetStrength(scout));

		// The Brand, borne by a minion and in no support area, counts as well.
		scn.StackCardsOn(brand, elder);
		assertEquals(12, scn.GetStrength(scout));
		assertEquals(12, scn.GetStrength(scout2));
		assertEquals(12, scn.GetStrength(corps));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ManeuverAbilityDiscardsTheScoutToDiscardAConditionOfEitherSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout = scn.GetShadowCard("scout");
		var scout2 = scn.GetShadowCard("scout2");
		var brand = scn.GetShadowCard("brand");
		var corps = scn.GetShadowCard("corps");
		var companies = scn.GetShadowCard("companies");
		var weary = scn.GetShadowCard("weary");
		var aragorn = scn.GetFreepsCard("aragorn");
		var saga = scn.GetFreepsCard("saga");
		var sword = scn.GetFreepsCard("sword");
		var lord = scn.GetFreepsCard("lord");
		var frodo = scn.GetRingBearer();

		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, saga, sword);
		scn.AttachCardsTo(frodo, weary);
		scn.MoveCardsToSupportArea(lord);

		scn.StartGame();
		scn.MoveMinionsToTable(scout, scout2, corps);
		// The only [men] possession in play is borne by a minion.
		scn.AttachCardsTo(corps, brand);
		scn.MoveCardsToSupportArea(companies);
		assertAttachedTo(saga, aragorn);
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(weary, frodo);
		assertAttachedTo(brand, corps);
		assertInZone(Zone.SUPPORT, lord, companies);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(scout));
		scn.ShadowUseCardAction(scout);

		// The cost is paid: this copy goes, the other stays.
		assertInZone(Zone.DISCARD, scout);
		assertInZone(Zone.SHADOW_CHARACTERS, scout2);
		// A condition of either side, in a support area or borne; no possession.
		assertTrue(scn.ShadowHasCardChoiceAvailable(saga, lord, weary, companies));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sword, brand));
		scn.ShadowChooseCard(saga);

		assertInZone(Zone.DISCARD, saga);
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(weary, frodo);
		assertAttachedTo(brand, corps);
		assertInZone(Zone.SUPPORT, lord, companies);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoConditionInPlayTheAbilityIsOfferedAndDiscardsOnlyTheScout() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout = scn.GetShadowCard("scout");
		var armor = scn.GetShadowCard("armor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");

		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(scout);
		scn.MoveCardsToSupportArea(armor);
		assertAttachedTo(sword, aragorn);
		assertInZone(Zone.SUPPORT, armor);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// A [men] possession in the support area is spotted.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(scout));
		scn.ShadowUseCardAction(scout);

		// The Scout is a [men] minion discarded in the maneuver phase: the Armor may stack him, and is declined.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(armor));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.DISCARD, scout);
		assertInZone(Zone.SUPPORT, armor);
		assertAttachedTo(sword, aragorn);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedUntilAMenPossessionIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout = scn.GetShadowCard("scout");
		var armor = scn.GetShadowCard("armor");
		var oath = scn.GetShadowCard("oath");
		var hides = scn.GetShadowCard("hides");
		var companies = scn.GetShadowCard("companies");
		var lord = scn.GetFreepsCard("lord");

		scn.MoveCardsToSupportArea(lord);

		scn.StartGame();
		scn.MoveMinionsToTable(scout);
		// A possession of another culture, a [men] condition, and a [men] possession stacked on that condition (not in play).
		scn.MoveCardsToSupportArea(hides, companies);
		scn.StackCardsOn(companies, armor);
		assertInZone(Zone.SUPPORT, lord, hides, companies);
		assertEquals(1, scn.GetStackedCards(companies).size());

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(scout));

		scn.MoveCardsToSupportArea(oath);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(scout));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
