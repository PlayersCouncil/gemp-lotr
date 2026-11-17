package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_081_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ecglaf", "5_81");

					put("recruit", "18_103");   // Rohirrim Recruit: a valiant [rohan] Man companion; strength +2 only with a [rohan] token
					put("scout", "5_90");       // Rohirrim Scout: a valiant [rohan] Man companion; a regroup ability only
					// Aragorn, Last Hope of Men: a valiant [gondor] Man; an optional draw at the start of the turn, a skirmish ability
					put("aragorn", "102_16");
					// Merry, Master Holbytla: a valiant [rohan] Hobbit, a Man only while 3 [rohan] companions or Eowyn can be
					// spotted; his other texts answer another [rohan] Man's exertion or are used only on purpose
					put("merry", "103_85");
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man who is not valiant; a skirmish ability only
					put("sword", "4_291");      // Sword of Rohan: a [rohan] possession, hand weapon, on a [rohan] Man
					put("shield", "5_91");      // Rohirrim Shield: a [rohan] possession, shield, on a [rohan] Man
					put("slits", "5_80");       // Arrow-slits: a [rohan] condition, no possession
					put("arrow", "9_46");       // The Red Arrow: a [rohan] artifact, no possession
					put("armor", "1_92");       // Armor: a [gondor] possession, on a Man
					put("mount", "4_287");      // Rider's Mount: a [rohan] possession, the Shadow player's copy in his own discard pile
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EcglafStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Ecglaf, Courageous Farmer
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Ally
		 * Subtype: Man
		 * Strength: 4
		 * Vitality: 2
		 * Site Number: 4T
		 * Game Text: <b>Villager</b>.<br><b>Fellowship:</b> Exert Ecglaf and spot 2 valiant Men to play a [rohan] possession from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ecglaf");

		assertEquals("Ecglaf", card.getBlueprint().getTitle());
		assertEquals("Courageous Farmer", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VILLAGER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.TWO_TOWERS, 4)));
	}

	@Test
	public void FellowshipActionExertsEcglafToPlayARohanPossessionFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ecglaf = scn.GetFreepsCard("ecglaf");
		var recruit = scn.GetFreepsCard("recruit");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var shield = scn.GetFreepsCard("shield");
		var slits = scn.GetFreepsCard("slits");
		var arrow = scn.GetFreepsCard("arrow");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveAlliesToTable(ecglaf);
		scn.MoveCompanionsToTable(recruit);
		scn.MoveCardsToDiscard(sword, shield, slits, arrow, armor);

		var mount = scn.GetShadowCard("mount");
		scn.MoveCardsToDiscard(mount);
		assertInDiscard(sword, shield, slits, arrow, armor, mount);

		scn.StartGame();
		// Aragorn goes down after the start of the turn, where his own optional draw would be asked.
		scn.MoveCompanionsToTable(aragorn);
		scn.FreepsRefreshDecision();

		// The Recruit, a [rohan] companion, and Aragorn, a [gondor] one, are the two valiant Men.
		assertEquals(0, scn.GetWoundsOn(ecglaf));
		assertTrue(scn.FreepsActionAvailable(ecglaf));
		scn.FreepsUseCardAction(ecglaf);
		assertEquals(1, scn.GetWoundsOn(ecglaf));

		// [rohan] possessions of two classes; not a [rohan] condition, a [rohan] artifact, a [gondor] possession,
		// nor the Shadow player's [rohan] possession in his own discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sword, shield));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(slits, arrow, armor, mount));
		scn.FreepsChooseCard(sword);

		// The Sword of Rohan goes on a [rohan] Man.
		assertTrue(scn.FreepsHasCardChoiceAvailable(recruit, ecglaf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(recruit);

		assertAttachedTo(sword, recruit);
		assertInDiscard(shield, slits, arrow, armor, mount);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWithOneValiantManBesideAValiantHobbitAndAManWhoIsNotValiant() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ecglaf = scn.GetFreepsCard("ecglaf");
		var recruit = scn.GetFreepsCard("recruit");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveAlliesToTable(ecglaf);
		scn.MoveCompanionsToTable(recruit, merry, boromir);
		scn.MoveCardsToDiscard(sword);
		assertInDiscard(sword);

		scn.StartGame();
		// Merry is valiant and no Man (two [rohan] companions only); Boromir, and Ecglaf himself, are Men who are not valiant.
		assertTrue(scn.HasKeyword(merry, Keyword.VALIANT));
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertFalse(scn.HasKeyword(boromir, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(ecglaf, Keyword.VALIANT));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(ecglaf));
		assertEquals(0, scn.GetWoundsOn(ecglaf));
		assertInDiscard(sword);
	}

	@Test
	public void NotOfferedWhileEcglafIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ecglaf = scn.GetFreepsCard("ecglaf");
		var recruit = scn.GetFreepsCard("recruit");
		var scout = scn.GetFreepsCard("scout");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveAlliesToTable(ecglaf);
		scn.MoveCompanionsToTable(recruit, scout);
		scn.MoveCardsToDiscard(sword);
		// Vitality 2 with 1 wound: he cannot exert.
		scn.AddWoundsToChar(ecglaf, 1);
		assertEquals(1, scn.GetWoundsOn(ecglaf));
		assertInDiscard(sword);

		scn.StartGame();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(ecglaf));
		assertInDiscard(sword);
	}

	@Test
	public void NotOfferedWithNoRohanPossessionInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ecglaf = scn.GetFreepsCard("ecglaf");
		var recruit = scn.GetFreepsCard("recruit");
		var scout = scn.GetFreepsCard("scout");
		var slits = scn.GetFreepsCard("slits");
		var arrow = scn.GetFreepsCard("arrow");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveAlliesToTable(ecglaf);
		scn.MoveCompanionsToTable(recruit, scout);
		scn.MoveCardsToDiscard(slits, arrow, armor);

		var mount = scn.GetShadowCard("mount");
		scn.MoveCardsToDiscard(mount);
		assertInDiscard(slits, arrow, armor, mount);

		scn.StartGame();
		// Ecglaf can exert and two valiant Men are there: only the play from the discard pile cannot be made.
		assertEquals(0, scn.GetWoundsOn(ecglaf));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(ecglaf));
		assertInDiscard(slits, arrow, armor, mount);
	}
}
