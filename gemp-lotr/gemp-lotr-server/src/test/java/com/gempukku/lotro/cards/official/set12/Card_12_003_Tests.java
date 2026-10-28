package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_003_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("clamour", "12_3");

					put("strike", "1_3");       // Axe Strike: [dwarven] Skirmish event, "Make a Dwarf strength +2 (or +3 if bearing a [dwarven] hand weapon)"
					put("strike2", "1_3");
					put("noble", "1_304");      // Noble Intentions: [shire] Skirmish event, "Exert a companion (except a Hobbit) to make a Hobbit strength +3"

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6, vitality 3; a skirmish ability used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, strength 4, vitality 2; "Maneuver: Exert Thrarin to allow him to participate in ... skirmishes"
					put("legolas", "11_21");    // Legolas, Companion of the Ring: Elf companion, strength 6, no archer; +2 only at a battleground or forest, which this path has none of
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, strength 4; to play, spot a Dwarf
					// Deagol, Fateful Finder: a follower.  "Skirmish: If bearer is not assigned to a skirmish, discard this from
					// play to play an artifact or possession from your draw deck on bearer."
					put("deagol", "13_46");
					put("axe", "1_9");          // Dwarven Axe: [dwarven] possession, hand weapon, on a Dwarf; its trigger answers a skirmish its bearer wins

					put("runner", "1_178");     // Goblin Runner: Orc, strength 5, vitality 1
					put("runner2", "1_178");
					put("raiders", "1_158");    // Uruk-hai Raiding Party: strength 9, vitality 3, damage +1, no other text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AClamourOfManyVoicesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: A Clamour of Many Voices
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time a Dwarf wins a skirmish in which you played a [dwarven] event, you may draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("clamour");

		assertEquals("A Clamour of Many Voices", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ADwarfCompanionAndADwarfAllyEachWinningASkirmishInWhichADwarvenEventWasPlayedMayDraw() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var clamour = scn.GetFreepsCard("clamour");
		var strike = scn.GetFreepsCard("strike");
		var strike2 = scn.GetFreepsCard("strike2");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		var guard = scn.GetFreepsCard("guard");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(clamour);
		scn.MoveCardsToHand(strike, strike2);

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		// The cards to be drawn: the Guard first, then the Axe.
		scn.MoveCardsToTopOfDeck(axe, guard);
		assertSame(guard, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, guard, axe);
		assertInHand(strike, strike2);
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, runner}, new PhysicalCardImpl[]{thrarin, runner2});

		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPlayCard(strike);
		scn.FreepsChooseCard(gimli);
		// 6 + 2 against 5
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(1, scn.GetFreepsHandCount());
		scn.ShadowPass();
		scn.FreepsPass();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(clamour));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetFreepsHandCount());
		assertInHand(guard);

		// The ally, taking part in skirmishes by his own ability: 4 + 2 against 5.
		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPlayCard(strike2);
		scn.FreepsChooseCard(thrarin);
		assertEquals(6, scn.GetStrength(thrarin));
		assertEquals(1, scn.GetFreepsHandCount());
		scn.ShadowPass();
		scn.FreepsPass();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(clamour));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetFreepsHandCount());
		assertInHand(axe);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenANonDwarfWinsTheSkirmishOfTheEventNorWhenTheDwarfWinsTheNextWithNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var clamour = scn.GetFreepsCard("clamour");
		var strike = scn.GetFreepsCard("strike");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(gimli, legolas);
		scn.MoveCardsToSupportArea(clamour);
		scn.MoveCardsToHand(strike);

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		assertInHand(strike);
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{legolas, runner}, new PhysicalCardImpl[]{gimli, runner2});

		// Legolas's skirmish: the Dwarven event is played there, on Gimli, who waits for his own.
		scn.FreepsResolveSkirmish(legolas);
		scn.FreepsPlayCard(strike);
		assertEquals(8, scn.GetStrength(gimli));
		int hand = scn.GetFreepsHandCount();
		scn.ShadowPass();
		scn.FreepsPass();
		// Legolas (6) beats the Runner (5): no Dwarf won it.
		assertInZone(Zone.DISCARD, runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(clamour));
		assertEquals(hand, scn.GetFreepsHandCount());

		// Gimli's skirmish is a phase of its own, with no event in it: he is 6 again, and wins.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(6, scn.GetStrength(gimli));
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner2);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(clamour));
		assertEquals(hand, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheDwarfLosesTheSkirmishInWhichADwarvenEventWasPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var clamour = scn.GetFreepsCard("clamour");
		var strike = scn.GetFreepsCard("strike");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(clamour);
		scn.MoveCardsToHand(strike);

		var raiders = scn.GetShadowCard("raiders");

		scn.StartGame();
		assertInHand(strike);
		scn.MoveMinionsToTable(raiders);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, raiders);
		scn.FreepsPlayCard(strike);
		// 6 + 2 against 9
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(0, scn.GetWoundsOn(gimli));
		int hand = scn.GetFreepsHandCount();
		scn.ShadowPass();
		scn.FreepsPass();

		// He lives through the loss, with two wounds (damage +1).
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(clamour));
		assertEquals(hand, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheEventPlayedIsNotDwarvenOrTheDwarvenCardPlayedIsNoEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var clamour = scn.GetFreepsCard("clamour");
		var noble = scn.GetFreepsCard("noble");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var guard = scn.GetFreepsCard("guard");
		var deagol = scn.GetFreepsCard("deagol");
		var axe = scn.GetFreepsCard("axe");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli, legolas, guard);
		scn.AttachCardsTo(guard, deagol);
		scn.MoveCardsToSupportArea(clamour);
		scn.MoveCardsToHand(noble);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertInHand(noble);
		assertAttachedTo(deagol, guard);
		assertInZone(Zone.DECK, axe);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, runner);

		// A [shire] event, played in Gimli's skirmish: Legolas exerts to make Frodo strength +3.
		assertEquals(4, scn.GetStrength(frodo));
		scn.FreepsPlayCard(noble);
		scn.FreepsChooseCard(legolas);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(7, scn.GetStrength(frodo));
		assertInDiscard(noble);
		scn.ShadowPass();

		// A [dwarven] card that is no event, played in the same skirmish: Deagol plays the Dwarven Axe on the Guard.
		scn.FreepsUseCardAction(deagol);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(axe);
		assertAttachedTo(axe, guard);
		int hand = scn.GetFreepsHandCount();
		scn.ShadowPass();
		scn.FreepsPass();

		// Gimli (6) beats the Runner (5).
		assertInZone(Zone.DISCARD, runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(clamour));
		assertEquals(hand, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OfferedAgainWhenTheSameDwarfWinsASecondSkirmishAndDecliningDrawsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var clamour = scn.GetFreepsCard("clamour");
		var strike = scn.GetFreepsCard("strike");
		var strike2 = scn.GetFreepsCard("strike2");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(clamour);
		scn.MoveCardsToHand(strike, strike2);

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(axe, guard);
		assertSame(guard, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, guard, axe);
		assertInHand(strike, strike2);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, runner);
		scn.FreepsPlayCard(strike);
		assertEquals(8, scn.GetStrength(gimli));
		scn.ShadowPass();
		scn.FreepsPass();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(clamour));
		assertEquals(1, scn.GetFreepsHandCount());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetFreepsHandCount());
		assertInHand(guard);

		// A second move, and a second skirmish for Gimli in the same turn.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.MoveMinionsToTable(runner2);
		scn.ShadowRefreshDecision();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, runner2);
		scn.FreepsPlayCard(strike2);
		assertEquals(8, scn.GetStrength(gimli));
		scn.ShadowPass();
		scn.FreepsPass();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(clamour));
		int hand = scn.GetFreepsHandCount();
		int deck = scn.GetFreepsDeckCount();
		assertSame(axe, scn.GetFreepsTopOfDeck());
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(hand, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, axe);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
