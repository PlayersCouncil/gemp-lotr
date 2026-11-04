package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_112_Tests
{

	// Frodo, Son of Drogo 1_290 is the Ring-bearer, strength 3 with The Great Ring 19_1, which adds no strength and
	// answers no wound while it is not worn.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("crusher", "13_112");
					put("goblin", "11_137");    // Scurrying Goblin: [orc] Orc, cost 1, strength 5, vitality 1; its played trigger is at an underground site only
					put("reaper", "13_116");    // Orc Reaper: [orc] Orc, cost 3, strength 8; strength +2 only while a Free Peoples culture token can be spotted
					put("halftroll", "14_12");  // Half-troll of Far Harad: [orc] Half-troll, no Orc, cost 3, strength 8; its trigger is at the start of the assignment phase, used only on purpose
					put("howler", "13_109");    // Howling Orc: [orc] Orc, strength 8, lurker; its trigger answers an [orc] minion killed
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its text is on its own play
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer, strength 3, vitality 4; his abilities are used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound, strength 8, vitality 4; his maneuver ability is used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.GreatRing
		);
	}

	@Test
	public void OrcCrusherStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Orc Crusher
		 * Unique: True
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: To play, spot an [orc] Orc.<br>This minion is twilight cost -1 for each burden.<br>Each time a Ring-bound companion loses a skirmish involving an [orc] minion, you may reveal this card from hand to add a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("crusher");

		assertEquals("Orc Crusher", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CanBePlayedOnlyWhileAnOrcOrcCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crusher = scn.GetShadowCard("crusher");
		var goblin = scn.GetShadowCard("goblin");
		var halftroll = scn.GetShadowCard("halftroll");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(crusher);

		scn.StartGame();
		scn.MoveMinionsToTable(halftroll, runner);
		scn.SetTwilight(10);
		assertInHand(crusher);
		scn.FreepsPass();

		// An [orc] minion who is no Orc, and an Orc who is not [orc]: not enough.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(crusher));

		scn.MoveMinionsToTable(goblin);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(crusher));
		scn.ShadowPlayCard(crusher);
		assertInZone(Zone.SHADOW_CHARACTERS, crusher);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsTwilightCostMinusOneForEachBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crusher = scn.GetShadowCard("crusher");
		var reaper = scn.GetShadowCard("reaper");
		var halftroll = scn.GetShadowCard("halftroll");
		scn.MoveCardsToHand(crusher, halftroll);

		scn.StartGame();
		assertInHand(crusher, halftroll);
		assertEquals(0, scn.GetBurdens());
		assertEquals(5, scn.GetTwilightCost(crusher));
		assertEquals(3, scn.GetTwilightCost(halftroll));

		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		assertEquals(4, scn.GetTwilightCost(crusher));
		assertEquals(3, scn.GetTwilightCost(halftroll));

		scn.AddBurdens(2);
		assertEquals(3, scn.GetBurdens());
		assertEquals(2, scn.GetTwilightCost(crusher));
		assertEquals(3, scn.GetTwilightCost(halftroll));

		// Orc Reaper, another [orc] Orc, meets the requirement.
		scn.MoveMinionsToTable(reaper);
		scn.SetTwilight(10);
		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(crusher));
		int pool = scn.GetTwilight();
		scn.ShadowPlayCard(crusher);

		// 5 - 3 burdens + 2 roaming (site 4 minion at site 2)
		assertEquals(pool - 4, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, crusher);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachTimeARingBoundCompanionLosesToAnOrcMinionItMayBeRevealedToAddABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, aragorn);

		var crusher = scn.GetShadowCard("crusher");
		var reaper = scn.GetShadowCard("reaper");
		var goblin = scn.GetShadowCard("goblin");
		var halftroll = scn.GetShadowCard("halftroll");
		var howler = scn.GetShadowCard("howler");
		scn.MoveCardsToHand(crusher, reaper);

		scn.StartGame();
		scn.MoveMinionsToTable(goblin, halftroll, howler);
		// Only so that the Half-troll (8) does not overwhelm Sam: 3 + 2 = 5.
		scn.ApplyAdHocModifier(new StrengthModifier(null, sam, null, 2));
		assertInHand(crusher, reaper);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, goblin}, new PhysicalCardImpl[]{sam, halftroll},
				new PhysicalCardImpl[]{aragorn, howler});

		// Frodo, the Ring-bearer (3), loses to the Goblin, an [orc] Orc (5).
		assertEquals(0, scn.GetBurdens());
		scn.FreepsResolveSkirmish(frodo);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(crusher));
		scn.ShadowAcceptOptionalTrigger();

		// The Crusher is shown to the Free Peoples player alone, and nothing else in the hand is.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(crusher, false));
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();
		assertEquals(1, scn.GetBurdens());
		assertInHand(crusher, reaper);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Sam, Ring-bound and not the Ring-bearer (5), loses to the Half-troll, an [orc] minion who is no Orc (8).
		scn.FreepsResolveSkirmish(sam);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(crusher));
		scn.ShadowAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();
		assertEquals(2, scn.GetBurdens());
		assertInHand(crusher);

		// Aragorn, unbound (8), loses to the Howling Orc (8, a tie goes to the Shadow side), skirmished last as a lurker: nothing.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ALossToAMinionThatIsNotOrcOrAWinOverAnOrcMinionDoesNotSetItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		var crusher = scn.GetShadowCard("crusher");
		var goblin = scn.GetShadowCard("goblin");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(crusher);

		scn.StartGame();
		scn.MoveMinionsToTable(goblin, runner);
		// Only so that Sam (3 + 3 = 6) beats the Goblin (5).
		scn.ApplyAdHocModifier(new StrengthModifier(null, sam, null, 3));
		assertInHand(crusher);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, runner}, new PhysicalCardImpl[]{sam, goblin});

		// Frodo (3) loses to the Goblin Runner (5), a [moria] Orc.
		scn.FreepsResolveSkirmish(frodo);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Sam wins against the [orc] Goblin, which dies of its wound.
		scn.FreepsResolveSkirmish(sam);
		scn.PassCurrentPhaseActions();
		assertEquals(0, scn.GetWoundsOn(sam));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetBurdens());
		assertInHand(crusher);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningShowsNothingAndAddsNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var crusher = scn.GetShadowCard("crusher");
		var goblin = scn.GetShadowCard("goblin");
		scn.MoveCardsToHand(crusher);

		scn.StartGame();
		scn.MoveMinionsToTable(goblin);
		assertInHand(crusher);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, goblin);
		scn.FreepsResolveSkirmish(frodo);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(crusher));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(0, scn.GetBurdens());
		assertInHand(crusher);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnTheTableItCannotBeRevealedFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var crusher = scn.GetShadowCard("crusher");
		var goblin = scn.GetShadowCard("goblin");

		scn.StartGame();
		scn.MoveMinionsToTable(goblin, crusher);
		assertInZone(Zone.SHADOW_CHARACTERS, crusher);
		scn.SkipToAssignments();
		// The Crusher is left unassigned.
		scn.FreepsAssignToMinions(frodo, goblin);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);
		scn.PassCurrentPhaseActions();

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ItsOwnersOwnRingBearerLosingOnHisOwnTurnDoesNotSetItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var crusher = scn.GetFreepsCard("crusher");
		var goblin = scn.GetShadowCard("goblin");
		scn.MoveCardsToHand(crusher);

		scn.StartGame();
		scn.MoveMinionsToTable(goblin);
		assertInHand(crusher);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, goblin);
		scn.FreepsResolveSkirmish(frodo);
		scn.PassCurrentPhaseActions();

		// On his own turn he is the Free Peoples player, and a Shadow card in his hand is not his to use then (Q-0382).
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(crusher));
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertEquals(0, scn.GetBurdens());
		assertInHand(crusher);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
