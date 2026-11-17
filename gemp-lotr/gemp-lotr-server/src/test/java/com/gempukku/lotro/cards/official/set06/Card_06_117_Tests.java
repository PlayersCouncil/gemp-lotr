package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_117_Tests
{

	// The Towers path with Meduseld as its site 3.  Site 2 is Eastfold (a Shadow action that needs 3 [isengard]
	// trackers, none of which is laid).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Companions with the Gandalf signet
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] Dwarf; a Skirmish ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man; a Maneuver ability only
					put("pippin", "1_307");     // Pippin, Hobbit of Some Intelligence: [shire] Hobbit; a play trigger, passed over by the cheat
					put("arwen", "3_8");        // Arwen, Lady Undómiel: [elven] Elf; a bonus that needs 3 [elven] allies
					// A companion with the Frodo signet
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard; his own strength only
					// For the dead pile and the discard pile
					put("ranger", "7_116");     // Ranger of Osgiliath: unbound [gondor] Man companion; a Regroup ability only
					put("legolas", "1_50");     // Legolas, Greenleaf: unbound [elven] Elf companion; an Archery ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally; a Skirmish ability only
					put("knight", "5_35");      // Gondorian Knight: unbound [gondor] Man companion, no text
					put("guard", "1_7");        // Dwarf Guard: unbound companion, "To play, spot a Dwarf"
				}},
				new HashMap<>()
				{{
					put("site1", "4_325");
					put("site2", "4_331");
					put("site3", "6_117");
					put("site4", "4_346");
					put("site5", "4_348");
					put("site6", "4_352");
					put("site7", "102_71");
					put("site8", "4_358");
					put("site9", "4_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MeduseldStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Meduseld
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 3T
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>Sanctuary</b>. <b>Fellowship:</b> Exert 3 companions with the Gandalf signet to play an unbound companion from your dead pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(3);

		assertEquals("Meduseld", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getSiteNumber());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void FellowshipActionExertsThreeGandalfSignetCompanionsToPlayAnUnboundCompanionFromTheDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var arwen = scn.GetFreepsCard("arwen");
		var gandalf = scn.GetFreepsCard("gandalf");
		var ranger = scn.GetFreepsCard("ranger");
		var legolas = scn.GetFreepsCard("legolas");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var knight = scn.GetFreepsCard("knight");

		scn.StartGame();
		scn.SkipToSite(3);
		var meduseld = scn.GetCurrentSite();
		assertEquals("Meduseld", meduseld.getBlueprint().getTitle());

		scn.MoveCompanionsToTable(gimli, aragorn, pippin, arwen, gandalf);
		scn.MoveCardsToDeadPile(ranger, legolas, sam, bounder);
		scn.MoveCardsToDiscard(knight);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.DEAD, ranger, legolas, sam, bounder);
		assertInDiscard(knight);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsActionAvailable(meduseld));

		scn.FreepsUseCardAction(meduseld);
		// Gandalf-signet companions of four cultures and races; not Gandalf or Frodo, who have the Frodo signet.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, aragorn, pippin, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, frodo));
		assertEquals(3, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(gimli, aragorn, arwen);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// Unbound companions of two cultures and races in the dead pile; not Sam (Ring-bound) or the Bounder (an
		// ally) there, nor the Knight, an unbound companion in the discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(ranger, legolas));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, bounder, knight));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(legolas);

		assertInZone(Zone.FREE_CHARACTERS, legolas);
		assertInZone(Zone.DEAD, ranger, sam, bounder);
		assertInDiscard(knight);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWhenOnlyTwoCompanionsWithTheGandalfSignetCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var gandalf = scn.GetFreepsCard("gandalf");
		var ranger = scn.GetFreepsCard("ranger");

		scn.StartGame();
		scn.SkipToSite(3);
		var meduseld = scn.GetCurrentSite();
		assertEquals("Meduseld", meduseld.getBlueprint().getTitle());

		scn.MoveCompanionsToTable(gimli, aragorn, pippin, gandalf);
		scn.MoveCardsToDeadPile(ranger);
		// Gimli, vitality 3, is exhausted; Gandalf could exert, but has the Frodo signet.
		scn.AddWoundsToChar(gimli, 2);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertInZone(Zone.DEAD, ranger);

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(meduseld));
	}

	@Test
	public void NotOfferedWithNoUnboundCompanionInTheDeadPileThatCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var arwen = scn.GetFreepsCard("arwen");
		var sam = scn.GetFreepsCard("sam");
		var guard = scn.GetFreepsCard("guard");
		var knight = scn.GetFreepsCard("knight");

		scn.StartGame();
		scn.SkipToSite(3);
		var meduseld = scn.GetCurrentSite();
		assertEquals("Meduseld", meduseld.getBlueprint().getTitle());

		// Three companions with the Gandalf signet can exert.  In the dead pile, Sam is Ring-bound and the Dwarf
		// Guard cannot be played with no Dwarf to spot; the Knight is in the discard pile.
		scn.MoveCompanionsToTable(aragorn, pippin, arwen);
		scn.MoveCardsToDeadPile(sam, guard);
		scn.MoveCardsToDiscard(knight);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.DEAD, sam, guard);
		assertInDiscard(knight);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(arwen));

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(meduseld));
		assertEquals(0, scn.GetWoundsOn(aragorn));
	}
}
