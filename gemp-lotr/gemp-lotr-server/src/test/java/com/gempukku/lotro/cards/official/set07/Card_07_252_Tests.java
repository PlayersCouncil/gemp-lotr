package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_252_Tests
{

	// The Towers path with Westemnet Hills 4_344 as site 4, the home of Guma (4T); the Herdsman's home is site 3,
	// Barrows of Edoras 4_337, a sanctuary and no plains.  Site 1, Eastemnet Gullies 4_325, is no plains either.
	// CR 4.2 "ally": an ally is at his home site, the fellowship's only when it is there.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arms", "7_252");
					put("arms2", "7_252");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6; valiant and nothing else
					put("rider", "4_286");      // Rider of Rohan: [rohan] Man companion, strength 5; a skirmish ability only when mounted
					put("boromir", "1_97");     // Boromir, Son of Denethor: a Man of [gondor], strength 7; a skirmish ability used only on purpose
					// Merry, Master Holbytla: a [rohan] companion who is a Hobbit, strength 3; a Man only beside three [rohan]
					// companions or Éowyn.  His other texts answer exertions and skirmishes, and none comes up here.
					put("merry", "103_85");
					put("aldor", "4_262");      // Aldor, Soldier of Edoras: [rohan] Man ally, no requirement to play; an archery ability
					put("guma", "4_277");       // Guma: [rohan] Man ally, home 4T, strength 4; a fellowship ability used only on purpose
					put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] Man ally, home 3T, strength 4; a regroup ability used only on purpose
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site4", "4_344");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StrongArmsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Strong Arms
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype:
		 * Game Text: To play, spot a [rohan] Man. Plays on a site.<br>This site is a <b>plains</b>.<br>Each [rohan] Man is strength +1 at this site.
		*/

		var scn = GetScenario();

		var arms = scn.GetFreepsCard("arms");

		assertEquals("Strong Arms", arms.getBlueprint().getTitle());
		assertNull(arms.getBlueprint().getSubtitle());
		assertFalse(arms.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, arms.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, arms.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, arms.getBlueprint().getCardType());
		assertEquals(1, arms.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilARohanManAllyIsPlayedAndThenGoesOnTheSiteAndMakesItAPlains() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arms = scn.GetFreepsCard("arms");
		var aldor = scn.GetFreepsCard("aldor");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		// A Man of another culture, and a [rohan] companion who is no Man.
		scn.MoveCompanionsToTable(boromir, merry);
		scn.MoveCardsToHand(arms, aldor);

		scn.StartGame();

		var site1 = scn.GetSite(1);
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertFalse(scn.HasKeyword(site1, Keyword.PLAINS));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(arms));

		scn.FreepsPlayCard(aldor);
		assertInZone(Zone.SUPPORT, aldor);
		assertTrue(scn.FreepsPlayAvailable(arms));

		// Site 1 is the one site in play: Strong Arms goes on it unasked.
		scn.FreepsPlayCard(arms);
		assertAttachedTo(arms, site1);
		assertTrue(scn.HasKeyword(site1, Keyword.PLAINS));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedOnTheFellowshipsSiteItIsAPlainsAndEachRohanManThereIsStrengthPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arms = scn.GetFreepsCard("arms");
		var arms2 = scn.GetFreepsCard("arms2");
		var rider = scn.GetFreepsCard("rider");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		var herdsman = scn.GetFreepsCard("herdsman");
		var guma = scn.GetFreepsCard("guma");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(rider, boromir, merry);
		scn.MoveAlliesToTable(herdsman, guma);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(arms);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		// A second copy on site 2, where the fellowship is not.
		scn.AttachCardsTo(site2, arms2);
		scn.FreepsRefreshDecision();

		// The fellowship is at Barrows of Edoras, the Herdsman's home: he is there with the companions.  Guma's home,
		// Westemnet Hills, is not yet laid: he is at no site.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertAttachedTo(arms2, site2);
		assertFalse(scn.HasKeyword(site3, Keyword.PLAINS));
		assertEquals(5, scn.GetStrength(rider));
		assertEquals(4, scn.GetStrength(herdsman));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(4, scn.GetStrength(guma));
		// Frodo 3 + 1 for The Ruling Ring.
		assertEquals(4, scn.GetStrength(frodo));

		assertTrue(scn.FreepsPlayAvailable(arms));
		scn.FreepsPlayCard(arms);
		// Any site in play; no character.
		assertTrue(scn.FreepsHasCardChoiceAvailable(site1, site2, site3));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(rider, herdsman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(site3);
		assertAttachedTo(arms, site3);

		assertTrue(scn.HasKeyword(site3, Keyword.PLAINS));
		// The [rohan] Men at this site, a companion and an ally, are +1 (the copy on site 2 adds nothing here); a Man
		// of [gondor], a [rohan] Hobbit, an ally at no site and the Ring-bearer are not.
		assertEquals(6, scn.GetStrength(rider));
		assertEquals(5, scn.GetStrength(herdsman));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(4, scn.GetStrength(guma));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedOnASiteTheFellowshipHasLeftOnlyThatSiteIsAPlainsAndNoCompanionIsStrengthened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arms = scn.GetFreepsCard("arms");
		var rider = scn.GetFreepsCard("rider");
		var boromir = scn.GetFreepsCard("boromir");
		// The Rider is the only [rohan] Man to spot.
		scn.MoveCompanionsToTable(rider, boromir);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(arms);
		scn.FreepsRefreshDecision();

		var site1 = scn.GetSite(1);
		var site3 = scn.GetSite(3);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(site1, Keyword.PLAINS));
		assertFalse(scn.HasKeyword(site3, Keyword.PLAINS));
		assertEquals(5, scn.GetStrength(rider));

		assertTrue(scn.FreepsPlayAvailable(arms));
		scn.FreepsPlayCard(arms);
		scn.FreepsChooseCard(site1);
		assertAttachedTo(arms, site1);

		assertTrue(scn.HasKeyword(site1, Keyword.PLAINS));
		assertFalse(scn.HasKeyword(site3, Keyword.PLAINS));
		assertEquals(5, scn.GetStrength(rider));
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnTheFellowshipsSiteAnAllyWhoseHomeIsAnotherSiteIsNotStrengthened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arms = scn.GetFreepsCard("arms");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var herdsman = scn.GetFreepsCard("herdsman");
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(guma, herdsman);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToHand(arms);
		scn.FreepsRefreshDecision();

		var site4 = scn.GetSite(4);
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(4, scn.GetStrength(guma));
		assertEquals(4, scn.GetStrength(herdsman));

		scn.FreepsPlayCard(arms);
		scn.FreepsChooseCard(site4);
		assertAttachedTo(arms, site4);

		// The companion and Guma, at his home site, are at Westemnet Hills with the fellowship; the Herdsman is at
		// Barrows of Edoras, his home.
		assertEquals(7, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(guma));
		assertEquals(4, scn.GetStrength(herdsman));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnAnAllysHomeSiteThatAllyIsStrengthenedWhileTheFellowshipIsAtAnother() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arms = scn.GetFreepsCard("arms");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var herdsman = scn.GetFreepsCard("herdsman");
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(guma, herdsman);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToHand(arms);
		scn.FreepsRefreshDecision();

		var site3 = scn.GetSite(3);
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(site3, Keyword.PLAINS));
		assertEquals(4, scn.GetStrength(herdsman));

		scn.FreepsPlayCard(arms);
		scn.FreepsChooseCard(site3);
		assertAttachedTo(arms, site3);

		// Barrows of Edoras is the Herdsman's home: he is at it, though the fellowship is at site 4 with the
		// companion and Guma.
		assertTrue(scn.HasKeyword(site3, Keyword.PLAINS));
		assertEquals(5, scn.GetStrength(herdsman));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(4, scn.GetStrength(guma));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
