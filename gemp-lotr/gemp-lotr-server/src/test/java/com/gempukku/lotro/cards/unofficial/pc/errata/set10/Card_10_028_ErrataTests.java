package com.gempukku.lotro.cards.unofficial.pc.errata.set10;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_10_028_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("denethor", "60_28");

					put("aragorn", "1_89");         // Aragorn, Ranger of the North: a [gondor] Man companion
					put("merchant", "7_97");        // Gondorian Merchant: the one [gondor] ally, a Man; cost 1; a maneuver ability used only on purpose
					put("pippin", "103_31");        // Pippin, Trollslayer: a [gondor] companion who is a Hobbit
					put("guard", "5_83");           // Household Guard: a Man who is [rohan]; valiant and nothing else

					put("armor", "1_92");           // Armor: [gondor] possession
					put("armor2", "1_92");
					put("shards", "3_44");          // The Shards of Narsil: [gondor] artifact
					put("bowmen", "3_41");          // Gondor Bowmen: [gondor] condition
					put("valor", "1_103");          // Elendil's Valor: [gondor] event
					put("knight", "5_35");          // Gondorian Knight: [gondor] companion; knight and nothing else
					put("dwarvenarmor", "1_8");     // Dwarven Armor: a possession that is [dwarven]
					put("fear", "1_278");           // Strength Born of Fear: a condition that is [sauron]
				}},
				// Sites 3 and 6, Rivendell Valley and Galadriel's Glade, are the sanctuaries.
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DenethorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Denethor, Lord of Minas Tirith
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: To play, spot 2 [gondor] Men.
		 * <b>Fellowship:</b> If at a sanctuary, exert Denethor to take a [gondor] item or condition into hand from
		 * your draw deck. Choose an opponent who may draw a card.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("denethor");

		assertEquals("Denethor", card.getBlueprint().getTitle());
		assertEquals("Lord of Minas Tirith", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void CanBePlayedForTwoOnceASecondGondorManIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var denethor = scn.GetFreepsCard("denethor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, pippin, guard);
		scn.MoveCardsToHand(denethor, merchant);

		scn.StartGame();

		// One [gondor] Man, a [gondor] Hobbit and a [rohan] Man.
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.FreepsPlayAvailable(denethor));

		// The second [gondor] Man is an ally.
		scn.FreepsPlayCard(merchant);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(denethor));

		scn.FreepsPlayCard(denethor);
		assertInZone(Zone.FREE_CHARACTERS, denethor);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtASanctuaryExertsDenethorToTakeAGondorItemOrConditionIntoHandAndTheOpponentMayDraw() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var denethor = scn.GetFreepsCard("denethor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var armor2 = scn.GetFreepsCard("armor2");
		var shards = scn.GetFreepsCard("shards");
		var bowmen = scn.GetFreepsCard("bowmen");
		var valor = scn.GetFreepsCard("valor");
		var knight = scn.GetFreepsCard("knight");
		var dwarvenArmor = scn.GetFreepsCard("dwarvenarmor");
		var fear = scn.GetFreepsCard("fear");
		// Aragorn is another [gondor] companion who could be made to exert in Denethor's place: he is read as untouched.
		scn.MoveCompanionsToTable(denethor, aragorn);
		scn.MoveCardsToFreepsDiscard("merchant", "pippin", "guard", "armor2");

		scn.StartGame();
		scn.SkipToSite(3);
		// The turns on the way drew the small deck into the hands: these go back.
		scn.MoveCardsToTopOfDeck(armor, shards, bowmen, valor, knight, dwarvenArmor, fear);
		scn.MoveCardsToTopOfDeck(scn.GetShadowCard("knight"));

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(7, scn.GetFreepsDeckCount());
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int shadowHand = scn.GetShadowHandCount();
		int shadowDeck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetWoundsOn(denethor));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsActionAvailable(denethor));

		scn.MarkGameLog();
		scn.FreepsUseCardAction(denethor);
		assertEquals(1, scn.GetWoundsOn(denethor));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsDismissRevealedCards();

		// A [gondor] possession, artifact and condition; not a [gondor] event or companion, nor a possession or a
		// condition of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(armor, shards, bowmen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(valor, knight, dwarvenArmor, fear));
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(shards);

		assertInZone(Zone.HAND, shards);
		assertInZone(Zone.DECK, armor, bowmen, valor, knight, dwarvenArmor, fear);
		// The Armor in the discard pile was never to be had.
		assertInZone(Zone.DISCARD, armor2);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(6, scn.GetFreepsDeckCount());
		// Taken by its type and not revealed: the game log names it, and not a card left in the deck.
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(shards));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(armor));

		// The opponent is shown nothing, and is asked whether to draw.
		assertTrue(scn.ShadowDecisionAvailable("draw a card"));
		assertEquals(shadowHand, scn.GetShadowHandCount());
		scn.ShadowChooseYes();
		assertEquals(shadowHand + 1, scn.GetShadowHandCount());
		assertEquals(shadowDeck - 1, scn.GetShadowDeckCount());
		assertEquals(1, scn.GetFreepsHandCount());

		// The deck that was looked through is shuffled; the other player's is not.
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NothingNeedBeTakenAndTheOpponentMayDeclineToDrawUntilDenethorIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var denethor = scn.GetFreepsCard("denethor");
		var armor = scn.GetFreepsCard("armor");
		var bowmen = scn.GetFreepsCard("bowmen");
		var valor = scn.GetFreepsCard("valor");
		scn.MoveCompanionsToTable(denethor);
		scn.MoveCardsToFreepsDiscard("aragorn", "merchant", "pippin", "guard", "armor2", "shards", "knight", "dwarvenarmor", "fear");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToTopOfDeck(armor, bowmen, valor);
		scn.MoveCardsToTopOfDeck(scn.GetShadowCard("knight"));

		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDeckCount());
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowHand = scn.GetShadowHandCount();
		int shadowDeck = scn.GetShadowDeckCount();

		// The Free Peoples player looks and takes nothing; the opponent is asked all the same, and says no.
		scn.FreepsUseCardAction(denethor);
		assertEquals(1, scn.GetWoundsOn(denethor));
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(armor, bowmen));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());

		assertTrue(scn.ShadowDecisionAvailable("draw a card"));
		scn.ShadowChooseNo();
		assertEquals(shadowHand, scn.GetShadowHandCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// No limit: a second use in the same phase, this time for the condition.
		assertTrue(scn.FreepsActionAvailable(denethor));
		scn.FreepsUseCardAction(denethor);
		assertEquals(2, scn.GetWoundsOn(denethor));
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(bowmen);
		assertInZone(Zone.HAND, bowmen);
		assertInZone(Zone.DECK, armor, valor);
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		scn.ShadowChooseYes();
		assertEquals(shadowHand + 1, scn.GetShadowHandCount());
		assertEquals(shadowDeck - 1, scn.GetShadowDeckCount());

		// Vitality 3 with 2 wounds: exhausted, with an Armor still in the deck.
		assertFalse(scn.FreepsActionAvailable(denethor));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AbilityIsOfferedAtSites3And6AndAtNoSiteBeforeOrBetween() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var denethor = scn.GetFreepsCard("denethor");
		var valor = scn.GetFreepsCard("valor");
		var knight = scn.GetFreepsCard("knight");
		var dwarvenArmor = scn.GetFreepsCard("dwarvenarmor");
		var fear = scn.GetFreepsCard("fear");
		scn.MoveCompanionsToTable(denethor);
		// Every [gondor] item and condition is out of the deck.
		scn.MoveCardsToFreepsDiscard("aragorn", "merchant", "pippin", "guard", "armor", "armor2", "shards", "bowmen");

		scn.StartGame();
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		assertFalse(scn.FreepsActionAvailable(denethor));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.SkipToSite(2);
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		assertFalse(scn.FreepsActionAvailable(denethor));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.SkipToSite(3);
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		assertTrue(scn.FreepsActionAvailable(denethor));

		scn.SkipToSite(4);
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		assertFalse(scn.FreepsActionAvailable(denethor));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.SkipToSite(5);
		assertFalse(scn.FreepsActionAvailable(denethor));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.SkipToSite(6);
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		assertTrue(scn.FreepsActionAvailable(denethor));

		// At the second sanctuary, with nothing in the deck that can be taken: Denethor exerts, the deck is looked
		// through and shuffled, and the opponent may still draw.
		scn.MoveCardsToTopOfDeck(valor, knight, dwarvenArmor, fear);
		scn.MoveCardsToTopOfDeck(scn.GetShadowCard("knight"));
		int hand = scn.GetFreepsHandCount();
		assertEquals(4, scn.GetFreepsDeckCount());
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowHand = scn.GetShadowHandCount();
		assertEquals(0, scn.GetWoundsOn(denethor));

		scn.FreepsUseCardAction(denethor);
		assertEquals(1, scn.GetWoundsOn(denethor));
		scn.FreepsDismissRevealedCards();
		assertEquals(hand, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());

		assertTrue(scn.ShadowDecisionAvailable("draw a card"));
		scn.ShadowChooseYes();
		assertEquals(shadowHand + 1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
