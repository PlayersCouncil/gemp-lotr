package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_92_033_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		// Gimli, Faithful Companion (7_6): FP Dwarf companion, game text contains "initiative"
		put("gimli", "7_6");
		// Black Numenorean (8_49): Shadow Raider minion, game text contains "initiative"
		put("blacknum", "8_49");
		// Ulaire Enquea, Lieutenant of Morgul (1_231): Shadow Nazgul, no "initiative" in game text
		put("enquea", "1_231");
	}};

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_33"
		);
	}

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_33", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_33
		 * Type: MetaSite
		 * Game Text: At the start of each of your Shadow phases, you may take one card
		 *   with "initiative" in its game text into hand from your draw deck.
		 */

		var scn = GetShadowScenario();

		var card = scn.GetShadowCard("mod");

		assertEquals("Race Text 92_33", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void TakesInitiativeCardFromDeckAtStartOfShadow() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var blacknum = scn.GetShadowCard("blacknum");
		var gimli = scn.GetShadowCard("gimli");
		var enquea = scn.GetShadowCard("enquea");

		// Leave blacknum, gimli, and enquea in the draw deck (default location)

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();
		scn.SkipToPhase(Phase.SHADOW);

		// Optional trigger fires at start of shadow phase
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowDismissRevealedCards();

		// Should be able to choose between blacknum and gimli (both have "initiative")
		// but not enquea (no "initiative" in game text)
		scn.ShadowHasCardChoicesAvailable(blacknum, gimli);
		scn.ShadowHasCardChoicesNotAvailable(enquea);
		scn.ShadowChooseCard(blacknum);

		assertInZone(Zone.HAND, blacknum);
		assertInZone(Zone.DECK, gimli);
	}

	@Test
	public void CanDeclineOptionalTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var blacknum = scn.GetShadowCard("blacknum");

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();
		scn.SkipToPhase(Phase.SHADOW);

		int handBefore = scn.GetShadowHandCount();

		scn.ShadowDeclineOptionalTrigger();

		assertEquals(handBefore, scn.GetShadowHandCount());
		assertInZone(Zone.DECK, blacknum);
	}

	@Test
	public void FreepsOwnerCannotUseTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		// When Freeps owns the modifier, the OwnerIsShadow requirement prevents use
		var scn = GetFreepsScenario();

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();
		scn.SkipToPhase(Phase.SHADOW);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}
}
