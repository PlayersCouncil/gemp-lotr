package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_108_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wisp", "6_108");
					put("wisp2", "6_108");
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, not twilight; a skirmish ability only
					put("nelya", "2_84");       // Úlairë Nelya: [wraith] twilight Nazgûl; fierce and damage +1 only at 2 burdens
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, not twilight, no text
					put("cover", "103_95");     // Cover of Darkness, Omen of Horror: a [sauron] twilight condition, no minion; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WispofPaleSheenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Wisp of Pale Sheen
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Wraith
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Twilight</b>.<br><b>Regroup:</b> Spot another [sauron] or twilight minion and discard this minion to add a burden.
		*/

		var scn = GetScenario();

		var wisp = scn.GetFreepsCard("wisp");

		assertEquals("Wisp of Pale Sheen", wisp.getBlueprint().getTitle());
		assertNull(wisp.getBlueprint().getSubtitle());
		assertFalse(wisp.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, wisp.getBlueprint().getSide());
		assertEquals(Culture.SAURON, wisp.getBlueprint().getCulture());
		assertEquals(CardType.MINION, wisp.getBlueprint().getCardType());
		assertEquals(Race.WRAITH, wisp.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(wisp, Keyword.TWILIGHT));
		assertEquals(3, wisp.getBlueprint().getTwilightCost());
		assertEquals(8, wisp.getBlueprint().getStrength());
		assertEquals(2, wisp.getBlueprint().getVitality());
		assertEquals(4, wisp.getBlueprint().getSiteNumber());
	}

	@Test
	public void RegroupAbilityDiscardsItToAddABurdenWhileASauronMinionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wisp = scn.GetShadowCard("wisp");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveMinionsToTable(wisp, soldier);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.SHADOW_CHARACTERS, wisp, soldier);
		assertTrue(scn.ShadowActionAvailable(wisp));

		scn.ShadowUseCardAction(wisp);

		assertInDiscard(wisp);
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ATwilightMinionOfAnotherCultureIsSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wisp = scn.GetShadowCard("wisp");
		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.MoveMinionsToTable(wisp, nelya);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(wisp));

		scn.ShadowUseCardAction(wisp);

		assertInDiscard(wisp);
		assertInZone(Zone.SHADOW_CHARACTERS, nelya);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedBesideOnlyAMoriaMinionAndASauronTwilightCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wisp = scn.GetShadowCard("wisp");
		var troop = scn.GetShadowCard("troop");
		var cover = scn.GetShadowCard("cover");
		scn.MoveCardsToSupportArea(cover);
		assertInZone(Zone.SUPPORT, cover);

		scn.StartGame();
		scn.MoveMinionsToTable(wisp, troop);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		// Itself is no other minion; the Troop is neither [sauron] nor twilight; the condition is no minion.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, wisp, troop);
		assertFalse(scn.ShadowActionAvailable(wisp));
	}

	@Test
	public void ASecondCopyIsAnotherMinionAndOnlyTheCopyUsedIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wisp = scn.GetShadowCard("wisp");
		var wisp2 = scn.GetShadowCard("wisp2");

		scn.StartGame();
		scn.MoveMinionsToTable(wisp, wisp2);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(wisp));

		scn.ShadowUseCardAction(wisp);

		assertInDiscard(wisp);
		assertInZone(Zone.SHADOW_CHARACTERS, wisp2);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// The copy left has no other minion to spot.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(wisp2));
	}
}
