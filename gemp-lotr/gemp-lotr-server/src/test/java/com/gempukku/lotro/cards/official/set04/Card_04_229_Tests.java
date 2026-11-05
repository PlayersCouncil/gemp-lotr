package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_229_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("skirmisher", "4_229");
					put("skirmisher2", "4_229");
					// Easterling Axeman: [raider] Man, Easterling, cost 2, site 4; strength +3 and fierce at 2 burdens
					put("axeman", "4_224");
					// Ulaire Attea, Khamul of the East: [raider] Nazgul, Easterling, unique; strength +3 to each
					// Easterling at 3 burdens and damage +1 at 5, neither of which reaches the twilight pool
					put("khamul", "103_61");
					// Southron Explorer: [raider] Man, Southron and no Easterling; a regroup ability used only on purpose
					put("explorer", "4_250");
					// Easterling Scout: a [men] Man, an Easterling by title only; a skirmish ability used only on purpose
					put("scout", "15_77");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EasterlingSkirmisherStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Easterling Skirmisher
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: <b>Easterling</b>.<br>When you play this minion, you may spot another Easterling to add (1) for each burden (limit (4)).
		*/

		var scn = GetScenario();

		var skirmisher = scn.GetFreepsCard("skirmisher");

		assertEquals("Easterling Skirmisher", skirmisher.getBlueprint().getTitle());
		assertNull(skirmisher.getBlueprint().getSubtitle());
		assertFalse(skirmisher.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, skirmisher.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, skirmisher.getBlueprint().getCulture());
		assertEquals(CardType.MINION, skirmisher.getBlueprint().getCardType());
		assertEquals(Race.MAN, skirmisher.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(skirmisher, Keyword.EASTERLING));
		assertEquals(1, skirmisher.getBlueprint().getTwilightCost());
		assertEquals(5, skirmisher.getBlueprint().getStrength());
		assertEquals(1, skirmisher.getBlueprint().getVitality());
		assertEquals(4, skirmisher.getBlueprint().getSiteNumber());
	}

	@Test
	public void SpottingAnEasterlingManAtOneBurdenItAddsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skirmisher = scn.GetShadowCard("skirmisher");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(skirmisher);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman);
		scn.AddBurdens(1);
		scn.SetTwilight(10);
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.SHADOW_CHARACTERS, axeman);

		scn.FreepsPass();
		scn.ShadowPlayCard(skirmisher);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skirmisher));
		int pool = scn.GetTwilight();
		scn.ShadowAcceptOptionalTrigger();

		// One burden: (1).
		assertEquals(pool + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SpottingAnEasterlingWhoIsNoManAtThreeBurdensItAddsThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skirmisher = scn.GetShadowCard("skirmisher");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToHand(skirmisher);

		scn.StartGame();
		scn.MoveMinionsToTable(khamul);
		scn.AddBurdens(3);
		scn.SetTwilight(10);
		assertEquals(3, scn.GetBurdens());
		assertInZone(Zone.SHADOW_CHARACTERS, khamul);

		scn.FreepsPass();
		scn.ShadowPlayCard(skirmisher);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skirmisher));
		int pool = scn.GetTwilight();
		scn.ShadowAcceptOptionalTrigger();

		// Three burdens: (3).
		assertEquals(pool + 3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtFourBurdensItAddsFourAndAtFiveASecondCopyStillAddsOnlyFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skirmisher = scn.GetShadowCard("skirmisher");
		var skirmisher2 = scn.GetShadowCard("skirmisher2");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(skirmisher, skirmisher2);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman);
		scn.AddBurdens(4);
		scn.SetTwilight(10);
		assertEquals(4, scn.GetBurdens());

		scn.FreepsPass();
		scn.ShadowPlayCard(skirmisher);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skirmisher));
		int pool = scn.GetTwilight();
		scn.ShadowAcceptOptionalTrigger();
		// Four burdens: (4), the limit.
		assertEquals(pool + 4, scn.GetTwilight());

		scn.AddBurdens(1);
		scn.ShadowRefreshDecision();
		assertEquals(5, scn.GetBurdens());
		scn.ShadowPlayCard(skirmisher2);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skirmisher2));
		pool = scn.GetTwilight();
		scn.ShadowAcceptOptionalTrigger();
		// Five burdens, but (limit (4)): (4), not (5).
		assertEquals(pool + 4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DeclinedItAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skirmisher = scn.GetShadowCard("skirmisher");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(skirmisher);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman);
		scn.AddBurdens(2);
		scn.SetTwilight(10);
		assertEquals(2, scn.GetBurdens());

		scn.FreepsPass();
		scn.ShadowPlayCard(skirmisher);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skirmisher));
		int pool = scn.GetTwilight();
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(pool, scn.GetTwilight());
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoOtherEasterlingItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skirmisher = scn.GetShadowCard("skirmisher");
		var explorer = scn.GetShadowCard("explorer");
		var scout = scn.GetShadowCard("scout");
		scn.MoveCardsToHand(skirmisher);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, scout);
		scn.AddBurdens(2);
		scn.SetTwilight(10);
		assertEquals(2, scn.GetBurdens());
		assertInZone(Zone.SHADOW_CHARACTERS, explorer, scout);

		scn.FreepsPass();
		scn.ShadowPlayCard(skirmisher);

		// A Southron and an Easterling by title only beside it, and the Skirmisher itself is no "other" Easterling.
		assertInZone(Zone.SHADOW_CHARACTERS, skirmisher);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(skirmisher));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoBurdenItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skirmisher = scn.GetShadowCard("skirmisher");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(skirmisher);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman);
		scn.SetTwilight(10);
		assertEquals(0, scn.GetBurdens());

		scn.FreepsPass();
		scn.ShadowPlayCard(skirmisher);

		// Another Easterling to spot, but (1) for each of no burdens adds nothing.
		assertInZone(Zone.SHADOW_CHARACTERS, skirmisher);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(skirmisher));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherEasterlingPlayedAfterItSetsOffNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skirmisher = scn.GetShadowCard("skirmisher");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(axeman);

		scn.StartGame();
		scn.MoveMinionsToTable(skirmisher);
		scn.AddBurdens(2);
		scn.SetTwilight(10);
		assertEquals(2, scn.GetBurdens());
		assertInZone(Zone.SHADOW_CHARACTERS, skirmisher);

		scn.FreepsPass();
		int pool = scn.GetTwilight();
		scn.ShadowPlayCard(axeman);

		// 2 to play the Axeman + 2 roaming at site 2, and nothing added for the burdens.
		assertEquals(pool - 4, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
