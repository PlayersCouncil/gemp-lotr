package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_154_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("chapter", "13_154");

					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit companion, twilight 1; a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion, not the Ring-bearer; abilities used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion (cheated in); a skirmish ability used only on purpose
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit (cheated in); an assignment ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability used only on purpose
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally, twilight 1, vitality 2; a skirmish ability used only on purpose

					// Daddy Twofoot, Next-door Neighbor: a [shire] follower, twilight 1; his texts answer his own
					// transfer only
					put("daddy", "13_144");
					// Radagast, Tender of Beasts: a [gandalf] follower, twilight 2; a standing text on minions skirmishing bearer
					put("radagast", "13_38");

					// Saruman, Servant of Sauron: an [isengard] SHADOW follower, twilight 1; a text on bearer's wins only
					put("saruman", "17_38");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NewChapterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: New Chapter
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot 2 Hobbits.<br>Each time you play a follower, you may heal a companion (or heal Sam twice).
		*/

		var scn = GetScenario();

		var chapter = scn.GetFreepsCard("chapter");

		assertEquals("New Chapter", chapter.getBlueprint().getTitle());
		assertNull(chapter.getBlueprint().getSubtitle());
		assertFalse(chapter.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, chapter.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, chapter.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, chapter.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(chapter, Keyword.SUPPORT_AREA));
		assertEquals(2, chapter.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilASecondHobbitIsSpottedAndAHobbitAllyIsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chapter = scn.GetFreepsCard("chapter");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(smeagol, aragorn);

		scn.StartGame();
		scn.MoveCardsToHand(chapter, bounder);
		scn.FreepsRefreshDecision();

		// Frodo, the Ring-bearer, is the one Hobbit: Smeagol is [shire] and no Hobbit, Aragorn a Man.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(chapter));

		// A second Hobbit, an ally, is played.
		scn.FreepsPlayCard(bounder);
		assertInZone(Zone.SUPPORT, bounder);
		assertTrue(scn.FreepsPlayAvailable(chapter));
		scn.FreepsPlayCard(chapter);

		assertInZone(Zone.SUPPORT, chapter);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AGondorHobbitBesideTheRingBearerLetsItBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chapter = scn.GetFreepsCard("chapter");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(pippin);

		scn.StartGame();
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		scn.MoveCardsToHand(chapter);
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsPlayAvailable(chapter));
		scn.FreepsPlayCard(chapter);
		assertInZone(Zone.SUPPORT, chapter);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeAFollowerIsPlayedACompanionMayBeHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chapter = scn.GetFreepsCard("chapter");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var daddy = scn.GetFreepsCard("daddy");
		var radagast = scn.GetFreepsCard("radagast");
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(chapter);
		scn.AddWoundsToChar(merry, 2);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(bounder, 1);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, chapter);
		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(bounder));
		scn.MoveCardsToHand(daddy, radagast);
		scn.FreepsRefreshDecision();

		// A [shire] follower.
		scn.FreepsPlayCard(daddy);
		assertInZone(Zone.SUPPORT, daddy);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(chapter));
		scn.FreepsAcceptOptionalTrigger();

		// Companions of two cultures and races; not the ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bounder));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(bounder));

		// A second follower, of another culture: offered again.
		scn.FreepsPlayCard(radagast);
		assertInZone(Zone.SUPPORT, radagast);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(chapter));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningTheHealLeavesEveryWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chapter = scn.GetFreepsCard("chapter");
		var merry = scn.GetFreepsCard("merry");
		var daddy = scn.GetFreepsCard("daddy");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToSupportArea(chapter);
		scn.AddWoundsToChar(merry, 2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, chapter);
		assertEquals(2, scn.GetWoundsOn(merry));
		scn.MoveCardsToHand(daddy);
		scn.FreepsRefreshDecision();

		scn.FreepsPlayCard(daddy);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(chapter));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(2, scn.GetWoundsOn(merry));
		assertInZone(Zone.SUPPORT, daddy);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithSamTheOnlyWoundedCompanionSamIsHealedTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chapter = scn.GetFreepsCard("chapter");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var bounder = scn.GetFreepsCard("bounder");
		var daddy = scn.GetFreepsCard("daddy");
		scn.MoveCompanionsToTable(sam, merry);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(chapter);
		scn.AddWoundsToChar(sam, 3);
		scn.AddWoundsToChar(bounder, 1);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, chapter);
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(bounder));
		scn.MoveCardsToHand(daddy);
		scn.FreepsRefreshDecision();

		scn.FreepsPlayCard(daddy);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(chapter));
		scn.FreepsAcceptOptionalTrigger();

		// 3 wounds, healed twice
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithSamWoundedSamIsHealedTwiceAndNoOtherCompanionIsChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chapter = scn.GetFreepsCard("chapter");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var daddy = scn.GetFreepsCard("daddy");
		scn.MoveCompanionsToTable(sam, merry);
		scn.MoveCardsToSupportArea(chapter);
		scn.AddWoundsToChar(sam, 3);
		scn.AddWoundsToChar(merry, 2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, chapter);
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetWoundsOn(merry));
		scn.MoveCardsToHand(daddy);
		scn.FreepsRefreshDecision();

		scn.FreepsPlayCard(daddy);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(chapter));
		scn.FreepsAcceptOptionalTrigger();

		// "(or heal Sam twice)" is a parenthesis with no condition but its own target, carried out whenever it can
		// be: Sam is healed twice, unasked, and Merry is not healed.
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithSamWoundedTwiceSamIsHealedOfBothAndNoOtherCompanionIsChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chapter = scn.GetFreepsCard("chapter");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var daddy = scn.GetFreepsCard("daddy");
		scn.MoveCompanionsToTable(sam, merry);
		scn.MoveCardsToSupportArea(chapter);
		scn.AddWoundsToChar(sam, 2);
		scn.AddWoundsToChar(merry, 2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, chapter);
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetWoundsOn(merry));
		scn.MoveCardsToHand(daddy);
		scn.FreepsRefreshDecision();

		scn.FreepsPlayCard(daddy);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(chapter));
		scn.FreepsAcceptOptionalTrigger();

		// Ketura (Q-0392: A2): 2 wounds, the fewest that let Sam be healed twice. The parenthesis is made, unasked.
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithSamWoundedOnceAnyWoundedCompanionIsChosenAndHealedOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chapter = scn.GetFreepsCard("chapter");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var daddy = scn.GetFreepsCard("daddy");
		scn.MoveCompanionsToTable(sam, merry);
		scn.MoveCardsToSupportArea(chapter);
		scn.AddWoundsToChar(sam, 1);
		scn.AddWoundsToChar(merry, 2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, chapter);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetWoundsOn(merry));
		scn.MoveCardsToHand(daddy);
		scn.FreepsRefreshDecision();

		scn.FreepsPlayCard(daddy);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(chapter));
		scn.FreepsAcceptOptionalTrigger();

		// Ketura (Q-0392: A2): with one wound Sam cannot be healed twice, so the parenthesis is not available and
		// the plain heal is made: any wounded companion, Sam among them, is chosen.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, merry));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithSamInPlayAndUnwoundedAnotherCompanionIsHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chapter = scn.GetFreepsCard("chapter");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var daddy = scn.GetFreepsCard("daddy");
		scn.MoveCompanionsToTable(sam, merry, aragorn);
		scn.MoveCardsToSupportArea(chapter);
		scn.AddWoundsToChar(merry, 2);
		scn.AddWoundsToChar(aragorn, 2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, chapter);
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		scn.MoveCardsToHand(daddy);
		scn.FreepsRefreshDecision();

		scn.FreepsPlayCard(daddy);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(chapter));
		scn.FreepsAcceptOptionalTrigger();

		// Sam has no wound to heal: a wounded companion is chosen and healed once.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, aragorn));
		scn.FreepsChooseCard(merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayingACompanionOffersNoHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chapter = scn.GetFreepsCard("chapter");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(chapter);
		scn.AddWoundsToChar(aragorn, 2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, chapter);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		scn.MoveCardsToHand(merry);
		scn.FreepsRefreshDecision();

		scn.FreepsPlayCard(merry);
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(chapter));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AFollowerTheOpponentPlaysOffersNoHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chapter = scn.GetFreepsCard("chapter");
		var aragorn = scn.GetFreepsCard("aragorn");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(chapter);
		scn.AddWoundsToChar(aragorn, 2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, chapter);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		scn.MoveCardsToHand(saruman);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(saruman);
		assertInZone(Zone.SUPPORT, saruman);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(chapter));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoCompanionWoundedTheHealIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chapter = scn.GetFreepsCard("chapter");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var daddy = scn.GetFreepsCard("daddy");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(chapter);
		scn.AddWoundsToChar(bounder, 1);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, chapter);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(bounder));
		scn.MoveCardsToHand(daddy);
		scn.FreepsRefreshDecision();

		// Only an ally is wounded: the heal would do nothing, and it is not offered.
		scn.FreepsPlayCard(daddy);
		assertInZone(Zone.SUPPORT, daddy);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(chapter));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
