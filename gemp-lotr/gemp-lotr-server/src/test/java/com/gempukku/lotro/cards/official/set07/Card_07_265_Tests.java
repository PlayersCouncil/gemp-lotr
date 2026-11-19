package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_265_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pike", "7_265");
					put("soldier", "1_271");        // Orc Soldier: [sauron] Orc, strength 7; his skirmish ability is used only on purpose
					put("band", "1_270");           // Orc Scouting Band: [sauron] Orc, strength 8, a tracker; a second [sauron] Orc of another title
					put("guard", "10_81");          // Cirith Ungol Guard: a [sauron] Uruk-hai, damage +1 and no other text
					put("hound", "7_192");          // Morgul Hound: a [wraith] Orc; +4 and fierce only at 2 wounds on the Ring-bearer

					put("stairs", "1_24");          // Stairs of Khazad-dum: [dwarven] support condition, cost 0, played only to take the hand from four to three
					put("filler1", "1_177");        // Goblin Patrol Troop, copies held in a hand
					put("filler2", "1_177");
					put("filler3", "1_177");
					put("filler4", "1_177");
					put("filler5", "1_177");

					// Mordor Fighter: [sauron] Orc, strength 7; while a threat and another [sauron] Orc can be spotted, the
					// Shadow has initiative whatever the Free Peoples hand holds
					put("fighter1", "7_286");
					put("fighter2", "7_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BesiegingPikeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Besieging Pike
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be a [sauron] Orc.<br>If you have initiative, bearer is strength +3.
		*/

		var scn = GetScenario();

		var pike = scn.GetFreepsCard("pike");

		assertEquals("Besieging Pike", pike.getBlueprint().getTitle());
		assertNull(pike.getBlueprint().getSubtitle());
		assertFalse(pike.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, pike.getBlueprint().getSide());
		assertEquals(Culture.SAURON, pike.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, pike.getBlueprint().getCardType());
		assertTrue(pike.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(0, pike.getBlueprint().getTwilightCost());
		assertEquals(2, pike.getBlueprint().getStrength());
	}

	@Test
	public void BearerMustBeASauronOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pike = scn.GetShadowCard("pike");
		var soldier = scn.GetShadowCard("soldier");
		var band = scn.GetShadowCard("band");
		var guard = scn.GetShadowCard("guard");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(pike);
		assertInHand(pike);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, band, guard, hound);

		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(pike));
		scn.ShadowPlayCard(pike);
		// Two [sauron] Orcs; not a [sauron] Uruk-hai, nor a [wraith] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, band));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, hound));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(band);
		assertAttachedTo(pike, band);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BearerIsStrengthPlus3OnlyOnceTheFreePeoplesHandFallsToThreeWhateverTheShadowHandHolds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pike = scn.GetShadowCard("pike");
		var soldier = scn.GetShadowCard("soldier");
		var band = scn.GetShadowCard("band");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCardsToHand(stairs, scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"));
		scn.MoveCardsToHand(scn.GetShadowCard("filler1"), scn.GetShadowCard("filler2"), scn.GetShadowCard("filler3"),
				scn.GetShadowCard("filler4"), scn.GetShadowCard("filler5"));
		assertEquals(4, scn.GetFreepsHandCount());

		scn.StartGame();
		// Laid early in the turn: the Orcs and the Pike stand together in the Shadow phase to come.
		scn.MoveMinionsToTable(soldier, band);
		scn.AttachCardsTo(soldier, pike);
		scn.FreepsRefreshDecision();
		assertAttachedTo(pike, soldier);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(5, scn.GetShadowHandCount());

		// Four cards in the Free Peoples hand: he has initiative, and the Shadow's five do not change that.
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(9, scn.GetStrength(soldier));  // 7 + 2 printed
		assertEquals(8, scn.GetStrength(band));

		// Four cards to three: the Shadow has initiative.
		scn.FreepsPlayCard(stairs);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(5, scn.GetShadowHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertEquals(12, scn.GetStrength(soldier)); // 7 + 2 printed + 3
		// Another [sauron] Orc who does not bear it is not strengthened.
		assertEquals(8, scn.GetStrength(band));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BearerIsStrengthPlus3WhileTheShadowHasInitiativeWhateverTheFreePeoplesHandHolds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pike = scn.GetShadowCard("pike");
		var soldier = scn.GetShadowCard("soldier");
		var fighter1 = scn.GetShadowCard("fighter1");
		var fighter2 = scn.GetShadowCard("fighter2");
		scn.MoveCardsToHand(scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"),
				scn.GetFreepsCard("filler4"), scn.GetFreepsCard("stairs"));
		assertEquals(5, scn.GetFreepsHandCount());

		scn.StartGame();

		scn.MoveMinionsToTable(soldier, fighter1, fighter2);
		scn.AttachCardsTo(soldier, pike);
		scn.FreepsRefreshDecision();
		assertAttachedTo(pike, soldier);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(9, scn.GetStrength(soldier));  // 7 + 2 printed

		// Two Mordor Fighters and a threat: the Shadow has initiative with five cards in the Free Peoples hand.
		scn.AddThreats(1);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetThreats());
		assertEquals(5, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertEquals(12, scn.GetStrength(soldier)); // 7 + 2 printed + 3
		assertEquals(7, scn.GetStrength(fighter1));

		// The threat gone, the hand of five gives the Free Peoples player initiative again.
		scn.RemoveThreats(1);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(9, scn.GetStrength(soldier));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
