package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_005_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("song", "3_5");
					put("song2", "3_5");

					put("gimli", "1_13");       // Gimli, Son of Glóin: Dwarf companion, vitality 3; a skirmish ability only used on purpose
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, vitality 3; a fellowship ability only used on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4: a companion who could exert and is no Dwarf
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: [shire] Hobbit ally, vitality 3: an ally who could exert and is no Dwarf
					put("lord", "1_21");        // Lord of Moria: [dwarven] support condition; each Dwarf is damage +1

					put("spear", "1_182");      // Goblin Spear: [moria] possession, hand weapon, cost 1, bearer a [moria] minion
					put("sword", "2_50");       // The Balrog's Sword: [moria] artifact, hand weapon, cost 1
					put("chain", "2_53");       // Cave Troll's Chain: [moria] possession that is no weapon, cost 1
					put("scimitar", "1_269");   // Orc Scimitar: [sauron] possession, hand weapon, cost 1
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion, cost 6, with no text

					put("armory", "1_157");     // Uruk-hai Armory: [isengard] support condition; does nothing without an Uruk-hai
					put("dam", "101_65");       // Sirannon Dam: [moria] artifact in the support area, no condition; the one [moria] card that is no condition and can lie in play in a fellowship phase
					put("pinned", "1_192");     // Pinned Down: [moria] support condition; does nothing without a [moria] archer
					put("swarms", "1_183");     // Goblin Swarms: [moria] support condition; its response and ability need [moria] Orcs
					put("throw", "2_71");       // Throw Yourself in Next Time: [moria] condition on a Hobbit companion; bearer may be exerted only by Shadow cards
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SongofDurinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Song of Durin
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: 
		 * Game Text: <b>Tale</b>. To play, exert a Dwarf. Plays to your support area.<br>The twilight cost of each [moria] weapon is +2.<br><b>Fellowship:</b> Discard this condition to discard up to 2 [moria] conditions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("song");

		assertEquals("Song of Durin", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedByExertingADwarfItMakesEachMoriaWeaponCost2More() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bilbo = scn.GetFreepsCard("bilbo");
		var spear = scn.GetShadowCard("spear");
		var sword = scn.GetShadowCard("sword");
		var chain = scn.GetShadowCard("chain");
		var scimitar = scn.GetShadowCard("scimitar");
		var pinned = scn.GetShadowCard("pinned");
		scn.MoveCardsToHand(song, spear, sword, chain, scimitar, pinned);
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(grimir, bilbo);

		scn.StartGame();

		assertEquals(1, scn.GetTwilightCost(spear));
		assertEquals(1, scn.GetTwilightCost(sword));
		assertEquals(1, scn.GetTwilightCost(chain));
		assertEquals(1, scn.GetTwilightCost(scimitar));
		assertEquals(1, scn.GetTwilightCost(pinned));

		assertEquals(0, scn.GetWoundsOn(grimir));
		assertTrue(scn.FreepsPlayAvailable(song));
		scn.FreepsPlayCard(song);
		// A Dwarf companion and a Dwarf ally; not Aragorn, a companion who could exert and is no Dwarf, nor Bilbo, an
		// ally who could exert and is no Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, bilbo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(grimir);

		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertInZone(Zone.SUPPORT, song);

		// The Spear is a [moria] possession and the Sword a [moria] artifact, both weapons: 1 printed + 2.  The Chain
		// is [moria] and no weapon, the Scimitar a weapon and [sauron], and Pinned Down a [moria] card that is no item.
		assertEquals(3, scn.GetTwilightCost(spear));
		assertEquals(3, scn.GetTwilightCost(sword));
		assertEquals(1, scn.GetTwilightCost(chain));
		assertEquals(1, scn.GetTwilightCost(scimitar));
		assertEquals(1, scn.GetTwilightCost(pinned));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlyDwarfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveCardsToHand(song);
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(bilbo);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));

		scn.StartGame();

		// Aragorn and Bilbo could exert but are no Dwarves.
		assertFalse(scn.FreepsPlayAvailable(song));
		assertInZone(Zone.HAND, song);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AMoriaWeaponPlayedWhileItIsInPlayTakes2MoreTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var spear = scn.GetShadowCard("spear");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(song);
		scn.MoveCardsToHand(spear);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for Ettenmoors
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(spear);

		assertInZone(Zone.ATTACHED, spear);
		// 1 printed + 2
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FellowshipAbilityDiscardsThisCopyToDiscardUpTo2MoriaConditions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var song2 = scn.GetFreepsCard("song2");
		var lord = scn.GetFreepsCard("lord");
		var ownPinned = scn.GetFreepsCard("pinned");
		var pinned = scn.GetShadowCard("pinned");
		var swarms = scn.GetShadowCard("swarms");
		var thrown = scn.GetShadowCard("throw");
		var armory = scn.GetShadowCard("armory");
		var dam = scn.GetShadowCard("dam");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(song, song2, lord, ownPinned, pinned, swarms, armory, dam);
		scn.AttachCardsTo(frodo, thrown);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, song, song2, lord, ownPinned, pinned, swarms, armory, dam);
		assertInZone(Zone.ATTACHED, thrown);
		assertTrue(scn.FreepsActionAvailable(song));
		scn.FreepsUseCardAction(song);

		// The cost is paid first: this copy goes, the other stays.
		assertInZone(Zone.DISCARD, song);
		assertInZone(Zone.SUPPORT, song2);

		// Two [moria] conditions in the support area and one borne by a companion; not Uruk-hai Armory, a condition
		// that is not [moria], nor Sirannon Dam, a [moria] card that is no condition, nor the Free Peoples conditions.
		assertTrue(scn.FreepsHasCardChoiceAvailable(pinned, swarms, thrown));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(armory, dam, song2, lord));
		// The Free Peoples player's own Shadow cards are inactive on his turn (CR 4.2 "active").
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ownPinned));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(pinned, thrown);

		assertInZone(Zone.DISCARD, pinned, thrown);
		assertInZone(Zone.SUPPORT, swarms, armory, dam, song2, lord, ownPinned);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoMoriaConditionInPlayTheSongIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var armory = scn.GetShadowCard("armory");
		var dam = scn.GetShadowCard("dam");
		scn.MoveCardsToSupportArea(song, armory, dam);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, song, armory, dam);
		// The ability has a cost, so it is offered with nothing it could discard.
		assertTrue(scn.FreepsActionAvailable(song));
		scn.FreepsUseCardAction(song);

		assertInZone(Zone.DISCARD, song);
		assertInZone(Zone.SUPPORT, armory, dam);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var pinned = scn.GetShadowCard("pinned");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(song, pinned);

		scn.StartGame();
		assertTrue(scn.FreepsActionAvailable(song));

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(song));
		assertInZone(Zone.SUPPORT, song, pinned);
	}
}
