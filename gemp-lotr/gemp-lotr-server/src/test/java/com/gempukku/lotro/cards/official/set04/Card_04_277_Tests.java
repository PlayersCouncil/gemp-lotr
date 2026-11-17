package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_277_Tests
{

	// The Towers path: Guma's home is its site 4, White Rocks 4_346 (a Shadow number for mounted companions, none
	// here); site 1 is Eastemnet Gullies 4_325, whose fellowship ability (exert 2 unbound companions to play Legolas)
	// is never used.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guma", "4_277");

					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6, vitality 3; valiant and nothing else
					put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] Man ally, home 3T, vitality 2; a regroup ability used only on purpose
					put("merry", "103_85");     // Merry, Master Holbytla: a [rohan] Hobbit, a Man only while 3 [rohan] companions (or Eowyn) can be spotted
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion; a maneuver ability used only on purpose

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
					put("guma", "4_277");
					put("explorer", "4_250");   // Southron Explorer: Regroup: exert this minion and remove (5) to take control of a site
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GumaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Guma, Plains Farmer
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
		 * 	Discard Guma if an opponent controls his home site.
		 * 	<b>Fellowship:</b> Exert Guma to heal a [rohan] Man.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("guma");

		assertEquals("Guma", card.getBlueprint().getTitle());
		assertEquals("Plains Farmer", card.getBlueprint().getSubtitle());
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

		var guma = scn.GetFreepsCard("guma");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveAlliesToTable(guma);

		scn.StartGame();
		scn.SkipToSite(5);
		// Both markers are at site 5: sites 1 to 4 are behind them.  The opponent holds sites 1 and 2 already.
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		assertTrue(scn.ShadowControls(scn.GetSite(2)));
		assertInZone(Zone.SUPPORT, guma);

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(explorer);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Site 3: not his home, and he stays.
		scn.ShadowUseCardAction(explorer);
		assertTrue(scn.ShadowControls(scn.GetSite(3)));
		assertInZone(Zone.SUPPORT, guma);

		// Site 4[T], his home: he is discarded.
		scn.FreepsPass();
		scn.ShadowUseCardAction(explorer);
		assertTrue(scn.ShadowControls(scn.GetSite(4)));
		assertInDiscard(guma);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PlayedWhileAnOpponentControlsHisHomeSiteHeIsDiscardedAtOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guma = scn.GetFreepsCard("guma");
		scn.MoveCardsToHand(guma);

		scn.StartGame();
		scn.SkipToSite(5);
		// The opponent took sites 1 to 4 on earlier turns.
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.FreepsRefreshDecision();
		assertTrue(scn.ShadowControls(scn.GetSite(4)));
		assertInHand(guma);
		assertTrue(scn.FreepsPlayAvailable(guma));

		scn.FreepsPlayCard(guma);
		assertInDiscard(guma);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnOpponentControllingSite4OfTheFellowshipPathDoesNotDiscardHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipPathScenario();

		var guma = scn.GetFreepsCard("guma");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveAlliesToTable(guma);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(3)));
		assertInZone(Zone.SUPPORT, guma);

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(explorer);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// Site 4 of the Fellowship block is no site 4[T] (CR 4.2 "block symbol"): he stays.
		scn.ShadowUseCardAction(explorer);
		assertTrue(scn.ShadowControls(scn.GetSite(4)));
		assertInZone(Zone.SUPPORT, guma);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void HisOwnerControllingHisHomeSiteDoesNotDiscardHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guma = scn.GetFreepsCard("guma");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveAlliesToTable(guma);

		scn.StartGame();
		scn.SkipToSite(5);
		// Player 1 took sites 1 to 4 as the Shadow player on player 2's turns.
		scn.FreepsTakeControlOfSite();
		scn.FreepsTakeControlOfSite();
		scn.FreepsTakeControlOfSite();
		scn.FreepsTakeControlOfSite();
		assertTrue(scn.FreepsControls(scn.GetSite(4)));
		assertInZone(Zone.SUPPORT, guma);

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(explorer);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// The opponent's take finds no site, and is paid for; Guma stays through it.
		scn.ShadowUseCardAction(explorer);
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertTrue(scn.FreepsControls(scn.GetSite(4)));
		assertInZone(Zone.SUPPORT, guma);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void FellowshipAbilityExertsGumaToHealOneWoundFromARohanMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guma = scn.GetFreepsCard("guma");
		var guard = scn.GetFreepsCard("guard");
		var herdsman = scn.GetFreepsCard("herdsman");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(guard, merry, aragorn);
		scn.MoveAlliesToTable(guma, herdsman);
		scn.AddWoundsToChar(guard, 2);
		scn.AddWoundsToChar(herdsman, 1);
		scn.AddWoundsToChar(merry, 1);
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(guma));
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(herdsman));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsActionAvailable(guma));

		scn.FreepsUseCardAction(guma);
		assertEquals(1, scn.GetWoundsOn(guma));
		// Merry answers the exertion of another [rohan] Man, and is declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merry));
		scn.FreepsDeclineOptionalTrigger();

		// Wounded [rohan] Men: a companion, an ally, and Guma himself, wounded by his own cost. Not Merry, a
		// [rohan] Hobbit (two [rohan] companions only), nor Aragorn, a [gondor] Man.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, herdsman, guma));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);

		// One wound of two healed; nobody else healed.
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(guma));
		assertEquals(1, scn.GetWoundsOn(herdsman));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoOtherRohanManWoundedGumaHealsTheWoundHisOwnExertionMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guma = scn.GetFreepsCard("guma");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(guard, aragorn);
		scn.MoveAlliesToTable(guma);
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();

		// The only wounded Man is of [gondor]; the [rohan] Man companion is unwounded.
		assertEquals(0, scn.GetWoundsOn(guma));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsActionAvailable(guma));

		// The exertion is paid first; Guma is then the one wounded [rohan] Man, and is healed unasked.
		scn.FreepsUseCardAction(guma);
		assertEquals(0, scn.GetWoundsOn(guma));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnExhaustedGumaCannotPayAndIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guma = scn.GetFreepsCard("guma");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(guma);
		scn.AddWoundsToChar(guma, 1);
		scn.AddWoundsToChar(guard, 1);

		scn.StartGame();

		// Vitality 2 with 1 wound: exhausted, with a wounded [rohan] Man to heal.
		assertEquals(1, scn.GetWoundsOn(guma));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(guma));
	}
}
