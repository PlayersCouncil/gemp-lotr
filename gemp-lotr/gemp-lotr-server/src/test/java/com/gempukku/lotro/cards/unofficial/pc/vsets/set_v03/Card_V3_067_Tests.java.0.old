package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_V3_067_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("illwind", "103_67");
					put("witchking", "1_237");
					put("rider1", "12_161");
					put("rider2", "12_161");
					put("rider3", "12_161");

					put("gimli", "1_13");
					put("legolas", "1_50");
					put("sam", "1_311");      // Strength 3
					put("aragorn", "1_89");   // Strength 8
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IllWindStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Ill Wind
		 * Unique: true
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: The second time you play a Nazgul each Shadow phase, you may hinder a wounded
		 * companion (except a companion with the highest strength).
		 * If you can spot The Witch-king, exert that companion first.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("illwind");

		assertEquals("Ill Wind", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}



// ======== TRIGGER TIMING TESTS ========

	@Test
	public void IllWindDoesNotTriggerOnFirstNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var illwind = scn.GetShadowCard("illwind");
		var rider1 = scn.GetShadowCard("rider1");
		var sam = scn.GetFreepsCard("sam");

		scn.MoveCardsToSupportArea(illwind);
		scn.MoveCardsToHand(rider1);
		scn.MoveCompanionsToTable(sam);
		scn.AddWoundsToChar(sam, 1);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		// Play first Nazgul
		scn.ShadowPlayCard(rider1);

		// Should NOT trigger on first Nazgul
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void IllWindTriggersOnSecondNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var illwind = scn.GetShadowCard("illwind");
		var rider1 = scn.GetShadowCard("rider1");
		var rider2 = scn.GetShadowCard("rider2");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(illwind);
		scn.MoveCardsToHand(rider1, rider2);
		scn.MoveCompanionsToTable(sam, aragorn);  // Aragorn is highest strength
		scn.AddWoundsToChar(sam, 1);  // Sam wounded, valid target

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		// Play first Nazgul
		scn.ShadowPlayCard(rider1);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		// Play second Nazgul
		scn.ShadowPlayCard(rider2);

		// Should trigger on second Nazgul
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void IllWindDoesNotTriggerOnThirdNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var illwind = scn.GetShadowCard("illwind");
		var rider1 = scn.GetShadowCard("rider1");
		var rider2 = scn.GetShadowCard("rider2");
		var rider3 = scn.GetShadowCard("rider3");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(illwind);
		scn.MoveCardsToHand(rider1, rider2, rider3);
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.AddWoundsToChar(sam, 1);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		// Play first Nazgul
		scn.ShadowPlayCard(rider1);

		// Play second Nazgul - decline trigger
		scn.ShadowPlayCard(rider2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowDeclineOptionalTrigger();

		// Play third Nazgul
		scn.ShadowPlayCard(rider3);

		// Should NOT trigger on third
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}

// ======== TARGET RESTRICTION TESTS ========

	@Test
	public void IllWindCannotTargetHighestStrengthCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var illwind = scn.GetShadowCard("illwind");
		var rider1 = scn.GetShadowCard("rider1");
		var rider2 = scn.GetShadowCard("rider2");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(illwind);
		scn.MoveCardsToHand(rider1, rider2);
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.AddWoundsToChar(sam, 1);
		scn.AddWoundsToChar(aragorn, 1);  // Both wounded

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(rider1);
		scn.ShadowPlayCard(rider2);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Sam (3 str) should be valid, Aragorn (8 str, highest) should not
		assertTrue(scn.IsHindered(sam));
	}

	@Test
	public void IllWindNoValidTargetsIfOnlyHighestStrengthIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var illwind = scn.GetShadowCard("illwind");
		var rider1 = scn.GetShadowCard("rider1");
		var rider2 = scn.GetShadowCard("rider2");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(illwind);
		scn.MoveCardsToHand(rider1, rider2);
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.AddWoundsToChar(aragorn, 1);  // Only highest strength is wounded

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(rider1);
		scn.ShadowPlayCard(rider2);

		// Only wounded companion is Aragorn (highest strength) - no valid targets
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertFalse(scn.IsHindered(sam));
		assertFalse(scn.IsHindered(aragorn));
	}

