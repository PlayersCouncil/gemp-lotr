package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_V3_029_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Two dots: two copies may be in play.
					put("eilenach1", "103_29");
					put("eilenach2", "103_29");
					// War-beacon: a [gondor] beacon, cost 1, to play spot an unbound Man; fellowship abilities never used here
					put("war1", "103_35");
					put("war2", "103_35");
					put("war3", "103_35");
					put("calenhad", "103_86");  // Northern Signal-fire, Flame of Calenhad: a [rohan] beacon; a maneuver ability never used here
					put("catapult", "8_32");    // Catapult: a [gondor] possession in the support area that is no beacon; a maneuver ability never used here

					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man, strength 7, vitality 3, no keyword; skirmish: exert him to make a Hobbit strength +3
					put("eomer", "4_267");      // Eomer, Third Marshal of Riddermark: [rohan] Man, strength 7, no keyword; stronger only against a wounded minion
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a ranger, strength 8; a maneuver ability never used here
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: a ranger who is an Elf; stronger only against a Nazgul
					put("knight", "8_39");      // Knight of Dol Amroth: a knight; his text needs him to win a skirmish
					put("pippin", "103_31");    // Pippin, Trollslayer: a knight who is a Hobbit; a skirmish ability never used here
					put("eowyn", "5_122");      // Eowyn, Daughter of Eomund: a valiant [rohan] Man, strength 6; a maneuver ability never used here
					put("guard", "5_83");       // Household Guard: a valiant [rohan] Man and nothing else
					put("defender", "68_9");    // Elven Defender: a valiant Elf, vitality 3; stronger only with an [elven] token in play
					// Gondorian Ranger: a Ring-bound Man (and, as every Ring-bound Man is, a ranger), strength 5; his text is
					// about the site number of minions
					put("ranger", "4_122");
					put("ranger2", "4_122");
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: an unbound companion who is no Man
					put("barliman", "1_70");    // Barliman Butterbur: a Man who is an ally, strength 1, vitality 2
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man who is an ally; a fellowship ability never used here
					// Horse of Rohan: a mount for a Man or an Elf; it exerts minions only in its bearer's own skirmish
					put("horse1", "4_283");
					put("horse2", "4_283");

					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; its text is about being played
					put("grima", "4_154");      // Grima, Wormtongue: a Man who is a minion, vitality 3; his maneuver ability needs a companion bearing 3 cards
					// Dunlending Brigand: a Man who is a minion with vitality 1, so always exhausted; no text
					put("brigand", "4_10");
					put("brigand2", "4_10");
					put("nomad", "7_132");      // Desert Nomad: a Southron, a Man who is a minion, vitality 2; damage +1 only with 2 threats
					put("mumak", "5_73");       // Mumak: a Shadow mount a Southron bears; makes its bearer fierce, and adds twilight only when its bearer wins a skirmish
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NorthernSignalfireStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Northern Signal-fire, Flame of Eilenach
		 * Unique: 2
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: Beacon. To play, hinder 2 beacons.
		 * 	Skirmish: Hinder this beacon to make your unbound Man strength +1 for each of the following you can spot: ranger, knight, mounted Man, valiant Man, ring-bound Man, exhausted Man.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("eilenach1");

		assertEquals("Northern Signal-fire", card.getBlueprint().getTitle());
		assertEquals("Flame of Eilenach", card.getBlueprint().getSubtitle());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.BEACON));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilTwoBeaconsCanBeHinderedAndThenCostsTwoAndHindersBoth() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eilenach = scn.GetFreepsCard("eilenach1");
		var war1 = scn.GetFreepsCard("war1");
		var war2 = scn.GetFreepsCard("war2");
		var calenhad = scn.GetFreepsCard("calenhad");
		var catapult = scn.GetFreepsCard("catapult");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(war1, calenhad, catapult);
		// A hindered beacon is no beacon while it is hindered, and the Catapult never was one.
		scn.HinderCard(calenhad);
		scn.MoveCardsToHand(eilenach, war2);

		scn.StartGame();

		// One beacon that can be hindered: one short.
		assertFalse(scn.FreepsPlayAvailable(eilenach));

		scn.FreepsPlayCard(war2);

		// 1 for the War-beacon
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(eilenach));
		assertFalse(scn.IsHindered(war1));
		assertFalse(scn.IsHindered(war2));

		scn.FreepsPlayCard(eilenach);

		// With exactly two beacons to hinder nothing is asked.
		assertInZone(Zone.SUPPORT, eilenach);
		assertTrue(scn.IsHindered(war1));
		assertTrue(scn.IsHindered(war2));
		assertTrue(scn.IsHindered(calenhad));
		assertFalse(scn.IsHindered(catapult));
		assertFalse(scn.IsHindered(eilenach));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheTwoBeaconsHinderedToPlayAreChosenFromUnhinderedBeaconsOfAnyCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eilenach = scn.GetFreepsCard("eilenach1");
		var war1 = scn.GetFreepsCard("war1");
		var war2 = scn.GetFreepsCard("war2");
		var war3 = scn.GetFreepsCard("war3");
		var calenhad = scn.GetFreepsCard("calenhad");
		var catapult = scn.GetFreepsCard("catapult");
		scn.MoveCardsToSupportArea(war1, war2, war3, calenhad, catapult);
		scn.HinderCard(war3);
		scn.MoveCardsToHand(eilenach);

		scn.StartGame();

		scn.FreepsPlayCard(eilenach);

		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		// Two [gondor] beacons and a [rohan] one
		assertTrue(scn.FreepsHasCardChoiceAvailable(war1, war2, calenhad));
		// A beacon already hindered, a [gondor] support possession that is no beacon, and the card being played
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(war3, catapult, eilenach));

		scn.FreepsChooseCards(war2, calenhad);

		assertInZone(Zone.SUPPORT, eilenach);
		assertTrue(scn.IsHindered(war2));
		assertTrue(scn.IsHindered(calenhad));
		assertFalse(scn.IsHindered(war1));
		assertFalse(scn.IsHindered(catapult));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityHindersItsOwnCopyAndMakesAnUnboundManOfYoursStrongerByTheKindsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eilenach1 = scn.GetFreepsCard("eilenach1");
		var eilenach2 = scn.GetFreepsCard("eilenach2");
		var war1 = scn.GetFreepsCard("war1");
		var boromir = scn.GetFreepsCard("boromir");
		var eomer = scn.GetFreepsCard("eomer");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ranger = scn.GetFreepsCard("ranger");
		var haldir = scn.GetFreepsCard("haldir");
		var barliman = scn.GetFreepsCard("barliman");
		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");
		var grima = scn.GetShadowCard("grima");
		scn.MoveCompanionsToTable(boromir, eomer, aragorn, ranger, haldir);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToSupportArea(eilenach1, eilenach2, war1);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, grima);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The phase before the skirmishes
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(eilenach1));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(boromir, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(boromir);

		assertTrue(scn.FreepsActionAvailable(eilenach1));
		assertTrue(scn.FreepsActionAvailable(eilenach2));
		assertFalse(scn.IsHindered(eilenach1));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(7, scn.GetStrength(eomer));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsUseCardAction(eilenach1);

		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		// A [gondor] Man in the skirmish, and a [rohan] Man and a [gondor] Man out of it
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, eomer, aragorn));
		// A Ring-bound Man, an unbound Elf, a Man who is an ally, a Man who is a minion, the Ring-bearer
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ranger, haldir, barliman, grima, frodo));
		scn.FreepsChooseCard(eomer);

		// A ranger (two of them: Aragorn and the Gondorian Ranger) and a Ring-bound Man: +2
		assertEquals(9, scn.GetStrength(eomer));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.IsHindered(eilenach1));
		assertFalse(scn.IsHindered(eilenach2));
		assertFalse(scn.IsHindered(war1));

		scn.ShadowPass();

		// The hindered copy has nothing more to offer; the other copy is its own.
		assertFalse(scn.FreepsActionAvailable(eilenach1));
		assertTrue(scn.FreepsActionAvailable(eilenach2));

		scn.FreepsUseCardAction(eilenach2);
		scn.FreepsChooseCard(boromir);

		assertEquals(9, scn.GetStrength(boromir));
		assertEquals(9, scn.GetStrength(eomer));
		assertTrue(scn.IsHindered(eilenach2));
		assertFalse(scn.IsHindered(war1));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoneOfTheSixKindsItGivesNothingAndAManExertedToExhaustionThenCountsForOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eilenach1 = scn.GetFreepsCard("eilenach1");
		var eilenach2 = scn.GetFreepsCard("eilenach2");
		var boromir = scn.GetFreepsCard("boromir");
		var eomer = scn.GetFreepsCard("eomer");
		var defender = scn.GetFreepsCard("defender");
		var horse = scn.GetFreepsCard("horse1");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(boromir, eomer, defender);
		scn.AttachCardsTo(defender, horse);
		scn.MoveCardsToSupportArea(eilenach1, eilenach2);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		// What nearly counts: an Elf who is valiant, mounted and exhausted (vitality 3), and Frodo, who is
		// Ring-bound. No ranger, no knight. Boromir has one exertion left before he is exhausted.
		scn.AddWoundsToChar(defender, 2);
		scn.AddWoundsToChar(boromir, 1);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(eomer, runner);

		assertTrue(scn.IsExhausted(defender));
		assertFalse(scn.IsExhausted(boromir));
		assertEquals(7, scn.GetStrength(eomer));
		// It has a cost, so it is on offer though it will give nothing.
		assertTrue(scn.FreepsActionAvailable(eilenach1));

		scn.FreepsUseCardAction(eilenach1);
		scn.FreepsChooseCard(eomer);

		assertTrue(scn.IsHindered(eilenach1));
		assertEquals(7, scn.GetStrength(eomer));

		scn.ShadowPass();
		// Boromir exerts to make a Hobbit (Frodo, the only one) strength +3, and is exhausted for it.
		scn.FreepsUseCardAction(boromir);
		assertTrue(scn.IsExhausted(boromir));
		scn.ShadowPass();

		scn.FreepsUseCardAction(eilenach2);
		scn.FreepsChooseCard(eomer);

		// An exhausted Man: +1
		assertEquals(8, scn.GetStrength(eomer));
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.IsHindered(eilenach2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AllSixKindsCountOnceEachHoweverManyCardsAreOfAKind() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eilenach = scn.GetFreepsCard("eilenach1");
		var boromir = scn.GetFreepsCard("boromir");
		var ranger = scn.GetFreepsCard("ranger");
		var ranger2 = scn.GetFreepsCard("ranger2");
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var eomer = scn.GetFreepsCard("eomer");
		var eowyn = scn.GetFreepsCard("eowyn");
		var guard = scn.GetFreepsCard("guard");
		var horse1 = scn.GetFreepsCard("horse1");
		var horse2 = scn.GetFreepsCard("horse2");
		var troop = scn.GetShadowCard("troop");
		var brigand = scn.GetShadowCard("brigand");
		var brigand2 = scn.GetShadowCard("brigand2");
		// Two cards of every kind.
		scn.MoveCompanionsToTable(boromir, ranger, ranger2, knight, pippin, eomer, eowyn, guard);
		scn.AttachCardsTo(eomer, horse1);
		scn.AttachCardsTo(eowyn, horse2);
		scn.MoveCardsToSupportArea(eilenach);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, brigand, brigand2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(boromir);

		assertTrue(scn.IsExhausted(brigand));
		assertTrue(scn.IsExhausted(brigand2));
		assertEquals(7, scn.GetStrength(boromir));

		scn.FreepsUseCardAction(eilenach);
		scn.FreepsChooseCard(boromir);

		// ranger (both Gondorian Rangers) 1 + knight (the Knight, Pippin) 1 + mounted Man (Eomer, Eowyn) 1
		// + valiant Man (Eowyn, the Household Guard) 1 + Ring-bound Man (both Gondorian Rangers) 1
		// + exhausted Man (both Brigands, minions) 1 = 6
		assertEquals(13, scn.GetStrength(boromir));
		assertTrue(scn.IsHindered(eilenach));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ARangerOrKnightWhoIsNoManCountsAndOneManCountsForEachKindSheIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eilenach = scn.GetFreepsCard("eilenach1");
		var boromir = scn.GetFreepsCard("boromir");
		var arwen = scn.GetFreepsCard("arwen");
		var pippin = scn.GetFreepsCard("pippin");
		var eowyn = scn.GetFreepsCard("eowyn");
		var horse = scn.GetFreepsCard("horse1");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(boromir, arwen, pippin, eowyn);
		scn.AttachCardsTo(eowyn, horse);
		scn.MoveCardsToSupportArea(eilenach);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(boromir, runner);

		assertEquals(6, scn.GetStrength(eowyn));
		assertEquals(7, scn.GetStrength(boromir));

		scn.FreepsUseCardAction(eilenach);
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, eowyn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arwen, pippin));
		scn.FreepsChooseCard(eowyn);

		// ranger (Arwen, an Elf) 1 + knight (Pippin, a Hobbit) 1 + mounted Man 1 + valiant Man 1 (Eowyn, both) = 4
		assertEquals(10, scn.GetStrength(eowyn));
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AManWhoIsAnAllyCountsAsAMountedManAndAsAnExhaustedMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eilenach = scn.GetFreepsCard("eilenach1");
		var boromir = scn.GetFreepsCard("boromir");
		var guma = scn.GetFreepsCard("guma");
		var horse = scn.GetFreepsCard("horse1");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(boromir);
		// "You can spot" names no card type. Of the six kinds an ally can be two: Guma, a Man, bears a horse, and
		// with 1 wound of his vitality 2 he is exhausted. Nobody else on the table is of any kind.
		scn.MoveAlliesToTable(guma);
		scn.AttachCardsTo(guma, horse);
		scn.MoveCardsToSupportArea(eilenach);

		scn.StartGame();
		scn.AddWoundsToChar(guma, 1);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(boromir, runner);

		assertTrue(scn.IsExhausted(guma));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(4, scn.GetStrength(guma));

		// Boromir is the only unbound Man of mine, so nothing is asked.
		scn.FreepsUseCardAction(eilenach);

		// mounted Man 1 + exhausted Man 1, Guma for both
		assertEquals(9, scn.GetStrength(boromir));
		assertEquals(4, scn.GetStrength(guma));
		assertTrue(scn.IsHindered(eilenach));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AManWhoIsAMinionOnAMountCountsAsAMountedMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eilenach = scn.GetFreepsCard("eilenach1");
		var boromir = scn.GetFreepsCard("boromir");
		var runner = scn.GetShadowCard("runner");
		var nomad = scn.GetShadowCard("nomad");
		var mumak = scn.GetShadowCard("mumak");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(eilenach);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, nomad);
		scn.AttachCardsTo(nomad, mumak);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(boromir);

		// Of the six kinds only a mounted Man is in play, and he is the Shadow player's: a Southron on a
		// Mumak, with vitality 2 and no wound, so not exhausted. "You can spot" names no side.
		assertFalse(scn.IsExhausted(nomad));
		assertEquals(7, scn.GetStrength(boromir));

		// Boromir is the only unbound Man of mine, so nothing is asked.
		scn.FreepsUseCardAction(eilenach);

		assertEquals(8, scn.GetStrength(boromir));
		assertTrue(scn.IsHindered(eilenach));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheBonusLastsThroughTheSkirmishItWasGivenInAndIsGoneInTheNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eilenach = scn.GetFreepsCard("eilenach1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var eomer = scn.GetFreepsCard("eomer");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn, eomer);
		scn.MoveCardsToSupportArea(eilenach);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{eomer, troop});
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(7, scn.GetStrength(eomer));

		// In Aragorn's skirmish, given to Eomer, whose own skirmish is still to come.
		scn.FreepsUseCardAction(eilenach);
		scn.FreepsChooseCard(eomer);

		// A ranger (Aragorn): +1
		assertEquals(8, scn.GetStrength(eomer));

		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(eomer));

		// Aragorn's 8 beats the Runner's 5 and that skirmish is over.
		scn.FreepsPass();
		scn.FreepsResolveSkirmish(eomer);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(eomer));
	}

	@Test
	public void WithNoUnboundManOfYoursItIsStillOfferedAndHindersItselfForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eilenach = scn.GetFreepsCard("eilenach1");
		var ranger = scn.GetFreepsCard("ranger");
		var barliman = scn.GetFreepsCard("barliman");
		var runner = scn.GetShadowCard("runner");
		// The only Men are Ring-bound or an ally. A ranger and a Ring-bound Man are there to be counted.
		scn.MoveCompanionsToTable(ranger);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToSupportArea(eilenach);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(ranger, runner);

		assertEquals(5, scn.GetStrength(ranger));
		assertEquals(1, scn.GetStrength(barliman));
		assertTrue(scn.FreepsActionAvailable(eilenach));

		scn.FreepsUseCardAction(eilenach);

		assertTrue(scn.IsHindered(eilenach));
		assertEquals(5, scn.GetStrength(ranger));
		assertEquals(1, scn.GetStrength(barliman));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
