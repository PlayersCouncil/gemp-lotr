package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_079_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("herdsman", "15_79");
					put("hunting", "15_83");    // Hunting Herdsman: [men] Man minion printed hunter 1; +3 per follower, and none is laid
					put("wildman", "15_91");    // Ravaging Wild Man: [men] Man minion who gains hunter 1 while a hunter can be spotted
					put("tracking", "15_171");  // Tracking Uruk: [uruk-hai] minion, hunter 1 is its whole text
					put("prowler1", "15_61");   // Gondorian Prowler: [gondor] Man companion, hunter 1
					put("prowler2", "15_61");   // a second copy, played from hand to tip the count
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion with no text in the way
					put("gimli", "15_5");       // Gimli, Eager Hunter: a companion, damage +1 and hunter 3
					put("barliman", "1_70");    // Barliman Butterbur: an ally, who is no companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EnragedHerdsmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Enraged Herdsman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: While you can spot more companions than minions, each [men] hunter is strength +2 and is an <b>archer</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("herdsman");

		assertEquals("Enraged Herdsman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhileCompanionsOutnumberMinionsEachMenHunterIsStrengthPlus2AndAnArcher() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var herdsman = scn.GetShadowCard("herdsman");
		var hunting = scn.GetShadowCard("hunting");
		var wildman = scn.GetShadowCard("wildman");
		var tracking = scn.GetShadowCard("tracking");
		var frodo = scn.GetRingBearer();
		var prowler1 = scn.GetFreepsCard("prowler1");
		var prowler2 = scn.GetFreepsCard("prowler2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(prowler1, aragorn, gimli);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(prowler2);

		scn.StartGame();
		scn.MoveMinionsToTable(herdsman, hunting, wildman, tracking);
		scn.FreepsRefreshDecision();

		// Four companions (Frodo, the Prowler, Aragorn, Gimli) against four minions: not more. The ally
		// would make five if he were counted.
		assertInPlay(frodo, prowler1, aragorn, gimli, barliman);
		assertInHand(prowler2);
		assertTrue(scn.HasKeyword(wildman, Keyword.HUNTER));
		assertEquals(8, scn.GetStrength(hunting));
		assertEquals(10, scn.GetStrength(wildman));
		assertEquals(14, scn.GetStrength(tracking));
		assertEquals(12, scn.GetStrength(herdsman));
		assertEquals(5, scn.GetStrength(prowler1));
		assertFalse(scn.HasKeyword(hunting, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(wildman, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(tracking, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(herdsman, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(prowler1, Keyword.ARCHER));

		scn.FreepsPlayCard(prowler2);

		// Five companions against four minions. Both [men] hunters, the printed one and the one whose hunter
		// is gained, are +2 and archers; the [uruk-hai] hunter, the [gondor] hunter and the Herdsman himself,
		// a [men] Man who is no hunter, are not.
		assertInPlay(prowler2);
		assertEquals(10, scn.GetStrength(hunting));
		assertEquals(12, scn.GetStrength(wildman));
		assertEquals(14, scn.GetStrength(tracking));
		assertEquals(12, scn.GetStrength(herdsman));
		assertEquals(5, scn.GetStrength(prowler1));
		assertEquals(5, scn.GetStrength(prowler2));
		assertTrue(scn.HasKeyword(hunting, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(wildman, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(tracking, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(herdsman, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(prowler1, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(prowler2, Keyword.ARCHER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
