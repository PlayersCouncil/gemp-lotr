package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_003_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer, the one Bilbo in play.
	// The skirmishes of turn 1 are at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armor", "33_3");
					put("balin", "30_2");       // Balin, Brother of Dwalin: the one Balin; [dwarven] Dwarf, 7/3; a fellowship ability
					put("dwalin", "30_9");      // Dwalin, Brother of Balin: another [dwarven] Dwarf companion, 7/3, damage +1; a maneuver ability used only on purpose

					// For the draw deck. Noble intentions and Battle Fury: [dwarven] skirmish events of cost 0 and 1.
					// Battle of Azanulbizar: a [dwarven] maneuver event. Take up arms. Fight!: a [gandalf] skirmish event.
					put("noble", "30_14");
					put("fury", "30_3");
					put("azanulbizar", "30_4");
					put("takeup", "30_31");

					put("bats", "32_30");       // Dark Bats: [moria] Creature, 4/2; a Shadow ability left unused
					put("runner", "31_30");     // Goblin Runner: [moria] Orc, 5/1; a "when you play" text, never reached
					put("spider", "31_55");     // Wicked Spider: [spider] Spider, 7/2, fierce; a skirmish ability that wants an Orc to discard
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void BalinsArmorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Balin's Armor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Armor
		 * Vitality: +1
		 * Game Text: Bearer must be Balin.
		 * 	Each time Bilbo wins a skirmish, you may heal Balin.
		 * 	<b>Fellowship:</b> Exert Balin to take a [dwarven] skirmish event into hand from your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("armor");

		assertEquals("Balin's Armor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.ARMOR));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void PlaysOnBalinAndNotWithoutHimOnAnotherDwarf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		scn.MoveCompanionsToTable(dwalin);
		scn.MoveCardsToHand(armor);

		scn.StartGame();
		assertInHand(armor);

		// Dwalin, a [dwarven] Dwarf companion, and Bilbo are no Balin.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(armor));

		scn.MoveCompanionsToTable(balin);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(armor));
		scn.FreepsPlayCard(armor);

		// Balin is the one bearer it may go on: it goes on him unasked.
		assertAttachedTo(armor, balin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeBilboWinsASkirmishBalinMayBeHealedAndTheOfferMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var armor = scn.GetFreepsCard("armor");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var spider = scn.GetShadowCard("spider");
		scn.MoveCompanionsToTable(balin, dwalin);
		scn.AttachCardsTo(balin, armor);
		scn.AddWoundsToChar(balin, 2);
		scn.AddWoundsToChar(dwalin, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(spider);
		// Bilbo's 3 raised to 8, to beat the fierce Spider's 7 twice.
		scn.ApplyAdHocModifier(new StrengthModifier(null, bilbo, null, 5));
		assertAttachedTo(armor, balin);
		assertEquals(2, scn.GetWoundsOn(balin));
		assertEquals(1, scn.GetWoundsOn(dwalin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, spider);
		scn.FreepsResolveSkirmish(bilbo);
		scn.PassSkirmishActions();

		// Bilbo's first win: the heal is offered, and declined.
		assertEquals(1, scn.GetWoundsOn(spider));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(armor));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(balin));
		assertEquals(1, scn.GetWoundsOn(dwalin));
		assertTrue(scn.InFierceRound());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(bilbo, spider);
		scn.FreepsResolveSkirmish(bilbo);
		scn.PassFierceSkirmishActions();

		// His second win offers it again; Balin heals one wound, and Dwalin none.
		assertInZone(Zone.DISCARD, spider);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(armor));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(1, scn.GetWoundsOn(dwalin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BilboLosingASkirmishAndAnotherCompanionWinningOneOfferNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var armor = scn.GetFreepsCard("armor");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var bats = scn.GetShadowCard("bats");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(balin, dwalin);
		scn.AttachCardsTo(balin, armor);
		scn.AddWoundsToChar(balin, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(bats, runner);
		assertAttachedTo(armor, balin);
		assertEquals(2, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(bilbo));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{bilbo, bats}, new PhysicalCardImpl[]{dwalin, runner});

		// Dwalin, 7, beats Goblin Runner, 5: a companion who is no Bilbo wins.
		scn.FreepsResolveSkirmish(dwalin);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(armor));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// Bilbo, 3, loses to Dark Bats, 4, and lives.
		scn.FreepsResolveSkirmish(bilbo);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(armor));
		assertEquals(2, scn.GetWoundsOn(balin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BilboWinningWithBalinUnwoundedOffersNoHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var armor = scn.GetFreepsCard("armor");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCompanionsToTable(balin, dwalin);
		scn.AttachCardsTo(balin, armor);
		// Dwalin wounded: the heal names Balin alone, so this is nothing for it to act on.
		scn.AddWoundsToChar(dwalin, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(bats);
		// Bilbo's 3 raised to 6, to beat Dark Bats' 4.
		scn.ApplyAdHocModifier(new StrengthModifier(null, bilbo, null, 3));
		assertAttachedTo(armor, balin);
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(1, scn.GetWoundsOn(dwalin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, bats);
		scn.FreepsResolveSkirmish(bilbo);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(bats));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(armor));
		assertEquals(1, scn.GetWoundsOn(dwalin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void FellowshipActionExertsBalinToTakeADwarvenSkirmishEventFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var noble = scn.GetFreepsCard("noble");
		var fury = scn.GetFreepsCard("fury");
		var azanulbizar = scn.GetFreepsCard("azanulbizar");
		var takeup = scn.GetFreepsCard("takeup");
		scn.MoveCompanionsToTable(balin, dwalin);
		scn.AttachCardsTo(balin, armor);

		scn.StartGame();
		scn.SetFreepsDrawDeck(noble, fury, azanulbizar, takeup);
		scn.FreepsRefreshDecision();
		assertAttachedTo(armor, balin);
		assertInZone(Zone.DECK, noble, fury, azanulbizar, takeup);
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(dwalin));
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		assertTrue(scn.FreepsActionAvailable(armor));
		scn.MarkGameLog();
		scn.FreepsUseCardAction(armor);

		// Balin exerts; Dwalin, another [dwarven] Dwarf able to, does not.
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(dwalin));

		scn.FreepsDismissRevealedCards();
		// [dwarven] skirmish events of two costs; not a [dwarven] maneuver event nor a [gandalf] skirmish event.
		assertTrue(scn.FreepsHasCardChoiceAvailable(noble, fury));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(azanulbizar, takeup));
		scn.FreepsChooseCardBPFromSelection(noble);

		assertInHand(noble);
		assertInZone(Zone.DECK, fury, azanulbizar, takeup);
		// The card taken is named in the game log, and one left in the deck is not.
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(noble));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(fury));
		// The deck looked through is shuffled, once; the other player's is not.
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoDwarvenSkirmishEventInTheDrawDeckTheFellowshipActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var balin = scn.GetFreepsCard("balin");
		var azanulbizar = scn.GetFreepsCard("azanulbizar");
		var takeup = scn.GetFreepsCard("takeup");
		scn.MoveCompanionsToTable(balin);
		scn.AttachCardsTo(balin, armor);

		scn.StartGame();
		scn.SetFreepsDrawDeck(azanulbizar, takeup);
		scn.FreepsRefreshDecision();
		assertAttachedTo(armor, balin);
		assertInZone(Zone.DECK, azanulbizar, takeup);
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetFreepsHandCount());
		int shuffles = scn.GetFreepsDeckShuffleCount();

		// A [dwarven] event of another phase and a skirmish event of another culture: nothing to take.
		// A Hobbit-set take from the draw deck with nothing to take is withheld (Q-0019; the orchestrator on Q-0406 and Q-0418): Balin does not exert for a
		// look that finds nothing.
		assertFalse(scn.FreepsActionAvailable(armor));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetFreepsHandCount());
		assertInZone(Zone.DECK, azanulbizar, takeup);
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionIsNotOfferedWhileBalinIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var noble = scn.GetFreepsCard("noble");
		scn.MoveCompanionsToTable(balin, dwalin);
		scn.AttachCardsTo(balin, armor);
		// Balin's 3 vitality, +1 for the Armor: three wounds leave him one.
		scn.AddWoundsToChar(balin, 3);

		scn.StartGame();
		scn.SetFreepsDrawDeck(noble);
		scn.FreepsRefreshDecision();
		assertAttachedTo(armor, balin);
		assertInZone(Zone.DECK, noble);
		assertEquals(3, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(dwalin));

		// Dwalin could exert, but he is no Balin.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(armor));
		assertEquals(0, scn.GetWoundsOn(dwalin));
		assertInZone(Zone.DECK, noble);
	}
}
