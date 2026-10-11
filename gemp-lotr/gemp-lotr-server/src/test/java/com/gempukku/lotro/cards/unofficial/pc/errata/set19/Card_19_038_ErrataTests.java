package com.gempukku.lotro.cards.unofficial.pc.errata.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_038_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nertea", "69_38");
					// Ulaire Nelya, Ringwraith in Twilight: another Nazgul, cost 5, strength 10. Her site number
					// is 2, so she is not roaming at site 2; nothing else below 2 burdens.
					put("nelya", "2_84");

					// Hobbits of two cultures: Pippin, Trollslayer is [gondor] and unbound (his skirmish ability
					// is used only on purpose); Sam is [shire] and Ring-bound. Frodo, the Ring-bearer, is the third.
					put("pippin", "103_31");
					put("sam", "1_311");
					// Smeagol, Once Like Us: a [shire] companion of no race. His assignment ability needs a
					// fellowship of [shire] companions only.
					put("smeagol", "103_116");
					// Bounder: a Hobbit ally whose home is site 2, so he may be assigned there
					put("bounder", "1_286");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man, strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf, strength 6, vitality 3

					// Eowyn, Northwoman: "Assignment: Exert Eowyn and assign a minion to the Ring-bearer to make
					// that minion lose all game text keywords ... until the regroup phase."
					put("eowyn", "17_96");
					// Slow-kindled Courage: bearer must be Merry or Pippin. Each minion gains "Assignment: Assign
					// this minion to bearer of Slow-kindled Courage."
					put("courage", "7_328");
					// Shadowy Mount: bearer must be a Nazgul. "Assignment: Make the Free Peoples player assign
					// bearer to an unbound companion bearing a [wraith] condition."
					put("mount", "12_171");
					// Blade Tip: a [wraith] condition a companion bears; it does nothing before the next
					// fellowship phase
					put("tip", "1_209");
					// Spider Poison: "Each time bearer is assigned to a skirmish, bearer must exert", a required
					// trigger of another card on the same assignment
					put("poison", "8_28");
					// Melilot Brandybuck, Merry Dancer: a Hobbit ally, vitality 2, home 1F (so not assigned at
					// site 2). "Response: If a burden is about to be added by a Shadow card, exert Melilot
					// Brandybuck to prevent that burden."
					put("melilot", "3_110");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NerteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Ulaire Nertea, Dark Horseman
		 * Unique: true
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: <b>Fierce.</b>
		 * 	When you play Úlairë Nertëa, name a race. The Free Peoples player must add a burden to assign
		 * 	Úlairë Nertëa to skirmish a companion of the named race.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nertea");

		assertEquals("Ulaire Nertea", card.getBlueprint().getTitle());
		assertEquals("Dark Horseman", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedFromHandForFourPlusRoamingAndTheShadowPlayerNamesARace() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToHand(nertea, nelya);

		scn.StartGame();
		scn.SetTwilight(8);
		scn.SkipToPhase(Phase.SHADOW);

		// 8 set + 1 companion + 2 for site 2
		assertEquals(11, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(nertea));

		scn.ShadowPlayCard(nertea);

		// 4 to play + 2 roaming: her site number is 3 and the fellowship is at site 2
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, nertea);
		assertTrue(scn.ShadowDecisionAvailable("Choose a race"));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowChoiceAvailable("Hobbit"));
		assertTrue(scn.ShadowChoiceAvailable("Man"));
		assertTrue(scn.ShadowChoiceAvailable("Elf"));
		assertTrue(scn.ShadowChoiceAvailable("Dwarf"));
		assertTrue(scn.ShadowChoiceAvailable("Wizard"));

		scn.ShadowChoose("Hobbit");
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another Nazgul played beside her names nothing
		scn.ShadowPlayCard(nelya);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, nelya);
		assertFalse(scn.ShadowDecisionAvailable("Choose a race"));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AssigningHerToACompanionOfTheNamedRaceAddsABurdenWithoutAsking() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var nelya = scn.GetShadowCard("nelya");
		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(pippin, sam, aragorn);
		scn.MoveCardsToHand(nertea);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);
		// The race is named as she is played, so she is played
		scn.ShadowPlayCard(nertea);
		scn.ShadowChoose("Hobbit");
		scn.MoveMinionsToTable(nelya);
		scn.SkipToAssignments();

		// Nothing is asked ahead of the assigning, and nothing is barred
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(nertea));
		assertTrue(scn.FreepsCanAssignAgainst(pippin, nertea));
		assertTrue(scn.FreepsCanAssignAgainst(sam, nertea));
		assertTrue(scn.FreepsCanAssignAgainst(frodo, nertea));
		assertTrue(scn.FreepsCanAssignAgainst(aragorn, nertea));

		// Sam, a Ring-bound Hobbit, to her; the other Hobbit to the other Nazgul, which costs nothing
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, nertea}, new PhysicalCardImpl[]{pippin, nelya});

		assertTrue(scn.IsCharAssignedAgainst(sam, nertea));
		assertTrue(scn.IsCharAssignedAgainst(pippin, nelya));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void AssigningHerToAnAllyOfTheNamedRaceOrToACompanionOfNoRaceAddsNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var smeagol = scn.GetFreepsCard("smeagol");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(nertea);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);
		scn.ShadowPlayCard(nertea);
		scn.ShadowChoose("Hobbit");
		scn.SkipToAssignments();

		// Bounder is a Hobbit and an ally, at home at site 2
		assertEquals(0, scn.GetBurdens());
		scn.FreepsAssignToMinions(bounder, nertea);

		assertTrue(scn.IsCharAssignedAgainst(bounder, nertea));
		assertEquals(0, scn.GetBurdens());

		// His strength of 2 against her 9: he is killed; she is fierce
		scn.FreepsResolveSkirmish(bounder);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();

		// Smeagol is a [shire] companion and no Hobbit
		scn.FreepsAssignToMinions(smeagol, nertea);

		assertTrue(scn.IsCharAssignedAgainst(smeagol, nertea));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void WithManNamedADwarfCostsNothingAndAManCostsABurdenInTheFierceAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(nertea);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);
		scn.ShadowPlayCard(nertea);
		scn.ShadowChoose("Man");
		scn.SkipToAssignments();

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));

		// Gimli, strength 6, loses to her 9 and lives; she is fierce
		scn.FreepsAssignToMinions(gimli, nertea);

		assertTrue(scn.IsCharAssignedAgainst(gimli, nertea));
		assertEquals(0, scn.GetBurdens());

		scn.FreepsResolveSkirmish(gimli);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));

		scn.FreepsAssignToMinions(aragorn, nertea);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, nertea));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void ABurdenIsAddedEachTimeSheIsAssignedSoTheFierceAssignmentAddsASecond() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(nertea);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);
		scn.ShadowPlayCard(nertea);
		scn.ShadowChoose("Man");
		scn.SkipToAssignments();

		assertEquals(0, scn.GetBurdens());
		scn.FreepsAssignToMinions(aragorn, nertea);
		assertEquals(1, scn.GetBurdens());

		// Aragorn, strength 8, loses to her 9 and lives
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();

		// The burden of the first assignment does not pay for this one
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.FreepsCanAssignAgainst(aragorn, nertea));
		assertTrue(scn.FreepsCanAssignAgainst(gimli, nertea));

		scn.FreepsAssignToMinions(aragorn, nertea);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, nertea));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void TheShadowPlayerAssignsHerToACompanionOfTheNamedRaceForNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var nelya = scn.GetShadowCard("nelya");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(pippin, aragorn);
		scn.MoveCardsToHand(nertea);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);
		scn.ShadowPlayCard(nertea);
		scn.ShadowChoose("Hobbit");
		scn.MoveMinionsToTable(nelya);
		scn.SkipToAssignments();

		// The Free Peoples player leaves her unassigned
		scn.FreepsAssignToMinions(aragorn, nelya);

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowCanAssignAgainst(pippin, nertea));

		scn.ShadowAssignToMinions(pippin, nertea);

		assertTrue(scn.IsCharAssignedAgainst(pippin, nertea));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void TheShadowPlayerAssignsHerByAnAssignmentActionForNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var courage = scn.GetFreepsCard("courage");
		scn.MoveCompanionsToTable(pippin, aragorn);
		scn.AttachCardsTo(pippin, courage);
		scn.MoveCardsToHand(nertea);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);
		scn.ShadowPlayCard(nertea);
		scn.ShadowChoose("Hobbit");
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// Slow-kindled Courage gives her "Assignment: Assign this minion to bearer", which is the Shadow player's
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.IsCharAssigned(nertea));
		assertTrue(scn.ShadowActionAvailable(nertea));

		scn.ShadowUseCardAction(nertea);

		assertTrue(scn.IsCharAssignedAgainst(pippin, nertea));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WhenNoBurdenCanBeAddedTheFreePeoplesPlayerCannotAssignHerToTheNamedRace() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var nelya = scn.GetShadowCard("nelya");
		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(pippin, aragorn);
		scn.MoveCardsToHand(nertea);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);
		scn.ShadowPlayCard(nertea);
		scn.ShadowChoose("Hobbit");
		scn.MoveMinionsToTable(nelya);
		// A hindered Ring-bearer takes no burden
		scn.HinderCard(frodo);
		scn.SkipToAssignments();

		assertTrue(scn.IsHindered(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertFalse(scn.FreepsCanAssignAgainst(pippin, nertea));
		assertTrue(scn.FreepsCanAssignAgainst(aragorn, nertea));
		assertTrue(scn.FreepsCanAssignAgainst(pippin, nelya));
		// The Shadow player owes nothing, and may still assign her to the Hobbit
		assertTrue(scn.ShadowCanAssignAgainst(pippin, nertea));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, nertea}, new PhysicalCardImpl[]{pippin, nelya});

		assertTrue(scn.IsCharAssignedAgainst(aragorn, nertea));
		assertTrue(scn.IsCharAssignedAgainst(pippin, nelya));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void WhenNoBurdenCanBeAddedAShadowCardCannotMakeTheFreePeoplesPlayerAssignHerToTheNamedRace() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var mount = scn.GetShadowCard("mount");
		var tip = scn.GetShadowCard("tip");
		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(pippin, aragorn);
		scn.MoveCardsToHand(nertea);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);
		scn.ShadowPlayCard(nertea);
		scn.ShadowChoose("Hobbit");
		// Pippin is the one unbound companion bearing a [wraith] condition
		scn.AttachCardsTo(nertea, mount);
		scn.AttachCardsTo(pippin, tip);
		scn.HinderCard(frodo);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.IsHindered(frodo));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());

		// Shadowy Mount would have the Free Peoples player assign her to Pippin, and he cannot.  Its ability costs
		// nothing and has nobody to assign her to, so it is not offered (Q-0034; withheld by the engine since
		// Q-0224).  TheFreePeoplesPlayerMadeToAssignHerByAShadowCardAddsTheBurden uses it on this board with
		// the Ring-bearer unhindered.
		assertFalse(scn.ShadowActionAvailable(mount));

		assertFalse(scn.IsCharAssigned(nertea));
		assertFalse(scn.IsCharAssigned(pippin));
		assertEquals(0, scn.GetBurdens());
	}

	@Test
	public void TheNamedRaceIsShownOnTheCardAndNamedWhenTheBurdenIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var poison = scn.GetShadowCard("poison");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToHand(nertea);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);
		scn.ShadowPlayCard(nertea);
		scn.ShadowChoose("Hobbit");

		// What the card shows of itself to either player, for as long as she is in play
		assertEquals("Named race is Hobbit", scn.GetDisplayableInformation(nertea));

		// Spider Poison's "bearer must exert" answers the same assignment.  Her burden is a cost paid before the
		// assignment is made and no trigger, so the two are never put in order: the burden is added, the game
		// log names the race, she is assigned, and then the bearer exerts
		scn.AttachCardsTo(pippin, poison);
		scn.SkipToAssignments();

		assertEquals("Named race is Hobbit", scn.GetDisplayableInformation(nertea));
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(pippin));

		scn.MarkGameLog();
		scn.FreepsAssignToMinions(pippin, nertea);

		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertTrue(scn.IsCharAssignedAgainst(pippin, nertea));
		assertTrue(scn.GameLogSinceMarkSays("named race (Hobbit)"));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void TheFreePeoplesPlayerAssigningHerByHisOwnAssignmentActionAddsTheBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var nelya = scn.GetShadowCard("nelya");
		var frodo = scn.GetRingBearer();
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(eowyn);
		scn.MoveCardsToHand(nertea);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);
		scn.ShadowPlayCard(nertea);
		scn.ShadowChoose("Hobbit");
		scn.MoveMinionsToTable(nelya);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertTrue(scn.FreepsActionAvailable(eowyn));

		// Eowyn's action assigns a minion to the Ring-bearer, a Hobbit
		scn.FreepsUseCardAction(eowyn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(nertea, nelya));
		scn.FreepsChooseCard(nertea);

		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertTrue(scn.IsCharAssignedAgainst(frodo, nertea));
		assertEquals(1, scn.GetBurdens());
		// What Éowyn's action then does to the minion it assigned
		assertFalse(scn.HasKeyword(nertea, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMadeToAssignHerByAShadowCardAddsTheBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var mount = scn.GetShadowCard("mount");
		var tip = scn.GetShadowCard("tip");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(pippin, aragorn);
		scn.MoveCardsToHand(nertea);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);
		scn.ShadowPlayCard(nertea);
		scn.ShadowChoose("Hobbit");
		// Pippin is the one unbound companion bearing a [wraith] condition
		scn.AttachCardsTo(nertea, mount);
		scn.AttachCardsTo(pippin, tip);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(mount));

		scn.ShadowUseCardAction(mount);

		assertTrue(scn.IsCharAssignedAgainst(pippin, nertea));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ABurdenPreventedAtHisOwnAssigningLeavesHerUnassignedAndTheShadowPlayersToAssign() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var sam = scn.GetFreepsCard("sam");
		var melilot = scn.GetFreepsCard("melilot");
		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(melilot);
		scn.MoveCardsToHand(nertea);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);
		scn.ShadowPlayCard(nertea);
		scn.ShadowChoose("Hobbit");
		scn.SkipToAssignments();

		assertEquals(0, scn.GetBurdens());
		scn.MarkGameLog();
		scn.FreepsAssignToMinions(sam, nertea);

		// The burden is about to be added, by a Shadow card, and she is not assigned yet
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(melilot));
		assertFalse(scn.IsCharAssigned(nertea));
		assertEquals(0, scn.GetBurdens());

		scn.FreepsAcceptOptionalTrigger();

		// The cost was not paid, so the assignment is not made
		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(melilot));
		assertFalse(scn.IsCharAssigned(nertea));
		assertFalse(scn.IsCharAssigned(sam));
		assertTrue(scn.GameLogSinceMarkSays("did not pay to assign"));

		// He is not asked to assign again.  Never assigned, she is a leftover minion, and the Shadow player
		// assigns her to the Hobbit for nothing
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(nertea));

		scn.ShadowAssignToMinions(sam, nertea);

		assertTrue(scn.IsCharAssignedAgainst(sam, nertea));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void ABurdenPreventedInHisOwnAssignmentActionAssignsNobodyAndTheActionDoesNothingMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var frodo = scn.GetRingBearer();
		var eowyn = scn.GetFreepsCard("eowyn");
		var melilot = scn.GetFreepsCard("melilot");
		scn.MoveCompanionsToTable(eowyn);
		scn.MoveAlliesToTable(melilot);
		scn.MoveCardsToHand(nertea);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);
		scn.ShadowPlayCard(nertea);
		scn.ShadowChoose("Hobbit");
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.FreepsActionAvailable(eowyn));

		// She is the one minion, so Éowyn's action takes her without a choice, to the Ring-bearer, a Hobbit
		scn.FreepsUseCardAction(eowyn);

		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(melilot));
		assertFalse(scn.IsCharAssigned(nertea));

		scn.FreepsAcceptOptionalTrigger();

		// Éowyn stays exerted; the assignment, part of what her action costs, was not made, so its effect is
		// not carried out: the minion keeps its keywords
		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(melilot));
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertFalse(scn.IsCharAssigned(nertea));
		assertFalse(scn.IsCharAssigned(frodo));
		assertTrue(scn.HasKeyword(nertea, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void ABurdenPreventedWhenAShadowCardHasHimAssignHerAssignsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var mount = scn.GetShadowCard("mount");
		var tip = scn.GetShadowCard("tip");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var melilot = scn.GetFreepsCard("melilot");
		scn.MoveCompanionsToTable(pippin, aragorn);
		scn.MoveAlliesToTable(melilot);
		scn.MoveCardsToHand(nertea);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);
		scn.ShadowPlayCard(nertea);
		scn.ShadowChoose("Hobbit");
		// Pippin is the one unbound companion bearing a [wraith] condition
		scn.AttachCardsTo(nertea, mount);
		scn.AttachCardsTo(pippin, tip);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(mount));
		scn.ShadowUseCardAction(mount);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(melilot));
		assertFalse(scn.IsCharAssigned(nertea));

		scn.FreepsAcceptOptionalTrigger();

		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(melilot));
		assertFalse(scn.IsCharAssigned(nertea));
		assertFalse(scn.IsCharAssigned(pippin));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
