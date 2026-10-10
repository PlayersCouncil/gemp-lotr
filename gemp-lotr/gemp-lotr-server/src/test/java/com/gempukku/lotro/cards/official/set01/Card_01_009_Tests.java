package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_009_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "1_9");
					put("gimli", "1_13");
					put("preparations", "7_12");
					put("dcard", "2_10");

					put("orc1", "1_178");
					put("orc2", "1_181");
					put("orc3", "1_184");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DwarvenAxeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Dwarven Axe
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: 2
		 * Game Text: Bearer must be a Dwarf.
		 * Each time a minion loses a skirmish to bearer, that minion's owner must discard the top card of their draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("axe");
		assertEquals("Dwarven Axe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void AxeMustBeBorneByDwarf() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(axe, gimli);

		scn.StartGame();
		assertFalse(scn.FreepsPlayAvailable(axe));
		scn.FreepsPlayCard(gimli);
		assertTrue(scn.FreepsPlayAvailable(axe));
	}

	@Test
	public void AxeCausesDeckDiscardOnSkirmishVictory() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);

		var orc1 = scn.GetShadowCard("orc1");
		scn.MoveMinionsToTable(orc1);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(gimli, orc1);

		scn.FreepsResolveSkirmish(gimli);
		scn.PassCurrentPhaseActions();

		var topcard = scn.GetShadowTopOfDeck();
		assertEquals(Zone.DECK, topcard.getZone());
		assertEquals(6, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowDiscardCount());

		scn.FreepsResolveActionOrder("Axe");
		assertEquals(Zone.DISCARD, topcard.getZone());
		assertEquals(5, scn.GetShadowDeckCount());
		assertEquals(2, scn.GetShadowDiscardCount());//orc1, and a card discarded from the deck

	}

	@Test
	public void AxeAffectsMaxOneMinionDuringSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);

		var orc1 = scn.GetShadowCard("orc1");
		var orc2 = scn.GetShadowCard("orc2");
		scn.MoveMinionsToTable(orc1, orc2);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);

		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(gimli, orc1, orc2);

		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsUseCardAction(gimli);
		scn.ShadowPass();
		scn.FreepsUseCardAction(gimli);
		scn.ShadowPass();
		scn.FreepsPass();

		// Comprehensive Rules 4.2, Section Three, DWARVEN AXE: "This card can trigger only once for each Shadow
		// player with a minion in that skirmish, regardless of how many minions that player had."
		// Both minions lost at the one moment, so the wounds for losing and a trigger for each minion are put to
		// the Free Peoples player to be ordered.
		assertEquals(3, scn.FreepsGetAvailableActions().size());

		var topcard = scn.GetShadowTopOfDeck();
		assertEquals(Zone.DECK, topcard.getZone());
		assertEquals(5, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowDiscardCount());

		// The first trigger carried out takes the Axe's one use for this skirmish.  The second is dropped: it is
		// not put to the player again, and nothing more is discarded (F-0199).
		scn.FreepsResolveActionOrder("Axe");
		assertEquals(Zone.DISCARD, topcard.getZone());
		assertEquals(4, scn.GetShadowDeckCount());
		assertEquals(3, scn.GetShadowDiscardCount());//2 dead minions, and a card discarded from the deck
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	protected VirtualTableScenario GetFierceScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "1_9");
					put("guard", "1_7");        // Dwarf Guard: strength 4, vitality 2, no damage bonus
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; Damage +1 and nothing else
					put("runner", "1_178");     // Goblin Runner: only ever a card in the draw deck here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The once is once a skirmish, not once a turn: a minion that loses to bearer in the skirmish and again in the
	// fierce skirmish has lost two skirmishes, and its owner discards each time.
	@Test
	public void AMinionThatLosesToBearerInTwoSkirmishesOfOneTurnDiscardsEachTime() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFierceScenario();

		var axe = scn.GetFreepsCard("axe");
		var guard = scn.GetFreepsCard("guard");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, axe);
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, CardType.MINION, null, Keyword.FIERCE));

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, savage);
		scn.FreepsResolveSkirmish(guard);

		int deck = scn.GetShadowDeckCount();
		int freepsDeck = scn.GetFreepsDeckCount();

		// The Guard, 4 + 2, beats the Savage, 5.
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertEquals(1, scn.GetShadowDiscardCount());

		// The fierce skirmish: the Savage loses to the Guard a second time.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(guard, savage);
		scn.FreepsResolveSkirmish(guard);
		scn.PassFierceSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertEquals(2, scn.GetShadowDiscardCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}


	/**
	 * https://github.com/PlayersCouncil/gemp-lotr/issues/571
	 * Basic issue is that based on the last CRD:
	 * 	 > A losing character is any character on the losing side when a skirmish revolves.
	 * 	 > If a character is removed from his or her skirmish and there are still one or more characters
	 * 	 > on each side of that skirmish, the removed character is neither a losing nor a winning character.
	 * 	 > A character removed from a skirmish is not wounded (or overwhelmed) when that skirmish resolves.
	 * This supercedes the Comprehensive Rules 4.0, which states:
	 *   > A losing character is any character on the losing side in a skirmish when it resolves.
	 *   > Also, any character removed during his or her skirmish is a losing character, even if
	 *   > that character’s side eventually wins.
	 *
	 * From a basic functionality standpoint, killed characters should not stick around as part of the
	 * tracked losing characters lists, or at the very least the skirmish resolution should check to see
	 * if they are still in the skirmish and disqualify them for trigger emission if so.
	 */

	@Test
	public void AxeDoesNotTriggerOnMinionKilledBeforeSkirmishResolves() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var dcard = scn.GetFreepsCard("dcard");
		var gimli = scn.GetFreepsCard("gimli");
		var preparations = scn.GetFreepsCard("preparations");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);
		scn.MoveCardsToSupportArea(preparations);
		scn.StackCardsOn(preparations, dcard);

		var orc1 = scn.GetShadowCard("orc1");
		var orc2 = scn.GetShadowCard("orc2");
		var orc3 = scn.GetShadowCard("orc3");
		scn.MoveMinionsToTable(orc1, orc2, orc3);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);

		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(gimli, orc1, orc2, orc3);

		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsUseCardAction(preparations);
		scn.FreepsChooseCard(orc3);
		assertEquals(Zone.DISCARD, orc3.getZone());

		scn.ShadowPass();
		scn.FreepsPass();

		//There should not be any axe trigger here; we should go straight to regroup

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}

	protected VirtualTableScenario GetKilledMinionScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "1_9");
					put("gimli", "1_13");       // Gimli, Son of Glóin: strength 6, vitality 3, damage +1; a skirmish ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1, so the wound for losing kills it
					put("troop", "1_177");      // Goblin Patrol Troop: no text; only ever a card in the draw deck here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The oldest of the old `at` tests (dwarvenAxeDoesNotFreeze, 2011): the minion that lost dies of the very loss
	// the Axe answers.  Here the wound is carried out first, so the minion is gone before the Axe's trigger is.
	@Test
	public void TheDiscardIsStillMadeWhenTheWoundForLosingIsCarriedOutFirstAndKillsTheMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKilledMinionScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, runner);
		scn.FreepsResolveSkirmish(gimli);

		var top = scn.GetShadowTopOfDeck();
		int deck = scn.GetShadowDeckCount();
		int freepsDeck = scn.GetFreepsDeckCount();
		assertTrue(deck >= 2);
		// 6 + 2 for the Axe, against the Runner's 5.
		assertEquals(8, scn.GetStrength(gimli));

		scn.PassSkirmishActions();

		// The wound for losing and the Axe's trigger come at the one moment, and the Free Peoples player is asked
		// which to carry out first.
		assertTrue(scn.FreepsDecisionAvailable("Required responses"));
		assertEquals(2, scn.FreepsGetAvailableActions().size());
		assertTrue(scn.FreepsActionAvailable("Dwarven Axe"));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertInZone(Zone.DECK, top);

		scn.FreepsResolveRuleFirst();

		// The Runner is dead, and its owner still discards: the top card of his own draw deck and no other.
		assertInZone(Zone.DISCARD, runner);
		assertInZone(Zone.DISCARD, top);
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// The same loss with no card left in the minion's owner's draw deck, which is the table the 2011 test stood on:
	// there is nothing to discard, and the game goes on.
	@Test
	public void WithNoCardInItsOwnersDrawDeckTheLosingMinionsOwnerDiscardsNothingAndTheGameGoesOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKilledMinionScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.MoveCardsToDiscard(new ArrayList<>(scn.GetShadowDrawDeck()).toArray(new PhysicalCardImpl[0]));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, runner);
		scn.FreepsResolveSkirmish(gimli);

		int discard = scn.GetShadowDiscardCount();
		int freepsDeck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetShadowDeckCount());
		assertTrue(freepsDeck > 0);

		scn.PassSkirmishActions();

		// The Axe's trigger is carried out first, and finds no card: nothing is discarded from either deck.
		assertTrue(scn.FreepsDecisionAvailable("Required responses"));
		scn.FreepsResolveActionOrder("Dwarven Axe");
		assertEquals(0, scn.GetShadowDeckCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());

		// The wound for losing follows without a question, and the Runner is the one card added to the pile.
		assertInZone(Zone.DISCARD, runner);
		assertEquals(discard + 1, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
