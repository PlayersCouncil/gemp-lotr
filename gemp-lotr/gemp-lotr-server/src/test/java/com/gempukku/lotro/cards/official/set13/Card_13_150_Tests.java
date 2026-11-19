package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_150_Tests
{

	// Galadriel, Bearer of Wisdom 9_14 is the Ring-bearer: a Ring-bound Elf, so the Ring-bearer is no Ring-bound
	// Hobbit here.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gamgee", "13_150");
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound [shire] Hobbit, cost 2; a fellowship ability and a response when Frodo dies
					put("bilbo", "12_119");     // Bilbo, Melancholy Hobbit: a Ring-bound [shire] Hobbit of another title, cost 2; a trigger in his own skirmishes
					put("faramir", "4_117");    // Faramir, Son of Denethor: a Ring-bound Man, strength 7; a bar in his own skirmishes
					put("merry", "1_302");      // Merry, Friend to Sam: an unbound Hobbit, strength 3; a skirmish ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: a minion whose text is about being played
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GaladrielRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FrodoGamgeeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Frodo Gamgee, Son of Samwise
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Follower
		 * Subtype:
		 * Vitality: +1
		 * Aid: Add 2 burdens
		 * Game Text: <b>Aid</b> – Add 2 burdens. <helper>(At the start of the maneuver phase, you may add 2 burdens to transfer this to a companion.)</helper><br>To play, spot a Ring-bound Hobbit.<br>Bearer is strength +1 for each Ring-bound Hobbit you can spot.
		*/

		var scn = GetScenario();

		var gamgee = scn.GetFreepsCard("gamgee");

		assertEquals("Frodo Gamgee", gamgee.getBlueprint().getTitle());
		assertEquals("Son of Samwise", gamgee.getBlueprint().getSubtitle());
		assertTrue(gamgee.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, gamgee.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, gamgee.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, gamgee.getBlueprint().getCardType());
		assertEquals(1, gamgee.getBlueprint().getTwilightCost());
		assertEquals(1, gamgee.getBlueprint().getVitality());
	}

	@Test
	public void CannotBePlayedUntilARingBoundHobbitIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gamgee = scn.GetFreepsCard("gamgee");
		var sam = scn.GetFreepsCard("sam");
		var faramir = scn.GetFreepsCard("faramir");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(faramir, merry);
		scn.MoveCardsToHand(gamgee, sam);

		scn.StartGame();
		assertInZone(Zone.HAND, gamgee, sam);
		// Galadriel and Faramir are Ring-bound and no Hobbits; Merry is a Hobbit and unbound.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(gamgee));

		scn.FreepsPlayCard(sam);
		assertTrue(scn.FreepsPlayAvailable(gamgee));
		scn.FreepsPlayCard(gamgee);
		assertInZone(Zone.SUPPORT, gamgee);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayableWithBilboAsTheOneRingBoundHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gamgee = scn.GetFreepsCard("gamgee");
		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveCompanionsToTable(bilbo);
		scn.MoveCardsToHand(gamgee);

		scn.StartGame();
		assertInZone(Zone.HAND, gamgee);
		assertTrue(scn.FreepsPlayAvailable(gamgee));
		scn.FreepsPlayCard(gamgee);
		assertInZone(Zone.SUPPORT, gamgee);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BearerIsStrengthPlusOneForEachRingBoundHobbitAndNobodyIsWhileItIsInTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gamgee = scn.GetFreepsCard("gamgee");
		var sam = scn.GetFreepsCard("sam");
		var bilbo = scn.GetFreepsCard("bilbo");
		var faramir = scn.GetFreepsCard("faramir");
		var merry = scn.GetFreepsCard("merry");
		var galadriel = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam, faramir, merry);
		scn.MoveCardsToSupportArea(gamgee);
		scn.MoveCardsToHand(bilbo);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, gamgee);
		assertInZone(Zone.HAND, bilbo);
		// Borne by nobody: no strength changes.  Galadriel is 3 + 1 for The Ruling Ring.
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(7, scn.GetStrength(faramir));
		assertEquals(4, scn.GetStrength(galadriel));

		scn.AttachCardsTo(merry, gamgee);
		assertAttachedTo(gamgee, merry);
		// One Ring-bound Hobbit, Sam: Faramir and Galadriel are Ring-bound and no Hobbits, Merry a Hobbit and unbound.
		// The bearer alone is strengthened.
		assertEquals(4, scn.GetStrength(merry));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(7, scn.GetStrength(faramir));
		assertEquals(4, scn.GetStrength(galadriel));

		// A second Ring-bound Hobbit played.
		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(bilbo);
		assertEquals(5, scn.GetStrength(merry));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(3, scn.GetStrength(bilbo));
		assertEquals(7, scn.GetStrength(faramir));

		// Borne by a Ring-bound Hobbit himself, he is counted as well: Sam and Bilbo, 3 + 2.
		scn.AttachCardsTo(sam, gamgee);
		assertAttachedTo(gamgee, sam);
		assertEquals(5, scn.GetStrength(sam));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(3, scn.GetStrength(bilbo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AidAddsTwoBurdensToTransferItToACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gamgee = scn.GetFreepsCard("gamgee");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(sam, merry);
		scn.MoveCardsToSupportArea(gamgee);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, gamgee);
		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetBurdens());
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gamgee));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, merry));
		scn.FreepsChooseCard(merry);
		assertAttachedTo(gamgee, merry);
		// 3 + 1 for Sam, the one Ring-bound Hobbit.
		assertEquals(4, scn.GetStrength(merry));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
