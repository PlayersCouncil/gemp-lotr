package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_122_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eowyn", "5_122");
					put("guard", "5_83");       // Household Guard: a second [rohan] Man companion, valiant and nothing else
					// Sword of Rohan: [rohan] hand weapon, "Bearer must be a [rohan] Man"; damage +1 only against an Uruk-hai
					put("sword", "4_291");
					put("sword2", "4_291");
					put("chain", "4_278");      // Heavy Chain: [rohan] armor, "Bearer must be a Man"; a bar on overwhelming only
					put("shield", "1_107");     // Great Shield: a [gondor] possession, "Bearer must be a Man"; minion archery total -1
					// Protecting the Hall: a [rohan] condition (toil 2), "Bearer must be a [rohan] Man"; an assignment ability only
					put("hall", "11_149");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EowynStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Éowyn, Daughter of Éomund
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: <b>Valiant</b>.
		 * 		<b>Maneuver:</b> Play a [rohan] possession on Éowyn to heal her.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("eowyn");

		assertEquals("Éowyn", card.getBlueprint().getTitle());
		assertEquals("Daughter of Éomund", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet());
	}

	@Test
	public void ManeuverActionPlaysARohanPossessionOnEowynToHealOneWoundFromHer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var guard = scn.GetFreepsCard("guard");
		var sword = scn.GetFreepsCard("sword");
		var chain = scn.GetFreepsCard("chain");
		var shield = scn.GetFreepsCard("shield");
		var hall = scn.GetFreepsCard("hall");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(eowyn, guard);
		scn.MoveCardsToHand(sword, chain, shield, hall);

		scn.StartGame();
		scn.AddWoundsToChar(eowyn, 2);
		scn.AddWoundsToChar(guard, 2);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetWoundsOn(eowyn));
		assertEquals(2, scn.GetWoundsOn(guard));
		assertInHand(sword, chain, shield, hall);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(eowyn));

		scn.FreepsUseCardAction(eowyn);
		// The two [rohan] possessions are offered; the [gondor] possession and the [rohan] condition are not.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(sword, chain));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(shield, hall));
		scn.FreepsChooseCard(sword);

		// The Sword goes on Éowyn, not on the other [rohan] Man who could bear it, and one of her two wounds is healed.
		assertAttachedTo(sword, eowyn);
		assertTrue(scn.GetAttachedCards(guard).isEmpty());
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertEquals(2, scn.GetWoundsOn(guard));
		assertInHand(chain, shield, hall);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverActionCanPlayARohanPossessionThatAnyManMayBear() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var guard = scn.GetFreepsCard("guard");
		var chain = scn.GetFreepsCard("chain");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(eowyn, guard);
		scn.MoveCardsToHand(chain);

		scn.StartGame();
		scn.AddWoundsToChar(eowyn, 2);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetWoundsOn(eowyn));
		assertInHand(chain);
		assertTrue(scn.FreepsActionAvailable(eowyn));

		// One such possession in hand: it is played unasked.
		scn.FreepsUseCardAction(eowyn);
		assertAttachedTo(chain, eowyn);
		assertTrue(scn.GetAttachedCards(guard).isEmpty());
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverActionIsNotOfferedWithNoRohanPossessionInHandThatEowynCanBear() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var guard = scn.GetFreepsCard("guard");
		var sword = scn.GetFreepsCard("sword");
		var sword2 = scn.GetFreepsCard("sword2");
		var shield = scn.GetFreepsCard("shield");
		var hall = scn.GetFreepsCard("hall");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(eowyn, guard);
		scn.AttachCardsTo(eowyn, sword);
		// A second hand weapon she cannot bear beside the first, a [gondor] possession and a [rohan] condition.
		scn.MoveCardsToHand(sword2, shield, hall);

		scn.StartGame();
		scn.AddWoundsToChar(eowyn, 2);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertAttachedTo(sword, eowyn);
		assertEquals(2, scn.GetWoundsOn(eowyn));
		assertInHand(sword2, shield, hall);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(eowyn));
	}
}
