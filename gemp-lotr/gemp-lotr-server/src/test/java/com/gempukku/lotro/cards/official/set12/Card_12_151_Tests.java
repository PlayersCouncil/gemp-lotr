package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_151_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("desecrator", "12_151");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound [gondor] Man, resistance 6; a maneuver ability never used here
					put("legolas", "1_50");     // Legolas, Greenleaf: unbound [elven] Elf, resistance 6; an archery ability never used here
					put("gimli", "1_13");       // Gimli, Son of Gloin: unbound [dwarven] Dwarf, resistance 6; a skirmish ability never used here
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound by rule, resistance 5
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukDesecratorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Uruk Desecrator
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 11
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>Each unbound companion is resistance -2 for each wound he or she has.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("desecrator");

		assertEquals("Uruk Desecrator", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachUnboundCompanionIsResistanceMinusTwoForEachWoundHeHas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var desecrator = scn.GetShadowCard("desecrator");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, legolas, gimli, sam);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(legolas, 1);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(sam, 1);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(legolas));
		assertEquals(6, scn.GetResistance(gimli));
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(sam));

		scn.MoveMinionsToTable(desecrator);

		// Two wounds and one.
		assertEquals(2, scn.GetResistance(aragorn));
		assertEquals(4, scn.GetResistance(legolas));
		// No wound of his own.
		assertEquals(6, scn.GetResistance(gimli));
		// The Ring-bearer, and a Ring-bound companion who is not the Ring-bearer.
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(sam));

		// Legolas exerts for his archery ability (and wounds the Desecrator, the only minion): a second wound.
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsUseCardAction(legolas);
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(2, scn.GetResistance(legolas));
		assertEquals(2, scn.GetResistance(aragorn));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}
}
