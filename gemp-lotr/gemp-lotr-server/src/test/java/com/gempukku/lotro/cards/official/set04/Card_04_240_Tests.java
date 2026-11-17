package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_04_240_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fear", "4_240");
					put("axeman", "4_224");     // Easterling Axeman: [raider] Easterling Man, vitality 2; strength +3 and fierce only at 2 burdens
					put("soldier", "4_220");    // Desert Soldier: [raider] Southron Man, vitality 2; a start-of-skirmish trigger only
					put("footmen", "6_3");      // Dunlending Footmen: [dunland] Man, vitality 2; a cost text only
					put("rearguard", "103_43"); // Bladetusk Rearguard: [raider] artifact that its maneuver ability makes a Southron minion who is no Man
					put("hunter", "4_188");     // Uruk Hunter: "Unbound Hobbits may not be discarded"

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf; a skirmish ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit, Ring-bound and not the Ring-bearer
					put("merry", "1_303");      // Merry, From O'er the Brandywine: unbound Hobbit; +2 only while bearing a weapon
					put("pippin", "1_306");     // Pippin, Friend to Frodo: unbound Hobbit; a bar on discarding tales only
					put("businessman", "13_157"); // Westfarthing Businessman: unbound Hobbit; +1 per [shire] card with a token
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: a Dwarf ally, no companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NewFearStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: New Fear
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 4
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: <b>Regroup:</b> Exert a [raider] Man to add a burden for each companion over 4. The Free Peoples player may discard 2 companions (except the Ring-bearer) to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fear");

		assertEquals("New Fear", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(4, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RegroupEventExertsARaiderManToAddABurdenForEachCompanionOverFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fear = scn.GetShadowCard("fear");
		var axeman = scn.GetShadowCard("axeman");
		var soldier = scn.GetShadowCard("soldier");
		var footmen = scn.GetShadowCard("footmen");
		var rearguard = scn.GetShadowCard("rearguard");
		scn.MoveCardsToHand(fear);
		scn.MoveCardsToSupportArea(rearguard);

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		// Six companions, Frodo among them: two over 4.
		scn.MoveCompanionsToTable(aragorn, gimli, sam, merry, pippin);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman, soldier, footmen);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();
		scn.ShadowUseCardAction(rearguard);
		// The Rearguard is now a [raider] Southron minion who is no Man, and could exert.
		assertTrue(scn.IsType(rearguard, CardType.MINION));
		assertFalse(scn.IsRace(rearguard, Race.MAN));

		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(10);
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(footmen));
		assertEquals(0, scn.GetWoundsOn(rearguard));
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(fear));
		scn.ShadowPlayCard(fear);
		// An Easterling and a Southron [raider] Man are offered; a [dunland] Man and a [raider] minion who is no Man are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(axeman, soldier));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(footmen, rearguard));
		scn.ShadowChooseCard(axeman);
		assertEquals(1, scn.GetWoundsOn(axeman));
		assertEquals(0, scn.GetWoundsOn(soldier));

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		// 6 companions - 4 = 2 burdens; nobody discarded.
		assertEquals(2, scn.GetBurdens());
		assertInZone(Zone.FREE_CHARACTERS, aragorn, gimli, sam, merry, pippin);
		assertInZone(Zone.DISCARD, fear);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithFiveCompanionsAndAnAllyOneBurdenIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fear = scn.GetShadowCard("fear");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(fear);

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var thrarin = scn.GetFreepsCard("thrarin");
		// Five companions, Frodo among them, and an ally, who is no companion.
		scn.MoveCompanionsToTable(aragorn, gimli, sam, merry);
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(10);
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(axeman));
		scn.FreepsPass();

		scn.ShadowPlayCard(fear);
		// The one [raider] Man exerts unasked.
		assertEquals(1, scn.GetWoundsOn(axeman));
		scn.FreepsChooseNo();

		// 5 companions - 4 = 1 burden; the ally adds nothing.
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.DISCARD, fear);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithFourCompanionsTheExertionIsPaidAndNoBurdenAddedNorPreventionAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fear = scn.GetShadowCard("fear");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(fear);

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var thrarin = scn.GetFreepsCard("thrarin");
		// Four companions, Frodo among them, and an ally: none over 4.
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(10);
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(axeman));
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(fear));
		scn.ShadowPlayCard(fear);
		assertEquals(1, scn.GetWoundsOn(axeman));

		// No burden is to be added, so there is nothing to prevent.
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.DISCARD, fear);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotPlayableWhileTheOnlyRaiderManIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fear = scn.GetShadowCard("fear");
		var axeman = scn.GetShadowCard("axeman");
		var footmen = scn.GetShadowCard("footmen");
		var rearguard = scn.GetShadowCard("rearguard");
		scn.MoveCardsToHand(fear);
		scn.MoveCardsToSupportArea(rearguard);

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, gimli, sam, merry);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman, footmen);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();
		scn.ShadowUseCardAction(rearguard);
		assertTrue(scn.IsType(rearguard, CardType.MINION));

		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(10);
		// Vitality 2: one wound exhausts the Axeman.  The [dunland] Man and the Southron who is no Man could exert.
		scn.AddWoundsToChar(axeman, 1);
		assertEquals(1, scn.GetWoundsOn(axeman));
		assertEquals(0, scn.GetWoundsOn(footmen));
		assertEquals(0, scn.GetWoundsOn(rearguard));
		scn.FreepsPass();

		assertFalse(scn.ShadowPlayAvailable(fear));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void FreePeoplesPlayerMayDiscardTwoCompanionsButNotTheRingBearerToPreventTheBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fear = scn.GetShadowCard("fear");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(fear);

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn, gimli, sam, merry, pippin);
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(10);
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(axeman));
		scn.FreepsPass();

		scn.ShadowPlayCard(fear);
		assertEquals(1, scn.GetWoundsOn(axeman));

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();
		// Companions of every culture and race offered; the Ring-bearer and the ally are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli, sam, merry, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, thrarin));
		assertEquals(2, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(aragorn, gimli);

		assertInZone(Zone.DISCARD, aragorn, gimli);
		assertInZone(Zone.FREE_CHARACTERS, sam, merry, pippin);
		// Prevented: no burden, and the exertion stays paid.
		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(axeman));
		assertInZone(Zone.DISCARD, fear);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ThePreventionIsNotOfferedWhenTwoCompanionsCannotBeDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fear = scn.GetShadowCard("fear");
		var axeman = scn.GetShadowCard("axeman");
		var hunter = scn.GetShadowCard("hunter");
		scn.MoveCardsToHand(fear);

		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var businessman = scn.GetFreepsCard("businessman");
		// Five companions: under the Uruk Hunter the three unbound Hobbits may not be discarded,
		// and the Ring-bearer is excepted, so Aragorn is the only one who could be.
		scn.MoveCompanionsToTable(aragorn, merry, pippin, businessman);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman, hunter);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(10);
		assertEquals(0, scn.GetBurdens());
		scn.FreepsPass();

		scn.ShadowPlayCard(fear);
		assertEquals(1, scn.GetWoundsOn(axeman));

		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.FREE_CHARACTERS, aragorn, merry, pippin, businessman);
		assertInZone(Zone.DISCARD, fear);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
