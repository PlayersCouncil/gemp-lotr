package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_056_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("castamir", "12_56");

					// Axeman of Harad: a [men] Man minion, strength 11, vitality 3.  Its text only lowers its own cost
					// in hand.
					put("axeman", "11_69");
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit, strength 3, vitality 4.  His
					// maneuver and regroup abilities are never used.
					put("ted", "18_75");
					// Easterling Axeman: a [raider] Man minion, strength 6, vitality 2; strength +3 and fierce only
					// at 2 burdens, and no burden is laid.
					put("raider", "4_224");
					// Goblin Patrol Troop: a [moria] minion with no text, vitality 3: one who could exert, of another
					// culture.
					put("troop", "1_177");

					// Aragorn, Ranger of the North: a [gondor] Man companion, strength 8, vitality 4.  His maneuver
					// ability is never used.
					put("aragorn", "1_89");
					// Defend It and Hope: [gondor] Maneuver event, exert a [gondor] Man to wound a minion once.
					put("defend", "4_115");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CastamirofUmbarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Castamir of Umbar, Corsair Vandal
		 * Unique: True
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 7
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 14
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: <b>Fierce</b>. <b>Toil 1</b>. <helper>(For each [men] character you exert when playing this, its twilight cost is -1.)</helper><br>Each wounded [men] minion is strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("castamir");

		assertEquals("Castamir of Umbar", card.getBlueprint().getTitle());
		assertEquals("Corsair Vandal", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(card, Keyword.TOIL));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.TOIL));
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(14, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ToilTakesOneOffItsCostForEachMenCharacterExertedAsItIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var castamir = scn.GetShadowCard("castamir");
		var axeman = scn.GetShadowCard("axeman");
		var ted = scn.GetShadowCard("ted");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetShadowHand(castamir);
		scn.MoveMinionsToTable(axeman, ted, troop);
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.FreepsPass();
		// Site 4 is its site number: it is not roaming.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// 7 - 1 - 1 is the least it can cost, with both [men] minions exerted; the [moria] Troop does not count.
		// Toil 2 would let 3 do, and no toil (or the Troop counted) would want 7 (or 4).
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(castamir));
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(castamir));

		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(castamir);
		// The pool is 1 short of its 7: one exertion is owed, and the second is the Shadow player's to make or not.
		// Toil 2 would owe none.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(axeman, ted));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));

		scn.ShadowChooseCard(ted);
		assertInZone(Zone.SHADOW_CHARACTERS, castamir);
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertEquals(1, scn.GetWoundsOn(ted));
		assertEquals(0, scn.GetWoundsOn(troop));
		// 6 - (7 - 1)
		assertEquals(0, scn.GetTwilight());
		// Ted, wounded by the exertion, is now a wounded [men] minion: 3 + 2.
		assertEquals(5, scn.GetStrength(ted));
		assertEquals(11, scn.GetStrength(axeman));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachWoundedMenMinionIsStrengthPlus2AndNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var castamir = scn.GetShadowCard("castamir");
		var axeman = scn.GetShadowCard("axeman");
		var ted = scn.GetShadowCard("ted");
		var raider = scn.GetShadowCard("raider");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(castamir, axeman, ted, raider);
		scn.AddWoundsToChar(castamir, 1);
		scn.AddWoundsToChar(ted, 2);
		scn.AddWoundsToChar(raider, 1);
		scn.AddWoundsToChar(aragorn, 1);

		assertEquals(1, scn.GetWoundsOn(castamir));
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertEquals(2, scn.GetWoundsOn(ted));
		assertEquals(1, scn.GetWoundsOn(raider));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetBurdens());

		// Castamir himself, wounded: 14 + 2.
		assertEquals(16, scn.GetStrength(castamir));
		// A [men] Hobbit with two wounds: 3 + 2, once for being wounded and not once for each wound.
		assertEquals(5, scn.GetStrength(ted));
		// An unwounded [men] minion is as printed.
		assertEquals(11, scn.GetStrength(axeman));
		// A wounded [raider] Man and a wounded companion are as printed.
		assertEquals(6, scn.GetStrength(raider));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AMenMinionWoundedInPlayBecomesStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var castamir = scn.GetShadowCard("castamir");
		var axeman = scn.GetShadowCard("axeman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		scn.StartGame();
		scn.MoveMinionsToTable(castamir, axeman);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(axeman));
		assertEquals(11, scn.GetStrength(axeman));
		assertEquals(14, scn.GetStrength(castamir));

		scn.FreepsPlayCard(defend);
		// Aragorn is the one [gondor] Man to exert; the minion to wound is chosen.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(axeman, castamir));
		scn.FreepsChooseCard(axeman);

		assertEquals(1, scn.GetWoundsOn(axeman));
		assertEquals(13, scn.GetStrength(axeman));
		assertEquals(0, scn.GetWoundsOn(castamir));
		assertEquals(14, scn.GetStrength(castamir));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
