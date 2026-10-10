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

public class Card_18_116_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "18_116");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetChoiceScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fury", "18_116");
					put("aggressor", "13_168"); // Uruk Aggressor: [uruk-hai] minion, strength 8, vitality 2, damage +1; its text is about its cost
					put("knight", "5_35");      // Gondorian Knight: strength 6, vitality 3, no text
					put("worker", "15_135");    // Rohan Worker: a follower; its texts answer a transfer and its bearer's win
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FuryoftheEvilArmyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Fury of the Evil Army
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: 
		 * Strength: 1
		 * Game Text: Bearer must be an [uruk-hai] minion.<br>Each time bearer wins a skirmish, you may choose one: discard a follower from play; heal an [uruk-hai] minion; or control a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Fury of the Evil Army", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void WithNoFollowerNoWoundedUrukHaiMinionAndNoSiteThatCanBeControlledBearerWinningASkirmishOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetChoiceScenario();

		var fury = scn.GetShadowCard("fury");
		var aggressor = scn.GetShadowCard("aggressor");
		var knight = scn.GetFreepsCard("knight");

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2.  Site 1 is the only site every marker has passed,
		// and player 1 took it on player 2's turn.
		scn.SkipToSite(2);
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(knight);
		scn.MoveMinionsToTable(aggressor);
		scn.AttachCardsTo(aggressor, fury);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, aggressor);
		scn.FreepsResolveSkirmish(knight);

		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertTrue(scn.FreepsControls(site1));

		scn.PassCurrentPhaseActions();
		// 8 + 1 for the Fury against 6: the Knight loses, and takes 2 wounds for damage +1.
		assertEquals(2, scn.GetWoundsOn(knight));
		// None of the three can be done, so there is nothing a yes could choose (Q-0034).
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(fury));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());
	}

	@Test
	public void WithOnlyASiteThatCanBeControlledBearerWinningASkirmishOffersTheChoiceAndAYesTakesTheSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetChoiceScenario();

		var fury = scn.GetShadowCard("fury");
		var aggressor = scn.GetShadowCard("aggressor");
		var knight = scn.GetFreepsCard("knight");

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 is behind every marker.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(knight);
		scn.MoveMinionsToTable(aggressor);
		scn.AttachCardsTo(aggressor, fury);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, aggressor);
		scn.FreepsResolveSkirmish(knight);

		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertFalse(scn.IsSiteControlled(site1));

		scn.PassCurrentPhaseActions();
		assertEquals(2, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fury));
		scn.ShadowAcceptOptionalTrigger();

		// The one thing of the three that can be done is done, and which is not asked.
		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithOnlyAWoundedUrukHaiMinionBearerWinningASkirmishOffersTheChoiceAndAYesHealsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetChoiceScenario();

		var fury = scn.GetShadowCard("fury");
		var aggressor = scn.GetShadowCard("aggressor");
		var knight = scn.GetFreepsCard("knight");

		scn.StartGame();
		scn.SkipToSite(2);
		// No site can be controlled: player 1 holds the only one every marker has passed.
		scn.FreepsTakeControlOfSite();
		scn.MoveCompanionsToTable(knight);
		scn.MoveMinionsToTable(aggressor);
		scn.AttachCardsTo(aggressor, fury);
		scn.AddWoundsToChar(aggressor, 1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, aggressor);
		scn.FreepsResolveSkirmish(knight);

		assertEquals(1, scn.GetWoundsOn(aggressor));

		scn.PassCurrentPhaseActions();
		assertEquals(2, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fury));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithOnlyAFollowerInPlayBearerWinningASkirmishOffersTheChoiceAndAYesDiscardsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetChoiceScenario();

		var fury = scn.GetShadowCard("fury");
		var aggressor = scn.GetShadowCard("aggressor");
		var knight = scn.GetFreepsCard("knight");
		var worker = scn.GetFreepsCard("worker");

		scn.StartGame();
		scn.SkipToSite(2);
		// No site can be controlled: player 1 holds the only one every marker has passed.
		scn.FreepsTakeControlOfSite();
		scn.MoveCompanionsToTable(knight);
		scn.MoveMinionsToTable(aggressor);
		scn.AttachCardsTo(aggressor, fury);

		scn.SkipToAssignments();
		// Laid after the maneuver phase, where its Aid would have been offered.
		scn.MoveCardsToSupportArea(worker);
		scn.FreepsAssignToMinions(knight, aggressor);
		scn.FreepsResolveSkirmish(knight);

		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertInZone(Zone.SUPPORT, worker);

		scn.PassCurrentPhaseActions();
		assertEquals(2, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fury));
		scn.ShadowAcceptOptionalTrigger();

		assertInDiscard(worker);
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
