package com.gempukku.lotro.cards.official.set14;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_14_014_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("healer", "14_14");
					// Troop of Uruk-hai: an [isengard] Uruk-hai, vitality 4; a character skirmishing it gains nothing from weapons.
					put("troop", "1_143");
					// Brawling Uruk: an [uruk-hai] Uruk-hai, vitality 3; a response to a companion's special ability, never used here.
					put("brawling", "11_179");
					// Saruman, Master of the White Hand: an [uruk-hai] minion who is no Uruk-hai (a Wizard), vitality 4;
					// each companion is resistance -1, and a skirmish ability never used here.
					put("saruman", "17_116");
					// Aragorn, Ranger of the North: a [gondor] Man companion; a maneuver ability never used here.
					put("aragorn", "1_89");
					// Gimli, Son of Gloin: a [dwarven] Dwarf companion; a skirmish ability never used here.
					put("gimli", "1_13");
					// Bounder: a [shire] Hobbit ally, no companion; a skirmish ability never used here.
					put("bounder", "1_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukhaiHealerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 14
		 * Name: Uruk-hai Healer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>At the start of each assignment phase, you may heal an Uruk-hai for each companion you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("healer");

		assertEquals("Uruk-hai Healer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtTheStartOfTheAssignmentPhaseHealsAnUrukHaiOnceForEachCompanionYouCanSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var healer = scn.GetShadowCard("healer");
		var troop = scn.GetShadowCard("troop");
		var brawling = scn.GetShadowCard("brawling");
		var saruman = scn.GetShadowCard("saruman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.MoveMinionsToTable(healer, troop, brawling, saruman);
		scn.AddWoundsToChar(healer, 1);
		scn.AddWoundsToChar(troop, 2);
		scn.AddWoundsToChar(brawling, 1);
		scn.AddWoundsToChar(saruman, 1);
		assertEquals(1, scn.GetWoundsOn(healer));
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(brawling));
		assertEquals(1, scn.GetWoundsOn(saruman));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(healer));
		scn.ShadowAcceptOptionalTrigger();

		// Three companions (Frodo, Aragorn, Gimli): three heals.  The ally is no companion and the minions are not
		// counted.  Each heal is a choice of a wounded Uruk-hai, of either culture, the Healer himself among them;
		// Saruman is an [uruk-hai] minion but no Uruk-hai.
		assertTrue(scn.ShadowHasCardChoiceAvailable(healer, troop, brawling));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(saruman));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troop);
		// "For each" may heal one Uruk-hai more than once (CR 4.2 "for each").
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop));
		scn.ShadowChooseCard(troop);
		scn.ShadowChooseCard(brawling);

		assertEquals(1, scn.GetWoundsOn(healer));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(brawling));
		assertEquals(1, scn.GetWoundsOn(saruman));
		// Had the ally been counted, a fourth heal would be asked for the Healer.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithFrodoTheOnlyCompanionToSpotItHealsOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var healer = scn.GetShadowCard("healer");
		var troop = scn.GetShadowCard("troop");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.MoveMinionsToTable(healer, troop);
		scn.AddWoundsToChar(troop, 2);
		assertEquals(2, scn.GetWoundsOn(troop));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(healer));
		scn.ShadowAcceptOptionalTrigger();

		// One companion, one heal: the Troop is the only wounded Uruk-hai.  The ally beside Frodo would make a
		// second heal asked for, and the Troop would read 0.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop));
		scn.ShadowChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(healer));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void DecliningTheOfferLeavesEveryWoundWhereItWas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var healer = scn.GetShadowCard("healer");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(healer, troop);
		scn.AddWoundsToChar(healer, 1);
		scn.AddWoundsToChar(troop, 2);
		assertEquals(1, scn.GetWoundsOn(healer));
		assertEquals(2, scn.GetWoundsOn(troop));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(healer));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(healer));
		assertEquals(2, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void IsNotOfferedAtTheStartOfTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var healer = scn.GetShadowCard("healer");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(healer, troop);
		scn.AddWoundsToChar(troop, 2);
		assertEquals(2, scn.GetWoundsOn(troop));

		scn.SkipToPhase(Phase.ARCHERY);
		// The phase before the assignment phase, a wounded Uruk-hai on the table: nothing is offered.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(healer));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());

		scn.SkipToPhase(Phase.ASSIGNMENT);
		// The control: at the start of the assignment phase it is.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(healer));
		assertEquals(2, scn.GetWoundsOn(troop));
	}

	@Test
	public void IsNotOfferedWhenTheOnlyWoundedUrukhaiMinionIsNoUrukhai() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var healer = scn.GetShadowCard("healer");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		scn.MoveMinionsToTable(healer, saruman);
		scn.AddWoundsToChar(saruman, 1);
		assertEquals(1, scn.GetWoundsOn(saruman));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		// Nothing it could heal: an [uruk-hai] Wizard is no Uruk-hai, and the Healer is unwounded (Q-0224).
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(healer));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(1, scn.GetWoundsOn(saruman));
	}

	@Test
	public void IsOfferedAgainAtTheSecondAssignmentPhaseOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var healer = scn.GetShadowCard("healer");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(healer);
		scn.AddWoundsToChar(healer, 1);
		assertEquals(1, scn.GetWoundsOn(healer));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(healer));
		scn.ShadowAcceptOptionalTrigger();
		// Two heals for Frodo and Aragorn; the Healer, his one wound gone, leaves nothing for the second.
		scn.ShadowChooseCard(healer);
		assertEquals(0, scn.GetWoundsOn(healer));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, healer);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		// Minions stay in play when the fellowship moves on in the same turn.
		assertInZone(Zone.SHADOW_CHARACTERS, healer);
		assertEquals(0, scn.GetWoundsOn(healer));
		scn.AddWoundsToChar(healer, 1);
		assertEquals(1, scn.GetWoundsOn(healer));
		scn.ShadowRefreshDecision();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		// The second assignment phase of the turn: offered again.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(healer));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(healer);
		assertEquals(0, scn.GetWoundsOn(healer));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
