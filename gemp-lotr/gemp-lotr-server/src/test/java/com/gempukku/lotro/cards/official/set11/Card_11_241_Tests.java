package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_241_Tests
{

	// The turn-skippers walk a Fellowship-block path, which they cannot do on a Shadows-block one.  Each adventure
	// deck has Fortress of Orthanc in place of site 9, and the tests put the Shadow player's copy on the path as the
	// site the fellowship moves to next, site 2 or site 3: region 1, so no region twilight is added for it.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Companions of four cultures and four races, none with text that adds twilight.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability never used here
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man; a skirmish ability
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] Dwarf; a skirmish ability
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf; an archery ability never reached
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit; a fellowship ability never used here
					put("pippin", "1_306");     // Pippin, Friend to Frodo: [shire] Hobbit; protects [shire] tales
					put("knight", "5_35");      // Gondorian Knight: a companion whose whole text is Knight; laid in the dead pile
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a Dwarf ally, who is no companion; a fellowship ability only
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("fortress", "11_241");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FortressofOrthancStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Fortress of Orthanc
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>Battleground</b>. When the fellowship moves to this site, add (2) for each companion over 5.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite("fortress");

		assertEquals("Fortress of Orthanc", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
	}

	@Test
	public void MovingThereWithSixCompanionsAddsTwoAndAnAllyAndADeadCompanionAreNotCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var sam = scn.GetFreepsCard("sam");
		var pippin = scn.GetFreepsCard("pippin");
		var knight = scn.GetFreepsCard("knight");
		var grimir = scn.GetFreepsCard("grimir");
		// Frodo and five more are six companions; the ally is a seventh Free Peoples character, and a seventh
		// companion lies in the dead pile.
		scn.MoveCompanionsToTable(aragorn, gimli, legolas, sam, pippin);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToDeadPile(knight);

		var fortress = scn.GetShadowSite("fortress");

		scn.StartGame();
		scn.MoveCardToAdventurePath(fortress);
		assertInZone(Zone.SUPPORT, grimir);
		assertInZone(Zone.DEAD, knight);
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();

		assertSame(fortress, scn.GetCurrentSite());
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 0 for the Shadow number + 6 companions + 1 companion over 5 x 2
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingThereInTheRegroupPhaseWithSevenCompanionsAddsFourAndTheMinionIsNotCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var sam = scn.GetFreepsCard("sam");
		var pippin = scn.GetFreepsCard("pippin");
		var grimir = scn.GetFreepsCard("grimir");
		// Frodo and six more are seven companions, and an ally.
		scn.MoveCompanionsToTable(aragorn, boromir, gimli, legolas, sam, pippin);
		scn.MoveAlliesToTable(grimir);

		var troop = scn.GetShadowCard("troop");
		var fortress = scn.GetShadowSite("fortress");

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		// A minion still in play in the regroup phase, as one that has lived through its skirmish is.
		scn.MoveMinionsToTable(troop);
		// The second player's Fortress of Orthanc is the next site, site 3.
		scn.MoveCardToAdventurePath(fortress);

		scn.SkipToMovementDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		int before = scn.GetTwilight();
		scn.FreepsChooseToMove();

		assertSame(fortress, scn.GetCurrentSite());
		assertEquals(3, scn.GetCurrentSiteNumber());
		// 0 for the Shadow number + 7 companions + 2 companions over 5 x 2
		assertEquals(before + 7 + 4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingThereWithFiveCompanionsAddsNothingAndMovingOnFromItWithSixAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var sam = scn.GetFreepsCard("sam");
		var pippin = scn.GetFreepsCard("pippin");
		// Frodo and four more are five companions.
		scn.MoveCompanionsToTable(aragorn, gimli, legolas, sam);

		var fortress = scn.GetShadowSite("fortress");

		scn.StartGame();
		scn.MoveCardToAdventurePath(fortress);
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();

		assertSame(fortress, scn.GetCurrentSite());
		// 0 for the Shadow number + 5 companions; none over 5
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A sixth companion for the move on from the Fortress, to Rivendell Valley (site 3, Shadow number 0).
		scn.MoveCompanionsToTable(pippin);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SkipToMovementDecision();
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		int before = scn.GetTwilight();
		scn.FreepsChooseToMove();

		assertEquals(3, scn.GetCurrentSiteNumber());
		// 0 for the Shadow number + 6 companions, and nothing for leaving the Fortress
		assertEquals(before + 6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
