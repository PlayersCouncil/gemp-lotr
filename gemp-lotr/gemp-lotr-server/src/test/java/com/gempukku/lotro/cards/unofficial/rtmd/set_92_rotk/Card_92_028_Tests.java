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

public class Card_92_028_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		// Mithril-coat (2_105): Shire artifact, twilight 2, bearer must be ring-bearer
		put("mithril", "2_105");
		// Library of Orthanc: Isengard Shadow artifact, twilight 2
		put("library", "9_39");
		// Gimli, Son of Gloin (1_13): Dwarven companion
		put("gimli", "1_13");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_28", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_28"
		);
	}

	protected VirtualTableScenario GetDualScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_28", "92_28"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_28
		 * Type: MetaSite
		 * Game Text: Fellowship or Shadow: Play an artifact from your draw deck (limit once per turn).
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_28", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void FreepsPlaysArtifactFromDeckInFellowship() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mithril = scn.GetFreepsCard("mithril");
		var mod = scn.GetFreepsCard("mod");

		// Leave mithril in draw deck (default location)
		scn.StartGame();

		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCard(mithril);

		assertInZone(Zone.ATTACHED, mithril);

		//Limit once per turn
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(mod));
	}

	@Test
	public void ShadowPlaysArtifactFromDeckInShadowPhase() throws DecisionResultInvalidException, CardNotFoundException {
		//Is not owner-gated
		var scn = GetShadowScenario();

		var library = scn.GetShadowCard("library");
		var mod = scn.GetShadowCard("mod");

		// Leave library in draw deck
		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();
		scn.SkipToPhase(Phase.SHADOW);

		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCards(library);

		assertInZone(Zone.SUPPORT, library);

		//Limit once per turn
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mod));
	}

	@Test
	public void OpponentCannotUse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mithril = scn.GetFreepsCard("mithril");
		var mod = scn.GetShadowCard("mod");

		// Leave mithril in draw deck (default location)
		scn.StartGame();

		assertFalse(scn.FreepsActionAvailable(mod));
	}

	@Test
	public void FreepsCannotUseOpponentsDuringFellowshipPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDualScenario();

		var mod = scn.GetFreepsCard("mod");
		var shadowMod = scn.GetShadowCard("mod");

		// Leave mithril in draw deck (default location)
		scn.StartGame();

		assertTrue(scn.FreepsActionAvailable(mod));
		//TODO: Need a better way of determining actions from cards with the same name
		//assertFalse(scn.FreepsActionAvailable(shadowMod));
	}
}
