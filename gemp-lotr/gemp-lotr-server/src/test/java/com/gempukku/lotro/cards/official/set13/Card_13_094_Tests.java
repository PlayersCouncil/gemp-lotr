package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_094_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("howdah", "13_94");
					put("howdah2", "13_94");

					// Ferocious Haradrim: a [men] archer; its other text answers a Free Peoples character killed
					put("haradrim", "11_80");
					// Southron Murderer: a [men] archer; while 4 culture tokens can be spotted it cannot take wounds
					// except in skirmishes
					put("murderer", "13_98");
					put("corps", "11_73");      // Corps of Harad: a [men] Man minion and no archer; fierce at a battleground
					put("marksman", "1_176");   // Goblin Marksman: a [moria] archer, Archer is its whole text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HowdahStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Howdah
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: When you play this, add a [men] token here for each [men] archer you spot.
		 * 	At the start of the archery phase, you may discard this condition from play or remove 2 tokens from here
		 * 	to make the minion archery total +1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("howdah");

		assertEquals("Howdah", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedItAsksHowManyMenArchersToSpotAndTakesATokenForEachOneNamed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var howdah = scn.GetShadowCard("howdah");
		scn.MoveCardsToHand(howdah);

		scn.StartGame();
		// Two [men] archers, a [men] minion who is no archer and an archer of another culture.
		scn.MoveMinionsToTable("haradrim", "murderer", "corps", "marksman");
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(howdah));
		scn.ShadowPlayCard(howdah);
		assertInZone(Zone.SUPPORT, howdah);

		// None, or any number up to the two [men] archers there are.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");

		assertEquals(2, scn.GetCultureTokensOn(howdah));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NamingOneOfTwoArchersTakesOneTokenAndASecondCopyPlayedAfterTakesItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var howdah = scn.GetShadowCard("howdah");
		var howdah2 = scn.GetShadowCard("howdah2");
		scn.MoveCardsToHand(howdah, howdah2);

		scn.StartGame();
		scn.MoveMinionsToTable("haradrim", "murderer");
		scn.FreepsPass();

		scn.ShadowPlayCard(howdah);
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("1");
		assertEquals(1, scn.GetCultureTokensOn(howdah));

		// The second copy's play is its own: its tokens go on it, and the first keeps its one.
		scn.ShadowPlayCard(howdah2);
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");
		assertInZone(Zone.SUPPORT, howdah, howdah2);
		assertEquals(2, scn.GetCultureTokensOn(howdah2));
		assertEquals(1, scn.GetCultureTokensOn(howdah));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtTheStartOfTheArcheryPhaseItMayRemoveTwoTokensToMakeTheMinionArcheryTotalPlusOneForThatPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var howdah = scn.GetShadowCard("howdah");
		var haradrim = scn.GetShadowCard("haradrim");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(howdah);

		scn.StartGame();
		scn.AddTokensToCard(howdah, 3);
		scn.MoveMinionsToTable(haradrim);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(3, scn.GetCultureTokensOn(howdah));
		// The Haradrim is the one archer.
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(howdah));
		scn.ShadowAcceptOptionalTrigger();

		// With 2 tokens or more, either cost may be paid.
		assertEquals(List.of("Discard this condition from play", "Remove 2 tokens from here"), scn.ShadowGetMultipleChoices());
		scn.ShadowChoose("Remove 2 tokens from here");
		assertEquals(1, scn.GetCultureTokensOn(howdah));
		assertInZone(Zone.SUPPORT, howdah);
		assertEquals(2, scn.GetShadowArcheryTotal());

		// Two archery wounds for the fellowship.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The archery phase is over, and the +1 with it.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ItMayBeDiscardedToMakeTheMinionArcheryTotalPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var howdah = scn.GetShadowCard("howdah");
		var haradrim = scn.GetShadowCard("haradrim");
		scn.MoveCompanionsToTable("aragorn");
		scn.MoveCardsToSupportArea(howdah);

		scn.StartGame();
		scn.AddTokensToCard(howdah, 2);
		scn.MoveMinionsToTable(haradrim);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(2, scn.GetCultureTokensOn(howdah));
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(howdah));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChoose("Discard this condition from play");
		assertInZone(Zone.DISCARD, howdah);
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void WithOneTokenOnlyTheDiscardCanBePaidAndNothingIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var howdah = scn.GetShadowCard("howdah");
		var haradrim = scn.GetShadowCard("haradrim");
		scn.MoveCompanionsToTable("aragorn");
		scn.MoveCardsToSupportArea(howdah);

		scn.StartGame();
		scn.AddTokensToCard(howdah, 1);
		scn.MoveMinionsToTable(haradrim);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(1, scn.GetCultureTokensOn(howdah));
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(howdah));
		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.DISCARD, howdah);
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void NotOfferedAtTheStartOfTheManeuverPhaseAndDecliningAtTheArcheryPhaseLeavesEverythingAsItWas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var howdah = scn.GetShadowCard("howdah");
		var haradrim = scn.GetShadowCard("haradrim");
		scn.MoveCompanionsToTable("aragorn");
		scn.MoveCardsToSupportArea(howdah);

		scn.StartGame();
		scn.AddTokensToCard(howdah, 3);
		scn.MoveMinionsToTable(haradrim);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(howdah));
		assertEquals(3, scn.GetCultureTokensOn(howdah));

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(howdah));
		assertEquals(3, scn.GetCultureTokensOn(howdah));
		assertEquals(1, scn.GetShadowArcheryTotal());
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, howdah);
		assertEquals(3, scn.GetCultureTokensOn(howdah));
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void EachCopyOffersItsOwnAndTheTokensComeOffTheOneTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var howdah = scn.GetShadowCard("howdah");
		var howdah2 = scn.GetShadowCard("howdah2");
		var haradrim = scn.GetShadowCard("haradrim");
		scn.MoveCompanionsToTable("aragorn");
		scn.MoveCardsToSupportArea(howdah, howdah2);

		scn.StartGame();
		scn.AddTokensToCard(howdah, 3);
		scn.AddTokensToCard(howdah2, 2);
		scn.MoveMinionsToTable(haradrim);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(howdah));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(howdah2));
		scn.ShadowChooseOptionalTrigger(howdah);
		scn.ShadowChoose("Remove 2 tokens from here");

		assertEquals(1, scn.GetCultureTokensOn(howdah));
		assertEquals(2, scn.GetCultureTokensOn(howdah2));
		assertEquals(2, scn.GetShadowArcheryTotal());

		// The other copy is still on offer, and declined.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(howdah2));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, howdah, howdah2);
		assertEquals(2, scn.GetCultureTokensOn(howdah2));
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
