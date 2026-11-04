package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_135_Tests
{
	// Foot of Mount Doom stands in each adventure deck in place of site 9, which no test reaches, and a test puts the
	// Shadow player's copy on the Fellowship-block path as the next site the fellowship moves to.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a [gondor] Man; a maneuver ability never used here
					put("defend", "4_115");       // Defend It and Hope: [gondor] Maneuver event; exert a [gondor] Man to wound a
					                              // minion once (twice an Uruk-hai)
					put("guard", "7_246");        // Rohirrim Guard: [rohan] Man, strength 6, vitality 3; Skirmish: discard a card
					                              // from hand to exert a minion skirmishing him
					put("firefoot", "13_126");    // Firefoot, Mearas of The Mark: a [rohan] mount, strength +1; its triggers need a
					                              // [rohan] token or Éomer
					put("gimli", "1_13");         // Gimli, Son of Glóin: a card to discard from hand

					put("runner", "1_178");       // Goblin Runner: [moria] Orc, vitality 1; a play trigger, never played here
					put("scimitar", "1_180");     // Goblin Scimitar: [moria] hand weapon on a [moria] Orc; a play trigger only
					put("tracker", "15_171");     // Tracking Uruk: [uruk-hai] Uruk-hai, strength 14, vitality 3, hunter 1 only
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("foot", "18_135");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FootofMountDoomStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Foot of Mount Doom
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 2
		 * Direction: Left
		 * Game Text: <b>Mountain</b>. Each card that is about to be placed into the discard pile is removed from the
		 * 	game instead.
		*/

		var scn = GetScenario();

		var foot = scn.GetFreepsSite("foot");

		assertEquals("Foot of Mount Doom", foot.getBlueprint().getTitle());
		assertNull(foot.getBlueprint().getSubtitle());
		assertFalse(foot.getBlueprint().isUnique());
		assertEquals(CardType.SITE, foot.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, foot.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, foot.getBlueprint().getSiteNumber());
		assertEquals(2, foot.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, foot.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(foot, Keyword.MOUNTAIN));
	}

	@Test
	public void AtTheFootAPlayedEventAKilledMinionAndTheWeaponItBoreAreRemovedFromTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		var runner = scn.GetShadowCard("runner");
		var scimitar = scn.GetShadowCard("scimitar");
		var foot = scn.GetShadowSite("foot");

		scn.StartGame();
		// Foot of Mount Doom is site 2, and the fellowship moves to it in the fellowship phase.
		scn.MoveCardToAdventurePath(foot);
		scn.MoveMinionsToTable(runner);
		scn.AttachCardsTo(runner, scimitar);
		assertInHand(defend);
		assertInZone(Zone.ATTACHED, scimitar);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(foot, scn.GetCurrentSite());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetShadowDiscardCount());

		// The only [gondor] Man exerts and the only minion takes the wound: nothing is asked.
		scn.FreepsPlayCard(defend);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// A Free Peoples event, a Shadow character killed and the Shadow item it bore: none reaches a discard pile.
		assertInZone(Zone.REMOVED, defend, runner, scimitar);
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AtTheFootACardDiscardedFromHandAndTheMountOfAKilledCompanionAreRemovedAndTheCompanionGoesToTheDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var firefoot = scn.GetFreepsCard("firefoot");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, firefoot);
		scn.MoveCardsToHand(gimli);

		var tracker = scn.GetShadowCard("tracker");
		var foot = scn.GetShadowSite("foot");

		scn.StartGame();
		scn.MoveCardToAdventurePath(foot);
		scn.MoveMinionsToTable(tracker);
		assertInHand(gimli);
		assertInZone(Zone.ATTACHED, firefoot);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, tracker);
		scn.FreepsResolveSkirmish(guard);

		assertEquals(foot, scn.GetCurrentSite());
		assertEquals(0, scn.GetWoundsOn(tracker));
		assertEquals(0, scn.GetFreepsDiscardCount());

		// The only card in hand pays, and the only minion skirmishing him exerts.
		scn.FreepsUseCardAction(guard);
		assertEquals(1, scn.GetWoundsOn(tracker));
		assertInZone(Zone.REMOVED, gimli);
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.PassSkirmishActions();
		// 14 and hunter 1 against 7: the Guard is overwhelmed. A killed companion goes to the dead pile, which is no
		// discard pile; the mount he bore is removed from the game.
		assertInZone(Zone.DEAD, guard);
		assertInZone(Zone.REMOVED, firefoot);
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasLeftTheFootTheEventAndTheKilledMinionGoToTheDiscardPiles() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		var runner = scn.GetShadowCard("runner");
		var scimitar = scn.GetShadowCard("scimitar");
		var foot = scn.GetShadowSite("foot");

		scn.StartGame();
		scn.MoveCardToAdventurePath(foot);
		assertInHand(defend);
		// The fellowship moves to the Foot (site 2), and on to site 3 in the regroup phase.
		scn.FreepsPass();
		assertEquals(foot, scn.GetCurrentSite());
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());

		scn.MoveMinionsToTable(runner);
		scn.AttachCardsTo(runner, scimitar);
		assertInHand(defend);
		assertInZone(Zone.ATTACHED, scimitar);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetShadowDiscardCount());

		scn.FreepsPlayCard(defend);
		assertInZone(Zone.DISCARD, defend, runner, scimitar);
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertEquals(2, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
