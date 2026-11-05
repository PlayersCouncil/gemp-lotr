package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_104_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("treebeard", "4_104");
					// In play, to be healed or not
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: an unbound Wizard
					put("gimli", "1_13");       // Gimli, Son of Gloin: an unbound Dwarf; a skirmish ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer; his abilities are not used
					// In hand, to be discarded or not
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound [gondor] Man
					put("merry", "1_302");      // Merry, Friend to Sam: an unbound [shire] Hobbit
					put("samcard", "1_311");    // a second Sam, held in hand: a Ring-bound companion card
					put("bounder", "1_286");    // Bounder: an ally card, which is no companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TreebeardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Treebeard, Oldest Living Thing
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Ally
		 * Subtype: Ent
		 * Strength: 12
		 * Vitality: 4
		 * Site Number: 2T & 8T
		 * Game Text: <b>Unhasty</b>.<br><b>Fellowship:</b> Exert Treebeard and discard an unbound companion from hand to heal an unbound companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("treebeard");

		assertEquals("Treebeard", card.getBlueprint().getTitle());
		assertEquals("Oldest Living Thing", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.ENT, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.UNHASTY));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.TWO_TOWERS, 2)));
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.TWO_TOWERS, 8)));
	}

	@Test
	public void FellowshipAbilityExertsTreebeardAndDiscardsAnUnboundCompanionFromHandToHealAnUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treebeard = scn.GetFreepsCard("treebeard");
		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var samcard = scn.GetFreepsCard("samcard");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveAlliesToTable(treebeard);
		scn.MoveCompanionsToTable(gandalf, gimli, sam);

		scn.StartGame();
		scn.MoveCardsToHand(aragorn, merry, samcard, bounder);
		scn.AddWoundsToChar(gandalf, 2);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(sam, 1);
		scn.AddWoundsToChar(frodo, 1);
		scn.FreepsRefreshDecision();

		assertInHand(aragorn, merry, samcard, bounder);
		assertEquals(0, scn.GetWoundsOn(treebeard));
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsActionAvailable(treebeard));
		scn.FreepsUseCardAction(treebeard);

		// The exertion, then the card from hand: the unbound companions, not the Ring-bound one or the ally.
		assertEquals(1, scn.GetWoundsOn(treebeard));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(samcard, bounder));
		scn.FreepsChooseCard(aragorn);
		assertInZone(Zone.DISCARD, aragorn);
		assertInHand(merry, samcard, bounder);

		// The heal: the wounded unbound companions, not Sam, not the Ring-bearer, not Treebeard, a wounded ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, frodo, treebeard));
		scn.FreepsChooseCard(gandalf);
		// One wound of two
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(treebeard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityIsNotOfferedWhileTreebeardIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treebeard = scn.GetFreepsCard("treebeard");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveAlliesToTable(treebeard);
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		scn.MoveCardsToHand(aragorn);
		scn.AddWoundsToChar(gandalf, 1);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(treebeard, 3);
		scn.FreepsRefreshDecision();

		assertInHand(aragorn);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(3, scn.GetWoundsOn(treebeard));
		assertFalse(scn.FreepsActionAvailable(treebeard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityIsNotOfferedWithNoUnboundCompanionInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treebeard = scn.GetFreepsCard("treebeard");
		var gandalf = scn.GetFreepsCard("gandalf");
		var samcard = scn.GetFreepsCard("samcard");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveAlliesToTable(treebeard);
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		// A Ring-bound companion and an ally in hand, and nothing else.
		scn.MoveCardsToHand(samcard, bounder);
		scn.AddWoundsToChar(gandalf, 1);
		scn.FreepsRefreshDecision();

		assertInHand(samcard, bounder);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(treebeard));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertFalse(scn.FreepsActionAvailable(treebeard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoWoundedUnboundCompanionTheCostsArePaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treebeard = scn.GetFreepsCard("treebeard");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveAlliesToTable(treebeard);
		scn.MoveCompanionsToTable(gandalf, sam);

		scn.StartGame();
		scn.MoveCardsToHand(merry);
		// Only Sam, who is Ring-bound, is wounded.
		scn.AddWoundsToChar(sam, 1);
		scn.FreepsRefreshDecision();

		assertInHand(merry);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsActionAvailable(treebeard));
		scn.FreepsUseCardAction(treebeard);

		// Both costs are paid, the lone card in hand unasked, and nothing is healed.
		assertEquals(1, scn.GetWoundsOn(treebeard));
		assertInZone(Zone.DISCARD, merry);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
