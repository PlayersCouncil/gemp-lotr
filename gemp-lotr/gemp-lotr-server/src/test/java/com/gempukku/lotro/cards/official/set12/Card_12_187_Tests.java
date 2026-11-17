package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_12_187_Tests
{

	// A Shadows-block path: the game starts at Sirannon Ruins, and the Shadow player lays Emyn Muil as the site after it.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion, vitality 3, not fierce, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai minion, strength 5, vitality 3, not fierce, damage +1 only
					put("rider", "12_161");     // Black Rider: [wraith] Nazgul minion, printed fierce; strength +1 for each companion
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, beats the Savage; a maneuver ability used only on purpose
				}},
				new HashMap<>()
				{{
					put("site1", "18_138");     // Sirannon Ruins: River, no other text
					put("site2", "12_187");
					// Never laid in these tests.
					put("site3", "11_234");
					put("site4", "17_148");
					put("site5", "11_230");
					put("site6", "12_185");
					put("site7", "13_185");
					put("site8", "12_188");
					put("site9", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void EmynMuilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Emyn Muil
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>Mountain</b>. <b>Maneuver:</b> Remove (2) and exert your minion that is not fierce to make that minion <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite("site2");

		assertEquals("Emyn Muil", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
	}

	@Test
	public void ManeuverActionRemovesTwoAndExertsAMinionThatIsNotFierceToMakeItFierceUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var rider = scn.GetShadowCard("rider");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site2"));
		var emyn = scn.GetCurrentSite();
		assertEquals("Emyn Muil", emyn.getBlueprint().getTitle());

		scn.MoveMinionsToTable(troop, savage, rider);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(4);
		scn.FreepsRefreshDecision();

		// "Your minion": the Free Peoples player, who has none, is not offered it with minions on the table.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(emyn));
		scn.FreepsPass();

		assertEquals(4, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(rider));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertTrue(scn.ShadowActionAvailable(emyn));
		scn.ShadowUseCardAction(emyn);

		// Either minion that is not fierce, of two cultures and races; not the fierce Black Rider, nor a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(rider, aragorn, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(savage);

		assertEquals(2, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(rider));
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Aragorn beats the Savage in the first round, and the Savage, wounded and alive, is still fierce after it.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));

		// The Savage is offered for the fierce assignment; the Troop is not.
		scn.PassFierceAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(savage));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(troop));
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		// At the first regroup decision he is fierce no longer.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
	}

	@Test
	public void WithOnly1TwilightTheActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site2"));
		var emyn = scn.GetCurrentSite();
		assertEquals("Emyn Muil", emyn.getBlueprint().getTitle());

		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(1);
		scn.FreepsPass();

		// Both minions could exert.
		assertEquals(1, scn.GetTwilight());
		assertEquals(3, scn.GetVitality(troop));
		assertEquals(3, scn.GetVitality(savage));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(emyn));
	}

	@Test
	public void WithHisOnlyMinionThatIsNotFierceExhaustedTheActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop");
		var rider = scn.GetShadowCard("rider");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site2"));
		var emyn = scn.GetCurrentSite();
		assertEquals("Emyn Muil", emyn.getBlueprint().getTitle());

		scn.MoveMinionsToTable(troop, rider);
		scn.AddWoundsToChar(troop, 2);
		assertEquals(2, scn.GetWoundsOn(troop));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(4);
		scn.FreepsPass();

		// The Troop has 1 vitality left; the fierce Rider could exert, and the pool holds enough.
		assertEquals(1, scn.GetVitality(troop));
		assertEquals(3, scn.GetVitality(rider));
		assertTrue(scn.HasKeyword(rider, Keyword.FIERCE));
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(emyn));
	}
}
