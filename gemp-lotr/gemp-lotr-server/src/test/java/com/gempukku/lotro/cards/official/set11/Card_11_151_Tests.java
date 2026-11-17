package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_151_Tests
{

	// FellowshipSites: site 2 is Ettenmoors 1_331, a plains; site 3 is Rivendell Valley 1_341, a sanctuary and no plains.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("javelin", "11_151");
					put("javelin2", "11_151");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, valiant and nothing else
					put("guma", "4_277");       // Guma, Plains Farmer: [rohan] Man ally, home 4T; a Fellowship ability only
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion, knight and nothing else
					// Merry, Master Holbytla: a [rohan] Hobbit, and a Man only while 3 [rohan] companions (or Eowyn) can be
					// spotted; beside one other [rohan] companion he is no Man
					put("merry", "103_85");
					// Legolas, Prince of Mirkwood: an archer, so the fellowship archery total is 1 before the Javelin;
					// his other text works only against a Nazgul
					put("legolas", "1_51");
					// Siege Troop: a minion of vitality 4, to take every archery wound; a regroup ability used only on purpose
					put("troop", "8_106");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path with Westemnet Plains 4_342 (a plains and a sanctuary; its text bars wounds on [dunland] Men
	// in the archery phase) as site 3 in place of Barrows of Edoras.  Site 4 is White Rocks 4_346, a battleground
	// and no plains (its Shadow number counts mounted companions; nobody is mounted here).
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("javelin", "11_151");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, valiant and nothing else
					// Rohirrim Herdsman: [rohan] Man ally, home 3T (here Westemnet Plains); a regroup ability only
					put("herdsman", "7_247");
					put("legolas", "1_51");     // Legolas, Prince of Mirkwood: an archer
					put("troop", "8_106");      // Siege Troop: a minion of vitality 4
				}},
				new HashMap<>()
				{{
					put("site1", "4_325");
					put("site2", "4_331");
					put("site3", "4_342");
					put("site4", "4_346");
					put("site5", "4_348");
					put("site6", "4_352");
					put("site7", "102_71");
					put("site8", "4_358");
					put("site9", "4_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RiddermarkJavelinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Riddermark Javelin
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Ranged Weapon
		 * Strength: +1
		 * Game Text: Bearer must be a [rohan] Man.<br><b>Archery:</b> Discard this possession to make the fellowship archery total +1 (or +2 if the fellowship is at a plains site).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("javelin");

		assertEquals("Riddermark Javelin", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RANGED_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnARohanManCompanionOrAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var javelin = scn.GetFreepsCard("javelin");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var knight = scn.GetFreepsCard("knight");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(guard, knight, merry);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(javelin);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(javelin));
		scn.FreepsPlayCard(javelin);

		// A [rohan] Man companion and a [rohan] Man ally; not a Man of [gondor], not the [rohan] Hobbit, not Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, guma));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, merry, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guma);

		assertAttachedTo(javelin, guma);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AwayFromAPlainsDiscardingItMakesTheFellowshipArcheryTotalPlus1ForTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var javelin = scn.GetFreepsCard("javelin");
		var javelin2 = scn.GetFreepsCard("javelin2");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var legolas = scn.GetFreepsCard("legolas");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(guard, legolas);
		scn.MoveAlliesToTable(guma);
		scn.AttachCardsTo(guard, javelin);
		scn.AttachCardsTo(guma, javelin2);

		scn.StartGame();
		assertAttachedTo(javelin, guard);
		assertAttachedTo(javelin2, guma);
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(troop);

		// The maneuver phase at Rivendell Valley, the phase before archery: not offered.
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(javelin));

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsActionAvailable(javelin));

		scn.FreepsUseCardAction(javelin);

		// Legolas's 1, +1: Rivendell Valley is no plains.  The minion archery total is untouched.
		assertInZone(Zone.DISCARD, javelin);
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());
		// The other copy, on Guma, stays where it is.
		assertAttachedTo(javelin2, guma);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());

		// The Troop is the only minion and takes both wounds unasked; in the assignment phase the total is
		// Legolas's 1 again.
		scn.PassCurrentPhaseActions();
		assertEquals(2, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertAttachedTo(javelin2, guma);
	}

	@Test
	public void AtAPlainsSiteDiscardingItMakesTheFellowshipArcheryTotalPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var javelin = scn.GetFreepsCard("javelin");
		var guard = scn.GetFreepsCard("guard");
		var legolas = scn.GetFreepsCard("legolas");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(guard, legolas);
		scn.AttachCardsTo(guard, javelin);

		scn.StartGame();
		assertAttachedTo(javelin, guard);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		// Ettenmoors, a plains and nothing else.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.FreepsUseCardAction(javelin);

		// 1 + 2
		assertInZone(Zone.DISCARD, javelin);
		assertEquals(3, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());

		scn.PassCurrentPhaseActions();
		assertEquals(3, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AtAPlainsSiteThatIsASanctuaryDiscardingItMakesTheFellowshipArcheryTotalPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var javelin = scn.GetFreepsCard("javelin");
		var guard = scn.GetFreepsCard("guard");
		var legolas = scn.GetFreepsCard("legolas");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(guard, legolas);
		scn.AttachCardsTo(guard, javelin);

		scn.StartGame();
		assertAttachedTo(javelin, guard);
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		// Westemnet Plains, a plains and a sanctuary.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(1, scn.GetFreepsArcheryTotal());

		scn.FreepsUseCardAction(javelin);

		// 1 + 2
		assertInZone(Zone.DISCARD, javelin);
		assertEquals(3, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void BorneByAnAllyAtHisPlainsHomeItGoesByTheFellowshipsSiteThatIsNoPlains() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var javelin = scn.GetFreepsCard("javelin");
		var herdsman = scn.GetFreepsCard("herdsman");
		var legolas = scn.GetFreepsCard("legolas");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveAlliesToTable(herdsman);
		scn.AttachCardsTo(herdsman, javelin);

		scn.StartGame();
		assertAttachedTo(javelin, herdsman);
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		// The fellowship is at White Rocks, no plains; the Herdsman is at his home, Westemnet Plains (a plains),
		// laid on the path behind the fellowship.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(1, scn.GetFreepsArcheryTotal());

		scn.FreepsUseCardAction(javelin);

		// 1 + 1: it is the fellowship's site that is read, not the bearer's.
		assertInZone(Zone.DISCARD, javelin);
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}
}
