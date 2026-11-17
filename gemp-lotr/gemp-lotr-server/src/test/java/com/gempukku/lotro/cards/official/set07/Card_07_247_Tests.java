package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_247_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("herdsman", "7_247");
					put("hguard", "5_83");      // Household Guard: [rohan] Man companion, strength 6, vitality 3; Valiant is its whole text
					// Merry, Master Holbytla: a [rohan] Hobbit companion, no Man beside fewer than 3 [rohan] companions
					// and no Eowyn; his texts are used only on purpose
					put("merry", "103_85");
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man companion; his skirmish ability is not used
					// Guma, Plains Farmer: [rohan] Man ally, home 4T; his abilities are used only on purpose
					put("guma", "4_277");

					// Cards held in hand, of four types: a companion (Gondorian Knight), a possession (Hobbit Sword),
					// an event (Sorry About Everything) and a condition (A Talent for Not Being Seen)
					put("h1", "5_35");
					put("h2", "1_299");
					put("h3", "1_312");
					put("h4", "1_316");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RohirrimHerdsmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Rohirrim Herdsman
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Ally
		 * Subtype: Man
		 * Strength: 4
		 * Vitality: 2
		 * Site Number: 3T
		 * Game Text: <b>Villager</b>. To play, spot 2 [rohan] Men.<br><b>Regroup:</b> Discard 3 cards from hand to remove a threat.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("herdsman");

		assertEquals("Rohirrim Herdsman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VILLAGER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.TWO_TOWERS, 3)));
	}

	@Test
	public void CannotBePlayedUntilTwoRohanMenAreSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var herdsman = scn.GetFreepsCard("herdsman");
		var hguard = scn.GetFreepsCard("hguard");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(hguard, merry, boromir);
		scn.MoveCardsToHand(herdsman);

		scn.StartGame();

		// One [rohan] Man; Merry is a [rohan] Hobbit (two [rohan] companions make him no Man) and Boromir a
		// [gondor] Man.
		assertInHand(herdsman);
		assertFalse(scn.FreepsPlayAvailable(herdsman));

		// A [rohan] Man ally makes two.
		scn.MoveAlliesToTable(guma);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, guma);
		assertTrue(scn.FreepsPlayAvailable(herdsman));

		scn.FreepsPlayCard(herdsman);
		assertInZone(Zone.SUPPORT, herdsman);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupActionDiscardsThreeCardsFromHandToRemoveAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var herdsman = scn.GetFreepsCard("herdsman");
		var hguard = scn.GetFreepsCard("hguard");
		var h1 = scn.GetFreepsCard("h1");
		var h2 = scn.GetFreepsCard("h2");
		var h3 = scn.GetFreepsCard("h3");
		var h4 = scn.GetFreepsCard("h4");
		scn.MoveCompanionsToTable(hguard);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToHand(h1, h2, h3, h4);

		scn.StartGame();
		// Two threats want two companions: Frodo and the Guard.
		scn.AddThreats(2);

		// No minion is played, so the game goes from the Shadow phase straight to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(2, scn.GetThreats());
		assertInHand(h1, h2, h3, h4);
		assertTrue(scn.FreepsActionAvailable(herdsman));
		scn.FreepsUseCardAction(herdsman);

		// Any three cards: a companion, a possession, an event and a condition are all offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(h1, h2, h3, h4));
		assertEquals(3, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(h1, h2, h4);

		assertInDiscard(h1, h2, h4);
		assertInHand(h3);
		// One threat of the two.
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedWithTwoCardsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var herdsman = scn.GetFreepsCard("herdsman");
		var hguard = scn.GetFreepsCard("hguard");
		var h1 = scn.GetFreepsCard("h1");
		var h2 = scn.GetFreepsCard("h2");
		scn.MoveCompanionsToTable(hguard);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToHand(h1, h2);

		scn.StartGame();
		scn.AddThreats(2);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(2, scn.GetThreats());
		assertInHand(h1, h2);
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(herdsman));
	}
}
