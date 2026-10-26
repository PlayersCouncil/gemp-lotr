package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_043_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("field", "10_43");
					// Southron Explorer: a [raider] Southron Man, strength 5, vitality 3, cost 2; a regroup ability only
					put("explorer", "4_250");
					// Desert Lancers: a [raider] Southron Man of another title, strength 11; to play, spot a [raider]
					// Man; a regroup ability only
					put("lancers", "4_217");
					// Bladetusk Rearguard: a [raider] artifact whose maneuver ability (remove (5)) makes it a Southron
					// minion of strength 5 and vitality 5 that is no Man; nothing is stacked on it
					put("rearguard", "103_43");
					// Easterling Blademaster: a [raider] Easterling Man, no Southron, strength 8, vitality 2;
					// "Skirmish: Exert this minion to make him damage +1", not used
					put("blademaster", "7_142");
					// Easterling Attacker: a [raider] Easterling Man, cost 2; a skirmish ability not used
					put("attacker", "7_141");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgul, no Man; his text needs 3 burdens
					put("khamul", "103_61");
					// Dunlending Brigand: a [dunland] Man, a Man who is not [raider]; no text
					put("brigand", "4_10");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man; a maneuver ability not used
					// Pippin, Trollslayer: a [gondor] Hobbit, strength 3; a skirmish ability not used
					put("pippin", "103_31");
					put("gimli", "1_13");       // Gimli, Son of Gloin: a [dwarven] Dwarf, strength 6; a skirmish ability only
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man, strength 6, vitality 3; no text
					// Gondorian Merchant: a [gondor] ally, strength 6; a maneuver ability not used
					put("merchant", "7_97");
					put("guard", "1_7");        // Dwarf Guard: a [dwarven] Dwarf; to play, spot a Dwarf
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FieldoftheFallenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Field of the Fallen
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot a [raider] Man.<br>While there is a character in the dead pile, each companion of the same culture as that character is strength -1.<br><b>Skirmish:</b> Exert your Southron to make him strength +1.
		*/

		var scn = GetScenario();

		var field = scn.GetFreepsCard("field");

		assertEquals("Field of the Fallen", field.getBlueprint().getTitle());
		assertNull(field.getBlueprint().getSubtitle());
		assertFalse(field.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, field.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, field.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, field.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(field, Keyword.SUPPORT_AREA));
		assertEquals(1, field.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilARaiderManIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var field = scn.GetShadowCard("field");
		var explorer = scn.GetShadowCard("explorer");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToHand(field, explorer);

		scn.StartGame();
		scn.MoveMinionsToTable(khamul, brigand);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// A [raider] Nazgul and a [dunland] Man are not enough.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(field));

		scn.ShadowPlayCard(explorer);
		assertTrue(scn.ShadowPlayAvailable(field));

		scn.ShadowPlayCard(field);
		assertInZone(Zone.SUPPORT, field);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedSpottingAnEasterlingMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var field = scn.GetShadowCard("field");
		var attacker = scn.GetShadowCard("attacker");
		scn.MoveCardsToHand(field);

		scn.StartGame();
		scn.MoveMinionsToTable(attacker);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(field));
		scn.ShadowPlayCard(field);
		assertInZone(Zone.SUPPORT, field);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhileAGondorCompanionIsInTheDeadPileEachGondorCompanionIsStrengthMinusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var field = scn.GetShadowCard("field");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var gimli = scn.GetFreepsCard("gimli");
		var merchant = scn.GetFreepsCard("merchant");
		var knight = scn.GetFreepsCard("knight");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, pippin, gimli);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToSupportArea(field);

		scn.StartGame();

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(merchant));
		// 3, and +1 for The Ruling Ring
		assertEquals(4, scn.GetStrength(frodo));

		scn.MoveCardsToDeadPile(knight);
		assertInZone(Zone.DEAD, knight);

		// The [gondor] companions, a Man and a Hobbit; not the [gondor] ally, nor companions of other cultures.
		assertEquals(7, scn.GetStrength(aragorn));
		assertEquals(2, scn.GetStrength(pippin));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(merchant));
		assertEquals(4, scn.GetStrength(frodo));
	}

	@Test
	public void AnAllyAndADwarfInTheDeadPileLowerTheCompanionsOfBothCulturesByOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var field = scn.GetShadowCard("field");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var merchant = scn.GetFreepsCard("merchant");
		var knight = scn.GetFreepsCard("knight");
		var guard = scn.GetFreepsCard("guard");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(field);

		scn.StartGame();

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));

		// A [gondor] ally is a character too.
		scn.MoveCardsToDeadPile(merchant);
		assertInZone(Zone.DEAD, merchant);
		assertEquals(7, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));

		// A [dwarven] character as well: each culture in the dead pile lowers its own companions.
		scn.MoveCardsToDeadPile(guard);
		assertInZone(Zone.DEAD, guard);
		assertEquals(7, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(frodo));

		// A second [gondor] character in the dead pile: still -1 (Q-0351).
		scn.MoveCardsToDeadPile(knight);
		assertInZone(Zone.DEAD, knight);
		assertEquals(7, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(gimli));
	}

	@Test
	public void AGondorCompanionInTheDiscardPileOrInTheShadowPlayersDeadPileLowersNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var field = scn.GetShadowCard("field");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var knight = scn.GetFreepsCard("knight");
		var shadowKnight = scn.GetShadowCard("knight");
		scn.MoveCompanionsToTable(aragorn, pippin);
		scn.MoveCardsToSupportArea(field);
		scn.MoveCardsToDiscard(knight);
		scn.MoveCardsToDeadPile(shadowKnight);

		scn.StartGame();

		// The dead pile is the Free Peoples player's (Q-0131), and the discard pile is no dead pile.
		assertInDiscard(knight);
		assertInZone(Zone.DEAD, shadowKnight);
		assertEquals(0, scn.GetFreepsDeadCount());
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetStrength(pippin));
	}

	@Test
	public void AGondorCompanionKilledInASkirmishLowersTheOtherGondorCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var field = scn.GetShadowCard("field");
		var blademaster = scn.GetShadowCard("blademaster");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.MoveCardsToSupportArea(field);

		scn.StartGame();
		scn.MoveMinionsToTable(blademaster);
		// Vitality 3 with 2 wounds: the wound of a lost skirmish kills him.
		scn.AddWoundsToChar(knight, 2);
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, blademaster);
		scn.FreepsResolveSkirmish(knight);
		assertEquals(8, scn.GetStrength(aragorn));

		// 6 against 8: the Knight loses and is killed.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, knight);
		assertEquals(7, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SkirmishAbilityExertsTheChosenSouthronToMakeHimStrengthPlusOneForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var field = scn.GetShadowCard("field");
		var explorer = scn.GetShadowCard("explorer");
		var lancers = scn.GetShadowCard("lancers");
		var rearguard = scn.GetShadowCard("rearguard");
		var blademaster = scn.GetShadowCard("blademaster");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(field, rearguard);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, lancers, blademaster);
		scn.SetTwilight(10);

		// The Rearguard made a Southron minion that is no Man.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertFalse(scn.HasKeyword(rearguard, Keyword.SOUTHRON));
		scn.ShadowUseCardAction(rearguard);
		assertTrue(scn.HasKeyword(rearguard, Keyword.SOUTHRON));
		assertTrue(scn.HasKeyword(rearguard, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, explorer}, new PhysicalCardImpl[]{gimli, blademaster});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(lancers));
		assertEquals(5, scn.GetStrength(explorer));
		assertEquals(11, scn.GetStrength(lancers));
		assertEquals(5, scn.GetStrength(rearguard));
		assertTrue(scn.ShadowActionAvailable(field));

		scn.ShadowUseCardAction(field);
		// Southrons in the skirmish and out of it, a Man of another title and one that is no Man; not the
		// Easterling.
		assertTrue(scn.ShadowHasCardChoiceAvailable(explorer, lancers, rearguard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(blademaster, aragorn, gimli));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(lancers);

		// The Southron exerted is the one made stronger.
		assertEquals(1, scn.GetWoundsOn(lancers));
		assertEquals(12, scn.GetStrength(lancers));
		assertEquals(5, scn.GetStrength(explorer));
		assertEquals(5, scn.GetStrength(rearguard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn, 8, beats the Explorer, 5; in the next skirmish the bonus is gone.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(11, scn.GetStrength(lancers));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithItsOnlySouthronExhaustedTheSkirmishAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var field = scn.GetShadowCard("field");
		var explorer = scn.GetShadowCard("explorer");
		var blademaster = scn.GetShadowCard("blademaster");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(field);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, blademaster);
		// Vitality 3 with 2 wounds: exhausted, so he cannot exert; the Easterling can, and is no Southron.
		scn.AddWoundsToChar(explorer, 2);
		assertEquals(2, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(blademaster));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, blademaster);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(field));
	}
}
