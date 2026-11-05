package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_052_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  The Shadow phase
	// and the skirmishes of turn 1 are at The Troll Hoard 30_50 (site 2), whose only text is a start-of-turn offer;
	// a minion of site 5 roams there.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lob", "31_52");

					put("runner", "31_30");     // Goblin Runner: [moria] Orc; a "when you play" offer, never reached (cheated in)
					put("veteran", "32_43");    // Orkish Veteran: [sauron] Orc; site 9's archers and a Regroup ability, neither reached
					put("bats", "32_30");       // Dark Bats: [moria] Creature, no Orc; a Shadow ability not used
					put("wicked", "31_55");     // Wicked Spider: [spider] Spider, strength 7, no damage bonus; a Skirmish ability not used
					put("shelob", "8_26");      // Shelob, Last Child of Ungoliant: a [gollum] Spider; damage +1 only in a fierce skirmish
					put("nest", "31_54");       // Spider Nest: a [spider] condition, no Spider; an Assignment ability not used
					put("tomnoddy", "31_53");   // Old Tomnoddy: [spider] Spider, cost 4: a minion in hand beside this one

					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion, strength 7, vitality 3; a Fellowship ability not used
					put("nori", "30_15");       // Nori, Of the Blue Mountains: [dwarven] companion, strength 7, vitality 3; a Maneuver ability not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void LazyLobStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Lazy Lob
		 * Unique: True
		 * Side: Shadow
		 * Culture: Spider
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Spider
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Fierce</b>.
		 * The twilight cost of this minion is -1 for each Orc you spot.
		 * <b>Skirmish:</b> Discard an Orc to make a Spider <b>damage +1</b>.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lob");

		assertEquals("Lazy Lob", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SPIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.SPIDER, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CostsOneLessForEachOrcSpottedAndNothingLessForOtherMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lob = scn.GetShadowCard("lob");
		var tomnoddy = scn.GetShadowCard("tomnoddy");
		var runner = scn.GetShadowCard("runner");
		var veteran = scn.GetShadowCard("veteran");
		var bats = scn.GetShadowCard("bats");
		var wicked = scn.GetShadowCard("wicked");
		scn.MoveCardsToHand(lob, tomnoddy);

		scn.StartGame();
		assertInHand(lob, tomnoddy);

		// A Creature and a Spider in play, no Orc: the printed 4.
		scn.MoveMinionsToTable(bats, wicked);
		assertInZone(Zone.SHADOW_CHARACTERS, bats, wicked);
		assertEquals(4, scn.GetTwilightCost(lob));
		assertEquals(4, scn.GetTwilightCost(tomnoddy));

		// A [moria] Orc: 4 - 1.  The other minion in hand is not lowered.
		scn.MoveMinionsToTable(runner);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(3, scn.GetTwilightCost(lob));
		assertEquals(4, scn.GetTwilightCost(tomnoddy));

		// And a [sauron] Orc: 4 - 2.
		scn.MoveMinionsToTable(veteran);
		assertInZone(Zone.SHADOW_CHARACTERS, veteran);
		assertEquals(2, scn.GetTwilightCost(lob));
		assertEquals(4, scn.GetTwilightCost(tomnoddy));

		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertEquals(10, scn.GetTwilight());
		scn.ShadowPlayCard(lob);

		// 10 - (4 - 2 for the two Orcs) - 2 for roaming at site 2.
		assertInZone(Zone.SHADOW_CHARACTERS, lob);
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsAnOrcToMakeASpiderDamagePlusOneForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lob = scn.GetShadowCard("lob");
		var wicked = scn.GetShadowCard("wicked");
		var shelob = scn.GetShadowCard("shelob");
		var runner = scn.GetShadowCard("runner");
		var veteran = scn.GetShadowCard("veteran");
		var bats = scn.GetShadowCard("bats");
		var nest = scn.GetShadowCard("nest");
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		scn.MoveCompanionsToTable(balin, nori);
		scn.MoveCardsToSupportArea(nest);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, nest);
		scn.MoveMinionsToTable(lob, wicked, shelob, runner, veteran, bats);

		// Nori takes Wicked Spider and Balin takes Lazy Lob; the rest stay out of the skirmishes.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{nori, wicked}, new PhysicalCardImpl[]{balin, lob});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(nori);
		assertEquals(0, scn.GetKeywordCount(lob, Keyword.DAMAGE));

		// In Wicked Spider's skirmish, Lazy Lob waiting for his own.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(lob));
		scn.ShadowUseCardAction(lob);

		// The cost: Orcs of two cultures, not the Creature or a Spider.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, veteran));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bats, wicked, shelob, lob));
		scn.ShadowChooseCard(runner);
		assertInDiscard(runner);

		// The Spider: one in this skirmish, one waiting for his, a [gollum] one in no skirmish; not the Orc, the
		// Creature or the [spider] condition.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(lob, wicked, shelob));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(veteran, bats, nest));
		scn.ShadowChooseCard(lob);
		assertEquals(1, scn.GetKeywordCount(lob, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(wicked, Keyword.DAMAGE));

		// Used again on him, damage +1 already, it adds: the Veteran, the only Orc left, is discarded unasked.
		scn.FreepsPass();
		scn.ShadowUseCardAction(lob);
		assertInDiscard(veteran);
		scn.ShadowChooseCard(lob);
		assertEquals(2, scn.GetKeywordCount(lob, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(wicked, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(shelob, Keyword.DAMAGE));

		// 7 against 7: Nori loses.  In the next skirmish, a skirmish phase of its own, Lazy Lob has no damage bonus.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(nori));
		scn.FreepsResolveSkirmish(balin);
		assertEquals(0, scn.GetKeywordCount(lob, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithNoOrcToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lob = scn.GetShadowCard("lob");
		var wicked = scn.GetShadowCard("wicked");
		var bats = scn.GetShadowCard("bats");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(lob, wicked, bats);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, lob);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);
		scn.FreepsPass();

		// A Creature and another Spider in play, and no Orc.
		assertInZone(Zone.SHADOW_CHARACTERS, wicked, bats);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(lob));
	}
}
