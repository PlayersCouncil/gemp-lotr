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

public class Card_92_020_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		// Úlairë Enquëa, Lieutenant of Morgul (1_231): twilight 6, strength 11, vitality 4
		put("enquea", "1_231");
		// Úlairë Attëa, Keeper of Dol Guldur (1_229): twilight 6, strength 12, vitality 3
		put("attea", "1_229");
		// Úlairë Lemenya, Lieutenant of Morgul (1_232): twilight 4, strength 9, vitality 2
		put("lemenya", "1_232");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_20", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_20"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_20
		 * Type: MetaSite
		 * Game Text: Shadow: Exert a Nazgul twice to play a Nazgul from your discard pile.
		 */

		var scn = GetShadowScenario();

		var card = scn.GetShadowCard("mod");

		assertEquals("Race Text 92_20", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void ExertNazgulTwiceToPlayNazgulFromDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var enquea = scn.GetShadowCard("enquea");
		var attea = scn.GetShadowCard("attea");

		scn.MoveMinionsToTable(enquea);
		scn.MoveCardsToDiscard(attea);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();
		scn.SkipToPhase(Phase.SHADOW);

		var mod = scn.GetShadowCard("mod");
		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);

		assertEquals(2, scn.GetWoundsOn(enquea));
		assertInZone(Zone.SHADOW_CHARACTERS, attea);
	}

	@Test
	public void AbilityNotAvailableIfNazgulCantExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var attea = scn.GetShadowCard("attea");

		// Lemenya has vitality 2 — exerting twice not available
		scn.MoveMinionsToTable(lemenya);
		scn.MoveCardsToDiscard(attea);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();
		scn.SkipToPhase(Phase.SHADOW);

		var mod = scn.GetShadowCard("mod");
		assertFalse(scn.ShadowActionAvailable(mod));
	}

	@Test
	public void AbilityNotAvailableIfCantAffordNazgulInDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var enquea = scn.GetShadowCard("enquea");
		var attea = scn.GetShadowCard("attea");

		scn.MoveMinionsToTable(enquea);
		scn.MoveCardsToDiscard(attea);

		scn.StartGame();
		scn.SetTwilight(0);
		scn.FreepsPassCurrentPhaseAction();
		scn.SkipToPhase(Phase.SHADOW);

		var mod = scn.GetShadowCard("mod");
		assertFalse(scn.ShadowActionAvailable(mod));
	}

	@Test
	public void AbilityNotAvailableIfNoNazgulInDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var enquea = scn.GetShadowCard("enquea");

		scn.MoveMinionsToTable(enquea);
		// No nazgul in discard

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();
		scn.SkipToPhase(Phase.SHADOW);

		var mod = scn.GetShadowCard("mod");
		assertFalse(scn.ShadowActionAvailable(mod));
	}

	@Test
	public void FreepsCannotUseAbility() throws DecisionResultInvalidException, CardNotFoundException {
		// When Freeps owns the modifier, the OwnerIsShadow requirement prevents use
		var scn = GetFreepsScenario();

		var enquea = scn.GetShadowCard("enquea");
		var attea = scn.GetShadowCard("attea");

		scn.MoveMinionsToTable(enquea);
		scn.MoveCardsToDiscard(attea);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();
		scn.SkipToPhase(Phase.SHADOW);

		var mod = scn.GetFreepsCard("mod");
		assertFalse(scn.ShadowActionAvailable(mod));
	}
}
