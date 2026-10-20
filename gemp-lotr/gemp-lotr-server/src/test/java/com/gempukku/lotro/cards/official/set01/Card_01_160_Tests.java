package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_160_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "1_160");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9, vitality 2; Damage +1 is its whole text
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, strength 8, vitality 2; Damage +1 is its whole text
					// Uruk Pursuer: [isengard] Uruk-hai printed fierce, strength 5, vitality 3; tracker; his texts are about
					// unbound Hobbits, and none is on the table
					put("pursuer", "4_190");
					put("worker", "3_62");      // Isengard Worker: [isengard] Orc, strength 5, vitality 3; a regroup ability used only on purpose
					// Saruman, Master of the White Hand: an [uruk-hai] Wizard (the culture without the race); each companion
					// is resistance -1, and a skirmish ability used only on purpose
					put("saruman", "17_116");

					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukhaiSwordStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Uruk-hai Sword
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +2
		 * Game Text: Bearer must be an Uruk-hai.
		 * Each time bearer wins a skirmish, the Free Peoples player must discard the top card of his or her draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sword");

		assertEquals("Uruk-hai Sword", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getPossessionClasses().size());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlayedOnAnUrukHaiOfAnyCultureAndNotOnAnOrcOrAWizard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var savage = scn.GetShadowCard("savage");
		var guard = scn.GetShadowCard("guard");
		var worker = scn.GetShadowCard("worker");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, guard, worker, saruman);
		scn.SetTwilight(10);
		assertInHand(sword);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(sword));

		scn.ShadowPlayCard(sword);
		// An Uruk-hai of [isengard] and one of [sauron]; neither the [isengard] Orc nor the [uruk-hai] Wizard.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(worker, saruman));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(guard);
		assertInZone(Zone.ATTACHED, sword);
		assertEquals(guard, sword.getAttachedTo());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenBearerWinsTheFreePeoplesPlayerDiscardsTheTopCardOfHisDrawDeckAndNotWhenAnotherUrukHaiWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var guard = scn.GetShadowCard("guard");
		var sentry = scn.GetShadowCard("sentry");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var top1 = scn.GetFreepsCard("savage");
		var top2 = scn.GetFreepsCard("worker");
		var shadowTop = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli, aragorn);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(top2, top1, shadowTop);
		assertEquals(top1, scn.GetFreepsTopOfDeck());
		assertEquals(top2, scn.GetFromTopOfFreepsDeck(2));
		assertEquals(shadowTop, scn.GetShadowTopOfDeck());
		assertInZone(Zone.DECK, top1, top2);
		scn.MoveMinionsToTable(guard, sentry);
		scn.AttachCardsTo(sentry, sword);
		assertEquals(sentry, sword.getAttachedTo());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, guard}, new PhysicalCardImpl[]{gimli, sentry});
		int deck = scn.GetFreepsDeckCount();
		int discard = scn.GetFreepsDiscardCount();
		int shadowDeck = scn.GetShadowDeckCount();
		assertEquals(top1, scn.GetFreepsTopOfDeck());
		assertEquals(shadowTop, scn.GetShadowTopOfDeck());

		// The Guard, an Uruk-hai with no Sword, 9 against 8: it wins, and nothing is discarded.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(top1, scn.GetFreepsTopOfDeck());

		// The bearer, 8 + 2 against 6, wins: the top card of the Free Peoples deck, then the skirmish damage.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		// What answers the win comes first, with nothing to order (engine pass 54).
		assertEquals(2, scn.GetWoundsOn(gimli));

		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(discard + 1, scn.GetFreepsDiscardCount());
		assertInDiscard(top1);
		assertEquals(top2, scn.GetFreepsTopOfDeck());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(shadowTop, scn.GetShadowTopOfDeck());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NothingIsDiscardedWhenTheBearerLosesASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.AttachCardsTo(savage, sword);
		assertEquals(savage, sword.getAttachedTo());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		int deck = scn.GetFreepsDeckCount();
		int discard = scn.GetFreepsDiscardCount();
		var top = scn.GetFreepsTopOfDeck();

		// The bearer, 5 + 2 against 8, loses and lives.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(discard, scn.GetFreepsDiscardCount());
		assertEquals(top, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AFierceBearerWinningTwiceInATurnDiscardsATopCardEachTime() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var pursuer = scn.GetShadowCard("pursuer");
		var gimli = scn.GetFreepsCard("gimli");
		var top1 = scn.GetFreepsCard("savage");
		var top2 = scn.GetFreepsCard("worker");
		var top3 = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(top3, top2, top1);
		assertEquals(top1, scn.GetFreepsTopOfDeck());
		assertEquals(top2, scn.GetFromTopOfFreepsDeck(2));
		assertEquals(top3, scn.GetFromTopOfFreepsDeck(3));
		assertInZone(Zone.DECK, top1, top2, top3);
		scn.MoveMinionsToTable(pursuer);
		scn.AttachCardsTo(pursuer, sword);
		assertEquals(pursuer, sword.getAttachedTo());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, pursuer);
		scn.FreepsResolveSkirmish(gimli);
		int deck = scn.GetFreepsDeckCount();
		assertEquals(top1, scn.GetFreepsTopOfDeck());

		// The bearer, 5 + 2 against 6, wins: one card.
		scn.PassSkirmishActions();
		// What answers the win comes first, with nothing to order (engine pass 54).
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertInDiscard(top1);
		assertEquals(top2, scn.GetFreepsTopOfDeck());

		// The fierce skirmish: Gimli again, and he loses again: a second card.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gimli, pursuer);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassFierceSkirmishActions();
		// What answers the win comes first, with nothing to order (engine pass 54).
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertInDiscard(top1, top2);
		assertEquals(top3, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
