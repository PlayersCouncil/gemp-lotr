package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_062_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axeman", "8_62");
					put("attacker", "7_141");   // Easterling Attacker: [raider] Easterling Man minion, strength 6; a Skirmish ability only
					put("smeagol", "7_71");     // Smeagol, Always Helps: companion, "To play, add a burden"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HeavyAxemanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Heavy Axeman
		 * Unique: True
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Easterling</b>.<br>While you can spot 4 burdens, Heavy Axeman is strength +4 and <b>fierce</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("axeman");

		assertEquals("Heavy Axeman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.EASTERLING));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtFourBurdensHeIsStrengthPlus4AndFierceAndAtThreeHeIsNeither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCardsToHand(smeagol);

		var axeman = scn.GetShadowCard("axeman");
		var attacker = scn.GetShadowCard("attacker");

		scn.StartGame();
		scn.MoveMinionsToTable(axeman, attacker);
		scn.AddBurdens(3);
		// A wound on the Ring-bearer and a threat, which are no burdens.
		scn.AddWoundsToChar(frodo, 1);
		scn.AddThreats(1);
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetThreats());
		assertEquals(11, scn.GetStrength(axeman));
		assertFalse(scn.HasKeyword(axeman, Keyword.FIERCE));
		assertEquals(6, scn.GetStrength(attacker));
		assertFalse(scn.HasKeyword(attacker, Keyword.FIERCE));

		// Smeagol's play adds the fourth burden.
		scn.FreepsPlayCard(smeagol);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertEquals(4, scn.GetBurdens());

		// 11 + 4
		assertEquals(15, scn.GetStrength(axeman));
		assertTrue(scn.HasKeyword(axeman, Keyword.FIERCE));
		// The other Easterling is untouched.
		assertEquals(6, scn.GetStrength(attacker));
		assertFalse(scn.HasKeyword(attacker, Keyword.FIERCE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
