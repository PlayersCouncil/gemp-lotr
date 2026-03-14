package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_061_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "103_61");
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireAtteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Ulaire Attea, Khamul of the East
		 * Unique: true
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Easterling.</b>  Fierce.
		* 	While you can spot 3 burdens, each Easterling is strength +3.
		* 	While you can spot 5 burdens, each Easterling is <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Ulaire Attea", card.getBlueprint().getTitle());
		assertEquals("Khamul of the East", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.EASTERLING));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void UlaireAtteaGivesEasterlingsStrengthPlus3With3Burdens() throws DecisionResultInvalidException, CardNotFoundException {
		// Errata: STR modifier is +3 (was +2 in original)
		var scn = GetScenario();

		var card = scn.GetShadowCard("card");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveMinionsToTable(card);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		// Base strength without burdens
		int baseStrength = scn.GetStrength(card);
		assertEquals(12, baseStrength);

		// Add 3 burdens (Frodo starts with 1, so add 2 more)
		scn.AddBurdens(2);
		assertEquals(3, scn.GetBurdens());

		// Errata: each Easterling should now be strength +3
		assertEquals(baseStrength + 3, scn.GetStrength(card));
	}
}
