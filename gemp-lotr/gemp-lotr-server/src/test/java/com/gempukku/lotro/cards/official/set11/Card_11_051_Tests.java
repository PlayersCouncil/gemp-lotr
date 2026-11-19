package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_051_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("smeagol", "11_51");
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; its (2) is never added, being cheated in
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1 only
					// Evil-smelling Fens: [gollum] Shadow condition, "Each site on the adventure path is a marsh"; its
					// Shadow ability is never used
					put("fens", "5_22");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Midgewater Marshes, a marsh, as site 2.
	protected VirtualTableScenario GetMarshScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("smeagol", "11_51");
					put("runner", "1_178");
					put("savage", "1_151");
					// Aragorn, Ranger of the North: strength 8, vitality 4; his maneuver ability is never used
					put("aragorn", "1_89");
				}},
				new HashMap<>() {{
					put("site1", "1_319");      // Bag End
					put("site2", "1_332");      // Midgewater Marshes: marsh; each Hobbit moving there exerts
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SmeagolStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Sméagol, Scout and Guide
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Companion
		 * Subtype:
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 5
		 * Game Text: <b>Ring-bound</b>. To play, add a burden.
		 * Each time Sméagol wins a skirmish, you may play the fellowship's next site.
		 * <b>Skirmish:</b> If Sméagol is at a marsh, exert him to cancel a skirmish involving him.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("smeagol");

		assertEquals("Sméagol", card.getBlueprint().getTitle());
		assertEquals("Scout and Guide", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getResistance());
	}

	@Test
	public void PlayingSmeagolAddsABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCardsToHand(smeagol);

		scn.StartGame();

		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.FreepsPlayAvailable(smeagol));
		scn.FreepsPlayCard(smeagol);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertEquals(1, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeSmeagolWinsTheFreePeoplesPlayerMayPlayTheNextSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();
		// Strength 3 + 5: he beats either strength-5 minion.
		scn.ApplyAdHocModifier(new StrengthModifier(null, smeagol, null, 5));
		scn.MoveMinionsToTable(runner);
		scn.FreepsPass();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, runner);
		scn.FreepsResolveSkirmish(smeagol);
		var freepsSite3 = scn.GetFreepsSite(3);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertNull(scn.GetSite(3));
		scn.PassSkirmishActions();

		assertInZone(Zone.DISCARD, runner);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(smeagol));
		scn.FreepsAcceptOptionalTrigger();
		assertSame(freepsSite3, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_PATH, freepsSite3);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// A second win in the same turn, after the regroup move to that site 3, is answered again.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(savage);
		scn.ShadowRefreshDecision();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, savage);
		scn.FreepsResolveSkirmish(smeagol);
		assertNull(scn.GetSite(4));
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(smeagol));
		scn.FreepsDeclineOptionalTrigger();
		assertNull(scn.GetSite(4));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ASkirmishSmeagolLosesAndLivesThroughOffersNoSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.FreepsPass();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, runner);
		scn.FreepsResolveSkirmish(smeagol);
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertNull(scn.GetSite(3));
		scn.PassSkirmishActions();

		// Strength 3 against 5: he loses and takes one wound.
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(smeagol));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertNull(scn.GetSite(3));
	}

	@Test
	public void WithHisOwnNextSiteAlreadyLaidAWinOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();
		scn.ApplyAdHocModifier(new StrengthModifier(null, smeagol, null, 5));
		scn.MoveMinionsToTable(runner);
		scn.FreepsPass();

		// The Free Peoples player's own site 3 already lies next: there is no site for him to play.
		var freepsSite3 = scn.GetFreepsSite(3);
		scn.MoveCardToAdventurePath(freepsSite3);
		scn.ShadowRefreshDecision();
		assertSame(freepsSite3, scn.GetSite(3));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, runner);
		scn.FreepsResolveSkirmish(smeagol);
		scn.PassSkirmishActions();

		assertInZone(Zone.DISCARD, runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(smeagol));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertSame(freepsSite3, scn.GetSite(3));
	}

	@Test
	public void AnotherCompanionWinningASkirmishOffersNoSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(smeagol, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.FreepsPass();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);
		assertNull(scn.GetSite(3));
		scn.PassSkirmishActions();

		// Aragorn (8) beats the Goblin Runner (5), whose one vitality goes.
		assertInZone(Zone.DISCARD, runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(smeagol));
		assertNull(scn.GetSite(3));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtAMarshSmeagolExertsToCancelHisSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.FreepsPass();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, savage);
		scn.FreepsResolveSkirmish(smeagol);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(smeagol));

		scn.FreepsUseCardAction(smeagol);
		assertEquals(1, scn.GetWoundsOn(smeagol));
		// Cancelled: nobody wins or loses, and the Uruk Savage (strength 5 against 3, damage +1) places no wounds.
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(1, scn.GetWoundsOn(smeagol));
	}

	@Test
	public void AwayFromAMarshTheCancelIsNotOfferedUntilEvilSmellingFensMakesTheSiteOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var savage = scn.GetShadowCard("savage");
		var fens = scn.GetShadowCard("fens");
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.FreepsPass();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, savage);
		scn.FreepsResolveSkirmish(smeagol);
		// Ettenmoors is a plains and no marsh.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(smeagol));

		scn.MoveCardsToSupportArea(fens);
		scn.FreepsRefreshDecision();
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MARSH));
		assertTrue(scn.FreepsActionAvailable(smeagol));
		scn.FreepsUseCardAction(smeagol);
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ExhaustedAtTheMarshSmeagolCannotCancel() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		// Vitality 4 with 3 wounds: he cannot exert.
		scn.AddWoundsToChar(smeagol, 3);
		assertEquals(3, scn.GetWoundsOn(smeagol));
		scn.FreepsPass();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, savage);
		scn.FreepsResolveSkirmish(smeagol);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(smeagol));
		assertEquals(3, scn.GetWoundsOn(smeagol));
	}

	@Test
	public void UsedInAnotherCompanionsSkirmishTheExertionCancelsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMarshScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(smeagol, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, savage);
		scn.FreepsPass();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{smeagol, savage});
		// Aragorn's skirmish first; Sméagol's waits.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(smeagol));

		scn.FreepsUseCardAction(smeagol);
		assertEquals(1, scn.GetWoundsOn(smeagol));
		// Aragorn's skirmish goes on: the Shadow player is asked for his skirmish actions.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, runner);

		scn.PassSkirmishActions();
		// Aragorn (8) beats the Goblin Runner (5), whose one vitality goes.
		assertInZone(Zone.DISCARD, runner);
		assertTrue(scn.IsCharAssigned(smeagol));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
