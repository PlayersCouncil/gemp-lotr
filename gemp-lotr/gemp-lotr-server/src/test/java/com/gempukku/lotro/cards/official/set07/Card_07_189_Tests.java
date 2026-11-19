package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_189_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cur", "7_189");
					put("cur2", "7_189");
					// Morgul Creeper: a [wraith] Orc who is no Nazgul, strength 8; its text reads 3 burdens
					put("creeper", "8_75");
					// Ulaire Lemenya, Lieutenant of Morgul: a [wraith] Nazgul; her text reaches only a companion or ally
					// bearing a [wraith] condition
					put("lemenya", "1_232");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgul; fierce, and his texts read burdens only
					put("khamul", "103_61");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulCurStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Morgul Cur
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: While you can spot a Nazgûl, this minion is strength +2.
		*/

		var scn = GetScenario();

		var cur = scn.GetFreepsCard("cur");

		assertEquals("Morgul Cur", cur.getBlueprint().getTitle());
		assertNull(cur.getBlueprint().getSubtitle());
		assertFalse(cur.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, cur.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, cur.getBlueprint().getCulture());
		assertEquals(CardType.MINION, cur.getBlueprint().getCardType());
		assertEquals(Race.ORC, cur.getBlueprint().getRace());
		assertEquals(3, cur.getBlueprint().getTwilightCost());
		assertEquals(8, cur.getBlueprint().getStrength());
		assertEquals(2, cur.getBlueprint().getVitality());
		assertEquals(4, cur.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsStrengthPlus2OnceAWraithNazgulIsPlayedAndNotForAWraithOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cur = scn.GetShadowCard("cur");
		var cur2 = scn.GetShadowCard("cur2");
		var creeper = scn.GetShadowCard("creeper");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(lemenya);
		assertInHand(lemenya);

		scn.StartGame();
		scn.MoveMinionsToTable(cur, cur2, creeper);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// A [wraith] minion who is no Nazgul is in play.
		assertEquals(8, scn.GetStrength(cur));
		assertEquals(8, scn.GetStrength(cur2));

		scn.ShadowPlayCard(lemenya);
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);

		assertEquals(10, scn.GetStrength(cur)); // 8 + 2
		// Each copy has its own bonus and no more; the other Orc is untouched.
		assertEquals(10, scn.GetStrength(cur2));
		assertEquals(8, scn.GetStrength(creeper));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ARaiderNazgulIsSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cur = scn.GetShadowCard("cur");
		var khamul = scn.GetShadowCard("khamul");

		scn.StartGame();
		scn.MoveMinionsToTable(cur, khamul);
		scn.FreepsPass();

		assertEquals(10, scn.GetStrength(cur)); // 8 + 2
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
