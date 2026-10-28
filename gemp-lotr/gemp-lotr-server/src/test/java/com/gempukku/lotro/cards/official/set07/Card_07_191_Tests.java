package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_191_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("detachment", "7_191");
					put("detachment2", "7_191");
					// Morgul Creeper: a [wraith] Orc who is no Nazgul; its text reads 3 burdens
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
	public void MorgulDetachmentStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Morgul Detachment
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 7
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 15
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: While you can spot a Nazgûl, this minion is <b>fierce</b> and <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var detachment = scn.GetFreepsCard("detachment");

		assertEquals("Morgul Detachment", detachment.getBlueprint().getTitle());
		assertNull(detachment.getBlueprint().getSubtitle());
		assertFalse(detachment.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, detachment.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, detachment.getBlueprint().getCulture());
		assertEquals(CardType.MINION, detachment.getBlueprint().getCardType());
		assertEquals(Race.ORC, detachment.getBlueprint().getRace());
		assertEquals(7, detachment.getBlueprint().getTwilightCost());
		assertEquals(15, detachment.getBlueprint().getStrength());
		assertEquals(4, detachment.getBlueprint().getVitality());
		assertEquals(4, detachment.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsFierceAndDamagePlus1OnceAWraithNazgulIsPlayedAndNotForAWraithOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var detachment = scn.GetShadowCard("detachment");
		var detachment2 = scn.GetShadowCard("detachment2");
		var creeper = scn.GetShadowCard("creeper");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(lemenya);
		assertInHand(lemenya);

		scn.StartGame();
		scn.MoveMinionsToTable(detachment, detachment2, creeper);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// A [wraith] minion who is no Nazgul is in play.
		assertFalse(scn.HasKeyword(detachment, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(detachment, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(detachment2, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(detachment2, Keyword.DAMAGE));

		scn.ShadowPlayCard(lemenya);
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);

		assertTrue(scn.HasKeyword(detachment, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(detachment, Keyword.DAMAGE));
		// Each copy has its own bonus and no more; the other Orc is untouched.
		assertTrue(scn.HasKeyword(detachment2, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(detachment2, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(creeper, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(creeper, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ARaiderNazgulIsSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var detachment = scn.GetShadowCard("detachment");
		var khamul = scn.GetShadowCard("khamul");

		scn.StartGame();
		scn.MoveMinionsToTable(detachment, khamul);
		scn.FreepsPass();

		assertTrue(scn.HasKeyword(detachment, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(detachment, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
