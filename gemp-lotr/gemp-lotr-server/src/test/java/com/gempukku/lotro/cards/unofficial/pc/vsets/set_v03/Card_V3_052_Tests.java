package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInDiscard;
import static com.gempukku.lotro.framework.Assertions.assertInHand;
import static org.junit.Assert.*;

public class Card_V3_052_Tests
{

// ----------------------------------------
// HAILING FROM THE FAR SOUTH TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hailing", "103_52");     // Hailing from the Far South
					put("legion", "4_218");       // Desert Legion - unique Southron, cost 6
					put("explorer", "4_250");     // Southron Explorer - non-unique, cost 2
					put("stalker", "103_49");     // Desert Wind Stalker - unique 2 (NOT unique)
					put("mumak", "5_73");         // Mumak - [Raider] mount possession
					put("charger", "103_41");     // Bladetusk Charger - transforms into mounted Southron
					put("southron1", "4_222");    // Desert Warrior - for stacking on Charger

					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HailingfromtheFarSouthStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Hailing from the Far South
		 * Unique: true
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time you play a unique Southron, you may exert it to take a non-unique [raider] item or
		 * minion into hand from discard.
		 * 	Response: If your mounted Southron wins a skirmish, hinder this condition to make that Southron
		 * 	<b>relentless</b> until the regroup phase <i>(they participate in 1 extra round of skirmishes after fierce)</i>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hailing");

		assertEquals("Hailing from the Far South", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}



	@Test
	public void HailingTriggersWhenUniqueSouthronPlayedToRetrieveNonUnique() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hailing = scn.GetShadowCard("hailing");
		var legion = scn.GetShadowCard("legion");
		var explorer = scn.GetShadowCard("explorer");
		var mumak = scn.GetShadowCard("mumak");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(hailing);
		scn.MoveCardsToHand(legion);
		scn.MoveCardsToDiscard(explorer, mumak); // Non-unique minion and item
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(15);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(legion);

		// Trigger available - unique Southron played
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Legion gets exerted as cost
		assertEquals(1, scn.GetWoundsOn(legion));

		// Can choose non-unique [raider] item or minion from discard
		assertTrue(scn.ShadowHasCardChoicesAvailable(explorer, mumak));
		scn.ShadowChooseCard(explorer);

		assertInHand(explorer);
		assertInDiscard(mumak);
	}

	@Test
	public void HailingDoesNotTriggerForNonUniqueSouthron() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hailing = scn.GetShadowCard("hailing");
		var explorer = scn.GetShadowCard("explorer");
		var stalker = scn.GetShadowCard("stalker"); // Unique 2 = not unique
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(hailing);
		scn.MoveCardsToHand(explorer, stalker);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(15);
		scn.FreepsPassCurrentPhaseAction();

		// Play non-unique Explorer
		scn.ShadowPlayCard(explorer);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		// Play Stalker (unique 2 = non-unique for game purposes)
		scn.ShadowPlayCard(stalker);
		scn.ShadowChoose("Remove"); // Pay the tracker tax
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void HailingCannotRetrieveUniqueCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hailing = scn.GetShadowCard("hailing");
		var legion = scn.GetShadowCard("legion");
		var explorer = scn.GetShadowCard("explorer");
		var stalker = scn.GetShadowCard("stalker"); // Unique 2 = not unique, should be retrievable
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(hailing);
		scn.MoveCardsToHand(legion);
		scn.MoveCardsToDiscard(explorer, stalker);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(15);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(legion);
		scn.ShadowAcceptOptionalTrigger();

		// Both explorer and stalker should be valid (both non-unique)
		assertTrue(scn.ShadowHasCardChoicesAvailable(explorer, stalker));
	}

	@Test
	public void HailingResponseGrantsRelentlessToMountedSouthronWinningSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hailing = scn.GetShadowCard("hailing");
		var explorer = scn.GetShadowCard("explorer");
		var mumak = scn.GetShadowCard("mumak");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(hailing);
		scn.MoveMinionsToTable(explorer);
		scn.AttachCardsTo(explorer, mumak); // Explorer is now mounted, str 5+3=8
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, explorer);
		scn.FreepsResolveSkirmish(aragorn);

		assertFalse(scn.IsHindered(hailing));
		assertFalse(scn.HasKeyword(explorer, Keyword.RELENTLESS));

		// Pass to let skirmish resolve - Explorer wins
		scn.PassCurrentPhaseActions();

		// Response available
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowUseCardAction(hailing);

		assertTrue(scn.IsHindered(hailing));
		assertTrue(scn.HasKeyword(explorer, Keyword.RELENTLESS));
	}

	@Test
	public void HailingResponseWorksWithTransformedMumak() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hailing = scn.GetShadowCard("hailing");
		var charger = scn.GetShadowCard("charger");
		var southron = scn.GetShadowCard("southron1");
		var legion = scn.GetShadowCard("legion");
		var explorer = scn.GetShadowCard("explorer");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(hailing, charger);
		scn.MoveMinionsToTable(explorer); // Bait to reach Maneuver
		scn.StackCardsOn(charger, southron, legion);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		// Transform Charger - becomes mounted Southron minion
		scn.ShadowUseCardAction(charger);

		assertTrue(scn.HasKeyword(charger, Keyword.MOUNTED));
		assertTrue(scn.HasKeyword(charger, Keyword.SOUTHRON));

		// Charger is str 4 base + 3 per stacked (2) = 10, need to beat Aragorn (8)

		scn.PassCurrentPhaseActions(); // Finish Maneuver
		scn.PassCurrentPhaseActions(); // Archery
		scn.PassCurrentPhaseActions(); // Assignment actions
		scn.FreepsAssignToMinions(aragorn, charger);
		scn.ShadowAcceptOptionalTrigger(); // Ambush from Charger
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		scn.PassCurrentPhaseActions();

		// Charger should win - Response available
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.IsHindered(hailing));
		assertTrue(scn.HasKeyword(charger, Keyword.RELENTLESS));
	}
}
