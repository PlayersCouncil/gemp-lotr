package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_068_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("city", "13_68");

					// Aragorn, Ranger of the North: [gondor] Man companion; his maneuver ability is never used
					put("aragorn", "1_89");
					// Gondorian Sword: [gondor] possession (hand weapon); a skirmish ability never used
					put("gsword", "7_98");
					// Heavy Chain: [rohan] possession (armor) on a Man; an overwhelm rule only
					put("chain", "4_278");
					// Ring of Barahir: a [gondor] ARTIFACT on Aragorn; an optional trigger at the start of the turn
					put("barahir", "6_55");
					// I Will Go: a [gondor] CONDITION on a [gondor] Man; defender +1 with initiative
					put("willgo", "7_105");
					// Pippin, Trollslayer: a [gondor] Hobbit companion; his skirmish ability is never used
					put("pippin", "103_31");
					// Hobbit Sword: [shire] possession, "Bearer must be a Hobbit", no other text
					put("hsword1", "1_299");
					put("hsword2", "1_299");
					put("sam", "1_311");        // Sam, Son of Hamfast: a [shire] Hobbit companion
					// Gondorian Merchant: the [gondor] Man ALLY; a maneuver ability that needs initiative
					put("merchant", "7_97");
					// Athelas: [gondor] possession on a [gondor] Man; a fellowship ability never used
					put("athelas", "1_94");

					// A Path Appointed and Support of the City: [gondor] support-area conditions that get [gondor]
					// tokens by their own abilities, which are never used here
					put("path", "8_42");
					put("support", "7_123");
					// Ithilien Trap: a [gondor] support-area condition with no token; its trigger wants a skirmish won
					put("trap", "4_126");
					// Wish For Our Kinfolk: a [dwarven] support-area condition that gets [dwarven] tokens
					put("wish", "8_8");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GuardedCityStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Guarded City
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Spot a [gondor] companion to reinforce a [gondor] token for each possession borne by that companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("city");

		assertEquals("Guarded City", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ReinforcesAGondorTokenForEachPossessionTheSpottedGondorCompanionBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var city = scn.GetFreepsCard("city");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gsword = scn.GetFreepsCard("gsword");
		var chain = scn.GetFreepsCard("chain");
		var barahir = scn.GetFreepsCard("barahir");
		var willgo = scn.GetFreepsCard("willgo");
		var pippin = scn.GetFreepsCard("pippin");
		var hsword1 = scn.GetFreepsCard("hsword1");
		var hsword2 = scn.GetFreepsCard("hsword2");
		var sam = scn.GetFreepsCard("sam");
		var merchant = scn.GetFreepsCard("merchant");
		var athelas = scn.GetFreepsCard("athelas");
		var path = scn.GetFreepsCard("path");
		var support = scn.GetFreepsCard("support");
		var trap = scn.GetFreepsCard("trap");
		var wish = scn.GetFreepsCard("wish");
		scn.MoveCompanionsToTable(aragorn, pippin, sam);
		scn.MoveAlliesToTable(merchant);
		// Aragorn: two possessions of two cultures, an artifact and a condition.
		scn.AttachCardsTo(aragorn, gsword, chain, willgo);
		scn.AttachCardsTo(pippin, hsword1);
		scn.AttachCardsTo(sam, hsword2);
		scn.AttachCardsTo(merchant, athelas);
		scn.MoveCardsToSupportArea(path, support, trap, wish);
		scn.MoveCardsToHand(city);

		scn.StartGame();
		// The Ring's start-of-turn trigger is behind us.
		scn.AttachCardsTo(aragorn, barahir);
		scn.AddTokensToCard(path, 1);
		scn.AddTokensToCard(support, 1);
		scn.AddTokensToCard(wish, 1);
		scn.FreepsRefreshDecision();
		assertAttachedTo(gsword, aragorn);
		assertAttachedTo(chain, aragorn);
		assertAttachedTo(willgo, aragorn);
		assertAttachedTo(barahir, aragorn);
		assertAttachedTo(hsword1, pippin);
		assertAttachedTo(hsword2, sam);
		assertAttachedTo(athelas, merchant);
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertEquals(1, scn.GetCultureTokensOn(support));
		assertEquals(0, scn.GetCultureTokensOn(trap));
		assertEquals(1, scn.GetCultureTokensOn(wish));
		assertTrue(scn.FreepsPlayAvailable(city));

		scn.FreepsPlayCard(city);
		// [gondor] companions of two races; not a [shire] companion or the [gondor] ally, each bearing a possession.
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, merchant));
		scn.FreepsChooseCard(aragorn);

		// The first of two: cards with a [gondor] token; not the [gondor] card with none, or the [dwarven] token.
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(path, support));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(trap, wish));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(path);
		assertEquals(2, scn.GetCultureTokensOn(path));
		assertEquals(1, scn.GetCultureTokensOn(support));

		// The second, and no third or fourth: the artifact and the condition he bears are no possessions.
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(support);
		assertEquals(2, scn.GetCultureTokensOn(path));
		assertEquals(2, scn.GetCultureTokensOn(support));
		assertEquals(0, scn.GetCultureTokensOn(trap));
		assertEquals(1, scn.GetCultureTokensOn(wish));
		assertInDiscard(city);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithPippinSpottedOneTokenIsReinforcedForHisOnePossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var city = scn.GetFreepsCard("city");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gsword = scn.GetFreepsCard("gsword");
		var chain = scn.GetFreepsCard("chain");
		var pippin = scn.GetFreepsCard("pippin");
		var hsword1 = scn.GetFreepsCard("hsword1");
		var path = scn.GetFreepsCard("path");
		var support = scn.GetFreepsCard("support");
		scn.MoveCompanionsToTable(aragorn, pippin);
		scn.AttachCardsTo(aragorn, gsword, chain);
		scn.AttachCardsTo(pippin, hsword1);
		scn.MoveCardsToSupportArea(path, support);
		scn.MoveCardsToHand(city);

		scn.StartGame();
		scn.AddTokensToCard(path, 1);
		scn.AddTokensToCard(support, 1);
		scn.FreepsRefreshDecision();
		assertAttachedTo(gsword, aragorn);
		assertAttachedTo(chain, aragorn);
		assertAttachedTo(hsword1, pippin);
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertEquals(1, scn.GetCultureTokensOn(support));

		scn.FreepsPlayCard(city);
		scn.FreepsChooseCard(pippin);
		// One possession on Pippin, and Aragorn's two do not count for him.
		assertTrue(scn.FreepsHasCardChoiceAvailable(path, support));
		scn.FreepsChooseCard(support);
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertEquals(2, scn.GetCultureTokensOn(support));
		assertInDiscard(city);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoGondorTokenInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var city = scn.GetFreepsCard("city");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gsword = scn.GetFreepsCard("gsword");
		var chain = scn.GetFreepsCard("chain");
		var trap = scn.GetFreepsCard("trap");
		var wish = scn.GetFreepsCard("wish");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, gsword, chain);
		scn.MoveCardsToSupportArea(trap, wish);
		scn.MoveCardsToHand(city);

		scn.StartGame();
		scn.AddTokensToCard(wish, 1);
		scn.FreepsRefreshDecision();
		assertAttachedTo(gsword, aragorn);
		assertAttachedTo(chain, aragorn);
		assertEquals(0, scn.GetCultureTokensOn(trap));
		assertEquals(1, scn.GetCultureTokensOn(wish));
		assertTrue(scn.FreepsPlayAvailable(city));

		// An event is played whatever its effect can reach: Aragorn, the one [gondor] companion, is spotted
		// unasked, and there is no [gondor] token to reinforce.
		scn.FreepsPlayCard(city);
		assertInDiscard(city);
		assertEquals(0, scn.GetCultureTokensOn(trap));
		assertEquals(1, scn.GetCultureTokensOn(wish));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutAGondorCompanionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var city = scn.GetFreepsCard("city");
		var sam = scn.GetFreepsCard("sam");
		var hsword2 = scn.GetFreepsCard("hsword2");
		var merchant = scn.GetFreepsCard("merchant");
		var athelas = scn.GetFreepsCard("athelas");
		var path = scn.GetFreepsCard("path");
		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(sam, hsword2);
		scn.AttachCardsTo(merchant, athelas);
		scn.MoveCardsToSupportArea(path);
		scn.MoveCardsToHand(city);

		scn.StartGame();
		scn.AddTokensToCard(path, 1);
		scn.FreepsRefreshDecision();
		assertAttachedTo(hsword2, sam);
		assertAttachedTo(athelas, merchant);
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertInZone(Zone.HAND, city);

		// A [shire] companion and the [gondor] ally, each bearing a possession: no [gondor] companion.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(city));
	}
}
