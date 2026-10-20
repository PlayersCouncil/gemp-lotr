package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_065_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gloom", "103_65");
					put("gloom2", "103_65");
					// Every Cover of Darkness is two-dot unique by its title, so only one other can be in play
					// when Omen of Gloom is played; the rest of the twilight conditions are Ominous Skies.
					put("fear", "103_64");      // Cover of Darkness, Omen of Fear: [wraith] twilight condition; an assignment ability that needs 3 [ringwraith] minions
					put("sky1", "103_97");      // Ominous Sky: [sauron] twilight condition, three-dot unique; adds (1) when its owner plays an Orc or Troll; a Shadow or Regroup ability of its own
					put("sky2", "103_97");
					put("sky3", "103_97");
					put("illwind", "103_67");   // Ill Wind: [wraith] condition without the twilight keyword; answers only the second Nazgul played in a phase
					put("nelya", "2_84");       // Ulaire Nelya, Ringwraith in Twilight: twilight [wraith] Nazgul; nothing else below 2 burdens
					put("lights", "6_99");      // Corpse Lights: twilight [sauron] Wraith; a skirmish ability
					put("lemenya", "1_232");    // Ulaire Lemenya: [wraith] Nazgul without the twilight keyword
					put("deadones", "6_100");   // Dead Ones: twilight [sauron] Wraith, strength 5, vitality 1, damage +1; a cost to play, so it is cheated in
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6
					put("elf", "1_53");         // Lorien Elf: an Elf with no text but what it takes to play him
					put("stand", "1_63");       // Stand Against Darkness: Maneuver: Exert an Elf to discard a [sauron] minion, condition or possession
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CoverofDarknessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Cover of Darkness, Omen of Gloom
		 * Unique: 2
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Twilight.
		 * 	To play, hinder 2 twilight conditions.
		 * 	Each time your minion is killed, you may remove (1) and hinder a twilight condition to shuffle that minion into your draw deck.
		 * 	Regroup: Hinder a twilight card to add (1).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gloom");

		assertEquals("Cover of Darkness", card.getBlueprint().getTitle());
		assertEquals("Omen of Gloom", card.getBlueprint().getSubtitle());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TWILIGHT));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedForTwoByHinderingTwoOfTheTwilightConditionsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gloom = scn.GetShadowCard("gloom");
		var gloom2 = scn.GetShadowCard("gloom2");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var sky3 = scn.GetShadowCard("sky3");
		var illwind = scn.GetShadowCard("illwind");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToHand(gloom);
		scn.MoveCardsToSupportArea(gloom2, sky1, sky2, sky3, illwind);
		scn.HinderCard(sky3);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for the site
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(gloom));
		scn.ShadowPlayCard(gloom);

		// Twilight conditions of either culture, another copy of this card among them; not one already
		// hindered, not a condition without the keyword, not a twilight minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gloom2, sky1, sky2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sky3, illwind, nelya));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(gloom2, sky1);

		assertTrue(scn.IsHindered(gloom2));
		assertTrue(scn.IsHindered(sky1));
		assertFalse(scn.IsHindered(sky2));
		assertFalse(scn.IsHindered(illwind));
		assertFalse(scn.IsHindered(nelya));
		assertEquals(11, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, gloom);
		assertFalse(scn.IsHindered(gloom));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilASecondTwilightConditionIsThereToHinder() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gloom = scn.GetShadowCard("gloom");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var sky3 = scn.GetShadowCard("sky3");
		var illwind = scn.GetShadowCard("illwind");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToHand(gloom, sky3);
		scn.MoveCardsToSupportArea(sky1, sky2, illwind);
		scn.HinderCard(sky2);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// One twilight condition that can be hindered; a hindered one, a twilight minion and a condition
		// without the keyword do not make a second.
		assertFalse(scn.ShadowPlayAvailable(gloom));

		scn.ShadowPlayCard(sky3);
		assertTrue(scn.ShadowPlayAvailable(gloom));

		scn.ShadowPlayCard(gloom);
		// Exactly two candidates, so they are hindered without a choice.
		assertTrue(scn.IsHindered(sky1));
		assertTrue(scn.IsHindered(sky3));
		assertFalse(scn.IsHindered(illwind));
		assertFalse(scn.IsHindered(nelya));
		assertInZone(Zone.SUPPORT, gloom);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void KilledMinionMayBeShuffledIntoTheDrawDeckByRemovingOneAndHinderingATwilightCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gloom = scn.GetShadowCard("gloom");
		var fear = scn.GetShadowCard("fear");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var illwind = scn.GetShadowCard("illwind");
		var nelya = scn.GetShadowCard("nelya");
		var deadones = scn.GetShadowCard("deadones");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(gloom, fear, sky1, sky2, illwind);
		scn.HinderCard(sky2);
		// A minion that was in the discard pile already is not "that minion".
		scn.MoveCardsToDiscard(deadones);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, nelya);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, runner);
		scn.SetTwilight(5);

		int deck = scn.GetShadowDeckCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		assertInZone(Zone.SHADOW_CHARACTERS, runner);

		// Aragorn 8 against the Runner's 5: one wound, and the Runner has 1 vitality.
		scn.PassSkirmishActions();

		assertInZone(Zone.DISCARD, runner);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gloom));
		scn.ShadowAcceptOptionalTrigger();

		// Any twilight condition, this one included; not one already hindered, not a condition without
		// the keyword, not a twilight minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gloom, fear, sky1));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sky2, illwind, nelya));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(fear);

		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.IsHindered(fear));
		assertFalse(scn.IsHindered(gloom));
		assertFalse(scn.IsHindered(sky1));
		assertFalse(scn.IsHindered(nelya));
		assertInZone(Zone.DECK, runner);
		assertInZone(Zone.DISCARD, deadones);
		assertEquals(deck + 1, scn.GetShadowDeckCount());
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoOtherTwilightConditionItHindersItselfAndTheMinionIsStillShuffledIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gloom = scn.GetShadowCard("gloom");
		var illwind = scn.GetShadowCard("illwind");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(gloom, illwind);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, runner);
		scn.SetTwilight(5);
		scn.PassSkirmishActions();

		assertInZone(Zone.DISCARD, runner);
		assertFalse(scn.IsHindered(gloom));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gloom));
		scn.ShadowAcceptOptionalTrigger();

		// "A twilight condition" is not "another": this card is the only one there is, and is hindered
		// without a choice. A card hindered in the middle of its own trigger still carries it out.
		assertTrue(scn.IsHindered(gloom));
		assertFalse(scn.IsHindered(illwind));
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.DECK, runner);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void KillTriggerCanBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gloom = scn.GetShadowCard("gloom");
		var sky1 = scn.GetShadowCard("sky1");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(gloom, sky1);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, runner);
		scn.SetTwilight(5);
		int shuffles = scn.GetShadowDeckShuffleCount();
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gloom));
		scn.ShadowDeclineOptionalTrigger();

		assertFalse(scn.IsHindered(sky1));
		assertFalse(scn.IsHindered(gloom));
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.DISCARD, runner);
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void KillTriggerIsNotOfferedWithNothingInThePoolToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gloom = scn.GetShadowCard("gloom");
		var sky1 = scn.GetShadowCard("sky1");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(gloom, sky1);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, runner);
		scn.SetTwilight(0);
		scn.PassSkirmishActions();

		assertInZone(Zone.DISCARD, runner);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.IsHindered(sky1));
		assertFalse(scn.IsHindered(gloom));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void MinionDiscardedFromPlayIsNotKilledAndIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gloom = scn.GetShadowCard("gloom");
		var sky1 = scn.GetShadowCard("sky1");
		var deadones = scn.GetShadowCard("deadones");
		var elf = scn.GetFreepsCard("elf");
		var stand = scn.GetFreepsCard("stand");
		scn.MoveCompanionsToTable(elf);
		scn.MoveCardsToHand(stand);
		scn.MoveCardsToSupportArea(gloom, sky1);

		scn.StartGame();
		scn.MoveMinionsToTable(deadones);
		scn.SkipToPhase(Phase.MANEUVER);

		// 2 companions + 2 for the site: enough for the trigger's (1), were it offered.
		assertEquals(4, scn.GetTwilight());
		scn.FreepsPlayCard(stand);
		// Dead Ones and Ominous Sky are both [sauron].
		assertTrue(scn.FreepsHasCardChoiceAvailable(deadones, sky1));
		scn.FreepsChooseCard(deadones);

		assertInZone(Zone.DISCARD, deadones);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		// 4 + 1 for the event
		assertEquals(5, scn.GetTwilight());
		assertFalse(scn.IsHindered(gloom));
		assertFalse(scn.IsHindered(sky1));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void EachMinionKilledIsAnsweredWhateverItsCultureOrRace() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gloom = scn.GetShadowCard("gloom");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var runner = scn.GetShadowCard("runner");
		var deadones = scn.GetShadowCard("deadones");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(gloom, sky1, sky2);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, deadones);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{gimli, deadones});
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(5);
		int shuffles = scn.GetShadowDeckShuffleCount();

		// The [moria] Orc.
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gloom));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(gloom, sky1, sky2));
		scn.ShadowChooseCard(sky1);
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.DECK, runner);
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());

		// The twilight [sauron] Wraith: Gimli 6 against 5.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, deadones);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gloom));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(gloom, sky2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sky1));
		scn.ShadowChooseCard(sky2);
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.DECK, deadones);
		assertInZone(Zone.DECK, runner);
		assertEquals(shuffles + 2, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.IsHindered(sky1));
		assertTrue(scn.IsHindered(sky2));
		assertFalse(scn.IsHindered(gloom));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityHindersAnyTwilightCardToAddOneAndIsNotAShadowAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gloom = scn.GetShadowCard("gloom");
		var gloom2 = scn.GetShadowCard("gloom2");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var illwind = scn.GetShadowCard("illwind");
		var nelya = scn.GetShadowCard("nelya");
		var lights = scn.GetShadowCard("lights");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToSupportArea(gloom, gloom2, sky1, sky2, illwind);
		scn.HinderCard(sky2);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, lights, lemenya);
		scn.FreepsPass();

		// An earlier version of this card had a Shadow ability; this one has only the Regroup ability.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gloom));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(gloom));
		int twilight = scn.GetTwilight();

		scn.ShadowUseCardAction(gloom);
		// Twilight conditions and twilight minions of either culture, this card and its other copy among
		// them; not one already hindered, not a condition or a Nazgul without the keyword.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gloom, gloom2, sky1, nelya, lights));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sky2, illwind, lemenya));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(nelya);

		assertTrue(scn.IsHindered(nelya));
		assertFalse(scn.IsHindered(gloom));
		assertFalse(scn.IsHindered(gloom2));
		assertFalse(scn.IsHindered(sky1));
		assertFalse(scn.IsHindered(lights));
		assertEquals(twilight + 1, scn.GetTwilight());

		// No limit, and the card it hinders may be itself: the (1) is still added.
		scn.FreepsPass();
		scn.ShadowUseCardAction(gloom);
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(nelya));
		scn.ShadowChooseCard(gloom);

		assertTrue(scn.IsHindered(gloom));
		assertFalse(scn.IsHindered(gloom2));
		assertEquals(twilight + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
