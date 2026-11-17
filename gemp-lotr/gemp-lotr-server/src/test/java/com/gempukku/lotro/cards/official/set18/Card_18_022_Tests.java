package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_022_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("librarian", "18_22");
					// Erland, Dale Counselor: [gandalf] Man companion, strength 5; a skirmish ability used only on purpose
					put("erland", "11_30");
					// Ottar, Man of Laketown: [gandalf] Man ally, strength 2; a fellowship ability used only on purpose
					put("ottar", "1_80");
					// Gandalf, Friend of the Shirefolk: a [gandalf] companion who is no Man; 6, +1 for each race in the
					// fellowship (Hobbit and Man here: 8)
					put("gandalf", "1_72");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, strength 8

					// Not the First Halfling: [gandalf] support-area condition that takes [gandalf] tokens by its own
					// fellowship ability, used only on purpose
					put("halfling", "8_18");
					put("halfling2", "8_18");
					// Fear and Great Wonder: [gandalf] support-area condition, cost 1; when played, a [gandalf] token
					// is added to it
					put("fear", "13_30");
					// A Path Appointed: [gondor] support-area condition that takes [gondor] tokens; its abilities are
					// used only on purpose
					put("path", "8_42");

					put("pallando", "13_37");   // Pallando, Far-travelling One: [gandalf] follower; Aid (2)
					put("worker", "15_135");    // Rohan Worker: [rohan] follower; its texts answer a transfer and a win
					put("saruman", "17_38");    // Saruman, Servant of Sauron: the Shadow player's [isengard] follower

					put("mwizard", "1_78");     // Mysterious Wizard: [gandalf] skirmish event
					put("secretfire", "1_83");  // Servant of the Secret Fire: [gandalf] skirmish event
					put("longfell", "4_97");    // Long I Fell: [gandalf] skirmish event, for the Shadow player's discard pile
					put("durin", "1_71");       // Durin's Secret: [gandalf] fellowship event
					put("dagger", "1_102");     // Dagger Strike: [gondor] skirmish event
					put("filler", "1_177");     // Goblin Patrol Troop: a card with no text, to be drawn
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LibrarianStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Librarian, Keeper of Ancient Texts
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 3
		 * Vitality: 3
		 * Resistance: 5
		 * Game Text: While you can spot 2 [gandalf] tokens, each of your [gandalf] Men is strength +2.<br><b>Regroup:</b> Discard a follower from play to choose one: draw a card; or take a [gandalf] skirmish event from your discard pile into hand.
		*/

		var scn = GetScenario();

		var librarian = scn.GetFreepsCard("librarian");

		assertEquals("Librarian", librarian.getBlueprint().getTitle());
		assertEquals("Keeper of Ancient Texts", librarian.getBlueprint().getSubtitle());
		assertTrue(librarian.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, librarian.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, librarian.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, librarian.getBlueprint().getCardType());
		assertEquals(Race.MAN, librarian.getBlueprint().getRace());
		assertEquals(2, librarian.getBlueprint().getTwilightCost());
		assertEquals(3, librarian.getBlueprint().getStrength());
		assertEquals(3, librarian.getBlueprint().getVitality());
		assertEquals(5, librarian.getBlueprint().getResistance());
	}

	@Test
	public void WithASecondGandalfTokenEachGandalfManIsStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var librarian = scn.GetFreepsCard("librarian");
		var erland = scn.GetFreepsCard("erland");
		var ottar = scn.GetFreepsCard("ottar");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var halfling = scn.GetFreepsCard("halfling");
		var fear = scn.GetFreepsCard("fear");
		var path = scn.GetFreepsCard("path");
		scn.MoveCompanionsToTable(librarian, erland, gandalf, aragorn);
		scn.MoveAlliesToTable(ottar);
		scn.MoveCardsToSupportArea(halfling, path);
		// One [gandalf] token, and a [gondor] token beside it that is no [gandalf] token.
		scn.AddTokensToCard(halfling, 1);
		scn.AddTokensToCard(path, 1);
		scn.MoveCardsToHand(fear);
		assertEquals(1, scn.GetCultureTokensOn(halfling));
		assertEquals(1, scn.GetCultureTokensOn(path));

		scn.StartGame();
		assertEquals(3, scn.GetStrength(librarian));
		assertEquals(5, scn.GetStrength(erland));
		assertEquals(2, scn.GetStrength(ottar));
		assertEquals(8, scn.GetStrength(gandalf));
		assertEquals(8, scn.GetStrength(aragorn));

		// Its play puts a [gandalf] token on it: two, on two cards.
		scn.FreepsPlayCard(fear);
		assertEquals(1, scn.GetCultureTokensOn(fear));
		assertEquals(1, scn.GetCultureTokensOn(halfling));
		assertEquals(5, scn.GetStrength(librarian));
		assertEquals(7, scn.GetStrength(erland));
		assertEquals(4, scn.GetStrength(ottar));
		assertEquals(8, scn.GetStrength(gandalf));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupAbilityDiscardsAFollowerFromPlayOfEitherSideToDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var librarian = scn.GetFreepsCard("librarian");
		var aragorn = scn.GetFreepsCard("aragorn");
		var halfling = scn.GetFreepsCard("halfling");
		var pallando = scn.GetFreepsCard("pallando");
		var worker = scn.GetFreepsCard("worker");
		var mwizard = scn.GetFreepsCard("mwizard");
		var filler = scn.GetFreepsCard("filler");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCompanionsToTable(librarian, aragorn);
		// Followers: one in the support area, one borne, and the Shadow player's in his support area; a
		// condition in the support area that is no follower.
		scn.MoveCardsToSupportArea(pallando, halfling, saruman);
		scn.AttachCardsTo(aragorn, worker);
		scn.MoveCardsToDiscard(mwizard);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCardsToTopOfDeck(filler);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.DECK, filler);
		assertInDiscard(mwizard);
		assertInZone(Zone.ATTACHED, worker);
		assertInZone(Zone.SUPPORT, pallando, saruman);
		int deck = scn.GetFreepsDeckCount();
		assertTrue(scn.FreepsActionAvailable(librarian));

		scn.FreepsUseCardAction(librarian);
		assertTrue(scn.FreepsHasCardChoiceAvailable(pallando, worker, saruman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(halfling, librarian, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(worker);
		assertInDiscard(worker);
		assertInZone(Zone.SUPPORT, pallando, saruman);
		// Both can be done: asked which.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());

		scn.FreepsChoose("Draw");
		assertInHand(filler);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertInDiscard(mwizard);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityTakesAGandalfSkirmishEventFromYourDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var librarian = scn.GetFreepsCard("librarian");
		var pallando = scn.GetFreepsCard("pallando");
		var mwizard = scn.GetFreepsCard("mwizard");
		var secretfire = scn.GetFreepsCard("secretfire");
		var durin = scn.GetFreepsCard("durin");
		var dagger = scn.GetFreepsCard("dagger");
		var halfling2 = scn.GetFreepsCard("halfling2");
		var longfell = scn.GetShadowCard("longfell");
		scn.MoveCompanionsToTable(librarian);
		scn.MoveCardsToSupportArea(pallando);
		// Two [gandalf] skirmish events; a [gandalf] event of another phase, a skirmish event of another culture, a
		// [gandalf] card that is no event; and a [gandalf] skirmish event in the Shadow player's discard pile.
		scn.MoveCardsToDiscard(mwizard, secretfire, durin, dagger, halfling2, longfell);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		assertInDiscard(mwizard, secretfire, durin, dagger, halfling2, longfell);
		assertInZone(Zone.SUPPORT, pallando);
		int deck = scn.GetFreepsDeckCount();

		// The only follower in play is discarded unasked.
		scn.FreepsUseCardAction(librarian);
		assertInDiscard(pallando);
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());

		scn.FreepsChoose("Take");
		assertTrue(scn.FreepsHasCardChoiceAvailable(mwizard, secretfire));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(durin, dagger, halfling2, longfell));

		scn.FreepsChooseCardBPFromSelection(secretfire);
		assertInHand(secretfire);
		assertInDiscard(mwizard, durin, dagger, halfling2, longfell);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoGandalfSkirmishEventInTheDiscardPileACardIsDrawnUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var librarian = scn.GetFreepsCard("librarian");
		var pallando = scn.GetFreepsCard("pallando");
		var durin = scn.GetFreepsCard("durin");
		var dagger = scn.GetFreepsCard("dagger");
		var filler = scn.GetFreepsCard("filler");
		var longfell = scn.GetShadowCard("longfell");
		scn.MoveCompanionsToTable(librarian);
		scn.MoveCardsToSupportArea(pallando);
		scn.MoveCardsToDiscard(durin, dagger, longfell);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCardsToTopOfDeck(filler);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.DECK, filler);
		assertInZone(Zone.SUPPORT, pallando);
		assertInDiscard(durin, dagger, longfell);
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsUseCardAction(librarian);
		assertInDiscard(pallando);
		assertInHand(filler);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertInDiscard(durin, dagger, longfell);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithAnEmptyDrawDeckTheSkirmishEventIsTakenUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var librarian = scn.GetFreepsCard("librarian");
		var pallando = scn.GetFreepsCard("pallando");
		var mwizard = scn.GetFreepsCard("mwizard");
		scn.MoveCompanionsToTable(librarian);
		scn.MoveCardsToSupportArea(pallando);
		scn.MoveCardsToDiscard(mwizard);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetFreepsDrawDeck();
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetFreepsDeckCount());
		assertInZone(Zone.SUPPORT, pallando);
		assertInDiscard(mwizard);

		scn.FreepsUseCardAction(librarian);
		assertInDiscard(pallando);
		assertInHand(mwizard);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithNoFollowerInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var librarian = scn.GetFreepsCard("librarian");
		var pallando = scn.GetFreepsCard("pallando");
		var mwizard = scn.GetFreepsCard("mwizard");
		scn.MoveCompanionsToTable(librarian);
		// A follower in hand is not in play.
		scn.MoveCardsToHand(pallando);
		scn.MoveCardsToDiscard(mwizard);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		assertInHand(pallando);
		assertInDiscard(mwizard);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(librarian));
	}
}
