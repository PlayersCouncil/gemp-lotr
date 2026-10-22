package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_097_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("breaker", "8_97");
					put("breaker2", "8_97");
					put("legolas", "1_50");     // Legolas, Greenleaf: an archer; his archery ability is used only on purpose
					put("garrison", "8_36");    // Garrison of Gondor: an archer; his other text needs a fortification
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("bow", "1_90");         // Aragorn's Bow: makes Aragorn an archer; an archery ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GorgorothBreakerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Gorgoroth Breaker
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Besieger</b>.<br><b>Archery:</b> Exert this minion or stack it on a site you control to make the fellowship archery total -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("breaker");

		assertEquals("Gorgoroth Breaker", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.BESIEGER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithBothCostsPayableThePlayerChoosesAndExertingMakesTheFellowshipArcheryTotalMinusTwoForTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var garrison = scn.GetFreepsCard("garrison");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCompanionsToTable(legolas, garrison, aragorn);
		scn.AttachCardsTo(aragorn, bow);

		var breaker = scn.GetShadowCard("breaker");
		var breaker2 = scn.GetShadowCard("breaker2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(breaker, breaker2, troop);
		scn.SkipToPhase(Phase.ARCHERY);

		// Three archers: Legolas, the Garrison and Aragorn with his Bow.
		assertTrue(scn.ShadowControls(site1));
		assertEquals(3, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(breaker));
		assertEquals(0, scn.GetWoundsOn(breaker2));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertTrue(scn.ShadowActionAvailable(breaker));
		scn.ShadowUseCardAction(breaker);

		// Both costs can be paid: the player is asked which.
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		assertTrue(scn.ShadowChoiceAvailable("Exert"));
		assertTrue(scn.ShadowChoiceAvailable("Stack"));
		scn.ShadowChooseOption("Exert");

		// 3 - 2: a -1 would read 2, a -3 or a total set to 0 would read 0.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(1, scn.GetWoundsOn(breaker));
		assertEquals(0, scn.GetWoundsOn(breaker2));
		assertInZone(Zone.SHADOW_CHARACTERS, breaker, breaker2);
		assertTrue(scn.GetStackedCards(site1).isEmpty());

		// Archery fire uses the lowered total: one wound for the Shadow player to place, and then the assignment
		// phase, where the total is back at 3.
		scn.PassArcheryActions();
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.ShadowChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(3, scn.GetFreepsArcheryTotal());
	}

	@Test
	public void ExhaustedItIsStackedOnTheChosenSiteItsOwnerControlsWithNoQuestionOfWhichCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var garrison = scn.GetFreepsCard("garrison");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCompanionsToTable(legolas, garrison, aragorn);
		scn.AttachCardsTo(aragorn, bow);

		var breaker = scn.GetShadowCard("breaker");
		var breaker2 = scn.GetShadowCard("breaker2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(4);
		// Site 1 is the other player's (he took it as the Shadow player of an earlier turn); sites 2 and 3 are his.
		scn.FreepsTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		scn.MoveMinionsToTable(breaker, breaker2, troop);
		// Vitality 2 with 1 wound: exhausted, so he cannot exert.
		scn.AddWoundsToChar(breaker, 1);
		assertEquals(1, scn.GetWoundsOn(breaker));
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.FreepsControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.ShadowControls(site3));
		assertEquals(3, scn.GetFreepsArcheryTotal());
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(breaker));
		scn.ShadowUseCardAction(breaker);

		// Only the stacking can be paid, so no choice of costs is asked: only the site.
		assertTrue(scn.ShadowHasCardChoiceAvailable(site2, site3));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(site1));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(site3);

		// 3 - 2
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertInZone(Zone.STACKED, breaker);
		assertTrue(scn.GetStackedCards(site3).contains(breaker));
		assertFalse(scn.GetStackedCards(site2).contains(breaker));
		assertTrue(scn.GetStackedCards(site1).isEmpty());
		// The other Breaker is neither stacked nor exerted.
		assertInZone(Zone.SHADOW_CHARACTERS, breaker2);
		assertEquals(0, scn.GetWoundsOn(breaker2));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void WithNoSiteOfItsOwnersItExertsWithNoQuestionOfWhichCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var garrison = scn.GetFreepsCard("garrison");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCompanionsToTable(legolas, garrison, aragorn);
		scn.AttachCardsTo(aragorn, bow);

		var breaker = scn.GetShadowCard("breaker");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(2);
		// The one controlled site is the other player's.
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(breaker, troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.FreepsControls(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(3, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(breaker));
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(breaker));
		scn.ShadowUseCardAction(breaker);

		// 3 - 2, paid by the exertion unasked.
		assertEquals(1, scn.GetWoundsOn(breaker));
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertInZone(Zone.SHADOW_CHARACTERS, breaker);
		assertTrue(scn.GetStackedCards(site1).isEmpty());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void ExhaustedAndWithNoSiteOfItsOwnersItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		var breaker = scn.GetShadowCard("breaker");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(breaker, troop);
		scn.AddWoundsToChar(breaker, 1);
		assertEquals(1, scn.GetWoundsOn(breaker));
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.FreepsControls(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(breaker));

		// A site of his own: the stacking can be paid, and the ability is offered.
		scn.ShadowTakeControlOfSite();
		scn.ShadowRefreshDecision();
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertTrue(scn.ShadowActionAvailable(breaker));
	}
}
