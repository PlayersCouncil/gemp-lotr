package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_038_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nertea", "19_38");
					// Ulaire Nelya, Ringwraith in Twilight: another Nazgul, cost 5, strength 10. Her site number
					// is 2, so she is not roaming at site 2; nothing else below 2 burdens.
					put("nelya", "2_84");

					// Pippin, Trollslayer: a Hobbit companion of [gondor] (his skirmish ability is used only on
					// purpose).  Frodo, the Ring-bearer, is the [shire] Hobbit.
					put("pippin", "103_31");
					// Smeagol, Once Like Us: a [shire] companion of no race. His assignment ability needs a
					// fellowship of [shire] companions only.
					put("smeagol", "103_116");
					// Bounder: a Hobbit ally whose home is site 2, so he may be assigned there
					put("bounder", "1_286");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man, strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf, strength 6, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireNerteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Úlairë Nertëa, Dark Horseman
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>.<br>When you play Úlairë Nertëa, name a race. The Free Peoples player cannot assign Úlairë Nertëa to skirmish a companion of the named race.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nertea");

		assertEquals("Úlairë Nertëa", card.getBlueprint().getTitle());
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
	public void PlayedFromHandTheShadowPlayerNamesARaceAndAnotherNazgulPlayedAfterwardsNamesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToHand(nertea, nelya);

		scn.StartGame();
		scn.SetTwilight(15);
		scn.SkipToPhase(Phase.SHADOW);

		scn.ShadowPlayCard(nertea);

		assertInZone(Zone.SHADOW_CHARACTERS, nertea);
		assertTrue(scn.ShadowDecisionAvailable("Choose a race"));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowChoiceAvailable("Hobbit"));
		assertTrue(scn.ShadowChoiceAvailable("Man"));
		assertTrue(scn.ShadowChoiceAvailable("Elf"));
		assertTrue(scn.ShadowChoiceAvailable("Dwarf"));
		assertTrue(scn.ShadowChoiceAvailable("Wizard"));

		scn.ShadowChoose("Hobbit");

		assertEquals("Named race is Hobbit", scn.GetDisplayableInformation(nertea));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another Nazgul played while she is in play names nothing
		scn.ShadowPlayCard(nelya);

		assertInZone(Zone.SHADOW_CHARACTERS, nelya);
		assertFalse(scn.ShadowDecisionAvailable("Choose a race"));
		assertEquals("Named race is Hobbit", scn.GetDisplayableInformation(nertea));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithHobbitNamedTheFreePeoplesPlayerCannotAssignHerToAHobbitCompanion() throws DecisionResultInvalidException, CardNotFoundException {
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
		// The race is named as she is played, so she is played
		scn.ShadowPlayCard(nertea);
		scn.ShadowChoose("Hobbit");
		scn.MoveMinionsToTable(nelya);
		scn.SkipToAssignments();

		// Nothing is asked of him ahead of his assigning: she is barred, not charged for
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(nertea));
		assertFalse(scn.FreepsCanAssignAgainst(frodo, nertea));
		assertFalse(scn.FreepsCanAssignAgainst(pippin, nertea));
		assertTrue(scn.FreepsCanAssignAgainst(aragorn, nertea));
		// The other Nazgul may go to a Hobbit
		assertTrue(scn.FreepsCanAssignAgainst(pippin, nelya));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, nertea}, new PhysicalCardImpl[]{pippin, nelya});

		assertTrue(scn.IsCharAssignedAgainst(aragorn, nertea));
		assertTrue(scn.IsCharAssignedAgainst(pippin, nelya));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void WithHobbitNamedSheMayBeAssignedToAHobbitAllyAndToAShireCompanionOfNoRace() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var frodo = scn.GetRingBearer();
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
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertFalse(scn.FreepsCanAssignAgainst(frodo, nertea));
		assertTrue(scn.FreepsCanAssignAgainst(bounder, nertea));
		scn.FreepsAssignToMinions(bounder, nertea);

		assertTrue(scn.IsCharAssignedAgainst(bounder, nertea));

		// His strength of 2 against her 9: he is killed; she is fierce
		scn.FreepsResolveSkirmish(bounder);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();

		// Smeagol is a [shire] companion and no Hobbit
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertFalse(scn.FreepsCanAssignAgainst(frodo, nertea));
		assertTrue(scn.FreepsCanAssignAgainst(smeagol, nertea));
		scn.FreepsAssignToMinions(smeagol, nertea);

		assertTrue(scn.IsCharAssignedAgainst(smeagol, nertea));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void WithManNamedAManIsRefusedAndADwarfOrAHobbitIsNotInEitherAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var frodo = scn.GetRingBearer();
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

		assertEquals("Named race is Man", scn.GetDisplayableInformation(nertea));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertFalse(scn.FreepsCanAssignAgainst(aragorn, nertea));
		assertTrue(scn.FreepsCanAssignAgainst(frodo, nertea));
		assertTrue(scn.FreepsCanAssignAgainst(gimli, nertea));

		// Gimli, strength 6, loses to her 9 and lives; she is fierce
		scn.FreepsAssignToMinions(gimli, nertea);
		assertTrue(scn.IsCharAssignedAgainst(gimli, nertea));

		scn.FreepsResolveSkirmish(gimli);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();

		// The bar holds in the fierce assignment as well
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertFalse(scn.FreepsCanAssignAgainst(aragorn, nertea));
		assertTrue(scn.FreepsCanAssignAgainst(gimli, nertea));

		scn.FreepsAssignToMinions(gimli, nertea);

		assertTrue(scn.IsCharAssignedAgainst(gimli, nertea));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void TheShadowPlayerMayAssignHerToACompanionOfTheNamedRace() throws DecisionResultInvalidException, CardNotFoundException {
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

		// The Free Peoples player, who may not give her to Pippin, leaves her unassigned
		assertFalse(scn.FreepsCanAssignAgainst(pippin, nertea));
		scn.FreepsAssignToMinions(aragorn, nelya);

		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowCanAssignAgainst(pippin, nertea));

		scn.ShadowAssignToMinions(pippin, nertea);

		assertTrue(scn.IsCharAssignedAgainst(pippin, nertea));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}
}
