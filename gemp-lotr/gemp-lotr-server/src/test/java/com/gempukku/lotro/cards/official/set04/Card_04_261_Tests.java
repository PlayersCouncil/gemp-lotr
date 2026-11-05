package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_261_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wrath", "4_261");
					put("wrath2", "4_261");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man ranger, vitality 4; a maneuver ability used only on purpose
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion who is no ranger; Knight is his whole text
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: [shire] ally; a fellowship ability only
					put("trails", "1_104");     // Eregion's Trails: Maneuver event, "Exert a ranger to ..." (spots or exerts a ranger)
					put("trails2", "1_104");
					put("valor", "1_103");      // Elendil's Valor: Maneuver event that neither spots nor exerts a ranger
					put("explorer", "4_250");   // Southron Explorer: [raider] Man, strength 5, vitality 3; a regroup ability only
					put("soldier", "4_220");    // Desert Soldier: [raider] Man, strength 9; a trigger only at the start of his own skirmish
					put("axeman", "4_224");     // Easterling Axeman: [raider] Man, strength 6; strength +3 and fierce only at 2 burdens
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					// Bladetusk Rearguard: [raider] artifact; "Maneuver: Remove (5) to make this artifact a fierce mounted
					// Southron minion until the end of the turn": the printed [raider] minion who is no Man.
					put("rearguard", "103_43");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WrathofHaradStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Wrath of Harad
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * While you can spot a [raider] Man, each Free Peoples event that spots or exerts a ranger gains this cost: "wound a companion."
		 * Skirmish: Discard this condition to make a [raider] Man strength +2.
		*/

		var scn = GetScenario();

		var wrath = scn.GetFreepsCard("wrath");

		assertEquals("Wrath of Harad", wrath.getBlueprint().getTitle());
		assertNull(wrath.getBlueprint().getSubtitle());
		assertFalse(wrath.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, wrath.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, wrath.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, wrath.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(wrath, Keyword.SUPPORT_AREA));
		assertEquals(1, wrath.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithARaiderManSpottedAnEventThatExertsARangerAlsoCostsAWoundOnACompanionAndAnotherEventDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var bilbo = scn.GetFreepsCard("bilbo");
		var frodo = scn.GetRingBearer();
		var trails = scn.GetFreepsCard("trails");
		var valor = scn.GetFreepsCard("valor");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCardsToHand(trails, valor);

		var wrath = scn.GetShadowCard("wrath");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(wrath);
		assertInZone(Zone.SUPPORT, wrath);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// An event that neither spots nor exerts a ranger gains no cost: its own target is the only thing asked.
		scn.FreepsPlayCard(valor);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, knight));
		scn.FreepsChooseCard(knight);

		assertInDiscard(valor);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.ShadowPass();

		// Eregion's Trails exerts a ranger: with the Desert Soldier, a [raider] Man, spotted, it also costs a wound
		// on a companion, any companion, chosen by the Free Peoples player who pays it; an ally is no companion.
		assertTrue(scn.FreepsPlayAvailable(trails));
		scn.FreepsPlayCard(trails);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, knight, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, soldier));
		scn.FreepsChooseCard(knight);

		assertEquals(1, scn.GetWoundsOn(knight));
		// Aragorn, the one ranger, paid the event's own exertion.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertInDiscard(trails);
		assertInZone(Zone.SUPPORT, wrath);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoRaiderManToSpotTheEventCostsNoWoundUntilARaiderManIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var frodo = scn.GetRingBearer();
		var trails = scn.GetFreepsCard("trails");
		var trails2 = scn.GetFreepsCard("trails2");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.MoveCardsToHand(trails, trails2);

		var wrath = scn.GetShadowCard("wrath");
		var brigand = scn.GetShadowCard("brigand");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToSupportArea(wrath);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The only [raider] card is Wrath of Harad itself, which is no Man, and the Brigand is a Man of another
		// culture: the event costs its own exertion and nothing more.
		scn.FreepsPlayCard(trails);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInDiscard(trails);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.ShadowPass();

		// The Easterling Axeman, a [raider] Man, comes in: the next such event costs a wound as well.
		scn.MoveMinionsToTable(axeman);
		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(trails2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, knight, frodo));
		scn.FreepsChooseCard(knight);

		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInDiscard(trails2);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsThisConditionToMakeARaiderManStrengthPlus2ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, knight);

		var wrath = scn.GetShadowCard("wrath");
		var wrath2 = scn.GetShadowCard("wrath2");
		var rearguard = scn.GetShadowCard("rearguard");
		var explorer = scn.GetShadowCard("explorer");
		var soldier = scn.GetShadowCard("soldier");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToSupportArea(wrath, wrath2, rearguard);
		assertInZone(Zone.SUPPORT, wrath, wrath2, rearguard);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, soldier, brigand);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);

		// The Rearguard becomes a [raider] minion who is no Man.
		scn.FreepsPass();
		scn.ShadowUseCardAction(rearguard);
		assertTrue(scn.IsType(rearguard, CardType.MINION));
		assertFalse(scn.IsRace(rearguard, Race.MAN));

		// The phase next door: the Shadow player's assignment actions.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(wrath));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, explorer}, new PhysicalCardImpl[]{knight, brigand});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(explorer));
		assertEquals(9, scn.GetStrength(soldier));
		assertInZone(Zone.SUPPORT, wrath, wrath2);
		assertTrue(scn.ShadowActionAvailable(wrath));
		scn.ShadowUseCardAction(wrath);

		// A [raider] Man, in this skirmish or not; not a Man of another culture, nor a [raider] minion who is no Man.
		assertTrue(scn.ShadowHasCardChoiceAvailable(explorer, soldier));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(brigand, rearguard));
		scn.ShadowChooseCard(explorer);

		assertEquals(7, scn.GetStrength(explorer));    // 5 + 2
		assertEquals(9, scn.GetStrength(soldier));
		assertInDiscard(wrath);
		// The copy used went, and the other stayed.
		assertInZone(Zone.SUPPORT, wrath2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn (8) beats the Explorer (7), who takes a wound and lives; in the next skirmish the bonus is gone.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(explorer));
		scn.FreepsResolveSkirmish(knight);
		assertEquals(5, scn.GetStrength(explorer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
