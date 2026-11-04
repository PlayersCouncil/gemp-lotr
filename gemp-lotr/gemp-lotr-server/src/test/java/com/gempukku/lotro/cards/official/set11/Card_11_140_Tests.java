package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_140_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("strength", "11_140");

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability used only on purpose
					put("sam", "1_311");         // Sam, Son of Hamfast: [shire] Hobbit companion, strength 3; his abilities are used only on purpose

					put("denizen", "11_115");    // Denizen of Khazad-dum: [orc] Orc, strength 9; +2 at an underground site, and site 2 is none
					put("halftroll", "14_12");   // Half-troll of Far Harad: [orc] minion who is no Orc, strength 8; its assignment trigger wants more companions than minions
					put("prowling", "11_136");   // Prowling Orc: [orc] Orc, lurker, strength 6; its skirmish ability costs (2) and is not used
					put("troop", "1_177");       // Goblin Patrol Troop: a [moria] Orc, strength 13, no text
					put("beckoning", "12_83");   // The Beckoning Shadow: an [orc] condition, no minion; its text wants 5 burdens
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StrengthinShadowsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Strength in Shadows
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make an [orc] minion strength +2 (or +4 if it is a lurker).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("strength");

		assertEquals("Strength in Shadows", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesTheChosenOrcMinionWhoIsNoLurkerStrengthPlusTwoUntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var prowling = scn.GetShadowCard("prowling");
		var troop = scn.GetShadowCard("troop");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToHand(strength);

		scn.StartGame();
		assertInHand(strength);
		scn.MoveMinionsToTable(denizen, halftroll, prowling, troop);
		scn.MoveCardsToSupportArea(beckoning);
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, denizen}, new PhysicalCardImpl[]{sam, prowling});
		// The Troop joins Denizen against Aragorn; the Half-troll stays in no skirmish.
		scn.ShadowAssignToMinions(aragorn, troop);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, denizen));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, troop));
		assertFalse(scn.IsCharAssigned(halftroll));
		// Prowling Orc is a lurker: Sam's skirmish comes last.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(9, scn.GetStrength(denizen));
		assertEquals(8, scn.GetStrength(halftroll));
		assertEquals(6, scn.GetStrength(prowling));
		assertEquals(13, scn.GetStrength(troop));
		assertInHand(strength);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(strength));
		scn.ShadowPlayCard(strength);

		// A minion in no skirmish is offered too: the text names no skirmish.
		assertTrue(scn.ShadowHasCardChoiceAvailable(denizen, halftroll, prowling));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, beckoning));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(denizen);

		// +2, not +4: Denizen is no lurker, though a lurker is in play.
		assertEquals(11, scn.GetStrength(denizen));
		assertEquals(8, scn.GetStrength(halftroll));
		assertEquals(6, scn.GetStrength(prowling));
		assertEquals(13, scn.GetStrength(troop));
		assertInZone(Zone.DISCARD, strength);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Each skirmish is a phase of its own: in Sam's, the bonus is gone.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(9, scn.GetStrength(denizen));
	}

	@Test
	public void MakesTheChosenOrcLurkerStrengthPlusFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength");
		var aragorn = scn.GetFreepsCard("aragorn");
		var denizen = scn.GetShadowCard("denizen");
		var prowling = scn.GetShadowCard("prowling");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(strength);

		scn.StartGame();
		assertInHand(strength);
		scn.MoveMinionsToTable(denizen, prowling);
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, prowling);
		scn.ShadowDeclineAssignments();
		assertFalse(scn.IsCharAssigned(denizen));
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(6, scn.GetStrength(prowling));
		assertEquals(9, scn.GetStrength(denizen));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPlayCard(strength);
		assertTrue(scn.ShadowHasCardChoiceAvailable(denizen, prowling));
		scn.ShadowChooseCard(prowling);

		assertEquals(10, scn.GetStrength(prowling));
		assertEquals(9, scn.GetStrength(denizen));
		assertInZone(Zone.DISCARD, strength);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength");
		var aragorn = scn.GetFreepsCard("aragorn");
		var denizen = scn.GetShadowCard("denizen");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(strength);

		scn.StartGame();
		assertInHand(strength);
		scn.MoveMinionsToTable(denizen);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(strength));
		assertInHand(strength);
	}

	@Test
	public void WithNoOrcMinionTheEventIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(strength);

		scn.StartGame();
		assertInHand(strength);
		scn.MoveMinionsToTable(troop);
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(13, scn.GetStrength(troop));
		assertEquals(8, scn.GetStrength(aragorn));
		// CR 4.2 "effect": an event may be played though it will have no effect.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(strength));
		scn.ShadowPlayCard(strength);

		assertEquals(13, scn.GetStrength(troop));
		assertEquals(8, scn.GetStrength(aragorn));
		assertInZone(Zone.DISCARD, strength);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
