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

public class Card_17_057_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("skirmisher", "17_57");
					put("skirmisher2", "17_57");

					put("armor", "17_41");      // Ceremonial Armor: [men] possession played to the support area; stacks a [men] minion discarded in the maneuver phase
					put("oath", "17_66");       // Wildman's Oath: [men] possession played to the support area; the same trigger, and a skirmish ability never used here
					put("brand", "17_47");      // Primitive Brand: [men] possession, a hand weapon borne by a [men] minion; bearer is strength +2
					// Hides: a [dunland] possession in the support area.  Ted Sandyman 18_75's regroup ability stacks a [men] Man
					// on any possession, so a [men] minion can lie on it in a game.  Its own texts answer its play and [dunland] wounds.
					put("hides", "4_19");
					put("corps", "11_73");      // Corps of Harad: [men] Man, strength 10, to bear the Brand; fierce only at a battleground
					put("haradrim", "11_74");   // Detachment of Haradrim: [men] Man, cost 5; a maneuver ability never used here
					put("elder", "11_77");      // Elder of Dunland: [men] Man, cost 4; damage +1 only at a plains site
					put("hillsman", "17_50");   // Stampeding Hillsman: [men] Man, cost 2; a regroup ability never used here
					put("hillsman2", "17_50");
					put("saruman", "17_48");    // Saruman, Coldly Still: a [men] minion who is no Man, to lie stacked on a possession
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SunlandSkirmisherStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Sunland Skirmisher
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: This minion is strength +1 for each [men] minion stacked on each [men] possession.<br>At the start of your maneuver phase, you may play a minion stacked on a [men] possession as if from hand.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("skirmisher");

		assertEquals("Sunland Skirmisher", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void StrengthIsPlusOneForEachMenMinionStackedOnEachMenPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skirmisher = scn.GetShadowCard("skirmisher");
		var skirmisher2 = scn.GetShadowCard("skirmisher2");
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
		scn.MoveMinionsToTable(skirmisher, skirmisher2, corps);
		scn.AttachCardsTo(corps, brand);
		scn.MoveCardsToSupportArea(armor, oath, hides);
		assertAttachedTo(brand, corps);
		assertInZone(Zone.SUPPORT, armor, oath, hides);

		// Three [men] possessions with nothing stacked on them.
		assertEquals(0, scn.GetStackedCards(armor).size());
		assertEquals(0, scn.GetStackedCards(oath).size());
		assertEquals(0, scn.GetStackedCards(brand).size());
		assertEquals(6, scn.GetStrength(skirmisher));
		assertEquals(6, scn.GetStrength(skirmisher2));

		// A [men] minion on a [dunland] possession does not count.
		scn.StackCardsOn(hides, hillsman);
		assertEquals(6, scn.GetStrength(skirmisher));

		scn.StackCardsOn(armor, hillsman2);
		assertEquals(7, scn.GetStrength(skirmisher));
		// Each copy is strength +1 for itself, and the other copy does not add to it.
		assertEquals(7, scn.GetStrength(skirmisher2));
		// Another [men] minion is not made stronger: 10 + 2 for the Brand.
		assertEquals(12, scn.GetStrength(corps));

		// A second minion on the same possession counts as well.
		scn.StackCardsOn(armor, saruman);
		assertEquals(8, scn.GetStrength(skirmisher));

		// So does one on the Brand, borne by a minion and in no support area.
		scn.StackCardsOn(brand, haradrim);
		assertEquals(9, scn.GetStrength(skirmisher));
		assertEquals(9, scn.GetStrength(skirmisher2));
		assertEquals(12, scn.GetStrength(corps));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfTheManeuverPhaseMayPlayAMinionStackedOnAMenPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skirmisher = scn.GetShadowCard("skirmisher");
		var armor = scn.GetShadowCard("armor");
		var brand = scn.GetShadowCard("brand");
		var hides = scn.GetShadowCard("hides");
		var corps = scn.GetShadowCard("corps");
		var haradrim = scn.GetShadowCard("haradrim");
		var elder = scn.GetShadowCard("elder");
		var hillsman = scn.GetShadowCard("hillsman");

		scn.StartGame();
		scn.SetTwilight(20);
		scn.MoveMinionsToTable(skirmisher, corps);
		scn.AttachCardsTo(corps, brand);
		scn.MoveCardsToSupportArea(armor, hides);
		scn.StackCardsOn(armor, haradrim);
		scn.StackCardsOn(brand, elder);
		scn.StackCardsOn(hides, hillsman);
		assertAttachedTo(brand, corps);
		assertInZone(Zone.SUPPORT, armor, hides);
		assertEquals(1, scn.GetStackedCards(armor).size());
		assertEquals(1, scn.GetStackedCards(brand).size());
		assertEquals(1, scn.GetStackedCards(hides).size());
		assertInZone(Zone.STACKED, haradrim, elder, hillsman);
		// 6 + 1 for the minion on the Armor + 1 for the minion on the Brand
		assertEquals(8, scn.GetStrength(skirmisher));

		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skirmisher));
		scn.ShadowAcceptOptionalTrigger();

		// A minion on either [men] possession, and not the one on the [dunland] Hides.
		assertTrue(scn.ShadowHasCardChoiceAvailable(haradrim, elder));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hillsman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(haradrim);

		assertInZone(Zone.SHADOW_CHARACTERS, haradrim);
		assertEquals(0, scn.GetStackedCards(armor).size());
		assertEquals(1, scn.GetStackedCards(brand).size());
		assertEquals(1, scn.GetStackedCards(hides).size());
		// One minion fewer is stacked on a [men] possession.
		assertEquals(7, scn.GetStrength(skirmisher));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void DeclinedItLeavesTheMinionStackedAndIsNotOfferedAtTheStartOfTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skirmisher = scn.GetShadowCard("skirmisher");
		var armor = scn.GetShadowCard("armor");
		var haradrim = scn.GetShadowCard("haradrim");

		scn.StartGame();
		scn.SetTwilight(20);
		scn.MoveMinionsToTable(skirmisher);
		scn.MoveCardsToSupportArea(armor);
		scn.StackCardsOn(armor, haradrim);
		assertInZone(Zone.SUPPORT, armor);
		assertEquals(1, scn.GetStackedCards(armor).size());

		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skirmisher));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetStackedCards(armor).size());
		assertEquals(7, scn.GetStrength(skirmisher));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.SkipToPhase(Phase.ARCHERY);

		// Nothing is offered to the Shadow player ahead of the archery actions.
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(1, scn.GetStackedCards(armor).size());
		assertEquals(7, scn.GetStrength(skirmisher));
	}

	@Test
	public void IsNotOfferedWhenNoMinionOnAMenPossessionCanBePaidFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skirmisher = scn.GetShadowCard("skirmisher");
		var armor = scn.GetShadowCard("armor");
		var hides = scn.GetShadowCard("hides");
		var haradrim = scn.GetShadowCard("haradrim");
		var hillsman = scn.GetShadowCard("hillsman");

		scn.StartGame();
		scn.SetTwilight(2);
		scn.MoveMinionsToTable(skirmisher);
		scn.MoveCardsToSupportArea(armor, hides);
		scn.StackCardsOn(armor, haradrim);
		scn.StackCardsOn(hides, hillsman);
		assertInZone(Zone.SUPPORT, armor, hides);
		assertEquals(1, scn.GetStackedCards(armor).size());
		assertEquals(1, scn.GetStackedCards(hides).size());

		scn.SkipToPhase(Phase.MANEUVER);

		// 2 + 2 for Ettenmoors + 1 for Frodo.  The Detachment would cost 5 + 2 roaming at site 2; the Hillsman on
		// the Hides, which the pool could pay for (2 + 2 roaming), is not on a [men] possession.
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(1, scn.GetStackedCards(armor).size());
		assertEquals(1, scn.GetStackedCards(hides).size());
	}

	@Test
	public void TheOwnersNextManeuverPhaseOffersItAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skirmisher = scn.GetShadowCard("skirmisher");
		var armor = scn.GetShadowCard("armor");
		var haradrim = scn.GetShadowCard("haradrim");
		var elder = scn.GetShadowCard("elder");

		scn.StartGame();
		scn.SetTwilight(20);
		scn.MoveMinionsToTable(skirmisher);
		scn.MoveCardsToSupportArea(armor);
		scn.StackCardsOn(armor, haradrim, elder);
		assertInZone(Zone.SUPPORT, armor);
		assertEquals(2, scn.GetStackedCards(armor).size());
		assertInZone(Zone.STACKED, haradrim, elder);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skirmisher));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(haradrim);
		assertInZone(Zone.SHADOW_CHARACTERS, haradrim);
		assertEquals(1, scn.GetStackedCards(armor).size());

		// The minions taken off, player 2's own turn played, and the Skirmisher laid again on turn 3.
		scn.MoveCardsToDiscard(skirmisher, haradrim);
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		scn.SetTwilight(20);
		scn.MoveMinionsToTable(skirmisher);
		assertInZone(Zone.SUPPORT, armor);
		assertEquals(1, scn.GetStackedCards(armor).size());

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skirmisher));
		scn.ShadowAcceptOptionalTrigger();

		// The one minion left is played unasked.
		assertInZone(Zone.SHADOW_CHARACTERS, elder);
		assertEquals(0, scn.GetStackedCards(armor).size());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
