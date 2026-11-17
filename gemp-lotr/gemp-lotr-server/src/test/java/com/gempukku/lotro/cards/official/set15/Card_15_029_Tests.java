package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_029_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "15_29");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion who is no Hobbit
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion; to play, spot a [gondor] companion
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally; a Skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetWeHatesThemScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "15_29");
					put("noser", "5_28");       // Smeagol, Old Noser: [gollum] companion, no Hobbit; a regroup ability used only on purpose
					// We Hates Them: on Smeagol; Regroup: discard it to discard a minion and play a companion from the
					// discard pile
					put("wehates", "7_77");
					put("ted", "18_75");        // Ted Sandyman: the Shadow player's Hobbit, a minion; abilities used only on purpose
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, its one text on its own play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSpellScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "15_29");
					// Salve: a spell that is a condition, played on a companion; its response needs a killing wound
					put("salve", "12_32");
					// A New Light: a Fellowship spell; with no minion in the Shadow discard pile it does nothing
					put("newlight", "17_15");
					// Wielder of the Flame: a Maneuver spell, spot Gandalf to make a companion defender +1; the Shadow
					// player may remove (3) to prevent that
					put("wielder", "2_28");
					put("depart", "3_31");      // Depart Silently: a [gandalf] Maneuver event that is no spell (stealth)
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("pursuer", "4_190");    // Uruk Pursuer: [isengard] Uruk-hai, vitality 3; its texts are about unbound Hobbits
					put("runner", "1_178");     // Goblin Runner: [moria] Orc of vitality 1, exhausted as it stands
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSkirmishScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "15_29");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the companion of the other skirmish
					// White Hand Destroyer: fierce [uruk-hai] Uruk-hai, strength 10, vitality 3; hunter only for sites
					// controlled. Gandalf loses to it and lives, and meets it again in the fierce round
					put("destroyer", "17_124");
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1, for Aragorn to beat
					// For the hand and the deck: an ally, a possession, a condition and events
					put("bounder", "1_286");    // Bounder: Hobbit ally
					put("sword", "1_112");      // Ranger's Sword: a possession
					put("salve", "12_32");      // Salve: a condition
					put("depart", "3_31");      // Depart Silently: an event
					put("newlight", "17_15");   // A New Light: an event, to be drawn
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GandalfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Gandalf, Powerful Guide
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Wizard
		 * Strength: 8
		 * Vitality: 4
		 * Resistance: 7
		 * Game Text: To play, spot 2 Hobbits.<br>Each time you play a spell, you may add a burden to wound a minion.<br>At the start of each skirmish involving Gandalf, you may draw a card and then discard a card from your hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gandalf");

		assertEquals("Gandalf", card.getBlueprint().getTitle());
		assertEquals("Powerful Guide", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(7, card.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedUntilASecondHobbitIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(aragorn, smeagol);
		scn.MoveCardsToHand(gandalf, pippin);

		scn.StartGame();

		// Frodo is one Hobbit. Aragorn and Smeagol, a [shire] companion, are none.
		assertFalse(scn.FreepsPlayAvailable(gandalf));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(pippin);
		assertInZone(Zone.FREE_CHARACTERS, pippin);

		// Frodo and Pippin, a [gondor] Hobbit.
		assertTrue(scn.FreepsPlayAvailable(gandalf));
		scn.FreepsPlayCard(gandalf);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AHobbitAllyIsOneOfTheTwoHobbits() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(gandalf);

		scn.StartGame();

		// Frodo and the Bounder.
		assertTrue(scn.FreepsPlayAvailable(gandalf));
		scn.FreepsPlayCard(gandalf);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheShadowPlayersHobbitMinionIsOneOfTheTwoHobbits() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWeHatesThemScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var noser = scn.GetFreepsCard("noser");
		var wehates = scn.GetFreepsCard("wehates");
		var ted = scn.GetShadowCard("ted");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(noser);
		scn.AttachCardsTo(noser, wehates);
		scn.MoveCardsToDiscard(gandalf);

		scn.StartGame();
		assertInZone(Zone.ATTACHED, wehates);
		assertInZone(Zone.DISCARD, gandalf);
		scn.SkipToPhase(Phase.REGROUP);
		// Minions that lived through the skirmishes: Ted Sandyman, a Hobbit, and a Goblin Runner for We Hates Them
		// to discard.
		scn.MoveMinionsToTable(ted, runner);
		scn.FreepsRefreshDecision();

		scn.FreepsUseCardAction(wehates);
		assertTrue(scn.FreepsHasCardChoiceAvailable(ted, runner));
		scn.FreepsChooseCard(runner);

		// Frodo and Ted Sandyman are two Hobbits: Gandalf, the one companion in the discard pile, is played.
		assertInDiscard(runner);
		assertInPlay(ted);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void EachSpellPlayedOffersABurdenForAWoundAndDecliningChangesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSpellScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var salve = scn.GetFreepsCard("salve");
		var newlight = scn.GetFreepsCard("newlight");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(salve, newlight);

		scn.StartGame();
		assertEquals(0, scn.GetBurdens());

		// A spell that is a condition.
		scn.FreepsPlayCard(salve);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gandalf));
		scn.FreepsChooseCard(frodo);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetBurdens());
		assertAttachedTo(salve, frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A spell that is an event: offered again. No minion is in play, and the burden is paid for nothing.
		scn.FreepsPlayCard(newlight);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetBurdens());
		assertInDiscard(newlight);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AManeuverSpellAddsABurdenToWoundAnyMinionAndAnEventThatIsNoSpellIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSpellScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var wielder = scn.GetFreepsCard("wielder");
		var depart = scn.GetFreepsCard("depart");
		var troop = scn.GetShadowCard("troop");
		var pursuer = scn.GetShadowCard("pursuer");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(wielder, depart);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, pursuer, runner);
		scn.SkipToPhase(Phase.MANEUVER);
		// Too little for the Shadow player to remove (3) against Wielder of the Flame.
		scn.SetTwilight(0);
		assertEquals(0, scn.GetBurdens());

		// A [gandalf] event that is no spell.
		scn.FreepsPlayCard(depart);
		assertInDiscard(depart);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(0, scn.GetBurdens());
		scn.ShadowPass();

		scn.FreepsPlayCard(wielder);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetBurdens());

		// A [moria] Orc, an [isengard] Uruk-hai and the Runner, exhausted, are all offered.
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, pursuer, runner));
		// No companion is offered, Gandalf and the Ring-bearer among those on the table.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, scn.GetRingBearer()));
		scn.FreepsChooseCard(runner);
		// A wound, not an exertion: the Runner, with 1 vitality, dies of it.
		assertInDiscard(runner);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(pursuer));

		// Wielder of the Flame's own choice of companion.
		scn.FreepsChooseCard(gandalf);
		assertTrue(scn.HasKeyword(gandalf, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AtTheStartOfEachOfHisSkirmishesHeMayDrawACardAndThenDiscardOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var sword = scn.GetFreepsCard("sword");
		var salve = scn.GetFreepsCard("salve");
		var depart = scn.GetFreepsCard("depart");
		var newlight = scn.GetFreepsCard("newlight");
		var destroyer = scn.GetShadowCard("destroyer");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gandalf, aragorn);

		scn.StartGame();
		scn.SetFreepsHand(bounder, sword, salve, depart);
		scn.SetFreepsDrawDeck(newlight);
		scn.MoveMinionsToTable(destroyer, runner);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDeckCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, destroyer}, new PhysicalCardImpl[]{aragorn, runner});

		// The start of a skirmish that does not involve Gandalf.
		scn.FreepsResolveSkirmish(aragorn);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();
		assertInDiscard(runner);

		scn.FreepsResolveSkirmish(gandalf);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.FreepsAcceptOptionalTrigger();

		// The top card is drawn, and then one card of the hand must be discarded: any of them, the card just drawn
		// among them.
		assertInHand(newlight);
		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(bounder, sword, salve, depart, newlight));
		scn.FreepsChooseCard(sword);
		assertInDiscard(sword);
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// 8 against 10: Gandalf loses, takes a wound, and meets the fierce Destroyer again.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gandalf, destroyer);
		scn.FreepsResolveSkirmish(gandalf);

		// A second skirmish involving him: offered again. Declined, the hand and the deck stay as they are.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(4, scn.GetFreepsHandCount());
		assertInHand(bounder, salve, depart, newlight);
		assertEquals(0, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
