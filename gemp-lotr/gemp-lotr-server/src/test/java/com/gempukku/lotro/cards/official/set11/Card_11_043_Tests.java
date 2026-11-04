package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_043_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("strong", "11_43");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, resistance 6; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] Dwarf, resistance 6, vitality 3; a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound by rule, resistance 5; abilities used only on purpose
					// Elven Scout: [elven] Elf, resistance 7; muster only while another Elf can be spotted (none here)
					put("scout", "11_18");
					// Thrarin, Dwarven Smith: Dwarf ally (resistance 0 by rule); a maneuver ability used only on purpose
					put("thrarin", "1_27");
					// Gollum, Nasty Treacherous Creature: [gollum] Gollum; a regroup ability only
					put("gollum", "5_24");
					// Gollum, Half a Wraith Himself: [wraith] Gollum; its trigger answers a Nazgûl played and its
					// assignment ability is for the assignment phase
					put("wraithgollum", "103_66");
					// Shelob, Last Child of Ungoliant: a [gollum] minion who is no Gollum; enduring, fierce
					put("shelob", "8_26");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HorriblyStrongStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Horribly Strong
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Discard Gollum from play to wound an unbound companion who has resistance 6 or less.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("strong");

		assertEquals("Horribly Strong", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsGollumToWoundAnUnboundCompanionOfResistance6OrLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var scout = scn.GetFreepsCard("scout");
		var thrarin = scn.GetFreepsCard("thrarin");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, gimli, sam, scout);
		scn.MoveAlliesToTable(thrarin);
		// Vitality 3 with 2 wounds: exhausted, so a wound kills him.
		scn.AddWoundsToChar(gimli, 2);

		var strong = scn.GetShadowCard("strong");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToHand(strong);

		scn.StartGame();
		assertInHand(strong);
		assertEquals(2, scn.GetWoundsOn(gimli));
		scn.MoveMinionsToTable(gollum);
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(gimli));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(7, scn.GetResistance(scout));
		assertEquals(0, scn.GetResistance(thrarin));
		assertEquals(10, scn.GetResistance(frodo));
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(strong));
		scn.ShadowPlayCard(strong);
		assertInDiscard(gollum);
		// Two unbound companions of resistance 6, of two cultures and races; not Sam, who is Ring-bound, nor the
		// Elven Scout, of resistance 7, nor Thrarin, an ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sam, scout, thrarin, frodo));
		scn.ShadowChooseCard(gimli);

		assertInZone(Zone.DEAD, gimli);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInDiscard(strong);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheWraithGollumPaysTheCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var strong = scn.GetShadowCard("strong");
		var wraithgollum = scn.GetShadowCard("wraithgollum");
		scn.MoveCardsToHand(strong);

		scn.StartGame();
		assertInHand(strong);
		scn.MoveMinionsToTable(wraithgollum);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowPlayAvailable(strong));
		scn.ShadowPlayCard(strong);
		assertInDiscard(wraithgollum, strong);
		// Aragorn is the only one it may wound, so nobody is asked.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotPlayableWithGollumInHandAndShelobInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var strong = scn.GetShadowCard("strong");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCardsToHand(strong, gollum);

		scn.StartGame();
		assertInHand(strong, gollum);
		scn.MoveMinionsToTable(shelob);
		scn.FreepsPass();

		// A [gollum] minion in play who is no Gollum, and Gollum not in play: the cost cannot be paid.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(strong));
		assertInZone(Zone.SHADOW_CHARACTERS, shelob);
	}

	@Test
	public void WithNoUnboundCompanionOfResistance6OrLessGollumIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout = scn.GetFreepsCard("scout");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(scout);

		var strong = scn.GetShadowCard("strong");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToHand(strong);

		scn.StartGame();
		assertInHand(strong);
		scn.MoveMinionsToTable(gollum);
		// Frodo, the Ring-bearer, 10; the Elven Scout 7.
		assertEquals(7, scn.GetResistance(scout));
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(strong));
		scn.ShadowPlayCard(strong);
		assertInDiscard(gollum, strong);
		assertEquals(0, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
