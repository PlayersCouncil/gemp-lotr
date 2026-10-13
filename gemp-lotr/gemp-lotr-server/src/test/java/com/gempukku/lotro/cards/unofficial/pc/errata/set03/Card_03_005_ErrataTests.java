package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_005_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("song", "53_5");
					put("song2", "53_5");

					put("gimli", "1_13");       // Gimli, Son of Glóin: Dwarf companion, vitality 3; a skirmish ability only used on purpose
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, vitality 3; a fellowship ability only used on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4: a companion who could exert and is no Dwarf

					put("spear", "1_182");      // Goblin Spear: [moria] possession, hand weapon, cost 1, bearer a [moria] minion
					put("sword", "2_50");       // The Balrog's Sword: [moria] artifact, hand weapon, cost 1
					put("chain", "2_53");       // Cave Troll's Chain: [moria] possession that is not a weapon, cost 1
					put("scimitar", "1_269");   // Orc Scimitar: [sauron] possession, hand weapon, cost 1
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion with no text

					put("armory", "1_157");     // Uruk-hai Armory: [isengard] support condition; does nothing without an Uruk-hai
					put("bowmen", "1_264");     // Orc Bowmen: [sauron] support condition; does nothing without a [sauron] Orc
					put("pinned", "1_192");     // Pinned Down: [moria] support condition; does nothing without a [moria] archer
					put("swarms", "1_183");     // Goblin Swarms: [moria] support condition; its response and ability need [moria] Orcs winning skirmishes
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
		 * Subtype: Support Area
		 * Game Text: <b>Tale</b>. To play, exert a Dwarf.
		 * The twilight cost of each [moria] weapon is +1.
		 * Fellowship: Discard this condition to hinder a Shadow condition (or discard up to 2 [moria] conditions).
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
	public void PlayedFor0ByExertingADwarfItMakesEachMoriaWeaponCost1More() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var spear = scn.GetShadowCard("spear");
		var sword = scn.GetShadowCard("sword");
		var chain = scn.GetShadowCard("chain");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.MoveCardsToHand(song, spear, sword, chain, scimitar);
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();

		assertEquals(1, scn.GetTwilightCost(spear));
		assertEquals(1, scn.GetTwilightCost(sword));
		assertEquals(1, scn.GetTwilightCost(chain));
		assertEquals(1, scn.GetTwilightCost(scimitar));
		assertEquals(0, scn.GetTwilight());

		assertEquals(0, scn.GetWoundsOn(grimir));
		assertTrue(scn.FreepsPlayAvailable(song));
		scn.FreepsPlayCard(song);
		// A Dwarf companion and a Dwarf ally; not Aragorn, a companion who could exert and is no Dwarf
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(grimir);

		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, song);

		// The Spear is a [moria] possession and the Sword a [moria] artifact; the Chain is [moria] and no weapon,
		// the Scimitar a weapon and [sauron]
		assertEquals(2, scn.GetTwilightCost(spear));
		assertEquals(2, scn.GetTwilightCost(sword));
		assertEquals(1, scn.GetTwilightCost(chain));
		assertEquals(1, scn.GetTwilightCost(scimitar));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenNoDwarfCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(song);
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.AddWoundsToChar(gimli, 2);

		scn.StartGame();

		// Gimli is exhausted; Aragorn could exert but is not a Dwarf
		assertFalse(scn.FreepsPlayAvailable(song));
		assertInZone(Zone.HAND, song);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AMoriaWeaponPlayedWhileItIsInPlayTakes1MoreTwilight() throws DecisionResultInvalidException, CardNotFoundException {
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
		// 1 printed + 1
		assertEquals(11, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FellowshipActionDiscardsThisCopyToHinderAShadowCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var song2 = scn.GetFreepsCard("song2");
		var bowmen = scn.GetShadowCard("bowmen");
		var armory = scn.GetShadowCard("armory");
		var ownBowmen = scn.GetFreepsCard("bowmen");
		var spear = scn.GetShadowCard("spear");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(song, song2, bowmen, armory, ownBowmen);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, spear);

		assertTrue(scn.FreepsActionAvailable(song));
		scn.FreepsUseCardAction(song);

		// No [moria] condition is in play: the Spear is a [moria] possession
		assertTrue(scn.FreepsHasCardChoiceAvailable(armory, bowmen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(song2, spear));
		// The Free Peoples player's own Shadow cards are inactive on his turn (CR 4.2 "active")
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ownBowmen));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertInZone(Zone.DISCARD, song);
		scn.FreepsChooseCard(armory);

		assertTrue(scn.IsHindered(armory));
		assertFalse(scn.IsHindered(bowmen));
		assertFalse(scn.IsHindered(ownBowmen));
		assertFalse(scn.IsHindered(song2));
		assertFalse(scn.IsHindered(spear));
		assertInZone(Zone.SUPPORT, song2);
		assertInZone(Zone.DISCARD, song);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithAMoriaConditionInPlayTheActionMustDiscardUpTo2MoriaConditionsAndHindersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var ownPinned = scn.GetFreepsCard("pinned");
		var pinned = scn.GetShadowCard("pinned");
		var swarms = scn.GetShadowCard("swarms");
		var thrown = scn.GetShadowCard("throw");
		var armory = scn.GetShadowCard("armory");
		var spear = scn.GetShadowCard("spear");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(song, ownPinned, pinned, swarms, armory);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, spear);
		scn.AttachCardsTo(frodo, thrown);

		scn.FreepsUseCardAction(song);
		// No choice between hindering and discarding: straight to the [moria] conditions
		assertFalse(scn.FreepsChoiceAvailable("Hinder"));

		assertTrue(scn.FreepsHasCardChoiceAvailable(pinned, swarms, thrown));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(armory, spear));
		// The Free Peoples player's own Shadow cards are inactive on his turn (CR 4.2 "active")
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ownPinned));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(pinned, thrown);

		assertInZone(Zone.DISCARD, pinned);
		assertInZone(Zone.DISCARD, thrown);
		assertInZone(Zone.SUPPORT, swarms);
		assertInZone(Zone.SUPPORT, ownPinned);
		assertInZone(Zone.SUPPORT, armory);
		assertFalse(scn.IsHindered(armory));
		assertInZone(Zone.DISCARD, song);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var armory = scn.GetShadowCard("armory");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(song, armory);

		scn.StartGame();
		assertTrue(scn.FreepsActionAvailable(song));

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(song));
		assertInZone(Zone.SUPPORT, song);
		assertFalse(scn.IsHindered(armory));
	}
}
