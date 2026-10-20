package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_329_Tests
{

	// The Fellowship path with Breeland Forest as its site 2.  Site 3 is Rivendell Valley (a sanctuary; nobody
	// here is wounded) and site 4 Balin's Tomb (a Maneuver action used only on purpose).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("depart", "3_31");      // Depart Silently: a [gandalf] Stealth Maneuver event; spot Gandalf
					put("stealth", "1_298");    // Hobbit Stealth: a [shire] Stealth Skirmish event
					put("noble", "1_304");      // Noble Intentions: a [shire] Skirmish event that is no Stealth event
					put("imitation", "2_106");  // Nice Imitation: a [shire] Stealth condition, no event; plays to the support area
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: for Depart Silently's spot and Noble Intentions' exertion
					put("merry", "1_302");      // Merry, Friend to Sam: an unbound Hobbit to skirmish, so that the Ring-bearer's skirmish is not the one cancelled
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_329");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BreelandForestStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Breeland Forest
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 2
		 * Shadow Number: 1
		 * Direction: Left
		 * Game Text: <b>Forest</b>. Stealth events may not be played.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(2);

		assertEquals("Breeland Forest", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(2, card.getBlueprint().getSiteNumber());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.FOREST));
	}

	@Test
	public void StealthEventsOfEitherCultureCannotBePlayedAtBreelandForest() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var depart = scn.GetFreepsCard("depart");
		var stealth = scn.GetFreepsCard("stealth");
		var noble = scn.GetFreepsCard("noble");
		var gandalf = scn.GetFreepsCard("gandalf");
		var merry = scn.GetFreepsCard("merry");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf, merry);
		scn.MoveCardsToHand(depart, stealth, noble);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals("Breeland Forest", scn.GetCurrentSite().getBlueprint().getTitle());
		assertInZone(Zone.FREE_CHARACTERS, gandalf, merry);
		assertInHand(depart, stealth, noble);
		// Gandalf can be spotted, but Depart Silently is a Stealth event.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(depart));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, troop);
		scn.FreepsResolveSkirmish(merry);

		// A skirmish involving a Hobbit: Hobbit Stealth is a Stealth event and cannot be played; Noble Intentions,
		// a Skirmish event of the same culture that is no Stealth event, can.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.IsCharSkirmishing(merry));
		assertFalse(scn.FreepsPlayAvailable(stealth));
		assertTrue(scn.FreepsPlayAvailable(noble));
	}

	@Test
	public void AStealthCardThatIsNoEventCanBePlayedAtBreelandForest() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var imitation = scn.GetFreepsCard("imitation");

		scn.StartGame();
		scn.SkipToSite(2);
		assertEquals("Breeland Forest", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.SetFreepsHand(imitation);
		scn.FreepsRefreshDecision();

		assertInHand(imitation);
		assertTrue(scn.FreepsPlayAvailable(imitation));
		scn.FreepsPlayCard(imitation);
		assertInZone(Zone.SUPPORT, imitation);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasLeftBreelandForestStealthEventsCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var depart = scn.GetFreepsCard("depart");
		var stealth = scn.GetFreepsCard("stealth");
		var gandalf = scn.GetFreepsCard("gandalf");
		var merry = scn.GetFreepsCard("merry");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf, merry);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetFreepsHand(depart, stealth);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// Site 4, with Breeland Forest behind the fellowship on the path.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals("Breeland Forest", scn.GetSite(2).getBlueprint().getTitle());
		assertInZone(Zone.FREE_CHARACTERS, gandalf, merry);
		assertInHand(depart, stealth);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(depart));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, troop);
		scn.FreepsResolveSkirmish(merry);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(stealth));
	}
}
