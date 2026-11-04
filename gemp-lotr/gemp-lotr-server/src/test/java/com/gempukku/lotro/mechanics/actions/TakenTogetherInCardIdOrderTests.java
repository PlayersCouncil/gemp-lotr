package com.gempukku.lotro.mechanics.actions;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Cards in play that one effect takes together are taken in the order of their card ids.
 *
 * "Wound each minion" and "wound every ally" wound one character at a time, each wound with its own window for
 * responses, so the order of the characters is something a player can see and act on.  The rules would have the
 * Free Peoples player order such a tie; the game orders it for him.
 *
 * Ketura, 2026-10-08 (Q-0142): "In any situation where there is a timing tie of required actions, the Free Peoples
 * player chooses the order, as you noted. However, I can see why these were coded that way, as the vast majority of
 * the time, the order isn't going to matter, and so the game chooses for them. Let's go with B; I think the
 * CardResolver should at some level be pulling the list of cards from GameState in a way that should be able to get
 * them in ID order."  (B: a fixed order with no prompt.)
 *
 * A card is given a new id as it comes into play, so for cards in play the order of ids is the order they came into
 * play; a card that takes another's place takes its id with it.  Before this the order was that of a hash set of
 * the cards, which differs from one run of the same game to the next: each test here laid out as it is would have
 * failed in most runs.
 */
public class TakenTogetherInCardIdOrderTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Mithrandir, Mithrandir!  Maneuver: Exert Gandalf to wound each minion who has strength of 6
					// or less.
					put("mithrandir", "4_98");
					// Here Lies Balin, Son of Fundin.  Maneuver: Exert a Dwarf to wound 2 Orcs or to wound 1 Orc
					// twice.
					put("balin", "1_19");
					// Sleep, Caradhras.  Fellowship: Exert Gandalf to discard every condition.
					put("sleep", "1_84");
					put("gandalf", "1_364");        // Gandalf, The Grey Wizard: a fellowship ability never used here
					put("gimli", "1_13");           // Gimli, Son of Gloin: a Dwarf; a skirmish ability never used here
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: a maneuver ability never used here

					// Isengard Shaman.  Response: If an [isengard] Orc is about to take a wound, remove (2) to
					// prevent that wound.  Strength 5, vitality 2.  With the twilight to pay for it, the Shadow
					// player is asked at every wound an [isengard] Orc is about to take: that is how these tests
					// see whose wound comes when.
					put("shaman", "3_59");
					put("worker1", "3_62");         // Isengard Worker: [isengard] Orc, strength 5, vitality 3; a regroup ability only
					put("worker2", "3_62");
					put("worker3", "3_62");
					put("forger1", "3_56");         // Isengard Forger: [isengard] Orc, strength 6, vitality 2; a Shadow ability only
					put("forger2", "3_56");

					// Desert Stalker.  Maneuver: Exert this minion and spot 4 Free Peoples cultures to exert every
					// companion.  Strength 11, vitality 2.
					put("stalker", "5_72");

					// Two-faced.  Restore a [gollum] card.  Then you may spot a [gollum] minion and remove a threat
					// to replace it with another minion from your discard pile with the same title.
					put("twofaced", "103_15");
					put("gollum", "5_24");          // Gollum, Nasty Treacherous Creature: strength 5, vitality 4; a regroup ability only
					put("villain", "6_40");         // Gollum, Old Villain: strength 5, vitality 4; a skirmish ability never used here

					// Conditions in a support area, none of which does anything unasked.
					put("lord", "1_21");            // Lord of Moria (Free Peoples)
					put("mallorn", "1_54");         // Mallorn-trees (Free Peoples)
					put("shoulder", "1_59");        // Shoulder to Shoulder (Free Peoples)
					put("foes", "1_105");           // Foes of Mordor (Free Peoples)
					put("fear", "1_278");           // Strength Born of Fear (Shadow)
					put("relics", "1_195");         // Relics of Moria (Shadow)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WoundEachMinionWoundsThemOneAtATimeInTheOrderTheyCameIntoPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mithrandir = scn.GetFreepsCard("mithrandir");
		var gandalf = scn.GetFreepsCard("gandalf");
		var shaman = scn.GetShadowCard("shaman");
		// The order they are laid down in, which is neither the order of their names nor of their titles.
		PhysicalCardImpl[] orcs = {
				scn.GetShadowCard("forger2"), scn.GetShadowCard("worker3"), shaman,
				scn.GetShadowCard("worker1"), scn.GetShadowCard("forger1"), scn.GetShadowCard("worker2")
		};
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(mithrandir);

		scn.StartGame();
		for (var orc : orcs)
			scn.MoveMinionsToTable(orc);
		// Each was given its id as it came into play.
		for (int i = 1; i < orcs.length; i++)
			assertTrue(orcs[i - 1].getCardId() < orcs[i].getCardId());

		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(20);
		scn.FreepsPlayCard(mithrandir);
		assertEquals(1, scn.GetWoundsOn(gandalf));

		// Six wounds and six windows.  At each, the Orcs laid down before the one now about to be wounded have
		// their wound already and the rest have none.
		for (int window = 0; window < orcs.length; window++) {
			assertTrue("window " + window, scn.ShadowHasOptionalTriggerAvailable(shaman));
			for (int i = 0; i < orcs.length; i++)
				assertEquals("window " + window + ", Orc " + i, i < window ? 1 : 0, scn.GetWoundsOn(orcs[i]));

			if (window < orcs.length - 1)
				scn.ShadowDeclineOptionalTrigger();
		}

		// The last window is the last Orc's: preventing "that wound" there leaves the last Orc unwounded.
		scn.ShadowAcceptOptionalTrigger();
		for (int i = 0; i < orcs.length - 1; i++)
			assertEquals(1, scn.GetWoundsOn(orcs[i]));
		assertEquals(0, scn.GetWoundsOn(orcs[orcs.length - 1]));
		assertEquals(20 + 3 - 2, scn.GetTwilight());
		assertInZone(Zone.DISCARD, mithrandir);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ACharacterWhoTookAnothersPlaceIsTakenWhereThatOneStood() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mithrandir = scn.GetFreepsCard("mithrandir");
		var gandalf = scn.GetFreepsCard("gandalf");
		var twofaced = scn.GetShadowCard("twofaced");
		var gollum = scn.GetShadowCard("gollum");
		var villain = scn.GetShadowCard("villain");
		var worker1 = scn.GetShadowCard("worker1");
		var shaman = scn.GetShadowCard("shaman");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(mithrandir, twofaced);
		scn.MoveCardsToDiscard(villain);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.MoveMinionsToTable(worker1);
		scn.MoveMinionsToTable(shaman);
		int gollumsId = gollum.getCardId();
		assertTrue(gollumsId < worker1.getCardId());
		assertTrue(worker1.getCardId() < shaman.getCardId());

		scn.SkipToPhase(Phase.MANEUVER);
		scn.AddThreats(1);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// The one Gollum in play gives way to the one Gollum in the discard pile, who is in play last of the
		// three and under the first one's id.
		scn.ShadowPlayCard(twofaced);
		assertTrue(scn.ShadowDecisionAvailable("remove a threat"));
		scn.ShadowChooseYes();
		assertInZone(Zone.SHADOW_CHARACTERS, villain);
		assertInZone(Zone.DISCARD, gollum);
		assertEquals(gollumsId, villain.getCardId());

		// So his wound comes first: by the first window the Shaman is given (the Worker's wound), he has it.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		scn.FreepsPlayCard(mithrandir);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shaman));
		assertEquals(1, scn.GetWoundsOn(villain));
		assertEquals(0, scn.GetWoundsOn(worker1));
		assertEquals(0, scn.GetWoundsOn(shaman));

		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shaman));
		assertEquals(1, scn.GetWoundsOn(worker1));
		assertEquals(0, scn.GetWoundsOn(shaman));

		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(shaman));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CharactersChosenTogetherAreTakenInThatOrderWhicheverWasNamedFirst() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var gimli = scn.GetFreepsCard("gimli");
		var worker1 = scn.GetShadowCard("worker1");
		var forger1 = scn.GetShadowCard("forger1");
		var shaman = scn.GetShadowCard("shaman");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(worker1);
		scn.MoveMinionsToTable(forger1);
		scn.MoveMinionsToTable(shaman);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(20);
		scn.FreepsPlayCard(balin);
		scn.FreepsChooseOption("Wound 2 Orcs");
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		// The later of the two is named first.
		scn.FreepsChooseCards(forger1, worker1);

		// Two wounds, two windows, and the earlier of the two is wounded first all the same.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shaman));
		assertEquals(0, scn.GetWoundsOn(worker1));
		assertEquals(0, scn.GetWoundsOn(forger1));
		scn.ShadowDeclineOptionalTrigger();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shaman));
		assertEquals(1, scn.GetWoundsOn(worker1));
		assertEquals(0, scn.GetWoundsOn(forger1));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(worker1));
		assertEquals(1, scn.GetWoundsOn(forger1));
		assertEquals(0, scn.GetWoundsOn(shaman));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ExertEveryCompanionIsOneExertionOfThemAllNamedInThatOrder() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var stalker = scn.GetShadowCard("stalker");
		// [shire], [dwarven], [gandalf] and [gondor]: the four cultures the Stalker spots.
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(stalker);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.MarkGameLog();
		scn.ShadowUseCardAction(stalker);
		assertEquals(1, scn.GetWoundsOn(stalker));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// One exertion of the four, with no window between them; the line that announces it names them in the
		// order it took them, the Ring-bearer (in play before anyone) first.
		String announced = null;
		for (String line : scn.GetGameLogSinceMark())
			if (line.contains("Frodo") && line.contains("exert"))
				announced = line;
		assertNotNull(scn.GetGameLogSinceMark().toString(), announced);
		assertTrue(announced, announced.indexOf("Frodo") < announced.indexOf("Gimli"));
		assertTrue(announced, announced.indexOf("Gimli") < announced.indexOf("Gandalf"));
		assertTrue(announced, announced.indexOf("Gandalf") < announced.indexOf("Aragorn"));
		assertTrue(announced, announced.indexOf("Aragorn") < announced.indexOf("Desert Stalker"));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void DiscardEveryConditionDiscardsThemInThatOrder() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sleep = scn.GetFreepsCard("sleep");
		var gandalf = scn.GetFreepsCard("gandalf");
		var lord = scn.GetFreepsCard("lord");
		var mallorn = scn.GetFreepsCard("mallorn");
		var shoulder = scn.GetFreepsCard("shoulder");
		var foes = scn.GetFreepsCard("foes");
		var fear = scn.GetShadowCard("fear");
		var relics = scn.GetShadowCard("relics");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(sleep);
		// Laid down in this order, the two players' conditions in among each other.
		scn.MoveCardsToSupportArea(shoulder);
		scn.MoveCardsToSupportArea(fear);
		scn.MoveCardsToSupportArea(lord);
		scn.MoveCardsToSupportArea(foes);
		scn.MoveCardsToSupportArea(relics);
		scn.MoveCardsToSupportArea(mallorn);

		scn.StartGame();
		scn.MarkGameLog();
		scn.FreepsPlayCard(sleep);
		assertInZone(Zone.DISCARD, shoulder, fear, lord, foes, relics, mallorn);

		String announced = null;
		for (String line : scn.GetGameLogSinceMark())
			if (line.contains("discards") && line.contains("Lord of Moria"))
				announced = line;
		assertNotNull(scn.GetGameLogSinceMark().toString(), announced);
		assertTrue(announced, announced.indexOf("Shoulder to Shoulder") < announced.indexOf("Strength Born of Fear"));
		assertTrue(announced, announced.indexOf("Strength Born of Fear") < announced.indexOf("Lord of Moria"));
		assertTrue(announced, announced.indexOf("Lord of Moria") < announced.indexOf("Foes of Mordor"));
		assertTrue(announced, announced.indexOf("Foes of Mordor") < announced.indexOf("Relics of Moria"));
		assertTrue(announced, announced.indexOf("Relics of Moria") < announced.indexOf("Mallorn-trees"));
		assertTrue(announced, announced.indexOf("Mallorn-trees") < announced.indexOf("Sleep, Caradhras"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
