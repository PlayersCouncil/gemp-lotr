package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_031_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("draws", "18_31");
					// Chasm's Edge: [gollum] Shadow condition in the support area; it strengthens Gollum and nothing else
					put("chasm", "13_44");
					put("chasm2", "13_44");
					// Gollum, Nasty Treacherous Creature: a [gollum] minion; a regroup ability used only on purpose
					put("gollum", "5_24");
					// Sméagol, Poor Creature: a [gollum] companion of the Free Peoples player's; a skirmish ability
					// used only on purpose
					put("smeagol", "6_45");
					// Ships of Great Draught: [raider] Shadow possession in the support area; its abilities are used
					// only on purpose
					put("ships", "8_65");
					put("ships2", "8_65");

					// Ruffian: [men] Man, cost 3, site 2; strength bonuses by region only
					put("ruffian", "18_72");
					// Goblin Man: [isengard] Orc, cost 2, site 2; discarded if underground (site 2 is not), and a
					// Shadow ability used only on purpose
					put("goblinman", "2_42");
					// Morgul Hunter: [sauron] Orc, cost 5, site 6; strength +1 for each companion
					put("hunter", "1_256");
					// Goblin Patrol Troop: [moria] Orc, cost 6, site 4; no text
					put("troop", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ItDrawsHimStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: It Draws Him
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Spot a [gollum] card to play a minion. Its twilight cost -1 (or -3 if it is roaming).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("draws");

		assertEquals("It Draws Him", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AMinionThatIsNotRoamingIsPlayedForOneLessAndTheNextMinionPaysInFull() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var draws = scn.GetShadowCard("draws");
		var chasm = scn.GetShadowCard("chasm");
		var ruffian = scn.GetShadowCard("ruffian");
		var goblinman = scn.GetShadowCard("goblinman");
		var hunter = scn.GetShadowCard("hunter");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToSupportArea(chasm);
		scn.MoveCardsToHand(draws, ruffian, goblinman, hunter, ships);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, chasm);
		assertInHand(draws, ruffian, goblinman, hunter, ships);
		scn.SetTwilight(7);
		scn.FreepsPass();

		// 7 set + 1 companion + 2 for site 2
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(draws));
		scn.ShadowPlayCard(draws);

		// A minion of each kind in hand: which kind is asked.
		assertTrue(scn.ShadowChoiceAvailable("non-roaming"));
		scn.ShadowChoose("non-roaming");
		// The two minions of site 2, of two cultures and races; not the roaming minion, nor a card that is no minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(ruffian, goblinman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hunter, ships));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(ruffian);

		assertInZone(Zone.SHADOW_CHARACTERS, ruffian);
		assertFalse(scn.HasKeyword(ruffian, Keyword.ROAMING));
		// Ruffian costs 3 - 1
		assertEquals(8, scn.GetTwilight());
		assertInDiscard(draws);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The discount was Ruffian's alone: Goblin Man, played next, costs his full 2.
		scn.ShadowPlayCard(goblinman);
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ARoamingMinionIsPlayedForThreeLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var draws = scn.GetShadowCard("draws");
		var chasm = scn.GetShadowCard("chasm");
		var ruffian = scn.GetShadowCard("ruffian");
		var hunter = scn.GetShadowCard("hunter");
		var troop = scn.GetShadowCard("troop");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToSupportArea(chasm);
		scn.MoveCardsToHand(draws, ruffian, hunter, troop, ships);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, chasm);
		assertInHand(draws, ruffian, hunter, troop, ships);
		scn.SetTwilight(7);
		scn.FreepsPass();

		// 7 set + 1 companion + 2 for site 2
		assertEquals(10, scn.GetTwilight());
		scn.ShadowPlayCard(draws);

		assertTrue(scn.ShadowChoiceAvailable("non-roaming"));
		scn.ShadowChoose("Play a roaming");
		// The two minions of a site above 2, of two cultures; not the minion of site 2, nor a card that is no minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(hunter, troop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ruffian, ships));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(hunter);

		assertInZone(Zone.SHADOW_CHARACTERS, hunter);
		assertTrue(scn.HasKeyword(hunter, Keyword.ROAMING));
		// Morgul Hunter costs 5 + 2 for roaming - 3
		assertEquals(6, scn.GetTwilight());
		assertInDiscard(draws);
		assertInHand(ruffian, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOnlyARoamingMinionInHandItIsPlayedForThreeLessUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var draws = scn.GetShadowCard("draws");
		var chasm = scn.GetShadowCard("chasm");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(chasm);
		scn.MoveCardsToHand(draws, troop);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, chasm);
		assertInHand(draws, troop);
		scn.SetTwilight(7);
		scn.FreepsPass();

		// 7 set + 1 companion + 2 for site 2
		assertEquals(10, scn.GetTwilight());
		scn.ShadowPlayCard(draws);

		// No minion of site 2 or less in hand: the roaming branch is the one carried out, with nothing asked.
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		// Goblin Patrol Troop costs 6 + 2 for roaming - 3
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilAGollumCardIsInPlayAndTheFreePeoplesPlayersGollumCompanionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var draws = scn.GetShadowCard("draws");
		var chasm2 = scn.GetShadowCard("chasm2");
		var ships = scn.GetShadowCard("ships");
		var ruffian = scn.GetShadowCard("ruffian");
		scn.MoveCardsToSupportArea(ships);
		scn.MoveCardsToHand(draws, chasm2, ruffian);

		var smeagol = scn.GetFreepsCard("smeagol");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, ships);
		assertInHand(draws, chasm2, ruffian);
		scn.SetTwilight(7);
		scn.FreepsPass();

		// A [gollum] card in hand and a card of another culture in play: nothing to spot.
		assertFalse(scn.ShadowPlayAvailable(draws));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.MoveCompanionsToTable(smeagol);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(draws));
		scn.ShadowPlayCard(draws);
		// Ruffian, the one minion in hand, is played unasked: 10 - (3 - 1)
		assertInZone(Zone.SHADOW_CHARACTERS, ruffian);
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AGollumMinionInPlayIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var draws = scn.GetShadowCard("draws");
		var gollum = scn.GetShadowCard("gollum");
		var ships = scn.GetShadowCard("ships");
		var ruffian = scn.GetShadowCard("ruffian");
		scn.MoveCardsToSupportArea(ships);
		scn.MoveCardsToHand(draws, ruffian);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, ships);
		assertInHand(draws, ruffian);
		scn.SetTwilight(7);
		scn.FreepsPass();

		assertFalse(scn.ShadowPlayAvailable(draws));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.MoveMinionsToTable(gollum);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(draws));
		scn.ShadowPlayCard(draws);
		assertInZone(Zone.SHADOW_CHARACTERS, ruffian);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNoMinionInHandOrWithThePoolShortOfTheMinionsReducedCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var draws = scn.GetShadowCard("draws");
		var chasm = scn.GetShadowCard("chasm");
		var ships2 = scn.GetShadowCard("ships2");
		var ruffian = scn.GetShadowCard("ruffian");
		scn.MoveCardsToSupportArea(chasm);
		scn.MoveCardsToHand(draws, ships2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, chasm);
		assertInHand(draws, ships2);
		scn.SetTwilight(7);
		scn.FreepsPass();

		// A [gollum] card to spot, but no minion in hand to play.
		assertFalse(scn.ShadowPlayAvailable(draws));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Ruffian costs 3 - 1: one short of it, and then exactly it.
		scn.MoveCardsToHand(ruffian);
		assertInHand(ruffian);
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(draws));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(draws));
		scn.ShadowPlayCard(draws);
		assertInZone(Zone.SHADOW_CHARACTERS, ruffian);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
