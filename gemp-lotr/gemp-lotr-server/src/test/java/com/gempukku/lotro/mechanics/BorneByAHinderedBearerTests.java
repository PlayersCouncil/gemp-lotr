package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * A card borne by a hindered card is not borne by "a companion", "a Hobbit" or any other kind of card.
 *
 * Comprehensive Rules 5.0, "hinder": "Other non-game-text card traits such as title, culture, card type, all
 * flavors of subtype (Race, Item Class, etc) also do not apply on hindered cards.  This means that hindered cards
 * cannot be spotted or targeted by their title, culture, card type, or race/item class by any game effect."  And:
 * "Cards which are borne by hindered cards ... are otherwise still in play and active; they can be interacted
 * with and use their game text normally."  So the borne card is still there to be found, and what cannot be found
 * is the bearer its text describes.  Ketura on Q-0108 (Gorgoroth Wastes 103_124), where armor on a hindered Gimli
 * was asked about as a card "borne by companions": "very good catch", it is not.
 *
 * A bearer that a text holds as a card is another matter: the card that bears this one ("bearer"), the companion
 * an ability has already chosen, the Ring-bearer, or any bearer at all ("each attached follower").  Hindering him
 * changes none of those, and what he bears is still found by them.
 *
 * A text that only rules a kind of bearer out is asked of a hindered bearer too, as he is printed: Asfaloth 1_31
 * is discarded at an underground site unless an ally bears it, who is at his home site and not the fellowship's,
 * and hindering its bearer moves neither of them (Ketura on Q-0252).
 *
 * The game used to ask a borne card's bearer what kind of card it was without asking whether it was hindered, so
 * each definition had to say "and not hindered" for itself, and those of the Decipher sets did not.
 *
 * Hindering the bearer is only how the board is laid, and is done by the rig, in every test but the last, where
 * it is the first half of the ability under way.
 */
public class BorneByAHinderedBearerTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "1_311");        // Sam, Son of Hamfast: a Hobbit; a fellowship ability never used here
					put("merry", "1_303");      // Merry, From O'er the Brandywine: a Hobbit; his text is about a weapon
					// Weary: [isengard] condition, "Plays on a companion or ally.  Limit 1 per character."  Its
					// trigger needs an [isengard] tracker.
					put("weary1", "4_212");
					put("weary2", "4_212");
					put("weary3", "4_212");
					// Seven We Had: "Fellowship: Add a burden to discard a condition borne by a Hobbit."
					put("seven1", "4_318");
					put("seven2", "4_318");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability never used here
					// Rohan Worker: a follower with no number; its texts answer a transfer and a skirmish won
					put("worker", "15_135");
					// Orkish Assassin: strength 8.  "While you can spot a follower attached to a companion, each
					// [orc] minion is strength +2."
					put("assassin", "17_72");

					put("arwen", "1_30");       // Arwen, Daughter of Elrond: her text is about Nazgul
					put("sword", "1_112");      // Ranger's Sword: possession, "Bearer must be Aragorn."
					put("gwemegil", "1_47");    // Gwemegil: possession, "Bearer must be Arwen."
					// Grima, Wormtongue: a minion to spot; his maneuver ability is never used here
					put("grima", "4_154");
					// Where Has Grima Stowed It?: "Maneuver: Spot Grima to discard a possession borne by an ally
					// or unbound companion."  Cost 2.
					put("stowed1", "4_214");
					put("stowed2", "4_214");

					// Barliman Butterbur, Red-faced Landlord: strength 4.  "Barliman Butterbur is strength +1
					// for each attached [gandalf] follower."  His maneuver ability is never used here.
					put("barliman", "17_16");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: his text is about his own strength
					// Pallando, Far-travelling One: [gandalf] follower; its texts answer a transfer and a
					// skirmish won
					put("pallando", "13_37");

					// Household Guard: a [rohan] Man with no text but valiant
					put("guardsman", "5_83");
					put("spear", "4_288");      // Rider's Spear: [rohan] possession, "Bearer must be a [rohan] Man."
					// Spider Poison: a Shadow possession a companion bears; it exerts him when he is assigned
					put("poison", "8_28");
					// Precise Attack: archery event.  "Discard a [rohan] possession from play to choose one:
					// wound an unwounded minion; exert a minion; or discard a Shadow card the Ring-bearer is
					// bearing."
					put("attack", "18_101");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text

					// Savage Harpoon: Shadow possession on a companion.  "Each time the fellowship moves, the
					// Free Peoples player must hinder bearer or a Free Peoples item on bearer."
					put("harpoon", "103_57");
					put("athelas", "1_94");     // Athelas: possession, "Bearer must be a [gondor] Man."

					// Grima, Chief Counselor (the erratum): "Maneuver: Exert Grima to spot an unbound companion.
					// Hinder a condition borne by that companion (or hinder that companion and all Free Peoples
					// cards they bear if you can spot 4 Free Peoples cultures)."
					put("counselor", "55_51");
					put("guard", "1_7");        // Dwarf Guard: an unbound [dwarven] companion with no text
					put("armor", "1_8");        // Dwarven Armor: possession, "Bearer must be a Dwarf."
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: an ally, for a fourth culture

					// Asfaloth: possession, "Bearer must be an Elf. ... Discard Asfaloth when at an underground
					// site."  Its discard asks that it be borne by a card that is not an ally.
					put("asfaloth", "1_31");
					put("orophin", "1_56");     // Orophin, Lorien Bowman: an Elf ally, home site 6; an archery ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AConditionOnAHinderedHobbitIsNotBorneByAHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var seven1 = scn.GetFreepsCard("seven1");
		var seven2 = scn.GetFreepsCard("seven2");
		var onSam = scn.GetShadowCard("weary1");
		var onMerry = scn.GetShadowCard("weary2");
		var onFrodo = scn.GetShadowCard("weary3");
		scn.MoveCompanionsToTable(sam, merry);
		scn.AttachCardsTo(sam, onSam);
		scn.AttachCardsTo(merry, onMerry);
		scn.AttachCardsTo(frodo, onFrodo);
		scn.MoveCardsToHand(seven1, seven2);

		scn.StartGame();
		scn.HinderCard(sam);

		assertEquals(0, scn.GetBurdens());
		scn.FreepsPlayCard(seven1);
		assertEquals(1, scn.GetBurdens());

		// Three Hobbits each bear a condition, and the one on the hindered Hobbit is not among those offered.
		// It is no hindered card itself, and it is still his to bear.
		assertTrue(scn.FreepsHasCardChoiceAvailable(onMerry, onFrodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(onSam));
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertFalse(scn.IsHindered(onSam));
		assertAttachedTo(onSam, sam);

		scn.FreepsChooseCard(onMerry);
		assertInZone(Zone.DISCARD, onMerry);
		assertAttachedTo(onSam, sam);
		assertAttachedTo(onFrodo, frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The control: restored, Sam is a Hobbit again and what he bears is borne by one
		scn.RestoreCard(sam);
		scn.FreepsPlayCard(seven2);
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.FreepsHasCardChoiceAvailable(onSam, onFrodo));
		assertEquals(2, scn.FreepsGetCardChoiceCount());

		scn.FreepsChooseCard(onSam);
		assertInZone(Zone.DISCARD, onSam);
		assertAttachedTo(onFrodo, frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AFollowerOnAHinderedCompanionIsNotAttachedToACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var worker = scn.GetFreepsCard("worker");
		var assassin = scn.GetShadowCard("assassin");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, worker);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin);

		// 8 + 2
		assertEquals(10, scn.GetStrength(assassin));

		scn.HinderCard(aragorn);
		// The follower is neither hindered nor gone: it is a follower attached to a card that is no companion
		assertFalse(scn.IsHindered(worker));
		assertAttachedTo(worker, aragorn);
		assertEquals(8, scn.GetStrength(assassin));

		scn.RestoreCard(aragorn);
		assertEquals(10, scn.GetStrength(assassin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// "An ally or unbound companion" is two kinds, and a hindered bearer is neither of them.
	@Test
	public void APossessionOnAHinderedCompanionIsNotBorneByAnAllyOrUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var sword = scn.GetFreepsCard("sword");
		var gwemegil = scn.GetFreepsCard("gwemegil");
		var grima = scn.GetShadowCard("grima");
		var stowed1 = scn.GetShadowCard("stowed1");
		var stowed2 = scn.GetShadowCard("stowed2");
		scn.MoveCompanionsToTable(aragorn, arwen);
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(arwen, gwemegil);
		scn.MoveCardsToHand(stowed1, stowed2);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(grima);
		scn.HinderCard(aragorn);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(sword));
		assertAttachedTo(sword, aragorn);

		// Gwemegil is the only possession found, so nothing is asked
		scn.ShadowPlayCard(stowed1);
		assertInZone(Zone.DISCARD, gwemegil);
		assertAttachedTo(sword, aragorn);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The control: restored, Aragorn is an unbound companion again and his Sword is the one that goes
		scn.RestoreCard(aragorn);
		scn.FreepsPass();
		scn.ShadowPlayCard(stowed2);
		assertInZone(Zone.DISCARD, sword);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	// "Each attached [gandalf] follower" asks nothing of the card it is attached to.
	@Test
	public void AFollowerOnAHinderedCompanionIsStillAttached() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barliman = scn.GetFreepsCard("barliman");
		var gandalf = scn.GetFreepsCard("gandalf");
		var pallando = scn.GetFreepsCard("pallando");
		scn.MoveCompanionsToTable(barliman, gandalf);

		scn.StartGame();

		assertEquals(4, scn.GetStrength(barliman));
		scn.AttachCardsTo(gandalf, pallando);
		// 4 + 1
		assertEquals(5, scn.GetStrength(barliman));

		scn.HinderCard(gandalf);
		assertFalse(scn.IsHindered(pallando));
		assertAttachedTo(pallando, gandalf);
		assertEquals(5, scn.GetStrength(barliman));

		// What is counted is the follower: hindered itself, it is no [gandalf] follower to find
		scn.HinderCard(pallando);
		assertEquals(4, scn.GetStrength(barliman));

		scn.RestoreCard(pallando, gandalf);
		assertEquals(5, scn.GetStrength(barliman));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// He is the Ring-bearer by the rules and not by anything printed on him (burdens are "on the Ring-bearer"
	// whatever his state: Ketura on Q-0186).  This is how the game stood before a bearer's kind was asked about,
	// and it is left so: Q-0252 asks whether it should be.
	@Test
	public void AShadowCardOnAHinderedRingBearerIsStillACardTheRingBearerIsBearing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var guardsman = scn.GetFreepsCard("guardsman");
		var spear = scn.GetFreepsCard("spear");
		var attack = scn.GetFreepsCard("attack");
		var poison = scn.GetShadowCard("poison");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(guardsman);
		scn.AttachCardsTo(guardsman, spear);
		scn.MoveCardsToHand(attack);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(frodo, poison);
		scn.HinderCard(frodo);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(poison));

		// The Spear is the one [rohan] possession in play
		scn.FreepsPlayCard(attack);
		assertInZone(Zone.DISCARD, spear);
		assertTrue(scn.FreepsChoiceAvailable("Discard a Shadow card"));
		scn.FreepsChoose("Discard a Shadow card");

		// The Poison is the one Shadow card he bears
		assertInZone(Zone.DISCARD, poison);
		assertTrue(scn.IsHindered(frodo));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	// "A Free Peoples item on bearer": the card that bears the Harpoon is that card, hindered or not.
	@Test
	public void AnItemOnAHinderedBearerIsStillOnTheBearerOfTheCardThatSaysSo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var athelas = scn.GetFreepsCard("athelas");
		var harpoon = scn.GetShadowCard("harpoon");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, harpoon, athelas);

		scn.StartGame();
		scn.HinderCard(aragorn);
		assertFalse(scn.IsHindered(athelas));

		// The fellowship moves.  The bearer cannot be hindered again, so it must be an item on him, and the
		// Athelas is the only one: nothing is asked.
		scn.FreepsPass();

		assertTrue(scn.IsHindered(athelas));
		assertTrue(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(harpoon));
		assertAttachedTo(athelas, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// "Hinder that companion and all Free Peoples cards they bear": by the time his cards are looked for he is
	// hindered, and he is still the companion the ability chose.
	@Test
	public void TheCardsOfACompanionAnAbilityHasChosenAreFoundOnceHeIsHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var sam = scn.GetFreepsCard("sam");
		var elrond = scn.GetFreepsCard("elrond");
		var armor = scn.GetFreepsCard("armor");
		var weary = scn.GetShadowCard("weary1");
		var counselor = scn.GetShadowCard("counselor");
		scn.MoveCompanionsToTable(aragorn, guard, sam);
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(guard, armor, weary);

		scn.StartGame();
		scn.MoveMinionsToTable(counselor);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// [gondor], [dwarven], [shire] and Elrond's [elven]
		assertEquals(4, scn.GetFreepsCultureCount());
		scn.ShadowUseCardAction(counselor);
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, guard));
		scn.ShadowChooseCard(guard);

		assertTrue(scn.IsHindered(guard));
		assertTrue(scn.IsHindered(armor));
		// A Shadow card he bears is none of "all Free Peoples cards they bear"
		assertFalse(scn.IsHindered(weary));
		assertFalse(scn.IsHindered(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	// "Not an ally" finds the bearer by no kind of card: it only rules one out, and a hindered bearer is asked it
	// as he is printed.
	@Test
	public void ACardOnAHinderedBearerIsStillBorneByAnAllyOrByOneWhoIsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var orophin = scn.GetFreepsCard("orophin");
		var asfaloth = scn.GetFreepsCard("asfaloth");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(orophin);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AttachCardsTo(orophin, asfaloth);
		scn.HinderCard(arwen, orophin);

		// Balin's Tomb is underground.  A hindered ally is an ally still for a text that leaves allies out: he
		// is at his home site, and Asfaloth with him.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.IsHindered(orophin));
		assertAttachedTo(asfaloth, orophin);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// On the hindered companion it is with the fellowship, underground, and goes as the game next looks.
		scn.AttachCardsTo(arwen, asfaloth);
		assertTrue(scn.IsHindered(arwen));
		assertAttachedTo(asfaloth, arwen);
		scn.ShadowPass();
		assertTrue(scn.IsHindered(arwen));
		assertInZone(Zone.DISCARD, asfaloth);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
