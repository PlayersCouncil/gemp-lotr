package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_051_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  Turn 1's skirmishes
	// are at The Troll Hoard 30_50 (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rise", "33_51");

					put("veteran", "32_43");    // Orkish Veteran: [sauron] Orc, strength 7, vitality 2; archer only at site 9, a Regroup ability not used
					put("sauron", "32_45");     // Sauron, The Necromancer: [sauron] Maia, strength 13; [gundabad] Orcs +1 and fierce; an Assignment ability not used
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, strength 6 (7 beside Sauron); an Assignment ability not used
					put("runner", "31_30");     // Goblin Runner: [moria] Orc, strength 5; its play trigger never fires (cheated in)
					put("footman", "30_42");    // Goblin Footman: [moria] Orc, strength 8; its play trigger never fires (cheated in)
					put("bats", "32_30");       // Dark Bats: [moria] Creature, no Orc, strength 4; a Shadow ability not used
					// Orkish Bow: [sauron] ranged weapon (possession) on a [sauron] Orc, cheated on; its play trigger never fires
					put("bow", "32_41");

					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion, strength 7; a Fellowship ability not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void RiseoftheNecromancerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Rise of the Necromancer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Spot 2 [sauron] minions to shuffle any number of Orcs from play into your draw deck. Make an Orc
		 * 	strength +3 for each Orc shuffled this way.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rise");

		assertEquals("Rise of the Necromancer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ShufflesTheChosenOrcsIntoTheDrawDeckToMakeAnOrcStrength3ForEach() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rise = scn.GetShadowCard("rise");
		var veteran = scn.GetShadowCard("veteran");
		var sauron = scn.GetShadowCard("sauron");
		var aggressor = scn.GetShadowCard("aggressor");
		var runner = scn.GetShadowCard("runner");
		var footman = scn.GetShadowCard("footman");
		var bats = scn.GetShadowCard("bats");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToHand(rise);

		scn.StartGame();
		assertInHand(rise);
		scn.MoveMinionsToTable(veteran, sauron, aggressor, runner, footman, bats);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, veteran);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);
		scn.SetTwilight(5);
		scn.FreepsPass();
		int deck = scn.GetShadowDeckCount();
		int shuffles = scn.GetShadowDeckShuffleCount();
		assertEquals(8, scn.GetStrength(footman));
		assertEquals(7, scn.GetStrength(veteran));

		// Spotted: the Veteran ([sauron] Orc) and Sauron ([sauron] Maia).
		assertTrue(scn.ShadowPlayAvailable(rise));
		scn.ShadowPlayCard(rise);
		// Orcs of three cultures, one skirmishing and three not; neither the Creature nor Sauron.
		assertTrue(scn.ShadowHasCardChoiceAvailable(veteran, aggressor, runner, footman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bats, sauron));
		scn.ShadowChooseCards(aggressor, runner);
		assertInZone(Zone.DECK, aggressor, runner);
		assertEquals(deck + 2, scn.GetShadowDeckCount());
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());

		// The Orc made stronger: the skirmishing Veteran or the Footman, who is in no skirmish; not the Creature.
		assertTrue(scn.ShadowHasCardChoiceAvailable(veteran, footman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bats, sauron));
		scn.ShadowChooseCard(footman);
		// 8 + 3 x 2
		assertEquals(14, scn.GetStrength(footman));
		assertEquals(7, scn.GetStrength(veteran));
		assertInDiscard(rise);
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void OneOrcShuffledMakesAnOrcOfALaterSkirmishStrength3UntilItsOwnSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rise = scn.GetShadowCard("rise");
		var veteran = scn.GetShadowCard("veteran");
		var sauron = scn.GetShadowCard("sauron");
		var aggressor = scn.GetShadowCard("aggressor");
		var runner = scn.GetShadowCard("runner");
		var balin = scn.GetFreepsCard("balin");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToHand(rise);

		scn.StartGame();
		assertInHand(rise);
		scn.MoveMinionsToTable(veteran, sauron, aggressor, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, veteran}, new PhysicalCardImpl[]{bilbo, runner});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);
		scn.SetTwilight(5);
		scn.FreepsPass();
		assertEquals(5, scn.GetStrength(runner));

		scn.ShadowPlayCard(rise);
		scn.ShadowChooseCards(aggressor);
		assertInZone(Zone.DECK, aggressor);
		scn.ShadowChooseCard(runner);
		// 5 + 3 x 1, in the Veteran's skirmish, before the Runner's own.
		assertEquals(8, scn.GetStrength(runner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The bonus lasts the skirmish it was played in: it is gone in the next.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(bilbo);
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ShufflingNoOrcStillShufflesTheDeckAndMakesNoOrcStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rise = scn.GetShadowCard("rise");
		var veteran = scn.GetShadowCard("veteran");
		var sauron = scn.GetShadowCard("sauron");
		var footman = scn.GetShadowCard("footman");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToHand(rise);

		scn.StartGame();
		assertInHand(rise);
		scn.MoveMinionsToTable(veteran, sauron, footman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, veteran);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);
		scn.SetTwilight(5);
		scn.FreepsPass();
		int deck = scn.GetShadowDeckCount();
		int shuffles = scn.GetShadowDeckShuffleCount();
		assertEquals(8, scn.GetStrength(footman));

		scn.ShadowPlayCard(rise);
		// "Any number" is 0 or more.
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();
		assertEquals(deck, scn.GetShadowDeckCount());
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		scn.ShadowChooseCard(footman);
		// 8 + 3 x 0
		assertEquals(8, scn.GetStrength(footman));
		assertInZone(Zone.SHADOW_CHARACTERS, veteran, footman);
		assertInDiscard(rise);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOneSauronMinionAndASauronPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rise = scn.GetShadowCard("rise");
		var veteran = scn.GetShadowCard("veteran");
		var aggressor = scn.GetShadowCard("aggressor");
		var bow = scn.GetShadowCard("bow");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToHand(rise);

		scn.StartGame();
		assertInHand(rise);
		scn.MoveMinionsToTable(veteran, aggressor);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, veteran);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);
		scn.SetTwilight(5);
		// A [sauron] card that is no minion, beside a minion of another culture.
		scn.AttachCardsTo(veteran, bow);
		assertAttachedTo(bow, veteran);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(rise));
		assertInHand(rise);
	}
}
