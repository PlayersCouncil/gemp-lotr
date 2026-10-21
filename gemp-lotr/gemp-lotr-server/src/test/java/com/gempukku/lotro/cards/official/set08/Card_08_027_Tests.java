package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_027_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("smeagol", "8_27");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: a companion; a skirmish ability used only on purpose
					put("assassin", "1_262");   // Orc Assassin: [sauron] Orc, vitality 2
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3; damage +1 and nothing else
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, vitality 3
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: [gollum] minion; a regroup ability used only on purpose
					put("shelob", "10_23");     // Shelob, Her Ladyship: [gollum] Spider minion; her text answers only her own play
					put("oldvillain", "6_40");  // Gollum, Old Villain: [gollum] minion; a skirmish ability used only on purpose
					put("poorcreature", "6_45"); // Sméagol, Poor Creature: a [gollum] companion
					put("runner", "1_178");     // Goblin Runner: [moria] Orc; its (2) answers only its own play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SmeagolStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Sméagol, Slippery Sneak
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Companion
		 * Subtype: 
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Frodo
		 * Game Text: Ring-bound. To play, add a burden.
		 * 	Skirmish: Add threats equal to the total vitality of the minions Sméagol is skirmishing to discard Sméagol.
		 * 	Regroup: Discard Sméagol to take a [gollum] minion from your discard pile into hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("smeagol");

		assertEquals("Sméagol", card.getBlueprint().getTitle());
		assertEquals("Slippery Sneak", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet());
	}

	@Test
	public void PlayingHimAddsABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCardsToHand(smeagol);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsPlayAvailable(smeagol));
		scn.FreepsPlayCard(smeagol);

		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityAddsThreatsForTheVitalityLeftOfTheMinionsHeIsSkirmishingToDiscardHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		// Four companions: room for four threats.
		scn.MoveCompanionsToTable(smeagol, aragorn, boromir);

		var assassin = scn.GetShadowCard("assassin");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, savage, troop);
		scn.AddWoundsToChar(savage, 1);
		assertEquals(1, scn.GetWoundsOn(savage));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{smeagol, assassin}, new PhysicalCardImpl[]{aragorn, troop});
		scn.ShadowAssignToMinions(smeagol, savage);
		scn.FreepsResolveSkirmish(smeagol);

		assertEquals(0, scn.GetThreats());
		assertEquals(4, scn.GetThreatLimit());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(smeagol));
		scn.FreepsUseCardAction(smeagol);

		// 2 for the Assassin and 2 for the wounded Savage (vitality 3, one wound); the Troop, waiting for
		// Aragorn's skirmish, is not counted.
		assertEquals(4, scn.GetThreats());
		assertInZone(Zone.DISCARD, smeagol);
		assertInZone(Zone.SHADOW_CHARACTERS, assassin, savage, troop);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedUntilEveryThreatCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(smeagol, aragorn, boromir);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, savage);
		scn.FreepsResolveSkirmish(smeagol);

		// Four companions and two threats: room for two, and the Savage's vitality is 3.
		assertEquals(4, scn.GetThreatLimit());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(smeagol));

		scn.RemoveThreats(1);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.FreepsActionAvailable(smeagol));
		scn.FreepsUseCardAction(smeagol);

		// 1 + 3
		assertEquals(4, scn.GetThreats());
		assertInZone(Zone.DISCARD, smeagol);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SkirmishAbilityCannotBeUsedDuringFrodosSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(smeagol, aragorn, boromir);

		var assassin = scn.GetShadowCard("assassin");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, assassin}, new PhysicalCardImpl[]{smeagol, savage});
		// CR 4.2 "skirmish phase(s)": Frodo's skirmish is resolved first, and Sméagol cannot use his ability in it.
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, assassin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(smeagol));

		scn.PassSkirmishActions();
		// Frodo loses to the Assassin; The Ruling Ring's offer is declined.
		scn.FreepsDeclineOptionalTrigger();
		scn.FreepsResolveSkirmish(smeagol);

		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(smeagol));
	}

	@Test
	public void RegroupAbilityDiscardsHimToTakeAGollumMinionFromYourDiscardPileIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var gollum = scn.GetFreepsCard("gollum");
		var shelob = scn.GetFreepsCard("shelob");
		var poorcreature = scn.GetFreepsCard("poorcreature");
		var runner = scn.GetFreepsCard("runner");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToDiscard(gollum, shelob, poorcreature, runner);

		// A [gollum] minion in the other player's discard pile, of a title the Free Peoples player's piles do not hold.
		var oldvillain = scn.GetShadowCard("oldvillain");
		scn.MoveCardsToDiscard(oldvillain);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertInZone(Zone.DISCARD, gollum, shelob, poorcreature, runner, oldvillain);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(smeagol));
		scn.FreepsUseCardAction(smeagol);

		assertInZone(Zone.DISCARD, smeagol);
		assertEquals(1, scn.FreepsGetChoiceMin());
		// [gollum] minions of two titles; a [gollum] companion, a minion of another culture, and the other
		// player's [gollum] minion are not offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gollum, shelob));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(poorcreature, runner, oldvillain, smeagol));
		scn.FreepsChooseCardBPFromSelection(shelob);

		assertInZone(Zone.HAND, shelob);
		assertInZone(Zone.DISCARD, gollum, poorcreature, runner, oldvillain, smeagol);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityWithNoGollumMinionInTheDiscardPileDiscardsHimForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var poorcreature = scn.GetFreepsCard("poorcreature");
		var runner = scn.GetFreepsCard("runner");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToDiscard(poorcreature, runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertInZone(Zone.DISCARD, poorcreature, runner);
		int hand = scn.GetFreepsHandCount();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(smeagol));
		scn.FreepsUseCardAction(smeagol);

		assertInZone(Zone.DISCARD, smeagol, poorcreature, runner);
		assertEquals(hand, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var gollum = scn.GetFreepsCard("gollum");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToDiscard(gollum);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// A minion in play, so that there is a maneuver phase.
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.DISCARD, gollum);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(smeagol));
	}
}
