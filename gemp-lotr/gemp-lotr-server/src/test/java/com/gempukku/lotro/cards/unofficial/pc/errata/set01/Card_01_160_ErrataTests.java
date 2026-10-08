package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.IsAdditionalRaceModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_160_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "51_160");
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai minion, strength 9, vitality 2; Damage +1 is its whole text
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai minion, strength 8, vitality 2; Damage +1 is its whole text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai minion, strength 5, vitality 3; Damage +1 is its whole text
					put("savage2", "1_151");
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc minion, strength 7; a regroup ability only

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8, vitality 4; a maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: companion, strength 3, vitality 4; a fellowship ability used only on purpose
					put("dwarfguard", "1_7");   // Dwarf Guard: companion, vitality 2; a requirement to play only
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: Elf ally; her text needs her owner's site 3, which is not in play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetCourteousHalflingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "51_160");
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai minion, strength 9, vitality 2; Damage +1 is its whole text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8, vitality 4; a maneuver ability used only on purpose
					put("dwarf1", "1_7");       // Dwarf Guard: companion, strength 4, vitality 2, no text in play
					put("dwarf2", "1_7");
					put("sam", "1_311");        // these two stay in the draw deck, so that two cards could be discarded from it
					put("silinde", "1_60");
				}},
				VirtualTableScenario.FellowshipSites,
				"4_301",                        // Frodo, Courteous Halfling: with 3 unbound companions spotted, Shadow cards may not discard cards from the top of your draw deck
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
		 * Each time bearer wins a skirmish, the Free Peoples player must exert a companion or discard the top 2 cards of their draw deck.
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
		assertEquals(0, card.getBlueprint().getVitality());
	}

	@Test
	public void PlayedForOneOnAnUrukHaiOfAnyCultureAndNotOnAnOrc() throws DecisionResultInvalidException, CardNotFoundException, InvalidCardDefinitionException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var sentry = scn.GetShadowCard("sentry");
		var savage = scn.GetShadowCard("savage");
		var servant = scn.GetShadowCard("servant");
		var sam = scn.GetFreepsCard("sam");
		// Sam is made an Uruk-hai wherever he is: the text names no side, so the Free Peoples player's Uruk-hai may
		// bear it too.  The modifier goes by his name because he is on the table before the game starts.
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, scn.GenerateFreepsFilter("name(Sam)"), null, Race.URUK_HAI));
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		scn.MoveMinionsToTable(sentry, savage, servant);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(sentry));
		assertTrue(scn.ShadowPlayAvailable(sword));
		int twilight = scn.GetTwilight();

		scn.ShadowPlayCard(sword);
		assertTrue(scn.ShadowHasCardChoiceAvailable(sentry, savage, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(servant));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(sentry);
		assertEquals(twilight - 1, scn.GetTwilight());
		assertInZone(Zone.ATTACHED, sword);
		assertEquals(sentry, sword.getAttachedTo());
		assertEquals(10, scn.GetStrength(sentry));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(3, scn.GetStrength(sam));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenBearerWinsTheFreePeoplesPlayerMayExertACompanionOfTheirChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var dwarfguard = scn.GetFreepsCard("dwarfguard");
		var silinde = scn.GetFreepsCard("silinde");
		var sword = scn.GetShadowCard("sword");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCompanionsToTable(aragorn, sam, dwarfguard);
		scn.MoveAlliesToTable(silinde);

		scn.StartGame();
		// Vitality 2 with 1 wound: exhausted, so he cannot be exerted.
		scn.AddWoundsToChar(dwarfguard, 1);
		scn.MoveMinionsToTable(guard);
		scn.AttachCardsTo(guard, sword);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, guard);
		scn.FreepsResolveSkirmish(aragorn);
		int deck = scn.GetFreepsDeckCount();
		scn.FreepsPass();
		scn.ShadowPass();

		// 9 + 2 against 8: the Guard wins.  The skirmish damage first: 1 + damage +1.
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// The choice is the Free Peoples player's.
		assertTrue(scn.FreepsChoiceAvailable("Exert a companion"));
		assertTrue(scn.FreepsChoiceAvailable("Discard the top 2"));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		scn.FreepsChooseOption("Exert");

		// Any companion who can exert, the Ring-bearer and the one who lost included; not an ally, not the exhausted.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(dwarfguard, silinde));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(silinde));
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenBearerWinsTheFreePeoplesPlayerMayDiscardTheTopTwoCardsOfTheirDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var sword = scn.GetShadowCard("sword");
		var guard = scn.GetShadowCard("guard");
		var top1 = scn.GetFreepsCard("savage");
		var top2 = scn.GetFreepsCard("sentry");
		var top3 = scn.GetFreepsCard("servant");
		scn.MoveCompanionsToTable(aragorn, sam);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(top3, top2, top1);
		scn.MoveMinionsToTable(guard);
		scn.AttachCardsTo(guard, sword);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, guard);
		scn.FreepsResolveSkirmish(aragorn);
		int deck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();
		assertEquals(top1, scn.GetFreepsTopOfDeck());
		scn.FreepsPass();
		scn.ShadowPass();

		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseOption("Discard");

		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertInDiscard(top1, top2);
		assertEquals(top3, scn.GetFreepsTopOfDeck());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(scn.GetRingBearer()));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhereShadowCardsMayNotDiscardFromTheTopOfTheDrawDeckTheDiscardIsStillOfferedAndTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCourteousHalflingScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var dwarf1 = scn.GetFreepsCard("dwarf1");
		var dwarf2 = scn.GetFreepsCard("dwarf2");
		var sword = scn.GetShadowCard("sword");
		var guard = scn.GetShadowCard("guard");
		var top1 = scn.GetFreepsCard("sam");
		var top2 = scn.GetFreepsCard("silinde");
		// Frodo is Ring-bound; Aragorn and the two Dwarf Guards are three unbound companions.
		scn.MoveCompanionsToTable(aragorn, dwarf1, dwarf2);

		scn.StartGame();
		scn.MoveMinionsToTable(guard);
		scn.MoveCardsToTopOfDeck(top2, top1);
		scn.AttachCardsTo(guard, sword);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, guard);
		scn.FreepsResolveSkirmish(aragorn);
		int deck = scn.GetFreepsDeckCount();
		int discard = scn.GetFreepsDiscardCount();
		// The Sword, the Guard, Sam and Silinde: more than the two cards the Sword would discard.
		assertEquals(4, deck);
		scn.FreepsPass();
		scn.ShadowPass();

		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		// Frodo stops Shadow cards that make the Free Peoples player discard, but the Sword gives him a choice
		// and he may choose to discard (F-0205: not a fault): the discard is offered beside the exertion, and taken.
		assertTrue(scn.FreepsChoiceAvailable("Exert"));
		assertTrue(scn.FreepsChoiceAvailable("Discard"));

		scn.FreepsChooseOption("Discard");
		assertInDiscard(top1, top2);
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertEquals(discard + 2, scn.GetFreepsDiscardCount());
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(dwarf1));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithOneCardInTheDrawDeckACompanionIsExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var sword = scn.GetShadowCard("sword");
		var guard = scn.GetShadowCard("guard");
		var last = scn.GetFreepsCard("savage");
		scn.MoveCompanionsToTable(aragorn, sam);

		scn.StartGame();
		// Everything else of the Free Peoples player's draw deck is put in the discard pile: one card left.
		scn.MoveCardsToFreepsDiscard("sword", "guard", "sentry", "savage2", "servant", "dwarfguard", "silinde");
		scn.MoveMinionsToTable(guard);
		scn.AttachCardsTo(guard, sword);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, guard);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(1, scn.GetFreepsDeckCount());
		assertEquals(last, scn.GetFreepsTopOfDeck());
		int discard = scn.GetFreepsDiscardCount();
		scn.FreepsPass();
		scn.ShadowPass();

		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// The top 2 cards cannot be discarded: exerting is what is left, and the companion is chosen.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, sam));
		scn.FreepsChooseCard(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetFreepsDeckCount());
		assertEquals(discard, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoCompanionAbleToExertTheTopTwoCardsAreDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetShadowCard("sword");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		// Frodo, vitality 4, exhausted; Aragorn, vitality 4, will be exhausted by the skirmish damage.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(aragorn, 1);
		scn.MoveMinionsToTable(guard);
		scn.AttachCardsTo(guard, sword);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, guard);
		scn.FreepsResolveSkirmish(aragorn);
		int deck = scn.GetFreepsDeckCount();
		scn.FreepsPass();
		scn.ShadowPass();

		scn.FreepsResolveRuleFirst();
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.IsExhausted(aragorn));

		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NothingHappensWhenAnotherMinionWinsOrTheBearerLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var sword = scn.GetShadowCard("sword");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(aragorn, sam);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, savage2);
		scn.AttachCardsTo(savage, sword);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{sam, savage2});
		int deck = scn.GetFreepsDeckCount();

		// The bearer, 5 + 2, loses to Aragorn, 8.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// The other Uruk Savage, 5 with no Sword, beats Sam, 3: 1 + damage +1.
		scn.FreepsResolveSkirmish(sam);
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(2, scn.GetWoundsOn(sam));

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(scn.GetRingBearer()));
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AFierceBearerWinningTwiceInATurnMakesTheChoiceTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetShadowCard("sword");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(guard);
		scn.AttachCardsTo(guard, sword);
		// The Guard made fierce, so that it skirmishes twice in the turn.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, guard, null, Keyword.FIERCE));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, guard);
		scn.FreepsResolveSkirmish(aragorn);
		int deck = scn.GetFreepsDeckCount();
		scn.FreepsPass();
		scn.ShadowPass();

		scn.FreepsResolveRuleFirst();
		scn.FreepsChooseOption("Discard");
		assertEquals(deck - 2, scn.GetFreepsDeckCount());

		scn.PassFierceAssignmentActions();
		// Aragorn again: 2 more wounds kill him, which is a win for the Guard all the same.
		scn.FreepsAssignToMinions(aragorn, guard);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.ShadowPass();

		scn.FreepsResolveRuleFirst();
		assertInZone(Zone.DEAD, aragorn);
		scn.FreepsChooseOption("Discard");
		assertEquals(deck - 4, scn.GetFreepsDeckCount());
		assertEquals(4, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
