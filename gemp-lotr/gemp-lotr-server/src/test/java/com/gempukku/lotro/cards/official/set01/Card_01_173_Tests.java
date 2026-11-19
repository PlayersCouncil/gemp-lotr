package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_173_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armory", "1_173");
					put("armory2", "1_173");    // a second copy: the card is not unique and says "this condition"

					put("spear", "1_182");      // Goblin Spear: [moria] hand weapon (possession), twilight 1, on a [moria] minion
					put("bow", "33_40");        // Goblin Bow: [moria] ranged weapon (possession), twilight 0, on a [moria] Orc
					put("balrogsword", "2_50"); // The Balrog's Sword: [moria] artifact hand weapon, twilight 1, on The Balrog
					put("chain", "2_53");       // Cave Troll's Chain: [moria] possession that is no weapon, twilight 1, on Cave Troll of Moria
					put("banner", "6_98");      // Banner of the Eye: [sauron] hand weapon, twilight 0, on a [sauron] Orc

					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1
					put("scout", "1_191");      // Moria Scout: [moria] Orc, strength 6, vitality 2; a play trigger only
					put("balrog", "12_79");     // The Balrog: bearer for the sword; his text acts only at an underground site (cheated in)
					put("troll", "1_165");      // Cave Troll of Moria: a [moria] minion who is no Orc, vitality 4 (cheated in)
					put("soldier", "1_271");    // Orc Soldier: a [sauron] Orc, vitality 2; a skirmish ability used only on purpose

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, vitality 4
					put("sword", "1_112");      // Ranger's Sword: Aragorn is damage +1
					put("defend1", "4_115");    // Defend It and Hope: exert a [gondor] Man to wound a minion once
					put("defend2", "4_115");
					put("defend3", "4_115");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GoblinArmoryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Goblin Armory
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Plays to your support area.
		 * Each time you play a [moria] weapon, add (1).
		 * <b>Response:</b> If a [moria] Orc is about to take a wound, discard this condition to prevent that wound.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("armory");

		assertEquals("Goblin Armory", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AddsOneEachTimeAMoriaWeaponIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armory = scn.GetShadowCard("armory");
		var spear = scn.GetShadowCard("spear");
		var bow = scn.GetShadowCard("bow");
		var runner = scn.GetShadowCard("runner");
		var scout = scn.GetShadowCard("scout");
		var balrog = scn.GetShadowCard("balrog");
		var balrogsword = scn.GetShadowCard("balrogsword");
		scn.MoveCardsToSupportArea(armory);
		scn.MoveCardsToHand(spear, bow, balrogsword);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, scout, balrog);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		int before = scn.GetTwilight();

		// A hand weapon: 1 to play, 1 added.
		scn.ShadowPlayCard(spear);
		scn.ShadowChooseCard(runner);
		assertAttachedTo(spear, runner);
		assertEquals(before, scn.GetTwilight());

		// A ranged weapon: 0 to play, 1 added again.
		scn.ShadowPlayCard(bow);
		scn.ShadowChooseCard(scout);
		assertAttachedTo(bow, scout);
		assertEquals(before + 1, scn.GetTwilight());

		// An artifact weapon: 1 to play, 1 added again.
		scn.ShadowPlayCard(balrogsword);
		assertAttachedTo(balrogsword, balrog);
		assertEquals(before + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AddsNothingForAMoriaCardThatIsNoWeaponOrAWeaponOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armory = scn.GetShadowCard("armory");
		var armory2 = scn.GetShadowCard("armory2");
		var chain = scn.GetShadowCard("chain");
		var banner = scn.GetShadowCard("banner");
		var troll = scn.GetShadowCard("troll");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(armory);
		scn.MoveCardsToHand(chain, banner, armory2);

		scn.StartGame();
		scn.MoveMinionsToTable(troll, soldier);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		int before = scn.GetTwilight();

		// A [moria] possession that is no weapon: 1 to play, nothing added.
		scn.ShadowPlayCard(chain);
		assertAttachedTo(chain, troll);
		assertEquals(before - 1, scn.GetTwilight());

		// A [sauron] weapon: 0 to play, nothing added.
		scn.ShadowPlayCard(banner);
		assertAttachedTo(banner, soldier);
		assertEquals(before - 1, scn.GetTwilight());

		// A [moria] condition: 0 to play, nothing added.
		scn.ShadowPlayCard(armory2);
		assertInZone(Zone.SUPPORT, armory2);
		assertEquals(before - 1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ResponsePreventsAWoundToAMoriaOrcAndNotToAMinionWhoIsNoMoriaOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armory = scn.GetShadowCard("armory");
		var armory2 = scn.GetShadowCard("armory2");
		var runner = scn.GetShadowCard("runner");
		var troll = scn.GetShadowCard("troll");
		var soldier = scn.GetShadowCard("soldier");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defend1 = scn.GetFreepsCard("defend1");
		var defend2 = scn.GetFreepsCard("defend2");
		var defend3 = scn.GetFreepsCard("defend3");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend1, defend2, defend3);
		scn.MoveCardsToSupportArea(armory, armory2);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troll, soldier);

		scn.SkipToPhase(Phase.MANEUVER);

		// A [moria] minion who is no Orc: no response is offered and the wound is taken.
		scn.FreepsPlayCard(defend1);
		scn.FreepsChooseCard(troll);
		assertEquals(1, scn.GetWoundsOn(troll));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.ShadowPass();

		// An Orc of another culture: the same.
		scn.FreepsPlayCard(defend2);
		scn.FreepsChooseCard(soldier);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.ShadowPass();

		// A [moria] Orc: each copy offers its response.
		scn.FreepsPlayCard(defend3);
		scn.FreepsChooseCard(runner);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(armory));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(armory2));
		scn.ShadowChooseOptionalTrigger(armory);

		assertInDiscard(armory);
		assertInZone(Zone.SUPPORT, armory2);
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		// The event's cost stays paid.
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void EachResponsePreventsOneWoundOfADamagePlusOneLoss() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armory = scn.GetShadowCard("armory");
		var scout = scn.GetShadowCard("scout");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveCardsToSupportArea(armory);

		scn.StartGame();
		scn.MoveMinionsToTable(scout);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, scout);
		scn.FreepsResolveSkirmish(aragorn);
		// Aragorn 8 +2 against 6: the Scout loses and takes 2 wounds (damage +1); 10 does not double 6.
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(armory));
		scn.ShadowChooseOptionalTrigger(armory);

		// The second wound is taken: there is no Armory left to answer it.
		assertInDiscard(armory);
		assertEquals(1, scn.GetWoundsOn(scout));
		assertInZone(Zone.SHADOW_CHARACTERS, scout);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
