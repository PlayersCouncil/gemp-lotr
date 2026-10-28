package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_07_293_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warrior", "7_293");
					put("warrior2", "7_293");
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7, vitality 2; its text answers a weapon played on it
					put("slayer", "3_97");      // Orc Slayer: [sauron] Orc, strength 5; a regroup ability used only on purpose
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, strength 7; a skirmish ability used only on purpose
					// Cirith Ungol Sentry: [sauron] Uruk-hai, a [sauron] minion who is no Orc; Damage +1 is its whole text
					put("sentry", "10_84");
					// Morgul Hound: [wraith] Orc, strength 5; +4 and fierce only at 2 wounds on the Ring-bearer, who has none here
					put("hound", "7_192");
					// Cards for the hand, of three types: a [moria] minion, a [sauron] condition and a [sauron] Shadow event
					put("runner", "1_178");     // Goblin Runner
					put("fear", "1_278");       // Strength Born of Fear
					put("curse", "1_241");      // Curse From Mordor
					// Enheartened Foe: "Response: If a [sauron] Orc wins a skirmish, make that Orc fierce until the regroup phase"
					put("foe", "1_247");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
					// Thrarin, Dwarven Smith: Dwarf ally, strength 4, vitality 2; "Maneuver: Exert Thrarin to allow him to
					// participate in archery fire and skirmishes until the regroup phase"
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MordorWarriorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Mordor Warrior
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: <b>Skirmish:</b> Discard a card from your hand to make a companion skirmishing this minion strength -1 for each other [sauron] Orc you spot (limit -3).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("warrior");

		assertEquals("Mordor Warrior", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithNoOtherSauronOrcToSpotTheCardIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetShadowCard("warrior");
		var sentry = scn.GetShadowCard("sentry");
		var hound = scn.GetShadowCard("hound");
		var runner = scn.GetShadowCard("runner");
		var fear = scn.GetShadowCard("fear");
		var curse = scn.GetShadowCard("curse");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(runner, fear, curse);

		scn.StartGame();
		assertInHand(runner, fear, curse);
		scn.MoveMinionsToTable(warrior, sentry, hound);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, warrior);
		// A [sauron] Uruk-hai and a [wraith] Orc stay out of every skirmish: neither is an other [sauron] Orc.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.ShadowActionAvailable(warrior));
		scn.ShadowUseCardAction(warrior);
		// Any card of the hand: a minion, a condition and an event.
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, fear, curse));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(fear);

		assertInDiscard(fear);
		assertInHand(runner, curse);
		assertEquals(2, scn.GetShadowHandCount());
		// Nothing to spot, so nothing to subtract, and no number is asked.
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithAnEmptyHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetShadowCard("warrior");
		var chieftain = scn.GetShadowCard("chieftain");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, chieftain);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, warrior);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(warrior));
		assertEquals(8, scn.GetStrength(aragorn));
	}

	@Test
	public void MakesTheCompanionSkirmishingHimStrengthMinusTheNumberOfOtherSauronOrcsNamedAndNobodyWhileHeWaits() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetShadowCard("warrior");
		var warrior2 = scn.GetShadowCard("warrior2");
		var chieftain = scn.GetShadowCard("chieftain");
		var sentry = scn.GetShadowCard("sentry");
		var hound = scn.GetShadowCard("hound");
		var fear = scn.GetShadowCard("fear");
		var curse = scn.GetShadowCard("curse");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(fear, curse);

		scn.StartGame();
		assertInHand(fear, curse);
		scn.MoveMinionsToTable(warrior, warrior2, chieftain, sentry, hound);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, warrior}, new PhysicalCardImpl[]{gimli, warrior2});
		// The Chieftain, the Sentry and the Hound stay out of every skirmish.
		scn.ShadowDeclineAssignments();

		// Gimli's skirmish against the other copy first: this Warrior waits for his own against Aragorn.
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(warrior));
		scn.ShadowUseCardAction(warrior);
		scn.ShadowChooseCard(curse);
		assertInDiscard(curse);
		// The other copy and the Chieftain; not the Warrior himself, a [sauron] Uruk-hai or a [wraith] Orc.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertEquals(Integer.valueOf(2), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("2");
		// Nobody is skirmishing this Warrior yet: Gimli fights the other copy and Aragorn waits.
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.PassSkirmishActions();
		// The other copy's 9 against Gimli's 6: a wound, not a kill.
		assertEquals(1, scn.GetWoundsOn(gimli));
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(warrior));
		scn.ShadowUseCardAction(warrior);
		// The one card left in hand goes with nothing asked.
		assertInDiscard(fear);
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("1");
		// One named of the two there are: -1, not -2.
		assertEquals(7, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ThePenaltyStopsAtMinus3ForThePhaseAndASecondCopyGivesItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetShadowCard("warrior");
		var warrior2 = scn.GetShadowCard("warrior2");
		var chieftain = scn.GetShadowCard("chieftain");
		var slayer = scn.GetShadowCard("slayer");
		var soldier = scn.GetShadowCard("soldier");
		var runner = scn.GetShadowCard("runner");
		var fear = scn.GetShadowCard("fear");
		var curse = scn.GetShadowCard("curse");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(runner, fear, curse);

		scn.StartGame();
		assertInHand(runner, fear, curse);
		scn.MoveMinionsToTable(warrior, warrior2, chieftain, slayer, soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, warrior);
		// The other copy goes into the same skirmish; the three other Orcs stay out of every skirmish.
		scn.ShadowAssignToMinions(aragorn, warrior2);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(aragorn));
		scn.ShadowUseCardAction(warrior);
		scn.ShadowChooseCard(curse);
		// Four other [sauron] Orcs: the other copy, the Chieftain, the Slayer and the Soldier.
		assertEquals(4, scn.ShadowGetChoiceMax());
		assertEquals(Integer.valueOf(4), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("4");
		// -4 named, -3 given.
		assertEquals(5, scn.GetStrength(aragorn));
		assertEquals(2, scn.GetShadowHandCount());

		// Again in the same skirmish: offered, the card discarded, and nothing more given.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(warrior));
		scn.ShadowUseCardAction(warrior);
		scn.ShadowChooseCard(fear);
		assertInDiscard(fear);
		scn.ShadowChoose("4");
		assertEquals(5, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetShadowHandCount());

		// The other copy's limit is its own: its -3 comes on top.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(warrior2));
		scn.ShadowUseCardAction(warrior2);
		assertInDiscard(runner);
		assertEquals(4, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("4");
		assertEquals(2, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheLimitIsCountedAcrossUsesInOneSkirmishAndStartsAgainInTheFierceSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetShadowCard("warrior");
		var chieftain = scn.GetShadowCard("chieftain");
		var slayer = scn.GetShadowCard("slayer");
		var runner = scn.GetShadowCard("runner");
		var fear = scn.GetShadowCard("fear");
		var curse = scn.GetShadowCard("curse");
		var foe = scn.GetShadowCard("foe");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(runner, fear, curse, foe);

		scn.StartGame();
		assertInHand(runner, fear, curse, foe);
		scn.MoveMinionsToTable(warrior, chieftain, slayer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, warrior);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(aragorn));
		scn.ShadowUseCardAction(warrior);
		scn.ShadowChooseCard(curse);
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");
		assertEquals(6, scn.GetStrength(aragorn));

		// Two more named, but only one is left under the limit of -3.
		scn.FreepsPass();
		scn.ShadowUseCardAction(warrior);
		scn.ShadowChooseCard(fear);
		scn.ShadowChoose("2");
		assertEquals(5, scn.GetStrength(aragorn));

		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();
		// The Warrior's 9 against Aragorn's 5: a wound, not a kill; Enheartened Foe makes the winner fierce.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowPlayAvailable(foe));
		scn.ShadowPlayCard(foe);
		assertTrue(scn.HasKeyword(warrior, Keyword.FIERCE));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, warrior);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.InFierceRound());
		// The penalty lasted its own skirmish only.
		assertEquals(8, scn.GetStrength(aragorn));
		scn.FreepsPass();

		// A new skirmish phase: the limit starts again.
		assertTrue(scn.ShadowActionAvailable(warrior));
		scn.ShadowUseCardAction(warrior);
		assertInDiscard(runner);
		scn.ShadowChoose("2");
		assertEquals(6, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnAllySkirmishingHimIsNoCompanionAndIsNotMadeWeaker() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetShadowCard("warrior");
		var chieftain = scn.GetShadowCard("chieftain");
		var curse = scn.GetShadowCard("curse");
		var thrarin = scn.GetFreepsCard("thrarin");
		var frodo = scn.GetRingBearer();
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(curse);

		scn.StartGame();
		assertInHand(curse);
		scn.MoveMinionsToTable(warrior, chieftain);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, warrior);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPass();

		assertEquals(4, scn.GetStrength(thrarin));
		scn.ShadowUseCardAction(warrior);
		assertInDiscard(curse);
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("1");
		assertEquals(4, scn.GetStrength(thrarin));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
