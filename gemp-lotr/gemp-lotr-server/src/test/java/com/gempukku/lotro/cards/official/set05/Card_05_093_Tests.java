package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.VitalityModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_093_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("theoden", "5_93");
					put("guard", "5_83");       // Household Guard: a second [rohan] Man companion, valiant and nothing else
					// Guma, Plains Farmer: a [rohan] Man ally, vitality 2, home 4T; a fellowship ability only
					put("guma", "4_277");
					// Rohirrim Herdsman: a [rohan] Man ally of another title, vitality 2, home 3T; a regroup ability only
					put("herdsman", "7_247");
					// Albert Dreary: a [gandalf] Man ally, vitality 3, home 1F; a maneuver ability used only on purpose
					put("albert", "1_69");
					// Sword of Rohan: [rohan] hand weapon, "Bearer must be a [rohan] Man"; damage +1 only against an Uruk-hai
					put("sword", "4_291");
					put("sword2", "4_291");
					put("chain", "4_278");      // Heavy Chain: [rohan] armor, "Bearer must be a Man"; a bar on overwhelming only
					put("shield", "1_107");     // Great Shield: a [gondor] possession, "Bearer must be a Man"; minion archery total -1
					// Herugrim: a [rohan] artifact (the erratum of 4_280), "Bearer must be Théoden", so he may bear it;
					// damage +1 only once it is borne
					put("herugrim", "54_280");
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
	public void TheodenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Théoden, King of the Golden Hall
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 2
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: <b>Valiant</b>.
		 * 		<b>Maneuver:</b> Play a [rohan] possession on Théoden to heal a [rohan] ally.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("theoden");

		assertEquals("Théoden", card.getBlueprint().getTitle());
		assertEquals("King of the Golden Hall", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet());
	}

	@Test
	public void ManeuverActionPlaysARohanPossessionOnTheodenToHealOneWoundFromARohanAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var herdsman = scn.GetFreepsCard("herdsman");
		var albert = scn.GetFreepsCard("albert");
		var sword = scn.GetFreepsCard("sword");
		var chain = scn.GetFreepsCard("chain");
		var shield = scn.GetFreepsCard("shield");
		var hall = scn.GetFreepsCard("hall");
		var herugrim = scn.GetFreepsCard("herugrim");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(theoden, guard);
		scn.MoveAlliesToTable(guma, herdsman, albert);
		scn.MoveCardsToHand(sword, chain, shield, hall, herugrim);

		scn.StartGame();
		// Guma is given a third vitality by hand, so that he can carry the two wounds that tell "heal" from "heal all".
		scn.ApplyAdHocModifier(new VitalityModifier(null, guma, null, 1));
		scn.AddWoundsToChar(theoden, 1);
		scn.AddWoundsToChar(guard, 2);
		scn.AddWoundsToChar(guma, 2);
		scn.AddWoundsToChar(herdsman, 1);
		scn.AddWoundsToChar(albert, 1);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetWoundsOn(guma));
		assertEquals(1, scn.GetWoundsOn(herdsman));
		assertEquals(1, scn.GetWoundsOn(albert));
		assertInHand(sword, chain, shield, hall, herugrim);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(theoden));

		scn.FreepsUseCardAction(theoden);
		// The two [rohan] possessions are offered; the [gondor] possession, the [rohan] condition and the [rohan]
		// artifact (which he could bear, but is no possession) are not.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(sword, chain));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(shield, hall, herugrim));
		scn.FreepsChooseCard(sword);

		// The Sword goes on Théoden, not on the other [rohan] Man who could bear it.
		assertAttachedTo(sword, theoden);
		assertTrue(scn.GetAttachedCards(guard).isEmpty());
		assertInHand(chain, shield, hall, herugrim);

		// The two wounded [rohan] allies are offered; the [gandalf] ally and the two [rohan] companions are not.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(guma, herdsman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(albert, guard, theoden));
		scn.FreepsChooseCard(guma);

		// One of Guma's two wounds is healed, and nobody else's.
		assertEquals(1, scn.GetWoundsOn(guma));
		assertEquals(1, scn.GetWoundsOn(herdsman));
		assertEquals(1, scn.GetWoundsOn(albert));
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverActionCanPlayARohanPossessionThatAnyManMayBear() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var herdsman = scn.GetFreepsCard("herdsman");
		var chain = scn.GetFreepsCard("chain");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(theoden, guard);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToHand(chain);

		scn.StartGame();
		scn.AddWoundsToChar(herdsman, 1);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetWoundsOn(herdsman));
		assertInHand(chain);
		assertTrue(scn.FreepsActionAvailable(theoden));

		// One such possession in hand and one wounded [rohan] ally: both are chosen unasked.
		scn.FreepsUseCardAction(theoden);
		assertAttachedTo(chain, theoden);
		assertTrue(scn.GetAttachedCards(guard).isEmpty());
		assertEquals(0, scn.GetWoundsOn(herdsman));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoWoundedRohanAllyThePossessionIsStillPlayedAndNobodyIsHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var albert = scn.GetFreepsCard("albert");
		var chain = scn.GetFreepsCard("chain");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(theoden, guard);
		scn.MoveAlliesToTable(guma, albert);
		scn.MoveCardsToHand(chain);

		scn.StartGame();
		// Guma, the [rohan] ally, is unwounded; the [gandalf] ally and the [rohan] companions are wounded.
		scn.AddWoundsToChar(theoden, 1);
		scn.AddWoundsToChar(guard, 2);
		scn.AddWoundsToChar(albert, 1);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(guma));
		assertEquals(1, scn.GetWoundsOn(albert));
		assertInHand(chain);
		assertTrue(scn.FreepsActionAvailable(theoden));

		scn.FreepsUseCardAction(theoden);
		assertAttachedTo(chain, theoden);
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(guma));
		assertEquals(1, scn.GetWoundsOn(albert));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverActionIsNotOfferedWithNoRohanPossessionInHandThatTheodenCanBear() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var herdsman = scn.GetFreepsCard("herdsman");
		var sword = scn.GetFreepsCard("sword");
		var sword2 = scn.GetFreepsCard("sword2");
		var shield = scn.GetFreepsCard("shield");
		var hall = scn.GetFreepsCard("hall");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(theoden, guard);
		scn.MoveAlliesToTable(herdsman);
		scn.AttachCardsTo(theoden, sword);
		// A second hand weapon he cannot bear beside the first, a [gondor] possession and a [rohan] condition.
		scn.MoveCardsToHand(sword2, shield, hall);

		scn.StartGame();
		scn.AddWoundsToChar(herdsman, 1);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertAttachedTo(sword, theoden);
		assertEquals(1, scn.GetWoundsOn(herdsman));
		assertInHand(sword2, shield, hall);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(theoden));
	}
}
