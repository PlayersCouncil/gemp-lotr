package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_013_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rope", "9_13");
					put("arwen", "1_30");      // Arwen, Daughter of Elrond: an Elf companion, cost 2, no requirement; +3 only against a Nazgul
					put("orophin", "1_56");    // Orophin, Lorien Bowman: an Elf ally, home 6F; an archery ability only
					put("pippin", "103_31");   // Pippin, Trollslayer: a [gondor] Hobbit companion (cheated in); a skirmish ability only
					put("bounder", "1_286");   // Bounder: a [shire] Hobbit ally, home 2F; a skirmish ability only
					put("gimli", "1_13");      // Gimli, Son of Gloin: a Dwarf companion, neither Elf nor Hobbit
					put("aragorn", "1_89");    // Aragorn, Ranger of the North: a Man companion, neither Elf nor Hobbit

					put("warrior", "1_185");   // Goblin Warrior: [moria] Orc, strength 6; a "when you play" trigger only (cheated in)
					put("savage", "1_151");    // Uruk Savage: [isengard] Uruk-hai, strength 5; Damage +1 only
					put("slayer", "3_93");     // Morgul Slayer: [sauron] Orc, strength 7; a regroup ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElvenRopeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Elven Rope
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype:
		 * Game Text: To play, spot an Elf. Bearer must be an Elf or Hobbit.<br><b>Maneuver:</b> Discard this possession to make a Shadow player return a minion with strength 6 or less to its owner's hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rope");

		assertEquals("Elven Rope", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAnElfCompanionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rope = scn.GetFreepsCard("rope");
		var arwen = scn.GetFreepsCard("arwen");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(rope, arwen);

		scn.StartGame();

		// Frodo, a Hobbit, could bear it; no Elf is in play.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(rope));

		scn.FreepsPlayCard(arwen);
		assertTrue(scn.FreepsPlayAvailable(rope));
		scn.FreepsPlayCard(rope);
		scn.FreepsChooseCard(frodo);

		assertAttachedTo(rope, frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnElfAllySpottedAloneLetsItBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rope = scn.GetFreepsCard("rope");
		var orophin = scn.GetFreepsCard("orophin");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(rope);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(rope));
		scn.FreepsPlayCard(rope);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, orophin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli));
		scn.FreepsChooseCard(frodo);

		assertAttachedTo(rope, frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBeBorneByAnElfOrHobbitCompanionOrAllyAndByNobodyElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rope = scn.GetFreepsCard("rope");
		var arwen = scn.GetFreepsCard("arwen");
		var orophin = scn.GetFreepsCard("orophin");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(arwen, pippin, gimli, aragorn);
		scn.MoveAlliesToTable(orophin, bounder);
		scn.MoveCardsToHand(rope);

		scn.StartGame();

		scn.FreepsPlayCard(rope);
		// Elves and Hobbits, companions and allies, of three cultures; a Dwarf and a Man are not offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, orophin, frodo, pippin, bounder));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(bounder);

		assertAttachedTo(rope, bounder);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ManeuverAbilityDiscardsTheRopeToMakeTheShadowPlayerReturnAMinionOfStrength6OrLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rope = scn.GetFreepsCard("rope");
		var arwen = scn.GetFreepsCard("arwen");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(frodo, rope);

		var warrior = scn.GetShadowCard("warrior");
		var savage = scn.GetShadowCard("savage");
		var slayer = scn.GetShadowCard("slayer");

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, savage, slayer);

		assertAttachedTo(rope, frodo);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(6, scn.GetStrength(warrior));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(slayer));
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.FreepsActionAvailable(rope));
		scn.FreepsUseCardAction(rope);

		assertInDiscard(rope);
		// The Shadow player, whose minions they are, chooses: strength 6 and 5 are offered, 7 is not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(warrior, savage));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(slayer));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(warrior);

		assertInHand(warrior);
		assertEquals(1, scn.GetShadowHandCount());
		assertInPlay(savage, slayer);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoMinionOfStrength6OrLessTheRopeIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rope = scn.GetFreepsCard("rope");
		var frodo = scn.GetRingBearer();
		scn.AttachCardsTo(frodo, rope);

		var slayer = scn.GetShadowCard("slayer");

		scn.StartGame();
		scn.MoveMinionsToTable(slayer);

		assertAttachedTo(rope, frodo);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.FreepsActionAvailable(rope));
		scn.FreepsUseCardAction(rope);

		assertInDiscard(rope);
		assertInPlay(slayer);
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rope = scn.GetFreepsCard("rope");
		var frodo = scn.GetRingBearer();
		scn.AttachCardsTo(frodo, rope);

		var warrior = scn.GetShadowCard("warrior");

		scn.StartGame();
		scn.MoveMinionsToTable(warrior);

		assertAttachedTo(rope, frodo);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(rope));
		assertAttachedTo(rope, frodo);
	}
}
