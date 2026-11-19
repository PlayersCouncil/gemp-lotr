package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_084_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bearer1", "11_84");
					put("bearer2", "11_84");    // not unique: "this minion" is read beside other copies
					put("bearer3", "11_84");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8, vitality 4; a maneuver ability used only on purpose
					put("guard", "1_7");        // Dwarf Guard: companion, strength 4, vitality 2, no text in play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HaradStandardbearerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Harad Standard-bearer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Skirmish:</b> Remove (3) to make this minion <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bearer1");

		assertEquals("Harad Standard-bearer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishRemovesThreeTwilightToMakeThisMinionAndNoOtherDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var bearer1 = scn.GetShadowCard("bearer1");
		var bearer2 = scn.GetShadowCard("bearer2");
		var bearer3 = scn.GetShadowCard("bearer3");

		scn.StartGame();
		scn.MoveMinionsToTable(bearer1, bearer2, bearer3);
		scn.SetTwilight(12);
		scn.SkipToAssignments();
		// Two Standard-bearers in one skirmish, the second put there by the Shadow player; the third is left out of the skirmishes.
		scn.FreepsAssignToMinions(aragorn, bearer1);
		scn.ShadowAssignToMinions(aragorn, bearer2);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetKeywordCount(bearer1, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(bearer2, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(bearer3, Keyword.DAMAGE));
		assertTrue(scn.ShadowActionAvailable(bearer1));
		scn.ShadowUseCardAction(bearer1);
		assertEquals(twilight - 3, scn.GetTwilight());
		assertEquals(1, scn.GetKeywordCount(bearer1, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(bearer2, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(bearer3, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// No limit is printed: a second use makes it damage +2.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(bearer1));
		scn.ShadowUseCardAction(bearer1);
		assertEquals(twilight - 6, scn.GetTwilight());
		assertEquals(2, scn.GetKeywordCount(bearer1, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The text names no skirmish: the Standard-bearer in none is offered it too, and gains the bonus alone.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(bearer3));
		scn.ShadowUseCardAction(bearer3);
		assertEquals(twilight - 9, scn.GetTwilight());
		assertEquals(1, scn.GetKeywordCount(bearer3, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(bearer1, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(bearer2, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWithTwoTwilightInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var bearer1 = scn.GetShadowCard("bearer1");

		scn.StartGame();
		scn.MoveMinionsToTable(bearer1);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, bearer1);
		scn.SetTwilight(2);
		assertEquals(2, scn.GetTwilight());
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(bearer1));
		assertEquals(0, scn.GetKeywordCount(bearer1, Keyword.DAMAGE));
	}

	@Test
	public void TheBonusLastsOneSkirmishPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, guard);

		var bearer1 = scn.GetShadowCard("bearer1");
		var bearer2 = scn.GetShadowCard("bearer2");

		scn.StartGame();
		scn.MoveMinionsToTable(bearer1, bearer2);
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, bearer1}, new PhysicalCardImpl[]{guard, bearer2});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetKeywordCount(bearer1, Keyword.DAMAGE));
		scn.ShadowUseCardAction(bearer1);
		assertEquals(1, scn.GetKeywordCount(bearer1, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.FreepsPass();
		scn.ShadowPass();

		// The Standard-bearer, 10, beats Aragorn, 8: two wounds with damage +1.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(guard);

		// A skirmish is a phase of its own: the bonus is gone.
		assertEquals(0, scn.GetKeywordCount(bearer1, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
