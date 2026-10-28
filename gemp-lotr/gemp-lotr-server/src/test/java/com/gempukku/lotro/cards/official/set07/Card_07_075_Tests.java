package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_075_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("meats", "7_75");
					put("meats2", "7_75");
					put("meats3", "7_75");
					put("gollum", "5_24");          // Gollum, Nasty Treacherous Creature: [gollum] minion, no race, strength 5, vitality 4; a regroup ability never used here
					// Gollum, Half a Wraith Himself: the one Gollum who is not [gollum], strength 5, vitality 4.  His
					// trigger answers a Nazgul being played and his assignment ability wants a threat or a companion of cost 0.
					put("wraithgollum", "103_66");
					put("shelob", "8_26");          // Shelob, Last Child of Ungoliant: a [gollum] minion who is no Gollum; fierce, enduring
					put("runner", "1_178");         // Goblin Runner: [moria] Orc, strength 5, vitality 1; adds (2) when played, which it never is here
					put("fighter", "7_286");        // Mordor Fighter: [sauron] Orc, strength 7; its initiative text wants a threat, and there is none
					put("gimli", "1_12");           // Gimli, Dwarf of Erebor: strength 6, damage +1; a fellowship ability never used here
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: strength 8; a maneuver ability never used here
					put("guard", "1_7");            // Dwarf Guard: strength 4, vitality 2, no other text in play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SweeterMeatsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Sweeter Meats
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If a minion is about to take a wound, spot Gollum to prevent that wound.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("meats");

		assertEquals("Sweeter Meats", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PreventsOneWoundOfAnyMinionAndNoneOfACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var meats = scn.GetShadowCard("meats");
		var meats2 = scn.GetShadowCard("meats2");
		var meats3 = scn.GetShadowCard("meats3");
		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		var fighter = scn.GetShadowCard("fighter");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(gimli, aragorn, guard);
		scn.MoveCardsToHand(meats, meats2, meats3);

		scn.StartGame();
		assertInHand(meats, meats2, meats3);
		scn.MoveMinionsToTable(gollum, runner, fighter);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, gollum}, new PhysicalCardImpl[]{aragorn, runner},
				new PhysicalCardImpl[]{guard, fighter});

		// Gimli (6, damage +1) beats Gollum (5): two wounds, each its own window.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassCurrentPhaseActions();
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(meats));
		scn.ShadowChooseOptionalTrigger(meats);
		assertInDiscard(meats);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(meats2));
		scn.ShadowDeclineOptionalTrigger();
		// "That wound" is the one: the other is taken.
		assertEquals(1, scn.GetWoundsOn(gollum));

		// Aragorn (8) beats Goblin Runner, a [moria] Orc of vitality 1: the wound that would kill it is prevented.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(meats2));
		scn.ShadowChooseOptionalTrigger(meats2);
		assertInDiscard(meats2);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(0, scn.GetWoundsOn(runner));

		// The Fighter (7) beats the Dwarf Guard (4): a companion's wound lands with nothing asked.
		scn.FreepsResolveSkirmish(guard);
		assertEquals(0, scn.GetWoundsOn(guard));
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(guard));
		assertInHand(meats3);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PreventsTheWoundOfOneOfTwoMinionsLosingTheSameSkirmishAndNotTheOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var meats = scn.GetShadowCard("meats");
		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(meats);

		scn.StartGame();
		assertInHand(meats);
		scn.MoveMinionsToTable(gollum, runner);
		// Aragorn, 8 + 4, beats the two together (5 + 5) without doubling them: both lose and are wounded together.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 4));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.ShadowAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(runner));
		scn.PassCurrentPhaseActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(meats));
		scn.ShadowChooseOptionalTrigger(meats);

		// "That wound" is one minion's: either may be chosen, and the other is wounded all the same.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gollum, runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(runner);

		assertInDiscard(meats);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheWraithGollumIsAGollumToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var meats = scn.GetShadowCard("meats");
		var wraithGollum = scn.GetShadowCard("wraithgollum");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(meats);

		scn.StartGame();
		assertInHand(meats);
		scn.MoveMinionsToTable(wraithGollum, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		assertFalse(scn.IsCharAssigned(wraithGollum));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(meats));
		scn.ShadowChooseOptionalTrigger(meats);
		assertInDiscard(meats);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithAGollumMinionWhoIsNoGollum() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var meats = scn.GetShadowCard("meats");
		var shelob = scn.GetShadowCard("shelob");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(meats);

		scn.StartGame();
		assertInHand(meats);
		scn.MoveMinionsToTable(shelob, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		// Shelob is a [gollum] minion and not Gollum: the Runner's wound kills it with nothing asked.
		assertInDiscard(runner);
		assertInHand(meats);
		// Shelob is fierce: the fierce assignment phase comes next.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
