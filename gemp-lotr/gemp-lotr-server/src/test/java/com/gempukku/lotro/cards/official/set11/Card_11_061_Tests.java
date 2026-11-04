package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_061_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("houses", "11_61");

					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion; Knight is its whole text
					// Gondorian Merchant: the [gondor] ally, a Man of vitality 1; a maneuver ability used only on purpose
					put("merchant", "7_97");
					// Narsil, Blade of the Faithful: a [gondor] artifact a [gondor] Man bears, vitality +1, so that the
					// Merchant can carry a wound
					put("narsil", "9_34");
					// Pippin, Trollslayer: a [gondor] Hobbit companion (to play, spot a [gondor] companion: cheated in)
					put("pippin", "103_31");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion; Valiant is its whole text
					// A Path Appointed: a [gondor] support-area condition that carries [gondor] tokens of its own
					put("path", "8_42");

					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HousesofHealingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Houses of Healing
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: When you play this condition, spot a [gondor] Man to add 3 [gondor] tokens here.
		 * 	<b>Fellowship:</b> Add (2) and either discard this condition or remove a [gondor] token from here to heal
		 * 	a [gondor] Man.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("houses");

		assertEquals("Houses of Healing", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedWithAGondorManCompanionItTakesThreeTokensAndAGondorConditionPlayedAfterAddsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var houses = scn.GetFreepsCard("houses");
		var path = scn.GetFreepsCard("path");
		scn.MoveCompanionsToTable("knight");
		scn.MoveCardsToHand(houses, path);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(houses));
		scn.FreepsPlayCard(houses);
		assertInZone(Zone.SUPPORT, houses);
		assertEquals(3, scn.GetCultureTokensOn(houses));

		// Another [gondor] condition played while it is in play sets nothing off here.
		scn.FreepsPlayCard(path);
		assertInZone(Zone.SUPPORT, path);
		assertEquals(0, scn.GetCultureTokensOn(path));
		assertEquals(3, scn.GetCultureTokensOn(houses));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedWithTheGondorAllyAsTheOnlyGondorManItTakesThreeTokens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var houses = scn.GetFreepsCard("houses");
		scn.MoveAlliesToTable("merchant");
		scn.MoveCardsToHand(houses);

		scn.StartGame();

		scn.FreepsPlayCard(houses);
		assertInZone(Zone.SUPPORT, houses);
		assertEquals(3, scn.GetCultureTokensOn(houses));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedWithAGondorHobbitAndARohanManButNoGondorManItTakesNoToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var houses = scn.GetFreepsCard("houses");
		scn.MoveCompanionsToTable("pippin", "guard");
		scn.MoveCardsToHand(houses);

		scn.StartGame();

		// Nothing in the text is needed to play it: it is played, and nothing is added.
		assertTrue(scn.FreepsPlayAvailable(houses));
		scn.FreepsPlayCard(houses);
		assertInZone(Zone.SUPPORT, houses);
		assertEquals(0, scn.GetCultureTokensOn(houses));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionAddsTwoAndRemovesATokenFromHereToHealAGondorMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var houses = scn.GetFreepsCard("houses");
		var path = scn.GetFreepsCard("path");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(knight, pippin, guard);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToSupportArea(houses, path);

		scn.StartGame();
		scn.AddTokensToCard(houses, 3);
		scn.AddTokensToCard(path, 1);
		scn.AddWoundsToChar(knight, 2);
		scn.AddWoundsToChar(merchant, 1);
		scn.AddWoundsToChar(pippin, 1);
		scn.AddWoundsToChar(guard, 1);

		assertEquals(0, scn.GetTwilight());
		assertEquals(3, scn.GetCultureTokensOn(houses));
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(guard));

		assertTrue(scn.FreepsActionAvailable(houses));
		scn.FreepsUseCardAction(houses);

		// With a token here, either may be paid.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Discard this condition"));
		assertTrue(scn.FreepsChoiceAvailable("token here"));
		scn.FreepsChooseOption("token here");
		assertEquals(2, scn.GetTwilight());
		assertEquals(2, scn.GetCultureTokensOn(houses));
		assertInZone(Zone.SUPPORT, houses);
		// The other [gondor] card's token is not "here".
		assertEquals(1, scn.GetCultureTokensOn(path));

		// A [gondor] Man, companion or ally; not a [gondor] Hobbit, nor a Man of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, guard));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(knight);

		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionDiscardsThisConditionWithItsTokensToHealTheGondorAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var houses = scn.GetFreepsCard("houses");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		scn.MoveCompanionsToTable(knight);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToSupportArea(houses);

		scn.StartGame();
		scn.AddTokensToCard(houses, 2);
		scn.AddWoundsToChar(knight, 1);
		scn.AddWoundsToChar(merchant, 1);

		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetCultureTokensOn(houses));
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetWoundsOn(merchant));

		scn.FreepsUseCardAction(houses);
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("Discard this condition");
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.DISCARD, houses);

		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, merchant));
		scn.FreepsChooseCard(merchant);

		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(1, scn.GetWoundsOn(knight));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoTokenHereTheFellowshipActionDiscardsThisConditionAndNothingIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var houses = scn.GetFreepsCard("houses");
		var path = scn.GetFreepsCard("path");
		var knight = scn.GetFreepsCard("knight");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(knight, guard);
		scn.MoveCardsToSupportArea(houses, path);

		scn.StartGame();
		// A token on another [gondor] card, none here.
		scn.AddTokensToCard(path, 1);
		scn.AddWoundsToChar(knight, 2);

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetCultureTokensOn(houses));
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertEquals(2, scn.GetWoundsOn(knight));

		assertTrue(scn.FreepsActionAvailable(houses));
		scn.FreepsUseCardAction(houses);

		// Only the discard can be paid: no choice of costs, and the one [gondor] Man is healed unasked.
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.DISCARD, houses);
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertEquals(1, scn.GetWoundsOn(knight));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var houses = scn.GetFreepsCard("houses");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(houses);

		scn.StartGame();
		scn.AddTokensToCard(houses, 3);
		scn.AddWoundsToChar(knight, 1);
		assertTrue(scn.FreepsActionAvailable(houses));

		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(houses));
		assertEquals(3, scn.GetCultureTokensOn(houses));
		assertEquals(1, scn.GetWoundsOn(knight));
	}
}
