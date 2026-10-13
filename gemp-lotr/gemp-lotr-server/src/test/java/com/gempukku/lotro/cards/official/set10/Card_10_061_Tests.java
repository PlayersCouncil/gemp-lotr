package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_10_061_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "10_61");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A [shire] ally at his home site and a [shire] companion, each to skirmish a [wraith] minion, with a [shire]
	// card in the dead pile.
	protected VirtualTableScenario GetAllyAndCompanionScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("houses1", "10_61");
					put("houses2", "10_61");
					put("nelya", "2_84");
					put("lemenya", "1_232");
					put("bounder", "1_286");
					put("sam", "1_311");
					put("pippin", "1_306");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HousesofLamentationStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Houses of Lamentation
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a [wraith] minion strength +2 (or +4 if skirmishing a companion of the same culture as a card in the dead pile).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Houses of Lamentation", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// "(or +4 if skirmishing a companion of the same culture as a card in the dead pile)": the definition gave +4
	// against an ally of that culture as well.
	@Test
	public void TheLargerBonusIsForSkirmishingACompanionAndNotAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyAndCompanionScenario();

		var bounder = scn.GetFreepsCard("bounder");
		var sam = scn.GetFreepsCard("sam");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveAlliesToTable(bounder);
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToDeadPile(pippin);

		var houses1 = scn.GetShadowCard("houses1");
		var houses2 = scn.GetShadowCard("houses2");
		var nelya = scn.GetShadowCard("nelya");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(houses1, houses2);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, lemenya);
		scn.SkipToAssignments();
		// Site 2 is Bounder's home.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{bounder, nelya}, new PhysicalCardImpl[]{sam, lemenya});

		// Bounder is [shire], as the Pippin in the dead pile is, and an ally: +2.
		scn.FreepsResolveSkirmish(bounder);
		scn.FreepsPass();
		scn.ShadowPlayCard(houses1);
		scn.ShadowChooseCard(nelya);
		assertEquals(12, scn.GetStrength(nelya));
		assertEquals(9, scn.GetStrength(lemenya));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// Sam is a [shire] companion: +4.
		scn.FreepsResolveSkirmish(sam);
		scn.FreepsPass();
		scn.ShadowPlayCard(houses2);
		scn.ShadowChooseCard(lemenya);
		assertEquals(13, scn.GetStrength(lemenya));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void HousesofLamentationTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(1, scn.GetTwilight());
	}
}
