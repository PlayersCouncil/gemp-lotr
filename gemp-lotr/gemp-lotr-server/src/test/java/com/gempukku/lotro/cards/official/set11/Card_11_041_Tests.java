package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_041_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("attack", "11_41");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, resistance 6, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] Dwarf, resistance 6; a skirmish ability used only on purpose
					// Elven Scout: [elven] Elf, resistance 7; muster only while another Elf can be spotted (none here)
					put("scout", "11_18");
					// Thrarin, Dwarven Smith: Dwarf ally (resistance 0 by rule), vitality 2; exerts in the maneuver phase to skirmish
					put("thrarin", "1_27");
					// Gollum, Nasty Treacherous Creature: [gollum] Gollum, strength 5; a regroup ability only
					put("gollum", "5_24");
					// Gollum, Half a Wraith Himself: [wraith] Gollum, strength 5; its trigger answers a Nazgûl played and its
					// assignment ability wants threats or a companion of cost 0 (neither here)
					put("wraithgollum", "103_66");
					// Shelob, Last Child of Ungoliant: a [gollum] minion who is no Gollum; enduring, fierce
					put("shelob", "8_26");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3; no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FrenziedAttackStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Frenzied Attack
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: If a minion is skirmishing a Free Peoples character who has resistance 2 or less, spot Gollum to wound that character twice.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("attack");

		assertEquals("Frenzied Attack", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void GollumSkirmishingACompanionOfResistance2WoundsThatCompanionTwiceAndNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var attack = scn.GetShadowCard("attack");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToHand(attack);

		scn.StartGame();
		assertInHand(attack);
		// Each burden is resistance -1 for every companion: Aragorn and Gimli 6 - 4.
		scn.AddBurdens(4);
		assertEquals(2, scn.GetResistance(aragorn));
		assertEquals(2, scn.GetResistance(gimli));
		scn.MoveMinionsToTable(gollum);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowPlayAvailable(attack));
		scn.ShadowPlayCard(attack);

		// Two wounds on the companion Gollum is skirmishing; Gimli, of the same resistance and in no skirmish, none.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInDiscard(attack);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnAllySkirmishingAnotherMinionWhileGollumIsSpottedIsWoundedAndKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);

		var attack = scn.GetShadowCard("attack");
		var gollum = scn.GetShadowCard("gollum");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(attack);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, troop);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		// Vitality 2 with 1 wound: a wound kills him, and he could not exert at all.
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(0, scn.GetResistance(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, troop);
		scn.ShadowDeclineAssignments();
		assertFalse(scn.IsCharAssigned(gollum));

		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(attack));
		scn.ShadowPlayCard(attack);

		assertInZone(Zone.DEAD, thrarin);
		assertInDiscard(attack);
		// His was the only skirmish, over once he is killed.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotPlayableWhileTheCharacterSkirmishingHasResistance3AndPlayableInTheSkirmishOfOneWithResistance2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var scout = scn.GetFreepsCard("scout");
		scn.MoveCompanionsToTable(aragorn, scout);

		var attack = scn.GetShadowCard("attack");
		var wraithgollum = scn.GetShadowCard("wraithgollum");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(attack);

		scn.StartGame();
		scn.AddBurdens(4);
		// Aragorn 6 - 4; the Elven Scout 7 - 4.
		assertEquals(2, scn.GetResistance(aragorn));
		assertEquals(3, scn.GetResistance(scout));
		scn.MoveMinionsToTable(wraithgollum, troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, wraithgollum}, new PhysicalCardImpl[]{scout, troop});

		// The Elven Scout's skirmish: resistance 3.  Aragorn, of resistance 2, is assigned and waiting, not skirmishing.
		scn.FreepsResolveSkirmish(scout);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(attack));
		scn.ShadowPass();

		// Aragorn's skirmish, against the [wraith] Gollum, who is the Gollum spotted.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowPlayAvailable(attack));
		scn.ShadowPlayCard(attack);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotPlayableWithShelobAndNoGollum() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var attack = scn.GetShadowCard("attack");
		var shelob = scn.GetShadowCard("shelob");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToHand(attack, gollum);

		scn.StartGame();
		assertInHand(attack, gollum);
		scn.AddBurdens(4);
		assertEquals(2, scn.GetResistance(aragorn));
		scn.MoveMinionsToTable(shelob);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// A [gollum] minion skirmishing a companion of resistance 2, and Gollum only in hand.
		assertInHand(gollum);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(attack));
	}
}
