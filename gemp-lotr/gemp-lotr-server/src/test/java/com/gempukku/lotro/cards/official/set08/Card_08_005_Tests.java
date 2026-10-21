package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_08_005_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "8_5");

					put("dwarfguard", "1_7");   // Dwarf Guard: a Dwarf companion, strength 4, vitality 2, no damage bonus; "To play, spot a Dwarf"
					// Gollum, Nasty Treacherous Creature: strength 5, vitality 4.  "Regroup: Discard Gollum to exert a
					// companion or ally."
					put("gollum", "5_24");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GimliStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Gimli, Counter of Foes
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: <b>Damage +1</b>.<br>For each wound on Gimli, he is strength +1 and <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gimli");

		assertEquals("Gimli", card.getBlueprint().getTitle());
		assertEquals("Counter of Foes", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet()); 
	}

	@Test
	public void ForEachWoundOnGimliHeIsStrengthPlusOneAndDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var dwarfguard = scn.GetFreepsCard("dwarfguard");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli, dwarfguard);

		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		// Another Dwarf companion with a wound of his own, for whom the text does nothing.
		scn.AddWoundsToChar(dwarfguard, 1);
		scn.SkipToPhase(Phase.REGROUP);

		// No wound: his printed 6 and damage +1.
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(1, scn.GetWoundsOn(dwarfguard));
		assertEquals(4, scn.GetStrength(dwarfguard));
		assertEquals(0, scn.GetKeywordCount(dwarfguard, Keyword.DAMAGE));

		// The Shadow player's Gollum exerts him: one wound, by legal play.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(gollum));
		scn.ShadowUseCardAction(gollum);
		// The Dwarf Guard, vitality 2 with a wound, is exhausted and cannot be exerted.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, frodo));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(dwarfguard));
		scn.ShadowChooseCard(gimli);

		// 6 + 1, and damage +1 + 1.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(7, scn.GetStrength(gimli));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(1, scn.GetWoundsOn(dwarfguard));
		assertEquals(4, scn.GetStrength(dwarfguard));
		assertEquals(0, scn.GetKeywordCount(dwarfguard, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// A second wound: 6 + 2, and damage +1 + 2.
		scn.AddWoundsToChar(gimli, 1);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(3, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(4, scn.GetStrength(dwarfguard));
		assertEquals(0, scn.GetKeywordCount(dwarfguard, Keyword.DAMAGE));
	}
}
