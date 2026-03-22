package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_92_030_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		put("nostranger1", "1_108");
		put("nostranger2", "1_108");
	}};

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_30"
		);
	}

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_30", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_30
		 * Type: MetaSite
		 * Game Text: At the start of each of your Shadow phases, you may remove (2) to draw a card.
		 */

		var scn = GetShadowScenario();

		var card = scn.GetShadowCard("mod");

		assertEquals("Race Text 92_30", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void RemovesTwilightToDrawAtStartOfShadow() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		scn.StartGame();
		scn.SetTwilight(5);
		scn.FreepsPassCurrentPhaseAction();
		scn.SkipToPhase(Phase.SHADOW);

		int handBefore = scn.GetShadowHandCount();
		int twilightBefore = scn.GetTwilight();

		// Optional trigger at start of shadow phase — accept it
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(handBefore + 1, scn.GetShadowHandCount());
		assertEquals(twilightBefore - 2, scn.GetTwilight());
	}

	@Test
	public void CanDeclineOptionalTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		scn.StartGame();
		scn.SetTwilight(5);
		scn.FreepsPassCurrentPhaseAction();
		scn.SkipToPhase(Phase.SHADOW);

		int handBefore = scn.GetShadowHandCount();
		int twilightBefore = scn.GetTwilight();

		scn.ShadowDeclineOptionalTrigger();

		assertEquals(handBefore, scn.GetShadowHandCount());
		assertEquals(twilightBefore, scn.GetTwilight());
	}

	@Test
	public void NotEnoughTwilightMeansNoTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var frodo = scn.GetRingBearer();
		var nostranger1 = scn.GetFreepsCard("nostranger1");
		var nostranger2 = scn.GetFreepsCard("nostranger2");

		//Reducing the shadow number of the site to 0
		scn.AttachCardsTo(frodo, nostranger1, nostranger2);

		scn.StartGame();

		scn.SkipToPhase(Phase.SHADOW);
		assertEquals(1, scn.GetTwilight());

		// With insufficient twilight, the optional trigger shouldn't fire
		// (no decision presented — goes straight to shadow actions)
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void OpponentNotAffected() throws DecisionResultInvalidException, CardNotFoundException {
		// Freeps owns the modifier; Shadow should not get the trigger
		var scn = GetFreepsScenario();

		scn.StartGame();
		scn.SetTwilight(5);
		scn.FreepsPassCurrentPhaseAction();
		scn.SkipToPhase(Phase.SHADOW);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}
}
