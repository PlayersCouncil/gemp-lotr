package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_108_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nostranger", "1_108");
					put("nostranger2", "1_108");
					put("nostranger3", "1_108");
					put("nostranger4", "1_108");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man ranger, a maneuver ability used only on purpose
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf ranger; her text needs a Nazgul
					put("osgiliath", "7_116");  // Ranger of Osgiliath: [gondor] Man ranger, not unique; a regroup ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man and no ranger, cost 3; a skirmish ability used only on purpose
					put("saga", "1_114");       // The Saga of Elendil: [gondor] condition borne by a [gondor] companion; a skirmish ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: minion of cost 6 with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetKingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nostranger", "1_108");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man ranger, a maneuver ability used only on purpose
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NoStrangerToTheShadowsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: No Stranger to the Shadows
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Condition
		 * Game Text: Bearer must be a ranger. Limit 1 per ranger.
		 * Each site's Shadow number is -1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nostranger");

		assertEquals("No Stranger to the Shadows", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnARanger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nostranger = scn.GetFreepsCard("nostranger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, arwen, boromir);
		scn.MoveCardsToHand(nostranger);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(nostranger));
		scn.FreepsPlayCard(nostranger);

		// Rangers of two cultures and races; Boromir is a [gondor] Man with no ranger.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(arwen);

		assertAttachedTo(nostranger, arwen);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARangerBearsOnlyOneAndEachCopyLowersTheShadowNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nostranger = scn.GetFreepsCard("nostranger");
		var nostranger2 = scn.GetFreepsCard("nostranger2");
		var nostranger3 = scn.GetFreepsCard("nostranger3");
		var nostranger4 = scn.GetFreepsCard("nostranger4");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var osgiliath = scn.GetFreepsCard("osgiliath");
		var boromir = scn.GetFreepsCard("boromir");
		var saga = scn.GetFreepsCard("saga");
		scn.MoveCompanionsToTable(aragorn, arwen, osgiliath, boromir);
		// Another [gondor] condition on a ranger does not count against the limit.
		scn.AttachCardsTo(osgiliath, saga);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToHand(nostranger, nostranger2, nostranger3, nostranger4);
		scn.FreepsRefreshDecision();

		// Ettenmoors, Shadow number 2.
		var ettenmoors = scn.GetSite(2);
		assertEquals(2, scn.GetTwilightCost(ettenmoors));

		scn.FreepsPlayCard(nostranger);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, arwen, osgiliath));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir));
		scn.FreepsChooseCard(aragorn);
		assertAttachedTo(nostranger, aragorn);
		assertEquals(1, scn.GetTwilightCost(ettenmoors));

		// The ranger who has one is left out; the ranger with nothing and the ranger with the Saga are not.
		assertTrue(scn.FreepsPlayAvailable(nostranger2));
		scn.FreepsPlayCard(nostranger2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, osgiliath));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, boromir));
		scn.FreepsChooseCard(osgiliath);
		assertAttachedTo(nostranger2, osgiliath);
		assertAttachedTo(saga, osgiliath);
		// Each copy takes 1 of its own.
		assertEquals(0, scn.GetTwilightCost(ettenmoors));

		// One ranger left without: no choice is put.
		assertTrue(scn.FreepsPlayAvailable(nostranger3));
		scn.FreepsPlayCard(nostranger3);
		assertAttachedTo(nostranger3, arwen);

		// Three rangers in play, each with one: the fourth copy has no bearer.
		assertInZone(Zone.HAND, nostranger4);
		assertFalse(scn.FreepsPlayAvailable(nostranger4));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachSiteIsOneLessOnceItIsPlayedAndNoneIsBelowZero() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nostranger = scn.GetFreepsCard("nostranger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetFreepsHand(nostranger, boromir);
		scn.SetShadowHand(troop);
		scn.FreepsRefreshDecision();

		// Bag End 0 and Ettenmoors 2, behind the fellowship; Rivendell Valley 0, where it is.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetTwilightCost(scn.GetSite(1)));
		assertEquals(2, scn.GetTwilightCost(scn.GetSite(2)));
		assertEquals(0, scn.GetTwilightCost(scn.GetSite(3)));
		// Cards that are no sites: what each would cost to play.
		assertEquals(3, scn.GetTwilightCost(boromir));
		assertEquals(6, scn.GetTwilightCost(troop));

		scn.FreepsPlayCard(nostranger);
		assertAttachedTo(nostranger, aragorn);

		// A site the fellowship is not at is one less; 0 - 1 is counted as 0 (CR 4.2 "modifier").
		assertEquals(0, scn.GetTwilightCost(scn.GetSite(1)));
		assertEquals(1, scn.GetTwilightCost(scn.GetSite(2)));
		assertEquals(0, scn.GetTwilightCost(scn.GetSite(3)));
		assertEquals(3, scn.GetTwilightCost(boromir));
		assertEquals(6, scn.GetTwilightCost(troop));

		scn.SetTwilight(0);
		scn.FreepsPass();

		// Balin's Tomb, Shadow number 3.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetTwilightCost(scn.GetSite(4)));
		assertEquals(1, scn.GetTwilightCost(scn.GetSite(2)));
		// 2 companions + 3 for the site - 1
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASiteOfAnotherBlockIsOneLessToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKingScenario();

		var nostranger = scn.GetFreepsCard("nostranger");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(nostranger);

		scn.StartGame();

		// Aragorn is the only ranger, so the bearer is not asked for.
		scn.FreepsPlayCard(nostranger);
		assertAttachedTo(nostranger, aragorn);

		scn.FreepsPass();

		// King's Tent, a King-block site with Shadow number 1.
		var tent = scn.GetSite(2);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, tent.getBlueprint().getTwilightCost());
		assertEquals(0, scn.GetTwilightCost(tent));
		// 2 companions + 1 for the site - 1
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
