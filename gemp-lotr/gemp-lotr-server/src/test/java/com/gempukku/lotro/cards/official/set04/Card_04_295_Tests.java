package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_295_Tests
{

	// The Towers path: Weland's home is its site 4, White Rocks 4_346 (+1 to its Shadow number for each mounted
	// companion, only once the fellowship is there); site 2 is Eastfold 4_331, whose Shadow ability needs 3
	// [isengard] trackers.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("weland", "4_295");

					put("guard1", "5_83");      // Household Guard: [rohan] Man companion, strength 6, vitality 3; valiant and nothing else
					put("firefoot", "13_126");  // Firefoot, Mearas of The Mark: a mount a [rohan] Man bears, strength +1; its texts need Éomer or a play
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound [shire] Hobbit companion; abilities used only on purpose
					put("bill", "13_143");      // Bill the Pony, Dearly-loved: a mount a Ring-bound Hobbit bears; a regroup ability used only on purpose
					put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] Man ally, home 3T; a regroup ability used only on purpose
					put("snowmane", "13_136");  // Snowmane, Noble Mearas: a mount a [rohan] Man bears, strength +1; its texts need Théoden or a play
					put("gimli", "1_13");       // Gimli, Son of Glóin: a Dwarf companion; a skirmish ability used only on purpose
					put("armor", "1_8");        // Dwarven Armor: a possession that is no mount, borne by a Dwarf
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, strength 8; a maneuver ability used only on purpose

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
					put("weland", "4_295");
					put("explorer", "4_250");   // Southron Explorer: Regroup: exert this minion and remove (5) to take control of a site
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WelandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Weland, Smith of the Riddermark
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
		 * 	Discard Weland if an opponent controls his home site.
		 * 	<b>Fellowship:</b> Exert Weland to heal a mounted companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("weland");

		assertEquals("Weland", card.getBlueprint().getTitle());
		assertEquals("Smith of the Riddermark", card.getBlueprint().getSubtitle());
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

		var weland = scn.GetFreepsCard("weland");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveAlliesToTable(weland);

		scn.StartGame();
		scn.SkipToSite(5);
		// Both markers are at site 5: sites 1 to 4 are behind them.  The opponent holds sites 1 and 2 already.
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		assertTrue(scn.ShadowControls(scn.GetSite(2)));
		assertInZone(Zone.SUPPORT, weland);

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(explorer);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Site 3: not his home, and he stays.
		scn.ShadowUseCardAction(explorer);
		assertTrue(scn.ShadowControls(scn.GetSite(3)));
		assertInZone(Zone.SUPPORT, weland);

		// Site 4[T], his home: he is discarded.
		scn.FreepsPass();
		scn.ShadowUseCardAction(explorer);
		assertTrue(scn.ShadowControls(scn.GetSite(4)));
		assertInDiscard(weland);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PlayedWhileAnOpponentControlsHisHomeSiteHeIsDiscardedAtOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weland = scn.GetFreepsCard("weland");
		scn.MoveCardsToHand(weland);

		scn.StartGame();
		scn.SkipToSite(5);
		// The opponent took sites 1 to 4 on earlier turns.
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.FreepsRefreshDecision();
		assertTrue(scn.ShadowControls(scn.GetSite(4)));
		assertInHand(weland);
		assertTrue(scn.FreepsPlayAvailable(weland));

		scn.FreepsPlayCard(weland);
		assertInDiscard(weland);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnOpponentControllingSite4OfTheFellowshipPathDoesNotDiscardHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipPathScenario();

		var weland = scn.GetFreepsCard("weland");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveAlliesToTable(weland);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(3)));
		assertInZone(Zone.SUPPORT, weland);

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(explorer);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// Site 4 of the Fellowship block is no site 4[T] (CR 4.2 "block symbol"): he stays.
		scn.ShadowUseCardAction(explorer);
		assertTrue(scn.ShadowControls(scn.GetSite(4)));
		assertInZone(Zone.SUPPORT, weland);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void HisOwnerControllingHisHomeSiteDoesNotDiscardHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weland = scn.GetFreepsCard("weland");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveAlliesToTable(weland);

		scn.StartGame();
		scn.SkipToSite(5);
		// Player 1 took sites 1 to 4 as the Shadow player on player 2's turns.
		scn.FreepsTakeControlOfSite();
		scn.FreepsTakeControlOfSite();
		scn.FreepsTakeControlOfSite();
		scn.FreepsTakeControlOfSite();
		assertTrue(scn.FreepsControls(scn.GetSite(4)));
		assertInZone(Zone.SUPPORT, weland);

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(explorer);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// The opponent's take finds no site, and is paid for; Weland stays through it.
		assertEquals(0, scn.GetWoundsOn(explorer));
		scn.ShadowUseCardAction(explorer);
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertTrue(scn.FreepsControls(scn.GetSite(4)));
		assertInZone(Zone.SUPPORT, weland);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void FellowshipAbilityExertsWelandToHealAMountedCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weland = scn.GetFreepsCard("weland");
		var guard1 = scn.GetFreepsCard("guard1");
		var firefoot = scn.GetFreepsCard("firefoot");
		var sam = scn.GetFreepsCard("sam");
		var bill = scn.GetFreepsCard("bill");
		var herdsman = scn.GetFreepsCard("herdsman");
		var snowmane = scn.GetFreepsCard("snowmane");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(guard1, sam, gimli, aragorn);
		scn.MoveAlliesToTable(weland, herdsman);
		scn.AttachCardsTo(guard1, firefoot);
		scn.AttachCardsTo(sam, bill);
		scn.AttachCardsTo(herdsman, snowmane);
		scn.AttachCardsTo(gimli, armor);
		scn.AddWoundsToChar(guard1, 2);
		scn.AddWoundsToChar(sam, 1);
		scn.AddWoundsToChar(herdsman, 1);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(aragorn, 1);
		assertAttachedTo(firefoot, guard1);
		assertAttachedTo(bill, sam);
		assertAttachedTo(snowmane, herdsman);
		assertAttachedTo(armor, gimli);
		assertEquals(2, scn.GetWoundsOn(guard1));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(herdsman));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(weland));
		assertTrue(scn.FreepsActionAvailable(weland));
		scn.FreepsUseCardAction(weland);
		assertEquals(1, scn.GetWoundsOn(weland));
		// Wounded mounted companions of two cultures and races.  Not a wounded mounted ally, a wounded companion
		// bearing a possession that is no mount, a wounded companion bearing nothing, or Weland himself.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard1, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(herdsman, gimli, aragorn, weland));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard1);

		// One wound of two healed.
		assertEquals(1, scn.GetWoundsOn(guard1));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(herdsman));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoMountedCompanionWoundedTheAbilityIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weland = scn.GetFreepsCard("weland");
		var guard1 = scn.GetFreepsCard("guard1");
		var firefoot = scn.GetFreepsCard("firefoot");
		var herdsman = scn.GetFreepsCard("herdsman");
		var snowmane = scn.GetFreepsCard("snowmane");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(guard1, gimli);
		scn.MoveAlliesToTable(weland, herdsman);
		scn.AttachCardsTo(guard1, firefoot);
		scn.AttachCardsTo(herdsman, snowmane);
		scn.AttachCardsTo(gimli, armor);
		scn.AddWoundsToChar(herdsman, 1);
		scn.AddWoundsToChar(gimli, 1);
		assertAttachedTo(firefoot, guard1);
		assertAttachedTo(snowmane, herdsman);
		assertAttachedTo(armor, gimli);
		assertEquals(0, scn.GetWoundsOn(guard1));
		assertEquals(1, scn.GetWoundsOn(herdsman));
		assertEquals(1, scn.GetWoundsOn(gimli));

		scn.StartGame();

		// The mounted companion is unwounded; the wounded are an ally and a companion who is not mounted.
		assertTrue(scn.FreepsActionAvailable(weland));
		scn.FreepsUseCardAction(weland);
		assertEquals(1, scn.GetWoundsOn(weland));
		assertEquals(0, scn.GetWoundsOn(guard1));
		assertEquals(1, scn.GetWoundsOn(herdsman));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnExhaustedWelandCannotPayAndTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weland = scn.GetFreepsCard("weland");
		var guard1 = scn.GetFreepsCard("guard1");
		var firefoot = scn.GetFreepsCard("firefoot");
		scn.MoveCompanionsToTable(guard1);
		scn.MoveAlliesToTable(weland);
		scn.AttachCardsTo(guard1, firefoot);
		scn.AddWoundsToChar(guard1, 1);
		scn.AddWoundsToChar(weland, 1);
		assertAttachedTo(firefoot, guard1);
		assertEquals(1, scn.GetWoundsOn(guard1));
		assertEquals(1, scn.GetWoundsOn(weland));

		scn.StartGame();

		// Vitality 2 with 1 wound: exhausted, with a wounded mounted companion on the table.
		assertFalse(scn.FreepsActionAvailable(weland));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetWoundsOn(guard1));
	}
}
