package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_13_146_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "13_146");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Hobbiton Woods 2_116 as site 1: a forest ("Fellowship: Exert a Hobbit to play your
	// site 2", used only on purpose), the home of Farmer Maggot (1F).  Site 2, where the skirmishes are, is
	// Ettenmoors 1_331, a plains and the home of Bounder (2F).
	protected VirtualTableScenario GetWoodsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bones", "13_146");
					put("maggot", "1_288");     // Farmer Maggot, Chaser of Rascals: Hobbit ally, home 1F, strength 2; a fellowship ability used only on purpose
					put("bounder", "1_286");    // Bounder: Hobbit ally, home 2F; a skirmish ability used only on purpose
					put("rosie", "1_309");      // Rosie Cotton, Hobbiton Lass: Hobbit ally, home 1F, strength 1; "Sam is strength +1", and a fellowship ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site1", "2_116");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Trollshaw Forest 1_334 as site 2, where the skirmishes are: a forest ("Each time you
	// play a possession or artifact on your companion, draw a card").  Site 1, the home of Rosie Cotton (1F), is Bag
	// End 1_319, no dwelling and no forest.
	protected VirtualTableScenario GetForestScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bones", "13_146");
					put("maggot", "1_288");     // Farmer Maggot, Chaser of Rascals: Hobbit ally, home 1F, strength 2; a fellowship ability used only on purpose
					put("bounder", "1_286");    // Bounder: Hobbit ally, home 2F; a skirmish ability used only on purpose
					put("rosie", "1_309");      // Rosie Cotton, Hobbiton Lass: Hobbit ally, home 1F, strength 1; "Sam is strength +1", and a fellowship ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "1_334");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EverythingbutMyBonesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Everything but My Bones
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: You may remove from the game 4 other [shire] cards in your discard pile to play this event from your discard pile. Then remove this event from the game.<br>Make your Hobbit at a dwelling or forest site strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Everything but My Bones", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void EverythingbutMyBonesTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(2, scn.GetTwilight());
	}

	@Test
	public void AnAllyAtHisHomeSiteAForestIsTheOneHobbitAtAForest() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWoodsScenario();

		var bones = scn.GetFreepsCard("bones");
		var frodo = scn.GetRingBearer();
		var maggot = scn.GetFreepsCard("maggot");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveAlliesToTable(maggot, bounder);
		scn.MoveCardsToHand(bones);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		assertEquals("Ettenmoors", scn.GetCurrentSite().getBlueprint().getTitle());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertTrue(scn.HasKeyword(scn.GetSite(1), Keyword.FOREST));
		scn.FreepsAssignToMinions(frodo, troop);
		scn.FreepsResolveSkirmish(frodo);
		int frodoStrength = scn.GetStrength(frodo);
		int bounderStrength = scn.GetStrength(bounder);
		assertEquals(2, scn.GetStrength(maggot));

		// Farmer Maggot is at his home site, Hobbiton Woods (CR 4.2 "ally"), a forest though the fellowship is not
		// at one: the only Hobbit at a forest, chosen unasked.  Frodo is at Ettenmoors with the fellowship, and
		// Bounder at his home site, Ettenmoors.
		assertTrue(scn.FreepsPlayAvailable(bones));
		scn.FreepsPlayCard(bones);
		assertEquals(5, scn.GetStrength(maggot));
		assertEquals(frodoStrength, scn.GetStrength(frodo));
		assertEquals(bounderStrength, scn.GetStrength(bounder));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnAllyWhoseHomeIsNoForestIsNotChosenWithTheFellowshipAtAForest() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetForestScenario();

		var bones = scn.GetFreepsCard("bones");
		var frodo = scn.GetRingBearer();
		var rosie = scn.GetFreepsCard("rosie");
		scn.MoveAlliesToTable(rosie);
		scn.MoveCardsToHand(bones);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		assertEquals("Trollshaw Forest", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		scn.FreepsAssignToMinions(frodo, troop);
		scn.FreepsResolveSkirmish(frodo);
		int frodoStrength = scn.GetStrength(frodo);

		// Frodo is at the forest with the fellowship; Rosie at her home site, Bag End (CR 4.2 "ally"): Frodo is
		// chosen unasked.
		scn.FreepsPlayCard(bones);
		assertEquals(frodoStrength + 3, scn.GetStrength(frodo));
		assertEquals(1, scn.GetStrength(rosie));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
