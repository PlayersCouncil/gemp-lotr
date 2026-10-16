package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_040_Tests
{

	// The King path: the skirmishes are at King's Tent 7_335, whose Shadow action is never used here.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gollum", "6_40");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound, strength 8; a maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer; a fellowship ability only
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					// The cards of the Shadow player's hand: a Shadow minion and a Free Peoples ally of his deck
					put("troop", "1_177");      // Goblin Patrol Troop: no text
					put("bounder", "1_286");    // Bounder
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GollumStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Gollum, Old Villain
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Minion
		 * Strength: 5
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: <b>Skirmish:</b> Discard a card from hand to make Gollum strength +1 (or +2 if skirmishing a Ring-bound companion).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gollum");

		assertEquals("Gollum", card.getBlueprint().getTitle());
		assertEquals("Old Villain", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishActionDiscardsACardOfHisChoiceToMakeGollumStrengthPlusOneAgainstAnUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var gollum = scn.GetShadowCard("gollum");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var bounder = scn.GetShadowCard("bounder");

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, gollum}, new PhysicalCardImpl[]{sam, savage});
		scn.SetShadowHand(troop, bounder);

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.ShadowActionAvailable(gollum));

		scn.ShadowUseCardAction(gollum);
		// A Shadow card and a Free Peoples card: either may be discarded, and one must be.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, bounder));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(bounder);

		assertEquals(1, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, bounder);
		assertInZone(Zone.HAND, troop);
		// Aragorn is unbound: +1, though Sam, who is Ring-bound, is assigned to another minion.
		assertEquals(6, scn.GetStrength(gollum));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void UsedInAnotherSkirmishItIsPlusOneAndGoneInHisOwnAgainstSamWhereItIsPlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var gollum = scn.GetShadowCard("gollum");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var bounder = scn.GetShadowCard("bounder");

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, gollum}, new PhysicalCardImpl[]{aragorn, savage});
		scn.SetShadowHand(troop, bounder);

		// Aragorn's skirmish first: Gollum is assigned to Sam, but is not yet skirmishing him.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(5, scn.GetStrength(gollum));
		scn.ShadowUseCardAction(gollum);
		scn.ShadowChooseCard(bounder);
		assertEquals(6, scn.GetStrength(gollum));
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn (8) beats the Uruk Savage (5).
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));

		// A skirmish action lasts for its own skirmish only.
		scn.FreepsResolveSkirmish(sam);
		assertEquals(5, scn.GetStrength(gollum));
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(gollum));

		scn.ShadowUseCardAction(gollum);
		// Skirmishing Sam, who is Ring-bound: +2 in place of +1, not both.
		assertEquals(7, scn.GetStrength(gollum));
		assertEquals(0, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, troop, bounder);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithAnEmptyHandItIsNotOfferedAndAgainstTheRingBearerItIsPlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gollum = scn.GetShadowCard("gollum");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, gollum);
		scn.SetShadowHand();

		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(0, scn.GetShadowHandCount());
		assertFalse(scn.ShadowActionAvailable(gollum));

		scn.SetShadowHand(troop);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(gollum));
		assertEquals(5, scn.GetStrength(gollum));

		scn.ShadowUseCardAction(gollum);
		assertInZone(Zone.DISCARD, troop);
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(7, scn.GetStrength(gollum));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
