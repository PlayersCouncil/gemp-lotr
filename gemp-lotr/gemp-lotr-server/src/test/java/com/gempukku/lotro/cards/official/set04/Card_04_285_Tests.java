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

public class Card_04_285_Tests
{

	// The Towers path: Léod's home is its site 4, White Rocks 4_346 (+1 to its Shadow number for each mounted
	// companion, only once the fellowship is there); site 2 is Eastfold 4_331, whose Shadow ability needs 3
	// [isengard] trackers.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("leod", "4_285");

					put("guard1", "5_83");      // Household Guard: [rohan] Man companion, strength 6, vitality 3; valiant and nothing else
					put("guard2", "5_83");
					put("firefoot", "13_126");  // Firefoot, Mearas of The Mark: a mount a [rohan] Man bears, strength +1; its texts need Éomer or a play
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound [shire] Hobbit companion; abilities used only on purpose
					put("bill", "13_143");      // Bill the Pony, Dearly-loved: a mount a Ring-bound Hobbit bears; a regroup ability used only on purpose
					put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] Man ally, home 3T; a regroup ability used only on purpose
					put("snowmane", "13_136");  // Snowmane, Noble Mearas: a mount a [rohan] Man bears, strength +1; its texts need Théoden or a play
					put("gimli", "1_13");       // Gimli, Son of Glóin: a Dwarf companion; a skirmish ability used only on purpose
					put("armor", "1_8");        // Dwarven Armor: a possession that is no mount, borne by a Dwarf
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, strength 8; a maneuver ability used only on purpose

					put("chieftain", "1_266");  // Orc Chieftain: a [sauron] Orc, strength 7, vitality 2; text only on a weapon played on him
					put("warg", "10_89");       // Gothmog's Warg: a mount a [sauron] Orc bears, strength +1; +1 more for each site its owner controls (none)
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
					put("leod", "4_285");
					put("explorer", "4_250");   // Southron Explorer: Regroup: exert this minion and remove (5) to take control of a site
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LeodStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Léod, Westfold Herdsman
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
		 * 	Discard Léod if an opponent controls his home site.
		 * 	<b>Skirmish:</b> Exert Léod to make a mounted companion strength +2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("leod");

		assertEquals("Léod", card.getBlueprint().getTitle());
		assertEquals("Westfold Herdsman", card.getBlueprint().getSubtitle());
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

		var leod = scn.GetFreepsCard("leod");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveAlliesToTable(leod);

		scn.StartGame();
		scn.SkipToSite(5);
		// Both markers are at site 5: sites 1 to 4 are behind them.  The opponent holds sites 1 and 2 already.
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		assertTrue(scn.ShadowControls(scn.GetSite(2)));
		assertInZone(Zone.SUPPORT, leod);

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(explorer);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Site 3: not his home, and he stays.
		scn.ShadowUseCardAction(explorer);
		assertTrue(scn.ShadowControls(scn.GetSite(3)));
		assertInZone(Zone.SUPPORT, leod);

		// Site 4[T], his home: he is discarded.
		scn.FreepsPass();
		scn.ShadowUseCardAction(explorer);
		assertTrue(scn.ShadowControls(scn.GetSite(4)));
		assertInDiscard(leod);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PlayedWhileAnOpponentControlsHisHomeSiteHeIsDiscardedAtOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leod = scn.GetFreepsCard("leod");
		scn.MoveCardsToHand(leod);

		scn.StartGame();
		scn.SkipToSite(5);
		// The opponent took sites 1 to 4 on earlier turns.
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.FreepsRefreshDecision();
		assertTrue(scn.ShadowControls(scn.GetSite(4)));
		assertInHand(leod);
		assertTrue(scn.FreepsPlayAvailable(leod));

		scn.FreepsPlayCard(leod);
		assertInDiscard(leod);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnOpponentControllingSite4OfTheFellowshipPathDoesNotDiscardHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipPathScenario();

		var leod = scn.GetFreepsCard("leod");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveAlliesToTable(leod);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(3)));
		assertInZone(Zone.SUPPORT, leod);

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(explorer);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// Site 4 of the Fellowship block is no site 4[T] (CR 4.2 "block symbol"): he stays.
		scn.ShadowUseCardAction(explorer);
		assertTrue(scn.ShadowControls(scn.GetSite(4)));
		assertInZone(Zone.SUPPORT, leod);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void HisOwnerControllingHisHomeSiteDoesNotDiscardHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leod = scn.GetFreepsCard("leod");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveAlliesToTable(leod);

		scn.StartGame();
		scn.SkipToSite(5);
		// Player 1 took sites 1 to 4 as the Shadow player on player 2's turns.
		scn.FreepsTakeControlOfSite();
		scn.FreepsTakeControlOfSite();
		scn.FreepsTakeControlOfSite();
		scn.FreepsTakeControlOfSite();
		assertTrue(scn.FreepsControls(scn.GetSite(4)));
		assertInZone(Zone.SUPPORT, leod);

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(explorer);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// The opponent's take finds no site, and is paid for; Léod stays through it.
		assertEquals(0, scn.GetWoundsOn(explorer));
		scn.ShadowUseCardAction(explorer);
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertTrue(scn.FreepsControls(scn.GetSite(4)));
		assertInZone(Zone.SUPPORT, leod);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SkirmishAbilityExertsLeodToMakeAMountedCompanionStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leod = scn.GetFreepsCard("leod");
		var guard1 = scn.GetFreepsCard("guard1");
		var firefoot = scn.GetFreepsCard("firefoot");
		var sam = scn.GetFreepsCard("sam");
		var bill = scn.GetFreepsCard("bill");
		var herdsman = scn.GetFreepsCard("herdsman");
		var snowmane = scn.GetFreepsCard("snowmane");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var chieftain = scn.GetShadowCard("chieftain");
		var warg = scn.GetShadowCard("warg");
		scn.MoveCompanionsToTable(guard1, sam, gimli, aragorn);
		scn.MoveAlliesToTable(leod, herdsman);
		scn.AttachCardsTo(guard1, firefoot);
		scn.AttachCardsTo(sam, bill);
		scn.AttachCardsTo(herdsman, snowmane);
		scn.AttachCardsTo(gimli, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);
		scn.AttachCardsTo(chieftain, warg);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard1, chieftain);
		scn.FreepsResolveSkirmish(guard1);

		assertTrue(scn.IsCharSkirmishingAgainst(guard1, chieftain));
		assertEquals(0, scn.GetWoundsOn(leod));
		// 6 + 1 for Firefoot
		assertEquals(7, scn.GetStrength(guard1));
		assertEquals(3, scn.GetStrength(sam));
		assertTrue(scn.FreepsActionAvailable(leod));

		scn.FreepsUseCardAction(leod);
		assertEquals(1, scn.GetWoundsOn(leod));
		// Mounted companions in and out of the skirmish, of two cultures and races.  Not a mounted ally, a companion
		// bearing a possession that is no mount, a companion bearing nothing, or the mounted minion he fights.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard1, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(herdsman, gimli, aragorn, chieftain, leod));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard1);

		assertEquals(9, scn.GetStrength(guard1));
		assertEquals(3, scn.GetStrength(sam));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheBonusLastsTheSkirmishPhaseItIsMadeInAndAnExhaustedLeodCannotPay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leod = scn.GetFreepsCard("leod");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var firefoot = scn.GetFreepsCard("firefoot");
		var brigand1 = scn.GetShadowCard("brigand1");
		var brigand2 = scn.GetShadowCard("brigand2");
		scn.MoveCompanionsToTable(guard1, guard2);
		scn.MoveAlliesToTable(leod);
		scn.AttachCardsTo(guard2, firefoot);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand1, brigand2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard1, brigand1}, new PhysicalCardImpl[]{guard2, brigand2});
		scn.FreepsResolveSkirmish(guard1);

		// The second Guard, mounted, waits for his own skirmish and is made strength +2 during the first's.  He is
		// the only mounted companion, and is taken without asking.
		assertEquals(7, scn.GetStrength(guard2));
		scn.FreepsUseCardAction(leod);
		assertEquals(9, scn.GetStrength(guard2));
		assertEquals(6, scn.GetStrength(guard1));

		// Vitality 2 with 1 wound: exhausted, and the ability is no longer offered, with a mounted companion on the table.
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(leod));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(leod));
		assertEquals(9, scn.GetStrength(guard2));

		// 6 against 8: the first Guard loses and takes a wound.  In the next skirmish phase the bonus is gone.
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(guard1));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(7, scn.GetStrength(guard2));
		scn.FreepsResolveSkirmish(guard2);
		assertTrue(scn.IsCharSkirmishingAgainst(guard2, brigand2));
		assertEquals(7, scn.GetStrength(guard2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
