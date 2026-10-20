package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_004_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hospitality", "103_4");
					put("hospitality2", "103_4");
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: unbound [gondor] Man, strength 8
					put("gimli", "1_13");           // Gimli, Son of Gloin: unbound [dwarven] Dwarf, strength 6
					put("pippin", "1_306");         // Pippin, Friend to Frodo: unbound [shire] Hobbit
					put("sam", "1_311");            // Sam, Son of Hamfast: Ring-bound, and not the Ring-bearer
					put("gaffer", "1_291");         // The Gaffer: [shire] Hobbit ally
					put("aragornspipe", "1_91");    // Aragorn's Pipe: [gondor] pipe, on a [gondor] companion
					put("gimlispipe", "3_2");       // Gimli's Pipe: [dwarven] pipe, on Gimli
					put("frodospipe", "3_107");     // Frodo's Pipe: [shire] pipe, on Frodo
					put("bilbospipe", "1_285");     // Bilbo's Pipe: [shire] pipe, on any Hobbit
					put("gafferspipe", "1_292");    // The Gaffer's Pipe: [shire] pipe, on any Hobbit
					put("agents", "6_57");          // Agents of Orthanc: strength 9, +2 and damage +1 for each Free Peoples culture over 2
					put("agents2", "6_57");
					put("stalker", "5_72");         // Desert Stalker: its maneuver ability needs 4 Free Peoples cultures spotted
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SarumansHospitalityStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Saruman's Hospitality
		 * Unique: 2
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype:
		 * Strength: +1
		 * Game Text: Pipeweed.
		 * 	Bearer must be an unbound companion bearing a pipe.
		 * 	Each time the fellowship moves, spot 3 pipes of different cultures to make the number of Free Peoples
		 * 	cultures that can be counted -1 until the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hospitality");

		assertEquals("Saruman's Hospitality", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.PIPEWEED));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnAnUnboundCompanionBearingAPipeAndOnNoOneElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hospitality = scn.GetFreepsCard("hospitality");
		var hospitality2 = scn.GetFreepsCard("hospitality2");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var gaffer = scn.GetFreepsCard("gaffer");
		scn.MoveCompanionsToTable(aragorn, gimli, pippin, sam);
		scn.MoveAlliesToTable(gaffer);
		scn.AttachCardsTo(aragorn, scn.GetFreepsCard("aragornspipe"));
		scn.AttachCardsTo(gimli, scn.GetFreepsCard("gimlispipe"));
		// Ring-bound companions with a pipe each: the Ring-bearer, and one who is not.
		scn.AttachCardsTo(frodo, scn.GetFreepsCard("frodospipe"));
		scn.AttachCardsTo(sam, scn.GetFreepsCard("bilbospipe"));
		// An ally with a pipe.
		scn.AttachCardsTo(gaffer, scn.GetFreepsCard("gafferspipe"));
		// An unbound companion who bears pipeweed and no pipe: the other copy of this card, cheated onto him.
		scn.AttachCardsTo(pippin, hospitality2);
		scn.MoveCardsToHand(hospitality);

		scn.StartGame();

		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.FreepsPlayAvailable(hospitality));

		scn.FreepsPlayCard(hospitality);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam, gaffer, pippin));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(aragorn);
		assertAttachedTo(hospitality, aragorn);
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilAnUnboundCompanionBearsAPipe() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hospitality = scn.GetFreepsCard("hospitality");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);
		// The only pipes in play are on the Ring-bound companions.
		scn.AttachCardsTo(scn.GetRingBearer(), scn.GetFreepsCard("frodospipe"));
		scn.AttachCardsTo(sam, scn.GetFreepsCard("bilbospipe"));
		scn.MoveCardsToHand(hospitality, aragornspipe);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(hospitality));

		scn.FreepsPlayCard(aragornspipe);
		assertAttachedTo(aragornspipe, aragorn);
		assertTrue(scn.FreepsPlayAvailable(hospitality));

		// Aragorn is now the one unbound companion bearing a pipe, so nobody is asked where it goes.
		scn.FreepsPlayCard(hospitality);
		assertAttachedTo(hospitality, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachMoveWithPipesOfThreeCulturesMakesOneFewerFreePeoplesCultureCountUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hospitality = scn.GetFreepsCard("hospitality");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var gaffer = scn.GetFreepsCard("gaffer");
		var gimlispipe = scn.GetFreepsCard("gimlispipe");
		var agents = scn.GetShadowCard("agents");
		var agents2 = scn.GetShadowCard("agents2");
		var stalker = scn.GetShadowCard("stalker");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(gaffer);
		scn.AttachCardsTo(aragorn, scn.GetFreepsCard("aragornspipe"), hospitality);
		// A pipe on an ally counts as well as one on a companion.
		scn.AttachCardsTo(gaffer, scn.GetFreepsCard("gafferspipe"));
		scn.MoveCardsToHand(gimlispipe);

		scn.StartGame();
		scn.MoveMinionsToTable(agents, agents2, stalker);
		scn.FreepsRefreshDecision();

		// Four cultures: [shire] (Frodo, The Gaffer), [gondor] (Aragorn), [dwarven] (Gimli), [gandalf] (this card).
		assertEquals(4, scn.GetFreepsCultureCount());
		// 9 + 2 for each of the 2 cultures over 2
		assertEquals(13, scn.GetStrength(agents));

		// The pipe of a third culture arrives by legal play: [gondor], [shire] and now [dwarven].
		scn.FreepsPlayCard(gimlispipe);
		assertAttachedTo(gimlispipe, gimli);
		assertEquals(4, scn.GetFreepsCultureCount());

		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetFreepsCultureCount());
		// 9 + 2 for the 1 culture over 2
		assertEquals(11, scn.GetStrength(agents));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		// "Spot 4 Free Peoples cultures" finds only 3.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(stalker));

		scn.ShadowPass();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, agents }, new PhysicalCardImpl[] { gimli, agents2 });
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		scn.PassCurrentPhaseActions();
		// 6 against 11: Gimli loses and takes 1 wound + 1 for the damage +1 of the 1 culture over 2
		assertEquals(2, scn.GetWoundsOn(gimli));

		// Still one fewer in a second skirmish, the last thing before the regroup phase.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertEquals(3, scn.GetFreepsCultureCount());
		assertEquals(11, scn.GetStrength(agents));

		scn.PassCurrentPhaseActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(4, scn.GetFreepsCultureCount());
		assertEquals(13, scn.GetStrength(agents));

		// The fellowship moves a second time.
		scn.PassRegroupActions();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetFreepsCultureCount());
		assertEquals(11, scn.GetStrength(agents));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ThreePipesOfTwoCulturesAndAPipeweedOfAThirdLeaveTheCountAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hospitality = scn.GetFreepsCard("hospitality");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var gaffer = scn.GetFreepsCard("gaffer");
		var agents = scn.GetShadowCard("agents");
		var stalker = scn.GetShadowCard("stalker");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(gaffer);
		// Pipes: one [gondor] and two [shire]. This card is a [gandalf] pipeweed, and no pipe.
		scn.AttachCardsTo(aragorn, scn.GetFreepsCard("aragornspipe"), hospitality);
		scn.AttachCardsTo(scn.GetRingBearer(), scn.GetFreepsCard("frodospipe"));
		scn.AttachCardsTo(gaffer, scn.GetFreepsCard("gafferspipe"));

		scn.StartGame();
		scn.MoveMinionsToTable(agents, stalker);

		assertEquals(4, scn.GetFreepsCultureCount());
		assertEquals(13, scn.GetStrength(agents));

		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(4, scn.GetFreepsCultureCount());
		assertEquals(13, scn.GetStrength(agents));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(stalker));
	}

	@Test
	public void TwoCopiesEachMakeOneFewerCultureCount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var gaffer = scn.GetFreepsCard("gaffer");
		var agents = scn.GetShadowCard("agents");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(gaffer);
		scn.AttachCardsTo(aragorn, scn.GetFreepsCard("aragornspipe"), scn.GetFreepsCard("hospitality"));
		scn.AttachCardsTo(gimli, scn.GetFreepsCard("gimlispipe"), scn.GetFreepsCard("hospitality2"));
		scn.AttachCardsTo(gaffer, scn.GetFreepsCard("gafferspipe"));

		scn.StartGame();
		scn.MoveMinionsToTable(agents);

		assertEquals(4, scn.GetFreepsCultureCount());
		assertEquals(13, scn.GetStrength(agents));

		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// 4 - 1 for each copy
		assertEquals(2, scn.GetFreepsCultureCount());
		// No culture over 2 is left to count.
		assertEquals(9, scn.GetStrength(agents));
	}
}
