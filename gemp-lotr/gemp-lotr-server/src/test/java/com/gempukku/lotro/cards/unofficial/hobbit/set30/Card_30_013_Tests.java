package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_013_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The maneuver phase
	// of turn 1 is at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lore", "30_13");
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion, vitality 3; a fellowship ability only
					put("dwalin", "30_9");      // Dwalin, Brother of Balin: [dwarven] Dwarf companion, vitality 3; a maneuver ability used only on purpose
					put("gloin", "30_11");      // Glóin, Father of Gimli: [dwarven] Dwarf companion, vitality 3; a skirmish ability
					// Dain Ironfoot, Lord of the Iron Hills: a [dwarven] Dwarf ALLY, vitality 3; a start-of-regroup offer only
					put("dain", "32_2");
					// Elrond, Elven Lord: the Hobbit sets' Elrond, an [elven] ally; his start-of-turn heal reaches a Wise
					// ally or himself, and neither is wounded
					put("elrond", "30_21");
					// Elrond, Lord of Rivendell: an Elrond ally of the Decipher sets; his start-of-turn heal of allies
					// whose home is site 3 finds none wounded (the wounds are put on after it)
					put("elrondally", "1_40");
					// Elrond, Elven Lord 7_21: an Elrond who is a companion; cheated in after the start of the turn,
					// so his start-of-turn offer is never made
					put("elrondcomp", "7_21");
					// Lindir, Elven Minstrel: an [elven] Elf ally who is not Elrond; a move trigger for [elven]
					// allies, none wounded
					put("lindir", "33_14");
					put("runner", "31_30");     // Goblin Runner: a [moria] Orc, so that there is a maneuver phase
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void LoreofImladrisStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Lore of Imladris
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Heal a companion (or 2 Dwarf companions if you spot Elrond).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lore");

		assertEquals("Lore of Imladris", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithoutElrondItHealsOneWoundedCompanionOfAnyRaceAndNoAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var lore = scn.GetFreepsCard("lore");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var dain = scn.GetFreepsCard("dain");
		var lindir = scn.GetFreepsCard("lindir");
		scn.MoveCompanionsToTable(balin, dwalin);
		scn.MoveAlliesToTable(dain, lindir);
		scn.MoveCardsToHand(lore);

		scn.StartGame();
		scn.MoveMinionsToTable("runner");
		scn.AddWoundsToChar(bilbo, 2);
		scn.AddWoundsToChar(balin, 2);
		scn.AddWoundsToChar(dain, 1);
		assertEquals(2, scn.GetWoundsOn(bilbo));
		assertEquals(2, scn.GetWoundsOn(balin));
		assertEquals(1, scn.GetWoundsOn(dain));
		assertEquals(0, scn.GetWoundsOn(dwalin));
		assertInHand(lore);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsPlayAvailable(lore));
		scn.FreepsPlayCard(lore);

		// Lindir is an [elven] Elf, but no Elrond: the plain heal. A Hobbit and a Dwarf companion are offered; the
		// wounded Dwarf ally is no companion, and Dwalin has no wound to heal.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bilbo, balin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(dain, dwalin));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(balin);

		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(2, scn.GetWoundsOn(bilbo));
		assertEquals(1, scn.GetWoundsOn(dain));
		assertInDiscard(lore);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoWoundedCompanionItIsStillPlayedAndHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var lore = scn.GetFreepsCard("lore");
		var balin = scn.GetFreepsCard("balin");
		var dain = scn.GetFreepsCard("dain");
		scn.MoveCompanionsToTable(balin);
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToHand(lore);

		scn.StartGame();
		scn.MoveMinionsToTable("runner");
		scn.AddWoundsToChar(dain, 1);
		assertEquals(1, scn.GetWoundsOn(dain));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertInHand(lore);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsPlayAvailable(lore));
		scn.FreepsPlayCard(lore);

		// Nothing is asked: the only wounded character is an ally.
		assertEquals(1, scn.GetWoundsOn(dain));
		assertInDiscard(lore);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void PlayableInTheManeuverPhaseAndNotInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var lore = scn.GetFreepsCard("lore");
		scn.MoveCardsToHand(lore);

		scn.StartGame();
		scn.MoveMinionsToTable("runner");
		scn.AddWoundsToChar(bilbo, 1);
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertInHand(lore);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsPlayAvailable(lore));

		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(lore));
		assertInHand(lore);
		assertEquals(1, scn.GetWoundsOn(bilbo));
	}

	@Test
	public void WithElrondTheAllyItHealsExactlyTwoOfThreeWoundedDwarfCompanionsAndNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var lore = scn.GetFreepsCard("lore");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var gloin = scn.GetFreepsCard("gloin");
		var dain = scn.GetFreepsCard("dain");
		var elrondally = scn.GetFreepsCard("elrondally");
		scn.MoveCompanionsToTable(balin, dwalin, gloin);
		scn.MoveAlliesToTable(dain, elrondally);
		scn.MoveCardsToHand(lore);

		scn.StartGame();
		scn.MoveMinionsToTable("runner");
		scn.AddWoundsToChar(bilbo, 2);
		scn.AddWoundsToChar(balin, 2);
		scn.AddWoundsToChar(dwalin, 2);
		scn.AddWoundsToChar(gloin, 2);
		scn.AddWoundsToChar(dain, 2);
		assertEquals(2, scn.GetWoundsOn(bilbo));
		assertEquals(2, scn.GetWoundsOn(balin));
		assertEquals(2, scn.GetWoundsOn(dwalin));
		assertEquals(2, scn.GetWoundsOn(gloin));
		assertEquals(2, scn.GetWoundsOn(dain));
		assertInHand(lore);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(lore);

		// Elrond is spotted, so the parenthesis is the effect (CR 4.2 "effect"; Q-0035, Q-0389): two Dwarf
		// companions, each healed once (CR 4.2 "healing"). Bilbo is no Dwarf and Dain is no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(balin, dwalin, gloin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, dain));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(balin, dwalin);

		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(1, scn.GetWoundsOn(dwalin));
		assertEquals(2, scn.GetWoundsOn(gloin));
		assertEquals(2, scn.GetWoundsOn(bilbo));
		assertEquals(2, scn.GetWoundsOn(dain));
		assertInDiscard(lore);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithElrondTheCompanionItHealsBothWoundedDwarfCompanionsUnaskedAndNotBilbo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var lore = scn.GetFreepsCard("lore");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var elrondcomp = scn.GetFreepsCard("elrondcomp");
		scn.MoveCompanionsToTable(balin, dwalin);
		scn.MoveCardsToHand(lore);

		scn.StartGame();
		scn.MoveCompanionsToTable(elrondcomp);
		scn.MoveMinionsToTable("runner");
		scn.AddWoundsToChar(bilbo, 2);
		scn.AddWoundsToChar(balin, 2);
		scn.AddWoundsToChar(dwalin, 2);
		assertEquals(2, scn.GetWoundsOn(bilbo));
		assertEquals(2, scn.GetWoundsOn(balin));
		assertEquals(2, scn.GetWoundsOn(dwalin));
		assertEquals(0, scn.GetWoundsOn(elrondcomp));
		assertInHand(lore);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(lore);

		// Exactly two wounded Dwarf companions: both are healed, nothing asked.
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(1, scn.GetWoundsOn(dwalin));
		assertEquals(2, scn.GetWoundsOn(bilbo));
		assertInDiscard(lore);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithElrondAndOneWoundedDwarfCompanionItHealsHimAndNotTheWoundedBilbo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var lore = scn.GetFreepsCard("lore");
		var balin = scn.GetFreepsCard("balin");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(balin);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(lore);

		scn.StartGame();
		scn.MoveMinionsToTable("runner");
		scn.AddWoundsToChar(bilbo, 2);
		scn.AddWoundsToChar(balin, 2);
		assertEquals(2, scn.GetWoundsOn(bilbo));
		assertEquals(2, scn.GetWoundsOn(balin));
		assertInHand(lore);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(lore);

		// With Elrond spotted the plain heal is not the effect, though it could reach Bilbo (CR 4.2 "effect":
		// a conditional effect is used when its condition is met; Q-0389). Of the 2 Dwarf companions, there
		// is one to heal, and he is healed.
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(2, scn.GetWoundsOn(bilbo));
		assertInDiscard(lore);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
