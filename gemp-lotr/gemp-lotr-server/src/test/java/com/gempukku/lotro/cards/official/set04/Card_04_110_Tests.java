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

public class Card_04_110_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arrows", "4_110");
					// Faramir, Son of Denethor: Ring-bound Man; an opponent may not play skirmish events or
					// abilities in his skirmishes
					put("faramir", "4_117");
					put("damrod", "4_114");     // Damrod, Ranger of Ithilien: Ring-bound Man; a Skirmish ability
					put("mablung", "4_127");    // Mablung, Soldier of Gondor: Ring-bound Man; a Skirmish ability
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, and a Hobbit
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man, and unbound
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, site 4; no text
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, vitality 2, site 6; Damage +1
					put("manofbree", "11_90");  // Man of Bree: [men] Man, vitality 1, site 4
					put("goblinman", "2_42");   // Goblin Man: [isengard] Orc, site 2, so not roaming at site 2
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArrowsThickintheAirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Arrows Thick in the Air
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Archery or Skirmish
		 * Game Text: <b>Archery</b> <i>or</i> <b>Skirmish:</b> Exert 2 Ring-bound Men to wound each roaming minion.
		*/

		var scn = GetScenario();

		var arrows = scn.GetFreepsCard("arrows");

		assertEquals("Arrows Thick in the Air", arrows.getBlueprint().getTitle());
		assertNull(arrows.getBlueprint().getSubtitle());
		assertFalse(arrows.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, arrows.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, arrows.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, arrows.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(arrows, Timeword.ARCHERY));
		assertTrue(scn.HasTimeword(arrows, Timeword.SKIRMISH));
		assertEquals(3, arrows.getBlueprint().getTwilightCost());
	}

	@Test
	public void InTheArcheryPhaseTwoRingBoundMenExertToWoundEachRoamingMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrows = scn.GetFreepsCard("arrows");
		var faramir = scn.GetFreepsCard("faramir");
		var damrod = scn.GetFreepsCard("damrod");
		var mablung = scn.GetFreepsCard("mablung");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");
		var sentry = scn.GetShadowCard("sentry");
		var manofbree = scn.GetShadowCard("manofbree");
		var goblinman = scn.GetShadowCard("goblinman");
		scn.MoveCompanionsToTable(faramir, damrod, mablung, sam, aragorn);
		scn.MoveCardsToHand(arrows);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, sentry, manofbree, goblinman);

		scn.SkipToPhase(Phase.ARCHERY);
		// At site 2: three minions of higher site numbers are roaming, Goblin Man (site 2) is not.
		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(sentry, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(manofbree, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(goblinman, Keyword.ROAMING));
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertEquals(0, scn.GetWoundsOn(damrod));
		assertEquals(0, scn.GetWoundsOn(mablung));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(goblinman));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(arrows));

		scn.FreepsPlayCard(arrows);
		// Exactly two of the Ring-bound Men; neither the Ring-bound Hobbits nor the unbound Man.
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(faramir, damrod, mablung));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, aragorn, frodo));
		scn.FreepsChooseCards(faramir, damrod);

		assertEquals(1, scn.GetWoundsOn(faramir));
		assertEquals(1, scn.GetWoundsOn(damrod));
		assertEquals(0, scn.GetWoundsOn(mablung));
		// One wound on each roaming minion: the Man of Bree, of vitality 1, is killed by his.
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertInDiscard(manofbree, arrows);
		assertEquals(0, scn.GetWoundsOn(goblinman));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void InASkirmishItWoundsRoamingMinionsInOtherSkirmishesToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrows = scn.GetFreepsCard("arrows");
		var faramir = scn.GetFreepsCard("faramir");
		var damrod = scn.GetFreepsCard("damrod");
		var mablung = scn.GetFreepsCard("mablung");
		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");
		var sentry = scn.GetShadowCard("sentry");
		scn.MoveCompanionsToTable(faramir, damrod, mablung);
		scn.MoveCardsToHand(arrows);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, sentry);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{faramir, troop}, new PhysicalCardImpl[]{frodo, sentry});
		scn.FreepsResolveSkirmish(faramir);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(damrod));
		assertEquals(0, scn.GetWoundsOn(mablung));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsPlayCard(arrows);
		scn.FreepsChooseCards(damrod, mablung);
		assertEquals(1, scn.GetWoundsOn(damrod));
		assertEquals(1, scn.GetWoundsOn(mablung));
		// The minion of this skirmish and the one waiting for Frodo's.
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertInDiscard(arrows);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileOnlyOneRingBoundManCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrows = scn.GetFreepsCard("arrows");
		var faramir = scn.GetFreepsCard("faramir");
		var damrod = scn.GetFreepsCard("damrod");
		var mablung = scn.GetFreepsCard("mablung");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(faramir, damrod, mablung, sam, aragorn);
		scn.MoveCardsToHand(arrows);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		// Vitality 3 with 2 wounds: exhausted. Sam and Aragorn could exert, but are no Ring-bound Men.
		scn.AddWoundsToChar(damrod, 2);
		scn.AddWoundsToChar(mablung, 2);
		assertEquals(2, scn.GetWoundsOn(damrod));
		assertEquals(2, scn.GetWoundsOn(mablung));

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(arrows));

		// A second Ring-bound Man able to exert, and it can be played.
		scn.RemoveWoundsFromChar(damrod, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(arrows));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void WithNoRoamingMinionItIsPlayedAndWoundsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrows = scn.GetFreepsCard("arrows");
		var faramir = scn.GetFreepsCard("faramir");
		var damrod = scn.GetFreepsCard("damrod");
		var mablung = scn.GetFreepsCard("mablung");
		var goblinman = scn.GetShadowCard("goblinman");
		scn.MoveCompanionsToTable(faramir, damrod, mablung);
		scn.MoveCardsToHand(arrows);

		scn.StartGame();
		scn.MoveMinionsToTable(goblinman);

		scn.SkipToPhase(Phase.ARCHERY);
		assertFalse(scn.HasKeyword(goblinman, Keyword.ROAMING));
		assertEquals(0, scn.GetWoundsOn(goblinman));
		assertTrue(scn.FreepsPlayAvailable(arrows));

		scn.FreepsPlayCard(arrows);
		scn.FreepsChooseCards(faramir, mablung);
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertEquals(1, scn.GetWoundsOn(mablung));
		assertEquals(0, scn.GetWoundsOn(damrod));
		assertEquals(0, scn.GetWoundsOn(goblinman));
		assertInDiscard(arrows);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}
}
