package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_049_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fanatic", "10_49");
					put("fanatic2", "10_49");   // a second Southron Fanatic
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, strength 13

					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man, strength 6, vitality 3; Knight only
					put("guard", "5_83");       // Household Guard: a [rohan] Man, strength 6, vitality 3; Valiant only
					put("gimli", "0_62");       // Gimli, Dwarven Delegate: a companion who is no Man; damage +1 only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path, on which the Gondorian Merchant (home site 3) is assigned at site 3 as an ally
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fanatic", "10_49");
					// Gondorian Merchant: a Man ally, vitality 1, home site 3 of the Towers block; a maneuver
					// ability that needs initiative and is only used on purpose
					put("merchant", "7_97");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SouthronFanaticStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Southron Fanatic
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: Southron.
		 * Each time a Man is assigned to skirmish this minion, wound that Man.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fanatic");

		assertEquals("Southron Fanatic", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.SOUTHRON));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AManAssignedToSkirmishItIsWoundedAndNoCompanionAssignedElsewhereOrWhoIsNoManIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var knight = scn.GetFreepsCard("knight");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(knight, guard, gimli);

		var fanatic = scn.GetShadowCard("fanatic");
		var fanatic2 = scn.GetShadowCard("fanatic2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(fanatic, fanatic2, troop);
		scn.SkipToAssignments();

		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The [gondor] Man to this Fanatic, the Dwarf to the other Fanatic, and the [rohan] Man to a minion that
		// is no Fanatic.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { knight, fanatic },
				new PhysicalCardImpl[] { gimli, fanatic2 },
				new PhysicalCardImpl[] { guard, troop });

		// One wound on the Man assigned to this Fanatic, and not a second one from the other copy, whose own
		// skirmish he is not in. Gimli is no Man; the Household Guard is assigned to the Troop.
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(fanatic));
		assertEquals(0, scn.GetWoundsOn(fanatic2));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AManAllyAssignedToItIsWoundedAndWithOneVitalityLeftIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveAlliesToTable(merchant);

		var fanatic = scn.GetShadowCard("fanatic");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(fanatic);
		scn.SkipToAssignments();

		// Site 3 of the Towers block: the Merchant is at his home and may be assigned.
		assertTrue(scn.FreepsCanAssign(merchant));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertInZone(Zone.SUPPORT, merchant);
		scn.FreepsAssignToMinions(merchant, fanatic);

		// A wound and not an exertion: with vitality 1 he is killed.
		assertInZone(Zone.DEAD, merchant);
		// He was the one character assigned, so no skirmish is left to fight.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachTimeTheShadowPlayerAssignsItToTheSameManHeIsWoundedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);

		var fanatic = scn.GetShadowCard("fanatic");

		scn.StartGame();
		scn.MoveMinionsToTable(fanatic);
		// Fierce only so that there is a second assignment, and the Guard strong enough to win and live.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, fanatic, null, Keyword.FIERCE));
		scn.ApplyAdHocModifier(new StrengthModifier(null, guard, null, 5));
		scn.SkipToAssignments();

		// The Free Peoples player assigns nobody; the Shadow player assigns the Fanatic to the Guard.
		assertEquals(0, scn.GetWoundsOn(guard));
		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(guard, fanatic);
		assertEquals(1, scn.GetWoundsOn(guard));

		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		// The Guard, 6 + 5, beats the Fanatic, 8, who takes one wound and lives.
		assertEquals(1, scn.GetWoundsOn(fanatic));
		assertEquals(1, scn.GetWoundsOn(guard));

		// The fierce assignment: the same Man, assigned to it again, is wounded again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(guard, fanatic);
		assertEquals(2, scn.GetWoundsOn(guard));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
