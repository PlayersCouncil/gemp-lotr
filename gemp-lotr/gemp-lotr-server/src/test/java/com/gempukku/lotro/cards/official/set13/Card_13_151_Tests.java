package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_151_Tests
{

	// The Fellowship path; site 2 is Ettenmoors 1_331, a plains, whose skirmish ability is used only on purpose.
	// Two Shadows-block dwellings stand by, laid as site 2 by MoveCardToAdventurePath where a test wants one.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gaffer", "13_151");

					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit companion; a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion; abilities used only on purpose
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit (cheated in); an assignment ability only
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion (cheated in); a skirmish ability used only on purpose
					// Daddy Twofoot, Next-door Neighbor: another [shire] follower, Aid - add a burden; his own transfer
					// trigger asks nothing with no [shire] event in the discard pile
					put("daddy", "13_144");

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text; only there so that there is a maneuver phase
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					// Buckland Homestead: a dwelling and nothing else; a Shadow ability used only on purpose.
					put("buckland", "11_230");
					// Valley of the Silverlode: a dwelling and a river; "When your fellowship moves to this site, you may
					// heal each Hobbit companion" (no Hobbit is wounded where it is laid).
					put("valley", "11_261");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheGafferStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: The Gaffer, Master Gardener
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Follower
		 * Subtype:
		 * Strength: +1
		 * Aid: Add a burden
		 * Game Text: <b>Aid</b> – Add a burden. <helper>(At the start of the maneuver phase, you may add a burden to transfer this to a companion.)</helper><br>When you transfer The Gaffer to a [shire] companion, heal bearer (or heal bearer twice if he or she is at a dwelling site).
		*/

		var scn = GetScenario();

		var gaffer = scn.GetFreepsCard("gaffer");

		assertEquals("The Gaffer", gaffer.getBlueprint().getTitle());
		assertEquals("Master Gardener", gaffer.getBlueprint().getSubtitle());
		assertTrue(gaffer.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, gaffer.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, gaffer.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, gaffer.getBlueprint().getCardType());
		assertEquals(1, gaffer.getBlueprint().getTwilightCost());
		assertEquals(1, gaffer.getBlueprint().getStrength());
	}

	@Test
	public void TransferredByItsAidToAShireCompanionAwayFromADwellingItHealsBearerOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaffer = scn.GetFreepsCard("gaffer");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(merry, sam);
		scn.MoveCardsToSupportArea(gaffer);
		scn.AddWoundsToChar(merry, 2);
		scn.AddWoundsToChar(sam, 2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, gaffer);
		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(2, scn.GetWoundsOn(sam));
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		// Ettenmoors, a plains: no dwelling.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.DWELLING));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gaffer, "Aid"));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(merry);

		// Aid - Add a burden.
		assertEquals(1, scn.GetBurdens());
		assertAttachedTo(gaffer, merry);
		// Bearer healed once, with nothing asked; Sam, wounded beside him, is not bearer.
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AtADwellingItHealsAShireHobbitBearerTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaffer = scn.GetFreepsCard("gaffer");
		var merry = scn.GetFreepsCard("merry");
		var buckland = scn.GetShadowSite("buckland");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToSupportArea(gaffer);
		scn.AddWoundsToChar(merry, 3);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, gaffer);
		assertEquals(3, scn.GetWoundsOn(merry));
		scn.MoveCardToAdventurePath(buckland);
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertSame(buckland, scn.GetCurrentSite());
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(merry);

		assertAttachedTo(gaffer, merry);
		// 3 wounds, healed twice
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AtADwellingThatIsARiverItHealsAShireCompanionWhoIsNoHobbitTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaffer = scn.GetFreepsCard("gaffer");
		var smeagol = scn.GetFreepsCard("smeagol");
		var valley = scn.GetShadowSite("valley");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToSupportArea(gaffer);
		scn.AddWoundsToChar(smeagol, 3);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, gaffer);
		assertEquals(3, scn.GetWoundsOn(smeagol));
		scn.MoveCardToAdventurePath(valley);
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertSame(valley, scn.GetCurrentSite());
		assertEquals(3, scn.GetWoundsOn(smeagol));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(smeagol);

		assertAttachedTo(gaffer, smeagol);
		// 3 wounds, healed twice
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TransferredToACompanionWhoIsNotShireItHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaffer = scn.GetFreepsCard("gaffer");
		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var buckland = scn.GetShadowSite("buckland");
		scn.MoveCompanionsToTable(pippin, merry);
		scn.MoveCardsToSupportArea(gaffer);
		scn.AddWoundsToChar(pippin, 2);
		scn.AddWoundsToChar(merry, 2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, gaffer);
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertEquals(2, scn.GetWoundsOn(merry));
		scn.MoveCardToAdventurePath(buckland);
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertSame(buckland, scn.GetCurrentSite());
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(pippin);

		// Pippin is a Hobbit, but [gondor]: nobody is healed.
		assertEquals(1, scn.GetBurdens());
		assertAttachedTo(gaffer, pippin);
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertEquals(2, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AnotherFollowerTransferredToAShireCompanionHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaffer = scn.GetFreepsCard("gaffer");
		var daddy = scn.GetFreepsCard("daddy");
		var merry = scn.GetFreepsCard("merry");
		var buckland = scn.GetShadowSite("buckland");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToSupportArea(gaffer, daddy);
		scn.AddWoundsToChar(merry, 2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, gaffer, daddy);
		assertEquals(2, scn.GetWoundsOn(merry));
		scn.MoveCardToAdventurePath(buckland);
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertSame(buckland, scn.GetCurrentSite());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gaffer, "Aid"));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(daddy, "Aid"));
		scn.FreepsChooseOptionalTrigger(daddy);
		scn.FreepsChooseCard(merry);

		// Daddy Twofoot went to Merry; The Gaffer did not, and Merry is not healed.
		assertAttachedTo(daddy, merry);
		assertEquals(2, scn.GetWoundsOn(merry));

		// The Gaffer's own Aid is still to be answered: declined, he stays in the support area.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gaffer, "Aid"));
		scn.FreepsDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, gaffer);
		assertEquals(2, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
