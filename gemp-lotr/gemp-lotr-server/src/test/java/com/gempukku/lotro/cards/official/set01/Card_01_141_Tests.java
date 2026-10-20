package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_141_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arrows", "1_141");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, cost 2; damage +1 and nothing else
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai; damage +1 and nothing else
					put("worker", "3_62");      // Isengard Worker: [isengard] Orc, neither Saruman nor an Uruk-hai; a regroup ability only
					put("keeper", "3_68");      // Saruman, Keeper of Isengard: the [isengard] Saruman, a minion
					put("rouser", "4_33");      // Saruman, Rabble-rouser: the [dunland] Saruman, a minion
					put("follower", "17_38");   // Saruman, Servant of Sauron: the Saruman who is a follower
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc archer, strength 7: an archer who is no companion or ally

					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: a Wizard who is no Saruman
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf archer companion, strength 6
					put("garrison", "8_36");    // Garrison of Gondor: [gondor] Man knight and archer companion, strength 8
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion who is no archer, strength 6 (+3 only against a Nazgul)
					put("orophin", "1_56");     // Orophin, Lorien Bowman: Elf archer ally, strength 3
					put("rumil", "1_57");       // Rumil, Elven Protector: Elf archer ally, strength 3
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: Elf ally who is no archer, strength 8
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheirArrowsEnrageStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Their Arrows Enrage
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot Saruman or an Uruk-hai. Plays to your support area.
		 * Each archer companion and archer ally is strength -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("arrows");

		assertEquals("Their Arrows Enrage", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAnUrukHaiIsSpottedAndThenEachArcherCompanionAndArcherAllyIsStrengthMinusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrows = scn.GetShadowCard("arrows");
		var savage = scn.GetShadowCard("savage");
		var worker = scn.GetShadowCard("worker");
		var marksman = scn.GetShadowCard("marksman");
		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var legolas = scn.GetFreepsCard("legolas");
		var garrison = scn.GetFreepsCard("garrison");
		var arwen = scn.GetFreepsCard("arwen");
		var orophin = scn.GetFreepsCard("orophin");
		var rumil = scn.GetFreepsCard("rumil");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(gandalf, legolas, garrison, arwen);
		scn.MoveAlliesToTable(orophin, rumil, elrond);
		scn.MoveCardsToHand(arrows, savage);

		scn.StartGame();
		scn.MoveMinionsToTable(worker, marksman);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// An [isengard] Orc on the table and a Wizard who is no Saruman in the fellowship: nothing to spot.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(arrows));

		assertEquals(4, scn.GetStrength(frodo));    // 3 + 1 for The Ruling Ring
		assertEquals(7, scn.GetStrength(gandalf));
		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(8, scn.GetStrength(garrison));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(3, scn.GetStrength(orophin));
		assertEquals(3, scn.GetStrength(rumil));
		assertEquals(8, scn.GetStrength(elrond));
		assertEquals(7, scn.GetStrength(marksman));
		assertEquals(5, scn.GetStrength(worker));

		scn.ShadowPlayCard(savage);
		assertTrue(scn.ShadowPlayAvailable(arrows));

		scn.ShadowPlayCard(arrows);
		assertInZone(Zone.SUPPORT, arrows);

		// The archer companions of two cultures and the two archer allies are each 1 less.
		assertEquals(5, scn.GetStrength(legolas));
		assertEquals(7, scn.GetStrength(garrison));
		assertEquals(2, scn.GetStrength(orophin));
		assertEquals(2, scn.GetStrength(rumil));
		// A companion and an ally who are no archers, an archer who is a minion, and the rest: as they were.
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(8, scn.GetStrength(elrond));
		assertEquals(7, scn.GetStrength(marksman));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(7, scn.GetStrength(gandalf));
		assertEquals(5, scn.GetStrength(worker));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedBesideTheIsengardSarumanWithNoUrukHai() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrows = scn.GetShadowCard("arrows");
		var keeper = scn.GetShadowCard("keeper");
		scn.MoveCardsToHand(arrows);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(arrows));

		scn.MoveMinionsToTable(keeper);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(arrows));

		scn.ShadowPlayCard(arrows);
		assertInZone(Zone.SUPPORT, arrows);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedBesideTheDunlandSaruman() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrows = scn.GetShadowCard("arrows");
		var rouser = scn.GetShadowCard("rouser");
		scn.MoveCardsToHand(arrows);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(arrows));

		scn.MoveMinionsToTable(rouser);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(arrows));

		scn.ShadowPlayCard(arrows);
		assertInZone(Zone.SUPPORT, arrows);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedBesideTheSarumanWhoIsAFollower() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrows = scn.GetShadowCard("arrows");
		var follower = scn.GetShadowCard("follower");
		scn.MoveCardsToHand(arrows);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(arrows));

		scn.MoveCardsToSupportArea(follower);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, follower);
		assertTrue(scn.ShadowPlayAvailable(arrows));

		scn.ShadowPlayCard(arrows);
		assertInZone(Zone.SUPPORT, arrows);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedBesideAnUrukHaiOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrows = scn.GetShadowCard("arrows");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToHand(arrows);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(arrows));

		scn.MoveMinionsToTable(guard);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(arrows));

		scn.ShadowPlayCard(arrows);
		assertInZone(Zone.SUPPORT, arrows);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
