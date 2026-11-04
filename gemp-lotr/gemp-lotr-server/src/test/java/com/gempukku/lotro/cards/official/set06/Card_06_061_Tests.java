package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_061_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("desertion", "6_61");
					put("desertion2", "6_61");  // not unique: a second copy, on another companion

					// [isengard] Men, the cards to spot. Rohirrim Traitor: strength 6, +2 while a companion bears an
					// [isengard] condition. Agents of Orthanc: strength 9, more for Free Peoples cultures over 2.
					put("traitor", "6_72");
					put("agents", "6_57");
					// Dunlending Brigand: a [dunland] Man, no text
					put("brigand", "4_10");

					// [isengard] minions with a strength either side of the bearer's
					put("forger", "3_56");      // Isengard Forger: an Orc, strength 6. "Shadow: Exert this minion to add (1)."
					put("savage", "1_151");     // Uruk Savage: an Uruk-hai, strength 5, no other text
					put("servant", "3_58");     // Isengard Servant: an Orc, strength 7; a regroup ability only
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc, strength 5; its play trigger is not set off by a cheat

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8; a maneuver ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer; texts used only on purpose
					// Barliman Butterbur: a [gandalf] Man ally; a fellowship ability only
					put("barliman", "1_70");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DesertionStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Desertion
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Strength: -1
		 * Game Text: To play, spot an [isengard] Man. Plays on a companion (except the Ring-bearer).<br><b>Assignment:</b> Assign an [isengard] minion with less strength than bearer to skirmish bearer. Discard this condition.
		*/

		var scn = GetScenario();

		var desertion = scn.GetFreepsCard("desertion");

		assertEquals("Desertion", desertion.getBlueprint().getTitle());
		assertNull(desertion.getBlueprint().getSubtitle());
		assertFalse(desertion.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, desertion.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, desertion.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, desertion.getBlueprint().getCardType());
		assertEquals(1, desertion.getBlueprint().getTwilightCost());
		assertEquals(-1, desertion.getBlueprint().getStrength());
	}

	@Test
	public void CannotBePlayedUntilAnIsengardManIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var desertion = scn.GetShadowCard("desertion");
		var traitor = scn.GetShadowCard("traitor");
		var forger = scn.GetShadowCard("forger");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToHand(desertion, traitor);

		scn.StartGame();
		assertInZone(Zone.HAND, desertion, traitor);
		// An [isengard] minion who is no Man, and a Man of another culture.
		scn.MoveMinionsToTable(forger, brigand);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(desertion));

		scn.ShadowPlayCard(traitor);
		assertInZone(Zone.SHADOW_CHARACTERS, traitor);
		assertTrue(scn.ShadowPlayAvailable(desertion));
		scn.ShadowPlayCard(desertion);

		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo));
		scn.ShadowChooseCard(aragorn);
		assertAttachedTo(desertion, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlaysOnACompanionWhoIsNotTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(barliman);

		var desertion = scn.GetShadowCard("desertion");
		var agents = scn.GetShadowCard("agents");
		scn.MoveCardsToHand(desertion);

		scn.StartGame();
		assertInZone(Zone.HAND, desertion);
		scn.MoveMinionsToTable(agents);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(desertion));
		scn.ShadowPlayCard(desertion);

		// Companions of two cultures and races, a Ring-bound one among them; not the Ring-bearer, an ally or a minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, barliman, agents));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sam);

		assertAttachedTo(desertion, sam);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AssignmentAbilityAssignsAnIsengardMinionWithLessStrengthThanBearerToHimAndDiscardsThisCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var desertion = scn.GetShadowCard("desertion");
		var desertion2 = scn.GetShadowCard("desertion2");
		var forger = scn.GetShadowCard("forger");
		var savage = scn.GetShadowCard("savage");
		var servant = scn.GetShadowCard("servant");
		var runner = scn.GetShadowCard("runner");
		scn.AttachCardsTo(aragorn, desertion);
		scn.AttachCardsTo(sam, desertion2);

		scn.StartGame();
		assertAttachedTo(desertion, aragorn);
		assertAttachedTo(desertion2, sam);
		scn.MoveMinionsToTable(forger, savage, servant, runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// Aragorn's 8, -1 for this condition.
		assertEquals(7, scn.GetStrength(aragorn));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(desertion));
		scn.ShadowUseCardAction(desertion);

		// [isengard] minions of strength 6 and 5, an Orc and an Uruk-hai; not the [isengard] Orc of strength 7,
		// equal to the bearer's, nor the [moria] Orc of strength 5.
		assertTrue(scn.ShadowHasCardChoiceAvailable(forger, savage));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(servant, runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(forger);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, forger));
		assertFalse(scn.IsCharAssigned(savage));
		assertFalse(scn.IsCharAssigned(servant));
		assertFalse(scn.IsCharAssigned(runner));
		assertInZone(Zone.DISCARD, desertion);
		assertEquals(8, scn.GetStrength(aragorn));
		// The copy used is discarded; the copy on Sam stays.
		assertAttachedTo(desertion2, sam);
		assertFalse(scn.IsCharAssigned(sam));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoIsengardMinionOfLessStrengthTheAbilityAssignsNobodyAndDiscardsThisCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var desertion = scn.GetShadowCard("desertion");
		var servant = scn.GetShadowCard("servant");
		var runner = scn.GetShadowCard("runner");
		scn.AttachCardsTo(aragorn, desertion);

		scn.StartGame();
		assertAttachedTo(desertion, aragorn);
		// An [isengard] Orc of the bearer's strength, 7, and a [moria] Orc of less.
		scn.MoveMinionsToTable(servant, runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(desertion));
		scn.ShadowUseCardAction(desertion);

		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(servant));
		assertFalse(scn.IsCharAssigned(runner));
		assertInZone(Zone.DISCARD, desertion);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AssignmentAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var desertion = scn.GetShadowCard("desertion");
		var forger = scn.GetShadowCard("forger");
		scn.AttachCardsTo(aragorn, desertion);

		scn.StartGame();
		assertAttachedTo(desertion, aragorn);
		scn.MoveMinionsToTable(forger);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(desertion));
		assertAttachedTo(desertion, aragorn);
	}
}
