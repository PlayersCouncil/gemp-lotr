package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_063_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("event", "3_63");

					put("raiding", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, vitality 3, damage +1 and nothing else
					// Saruman, Keeper of Isengard: [isengard] Wizard minion, vitality 4; Uruk-hai are fierce, a Response used only on purpose
					put("saruman", "3_68");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion, vitality 3, no text
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man minion, no text

					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, vitality 3; a skirmish ability
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a maneuver ability only
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion, vitality 3; strength +3 against a Nazgul
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, vitality 3, valiant and nothing else
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion, vitality 4; a strength modifier
					// Pippin, Trollslayer: a [gondor] Hobbit companion, vitality 4; to play, spot a [gondor] companion (cheated in); a skirmish ability
					put("pippin", "103_31");
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] Ent companion, vitality 4; strength +2 with 3 Ents
					put("rumil", "1_57");       // Rúmil, Elven Protector: an Elf ally; an archery ability
					put("albert", "1_69");      // Albert Dreary, Entertainer from Bree: a Man ally; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OneofYouMustDoThisStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: One of You Must Do This
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Exert an [isengard] minion to make the Free Peoples player exert X companions, where X is the number of these races you can spot in the fellowship: Dwarf, Elf, Man, and Wizard.
		*/

		var scn = GetScenario();

		var event = scn.GetFreepsCard("event");

		assertEquals("One of You Must Do This", event.getBlueprint().getTitle());
		assertNull(event.getBlueprint().getSubtitle());
		assertFalse(event.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, event.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, event.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, event.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(event, Timeword.MANEUVER));
		assertEquals(1, event.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithAllFourRacesInTheFellowshipTheFreePeoplesPlayerExertsFourCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var guard = scn.GetFreepsCard("guard");
		var gandalf = scn.GetFreepsCard("gandalf");
		// A Dwarf, an Elf, two Men and a Wizard beside the Hobbit: four races of the list, the second Man
		// counting for nothing more.
		scn.MoveCompanionsToTable(gimli, arwen, aragorn, guard, gandalf);
		// Arwen, vitality 3, is exhausted and the only Elf: she is still an Elf to spot, and cannot be exerted.
		scn.AddWoundsToChar(arwen, 2);
		assertEquals(2, scn.GetWoundsOn(arwen));

		var event = scn.GetShadowCard("event");
		var raiding = scn.GetShadowCard("raiding");
		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(event);

		scn.StartGame();
		scn.MoveMinionsToTable(raiding, saruman, troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(raiding));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(event));
		scn.ShadowPlayCard(event);

		// The cost: an [isengard] minion of either race, and not a minion of another culture.
		assertTrue(scn.ShadowHasCardChoiceAvailable(raiding, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(raiding);
		assertEquals(1, scn.GetWoundsOn(raiding));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(troop));

		// X is 4: the Free Peoples player chooses exactly four companions to exert, from those who can exert.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gimli, aragorn, guard, gandalf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arwen, raiding, saruman, troop));
		assertEquals(4, scn.FreepsGetChoiceMin());
		assertEquals(4, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(frodo, gimli, aragorn, gandalf);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertInDiscard(event);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AHobbitAnEntAndCharactersOutsideTheFellowshipAreNotCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var pippin = scn.GetFreepsCard("pippin");
		var avenger = scn.GetFreepsCard("avenger");
		var rumil = scn.GetFreepsCard("rumil");
		var albert = scn.GetFreepsCard("albert");
		// The Dwarf is the one race of the list in the fellowship.  The [gondor] Hobbit is no Man, the [gandalf]
		// Ent no Wizard; the Elf and the Man allies are not in the fellowship, nor are the Man and Wizard minions.
		scn.MoveCompanionsToTable(gimli, pippin, avenger);
		scn.MoveAlliesToTable(rumil, albert);

		var event = scn.GetShadowCard("event");
		var saruman = scn.GetShadowCard("saruman");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToHand(event);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, brigand);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertEquals(0, scn.GetWoundsOn(rumil));
		assertEquals(0, scn.GetWoundsOn(albert));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		// Saruman is the one [isengard] minion, and is exerted with nothing asked.
		scn.ShadowPlayCard(event);
		assertEquals(1, scn.GetWoundsOn(saruman));

		// X is 1: exactly one companion, any of the four; no ally and no minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gimli, pippin, avenger));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(rumil, albert, saruman, brigand));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(pippin);

		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertEquals(0, scn.GetWoundsOn(rumil));
		assertEquals(0, scn.GetWoundsOn(albert));
		assertInDiscard(event);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyIsengardMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var event = scn.GetShadowCard("event");
		var raiding = scn.GetShadowCard("raiding");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(event);
		assertInHand(event);

		scn.StartGame();
		scn.MoveMinionsToTable(raiding, troop);
		// Vitality 3 with 2 wounds: the Uruk-hai is exhausted; the [moria] Orc beside him could exert.
		scn.AddWoundsToChar(raiding, 2);
		assertEquals(2, scn.GetWoundsOn(raiding));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(2, scn.GetWoundsOn(raiding));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInHand(event);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(event));
	}
}
