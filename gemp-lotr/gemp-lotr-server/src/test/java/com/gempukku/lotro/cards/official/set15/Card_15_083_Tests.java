package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_083_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("herdsman", "15_83");
					put("herdsman2", "15_83");
					put("hillman", "15_82");    // Grousing Hillman: another [men] minion, strength 9; his text needs a controlled site
					put("worker", "15_135");    // Rohan Worker: a Free Peoples follower that changes no number
					put("stablemaster", "15_134"); // Rohan Stable Master: a Free Peoples follower, borne here by Aragorn
					put("saruman", "17_38");    // Saruman, Servant of Sauron: the Shadow player's own follower
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the Stable Master's bearer
					put("maggot", "1_288");     // Farmer Maggot, Chaser of Rascals: an ally, the character neighbour of a follower
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HuntingHerdsmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Hunting Herdsman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Hunter 1</b>. <helper>(While skirmishing a non-hunter character, this character is strength +1.)</helper><br>This minion is strength +3 for each follower you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("herdsman");

		assertEquals("Hunting Herdsman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.HUNTER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsStrengthPlus3ForEachFollowerOfEitherPlayerInASupportAreaOrBorneAndNotForAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var herdsman = scn.GetShadowCard("herdsman");
		var herdsman2 = scn.GetShadowCard("herdsman2");
		var hillman = scn.GetShadowCard("hillman");
		var saruman = scn.GetShadowCard("saruman");
		var worker = scn.GetFreepsCard("worker");
		var stablemaster = scn.GetFreepsCard("stablemaster");
		var aragorn = scn.GetFreepsCard("aragorn");
		var maggot = scn.GetFreepsCard("maggot");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(maggot);

		scn.StartGame();
		scn.MoveMinionsToTable(herdsman, herdsman2, hillman);

		// No follower in play; the ally is no follower.
		assertInZone(Zone.SUPPORT, maggot);
		assertEquals(8, scn.GetStrength(herdsman));
		assertEquals(8, scn.GetStrength(herdsman2));
		assertEquals(9, scn.GetStrength(hillman));

		// A Free Peoples follower in its owner's support area.
		scn.MoveCardsToSupportArea(worker);
		assertInZone(Zone.SUPPORT, worker);
		assertEquals(8 + 3, scn.GetStrength(herdsman));

		// A Free Peoples follower borne by a companion.
		scn.AttachCardsTo(aragorn, stablemaster);
		assertAttachedTo(stablemaster, aragorn);
		assertEquals(8 + 6, scn.GetStrength(herdsman));

		// The Shadow player's own follower, in his support area.
		scn.MoveCardsToSupportArea(saruman);
		assertInZone(Zone.SUPPORT, saruman);
		assertEquals(8 + 9, scn.GetStrength(herdsman));

		// Each copy is made stronger by its own text alone (+9, not +18), and no other minion is.
		assertEquals(8 + 9, scn.GetStrength(herdsman2));
		assertEquals(9, scn.GetStrength(hillman));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AFollowerPlayedFromHandMakesHimStrengthPlus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var herdsman = scn.GetShadowCard("herdsman");
		var worker = scn.GetFreepsCard("worker");
		scn.MoveCardsToHand(worker);

		scn.StartGame();
		// Laid early: a minion in play later in the turn (Q-0228).
		scn.MoveMinionsToTable(herdsman);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.HAND, worker);
		assertEquals(8, scn.GetStrength(herdsman));

		scn.FreepsPlayCard(worker);

		assertInZone(Zone.SUPPORT, worker);
		assertEquals(8 + 3, scn.GetStrength(herdsman));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
