package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_016_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The skirmishes of
	// turn 1 are at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wrath", "31_16");
					put("balin", "30_2");       // Balin, Brother of Dwalin: a [dwarven] companion, strength 7; a fellowship ability only
					put("gloin", "30_11");      // Glóin, Father of Gimli: a [dwarven] companion, strength 6; a skirmish ability used only on purpose
					put("nori", "30_15");       // Nori, Of the Blue Mountains: a [dwarven] companion, strength 7; a maneuver ability used only on purpose

					// Beorn, Skin-Changer: a [gandalf] follower, strength +2; his Aid (exert Gandalf) and a win trigger
					put("beorn", "31_13");
					// Dori: a [dwarven] follower, strength +1; Aid - exert Bilbo; a bar on overwhelming
					put("dori", "30_8");
					// Bifur: a [dwarven] follower, strength +1; Aid - exert a companion; a fierce-win trigger
					put("bifur", "30_5");
					// Watch and Wait: a [gandalf] condition on a companion who is not the Ring-bearer; a win trigger
					// that removes a burden (none laid)
					put("watch", "12_35");
					put("axe", "31_1");         // Dwarven Axe: a [dwarven] hand weapon, strength +2; damage +1 at a mountain (none here)

					// Gandalf, Friend of Thorin: a Gandalf to exert for The Eagles Are Coming
					put("gandalf", "30_26");
					// The Eagles Are Coming: "Exert Gandalf to play a [gandalf] follower from your draw deck or
					// discard pile. You may attach a [gandalf] follower from your support area to a companion"
					put("eagles", "31_14");
					// Alatar, Final Envoy: a [gandalf] follower for The Eagles Are Coming to play
					put("alatar", "13_28");

					put("runner1", "31_30");    // Goblin Runner: a [moria] Orc, strength 5, vitality 1
					put("runner2", "31_30");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void HisWrathWasRedoubledStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: His Wrath Was Redoubled
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Assignment
		 * Game Text: <b>Spell</b>.
		 * 	Each [dwarven] companion bearing a follower is strength +2 (and <b>defender +1</b> if it is a [gandalf] follower) until the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wrath");

		assertEquals("His Wrath Was Redoubled", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ASSIGNMENT));
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedInTheAssignmentPhaseEachDwarvenCompanionBearingAFollowerIsStrongerUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wrath = scn.GetFreepsCard("wrath");
		var balin = scn.GetFreepsCard("balin");
		var gloin = scn.GetFreepsCard("gloin");
		var nori = scn.GetFreepsCard("nori");
		var bilbo = scn.GetRingBearer();
		var beorn = scn.GetFreepsCard("beorn");
		var dori = scn.GetFreepsCard("dori");
		var bifur = scn.GetFreepsCard("bifur");
		var watch = scn.GetFreepsCard("watch");
		var axe = scn.GetFreepsCard("axe");
		var alatar = scn.GetFreepsCard("alatar");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(balin, gloin, nori);

		scn.StartGame();
		scn.AttachCardsTo(balin, beorn);
		scn.AttachCardsTo(gloin, dori, watch);
		scn.AttachCardsTo(nori, axe);
		scn.AttachCardsTo(bilbo, bifur, alatar);
		scn.MoveCardsToHand(wrath);
		scn.MoveMinionsToTable(runner1, runner2);
		assertAttachedTo(beorn, balin);
		assertAttachedTo(dori, gloin);
		assertAttachedTo(watch, gloin);
		assertAttachedTo(axe, nori);
		assertAttachedTo(bifur, bilbo);
		assertAttachedTo(alatar, bilbo);
		assertInHand(wrath);

		// Printed strength and what each bears: Balin 7 + 2 (Beorn), Glóin 6 + 1 (Dori), Nori 7 + 2 (the Axe),
		// Bilbo 3 + 1 (Bifur).
		assertEquals(9, scn.GetStrength(balin));
		assertEquals(7, scn.GetStrength(gloin));
		assertEquals(9, scn.GetStrength(nori));
		assertEquals(4, scn.GetStrength(bilbo));
		assertEquals(0, scn.GetKeywordCount(balin, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(gloin, Keyword.DEFENDER));

		// Not in the maneuver phase. The followers' Aids are offered there first, and declined.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(wrath));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(wrath));
		scn.FreepsPlayCard(wrath);
		assertInDiscard(wrath);

		// Balin bears a [gandalf] follower: +2 and defender +1. Glóin bears a [dwarven] follower (and a [gandalf]
		// condition): +2 alone. Nori bears no follower, and Bilbo is no [dwarven] companion: unchanged.
		assertEquals(11, scn.GetStrength(balin));
		assertEquals(1, scn.GetKeywordCount(balin, Keyword.DEFENDER));
		assertEquals(9, scn.GetStrength(gloin));
		assertEquals(0, scn.GetKeywordCount(gloin, Keyword.DEFENDER));
		assertEquals(9, scn.GetStrength(nori));
		assertEquals(0, scn.GetKeywordCount(nori, Keyword.DEFENDER));
		assertEquals(4, scn.GetStrength(bilbo));
		assertEquals(0, scn.GetKeywordCount(bilbo, Keyword.DEFENDER));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gloin, runner1},
				new PhysicalCardImpl[]{balin, runner2});
		scn.FreepsResolveSkirmish(gloin);
		scn.PassSkirmishActions();

		// Glóin's skirmish is over; in Balin's, which follows, the bonus still holds.
		scn.FreepsResolveSkirmish(balin);
		assertTrue(scn.IsCharSkirmishing(balin));
		assertEquals(11, scn.GetStrength(balin));
		assertEquals(1, scn.GetKeywordCount(balin, Keyword.DEFENDER));
		assertEquals(9, scn.GetStrength(gloin));
		scn.PassSkirmishActions();
		// Beorn's "each time bearer wins a skirmish" offer: declined.
		scn.FreepsDeclineOptionalTrigger();

		// The regroup phase: the followers are still borne, and the bonus is gone.
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertAttachedTo(beorn, balin);
		assertAttachedTo(dori, gloin);
		assertEquals(9, scn.GetStrength(balin));
		assertEquals(0, scn.GetKeywordCount(balin, Keyword.DEFENDER));
		assertEquals(7, scn.GetStrength(gloin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionWhoComesToBearAFollowerAfterThePlayIsNotStrengthened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wrath = scn.GetFreepsCard("wrath");
		var eagles = scn.GetFreepsCard("eagles");
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		var gandalf = scn.GetFreepsCard("gandalf");
		var beorn = scn.GetFreepsCard("beorn");
		var dori = scn.GetFreepsCard("dori");
		var alatar = scn.GetFreepsCard("alatar");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(balin, nori, gandalf);
		scn.MoveCardsToSupportArea(beorn);

		scn.StartGame();
		scn.AttachCardsTo(balin, dori);
		scn.MoveCardsToHand(wrath, eagles);
		scn.MoveCardsToDiscard(alatar);
		scn.MoveMinionsToTable(runner1, runner2);
		assertAttachedTo(dori, balin);
		assertInZone(Zone.SUPPORT, beorn);
		assertInDiscard(alatar);
		assertInHand(wrath, eagles);
		assertEquals(0, scn.GetWoundsOn(gandalf));

		// Balin 7 + 1 (Dori); Nori 7.
		assertEquals(8, scn.GetStrength(balin));
		assertEquals(7, scn.GetStrength(nori));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPlayCard(wrath);
		assertEquals(10, scn.GetStrength(balin));
		assertEquals(7, scn.GetStrength(nori));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{nori, runner1},
				new PhysicalCardImpl[]{balin, runner2});
		scn.FreepsResolveSkirmish(nori);

		// The Eagles Are Coming: Gandalf exerts, Alatar is played from the discard pile, and Beorn goes to Nori.
		assertTrue(scn.FreepsPlayAvailable(eagles));
		scn.FreepsPlayCard(eagles);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		// Not asked which pile: the draw deck holds no [gandalf] follower, so Alatar is played unasked from the discard pile.
		assertInZone(Zone.SUPPORT, alatar);
		scn.FreepsChooseYes();
		scn.FreepsChooseCard(beorn);
		scn.FreepsChooseCard(nori);
		assertAttachedTo(beorn, nori);

		// Nori now bears a [gandalf] follower, but was not one of those the event reached: 7 + 2 (Beorn), not 11,
		// and no defender. Balin keeps his +2.
		assertEquals(9, scn.GetStrength(nori));
		assertEquals(0, scn.GetKeywordCount(nori, Keyword.DEFENDER));
		assertEquals(10, scn.GetStrength(balin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
