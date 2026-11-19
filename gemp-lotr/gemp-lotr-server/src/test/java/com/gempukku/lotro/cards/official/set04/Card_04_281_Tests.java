package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_281_Tests
{

	// The Towers path: Hlafwine's home is its site 4, White Rocks 4_346 (a Shadow number for mounted companions, none
	// here); site 2 is Eastfold 4_331, whose Shadow ability needs 3 [isengard] trackers.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hlafwine", "4_281");

					put("guard1", "5_83");      // Household Guard: [rohan] Man companion, strength 6, vitality 3; valiant and nothing else
					put("guard2", "5_83");
					put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] Man ally, home 3T, strength 4; a regroup ability used only on purpose
					put("merry", "103_85");     // Merry, Master Holbytla: a [rohan] Hobbit, a Man only while 3 [rohan] companions (or Eowyn) can be spotted
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, strength 8; a maneuver ability used only on purpose

					put("brigand1", "4_10");    // Dunlending Brigand: a [dunland] Man minion, strength 8, vitality 1, no text
					put("brigand2", "4_10");
					put("explorer", "4_250");   // Southron Explorer: Regroup: exert this minion and remove (5) to take control of a site
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path, whose site 4 (Balin's Tomb 1_343) is no site 4[T].
	protected VirtualTableScenario GetFellowshipPathScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hlafwine", "4_281");
					put("explorer", "4_250");   // Southron Explorer: Regroup: exert this minion and remove (5) to take control of a site
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HlafwineStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Hlafwine, Village Farmhand
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Ally
		 * Subtype: Man
		 * Strength: 4
		 * Vitality: 2
		 * Site Number: 4T
		 * Game Text: <b>Villager</b>.
		 * 	Discard Hlafwine if an opponent controls his home site.
		 * 	<b>Skirmish:</b> Exert Hlafwine to make a [rohan] Man strength +2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hlafwine");

		assertEquals("Hlafwine", card.getBlueprint().getTitle());
		assertEquals("Village Farmhand", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VILLAGER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.TWO_TOWERS, 4)));
	}

	@Test
	public void IsDiscardedWhenAnOpponentTakesControlOfSite4OfTheTowersPathAndNotBefore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hlafwine = scn.GetFreepsCard("hlafwine");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveAlliesToTable(hlafwine);

		scn.StartGame();
		scn.SkipToSite(5);
		// Both markers are at site 5: sites 1 to 4 are behind them.  The opponent holds sites 1 and 2 already.
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		assertTrue(scn.ShadowControls(scn.GetSite(2)));
		assertInZone(Zone.SUPPORT, hlafwine);

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(explorer);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Site 3: not his home, and he stays.
		scn.ShadowUseCardAction(explorer);
		assertTrue(scn.ShadowControls(scn.GetSite(3)));
		assertInZone(Zone.SUPPORT, hlafwine);

		// Site 4[T], his home: he is discarded.
		scn.FreepsPass();
		scn.ShadowUseCardAction(explorer);
		assertTrue(scn.ShadowControls(scn.GetSite(4)));
		assertInDiscard(hlafwine);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PlayedWhileAnOpponentControlsHisHomeSiteHeIsDiscardedAtOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hlafwine = scn.GetFreepsCard("hlafwine");
		scn.MoveCardsToHand(hlafwine);

		scn.StartGame();
		scn.SkipToSite(5);
		// The opponent took sites 1 to 4 on earlier turns.
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.FreepsRefreshDecision();
		assertTrue(scn.ShadowControls(scn.GetSite(4)));
		assertInHand(hlafwine);
		assertTrue(scn.FreepsPlayAvailable(hlafwine));

		scn.FreepsPlayCard(hlafwine);
		assertInDiscard(hlafwine);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnOpponentControllingSite4OfTheFellowshipPathDoesNotDiscardHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipPathScenario();

		var hlafwine = scn.GetFreepsCard("hlafwine");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveAlliesToTable(hlafwine);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(3)));
		assertInZone(Zone.SUPPORT, hlafwine);

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(explorer);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// Site 4 of the Fellowship block is no site 4[T] (CR 4.2 "block symbol"): he stays.
		scn.ShadowUseCardAction(explorer);
		assertTrue(scn.ShadowControls(scn.GetSite(4)));
		assertInZone(Zone.SUPPORT, hlafwine);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void HisOwnerControllingHisHomeSiteDoesNotDiscardHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hlafwine = scn.GetFreepsCard("hlafwine");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveAlliesToTable(hlafwine);

		scn.StartGame();
		scn.SkipToSite(5);
		// Player 1 took sites 1 to 4 as the Shadow player on player 2's turns.
		scn.FreepsTakeControlOfSite();
		scn.FreepsTakeControlOfSite();
		scn.FreepsTakeControlOfSite();
		scn.FreepsTakeControlOfSite();
		assertTrue(scn.FreepsControls(scn.GetSite(4)));
		assertInZone(Zone.SUPPORT, hlafwine);

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(explorer);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// The opponent's take finds no site, and is paid for; Hlafwine stays through it.
		scn.ShadowUseCardAction(explorer);
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertTrue(scn.FreepsControls(scn.GetSite(4)));
		assertInZone(Zone.SUPPORT, hlafwine);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SkirmishAbilityExertsHlafwineToMakeARohanManStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hlafwine = scn.GetFreepsCard("hlafwine");
		var guard1 = scn.GetFreepsCard("guard1");
		var herdsman = scn.GetFreepsCard("herdsman");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var brigand1 = scn.GetShadowCard("brigand1");
		scn.MoveCompanionsToTable(guard1, merry, aragorn);
		scn.MoveAlliesToTable(hlafwine, herdsman);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard1, brigand1);
		scn.FreepsResolveSkirmish(guard1);

		assertTrue(scn.IsCharSkirmishingAgainst(guard1, brigand1));
		assertEquals(0, scn.GetWoundsOn(hlafwine));
		assertEquals(6, scn.GetStrength(guard1));
		assertEquals(4, scn.GetStrength(herdsman));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsActionAvailable(hlafwine));

		scn.FreepsUseCardAction(hlafwine);
		assertEquals(1, scn.GetWoundsOn(hlafwine));
		// Merry answers the exertion of another [rohan] Man, and is declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merry));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(hlafwine));
		// [rohan] Men in and out of the skirmish, a companion and two allies, Hlafwine himself among them; not Merry,
		// a [rohan] Hobbit (two [rohan] companions only), Aragorn, a [gondor] Man, or the [dunland] Man he fights.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard1, herdsman, hlafwine));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, aragorn, brigand1));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard1);

		assertEquals(8, scn.GetStrength(guard1));
		assertEquals(4, scn.GetStrength(herdsman));
		assertEquals(4, scn.GetStrength(hlafwine));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheBonusLastsTheSkirmishPhaseItIsMadeInAndAnExhaustedHlafwineCannotPay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hlafwine = scn.GetFreepsCard("hlafwine");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var brigand1 = scn.GetShadowCard("brigand1");
		var brigand2 = scn.GetShadowCard("brigand2");
		scn.MoveCompanionsToTable(guard1, guard2);
		scn.MoveAlliesToTable(hlafwine);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand1, brigand2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard1, brigand1}, new PhysicalCardImpl[]{guard2, brigand2});
		scn.FreepsResolveSkirmish(guard1);

		// The second Guard, who waits for his own skirmish, is made strength +2 during the first's.
		assertEquals(6, scn.GetStrength(guard2));
		scn.FreepsUseCardAction(hlafwine);
		scn.FreepsChooseCard(guard2);
		assertEquals(8, scn.GetStrength(guard2));
		assertEquals(6, scn.GetStrength(guard1));

		// Vitality 2 with 1 wound: exhausted, and the ability is no longer offered, with [rohan] Men on the table.
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(hlafwine));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(hlafwine));
		assertEquals(8, scn.GetStrength(guard2));

		// 6 against 8: the first Guard loses and takes a wound.  In the next skirmish phase the bonus is gone.
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(guard1));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(6, scn.GetStrength(guard2));
		scn.FreepsResolveSkirmish(guard2);
		assertTrue(scn.IsCharSkirmishingAgainst(guard2, brigand2));
		assertEquals(6, scn.GetStrength(guard2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