// ======== WITCH-KING BONUS TEST ========

	@Test
	public void IllWindExertsTargetBeforeHinderingIfWitchKingSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var illwind = scn.GetShadowCard("illwind");
		var witchking = scn.GetShadowCard("witchking");
		var rider1 = scn.GetShadowCard("rider1");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(illwind);
		scn.MoveCardsToHand(witchking, rider1);
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.AddWoundsToChar(sam, 1);  // 1 wound

		scn.StartGame();
		scn.SetTwilight(15);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(1, scn.GetWoundsOn(sam));

		// Play Witch-king (first Nazgul)
		scn.ShadowPlayCard(witchking);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		// Play Black Rider (second Nazgul)
		scn.ShadowPlayCard(rider1);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		// Sam is only valid target - auto-selected

		// Sam should be exerted (2 wounds now) AND hindered
		assertEquals(2, scn.GetWoundsOn(sam));
		assertTrue(scn.IsHindered(sam));
	}

	@Test
	public void IllWindDoesNotExertIfWitchKingNotSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var illwind = scn.GetShadowCard("illwind");
		var rider1 = scn.GetShadowCard("rider1");
		var rider2 = scn.GetShadowCard("rider2");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(illwind);
		scn.MoveCardsToHand(rider1, rider2);
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.AddWoundsToChar(sam, 1);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(1, scn.GetWoundsOn(sam));

		scn.ShadowPlayCard(rider1);
		scn.ShadowPlayCard(rider2);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Without Witch-king, Sam should just be hindered, not exerted
		assertEquals(1, scn.GetWoundsOn(sam));  // Still just 1 wound
		assertTrue(scn.IsHindered(sam));
	}

// ======== BASIC EFFECT TEST ========

	@Test
	public void IllWindHindersChosenCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var illwind = scn.GetShadowCard("illwind");
		var rider1 = scn.GetShadowCard("rider1");
		var rider2 = scn.GetShadowCard("rider2");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(illwind);
		scn.MoveCardsToHand(rider1, rider2);
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.AddWoundsToChar(sam, 1);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertFalse(scn.IsHindered(sam));

		scn.ShadowPlayCard(rider1);
		scn.ShadowPlayCard(rider2);
		scn.ShadowAcceptOptionalTrigger();
		// Sam auto-selected

		assertTrue(scn.IsHindered(sam));
		assertFalse(scn.IsHindered(aragorn));
	}

	// ======== TIED HIGHEST STRENGTH TESTS ========

	@Test
	public void IllWindExcludesAllCompanionsTiedForHighestStrength() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var illwind = scn.GetShadowCard("illwind");
		var rider1 = scn.GetShadowCard("rider1");
		var rider2 = scn.GetShadowCard("rider2");
		var sam = scn.GetFreepsCard("sam");        // Strength 3
		var gimli = scn.GetFreepsCard("gimli");    // Strength 6
		var legolas = scn.GetFreepsCard("legolas"); // Strength 6

		scn.MoveCardsToSupportArea(illwind);
		scn.MoveCardsToHand(rider1, rider2);
		scn.MoveCompanionsToTable(sam, gimli, legolas);
		// Wound all three
		scn.AddWoundsToChar(sam, 1);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(legolas, 1);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(rider1);
		scn.ShadowPlayCard(rider2);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.IsHindered(sam));
		assertFalse(scn.IsHindered(gimli));
		assertFalse(scn.IsHindered(legolas));
	}

	@Test
	public void IllWindNoValidTargetsIfAllWoundedCompanionsTiedForHighest() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var illwind = scn.GetShadowCard("illwind");
		var rider1 = scn.GetShadowCard("rider1");
		var rider2 = scn.GetShadowCard("rider2");
		var gimli = scn.GetFreepsCard("gimli");    // Strength 6
		var legolas = scn.GetFreepsCard("legolas"); // Strength 6

		scn.MoveCardsToSupportArea(illwind);
		scn.MoveCardsToHand(rider1, rider2);
		scn.MoveCompanionsToTable(gimli, legolas);
		// Wound both - they're tied for highest strength
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(legolas, 1);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(legolas));


		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(rider1);
		scn.ShadowPlayCard(rider2);

		// Gimli and Legolas are both wounded but tied for highest strength
		// Both excluded - no valid targets
		scn.ShadowAcceptOptionalTrigger();
		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(gimli));
		assertFalse(scn.IsHindered(legolas));
	}

	@Test
	public void IllWindPermitsHinderingOfMiddlingStrengthCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(aragorn, gimli, legolas);

		var wind = scn.GetShadowCard("illwind");
		var rider1 = scn.GetShadowCard("rider1");
		var rider2 = scn.GetShadowCard("rider2");
		var rider3 = scn.GetShadowCard("rider3");
		scn.MoveCardsToHand(rider1, rider2, rider3);
		scn.MoveCardsToSupportArea(wind);

		scn.StartGame();

		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(legolas, 1);
		
		scn.SetTwilight(50);
		scn.FreepsPass();

		assertInZone(Zone.SUPPORT, wind);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(gimli));
		assertFalse(scn.IsHindered(legolas));

		assertTrue(scn.ShadowPlayAvailable(rider1));
		assertTrue(scn.ShadowPlayAvailable(rider2));
		assertTrue(scn.ShadowPlayAvailable(rider3));

		scn.ShadowPlayCard(rider1);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		scn.ShadowPlayCard(rider2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowHasCardChoicesAvailable(gimli, legolas, frodo));
		//As Aragorn has the highest strength (8 vs 6/6/4), he is ineligible
		assertFalse(scn.ShadowHasCardChoicesAvailable(aragorn));

		scn.ShadowChooseCard(gimli);
		assertTrue(scn.IsHindered(gimli));

		scn.ShadowPlayCard(rider3);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}
}
