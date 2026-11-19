package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_02_084_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nelya", "2_84");
					// Úlairë Lemenya, Lieutenant of Morgul: another Nazgûl, neither fierce nor damage +X; his text
					// reaches only a character bearing a [wraith] condition
					put("lemenya", "1_232");
					put("sam", "1_311");          // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer
					// Dear Friends: [shire] Fellowship event, cost 0; "Exert a companion to place a Free Peoples card
					// from your discard pile beneath your draw deck", with an empty discard pile here
					put("friends", "2_98");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireNelyaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Úlairë Nelya, Ringwraith in Twilight
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 2
		 * Game Text: <b>Twilight</b>.<br>While you can spot 2 burdens or 2 wounds on the Ring-bearer, Úlairë Nelya is <b>fierce</b> and <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nelya");

		assertEquals("Úlairë Nelya", card.getBlueprint().getTitle());
		assertEquals("Ringwraith in Twilight", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TWILIGHT));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(2, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void OneBurdenAndOneWoundOnTheRingBearerDoNotMakeHerFierceButASecondBurdenDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam);

		var nelya = scn.GetShadowCard("nelya");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		// Minions laid in the fellowship phase, to be read before and after the count changes.
		scn.MoveMinionsToTable(nelya, lemenya);
		scn.AddBurdens(1);
		scn.AddWoundsToChar(frodo, 1);
		// Two wounds on a companion who is not the Ring-bearer.
		scn.AddWoundsToChar(sam, 2);

		// One of each is two of neither.
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertFalse(scn.HasKeyword(nelya, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(nelya, Keyword.DAMAGE));

		scn.AddBurdens(1);

		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.HasKeyword(nelya, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(nelya, Keyword.DAMAGE));
		// Her text is about herself: the Nazgûl beside her gains neither.
		assertFalse(scn.HasKeyword(lemenya, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ASecondWoundOnTheRingBearerMakesHerFierceAndDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var friends = scn.GetFreepsCard("friends");
		scn.MoveCardsToHand(friends);

		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		scn.FreepsRefreshDecision();
		scn.AddWoundsToChar(frodo, 1);

		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertFalse(scn.HasKeyword(nelya, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(nelya, Keyword.DAMAGE));

		// Frodo is the only companion, and exerts for Dear Friends unasked.
		scn.FreepsPlayCard(friends);

		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.HasKeyword(nelya, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(nelya, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
