package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_94_007_Tests {

	private final HashMap<String, String> cards = new HashMap<>() {{
		put("aragorn", "1_89"); // Aragorn, Ranger of the North: a non-Ring-bearer companion, for the "chose someone else" case
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_7", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_7"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_7
		 * Type: MetaSite
		 * Intensity: -1
		 * Game Text: You may remove burdens instead of wounds during Sanctuary healing.
		 */
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		assertEquals("Race Text 94_7", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(-1, mod.getBlueprint().getIntensity());
	}

	@Test
	public void RingBearerWoundedWithBurdenOffersPromptAndCanHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		scn.AddBurdens(1);
		scn.SkipToSite(3);

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo));
		scn.FreepsChooseCard(frodo);

		// Both a wound and a burden are available on the chosen Ring-bearer, so the choice is offered.
		assertTrue(scn.FreepsChoiceAvailable("remove a burden"));
		assertTrue(scn.FreepsChoiceAvailable("heal a wound"));
		scn.FreepsChooseOption("Heal a wound");

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetBurdens());
	}

	@Test
	public void RingBearerWoundedWithBurdenOffersPromptAndCanRemoveBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		scn.AddBurdens(1);
		scn.SkipToSite(3);

		scn.FreepsChooseCard(frodo);
		assertTrue(scn.FreepsChoiceAvailable("remove a burden"));
		scn.FreepsChooseOption("Remove a burden");

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
	}

	@Test
	public void RingBearerUnwoundedWithBurdenRemovesBurdenWithoutPrompt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.AddBurdens(1);
		scn.SkipToSite(3);

		// No wound to heal, so the Ring-bearer is only selectable because of the burden.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo));
		scn.FreepsChooseCard(frodo);

		// Only the burden-removal option exists, so it happens directly, with no follow-up prompt.
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
	}

	@Test
	public void RingBearerWoundedWithNoBurdenHealsWithoutPrompt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		scn.SkipToSite(3);

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo));
		scn.FreepsChooseCard(frodo);

		// Only the heal option exists (no burdens to remove), so it happens directly.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
	}

	@Test
	public void OtherCompanionChosenHealsNormallyWithNoPrompt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddBurdens(1);
		scn.SkipToSite(3);

		// Both the Ring-bearer (burden path) and Aragorn (wound) are selectable this heal.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		scn.FreepsChooseCard(aragorn);

		// The burden option only ever applies to the Ring-bearer, so choosing someone else just heals.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetBurdens());
	}

	@Test
	public void NotOfferedToNonOwner() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.AddBurdens(1);
		scn.SkipToSite(3);

		// The mod's default "player" scope is "owner" (Shadow here); the Free Peoples player, who is
		// doing the healing at this Sanctuary site, is not offered the burden-removal option, so the
		// unwounded Ring-bearer is not made selectable by the burden alone.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
	}
}
