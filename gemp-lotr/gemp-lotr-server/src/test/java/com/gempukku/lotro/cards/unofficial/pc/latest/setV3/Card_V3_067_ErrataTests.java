package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_067_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("illwind", "103_67");
					put("nazgul1", "1_232"); // Ulaire Enquea
					put("nazgul2", "1_234"); // Ulaire Nertea
					put("guard", "1_7");
					put("gimli", "1_12");
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
		 * Game Text: The second time you play a Nazgul each Shadow phase, you may hinder a wounded companion.
		 *   The Free Peoples player may make you exert another companion to prevent this
		 *   (or wound if you can spot The Witch-king).
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

	@Test
	public void IllWindDoesNotTriggerOnFirstNazgulPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var illwind = scn.GetShadowCard("illwind");
		var nazgul1 = scn.GetShadowCard("nazgul1");
		var nazgul2 = scn.GetShadowCard("nazgul2");
		scn.MoveCardsToSupportArea(illwind);
		scn.MoveCardsToHand(nazgul1, nazgul2);

		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.AddWoundsToChar(guard, 1);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		// Play first Nazgul -- should NOT trigger Ill Wind
		scn.ShadowPlayCard(nazgul1);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void IllWindTriggersOnSecondNazgulPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var illwind = scn.GetShadowCard("illwind");
		var nazgul1 = scn.GetShadowCard("nazgul1");
		var nazgul2 = scn.GetShadowCard("nazgul2");
		scn.MoveCardsToSupportArea(illwind);
		scn.MoveCardsToHand(nazgul1, nazgul2);

		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		// Wound the guard so it can be a valid target for Ill Wind
		scn.AddWoundsToChar(guard, 1);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		// Play first Nazgul
		scn.ShadowPlayCard(nazgul1);

		// Play second Nazgul -- SHOULD trigger Ill Wind (errata: triggers on 2nd Nazgul played)
		scn.ShadowPlayCard(nazgul2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
	}
}
