package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_123_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("support1", "7_123");
					put("support2", "7_123");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, twilight 4; a maneuver ability only
					put("aragorn2", "1_89");    // a second copy, held in hand while the first is in play: it cannot be played
					put("ingold", "7_106");     // Ingold: [gondor] Man companion, twilight 2; a fellowship ability only
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] Man ally, twilight 1; a maneuver ability only
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion; to play, spot a [gondor] companion
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion; valiant only

					put("savage", "1_151");     // Uruk Savage: a minion, for a maneuver phase; Damage +1 only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SupportoftheCityStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Support of the City
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Fellowship:</b> Play a [gondor] Man to place a [gondor] token here.
		 * 	<b>Fellowship:</b> Play a [gondor] companion. That companion's twilight cost is -1 for each token here. Discard this condition.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("support1");

		assertEquals("Support of the City", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayingAGondorManFromHandPlacesATokenOnTheCopyUsed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var support1 = scn.GetFreepsCard("support1");
		var support2 = scn.GetFreepsCard("support2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ingold = scn.GetFreepsCard("ingold");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToSupportArea(support1, support2);
		// Aragorn in play lets Pippin be played: only the wording keeps him out.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(ingold, merchant, pippin, guard);
		assertInHand(ingold, merchant, pippin, guard);

		scn.StartGame();

		assertEquals(0, scn.GetCultureTokensOn(support1));
		assertEquals(0, scn.GetCultureTokensOn(support2));
		assertTrue(scn.FreepsPlayAvailable(pippin));
		assertTrue(scn.FreepsPlayAvailable(guard));
		assertTrue(scn.FreepsActionAvailable(support1, "token"));

		scn.FreepsUseCardAction(support1, "token");
		// A [gondor] Man, companion or ally; not a [gondor] Hobbit or a [rohan] Man.
		assertTrue(scn.FreepsHasCardChoiceAvailable(ingold, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, guard, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(ingold);

		assertInZone(Zone.FREE_CHARACTERS, ingold);
		assertEquals(1, scn.GetCultureTokensOn(support1));
		assertEquals(0, scn.GetCultureTokensOn(support2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Again: the ally, the one [gondor] Man left in hand, is played unasked, and a second token goes on the same copy.
		scn.FreepsUseCardAction(support1, "token");
		assertInZone(Zone.SUPPORT, merchant);
		assertEquals(2, scn.GetCultureTokensOn(support1));
		assertEquals(0, scn.GetCultureTokensOn(support2));
		assertInHand(pippin, guard);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TokenAbilityIsNotOfferedWithNoGondorManInHandThatCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var support1 = scn.GetFreepsCard("support1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var aragorn2 = scn.GetFreepsCard("aragorn2");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToSupportArea(support1);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(aragorn2, pippin, guard);
		assertInHand(aragorn2, pippin, guard);

		scn.StartGame();

		// The one [gondor] Man in hand is a copy of Aragorn, who is in play already.
		assertFalse(scn.FreepsPlayAvailable(aragorn2));
		assertTrue(scn.FreepsPlayAvailable(pippin));
		assertTrue(scn.FreepsPlayAvailable(guard));
		assertEquals(0, scn.GetCultureTokensOn(support1));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(support1, "token"));
		// Pippin is a [gondor] companion who can be played: the other ability is offered.
		assertTrue(scn.FreepsActionAvailable(support1, "discount"));
	}

	@Test
	public void PlaysAGondorCompanionOneCheaperForTheOneTokenOnTheCopyUsedAndDiscardsThatCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var support1 = scn.GetFreepsCard("support1");
		var support2 = scn.GetFreepsCard("support2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ingold = scn.GetFreepsCard("ingold");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToSupportArea(support1, support2);
		// Ingold in play lets Pippin be played.
		scn.MoveCompanionsToTable(ingold);
		scn.MoveCardsToHand(aragorn, merchant, pippin, guard);
		assertInHand(aragorn, merchant, pippin, guard);
		assertInZone(Zone.SUPPORT, support1, support2);

		scn.StartGame();
		scn.AddTokensToCard(support1, 1);
		scn.AddTokensToCard(support2, 2);
		assertEquals(1, scn.GetCultureTokensOn(support1));
		assertEquals(2, scn.GetCultureTokensOn(support2));
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetTwilight());
		// No standing discount: Aragorn costs 4 in hand.
		assertEquals(4, scn.GetTwilightCost(aragorn));
		assertTrue(scn.FreepsPlayAvailable(merchant));
		assertTrue(scn.FreepsPlayAvailable(guard));
		assertTrue(scn.FreepsActionAvailable(support1, "discount"));

		scn.FreepsUseCardAction(support1, "discount");
		// A [gondor] companion, Man or Hobbit; not the [gondor] ally or the [rohan] companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merchant, guard, ingold));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		// 4 for Aragorn - 1 for the one token on the copy used (not the 2 on the other copy)
		assertEquals(3, scn.GetTwilight());
		assertInDiscard(support1);
		assertInZone(Zone.SUPPORT, support2);
		assertEquals(2, scn.GetCultureTokensOn(support2));
		assertInHand(merchant, pippin, guard);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithTwoTokensTheCompanionIsTwoCheaper() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var support1 = scn.GetFreepsCard("support1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(support1);
		scn.MoveCardsToHand(aragorn);
		assertInHand(aragorn);
		assertInZone(Zone.SUPPORT, support1);

		scn.StartGame();
		scn.AddTokensToCard(support1, 2);
		assertEquals(2, scn.GetCultureTokensOn(support1));
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetTwilight());
		scn.FreepsUseCardAction(support1, "discount");
		// The one [gondor] companion in hand is played unasked.
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		// 4 for Aragorn - 2 for the two tokens here
		assertEquals(2, scn.GetTwilight());
		assertInDiscard(support1);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscountAbilityIsNotOfferedWithNoGondorCompanionInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var support1 = scn.GetFreepsCard("support1");
		var ingold = scn.GetFreepsCard("ingold");
		var merchant = scn.GetFreepsCard("merchant");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToSupportArea(support1);
		scn.MoveCompanionsToTable(ingold);
		scn.MoveCardsToHand(merchant, guard);
		assertInHand(merchant, guard);

		scn.StartGame();
		scn.AddTokensToCard(support1, 1);
		assertEquals(1, scn.GetCultureTokensOn(support1));
		scn.FreepsRefreshDecision();

		// The [gondor] ally and the [rohan] companion can be played, but neither is a [gondor] companion.
		assertTrue(scn.FreepsPlayAvailable(merchant));
		assertTrue(scn.FreepsPlayAvailable(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(support1, "discount"));
		// The Merchant is a [gondor] Man: the other ability is offered.
		assertTrue(scn.FreepsActionAvailable(support1, "token"));
		assertInZone(Zone.SUPPORT, support1);
	}

	@Test
	public void NeitherAbilityIsOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var support1 = scn.GetFreepsCard("support1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ingold = scn.GetFreepsCard("ingold");
		scn.MoveCardsToSupportArea(support1);
		scn.MoveCardsToHand(aragorn, ingold);
		assertInHand(aragorn, ingold);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.AddTokensToCard(support1, 1);
		assertEquals(1, scn.GetCultureTokensOn(support1));
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(support1, "token"));
		assertFalse(scn.FreepsActionAvailable(support1, "discount"));
		assertInHand(aragorn, ingold);
	}
}
