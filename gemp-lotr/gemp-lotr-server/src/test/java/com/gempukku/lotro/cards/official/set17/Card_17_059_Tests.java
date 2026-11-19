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

public class Card_17_059_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("trooper", "17_59");
					put("trooper2", "17_59");

					put("oath", "17_66");       // Wildman's Oath: [men] possession played to the support area; stacks a [men] minion discarded in the maneuver phase (declined); a skirmish ability never used here
					put("armor", "17_41");      // Ceremonial Armor: [men] possession played to the support area; the same trigger, and a response to a wound, not met here
					put("brand", "17_47");      // Primitive Brand: [men] possession, a hand weapon borne by a [men] minion; bearer is strength +2
					// Hides: a [dunland] possession in the support area.  Ted Sandyman 18_75's regroup ability stacks a [men] Man
					// on any possession, so a [men] minion can lie on it in a game.  Its own texts answer its play and [dunland] wounds.
					put("hides", "4_19");
					put("haradrim", "11_74");   // Detachment of Haradrim: a [men] minion to lie stacked on a possession
					put("corps", "11_73");      // Corps of Harad: a second [men] minion to lie stacked on a possession, or to bear the Brand (strength 10)
					put("hillsman", "17_50");   // Stampeding Hillsman: a [men] minion to lie stacked on a possession
					put("hillsman2", "17_50");
					put("saruman", "17_48");    // Saruman, Coldly Still: a [men] minion who is no Man, to lie stacked on a possession
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SunlandTrooperStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Sunland Trooper
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: This minion is strength +2 for each [men] possession with a [men] minion stacked on it.<br><b>Maneuver:</b> Spot X [men] minions stacked on a [men] possession and discard this minion to add (X).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("trooper");

		assertEquals("Sunland Trooper", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void StrengthIsPlusTwoForEachMenPossessionWithAMenMinionStackedOnIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trooper = scn.GetShadowCard("trooper");
		var trooper2 = scn.GetShadowCard("trooper2");
		var armor = scn.GetShadowCard("armor");
		var oath = scn.GetShadowCard("oath");
		var brand = scn.GetShadowCard("brand");
		var hides = scn.GetShadowCard("hides");
		var corps = scn.GetShadowCard("corps");
		var hillsman = scn.GetShadowCard("hillsman");
		var hillsman2 = scn.GetShadowCard("hillsman2");
		var haradrim = scn.GetShadowCard("haradrim");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		scn.MoveMinionsToTable(trooper, trooper2, corps);
		scn.AttachCardsTo(corps, brand);
		scn.MoveCardsToSupportArea(armor, oath, hides);
		assertAttachedTo(brand, corps);
		assertInZone(Zone.SUPPORT, armor, oath, hides);

		// Three [men] possessions with nothing stacked on them.
		assertEquals(0, scn.GetStackedCards(armor).size());
		assertEquals(0, scn.GetStackedCards(oath).size());
		assertEquals(0, scn.GetStackedCards(brand).size());
		assertEquals(9, scn.GetStrength(trooper));
		assertEquals(9, scn.GetStrength(trooper2));

		// A [men] minion on a [dunland] possession does not count.
		scn.StackCardsOn(hides, hillsman);
		assertEquals(9, scn.GetStrength(trooper));

		scn.StackCardsOn(armor, hillsman2);
		assertEquals(11, scn.GetStrength(trooper));
		// Each copy is strength +2 for itself, and the other copy does not add to it.
		assertEquals(11, scn.GetStrength(trooper2));
		// Another [men] minion is not made stronger: 10 + 2 for the Brand.
		assertEquals(12, scn.GetStrength(corps));

		// A second minion on the same possession: still one possession with a minion on it.
		scn.StackCardsOn(armor, saruman);
		assertEquals(11, scn.GetStrength(trooper));

		// The Brand, borne by a minion and in no support area, counts as well.
		scn.StackCardsOn(brand, haradrim);
		assertEquals(13, scn.GetStrength(trooper));
		assertEquals(13, scn.GetStrength(trooper2));
		assertEquals(12, scn.GetStrength(corps));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheNumberOfMinionsToSpotComesFilledInWithAllStackedOnMenPossessions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trooper = scn.GetShadowCard("trooper");
		var oath = scn.GetShadowCard("oath");
		var hides = scn.GetShadowCard("hides");
		var haradrim = scn.GetShadowCard("haradrim");
		var corps = scn.GetShadowCard("corps");
		var hillsman = scn.GetShadowCard("hillsman");

		scn.StartGame();
		scn.MoveCardsToSupportArea(oath, hides);
		scn.StackCardsOn(oath, haradrim, corps);
		scn.StackCardsOn(hides, hillsman);
		scn.MoveMinionsToTable(trooper);
		assertInZone(Zone.SUPPORT, oath, hides);
		assertEquals(2, scn.GetStackedCards(oath).size());
		assertEquals(1, scn.GetStackedCards(hides).size());

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowActionAvailable(trooper));
		scn.ShadowUseCardAction(trooper);

		// Two [men] minions stacked on a [men] possession; the [men] minion on the [dunland] Hides is not one.  The box
		// comes filled in with both (Q-0341: A, as F-0561), and the player accepts it.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertEquals(Integer.valueOf(2), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("2");

		// The Trooper is a [men] minion discarded in the maneuver phase: the Oath may stack him, and is declined.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(oath));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.DISCARD, trooper);
		assertEquals(pool + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void SpottingFewerThanAllAddsThatManyAndOnlyThisCopyIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trooper = scn.GetShadowCard("trooper");
		var trooper2 = scn.GetShadowCard("trooper2");
		var armor = scn.GetShadowCard("armor");
		var brand = scn.GetShadowCard("brand");
		var corps = scn.GetShadowCard("corps");
		var hillsman = scn.GetShadowCard("hillsman");
		var hillsman2 = scn.GetShadowCard("hillsman2");
		var haradrim = scn.GetShadowCard("haradrim");

		scn.StartGame();
		scn.MoveMinionsToTable(trooper, trooper2, corps);
		scn.AttachCardsTo(corps, brand);
		scn.MoveCardsToSupportArea(armor);
		// Two minions on the Armor in the support area, one on the Brand that a minion bears.
		scn.StackCardsOn(armor, hillsman, hillsman2);
		scn.StackCardsOn(brand, haradrim);
		assertAttachedTo(brand, corps);
		assertInZone(Zone.SUPPORT, armor);
		assertEquals(2, scn.GetStackedCards(armor).size());
		assertEquals(1, scn.GetStackedCards(brand).size());

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		int pool = scn.GetTwilight();
		scn.ShadowUseCardAction(trooper);

		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		assertEquals(Integer.valueOf(3), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("1");

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(armor));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.DISCARD, trooper);
		assertInZone(Zone.SHADOW_CHARACTERS, trooper2);
		assertEquals(pool + 1, scn.GetTwilight());
		// Spotting stacked minions moves none of them.
		assertEquals(2, scn.GetStackedCards(armor).size());
		assertEquals(1, scn.GetStackedCards(brand).size());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoMinionStackedTheAbilityIsOfferedAndAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trooper = scn.GetShadowCard("trooper");
		var armor = scn.GetShadowCard("armor");

		scn.StartGame();
		scn.MoveMinionsToTable(trooper);
		scn.MoveCardsToSupportArea(armor);
		assertInZone(Zone.SUPPORT, armor);
		assertEquals(0, scn.GetStackedCards(armor).size());

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowActionAvailable(trooper));
		scn.ShadowUseCardAction(trooper);

		// X is 0, the only number there is, so nothing is asked; the Armor may stack the Trooper, and is declined.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(armor));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.DISCARD, trooper);
		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheAbilityIsNotOfferedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trooper = scn.GetShadowCard("trooper");
		var armor = scn.GetShadowCard("armor");
		var hillsman = scn.GetShadowCard("hillsman");

		scn.StartGame();
		scn.MoveMinionsToTable(trooper);
		scn.MoveCardsToSupportArea(armor);
		scn.StackCardsOn(armor, hillsman);
		assertInZone(Zone.SUPPORT, armor);
		assertEquals(1, scn.GetStackedCards(armor).size());

		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(trooper));
		assertInZone(Zone.SHADOW_CHARACTERS, trooper);
	}
}
