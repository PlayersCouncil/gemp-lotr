package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_101_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("reach", "103_101");      // Sauron's Reach
					put("sauron", "9_48");        // Sauron, The Lord of the Rings
					put("slayer1", "3_93");       // Morgul Slayer - [Sauron] minion
					put("slayer2", "3_93");
					put("slayer3", "3_93");
					put("runner", "1_178");       // Goblin Runner - [Moria], not [Sauron]

					put("lordofmoria", "1_21");   // FP [Dwarven] Condition - support area
					put("elrond", "3_13");        // Elrond - Ally in support area
					put("vilya", "3_27");         // Vilya - Artifact that attaches to Elrond
					put("hollowing1", "3_54");    // Shadow [Isengard] Condition - support area
					put("hollowing2", "3_54");
					put("stone", "9_47");         // Shadow [Sauron] Artifact - support area

					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SauronsReachStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Sauron's Reach
		 * Unique: false
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Exert 3 [sauron] minions (or spot Sauron) to hinder all Free Peoples support cards.  For each card hindered, hinder a Shadow support card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("reach");

		assertEquals("Sauron's Reach", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}



	@Test
	public void SauronsReachPlaysFreeWhenSauronPresent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reach = scn.GetShadowCard("reach");
		var sauron = scn.GetShadowCard("sauron");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		var hollowing1 = scn.GetShadowCard("hollowing1");

		scn.MoveCardsToHand(reach);
		scn.MoveMinionsToTable(sauron);
		scn.MoveCardsToSupportArea(lordofmoria, hollowing1);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(sauron));

		assertTrue(scn.ShadowPlayAvailable(reach));
		scn.ShadowPlayCard(reach);

		// No exertions required when Sauron spotted
		assertEquals(0, scn.GetWoundsOn(sauron));

		assertTrue(scn.IsHindered(lordofmoria));
	}

	@Test
	public void SauronsReachExertsThreeSauronMinionsWithoutSauron() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reach = scn.GetShadowCard("reach");
		var slayer1 = scn.GetShadowCard("slayer1");
		var slayer2 = scn.GetShadowCard("slayer2");
		var slayer3 = scn.GetShadowCard("slayer3");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		var hollowing1 = scn.GetShadowCard("hollowing1");

		scn.MoveCardsToHand(reach);
		scn.MoveMinionsToTable(slayer1, slayer2, slayer3);
		scn.MoveCardsToSupportArea(lordofmoria, hollowing1);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(reach));
		scn.ShadowPlayCard(reach);

		assertEquals(1, scn.GetWoundsOn(slayer1));
		assertEquals(1, scn.GetWoundsOn(slayer2));
		assertEquals(1, scn.GetWoundsOn(slayer3));

		assertTrue(scn.IsHindered(lordofmoria));
	}

	@Test
	public void SauronsReachCannotPlayWithoutSauronAndFewerThanThreeSauronMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reach = scn.GetShadowCard("reach");
		var slayer1 = scn.GetShadowCard("slayer1");
		var slayer2 = scn.GetShadowCard("slayer2");
		var runner = scn.GetShadowCard("runner");  // [Moria], doesn't count

		scn.MoveCardsToHand(reach);
		scn.MoveMinionsToTable(slayer1, slayer2, runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Only 2 [Sauron] minions, runner is [Moria]
		assertFalse(scn.ShadowPlayAvailable(reach));
	}

	@Test
	public void SauronsReachHindersAllFPSupportsAndAttachmentsToSupports() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reach = scn.GetShadowCard("reach");
		var sauron = scn.GetShadowCard("sauron");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		var elrond = scn.GetFreepsCard("elrond");
		var vilya = scn.GetFreepsCard("vilya");
		var hollowing1 = scn.GetShadowCard("hollowing1");
		var hollowing2 = scn.GetShadowCard("hollowing2");
		var stone = scn.GetShadowCard("stone");

		scn.MoveCardsToHand(reach);
		scn.MoveMinionsToTable(sauron);
		scn.MoveCardsToSupportArea(lordofmoria, elrond);
		scn.AttachCardsTo(elrond, vilya);  // Vilya attached to ally in support
		scn.MoveCardsToSupportArea(hollowing1, hollowing2, stone);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertFalse(scn.IsHindered(lordofmoria));
		assertFalse(scn.IsHindered(elrond));
		assertFalse(scn.IsHindered(vilya));

		scn.ShadowPlayCard(reach);

		// All FP supports AND attachments to supports hindered (3 total)
		assertTrue(scn.IsHindered(lordofmoria));
		assertTrue(scn.IsHindered(elrond));
		assertTrue(scn.IsHindered(vilya));

		assertTrue(scn.IsHindered(hollowing1));
		assertTrue(scn.IsHindered(hollowing2));
		assertTrue(scn.IsHindered(stone));
	}
}
