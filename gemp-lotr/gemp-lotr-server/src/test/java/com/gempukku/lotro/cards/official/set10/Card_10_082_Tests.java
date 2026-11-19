package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_082_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("patroller", "10_82");
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, damage +1 and no other text; the other minion, never fierce
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc; text only on a weapon PLAYED on him
					put("banner", "6_98");      // Banner of the Eye: [sauron] possession (hand weapon) on a [sauron] Orc; text only on a skirmish won

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion; skirmish ability only
					put("armor", "1_8");        // Dwarven Armor: [dwarven] possession, cost 0, on a Dwarf; changes no number
					put("ring", "17_4");        // Ring of Artifice: [dwarven] ARTIFACT on a Dwarf, no possession
					put("toby", "1_305");       // Old Toby: [shire] possession in the support area; its draw only on its own play
					put("stairs", "1_24");      // Stairs of Khazad-dum: [dwarven] condition in the support area, no possession
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CirithUngolPatrollerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Cirith Ungol Patroller
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: <b>Damage +1</b>.<br>While you can spot 3 possessions, this minion is <b>fierce</b>.
		*/

		var scn = GetScenario();

		var patroller = scn.GetFreepsCard("patroller");

		assertEquals("Cirith Ungol Patroller", patroller.getBlueprint().getTitle());
		assertNull(patroller.getBlueprint().getSubtitle());
		assertFalse(patroller.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, patroller.getBlueprint().getSide());
		assertEquals(Culture.SAURON, patroller.getBlueprint().getCulture());
		assertEquals(CardType.MINION, patroller.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, patroller.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(patroller, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(patroller, Keyword.DAMAGE));
		assertEquals(3, patroller.getBlueprint().getTwilightCost());
		assertEquals(8, patroller.getBlueprint().getStrength());
		assertEquals(2, patroller.getBlueprint().getVitality());
		assertEquals(6, patroller.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsFierceOnlyWhileThreePossessionsOfEitherSideCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var ring = scn.GetFreepsCard("ring");
		var toby = scn.GetFreepsCard("toby");
		var stairs = scn.GetFreepsCard("stairs");
		var patroller = scn.GetShadowCard("patroller");
		var guard = scn.GetShadowCard("guard");
		var chieftain = scn.GetShadowCard("chieftain");
		var banner = scn.GetShadowCard("banner");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, ring);
		scn.MoveCardsToSupportArea(toby, stairs);
		scn.MoveCardsToHand(armor);
		assertAttachedTo(ring, gimli);
		assertInZone(Zone.SUPPORT, toby);
		assertInZone(Zone.SUPPORT, stairs);
		assertInHand(armor);

		scn.StartGame();
		// Laid in the fellowship phase, so that a Free Peoples possession played there brings the count up (Q-0228).
		scn.MoveMinionsToTable(patroller, guard, chieftain);
		scn.AttachCardsTo(chieftain, banner);
		assertAttachedTo(banner, chieftain);

		// Two possessions, a Free Peoples one in a support area and a Shadow one borne by a minion; beside them an
		// artifact and a condition, neither a possession.
		assertFalse(scn.HasKeyword(patroller, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(guard, Keyword.FIERCE));

		// A third, borne by a companion, played by the Free Peoples player.
		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(armor);
		assertAttachedTo(armor, gimli);
		assertTrue(scn.HasKeyword(patroller, Keyword.FIERCE));
		// "This minion": not the other minion.
		assertFalse(scn.HasKeyword(guard, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(chieftain, Keyword.FIERCE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
