package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_122_Tests
{

	// A Shadows-block path.  Fangorn Glade 11_239 ("site1") is a forest and no dwelling; Streets of Bree 18_140
	// is a dwelling whose only text is about followers.  A test starts at whichever of the two it needs.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hearth", "12_122");
					put("sam", "1_310");        // Sam, Faithful Companion: Ring-bound, not the Ring-bearer
					put("pippin", "1_306");     // Pippin, Friend to Frodo: a [shire] Hobbit companion
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally
					// Merry, Master Holbytla: a [rohan] Hobbit companion; his abilities are never used here
					put("merry", "103_85");
					// Goldberry, River-daughter: a [shire] ally who is no Hobbit; her texts need a river
					put("goldberry", "9_51");
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit, site 2; his abilities are never used
					put("ted", "18_75");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.ShadowsSites);
					remove("site9");
					put("streets", "18_140");   // Streets of Bree
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void HomeandHearthStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Home and Hearth
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each time you play a Hobbit, you may remove a burden. Then, if the fellowship is at dwelling site, heal the Ring-bearer.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hearth");

		assertEquals("Home and Hearth", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtADwellingEachHobbitYouPlayRemovesABurdenAndHealsTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hearth = scn.GetFreepsCard("hearth");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var merry = scn.GetFreepsCard("merry");
		var streets = scn.GetFreepsSite("streets");
		scn.MoveCardsToSupportArea(hearth);
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(pippin, bounder, merry);

		scn.StartGame(streets);
		scn.AddBurdens(3);
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(sam, 1);

		assertSame(streets, scn.GetCurrentSite());
		assertTrue(scn.HasKeyword(streets, Keyword.DWELLING));
		assertEquals(3, scn.GetBurdens());
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));

		// A [shire] Hobbit companion.
		scn.FreepsPlayCard(pippin);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hearth));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));

		// A [shire] Hobbit ally: each time, once more.
		scn.FreepsPlayCard(bounder);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hearth));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));

		// A [rohan] Hobbit companion.
		scn.FreepsPlayCard(merry);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hearth));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		// Sam is Ring-bound but not the Ring-bearer: never healed.
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningRemovesNoBurdenAndHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hearth = scn.GetFreepsCard("hearth");
		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var streets = scn.GetFreepsSite("streets");
		scn.MoveCardsToSupportArea(hearth);
		scn.MoveCardsToHand(pippin);

		scn.StartGame(streets);
		scn.AddBurdens(2);
		scn.AddWoundsToChar(frodo, 2);

		assertSame(streets, scn.GetCurrentSite());
		assertEquals(2, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(frodo));

		scn.FreepsPlayCard(pippin);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hearth));
		scn.FreepsDeclineOptionalTrigger();

		// The heal follows the burden ("Then"): declined, neither happens.
		assertEquals(2, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtADwellingWithNoBurdenLeftTheRingBearerIsStillHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hearth = scn.GetFreepsCard("hearth");
		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var streets = scn.GetFreepsSite("streets");
		scn.MoveCardsToSupportArea(hearth);
		scn.MoveCardsToHand(pippin);

		scn.StartGame(streets);
		scn.AddWoundsToChar(frodo, 2);

		assertSame(streets, scn.GetCurrentSite());
		assertTrue(scn.HasKeyword(streets, Keyword.DWELLING));
		assertEquals(0, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(frodo));

		// No burden to remove, but the heal can still be done: CR 4.2 "effect", as much as can be done.
		scn.FreepsPlayCard(pippin);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hearth));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AwayFromADwellingABurdenIsRemovedAndTheRingBearerIsNotHealed()throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hearth = scn.GetFreepsCard("hearth");
		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var glade = scn.GetFreepsSite("site1");
		var streets = scn.GetFreepsSite("streets");
		scn.MoveCardsToSupportArea(hearth);
		scn.MoveCardsToHand(pippin);

		scn.StartGame(glade);
		// The dwelling lies next on the path, and the fellowship is not at it.
		scn.MoveCardToAdventurePath(streets);
		scn.AddBurdens(2);
		scn.AddWoundsToChar(frodo, 2);

		assertSame(glade, scn.GetCurrentSite());
		assertFalse(scn.HasKeyword(glade, Keyword.DWELLING));
		assertSame(streets, scn.GetSite(2));
		assertEquals(2, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(frodo));

		scn.FreepsPlayCard(pippin);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hearth));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoBurdenAwayFromADwellingNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hearth = scn.GetFreepsCard("hearth");
		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var glade = scn.GetFreepsSite("site1");
		scn.MoveCardsToSupportArea(hearth);
		scn.MoveCardsToHand(pippin);

		scn.StartGame(glade);
		scn.AddWoundsToChar(frodo, 2);

		assertSame(glade, scn.GetCurrentSite());
		assertEquals(0, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(frodo));

		scn.FreepsPlayCard(pippin);

		// No burden to remove and no dwelling: the trigger would do nothing, and is not offered.
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(hearth));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayingAnAllyWhoIsNoHobbitOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hearth = scn.GetFreepsCard("hearth");
		var frodo = scn.GetRingBearer();
		var goldberry = scn.GetFreepsCard("goldberry");
		var streets = scn.GetFreepsSite("streets");
		scn.MoveCardsToHand(hearth, goldberry);

		scn.StartGame(streets);
		scn.AddBurdens(2);
		scn.AddWoundsToChar(frodo, 2);

		assertSame(streets, scn.GetCurrentSite());
		assertEquals(2, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(frodo));

		// Its own play is no Hobbit's.
		scn.FreepsPlayCard(hearth);
		assertInZone(Zone.SUPPORT, hearth);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(hearth));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A [shire] ally who is no Hobbit, at the dwelling, with a burden to remove and a wound to heal.
		scn.FreepsPlayCard(goldberry);

		assertInZone(Zone.SUPPORT, goldberry);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(hearth));
		assertEquals(2, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AHobbitPlayedByTheShadowPlayerOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hearth = scn.GetFreepsCard("hearth");
		var frodo = scn.GetRingBearer();
		var ted = scn.GetShadowCard("ted");
		var streets = scn.GetFreepsSite("streets");
		scn.MoveCardsToSupportArea(hearth);
		scn.MoveCardsToHand(ted);

		scn.StartGame(streets);
		scn.AddBurdens(2);
		scn.AddWoundsToChar(frodo, 2);
		scn.SetTwilight(5);

		scn.FreepsPass();
		// The fellowship moves, and the Shadow player lays his Abandoned Mine Shaft as site 2.
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site2"));

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(frodo));

		// The Shadow player plays a Hobbit minion on the Free Peoples player's turn: he is not "you".
		scn.ShadowPlayCard(ted);

		assertInZone(Zone.SHADOW_CHARACTERS, ted);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(hearth));
		assertEquals(2, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
