package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertAttachedTo;
import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_V3_014_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("strands", "103_14");
					put("strands2", "103_14"); // Second copy for limit testing
					put("pass", "103_12"); // Gollum card to hinder
					put("shelob", "8_26");
					put("gollum", "9_28");
					put("runner", "1_178");

					put("aragorn", "1_89");
					put("glorfindel", "9_16");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StickyStrandsofSpidersilkStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Sticky Strands of Spider-silk
		 * Unique: false
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support area
		 * Game Text: At the end of each skirmish involving bearer, make bearer strength -1 until the end of the turn.
		 * 		Skirmish: Hinder your [gollum] card to transfer this to a companion skirmishing a [Gollum] minion.
		 * 		Limit 1 per bearer unless they are skirmishing Shelob.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("strands");

		assertEquals("Sticky Strands of Spider-silk", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void StickyStrandsTransfersToCompanionSkirmishingShelob() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var strands = scn.GetShadowCard("strands");
		var pass = scn.GetShadowCard("pass");
		var shelob = scn.GetShadowCard("shelob");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(strands, pass);
		scn.MoveMinionsToTable(shelob);

		scn.StartGame();
		scn.SkipToAssignments();

		assertFalse(scn.IsHindered(pass));
		assertInZone(Zone.SUPPORT, strands);

		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Use Sticky Strands ability
		assertTrue(scn.ShadowActionAvailable(strands));
		scn.ShadowUseCardAction(strands);

		// Choose Gollum card to hinder
		scn.ShadowChooseCard(pass);
		assertTrue(scn.IsHindered(pass));

		// Strands transfers to Aragorn (auto-selected as only valid target)
		assertAttachedTo(strands, aragorn);
	}

	@Test
	public void StickyStrandsTransfersToCompanionSkirmishingGollum() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var strands = scn.GetShadowCard("strands");
		var pass = scn.GetShadowCard("pass");
		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(strands, pass);
		scn.MoveMinionsToTable(gollum, runner);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Use Sticky Strands ability
		assertTrue(scn.ShadowActionAvailable(strands));
		scn.ShadowUseCardAction(strands);

		// Choose Gollum card to hinder
		scn.ShadowChooseCard(pass);

		// Strands transfers to Aragorn
		assertAttachedTo(strands, aragorn);
	}

	@Test
	public void StickyStrandsCannotTransferToCompanionNotSkirmishingGollumMinion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var strands = scn.GetShadowCard("strands");
		var pass = scn.GetShadowCard("pass");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(strands, pass);
		scn.MoveMinionsToTable(runner); // Runner is not Gollum culture

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Sticky Strands ability should not be available - no Gollum minion in skirmish
		assertFalse(scn.ShadowActionAvailable(strands));
	}


	@Test
	public void StickyStrandsLimitedToOnePerBearerAgainstGollum() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var strands = scn.GetShadowCard("strands");
		var strands2 = scn.GetShadowCard("strands2");
		var pass = scn.GetShadowCard("pass");
		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(strands2, pass);
		scn.MoveMinionsToTable(gollum, runner);
		scn.AttachCardsTo(aragorn, strands); // First copy already attached

		scn.StartGame();
		scn.SkipToAssignments();

		assertAttachedTo(strands, aragorn);
		assertInZone(Zone.SUPPORT, strands2);

		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// Second Sticky Strands ability should not be available - limit 1 per bearer against Gollum
		assertFalse(scn.ShadowActionAvailable(strands2));
	}

	@Test
	public void StickyStrandsNotLimitedAgainstShelob() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var strands = scn.GetShadowCard("strands");
		var strands2 = scn.GetShadowCard("strands2");
		var pass = scn.GetShadowCard("pass");
		var shelob = scn.GetShadowCard("shelob");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(strands2, pass);
		scn.MoveMinionsToTable(shelob);
		scn.AttachCardsTo(aragorn, strands); // First copy already attached

		scn.StartGame();
		scn.SkipToAssignments();

		assertAttachedTo(strands, aragorn);
		assertInZone(Zone.SUPPORT, strands2);

		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Second Sticky Strands should be available against Shelob - no limit
		assertTrue(scn.ShadowActionAvailable(strands2));
		scn.ShadowUseCardAction(strands2);

		scn.ShadowChooseCard(pass);

		// Both strands attached to Aragorn
		assertAttachedTo(strands, aragorn);
		assertAttachedTo(strands2, aragorn);
	}

	@Test
	public void StickyStrandsReducesStrengthByOneAfterSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var strands = scn.GetShadowCard("strands");
		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(gollum, runner);
		scn.AttachCardsTo(aragorn, strands);

		scn.StartGame();

		// Strands gives -1 strength while attached
		int aragornBaseStrength = scn.GetStrength(aragorn);

		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// Before skirmish ends, still just -1 from being attached
		assertEquals(aragornBaseStrength, scn.GetStrength(aragorn));

		scn.PassCurrentPhaseActions();

		// After skirmish ends, trigger gives additional -1 until end of turn
		assertEquals(aragornBaseStrength - 1, scn.GetStrength(aragorn));
	}

	@Test
	public void StickyStrandsStrengthReductionStacksWithMultipleSkirmishes() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var strands = scn.GetShadowCard("strands");
		var shelob = scn.GetShadowCard("shelob"); // Fierce - will skirmish twice
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(shelob);
		scn.AttachCardsTo(aragorn, strands);

		scn.StartGame();

		int aragornBaseStrength = scn.GetStrength(aragorn);

		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		// After first skirmish ends: -1 until end of turn
		assertEquals(aragornBaseStrength - 1, scn.GetStrength(aragorn));

		// Fierce skirmish - return to assignment phase
		scn.BothPass();
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		// After second (fierce) skirmish ends: -2 until end of turn (stacking)
		assertEquals(aragornBaseStrength - 2, scn.GetStrength(aragorn));
	}
}
