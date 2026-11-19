package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_165_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("habits", "11_165");
					put("habits2", "11_165");
					put("habits3", "11_165");
					put("habits4", "11_165");
					// Pippin, Trollslayer: a [gondor] Hobbit companion (to play, spot a [gondor] companion: cheated in);
					// a skirmish ability used only on purpose.
					put("pippin", "103_31");
					// Farmer Maggot, Chaser of Rascals: a [shire] Hobbit ally; a fellowship ability used only on purpose.
					put("maggot", "1_288");
					// Smeagol, Once Like Us: a [shire] companion who is no Hobbit; an assignment ability only.
					put("smeagol", "103_116");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion
					put("sam", "1_311");        // Sam, Son of Hamfast: a [shire] Hobbit companion; abilities used only on purpose
					put("merry", "1_302");      // Merry, Friend to Sam: a [shire] Hobbit companion; a skirmish ability only
					// There and Back Again: another [shire] condition on a Hobbit companion; a maneuver ability only.
					put("tale", "1_317");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					// Site 1 is Bag End, which has no terrain.
					put("site2", "1_329");      // Breeland Forest: a forest, Shadow number 1; "Stealth events may not be played"
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A forest at site 1, then a dwelling laid after it.
	protected VirtualTableScenario GetWoodsAndBreeScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("habits", "11_165");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					// Hobbiton Woods: a forest with no other keyword, Shadow number 0; a fellowship ability used only on purpose.
					put("site1", "2_116");
					// Streets of Bree: a Shadows-block dwelling with no other keyword, Shadow number 0, laid on the path by
					// MoveCardToAdventurePath as site 2; a bar on transferring followers.
					put("bree", "18_140");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A forest that is a sanctuary, then a dwelling that is a river laid after it.
	protected VirtualTableScenario GetWaterfallAndValleyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("habits", "11_165");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					// Rivendell Waterfall: a forest and a sanctuary, Shadow number 0; a move-limit text for a ranger.
					put("site3", "1_342");
					// Valley of the Silverlode: a Shadows-block dwelling and river, Shadow number 3, laid on the path by
					// MoveCardToAdventurePath as site 4; "When your fellowship moves to this site, you may heal each
					// Hobbit companion" (nobody is wounded).
					put("valley", "11_261");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HabitsofHomeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Habits of Home
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 4
		 * Type: Condition
		 * Subtype: 
		 * Game Text: <b>Toil 2</b>. <helper>(For each [shire] character you exert when playing this, its twilight cost is -2.)</helper>
		 * Bearer must be a Hobbit. Limit 1 per bearer.
		 * Each time the fellowship moves from a dwelling or forest site, remove (1).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("habits");

		assertEquals("Habits of Home", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TOIL));
		assertEquals(2, scn.GetKeywordCount(card, Keyword.TOIL));
		assertEquals(4, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnAHobbitOfAnyCultureCompanionOrAllyAndEachShireCharacterExertedForToilTakesTwoOffItsCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var habits = scn.GetFreepsCard("habits");
		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var maggot = scn.GetFreepsCard("maggot");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(pippin, smeagol, aragorn);
		scn.MoveAlliesToTable(maggot);
		scn.MoveCardsToHand(habits);
		assertInHand(habits);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(habits));
		scn.FreepsPlayCard(habits);

		// The Ring-bearer, a [gondor] Hobbit and a Hobbit ally; not a [shire] companion who is no Hobbit, nor a Man.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, pippin, maggot));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(smeagol, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(maggot);

		// Toil: any [shire] character may exert, Hobbit or not (Smeagol), ally or not; not the [gondor] ones.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, maggot, smeagol));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, aragorn));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		assertEquals(0, scn.GetWoundsOn(smeagol));
		scn.FreepsChooseCards(smeagol);

		assertAttachedTo(habits, maggot);
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(frodo));
		// 4 - 2 for Smeagol's exertion
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AHobbitWhoBearsOneIsNotOfferedForAnotherAndWithEveryHobbitBearingOneItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var habits = scn.GetFreepsCard("habits");
		var habits2 = scn.GetFreepsCard("habits2");
		var habits3 = scn.GetFreepsCard("habits3");
		var habits4 = scn.GetFreepsCard("habits4");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var tale = scn.GetFreepsCard("tale");
		scn.MoveCompanionsToTable(sam, merry);
		scn.AttachCardsTo(sam, habits);
		scn.AttachCardsTo(frodo, tale);
		scn.MoveCardsToHand(habits2, habits3, habits4);
		assertAttachedTo(habits, sam);
		assertAttachedTo(tale, frodo);
		assertInHand(habits2, habits3, habits4);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(habits2));
		scn.FreepsPlayCard(habits2);
		// Sam bears a copy already; Frodo bears another [shire] condition, which does not count, and Merry nothing.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam));
		scn.FreepsChooseCard(merry);
		scn.FreepsDeclineChoosing();
		assertAttachedTo(habits2, merry);

		// Frodo is the one Hobbit left without a copy.
		assertTrue(scn.FreepsPlayAvailable(habits3));
		scn.FreepsPlayCard(habits3);
		scn.FreepsDeclineChoosing();
		assertAttachedTo(habits3, frodo);

		// Every Hobbit bears one: the fourth copy cannot be played.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(habits4));
		assertInHand(habits4);
	}

	@Test
	public void MovingToAForestFromASiteOfNeitherRemovesNothingAndMovingFromItRemovesOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var habits = scn.GetFreepsCard("habits");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(frodo, habits);
		assertAttachedTo(habits, frodo);

		scn.StartGame();
		scn.SetTwilight(4);
		assertEquals(4, scn.GetTwilight());

		// From Bag End, which has no terrain, to Breeland Forest.
		scn.FreepsPass();
		assertSame(scn.GetFreepsSite("site2").getBlueprint(), scn.GetCurrentSite().getBlueprint());
		// 4 + 1 (Breeland Forest's Shadow number) + 2 companions: nothing removed
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// No minion: on to the regroup phase, and the regroup move from Breeland Forest to Rivendell Valley.
		scn.SkipToMovementDecision();
		scn.SetTwilight(4);
		assertEquals(4, scn.GetTwilight());
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		// 4 - 1 + 0 (Rivendell Valley's Shadow number) + 2 companions
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachMoveFromAForestAndFromADwellingRemovesOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWoodsAndBreeScenario();

		var habits = scn.GetFreepsCard("habits");
		var frodo = scn.GetRingBearer();
		var bree = scn.GetShadowSite("bree");
		scn.AttachCardsTo(frodo, habits);
		assertAttachedTo(habits, frodo);

		scn.StartGame();
		assertEquals("Hobbiton Woods", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveCardToAdventurePath(bree);
		scn.SetTwilight(4);
		assertEquals(4, scn.GetTwilight());

		// From Hobbiton Woods, a forest, to Streets of Bree.
		scn.FreepsPass();
		assertSame(bree, scn.GetCurrentSite());
		// 4 - 1 + 0 (Streets of Bree's Shadow number) + 1 companion
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// From Streets of Bree, a dwelling, to Rivendell Valley, in the regroup phase.
		scn.SkipToMovementDecision();
		scn.SetTwilight(4);
		assertEquals(4, scn.GetTwilight());
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		// 4 - 1 + 0 (Rivendell Valley's Shadow number) + 1 companion
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AForestThatIsASanctuaryAndADwellingThatIsARiverEachRemoveOneAsTheFellowshipMovesFromThem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWaterfallAndValleyScenario();

		var habits = scn.GetFreepsCard("habits");
		var frodo = scn.GetRingBearer();
		var valley = scn.GetShadowSite("valley");

		scn.StartGame();
		scn.SkipToSite(3);
		assertSame(scn.GetFreepsSite("site3").getBlueprint(), scn.GetCurrentSite().getBlueprint());
		scn.AttachCardsTo(frodo, habits);
		assertAttachedTo(habits, frodo);
		scn.MoveCardToAdventurePath(valley);
		scn.SetTwilight(4);
		assertEquals(4, scn.GetTwilight());

		// From Rivendell Waterfall, a forest and a sanctuary, to Valley of the Silverlode.
		scn.FreepsPass();
		assertSame(valley, scn.GetCurrentSite());
		// 4 - 1 + 3 (the Valley's Shadow number) + 3 (region 2, which the game adds for a Shadows-block site; the same
		// move with no Habits of Home on the table reads 11) + 1 companion
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// From Valley of the Silverlode, a dwelling and a river, to The Bridge of Khazad-dum, in the regroup phase.
		scn.SkipToMovementDecision();
		scn.SetTwilight(4);
		assertEquals(4, scn.GetTwilight());
		scn.FreepsChooseToMove();
		assertEquals(5, scn.GetCurrentSiteNumber());
		// 4 - 1 + 6 (the Bridge's Shadow number) + 1 companion
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
