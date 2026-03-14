package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_075_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lemenya", "103_75");
					put("nazgul2", "1_234"); // Ulaire Nertea
					put("nazgul3", "1_231"); // Ulaire Cantea
					put("guard", "1_7");
					put("gimli", "1_12");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireLemenyaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Ulaire Lemenya, Anointed with Terror
		 * Unique: true
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: Fierce.
		* 	Each character skirmishing this minion is strength -1 for each other Nazgul you can spot
		*   (or -1 for each other companion you can spot if you can spot 6 companions).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lemenya");

		assertEquals("Ulaire Lemenya", card.getBlueprint().getTitle());
		assertEquals("Anointed with Terror", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void LemenyaReducesOpponentStrengthByOtherNazgulCount() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var nazgul2 = scn.GetShadowCard("nazgul2");
		var nazgul3 = scn.GetShadowCard("nazgul3");
		scn.MoveMinionsToTable(lemenya, nazgul2, nazgul3);

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();

		// Gimli base strength is 6.
		// Lemenya's errata: -1 for each OTHER Nazgul (changed to other,Nazgul filter).
		// We have 2 other Nazgul on table (nazgul2 and nazgul3).
		// The debuff only applies during skirmish with Lemenya.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, lemenya);
		scn.FreepsResolveSkirmish(gimli);

		// During skirmish, Gimli should be STR 6 - 2 = 4 (2 other Nazgul spotted)
		assertEquals(4, scn.GetStrength(gimli));
	}
}
