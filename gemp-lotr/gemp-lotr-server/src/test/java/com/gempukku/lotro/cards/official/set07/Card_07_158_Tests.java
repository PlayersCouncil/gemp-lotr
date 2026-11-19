package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_158_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rout", "7_158");
					put("explorer", "4_250");   // Southron Explorer: a [raider] Southron Man; a regroup ability used only on purpose
					// Easterling Axeman: a [raider] Easterling Man; strength +3 and fierce only at 2 burdens
					put("axeman", "4_224");
					// Ulaire Attea, Khamul of the East: a [raider] minion who is a Nazgul and no Man; his text needs 3 burdens
					put("khamul", "103_61");
					put("brigand", "4_10");     // Dunlending Brigand: a Man who is not [raider]; no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, vitality 3
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion, vitality 3; an archery ability used only on purpose
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a Dwarf ally, vitality 3: a character who is no companion
					// Cards for the Free Peoples hand, which decides initiative: Goblin Patrol Troop, no text.
					put("troop1", "1_177");
					put("troop2", "1_177");
					put("troop3", "1_177");
					put("troop4", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RoutStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Rout
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot a [raider] Man.<br>Each time the fellowship moves, the Free Peoples player must exert 2 companions, if you have initiative.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rout");

		assertEquals("Rout", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedToTheSupportAreaWithASouthronToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rout = scn.GetShadowCard("rout");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveCardsToHand(rout);
		assertInHand(rout);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(rout));
		scn.ShadowPlayCard(rout);
		assertInZone(Zone.SUPPORT, rout);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilARaiderManIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var rout = scn.GetShadowCard("rout");
		var axeman = scn.GetShadowCard("axeman");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToHand(rout, axeman);
		assertInHand(rout, axeman);

		scn.StartGame();
		// A [raider] minion who is no Man, a Man who is not [raider], and the Free Peoples player's own Man.
		scn.MoveMinionsToTable(khamul, brigand);
		// With (2) left over once the Axeman is paid for: 2 set + 2 companions + 2 for the site, less 2 + 2 roaming.
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(rout));

		// An Easterling is a [raider] Man.
		scn.ShadowPlayCard(axeman);
		assertInZone(Zone.SHADOW_CHARACTERS, axeman);
		assertTrue(scn.ShadowPlayAvailable(rout));

		scn.ShadowPlayCard(rout);
		assertInZone(Zone.SUPPORT, rout);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachMoveWhileTheShadowHasInitiativeMakesTheFreePeoplesPlayerExertTwoCompanionsOfHisChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(aragorn, gimli, legolas);
		scn.MoveAlliesToTable(grimir);
		// Three cards in hand: the Free Peoples player has no initiative, so the Shadow has it.
		scn.MoveCardsToHand(scn.GetFreepsCard("troop1"), scn.GetFreepsCard("troop2"), scn.GetFreepsCard("troop3"));
		assertEquals(3, scn.GetFreepsHandCount());

		var rout = scn.GetShadowCard("rout");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToSupportArea(rout);

		scn.StartGame();
		// Vitality 3 with 2 wounds: Legolas is exhausted and cannot exert.
		scn.AddWoundsToChar(legolas, 2);
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertInZone(Zone.SUPPORT, rout);
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(grimir));

		// The move that ends the fellowship phase.  The Free Peoples player chooses two: any companion who can
		// exert, the Ring-bearer included; not the exhausted Legolas, nor an ally.
		scn.FreepsPass();
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas, grimir));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(aragorn, gimli);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The regroup move, with a minion of the Shadow phase still on the table: the condition answers it again.
		scn.MoveMinionsToTable(brigand);
		scn.SkipToMovementDecision();
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsChooseToMove();

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas, grimir, brigand));
		scn.FreepsChooseCards(frodo, aragorn);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithFourCardsInTheFreePeoplesHandAMoveExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		// Four cards in hand: the Free Peoples player has initiative.
		scn.MoveCardsToHand(scn.GetFreepsCard("troop1"), scn.GetFreepsCard("troop2"),
				scn.GetFreepsCard("troop3"), scn.GetFreepsCard("troop4"));

		var rout = scn.GetShadowCard("rout");
		scn.MoveCardsToSupportArea(rout);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, rout);
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));

		scn.FreepsPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StayingAtTheRegroupDecisionExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var rout = scn.GetShadowCard("rout");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToSupportArea(rout);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, rout);
		assertFalse(scn.FreepsHasInitiative());
		scn.FreepsPass();
		scn.FreepsChooseCards(aragorn, gimli);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));

		scn.MoveMinionsToTable(brigand);
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertFalse(scn.FreepsHasInitiative());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The fellowship stays: the turn ends with nobody exerted, and the other player's begins.
		scn.FreepsChooseToStay();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
