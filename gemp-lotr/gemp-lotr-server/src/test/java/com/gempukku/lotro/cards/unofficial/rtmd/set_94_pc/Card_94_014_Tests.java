package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_94_014_Tests {

	private final HashMap<String, String> cards = new HashMap<>() {{
	}};

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_14"
		);
	}

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_14", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_14
		 * Type: MetaSite
		 * Game Text: Shadow: Remove 3 threats to add a burden.
		 */

		var scn = GetShadowScenario();
		var mod = scn.GetShadowCard("mod");
		assertEquals("Race Text 94_14", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(1, mod.getBlueprint().getIntensity());
	}

	@Test
	public void RemovingThreeThreatsAddsABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var mod = scn.GetShadowCard("mod");

		scn.StartGame();
		scn.AddThreats(3);
		scn.SkipToPhase(Phase.SHADOW);

		int b = scn.GetBurdens();
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(mod));

		scn.ShadowUseCardAction(mod);

		assertEquals(0, scn.GetThreats());
		assertEquals(b + 1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void UnavailableWithFewerThanThreeThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var mod = scn.GetShadowCard("mod");

		scn.StartGame();
		scn.AddThreats(2);
		scn.SkipToPhase(Phase.SHADOW);

		assertFalse(scn.ShadowActionAvailable(mod));
	}

	@Test
	public void ActionIsOfferedToWhicheverPlayerIsCurrentlyShadowRegardlessOfWhoOwnsTheMod() throws DecisionResultInvalidException, CardNotFoundException {
		// No "your" in the text. The Shadow phase is Shadow-role-only (rule: only the current
		// Shadow-role player is ever asked anything in it), and a MetaSite's Activated ability
		// carries no owner restriction of its own - so this is available to whichever physical
		// player currently holds the Shadow role, every turn, regardless of which physical player
		// owns the printed mod card. Here the mod is owned by the player who starts as Free
		// Peoples ("freeps"); on turn 1 the *other* physical player is Shadow-active and still
		// receives the action.
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");

		scn.StartGame();
		scn.AddThreats(3);
		scn.SkipToPhase(Phase.SHADOW);

		// The owner (Freeps-active this turn) is not asked anything during the Shadow phase...
		assertFalse(scn.FreepsActionAvailable(mod));
		// ...but the non-owner, currently playing the Shadow role, is.
		assertTrue(scn.ShadowActionAvailable(mod));

		int b = scn.GetBurdens();
		scn.ShadowUseCardAction(mod);
		assertEquals(0, scn.GetThreats());
		assertEquals(b + 1, scn.GetBurdens());
	}
}
