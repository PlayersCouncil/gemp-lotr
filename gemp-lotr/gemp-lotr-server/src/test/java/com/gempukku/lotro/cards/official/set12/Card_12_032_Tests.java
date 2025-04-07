package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_12_032_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("salve", "12_32");
					put("aragorn", "1_89");

					put("marksman", "1_176");

				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void SalveStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Salve
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: 
		 * Game Text: <b>Spell</b>. To play, spot a [gandalf] Wizard. Bearer must be a companion. Limit 1 per bearer.
		 * <b>Response:</b> If bearer is about to take a wound that would kill him or her, discard this condition to prevent that wound.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("salve");

		assertEquals("Salve", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(card, Keyword.SPELL));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SalveProtectsBearerFromLethalDamage() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var salve = scn.GetFreepsCard("salve");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.FreepsMoveCharToTable(aragorn);
		scn.FreepsAttachCardsTo(aragorn, salve);

		scn.ShadowMoveCharToTable("marksman");

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 3);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetVitality(aragorn));
		assertEquals(Zone.ATTACHED, salve.getZone());
		assertEquals(Zone.FREE_CHARACTERS, aragorn.getZone());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(Zone.DISCARD, salve.getZone());
		assertEquals(Zone.FREE_CHARACTERS, aragorn.getZone());
		assertEquals(1, scn.GetVitality(aragorn));

	}
}
