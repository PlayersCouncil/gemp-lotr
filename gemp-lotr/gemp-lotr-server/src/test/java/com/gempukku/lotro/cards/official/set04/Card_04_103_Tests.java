package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_103_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("treebeard", "4_103");
					put("merry", "1_302");      // Merry, Friend to Sam: [shire] unbound Hobbit, cost 1; a skirmish ability used only on purpose
					// Pippin, Trollslayer: a [gondor] unbound Hobbit, cost 2.  "To play, spot a [gondor] companion";
					// his skirmish ability is used only on purpose.
					put("pippin", "103_31");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] unbound companion who is no Hobbit
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound Hobbit who is not the Ring-bearer
					put("bounder", "1_286");    // Bounder: a Hobbit ally (only companions are unbound); his ability is not used

					// Beauty Is Fading: [wraith] Maneuver event, cost 5.  "Exert a Nazgûl to discard a Free Peoples
					// possession or Free Peoples condition.  If you can spot no such card, discard an ally or
					// companion (except the Ring-bearer) instead."  No Free Peoples possession or condition is
					// laid where it is played, so the Shadow player chooses the ally or companion.
					put("beauty1", "1_205");
					put("beauty2", "1_205");
					put("beauty3", "1_205");
					// Úlairë Enquëa: a Nazgûl of vitality 4, to exert for the event three times; his own ability
					// needs 6 companions and is never offered.
					put("enquea", "1_231");
					// Warmed Up a Bit: [shire] Skirmish event, cost 0.  "Discard an unbound Hobbit."
					put("warmed", "4_322");
					put("runner", "1_178");     // Goblin Runner: strength 5; cheated in, so its text never comes up
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text; overwhelms Merry
					// Refuge: [elven] condition.  "Fellowship: Discard a companion from hand to heal a companion."
					// Cheated into play, so its "To play, spot an Elf" does not come up.
					put("refuge", "12_21");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetInsteadScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("treebeard", "4_103");
					// Merry, Unquenchable Hobbit: "Skirmish: If Merry is not assigned to a skirmish, discard him
					// to remove a [sauron] Orc from a skirmish involving an unbound companion."
					put("merry", "4_311");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound companion, strength 8
					// Orc Soldier: a [sauron] Orc, strength 7, vitality 2; its skirmish ability is not used.
					put("soldier", "1_271");
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc, strength 5; cheated in
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetStackScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("treebeard", "4_103");
					put("merry", "1_302");      // Merry, Friend to Sam: [shire] unbound Hobbit, cost 1
					// Pippin, Trollslayer: a [gondor] unbound Hobbit, cost 2.  "To play, spot a [gondor] companion."
					put("pippin", "103_31");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the [gondor] companion Pippin needs
					// Merry, Unquenchable Hobbit: an unbound Hobbit of another blueprint, stacked on the tale below
					// and not on Treebeard (a choice from a stack is read by blueprint).
					put("merry2", "4_311");
					// Ever My Heart Rises: [dwarven] tale in the support area.  "When you play this condition,
					// reveal the top 6 cards of your draw deck and stack them here.  Fellowship: Spot a Dwarf and
					// discard the top card of your draw deck to take a Free Peoples card stacked here into hand."
					// Cheated in; with no Dwarf its ability is never offered.
					put("heart", "4_46");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TreebeardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Treebeard, Earthborn
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Ally
		 * Subtype: Ent
		 * Strength: 12
		 * Vitality: 4
		 * Site Number: 2T & 8T
		 * Game Text: <b>Unhasty</b>.<br><b>Response:</b> If an unbound Hobbit is about to be discarded, stack him here instead.<br><b>Fellowship:</b> Exert Treebeard and add (1) to play an unbound Hobbit stacked here.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("treebeard");

		assertEquals("Treebeard", card.getBlueprint().getTitle());
		assertEquals("Earthborn", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.ENT, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.UNHASTY));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.TWO_TOWERS, 2)));
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.TWO_TOWERS, 8)));
	}

	@Test
	public void UnboundHobbitsOfEitherCultureAboutToBeDiscardedAreStackedOnTreebeardInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treebeard = scn.GetFreepsCard("treebeard");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var warmed = scn.GetFreepsCard("warmed");
		scn.MoveAlliesToTable(treebeard);
		scn.MoveCompanionsToTable(merry, pippin, aragorn);
		scn.MoveCardsToHand(warmed);

		var beauty = scn.GetShadowCard("beauty1");
		var enquea = scn.GetShadowCard("enquea");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(beauty);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea, runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// The Shadow player discards Merry, the [shire] Hobbit, by his event.
		assertEquals(0, scn.GetStackedCards(treebeard).size());
		assertTrue(scn.ShadowPlayAvailable(beauty));
		scn.ShadowPlayCard(beauty);
		assertEquals(1, scn.GetWoundsOn(enquea));
		assertTrue(scn.ShadowHasCardChoiceAvailable(merry, aragorn, treebeard));
		scn.ShadowChooseCard(merry);

		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(treebeard));
		scn.FreepsChooseOptionalTrigger(treebeard);
		assertInZone(Zone.STACKED, merry);
		assertTrue(scn.GetStackedCards(treebeard).contains(merry));
		assertEquals(0, scn.GetFreepsDiscardCount());

		// The Free Peoples player discards Pippin, the [gondor] Hobbit, by his own event in a skirmish.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertTrue(scn.FreepsPlayAvailable(warmed));
		scn.FreepsPlayCard(warmed);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(treebeard));
		scn.FreepsChooseOptionalTrigger(treebeard);
		assertInZone(Zone.STACKED, pippin);
		assertTrue(scn.GetStackedCards(treebeard).contains(pippin));
		assertEquals(2, scn.GetStackedCards(treebeard).size());
		// Only the event itself is in the discard pile.
		assertInZone(Zone.DISCARD, warmed);
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedForAnUnboundCompanionWhoIsNoHobbitARingBoundHobbitOrAHobbitAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treebeard = scn.GetFreepsCard("treebeard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveAlliesToTable(treebeard, bounder);
		scn.MoveCompanionsToTable(aragorn, sam);

		var beauty1 = scn.GetShadowCard("beauty1");
		var beauty2 = scn.GetShadowCard("beauty2");
		var beauty3 = scn.GetShadowCard("beauty3");
		var enquea = scn.GetShadowCard("enquea");
		scn.MoveCardsToHand(beauty1, beauty2, beauty3);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea);
		scn.SkipToPhase(Phase.MANEUVER);
		// 5 for each of the three events
		scn.SetTwilight(15);
		scn.FreepsPass();

		// Aragorn: unbound, and no Hobbit.
		scn.ShadowPlayCard(beauty1);
		scn.ShadowChooseCard(aragorn);
		assertInZone(Zone.DISCARD, aragorn);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(treebeard));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		scn.FreepsPass();

		// Sam: a Hobbit, and Ring-bound.
		scn.ShadowPlayCard(beauty2);
		scn.ShadowChooseCard(sam);
		assertInZone(Zone.DISCARD, sam);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(treebeard));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		scn.FreepsPass();

		// Bounder: a Hobbit, and an ally, who is never unbound.
		scn.ShadowPlayCard(beauty3);
		scn.ShadowChooseCard(bounder);
		assertInZone(Zone.DISCARD, bounder);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(treebeard));
		assertEquals(0, scn.GetStackedCards(treebeard).size());
		assertEquals(3, scn.GetWoundsOn(enquea));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWhenAnUnboundHobbitIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treebeard = scn.GetFreepsCard("treebeard");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveAlliesToTable(treebeard);
		scn.MoveCompanionsToTable(merry);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, troop);
		scn.FreepsResolveSkirmish(merry);
		// Merry 3 against the Troop's 13: overwhelmed, and so killed, not discarded.
		scn.PassSkirmishActions();

		assertInZone(Zone.DEAD, merry);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(treebeard));
		assertEquals(0, scn.GetStackedCards(treebeard).size());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenAnUnboundHobbitIsDiscardedFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treebeard = scn.GetFreepsCard("treebeard");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var refuge = scn.GetFreepsCard("refuge");
		scn.MoveAlliesToTable(treebeard);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(refuge);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 1);
		scn.MoveCardsToHand(merry);
		scn.FreepsRefreshDecision();

		assertInHand(merry);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsActionAvailable(refuge));
		// Merry is the one companion in hand, and is discarded from it to pay for Refuge.
		scn.FreepsUseCardAction(refuge);

		assertInZone(Zone.DISCARD, merry);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(treebeard));
		assertEquals(0, scn.GetStackedCards(treebeard).size());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void MerryDiscardedToPayForHisAbilityIsStackedInsteadAndTheOrcIsStillRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetInsteadScenario();

		var treebeard = scn.GetFreepsCard("treebeard");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.MoveAlliesToTable(treebeard);

		var soldier = scn.GetShadowCard("soldier");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, runner);
		scn.SkipToAssignments();
		// Both minions on Aragorn, and Merry assigned to nobody.
		scn.FreepsAssignToMinions(aragorn, soldier);
		scn.ShadowAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishing(soldier));
		assertTrue(scn.FreepsActionAvailable(merry));
		scn.FreepsUseCardAction(merry);

		// The discard that pays for the ability is answered, and Merry is stacked in its place.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(treebeard));
		scn.FreepsChooseOptionalTrigger(treebeard);
		assertInZone(Zone.STACKED, merry);
		assertTrue(scn.GetStackedCards(treebeard).contains(merry));
		assertEquals(0, scn.GetFreepsDiscardCount());

		// The cost is paid all the same (Ketura: "instead" is a conversion, not a prevention), so the [sauron]
		// Orc leaves the skirmish, which goes on with the Runner.
		assertFalse(scn.IsCharSkirmishing(soldier));
		assertTrue(scn.IsCharSkirmishing(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void FellowshipAbilityExertsTreebeardAndAdds1ToPlayAnUnboundHobbitStackedHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStackScenario();

		var treebeard = scn.GetFreepsCard("treebeard");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry2 = scn.GetFreepsCard("merry2");
		var heart = scn.GetFreepsCard("heart");
		scn.MoveAlliesToTable(treebeard);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(heart);
		scn.StackCardsOn(treebeard, merry, pippin);
		scn.StackCardsOn(heart, merry2);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(treebeard));
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.STACKED, merry, pippin, merry2);
		assertTrue(scn.FreepsActionAvailable(treebeard));
		scn.FreepsUseCardAction(treebeard);

		// Both costs are paid ahead of the choice.
		assertEquals(1, scn.GetWoundsOn(treebeard));
		assertEquals(1, scn.GetTwilight());
		// Both Hobbits stacked here are offered; the Hobbit stacked on the tale is not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry2));
		scn.FreepsChooseCard(merry);

		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertInZone(Zone.STACKED, pippin, merry2);
		assertTrue(scn.GetStackedCards(treebeard).contains(pippin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityIsNotOfferedWhileTreebeardIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStackScenario();

		var treebeard = scn.GetFreepsCard("treebeard");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveAlliesToTable(treebeard);
		scn.StackCardsOn(treebeard, merry);

		scn.StartGame();
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(treebeard, 3);
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetWoundsOn(treebeard));
		assertInZone(Zone.STACKED, merry);
		assertFalse(scn.FreepsActionAvailable(treebeard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityIsNotOfferedUntilAHobbitStackedHereCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStackScenario();

		var treebeard = scn.GetFreepsCard("treebeard");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveAlliesToTable(treebeard);

		scn.StartGame();

		// Nothing stacked here: the play is the price of the ability, and there is nothing to pay it with.
		assertEquals(0, scn.GetStackedCards(treebeard).size());
		assertFalse(scn.FreepsActionAvailable(treebeard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Pippin stacked, with no [gondor] companion for his own "To play": he cannot be played.
		scn.StackCardsOn(treebeard, pippin);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.STACKED, pippin);
		assertFalse(scn.FreepsActionAvailable(treebeard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// With Aragorn in play he can, and the ability is offered.
		scn.MoveCompanionsToTable(aragorn);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(treebeard));
		scn.FreepsUseCardAction(treebeard);
		assertEquals(1, scn.GetWoundsOn(treebeard));
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
