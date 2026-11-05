package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_172_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rohan", "4_172");

					// Rohirrim Traitor: an [isengard] Man, cost 2, site 3; stronger while a companion bears an
					// [isengard] condition.
					put("traitor", "6_72");
					// Gríma, Wormtongue: an [isengard] Man of another title, vitality 3; a maneuver ability.
					put("grima", "4_154");
					// Saruman, Servant of the Eye: the [isengard] Saruman, a Wizard and no Man.
					put("saruman", "3_69");
					// Saruman, Rabble-rouser: the [dunland] Saruman; his move trigger is laid after the move.
					put("rabble", "4_33");
					// Saruman, Servant of Sauron: the Saruman who is a follower, no character; his Aid is offered only
					// as a maneuver phase starts.
					put("follower", "17_38");
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					put("savage2", "1_151");
					put("savage3", "1_151");
					// Dunlending Footmen: a [dunland] Man; cheaper during a skirmish phase.
					put("footmen", "6_3");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man companion; a skirmish ability used only on purpose
					put("merry", "1_303");      // Merry, From O'er the Brandywine: a [shire] Hobbit companion; stronger with a weapon
					put("gimli", "1_13");       // Gimli, Son of Gloin: a [dwarven] Dwarf companion; a skirmish ability used only on purpose
					// Guma, Plains Farmer: a [rohan] Man ally, vitality 2, home 4T; discarded only if an opponent
					// controls a Towers site 4, which is not on this path.
					put("guma", "4_277");
					// Elrond, Lord of Rivendell: an [elven] Elf ally, vitality 4; heals at the start of his owner's
					// turns, which no test reaches.
					put("elrond", "1_40");
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: a [dwarven] Dwarf ally, vitality 2; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RohanIsMineStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Rohan Is Mine
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype:
		 * Game Text: To play, spot Saruman or an [isengard] Man. Plays on a Free Peoples Man.<br>Each time the Free Peoples player assigns bearer to a skirmish, exert each ally.
		*/

		var scn = GetScenario();

		var rohan = scn.GetFreepsCard("rohan");

		assertEquals("Rohan Is Mine", rohan.getBlueprint().getTitle());
		assertNull(rohan.getBlueprint().getSubtitle());
		assertFalse(rohan.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, rohan.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, rohan.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, rohan.getBlueprint().getCardType());
		assertEquals(0, rohan.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAnIsengardManIsSpottedAndThenPlaysOnAFreePeoplesMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.MoveAlliesToTable(guma);

		var rohan = scn.GetShadowCard("rohan");
		var traitor = scn.GetShadowCard("traitor");
		var savage = scn.GetShadowCard("savage");
		var footmen = scn.GetShadowCard("footmen");

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.MoveMinionsToTable(savage, footmen);
		scn.MoveCardsToHand(rohan, traitor);
		scn.ShadowRefreshDecision();
		// An [isengard] minion who is no Man, and a Man of another culture.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(rohan));

		scn.ShadowPlayCard(traitor);
		assertInZone(Zone.SHADOW_CHARACTERS, traitor);
		assertTrue(scn.ShadowPlayAvailable(rohan));

		scn.ShadowPlayCard(rohan);
		// A Man companion and a Man ally; not the Hobbits, nor the Shadow player's own Men.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, guma));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, merry, traitor, footmen));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);

		assertAttachedTo(rohan, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnExhaustedIsengardManOfAnotherTitleIsSpottedAsWell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var rohan = scn.GetShadowCard("rohan");
		var grima = scn.GetShadowCard("grima");

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.MoveMinionsToTable(grima);
		// Vitality 3: exhausted.  To spot him is no exertion.
		scn.AddWoundsToChar(grima, 2);
		scn.MoveCardsToHand(rohan);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetWoundsOn(grima));
		assertTrue(scn.ShadowPlayAvailable(rohan));

		// Aragorn is the only Free Peoples Man: the condition goes on him unasked.
		scn.ShadowPlayCard(rohan);
		assertAttachedTo(rohan, aragorn);
		assertEquals(2, scn.GetWoundsOn(grima));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheIsengardSarumanAloneIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var rohan = scn.GetShadowCard("rohan");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.MoveMinionsToTable(saruman);
		scn.MoveCardsToHand(rohan);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, saruman);
		assertTrue(scn.ShadowPlayAvailable(rohan));

		scn.ShadowPlayCard(rohan);
		assertAttachedTo(rohan, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheDunlandSarumanAloneIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var rohan = scn.GetShadowCard("rohan");
		var rabble = scn.GetShadowCard("rabble");

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.MoveMinionsToTable(rabble);
		scn.MoveCardsToHand(rohan);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, rabble);
		assertTrue(scn.ShadowPlayAvailable(rohan));

		scn.ShadowPlayCard(rohan);
		assertAttachedTo(rohan, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheSarumanFollowerAloneIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var rohan = scn.GetShadowCard("rohan");
		var follower = scn.GetShadowCard("follower");

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.MoveCardsToSupportArea(follower);
		scn.MoveCardsToHand(rohan);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, follower);
		assertTrue(scn.ShadowPlayAvailable(rohan));

		scn.ShadowPlayCard(rohan);
		assertAttachedTo(rohan, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachTimeTheFreePeoplesPlayerAssignsBearerEachAllyExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var elrond = scn.GetFreepsCard("elrond");
		var guma = scn.GetFreepsCard("guma");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli);
		scn.MoveAlliesToTable(elrond, guma, thrarin);

		var rohan = scn.GetShadowCard("rohan");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		var savage3 = scn.GetShadowCard("savage3");
		scn.AttachCardsTo(aragorn, rohan);

		scn.StartGame();
		assertAttachedTo(rohan, aragorn);
		// Thrarin, vitality 2, is exhausted: wounded after Elrond's heal at the start of the turn.
		scn.AddWoundsToChar(thrarin, 1);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage, savage2);
		scn.SkipToAssignments();

		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(guma));
		// Bearer and another Free Peoples Man in one answer: one assignment of bearer.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, savage},
				new PhysicalCardImpl[]{boromir, savage2});

		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(guma));
		// The exhausted ally cannot exert and takes no wound in its place.
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Aragorn beats his Savage 8 to 5, and Boromir his 7 to 5.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// A second move, to site 3, and a second assignment phase.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.MoveMinionsToTable(savage3);
		scn.ShadowRefreshDecision();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, savage3);
		// Elrond exerts again; Guma, now exhausted, does not.
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(guma));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
	}

	@Test
	public void NeitherAnotherManTheFreePeoplesPlayerAssignsNorBearerAssignedByTheShadowPlayerExertsAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveAlliesToTable(elrond);

		var rohan = scn.GetShadowCard("rohan");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		scn.AttachCardsTo(aragorn, rohan);

		scn.StartGame();
		assertAttachedTo(rohan, aragorn);
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage, savage2);
		scn.SkipToAssignments();
		assertEquals(0, scn.GetWoundsOn(elrond));

		scn.FreepsAssignToMinions(boromir, savage);
		assertEquals(0, scn.GetWoundsOn(elrond));

		// The leftover minion is the Shadow player's to assign, and he assigns it to bearer.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		scn.ShadowAssignToMinions(aragorn, savage2);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, savage2));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
